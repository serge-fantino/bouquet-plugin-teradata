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
import com.squid.core.sql.db.render.StddevOperatorRenderer;
import com.squid.core.sql.render.IPiece;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataVarStdevRenderer extends StddevOperatorRenderer {

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		String result = "";
		ExtendedType[] types = piece.getParamTypes();
		//
		if (types.length == 1) {
			if (types[0].getDomain().isInstanceOf(IDomain.TEMPORAL)) {
				// OperatorDefinition opdef =
				// OperatorScope.getDefault().lookupByExtendedID(DateOperatorDefinition.DATE_INTERVAL);
				// IPiece[] params = new IPiece[3];
				// ExtendedType[] xtypes = new ExtendedType[3];
				// params[0] = piece.getParams()[0];
				// xtypes[0] = types[0];
				// Calendar c = Calendar.getInstance();
				// c.set(1990, 0, 1, 0, 0, 0);
				// ConstantValuePiece xx = new ConstantValuePiece(c.getTime(),
				// IDomain.DATE);
				// params[1] = xx;
				// xtypes[1] = ExtendedType.DATE;
				// ConstantValuePiece yy = new ConstantValuePiece("SECOND",
				// IDomain.STRING);
				// params[2] = yy;
				// xtypes[2] = ExtendedType.STRING;
				// OperatorPiece date_sub = new
				// OperatorPiece(opdef,params,xtypes);
				// String zz;
				// try {
				// zz = date_sub.render(skin);
				// } catch (IOException e) {
				// throw new RenderingException(e);
				// }
				// result = opDef.prettyPrint(new String[]{zz}, false);
				if (types[0].getDomain().isInstanceOf(IDomain.TIMESTAMP)) {
					String arg = getLocalEpoch(skin, piece.getParams()[0], types[0], args[0]);
					result = opDef.prettyPrint(new String[] { arg }, false);
				} else {
					String arg = "CAST((CAST('1970-01-01' as DATE FORMAT 'YYYY-MM-DD') - " + args[0]
							+ ") AS FLOAT)*86400";
					result = opDef.prettyPrint(new String[] { arg }, false);
				}
			} else {
				result = opDef.prettyPrint(args, false);
			}
		} else {
			return super.prettyPrint(skin, piece, opDef, args);
		}
		//
		return result;
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTimestamp(SQLSkin skin, IPiece piece, String date) throws RenderingException {
		CastOperatorDefinition toTimestamp = new CastOperatorDefinition("TO_TIMESTAMP",
				CastOperatorDefinition.TO_TIMESTAMP, IDomain.TIMESTAMP, OperatorDefinition.DATE_TIME_TYPE);
		OperatorPiece castPiece = new OperatorPiece(toTimestamp, new IPiece[] { piece },
				new ExtendedType[] { ExtendedType.STRING });
		return skin.render(skin, castPiece, toTimestamp, new String[] { "'1970-01-01 00:00:00'" });

	}

}
