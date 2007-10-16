/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.email.trim;

import javax.mail.Address;

public class TrimAddress extends Address {
	String s;

	public TrimAddress(String s) {
		this.s = s;
	}

	@Override
    public String getType() {
		return "trim";
	}

	@Override
    public String toString() {
		return s;
	}

	@Override
    public boolean equals(Object object) {
		return ((TrimAddress) object).s.equals(s);
	}
}
