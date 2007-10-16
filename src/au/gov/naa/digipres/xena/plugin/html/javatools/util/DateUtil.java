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

import java.sql.*;

/**
 *  A utility class for use with dates.
 *
 * @created    December 13, 2001
 */
public class DateUtil {
	static Date _minusInfinity;
	static Date _plusInfinity;
	final static int LEAP_DAYS_IN_FEB = 29;
	final static int FEB = 1;

	/**
	 *  Return a date represting a long time in the past.
	 *
	 * @return    Description of the Returned Value
	 */
	public static Date minusInfinity() {
		init();
		return _minusInfinity;
	}

	/**
	 *  Return a date representing a long time in the future. Sometimes people who
	 *  don't like to use NULL values in databases use this as a kind of NULL
	 *  value. Alternatively it can be sometimes useful in a SELECT to force use of
	 *  an index.
	 *
	 * @return    Description of the Returned Value
	 */
	public static Date plusInfinity() {
		init();
		return _plusInfinity;
	}

	/**
	 *  Return a Timestamp object representing the time now.
	 *
	 * @return    Description of the Returned Value
	 */
	public static Timestamp timestampNow() {
		return new Timestamp(new java.util.Date().getTime());
	}

	public static boolean leapYear(int year) {
		if (year % 400 == 0) {
			return true;
		} else if (year % 100 == 0) {
			return false;
		} else if (year % 4 == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static int monthSize(int month) {
		final int monthSizes[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		return monthSizes[month];
	}

	public static int maxMonthSize(int month) {
		if (month == FEB) {
			return LEAP_DAYS_IN_FEB;
		} else {
			return monthSize(month);
		}
	}

	/**
	 *  Month is zero based.
	 *
	 * @param  month  Description of Parameter
	 * @param  year   Description of Parameter
	 * @return        Description of the Returned Value
	 */
	public static int daysInMonth(int month, int year) {
		if (month == FEB && leapYear(year)) {
			// February 29th
			return LEAP_DAYS_IN_FEB;
		} else {
			return monthSize(month);
		}
	}

	/**
	 *  month is zero based. date is not.
	 *
	 * @param  date   Description of Parameter
	 * @param  month  Description of Parameter
	 * @param  year   Description of Parameter
	 * @return        Description of the Returned Value
	 */
	public static boolean validDate(int date, int month, int year) {
		return 0 < date && date <= daysInMonth(month, year);
	}

	// Hmm. Visual age doesn't seem to call anonymous {} initialisers.
	// So we have init.
	static void init() {
		if (_minusInfinity == null) {
			synchronized (DateUtil.class) {
				if (_minusInfinity == null) {
					java.util.Calendar cal = java.util.Calendar.getInstance();
					// 0001/01/01
					cal.set(0001, 0, 1);
					java.util.Date d = cal.getTime();
					_minusInfinity = new Date(d.getTime());
					// 9999/12/31
					cal.set(9999, 11, 31);
					d = cal.getTime();
					_plusInfinity = new Date(d.getTime());
				}
			}
		}
	}
}
