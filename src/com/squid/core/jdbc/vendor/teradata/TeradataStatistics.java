/*******************************************************************************
 * Copyright © Squid Solutions, 2016
 *
 * This file is part of Open Bouquet software.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * There is a special FOSS exception to the terms and conditions of the
 * licenses as they are applied to this program. See LICENSE.txt in
 * the directory of this program distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Squid Solutions also offers commercial licenses with additional warranties,
 * professional functionalities or services. If you purchase a commercial
 * license, then it supersedes and replaces any other agreement between
 * you and Squid Solutions (above licenses and LICENSE.txt included).
 * See http://www.squidsolutions.com/EnterpriseBouquet/
 *******************************************************************************/
package com.squid.core.jdbc.vendor.teradata;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squid.core.database.impl.DataSourceReliable;
import com.squid.core.database.model.Column;
import com.squid.core.database.model.Schema;
import com.squid.core.database.model.Table;
import com.squid.core.database.statistics.ColumnStatistics;
import com.squid.core.database.statistics.DatabaseStatistics;
import com.squid.core.database.statistics.ObjectStatistics;
import com.squid.core.database.statistics.PartitionInfo;
import com.squid.core.domain.IDomain;

/**
 * extend postgres support: - handles min/max statistics using histogram -
 * handles partition info
 *
 * @author sergefantino
 *
 */
public class TeradataStatistics extends DatabaseStatistics {

	static final Logger logger = LoggerFactory.getLogger(TeradataStatistics.class);

	private DataSourceReliable ds;

	public TeradataStatistics(DataSourceReliable ds) {
		super(ds);
		this.ds = ds;
	}

	@Override
	protected void computeColumnsStatistics(Table table, Connection connection) {
		try {
			Statement statement = null;
			try {
				ObjectStatistics tableStatistics = getStatistics(table, connection);
				float tableSize = tableStatistics != null ? tableStatistics.getSize() : -1;
				statement = connection.createStatement();
				String sql = "select attname, n_distinct, histogram_bounds from pg_stats where schemaname='"
						+ table.getSchema().getName() + "' and tablename='" + table.getName() + "'";
				ResultSet resultset = statement.executeQuery(sql);
				while (resultset.next()) {
					String attname = resultset.getString(1);
					float n_distinct = resultset.getFloat(2);
					Array histogram = resultset.getArray(3);
					Column col = table.findColumnByName(attname);
					if (col != null) {
						/*
						 * If greater than zero, the estimated number of
						 * distinct values in the column. If less than zero, the
						 * negative of the number of distinct values divided by
						 * the number of rows. (The negated form is used when
						 * ANALYZE believes that the number of distinct values
						 * is likely to increase as the table grows; the
						 * positive form is used when the column seems to have a
						 * fixed number of possible values.) For example, -1
						 * indicates a unique column in which the number of
						 * distinct values is the same as the number of rows.
						 */
						float stats = n_distinct > 0 ? n_distinct : (-n_distinct * tableSize);
						putStatistics(col, new ColumnStatistics(stats));
						// bug with Array.getArray()
						/*
						 * if (histogram==null) { putStatistics(col,new
						 * ColumnStatistics(stats)); } else { try { int x =
						 * histogram.getBaseType(); Object[] data = (Object[])
						 * histogram.getArray(); Object min =
						 * data.length>1?data[0]:null; Object max =
						 * data.length>1?data[1]:null; putStatistics(col,new
						 * ColumnStatistics(stats,min,max)); } catch (Exception
						 * e) { putStatistics(col,new ColumnStatistics(stats));
						 * } }
						 */
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				if (statement != null) {
					statement.close();
				}
			}
		} catch (SQLException e) {
			logger.info(e.getLocalizedMessage());
		}
	}

	private ConcurrentHashMap<Table, PartitionInfo> partitionInfos = new ConcurrentHashMap<Table, PartitionInfo>();

	@Override
	public boolean isPartitionTable(Table table) {
		PartitionInfo info = getPartitionInfo(table);
		return info != null && info.hasPartition();
	}

	@Override
	public PartitionInfo getPartitionInfo(Table table) {
		if (table == null) {
			return null;
		}
		PartitionInfo info = partitionInfos.get(table);
		if (info == null) {
			synchronized (table) {
				info = partitionInfos.get(table);
				if (info == null) {// double-check
					info = computePartitionTable(table);
					partitionInfos.put(table, info);
				}
			}
		}
		return info;
	}

	/**
	 * return partition information for the given table; if the table is not
	 * partitioned, partitionInfo.hasPartition() return false;
	 *
	 * @param table
	 * @return
	 */
	public PartitionInfo computePartitionTable(Table table) {
		PartitionInfo info = new PartitionInfo(table);
		try {
			Connection connection = null;
			try {
				connection = this.ds.getConnectionBlocking();
				Statement statement = null;
				try {
					statement = connection.createStatement();
					populatePartitionKeys(statement, info);
					if (info.hasKeys()) {
						populatePartitionTables(statement, info);
						return info;
					} else {
						// not partitionned
						return info;
					}
				} catch (ExecutionException e) {
					logger.error(e.getLocalizedMessage());
					return info;
				} finally {
					if (statement != null) {
						statement.close();
					}
				}
			} finally {
				if (connection != null) {
					connection.close();
					ds.releaseSemaphore();
				}
			}
		} catch (SQLException e) {
			return info;
		}
	}

	private void populatePartitionTables(Statement statement, PartitionInfo info)
			throws SQLException, ExecutionException {
		Table table = info.getTable();
		String sql = "select partitiontablename, partitionrangestart, partitionrangeend from pg_partitions WHERE schemaname='"
				+ table.getSchema().getName() + "' and tablename='" + table.getName()
				+ "' order by partitionrangestart";
		Collection<Column> keys = info.getKeys();
		IDomain rangeType = IDomain.UNKNOWN;
		if (keys.size() == 1) {
			Column first = keys.iterator().next();
			rangeType = first.getTypeDomain();
		}
		ResultSet resultset = statement.executeQuery(sql);
		while (resultset.next()) {
			String tablename = resultset.getString(1);
			Object rangeStart = parseRangeValue(resultset.getString(2), rangeType);
			Object rangeEnd = parseRangeValue(resultset.getString(3), rangeType);
			if (tablename != null && tablename != "") {
				info.addPartitionTable(tablename, rangeStart, rangeEnd);
			}
		}
	}

	private Object parseRangeValue(String rangeValue, IDomain rangeType) {
		if (rangeType.isInstanceOf(IDomain.DATE)) {
			// the gp format is = 'yyyy-MM-dd'::date
			if (rangeValue.endsWith("::date")) {
				String source = rangeValue.substring(1, rangeValue.length() - 7);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date date = format.parse(source);
					return date;
				} catch (ParseException e) {
					//
				}
			}
		}
		// else
		return null;
	}

	protected void populatePartitionKeys(Statement statement, PartitionInfo info)
			throws SQLException, ExecutionException {
		Table table = info.getTable();
		String sql = "select columnname from pg_partition_columns WHERE schemaname='" + table.getSchema().getName()
				+ "' and tablename='" + table.getName() + "' order by position_in_partition_key ASC";
		ResultSet resultset = statement.executeQuery(sql);
		while (resultset.next()) {
			String columnname = resultset.getString(1);
			if (columnname != null && columnname != "") {
				info.addPartitionKey(columnname);
			}
		}
	}

	@Override
	protected void computeTablesStatistics(Schema schema, Connection connection) throws ExecutionException {
		// TODO Auto-generated method stub

	}

}
