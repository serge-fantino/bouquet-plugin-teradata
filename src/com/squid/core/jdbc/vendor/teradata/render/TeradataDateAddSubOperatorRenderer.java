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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squid.core.domain.IDomain;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.DateAddSubOperatorRenderer;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class TeradataDateAddSubOperatorRenderer extends DateAddSubOperatorRenderer {

	public TeradataDateAddSubOperatorRenderer(OperatorType operatorType) {
		super(operatorType);

	}

	@Override
	protected String getSqlCode(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args,
			OperatorType type) throws RenderingException {
		String txt = "";
		ExtendedType[] extendedTypes = getExtendedPieces(piece);

		/**
		 * In case extendedTypes have 3 elements (for example: date, timestamp,
		 * string ), the type computed always is DATE or TIMESTAMP, but we hope
		 * that it's INTERVAL. So we remove the 3th type of extendedTypes in
		 * order for the operator definition to compute exact type.
		 */

		if (extendedTypes.length == 3 && (extendedTypes[0].getDomain().isInstanceOf(IDomain.TIMESTAMP)
				&& extendedTypes[1].getDomain().isInstanceOf(IDomain.TIMESTAMP)
				|| extendedTypes[0].getDomain().isInstanceOf(IDomain.DATE)
						&& extendedTypes[0].getDomain().isInstanceOf(IDomain.TIMESTAMP) == false
						&& extendedTypes[1].getDomain().isInstanceOf(IDomain.TIMESTAMP)
				|| extendedTypes[0].getDomain().isInstanceOf(IDomain.TIMESTAMP)
						&& extendedTypes[1].getDomain().isInstanceOf(IDomain.DATE)
						&& extendedTypes[1].getDomain().isInstanceOf(IDomain.TIMESTAMP) == false
				|| extendedTypes[0].getDomain().isInstanceOf(IDomain.INTERVAL))) {
			List<ExtendedType> list = new ArrayList<ExtendedType>(Arrays.asList(extendedTypes));
			list.remove(2);
			extendedTypes = list.toArray(new ExtendedType[0]);
		}

		ExtendedType extendedType = opDef.computeExtendedType(extendedTypes);
		boolean isInterval = extendedType.getDomain().isInstanceOf(IDomain.INTERVAL);
		if (isInterval) {
			txt += "(";
		}
		txt += super.getSqlCode(skin, piece, opDef, args, type);
		if (isInterval) {
			txt += ") DAY(4) TO SECOND";
		}
		return txt;

	}
}
