
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

public interface Stoppable {

	public final static int TYPECHECK = 1;
	public final static int STACKOVERFLOW = 2;
	public final static int STACKUNDERFLOW = 3;
	public final static int EXSTACKOVERFLOW = 4;
	public final static int DICTSTACKOVERFLOW = 5;
	public final static int DICTSTACKUNDERFLOW = 6;
	public final static int UNDEFINED = 7;
	public final static int UNDEFINEDRESULT = 8;
	public final static int RANGECHECK = 9;
	public final static int UNMATCHEDMARK = 10;
	public final static int LIMITCHECK = 11;
	public final static int SYNTAXERROR = 12;
	public final static int INVALIDACCESS = 13;
	public final static int INVALIDEXIT = 14;
	public final static int INVALIDRESTORE = 15;
	public final static int UNDEFINEDFILENAME = 16;
	public final static int UNDEFINEDRESOURCE = 17;
	public final static int INVALIDFILEACCESS = 18;
	public final static int INVALIDFONT = 19;
	public final static int IOERROR = 20;
	public final static int NOCURRENTPOINT = 21;
	public final static int SECURITYCHECK = 22;
	public final static int INTERRUPT = 23;
	public final static int INTERNALERROR = 24;
	public final static int TIMEOUT = 25;

}
