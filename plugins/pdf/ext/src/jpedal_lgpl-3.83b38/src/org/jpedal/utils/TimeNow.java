/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
*
* 	This file is part of JPedal
*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


*
* ---------------
* TimeNow.java
* ---------------
*/
package org.jpedal.utils;
import java.text.DateFormat;
import java.util.Calendar;

/**
 * return date of time now
 * int shortdate format
 * <br><b>This class is NOT part of the API</b>
 */
public class TimeNow
{
	public TimeNow() 
	{
		
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get date as YYYYMMDDHHMMSS
	 */
	final static public String getShortTimeNow()
	{
		Calendar now = Calendar.getInstance();
		String format_string = now.get( Calendar.YEAR ) + format( 1 + now.get( Calendar.MONTH ) ) + format( now.get( Calendar.DAY_OF_MONTH ) ) + format( now.get( Calendar.HOUR_OF_DAY ) ) + format( now.get( Calendar.MINUTE ) ) + format( now.get( Calendar.SECOND ) );
		return format_string;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * get Time as a string
	 */
	final static public String getTimeNow()
	{
		DateFormat short_date = DateFormat.getDateInstance();
		DateFormat short_time = DateFormat.getTimeInstance( DateFormat.SHORT );
		return ( short_date.format( new java.util.Date() ) + ' ' + short_time.format( new java.util.Date() ) );
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * pad out to 2 chars
	 */
	final private static String format( int number )
	{
		String value = String.valueOf(number);
		if( value.length() == 1 )
			value = '0' + value;
		return value;
	}
}
