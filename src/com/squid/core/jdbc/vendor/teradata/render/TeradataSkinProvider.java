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
package com.squid.core.jdbc.vendor.teradata.render;

import java.util.List;

import com.squid.core.database.impl.DataSourceReliable;
import com.squid.core.database.model.DatabaseProduct;
import com.squid.core.domain.extensions.cast.CastOperatorDefinition;
import com.squid.core.domain.extensions.date.extract.ExtractOperatorDefinition;
import com.squid.core.domain.extensions.date.operator.DateOperatorDefinition;
import com.squid.core.domain.extensions.string.PosStringOperatorDefinition;
import com.squid.core.domain.extensions.string.StringLengthOperatorsDefinition;
import com.squid.core.domain.extensions.string.SubstringOperatorDefinition;
import com.squid.core.domain.extensions.string.pad.PadOperatorDefinition;
import com.squid.core.domain.extensions.string.translate.TranslateOperatorDefinition;
import com.squid.core.domain.maths.CeilOperatorDefinition;
import com.squid.core.domain.maths.FloorOperatorDefintion;
import com.squid.core.domain.maths.PiOperatorDefintion;
import com.squid.core.domain.maths.PowerOperatorDefintion;
import com.squid.core.domain.maths.RandOperatorDefinition;
import com.squid.core.domain.maths.RoundOperatorDefintion;
import com.squid.core.domain.maths.SignOperatorDefintion;
import com.squid.core.domain.maths.TruncateOperatorDefintion;
import com.squid.core.domain.operators.AggregateOperatorDefinition;
import com.squid.core.domain.operators.IntrinsicOperators;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.domain.operators.RankOperatorDefinition;
import com.squid.core.domain.operators.StdevPopOperatorDefinition;
import com.squid.core.sql.db.features.IGroupingSetSupport;
import com.squid.core.sql.db.render.AlternativePiOperatorRenderer;
import com.squid.core.sql.db.render.DateAddSubOperatorRenderer.OperatorType;
import com.squid.core.sql.db.render.DateEpochOperatorRenderer;
import com.squid.core.sql.db.render.IExplainFeatureSupport;
import com.squid.core.sql.db.render.MetatdataSearchFeatureSupport;
import com.squid.core.sql.db.render.OrderedAnalyticOperatorRenderer;
import com.squid.core.sql.db.render.RankOperatorRenderer;
import com.squid.core.sql.db.render.StringLengthRenderer;
import com.squid.core.sql.db.templates.DefaultJDBCSkin;
import com.squid.core.sql.db.templates.DefaultSkinProvider;
import com.squid.core.sql.db.templates.ISkinProvider;
import com.squid.core.sql.db.templates.SkinRegistry;
import com.squid.core.sql.render.ISkinFeatureSupport;
import com.squid.core.sql.render.SQLSkin;
import com.squid.core.sql.render.ZeroIfNullFeatureSupport;
import com.squid.core.sql.statements.SelectStatement;

public class TeradataSkinProvider extends DefaultSkinProvider {

	private static final ZeroIfNullFeatureSupport zeroIfNull = new TeradataZeroIfNullFeatureSupport();

