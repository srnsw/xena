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
* sleep.java
* ---------------
*/
package org.jpedal.utils;

/**
 * provide a delay
 */
public class sleep extends Thread
{
	
	/**
	 *The time the program sleeps (in seconds) before repolling incoming dir
	 */
	private int delay = 5;
	
	
	/**
	 * pass through value of sleep in seconds
	 */
	public sleep( int delay ) 
	{
		this.delay = delay;
		try
		{
			yield();
			sleep( delay);
		}
		catch( Exception e )
		{e.printStackTrace();}
	}
	
}
