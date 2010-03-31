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
 * DecodeStatus.java
 * ---------------
 */
package org.jpedal.parser;

/*
 * flags which can return values after page decode
 */
public class DecodeStatus {
    final public static int PageDecodingSuccessful=1;
    final public static int ImagesProcessed=2;
    final public static int NonEmbeddedCIDFonts=4;
    final public static int YCCKImages=8;
}
