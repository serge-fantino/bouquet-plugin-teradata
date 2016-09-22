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

import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.BaseOperatorRenderer;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

/**
 * Provide WIDTH_BUCKET render for Teradata
 *
 * @author nhadt
 *
 */
public class TeraWidthBucketRenderer extends BaseOperatorRenderer {

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
		String metric, lowerBound, upperBound, bucketCount;
		if (args != null) {
			if (args.length != 4) {
				throw new RuntimeException("invalid syntax for EquiWidthBucket operator");
			}
			metric = args[0];
			lowerBound = args[1];
			upperBound = args[2];
			bucketCount = args[3];
		} else {
			metric = lowerBound = upperBound = bucketCount = "?";
		}
		String str = "WIDTH_BUCKET(" + metric + "," + lowerBound + "," + upperBound + "," + bucketCount + ")";
		return str;
	}

}
