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

package au.gov.naa.digipres.xena.plugin.html.javatools.util;

/**
 * This class returns the bit that you tag onto the end of number. e.g. if you
 * pass in the number "1" it will return "st", as in 1st. If you pass in
 * "2" it will return "nd", as in "2nd". etc.
 * Yes, the name is pretty wierd, but what can you name something like this?
 */
public class OrdinalPostfix {
	final static String st = "st";
	final static String nd = "nd";
	final static String rd = "rd";
	final static String th = "th";

	public static String postfix(int number) {
		int mod100 = number % 100;
		int mod10 = number % 10;
		if (mod100 == 11 || mod100 == 12 || mod100 == 13) {
			return th;
		} else if (mod10 == 1) {
			return st;
		} else if (mod10 == 2) {
			return nd;
		} else if (mod10 == 3) {
			return rd;
		} else {
			return th;
		}
	}
}
