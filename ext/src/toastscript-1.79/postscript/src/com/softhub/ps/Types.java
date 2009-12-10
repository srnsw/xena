
package com.softhub.ps;

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

public interface Types {

	public final static int ANY = -1;
	public final static int INTEGER = 1<<0;
	public final static int REAL = 1<<1;
	public final static int NUMBER = INTEGER | REAL;
	public final static int BOOLEAN = 1<<2;
	public final static int NAME = 1<<3;
	public final static int STRING = 1<<4;
	public final static int ARRAY = 1<<5;
	public final static int DICT = 1<<6;
	public final static int OPERATOR = 1<<7;
	public final static int NULL = 1<<8;
	public final static int MARK = 1<<9;
	public final static int FILE = 1<<10;
	public final static int FONTID = 1<<11;
	public final static int SAVE = 1<<12;
	public final static int GSTATE = 1<<13;

}
