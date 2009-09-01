
package com.softhub.ps.graphics;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import com.softhub.ps.util.CharWidth;
import java.awt.geom.Rectangle2D;

public class CharacterShape extends DisplayList implements Reusable {

	private String charCode;
	private CharWidth charWidth;

	public CharacterShape() {
		super(1);
	}

	public void setCharCode(String charCode) {
		this.charCode = charCode;
	}

	public String getCharCode() {
		return charCode;
	}

	public void setCharWidth(CharWidth charWidth) {
		this.charWidth = charWidth;
	}

	public CharWidth getCharWidth() {
		return charWidth;
	}

	public Rectangle2D getBounds2D() {
		Rectangle2D rect = null;
		for (int i = 0; i < index; i++) {
			Rectangle2D r = buffer[i].getBounds2D();
			if (r != null) {
				if (rect == null) {
					rect = r;
				} else {
					rect = rect.createUnion(r);
				}
			}
		}
		return rect;
	}

	public String toString() {
		return "character-shape<" + charCode + ">";
	}

}
