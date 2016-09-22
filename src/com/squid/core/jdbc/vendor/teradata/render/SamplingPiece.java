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

import com.squid.core.sql.render.DelegateSamplingDecorator;
import com.squid.core.sql.render.ISamplingDecorator;
import com.squid.core.sql.render.RenderingException;
import com.squid.core.sql.render.SQLSkin;

public class SamplingPiece implements ISamplingDecorator {

	private double size;
	private double percent;
	private int mode = FRACTION;

	public SamplingPiece(DelegateSamplingDecorator sampling) {
		super();
		setSize(sampling.getSize(), sampling.getMode());
		setPercent(sampling.getPercent(), sampling.getMode());
	}

	@Override
	public double getSize() {
		return size;
	}

	@Override
	public int getMode() {
		return mode;
	}

	@Override
	public void setSize(double size, int mode) {
		this.size = size;
		this.mode = mode;
	}

	@Override
	public String render(SQLSkin skin) throws RenderingException {
		String render = "";
		render += " " + skin.getToken(TeradataSQLTokenConstant.SAMPLE) + " ";
		switch (mode) {
		case FRACTION:
			render += "" + percent + "";
			break;
		case COUNT:
			render += "" + size + "";
			break;
		}
		return render;
	}

	@Override
	public double getPercent() {
		return percent;
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}

	@Override
	public void setPercent(double percent, int mode) {
		this.percent = percent;
		this.mode = mode;
		if (percent > 1) {
			this.percent = this.percent / 100;
		}
	}

}
