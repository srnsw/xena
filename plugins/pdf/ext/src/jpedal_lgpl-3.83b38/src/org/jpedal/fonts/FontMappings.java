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
* FontMappings.java
* ---------------
*/
package org.jpedal.fonts;

import java.util.Hashtable;
import java.util.Map;

/**
 * Holds Maps which are used to map font names onto actual fonts and files
 */
public class FontMappings {

	/**
     * used to remap fonts onto truetype fonts (set internally)
     */
    public static Map fontSubstitutionTable = null;
    
    /**
     * hold details of all fonts
     */
    public static Map fontPropertiesTable=null;

    /**
     * used to ensure substituted fonts unique
     */
    public static Map fontPossDuplicates=null;

    /**
     * used to store number for subfonts in TTC
     */
    public static Map fontSubstitutionFontID = null;

    /**
     * used to remap fonts onto truetype fonts (set internally)
     */
    public static Map fontSubstitutionLocation = new Hashtable();

    /**
     * used to remap fonts onto truetype fonts (set internally)
     */
    public static Map fontSubstitutionAliasTable = new Hashtable();
    
	
   private FontMappings(){}

    public String toString(){
    	return "Static font maps: fontSubstitutionTable, fontSubstitutionFontID, fontSubstitutionLocation, fontSubstitutionAliasTable";
    }
    
    public static void dispose(){
    	
    	fontSubstitutionTable=null;
    	fontPropertiesTable=null;
    	fontPossDuplicates=null;
    	fontSubstitutionFontID = null;
    	fontSubstitutionLocation = null;
    	fontSubstitutionAliasTable = null;
    }

}