	public TeradataSkinProvider() {
		// registerOperatorRender("com.sodad.domain.operator.density.PERCENTILE",
		// new PercentileRenderer());
		registerOperatorRender("com.sodad.domain.operator.density.EQWBUCKET", new TeraWidthBucketRenderer());
		registerOperatorRender(PosStringOperatorDefinition.STRING_POSITION, new PosStringRenderer());
		registerOperatorRender(SubstringOperatorDefinition.STRING_SUBSTRING, new SubStringRenderer());
		registerOperatorRender(StringLengthOperatorsDefinition.STRING_LENGTH,
				new StringLengthRenderer("CHARACTER_LENGTH"));
		registerOperatorRender(PadOperatorDefinition.STRING_LPAD,
				new TeradataPadOperatorRenderer(PadOperatorDefinition.STRING_LPAD));
		registerOperatorRender(PadOperatorDefinition.STRING_RPAD,
				new TeradataPadOperatorRenderer(PadOperatorDefinition.STRING_RPAD));
		//
		registerOperatorRender(CastOperatorDefinition.TO_CHAR, new TeradataCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_DATE, new TeradataCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_NUMBER, new TeradataCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_TIMESTAMP, new TeradataCastOperatorRenderer());
		registerOperatorRender(CastOperatorDefinition.TO_INTEGER, new TeradataCastOperatorRenderer());
		//
		registerOperatorRender(DateOperatorDefinition.DATE_ADD,
				new TeradataDateAddSubOperatorRenderer(OperatorType.ADD));
		registerOperatorRender(DateOperatorDefinition.DATE_MONTHS_BETWEEN, new MonthsBetweenRenderer());
		registerOperatorRender(DateOperatorDefinition.DATE_INTERVAL, new TeradataDateIntervalOperatorRenderer());
		registerOperatorRender(DateOperatorDefinition.DATE_SUB,
				new TeradataDateAddSubOperatorRenderer(OperatorType.SUB));
		registerOperatorRender(DateOperatorDefinition.FROM_UNIXTIME,
				new TeradataDateEpochOperatorRenderer(DateEpochOperatorRenderer.FROM));
		registerOperatorRender(DateOperatorDefinition.TO_UNIXTIME,
				new TeradataDateEpochOperatorRenderer(DateEpochOperatorRenderer.TO));
		registerOperatorRender(ExtractOperatorDefinition.EXTRACT_DAY_OF_YEAR,
				new TeradataExtractOperatorRenderer("DAY_OF_YEAR"));
		registerOperatorRender(ExtractOperatorDefinition.EXTRACT_DAY_OF_WEEK,
				new TeradataExtractOperatorRenderer("DAY_OF_WEEK"));
		//
		registerOperatorRender(RankOperatorDefinition.RANK_ID, new RankOperatorRenderer());
		registerOperatorRender(RankOperatorDefinition.ROWNUMBER_ID, new RankOperatorRenderer());
		// registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.AVG),new
		// OrderedAnalyticOperatorRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.MIN),
				new OrderedAnalyticOperatorRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.MAX),
				new OrderedAnalyticOperatorRenderer());
		registerOperatorRender(OperatorDefinition.getExtendedId(IntrinsicOperators.SUM),
				new OrderedAnalyticOperatorRenderer());
		//
		registerOperatorRender(CeilOperatorDefinition.CEIL, new TeradataCeilOperatorRenderer());
		registerOperatorRender(FloorOperatorDefintion.FLOOR, new TeradataFloorOperatorRenderer());
		registerOperatorRender(SignOperatorDefintion.SIGN, new TeradataSignOperatorRenderer());
		registerOperatorRender(RoundOperatorDefintion.ROUND, new TeradataRoundOperatorRenderer());
		registerOperatorRender(PowerOperatorDefintion.POWER, new TeradataPowerOperatorRenderer());
		registerOperatorRender(TruncateOperatorDefintion.TRUNCATE, new TeradataTruncateOperatorRenderer());
		registerOperatorRender(RandOperatorDefinition.RAND, new TeradataRandOperatorRenderer());
		registerOperatorRender(PiOperatorDefintion.PI, new AlternativePiOperatorRenderer());
		//
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.VARIANCE),
				new TeradataVarStdevRenderer());
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.VAR_SAMP),
				new TeradataVarStdevRenderer());
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.STDDEV_POP),
				new TeradataVarStdevRenderer());
		registerOperatorRender(StdevPopOperatorDefinition.getExtendedId(IntrinsicOperators.STDDEV_SAMP),
				new TeradataVarStdevRenderer());
		registerOperatorRender(AggregateOperatorDefinition.getExtendedId(IntrinsicOperators.AVG),
				new TeradataAvgRenderer());
		//
		unregisterOperatorRender(TranslateOperatorDefinition.STRING_TRANSLATE);// not
																				// yet
																				// available
																				// on
																				// TD

		// Date truncation
		// Regexp functions
		// Coalesce
		// Nullif

	}

	@Override
	public double computeAccuracy(DatabaseProduct product) {
		try {
			if (product != null) {
				if ("teradata".equalsIgnoreCase(product.getProductName())) {
					return PERFECT_MATCH;
				} else {
					return NOT_APPLICABLE;
				}
			} else {
				return NOT_APPLICABLE;
			}
		} catch (Exception e) {
			return NOT_APPLICABLE;
		}
	}

	@Override
	public SQLSkin createSkin(DatabaseProduct product) {
		return new TeradataSQLSkin(this, product);
	}

	@Override
	public ISkinFeatureSupport getFeatureSupport(DefaultJDBCSkin skin, String featureID) {
		if (featureID == DataSourceReliable.FeatureSupport.GROUPBY_ALIAS) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID == IGroupingSetSupport.ID) {
			return IGroupingSetSupport.IS_SUPPORTED;
		} else if (featureID == SelectStatement.SampleFeatureSupport.SELECT_SAMPLE) {
			return SAMPLE_SUPPORT;
		} else if (featureID == ZeroIfNullFeatureSupport.ID) {
			return zeroIfNull;
		} else if (featureID == SelectStatement.SampleFeatureSupport.ORDERED_ANALYTICAL_FUNCTIONS) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		} else if (featureID == IExplainFeatureSupport.EXPLAIN_FEATURE_ID) {
			return EXPLAIN_SUPPORT;
		} else if (featureID == MetatdataSearchFeatureSupport.METADATA_SEARCH_FEATURE_ID) {
			return METADATA_SEARCH_SUPPORT;
		} else if (featureID == DataSourceReliable.FeatureSupport.AUTOCOMMIT) {
			return ISkinFeatureSupport.IS_SUPPORTED;
		}
		// else
		return super.getFeatureSupport(skin, featureID);
	}

	private SelectStatement.SampleFeatureSupport SAMPLE_SUPPORT = new SelectStatement.SampleFeatureSupport() {

		@Override
		public boolean isCountSupported() {
			return true;
		}

		@Override
		public boolean isPercentageSupported() {
			return true;
		}

	};

	private IExplainFeatureSupport EXPLAIN_SUPPORT = new IExplainFeatureSupport() {

		@Override
		public String createExplainStatement(String statement) {
			StringBuilder expSQL = new StringBuilder();
			// create explain SQL
			expSQL.append("EXPLAIN ");
			expSQL.append(CR_LF);
			expSQL.append(statement);
			return expSQL.toString();
		}

	};

	private MetatdataSearchFeatureSupport METADATA_SEARCH_SUPPORT = new MetatdataSearchFeatureSupport() {

		@Override
		public String createTableSearch(List<String> schemas, String tableName, boolean isCaseSensitive) {
			StringBuilder sqlCode = new StringBuilder();
			sqlCode.append("SELECT DATABASENAME, TABLENAME,COMMENTSTRING");
			sqlCode.append(CR_LF);
			sqlCode.append(" FROM DBC.TABLESX TABS");
			sqlCode.append(CR_LF);
			sqlCode.append(" WHERE DATABASENAME IN (" + getGroupSchemaNames(schemas) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND (" + applyCaseSensitive("TABLENAME", isCaseSensitive) + " LIKE "
					+ applyCaseSensitive(tableName, isCaseSensitive) + " OR "
					+ applyCaseSensitive("COMMENTSTRING", isCaseSensitive) + " LIKE "
					+ applyCaseSensitive(tableName, isCaseSensitive) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" ORDER BY 1, 2");
			return sqlCode.toString();
		}

		@Override
		public String createColumnSearch(List<String> schemas, String tableName, String columnName,
				boolean isCaseSensitive) {
			StringBuilder sqlCode = new StringBuilder();
			sqlCode.append("SELECT COLS.DATABASENAME, COLS.TABLENAME, COLUMNNAME,COLS.COMMENTSTRING");
			sqlCode.append(CR_LF);
			sqlCode.append(" FROM DBC.COLUMNSX COLS,  DBC.TABLESX TABS");
			sqlCode.append(CR_LF);
			sqlCode.append(" WHERE TABS.DATABASENAME IN (" + getGroupSchemaNames(schemas) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" AND COLS.DATABASENAME=TABS.DATABASENAME AND COLS.TABLENAME=TABS.TABLENAME");
			if (tableName != null) {
				sqlCode.append(CR_LF);
				sqlCode.append(" AND (" + applyCaseSensitive("TABS.TABLENAME", isCaseSensitive) + " LIKE "
						+ applyCaseSensitive(tableName, isCaseSensitive) + " OR "
						+ applyCaseSensitive("TABS.COMMENTSTRING", isCaseSensitive) + " LIKE "
						+ applyCaseSensitive(tableName, isCaseSensitive) + ")");
			}
			sqlCode.append(CR_LF);
			sqlCode.append(" AND (" + applyCaseSensitive("COLUMNNAME", isCaseSensitive) + " LIKE "
					+ applyCaseSensitive(columnName, isCaseSensitive) + " OR "
					+ applyCaseSensitive("COLS.COMMENTSTRING", isCaseSensitive) + " LIKE "
					+ applyCaseSensitive(columnName, isCaseSensitive) + ")");
			sqlCode.append(CR_LF);
			sqlCode.append(" ORDER BY 1, 2");
			return sqlCode.toString();
		}

	};

	@Override
	public String getSkinPrefix(DatabaseProduct product) {
		return "teradata";
	}

	@Override
	public ISkinProvider getParentSkinProvider() {
		return SkinRegistry.INSTANCE.findSkinProvider(DefaultSkinProvider.class);
	}

}
