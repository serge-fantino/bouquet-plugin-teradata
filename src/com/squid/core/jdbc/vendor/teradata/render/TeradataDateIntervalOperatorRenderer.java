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
import com.squid.core.domain.extensions.date.operator.DateOperatorDefinition;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.DateAddSubOperatorRenderer.OperatorType;
import com.squid.core.sql.db.render.DateIntervalOperatorRenderer;
import com.squid.core.sql.db.render.ExtractOperatorRenderer;
import com.squid.core.sql.render.IPiece;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;
import com.squid.core.sql.render.SimpleConstantValuePiece;

/**
 * The SQL render for converting a time difference into a specific period
 *
 * @author julien theulier
 *
 */
public class TeradataDateIntervalOperatorRenderer extends DateIntervalOperatorRenderer {

	private TeradataDateAddSubOperatorRenderer dateSubRenderer = new TeradataDateAddSubOperatorRenderer(
			OperatorType.SUB);

	public TeradataDateIntervalOperatorRenderer() {
		super();
	}

	@Override
	public String prettyPrint(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		super.validateArgs(skin, opDef, args);
		// Time difference computation

		// Extract periods & convert them into the desired period
		String txt = "(";
		int position = periods.indexOf(args[2].trim().replaceAll("'", ""));
		if (position == -1) {
			throw new RuntimeException("The last argument must be a valid period");
		}
		ExtendedType[] extendedTypes = getExtendedPieces(piece);
		String complement = "";
		TeradataCastOperatorRenderer tcor = new TeradataCastOperatorRenderer();
		DateOperatorDefinition opDefSub = new DateOperatorDefinition("DATE_SUB", DateOperatorDefinition.DATE_SUB,
				IDomain.DATE, OperatorDefinition.DATE_TIME_TYPE);
		CastOperatorDefinition toDate = new CastOperatorDefinition("TO_DATE", CastOperatorDefinition.TO_DATE,
				IDomain.DATE);
		CastOperatorDefinition toTimestamp = new CastOperatorDefinition("TO_TIMESTAMP",
				CastOperatorDefinition.TO_TIMESTAMP, IDomain.TIMESTAMP);
		CastOperatorDefinition toNumber = new CastOperatorDefinition("TO_NUMBER", CastOperatorDefinition.TO_NUMBER,
				IDomain.NUMERIC);
		for (int i = 0; i < periods.size(); i++) {

			String[] subArgsSub = new String[2];
			String[] subArgsCast1 = new String[1];
			String[] subArgsCast2 = new String[1];
			subArgsCast1[0] = args[0];
			subArgsCast2[0] = args[1];
			OperatorPiece subOperatorPiece = piece;
			if (periods.get(i).equals("DAY")) {
				for (int j = 0; j < 2; j++) {
					if (!extendedTypes[j].getDomain().isInstanceOf(IDomain.TIMESTAMP)
							&& extendedTypes[j].getDomain().isInstanceOf(IDomain.DATE)) {
						subArgsSub[j] = args[j];
					} else {
						String[] subArgsCast = new String[1];
						subArgsCast[0] = args[j];
						subArgsSub[j] = tcor.prettyPrint(skin, piece, toDate, subArgsCast);
					}
				}
				IPiece piece1 = new SimpleConstantValuePiece(subArgsSub[0], ExtendedType.DATE);
				IPiece piece2 = new SimpleConstantValuePiece(subArgsSub[1], ExtendedType.DATE);
				subOperatorPiece = new OperatorPiece(opDefSub, new IPiece[] { piece1, piece2 },
						new ExtendedType[] { ExtendedType.DATE, ExtendedType.DATE });
			} else {
				for (int j = 0; j < 2; j++) {
					if (extendedTypes[j].getDomain().isInstanceOf(IDomain.TIMESTAMP)) {
						subArgsSub[j] = args[j];
					} else {
						String[] subArgsCast = new String[1];
						subArgsCast[0] = args[j];
						subArgsSub[j] = tcor.prettyPrint(skin, piece, toTimestamp, subArgsCast);
					}
				}
			}
			String intervalAsDate = dateSubRenderer.prettyPrint(skin, subOperatorPiece, opDefSub, subArgsSub);
			String[] subArgsExtract = new String[1];
			subArgsExtract[0] = intervalAsDate;
			String operation = "";
			if (periods.get(i).equals("DAY")) {
				operation = subArgsExtract[0];
			} else {
				ExtractOperatorRenderer extractOperatorRenderer = new ExtractOperatorRenderer(periods.elementAt(i));
				if (subArgsSub[1].indexOf("1970-01-01 00:00:00") == -1) {
					operation = "("
							+ extractOperatorRenderer.prettyPrint(skin, piece, opDef, new String[] { subArgsSub[0] })
							+ " - "
							+ extractOperatorRenderer.prettyPrint(skin, piece, opDef, new String[] { subArgsSub[1] })
							+ ")";
				} else {
					operation = extractOperatorRenderer.prettyPrint(skin, piece, opDef, new String[] { subArgsSub[0] });
				}
			}
			if (i < position) {
				operation = skin.render(skin,
						new OperatorPiece(toNumber, new IPiece[] { subOperatorPiece },
								new ExtendedType[] { subOperatorPiece.getType() }),
						toNumber, new String[] { operation });
			}
			txt += complement + operation;
			if (i < position) {
				for (int j = i; j < position; j++) {
					txt += "/" + multipliers.elementAt(j + 1);
				}
			} else {
				for (int j = i; j > position; j--) {
					txt += "*" + multipliers.elementAt(j);
				}
			}
			complement = " + ";
		}
		if (position > 0) {
			txt = "CAST" + txt + " AS INTEGER";
		}
		txt += ")";
		return txt;
	}

}
