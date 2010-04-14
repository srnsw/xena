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
* CanooOptions.java
* ---------------
*/
package org.jpedal.constants;

/**
 * specific flags used in Custom versions of JPedal - will have
 * no effect in main version
 */
public class SpecialOptions {

    public static final int TRANSFER_AS_IMAGE = 0;
    public static final int TRANSFER_AS_OBJECT = 1;
    public static final int TRANSFER_AS_DECODE_FONTS_ON_CLIENT = 2;
    public static final int TRANSFER_DECODE_ON_CLIENT = 3;
    
	public static final int SWING_WIDGETS_ON_CLIENT = 4;
	public static final int ULC_WIDGETS_ON_SERVER = 5;

    public static final int NONE = -1;
    
    public static final int SINGLE_PAGE =1;
    public static final int HORIZONTAL_DOUBLE_PAGE =2;
    public static final int VERTICAL_DOUBLE_PAGE=3;

}
