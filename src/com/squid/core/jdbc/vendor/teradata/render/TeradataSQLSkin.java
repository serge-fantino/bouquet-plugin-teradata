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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import com.squid.core.database.model.Column;
import com.squid.core.database.model.DatabaseProduct;
import com.squid.core.database.model.Table;
import com.squid.core.domain.CustomTypes;
import com.squid.core.domain.IDomain;
import com.squid.core.domain.extensions.date.operator.DateOperatorDefinition;
import com.squid.core.domain.operators.ExtendedType;
import com.squid.core.domain.operators.IntrinsicOperators;
import com.squid.core.domain.operators.OperatorDefinition;
import com.squid.core.sql.db.render.FromTablePiece;
import com.squid.core.sql.db.templates.DefaultJDBCSkin;
import com.squid.core.sql.db.templates.ISkinProvider;
import com.squid.core.sql.render.DelegateSamplingDecorator;
import com.squid.core.sql.render.ExpressionListPiece;
import com.squid.core.sql.render.IPiece;
import com.squid.core.sql.render.ISamplingDecorator;
import com.squid.core.sql.render.InConditionPiece;
import com.squid.core.sql.render.OperatorPiece;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;
import com.squid.core.sql.render.SQLTokenConstant;

public class TeradataSQLSkin extends DefaultJDBCSkin {

	protected TeradataSQLSkin(ISkinProvider provider, DatabaseProduct product) {
		super(provider, product);
	}

	@Override
	protected void initFormat() {
		super.initFormat();
		setIdentifier_quote("\"");
		setLiteral_quote("\'");
	}

	@Override
	public String overrideTemplateID(String templateID) {
		// TODO Auto-generated method stub
		return super.overrideTemplateID(templateID);
	}

	@Override
	public String getToken(int token) throws RenderingException {
		switch (token) {
		case TeradataSQLTokenConstant.SAMPLE:
			return "SAMPLE";
		default:
			return super.getToken(token);
		}
	}

	@Override
	public ISamplingDecorator createSamplingDecorator(DelegateSamplingDecorator sampling) {
		return new SamplingPiece(sampling);
	}

	@Override
	public String render(SQLSkin skin, IPiece piece) throws RenderingException {
		if (piece instanceof InConditionPiece) {
			InConditionPiece in = (InConditionPiece) piece;
			if (in.getLeft().getPieces().length > 1) {
				return render(skin, in);
			}
		}
		// else
		return super.render(skin, piece);
	}

	protected String render(SQLSkin skin, InConditionPiece piece) throws RenderingException {
		// inline the expression
		String result = "";
		int length = piece.getLeft().getPieces().length;
		for (IPiece right : piece.getRight().getPieces()) {
			if (right instanceof ExpressionListPiece) {
				ExpressionListPiece piecesR = (ExpressionListPiece) right;
				if (piecesR.getPieces().length != length) {
					throw new RenderingException("Invalid SQL expression");
				}
				if (result != "") {
					result += getToken(SQLTokenConstant.OR);
				}
				result += "(";
				for (int i = 0; i < length; i++) {
					if (i > 0) {
						result += getToken(SQLTokenConstant.AND);
					}
					result += "(";
					result += piece.getLeft().getPieces()[i].render(this);
					if (piece.isNot()) {
						result += "!=";
					} else {
						result += "=";
					}
					result += piecesR.getPieces()[i].render(this);
					result += ")";
				}
				result += ")";
			} else {
				throw new RenderingException(
						"Teradata not supporting that SQL expression: multi-columns constant IN operator");
			}
		}
		return result;
	}

	@Override
	protected String render(SQLSkin skin, FromTablePiece piece) throws RenderingException {
		//
		String render = "(";// need to inforce evaluation order because outer
							// joins are not associative/commutative operations
		//
		final Table table = piece.getTable();
		if (table == null) {
			throw new RenderingException("table definition is null");
		}
		if (table.getSchema() != null && !table.getSchema().isNullSchema()) {
			render += skin.quoteSchemaIdentifier(table.getSchema());
			render += ".";
		}
		render += skin.quoteTableIdentifier(table);
		//
		// alias
		render += " " + piece.getAlias();
		//
		// joining
		render += renderJoinDecorator(skin, piece);
		//
		render += ")";
		//
		// sampling (must be outside of the parenthesis)
		if (piece.getSamplingDecorator() != null) {
			render += " " + piece.getSamplingDecorator().render(this);
		}
		//
		return render;
	}

	@Override
	public String render(SQLSkin skin, OperatorPiece piece, OperatorDefinition opDef, String[] args)
			throws RenderingException {
		if (opDef.getId() == IntrinsicOperators.CONCAT) {
			return opDef.prettyPrint("||", OperatorDefinition.INFIX_POSITION, args, true);
		} else if (opDef.getId() == IntrinsicOperators.NOT_EQUAL) {
			return opDef.prettyPrint("<>", OperatorDefinition.INFIX_POSITION, args, true);
		} else if (opDef.getId() == IntrinsicOperators.MODULO) {
			return opDef.prettyPrint(" MOD ", OperatorDefinition.INFIX_POSITION, args, true);
		}
		// else
		return super.render(skin, piece, opDef, args);
	}

	@Override
	public String getTypeDefinition(Column type) {
		if (type == null) {
			return "NULL";
		}
		switch (type.getType().getDataType()) {
		case Types.BIGINT:
			return "DECIMAL(18,0)";
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.FLOAT:
		case Types.DATE:
			return type.getName();
		case Types.DOUBLE:
			return "FLOAT";
		case Types.TIME:
			return "TIME(" + type.getType().getScale() + ")";
		case Types.TIMESTAMP:
			return "TIMESTAMP(" + type.getType().getScale() + ")";
		case Types.TINYINT:
			return "BYTEINT";
		case Types.CHAR:
		case Types.LONGNVARCHAR:
		case Types.LONGVARCHAR:
		case Types.NVARCHAR:
		case Types.VARCHAR:
			return super.getTypeDefinition(type) + " CHARACTER SET UNICODE CASESPECIFIC";
		case CustomTypes.INTERVAL:
			return "INTERVAL DAY(4) TO SECOND";
		default:
			return super.getTypeDefinition(type);
		}
	}

	@Override
	public OperatorDefinition overrideOperatorDefinition(OperatorDefinition op, ExtendedType[] args) {
		if (op.getExtendedID() == DateOperatorDefinition.DATE_SUB
				&& (args[0].getDomain() == IDomain.TIMESTAMP && args[1].getDomain() == IDomain.TIMESTAMP
						|| args[0].getDomain() == IDomain.INTERVAL)) {
			return new DateOperatorDefinition(op.getName(), op.getExtendedID(), IDomain.INTERVAL);
		} else {
			return super.overrideOperatorDefinition(op, args);
		}
	}

	@Override
	public boolean isShareNothing() {
		return true;
	}

	@Override
	public void initializeConnection(Connection conn) {
		try {
			if (conn.getMetaData().supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED)) {
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
