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

import com.squid.core.domain.extensions.cast.CastOperatorDefinition;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.CastOperatorRenderer;
import com.squid.core.sql.db.render.OperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataCastOperatorRenderer extends CastOperatorRenderer implements OperatorRenderer {

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		if (args.length == 1) {
			if (CastOperatorDefinition.TO_INTEGER.equals(opDef.getExtendedID())) {
				StringBuilder strBuilder = new StringBuilder();
				strBuilder.append("CAST(");
				strBuilder.append(args[0].toString());
				strBuilder.append(" as INTEGER) + (CASE WHEN (");
				strBuilder.append(args[0].toString());
				strBuilder.append(" - CAST(");
				strBuilder.append(args[0].toString());
				strBuilder.append(" as INTEGER))>=(1.0/2) THEN 1 ELSE 0 END)");
				return strBuilder.toString();
			} else {
				return prettyPrintSingleArg(skin, opDef, piece, args);
			}
		} else if (args.length == 2) {
			return prettyPrintTwoArgs(skin, piece, opDef, args);
		} else {
			if (CastOperatorDefinition.TO_NUMBER.equals(opDef.getExtendedID())) {
				return prettyPrintSingleArg(skin, opDef, piece, args);
			} else {
				throw new RenderingException("Invalid operator " + opDef.getSymbol());
			}
		}
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
		return prettyPrint(skin, null, opDef, args);
	}

	@Override
	protected String prettyPrintTwoArgs(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		String txt = "CAST(";
		txt += args[0] + " AS ";
		if (CastOperatorDefinition.TO_TIMESTAMP.equals(opDef.getExtendedID())) {
			txt += "TIMESTAMP FORMAT " + args[1].replaceAll(" ", "B").replaceAll("HH24", "HH") + ")";
			return txt;
		} else if (CastOperatorDefinition.TO_DATE.equals(opDef.getExtendedID())) {
			txt += "DATE FORMAT " + args[1].replaceAll(" ", "B").replaceAll("HH24", "HH") + ")";
			return txt;
		} else if (CastOperatorDefinition.TO_CHAR.equals(opDef.getExtendedID())) {
			txt = "CAST((" + args[0] + " (FORMAT " + args[1] + ")) AS VARCHAR(" + args[1].length() + "))";
			return txt;
		}
		return opDef.getSymbol() + "(" + args[0] + "," + args[1] + ")";
	}
}
