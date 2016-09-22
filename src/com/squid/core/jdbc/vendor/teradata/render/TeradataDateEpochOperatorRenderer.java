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
import com.squid.core.sql.db.render.DateAddSubOperatorRenderer.OperatorType;
import com.squid.core.sql.db.render.DateEpochOperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataDateEpochOperatorRenderer extends DateEpochOperatorRenderer {

	public TeradataDateEpochOperatorRenderer(int type) {
		super(type);
		cor = new TeradataCastOperatorRenderer();
		tdior = new TeradataDateIntervalOperatorRenderer();
		dasor = new TeradataDateAddSubOperatorRenderer(OperatorType.ADD);
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		String txt = "";
		switch (type) {
		case FROM:
			/*
			 * txt = "CASE WHEN " + args[0] + ">=0 THEN"; txt +=
			 * "\n\t\t\t CAST(((DATE '1970-01-01' + (CAST(CAST(" + args[0] +
			 * " / 86400 as INTEGER) AS INTEGER)) ) ( FORMAT 'YYYY-MM-DD'))";
			 * txt += "\n\t\t\t || ' ' || (((CAST(" + args[0] +
			 * " AS INTEGER) MOD 86400) / 3600 ) (FORMAT '99'))"; txt +=
			 * "\n\t\t\t || ':' || (((CAST(" + args[0] +
			 * " AS INTEGER) MOD 3600 ) / 60) (FORMAT '99'))"; txt +=
			 * "\n\t\t\t || ':' || ((CAST(" + args[0] +
			 * " AS INTEGER) MOD 60) (FORMAT '99')) AS TIMESTAMP(0) )"; txt +=
			 * "\n\t\t ELSE"; txt +=
			 * "\n\t\t\t CAST(((DATE '1970-01-01' + (CAST(CAST(" + args[0] +
			 * " / 86400 as INTEGER) + (CASE WHEN (" + args[0] +
			 * " / 86400 - CAST(" + args[0] +
			 * " / 86400 as INTEGER))<0.5 THEN 0 ELSE 1 END) + (CASE WHEN ((CAST("
			 * + args[0] + " AS INTEGER) MOD 60) + 60)>0 OR ((CAST(" + args[0] +
			 * " AS INTEGER) MOD 3600) / 60)>0  OR ((CAST(" + args[0] +
			 * " AS INTEGER) MOD 86400) / 3600)>0 THEN -1 ELSE 0 END) AS INTEGER)) ) ( FORMAT 'YYYY-MM-DD'))"
			 * ; txt += "\n\t\t\t || ' ' || (((CAST(" + args[0] +
			 * " AS INTEGER) MOD 86400 ) / 3600 + (CASE WHEN ((CAST(" + args[0]
			 * + " AS INTEGER) MOD 60) + 60)>0 OR ((CAST(" + args[0] +
			 * " AS INTEGER) MOD 3600) / 60)>0  THEN 23 ELSE 24 END)) (FORMAT '99'))"
			 * ; txt += "\n\t\t\t || ':' || (((CAST(" + args[0] +
			 * " AS INTEGER) MOD 3600 ) / 60 + (CASE WHEN ((CAST(" + args[0] +
			 * " AS INTEGER) MOD 60) + 60)>0 THEN 59 ELSE 60 END)) (FORMAT '99'))"
			 * ; txt += "\n\t\t\t || ':' || (((CAST(" + args[0] +
			 * " AS INTEGER) MOD 60) + 60) (FORMAT '99')) AS TIMESTAMP(0) )";
			 * txt += "\n\t\tEND";
			 */
			txt = "CAST(((CAST(DATE '1970-01-01' + (" + args[0] + " / 86400) AS TIMESTAMP(0) AT 0)) AT 0) + (("
					+ args[0] + " MOD 86400) * INTERVAL '00:00:01' HOUR TO SECOND) AS TIMESTAMP(0))";
			return txt;
		default:
			return super.prettyPrint(skin, piece, opDef, args);
		}
	}
}
