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
* ToInteger.java
* ---------------
*/
package org.jpedal.utils;

/**
 * convert a String into an int
 * <br><b>This class is NOT part of the API</b>
 */
public class ToInteger
{
	public ToInteger() 
	{
		
	}
	
	////////////////////////////////////////////////////////////////////////
	/**
	 * make a string into an integer
	 */
	final static public int getInteger( String value )
	{
		int pointer = value.indexOf('.');
		if( pointer > 0 )
			value = value.substring( 0, pointer );
		int return_value = 0;
		if( value.length() > 0 )
		{
			try
			{
				return_value = Integer.parseInt( value );
			}
			catch( Exception e )
			{
				LogWriter.writeLog( "Exception " + e + " in converting " + value + " to number" );
			}
		}
		return return_value;
	}
}
