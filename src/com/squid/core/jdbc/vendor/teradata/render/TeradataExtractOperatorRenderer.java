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

import com.squid.core.domain.IDomain;
import com.squid.core.domain.extensions.cast.CastOperatorDefinition;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.ExtractAsFunctionOperatorRenderer;
import com.squid.core.sql.render.IPiece;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataExtractOperatorRenderer extends ExtractAsFunctionOperatorRenderer {

	public TeradataExtractOperatorRenderer(String mode) {
		super(mode);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {

		if (args.length != 1) {
			throw new RenderingException("invalid EXTRACT operator");
		}
		ExtendedType[] extendedTypes = null;
		extendedTypes = getExtendedPieces(piece);
		String txt = args[0];
		if (extendedTypes[0].getDomain().isInstanceOf(IDomain.TIMESTAMP)) {
			txt = castTimestampAsDate(skin, piece.getParams()[0], args[0]);
		}
		if (prepend.equals("DAY_OF_YEAR")) {
			return "(" + txt + " - CAST(CAST(EXTRACT(YEAR FROM (" + args[0]
					+ ")) AS CHAR(4)) AS DATE FORMAT 'YYYY') + 1)";
		} else {
			return "((" + txt + " - CAST('1900' AS DATE FORMAT 'YYYY')) MOD 7 + 1)";
		}
	}

	protected String castTimestampAsDate(SQLSkin skin, IPiece piece, String arg) throws RenderingException {
		String[] subArgs = new String[1];
		subArgs[0] = arg;
		CastOperatorDefinition toTimestamp = new CastOperatorDefinition("TO_DATE", CastOperatorDefinition.TO_DATE,
				IDomain.DATE, OperatorDefinition.DATE_TIME_TYPE);
		OperatorPiece operatorPiece = new OperatorPiece(toTimestamp, new IPiece[] { piece });
		return skin.render(skin, operatorPiece, toTimestamp, subArgs);
	}

}
