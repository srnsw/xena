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
* SearchType.java
* ---------------
*/
package org.jpedal.grouping;

/**
 * This class holds constants used to describe the nature of a given search.  Methods
 * in the PdfGroupingAlgorithums class that take a search type parameter will take
 * either one, or a combination of the values contained in this class.  Multiple
 * constants can be used by using the logical or operator.
 */
public class SearchType {

	/**
	 * The default parameter, this describes a search that will not be limited to finding
	 * whole words only, is not case-sensitive, and will find all occurrences.
	 */
	public static final int DEFAULT = 0;
	
	/**
	 * Used to describe a search that will find whole words only
	 */
	public static final int WHOLE_WORDS_ONLY = 1;
	
	/**
	 * Used to describe a search that is case-sensitive
	 */
	public static final int CASE_SENSITIVE = 2;
	
	/**
	 * Used to describe a search that will find first occurrences only
	 */
	public static final int FIND_FIRST_OCCURANCE_ONLY = 4;
	
	/**
	 * USed to describe a search that will find results split across multiple lines
	 */
	public static final int MUTLI_LINE_RESULTS = 8;

}
