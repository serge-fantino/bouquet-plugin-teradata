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

import java.sql.Types;

import com.squid.core.domain.IDomain;
import com.squid.core.domain.extensions.cast.CastOperatorDefinition;
import com.squid.core.domain.extensions.date.operator.DateOperatorDefinition;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.AverageOperatorRenderer;
import com.squid.core.sql.render.IPiece;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataAvgRenderer extends AverageOperatorRenderer {

	@Override
	public String prettyPrint(SQLSkin skin, OperatorDefinition opDef, String[] args) throws RenderingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		String result = "";
		ExtendedType[] types = piece.getParamTypes();
		//
		if (types.length == 1) {
			if (types[0].getDomain().isInstanceOf(IDomain.TEMPORAL)) {
				if (types[0].getDomain().isInstanceOf(IDomain.TIMESTAMP)) {
					// String arg = "CAST((" +
					// getLocalEpoch(skin,piece.getParams()[0],types[0],args[0])
					// + ") AS FLOAT)";
					// result = "CAST('1970-01-01 00:00:00' as TIMESTAMP) +
					// CAST(" + opDef.prettyPrint(new String[]{arg}, false) +"
					// AS INTEGER)";
					// String arg = "CAST("+ args[0]+" AS DATE) -
					// CAST('1970-01-01' as DATE FORMAT 'YYYY-MM-DD')";
					// result = "CAST(CAST('1970-01-01' as DATE FORMAT
					// 'YYYY-MM-DD') + CAST(" + opDef.prettyPrint(new
					// String[]{arg}, false) +" AS INTEGER) AS TIMESTAMP(6))" +
					// getLocalEpoch(skin,piece.getParams()[0],types[0],args[0]);
					DateOperatorDefinition to_unixtime = new DateOperatorDefinition("TO_EPOCH",
							DateOperatorDefinition.TO_UNIXTIME, IDomain.NUMERIC, OperatorDefinition.DATE_TIME_TYPE);
					String arg = skin.render(skin, piece, to_unixtime, args);
					DateOperatorDefinition from_unixtime = new DateOperatorDefinition("FROM_EPOCH",
							DateOperatorDefinition.FROM_UNIXTIME, IDomain.TIMESTAMP, OperatorDefinition.DATE_TIME_TYPE);
					OperatorPiece fromUnixTimePiece = new OperatorPiece(from_unixtime, new IPiece[] { piece }, types);
					result = skin
							.render(skin, fromUnixTimePiece, from_unixtime,
									new String[] { "CAST("
											+ opDef.prettyPrint(new String[] { "CAST(" + arg + "AS FLOAT)" }, false)
											+ " AS BIGINT)" });
					;
				} else {
					String arg = "CAST((" + args[0] + " - CAST('1970-01-01' as DATE FORMAT 'YYYY-MM-DD')) AS FLOAT)";
					result = "CAST('1970-01-01' as DATE FORMAT 'YYYY-MM-DD') + CAST("
							+ opDef.prettyPrint(new String[] { arg }, false) + " AS INTEGER)";
				}
			} else {
				if (types[0].getDataType() == Types.INTEGER) {
					String[] newArgs = new String[] { "CAST(" + args[0] + " AS FLOAT)" };
					result = opDef.prettyPrint(newArgs, false);
				} else {
					result = opDef.prettyPrint(args, false);
				}
			}
		}
		//
		return result;
	}

	@Override
	public String getTimestamp(SQLSkin skin, IPiece piece, String date) throws RenderingException {
		CastOperatorDefinition toTimestamp = new CastOperatorDefinition("TO_TIMESTAMP",
				CastOperatorDefinition.TO_TIMESTAMP, IDomain.TIMESTAMP, OperatorDefinition.DATE_TIME_TYPE);
		OperatorPiece castPiece = new OperatorPiece(toTimestamp, new IPiece[] { piece },
				new ExtendedType[] { ExtendedType.STRING });
		return skin.render(skin, castPiece, toTimestamp, new String[] { "'1970-01-01 00:00:00'" });

	}

	// @Override
	// public String getLocalEpoch(SQLSkin skin, IPiece piece, ExtendedType
	// type,
	// String arg) throws RenderingException {
	//// String txt = "";
	//// txt = " + CAST(CAST(AVG((EXTRACT(HOUR FROM ("+arg+")))) AS INTEGER) AS
	// INTERVAL HOUR) + ";
	//// txt += "CAST(CAST(AVG((EXTRACT(SECOND FROM ("+arg+")))) AS INTEGER) AS
	// INTERVAL MINUTE) + ";
	//// txt += "CAST(CAST(AVG((EXTRACT(SECOND FROM ("+arg+")))) AS DEC(10,6))
	// AS INTERVAL SECOND(3))";
	//// return txt;
	// DateOperatorDefinition date_interval = new
	// DateOperatorDefinition("DATE_INTERVAL",DateOperatorDefinition.DATE_INTERVAL,IDomain.NUMERIC,
	// OperatorDefinition.DATE_TIME_TYPE);
	// DateOperatorDefinition from_unixtime = new
	// DateOperatorDefinition("FROM_EPOCH",DateOperatorDefinition.FROM_UNIXTIME,IDomain.TIMESTAMP,
	// OperatorDefinition.DATE_TIME_TYPE);
	// CastOperatorDefinition toTimestamp= new
	// CastOperatorDefinition("TO_TIMESTAMP",CastOperatorDefinition.TO_TIMESTAMP,IDomain.TIMESTAMP,
	// OperatorDefinition.DATE_TIME_TYPE);
	// OperatorPiece castPiece = new OperatorPiece(toTimestamp, new
	// IPiece[]{piece}, new ExtendedType[] {ExtendedType.STRING});
	// SimpleConstantValuePiece intervalTypePiece = new
	// SimpleConstantValuePiece("SECOND",IDomain.STRING);
	// OperatorPiece intervalPiece = new OperatorPiece(date_interval, new
	// IPiece[] {piece, piece,intervalTypePiece}, new ExtendedType[] {type,
	// castPiece.getType(), ExtendedType.STRING});
	// String txt = "";
	// try {
	// txt = skin.render(skin, intervalPiece, date_interval, new String[] {arg,
	// skin.render(skin, castPiece, toTimestamp, new String[] {"'1970-01-01
	// 00:00:00'"}), intervalTypePiece.render(skin)});
	// } catch (IOException e) {
	// }
	//
	// return skin.render(skin, new OperatorPiece(from_unixtime, new IPiece[]
	// {piece}, new ExtendedType[] {ExtendedType.BIGINT}),from_unixtime, new
	// String[] {txt});
	// }
}
