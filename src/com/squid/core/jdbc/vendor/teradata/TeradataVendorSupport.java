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

import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import com.squid.core.database.impl.DataSourceReliable;
import com.squid.core.database.metadata.IMetadataEngine;
import com.squid.core.database.metadata.VendorMetadataSupport;
import com.squid.core.database.model.DatabaseProduct;
import com.squid.core.database.statistics.IDatabaseStatistics;
import com.squid.core.jdbc.formatter.DataFormatter;
import com.squid.core.jdbc.formatter.IJDBCDataFormatter;
import com.squid.core.jdbc.vendor.DefaultVendorSupport;

public class TeradataVendorSupport extends DefaultVendorSupport {

	public static final String VENDOR_ID = IMetadataEngine.TERADATA_NAME;

	public static final VendorMetadataSupport METADATA = new TeradataMetadataSupport();
	private Properties properties;

	@Override
	public String getVendorId() {
		return VENDOR_ID;
	}

	@Override
	public String getVendorVersion() {
		try {
			this.properties = new Properties();
			properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
			return properties.getProperty("application.version");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "-1";
	}

	@Override
	public boolean isSupported(DatabaseProduct product) {
		return VENDOR_ID.equalsIgnoreCase(product.getProductName());
	}

	@Override
	public IJDBCDataFormatter createFormatter(DataFormatter formatter, Connection connection) {
		return new TeradataIJDBCDataFormater(formatter, connection);
	}

	@Override
	public IDatabaseStatistics createDatabaseStatistics(DataSourceReliable ds) {
		return new TeradataStatistics(ds);
	}

	@Override
	public VendorMetadataSupport getVendorMetadataSupport() {
		return METADATA;
	}

}
