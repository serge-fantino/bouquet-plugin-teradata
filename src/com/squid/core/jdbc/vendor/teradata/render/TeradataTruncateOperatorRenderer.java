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
 * Ticket #1190 implements some ANSI functions
 *
 * @author loivd Truncate function for teradata
 */
public class TeradataTruncateOperatorRenderer extends BaseOperatorRenderer {
	public TeradataTruncateOperatorRenderer() {
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
		String parameter = args.length > 1 ? "parameter" : "parameters";
		if (args.length != 1 && args.length != 2) {
			throw new RenderingException("Have not supported function TRUNCATE with " + args.length + parameter);
		}
		if (args.length == 1) {
			// TRUNCATE(x) and FLOOR(x) this case, the returned value is the
			// same and equal CAST(x as INTEGER)
			// Truncate(x)=Floor(x)
			String str = "CAST(";
			str += args[0];
			str += " as INTEGER)";
			return str;
		} else {
			// TRUNCATE(var, precision) = CCAST(var * 10**precision AS
			// INTEGER)/10**precision
			int precision = Integer.valueOf(args[1]);
			String str = "CAST(var * 10**precision AS INTEGER)/10**precision";
			str = str.replace("var", args[0]);
			str = str.replace("precision", String.valueOf(precision));
			return str;
		}
	}
}
