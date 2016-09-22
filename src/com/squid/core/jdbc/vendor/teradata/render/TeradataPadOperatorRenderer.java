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

import com.squid.core.domain.extensions.string.pad.PadOperatorDefinition;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.PadOperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataPadOperatorRenderer extends PadOperatorRenderer {

	public TeradataPadOperatorRenderer(String mode) {
		super(mode);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		if (args != null) {
			if (args.length != 3) {
				throw new RuntimeException("invalid syntax for pad operator");
			}

		}
		String trimMode = "LEADING";
		if (opDef.getExtendedID().equals(PadOperatorDefinition.STRING_RPAD)) {
			trimMode = "TRAILING";
		}
		int repeat = 1;
		String padString = "";
		try {
			repeat = new Integer(args[1]).intValue();
		} catch (NumberFormatException nfe) {

		}
		for (int j = 0; j < repeat; j++) {
			padString += args[2].substring(1, args[2].length() - 1);
		}
		String varTrim = "TRIM(" + trimMode + " FROM " + args[0] + ")";
		String varLength = "CHARACTER_LENGTH(" + varTrim + ")";
		String txt = "CASE \n";
		txt += "	WHEN " + varLength + "=0 THEN NULL\n";
		txt += "	WHEN " + args[0] + " IS NOT NULL AND " + varLength + ">=" + args[1] + " THEN SUBSTRING(" + varTrim
				+ " FROM 1 FOR " + args[1] + ")\n";
		if (opDef.getExtendedID().equals(PadOperatorDefinition.STRING_LPAD)) {
			txt += "	ELSE SUBSTRING('" + padString + "' FROM 1 FOR (" + args[1] + " - " + varLength + ")) || "
					+ varTrim + "\n";
		} else {
			txt += "	ELSE " + varTrim + " ||  SUBSTRING('" + padString + "' FROM 1 FOR (" + args[1] + " - "
					+ varLength + "))\n";

		}
		txt += "END";
		return txt;
	}

}
