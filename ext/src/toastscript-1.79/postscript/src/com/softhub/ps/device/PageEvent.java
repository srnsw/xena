
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

public class PageEvent {

	public final static int ERROR = 0;
	public final static int BEGINJOB = 1;
	public final static int ENDJOB = 2;
	public final static int RESIZE = 3;
	public final static int SHOWPAGE = 4;
	public final static int COPYPAGE = 5;
	public final static int ERASEPAGE = 6;

	private int type;
	private PageDevice device;

	public PageEvent(int type, PageDevice device) {
		this.type = type;
		this.device = device;
	}

	public int getType() {
		return type;
	}

	public PageDevice getPageDevice() {
		return device;
	}

	public String toString() {
		switch (type) {
		case ERROR:
			return "ERROR";
		case BEGINJOB:
			return "BEGINJOB";
		case ENDJOB:
			return "ENDJOB";
		case RESIZE:
			return "RESIZE";
		case SHOWPAGE:
			return "SHOWPAGE";
		case COPYPAGE:
			return "COPYPAGE";
		case ERASEPAGE:
			return "ERASEPAGE";
		default:
			return super.toString();
		}
	}

}
