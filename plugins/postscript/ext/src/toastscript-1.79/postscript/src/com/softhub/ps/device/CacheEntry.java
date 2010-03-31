
package com.softhub.ps.device;

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

import com.softhub.ps.graphics.Reusable;
import com.softhub.ps.util.CharWidth;

class CacheEntry {

	private String character;
	private Object fontID;
	private int fontType;
	private Reusable obj;
	private CharWidth charWidth;

	CacheEntry(FontInfo info, String character) {
		fontID = info.getFontUniqueID();
		fontType = info.getFontType();
		this.character = character;
	}

	CacheEntry(FontInfo info, String ch, CharWidth cw, Reusable obj) {
		this(info, ch);
		this.obj = obj;
		this.charWidth = cw;
	}

	Reusable getObject() {
		return obj;
	}

	CharWidth getCharWidth() {
		return charWidth;
	}

	public int hashCode() {
		return character.hashCode() ^ fontID.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CacheEntry))
			return false;
		CacheEntry entry = (CacheEntry) obj;
		return character.equals(entry.character) &&
			   fontID.equals(entry.fontID) &&
			   fontType == entry.fontType;
	}

}
