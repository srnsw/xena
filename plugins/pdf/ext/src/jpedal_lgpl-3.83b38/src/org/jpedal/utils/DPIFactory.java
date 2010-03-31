/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2008, IDRsolutions and Contributors.
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

  * DPIFactory.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.utils;

public class DPIFactory {
	
	//Current DPI value, Java is 72 by default
	private float dpi = 72f;

	/**
	 * Corrects the image scaling to take into account the user specified DPI value
	 * @param scaling :: Raw scaling value before DPI is applied (DPI of 72 is default)
	 * @return Corrected scaling in the form of a float
	 */
    public float adjustScaling(float scaling) {
    		return scaling * (dpi/72f);
    }
    
    /**
	 * Corrects the image scaling to take into account the user specified DPI value
	 * @param scaling :: Raw scaling value before DPI is applied (DPI of 72 is default)
	 * @return Corrected scaling in the form of a float
	 */
    public float removeScaling(float scaling) {
    		return scaling / (dpi/72f);
    }
    
    /**
     * Get the current value of the user defined DPI (default = 72)
     * @return :: The current DPI value as a float
     */
	public float getDpi() {
		return dpi;
	}

	/**
	 * Sets the current DPI to the input value dpi
	 * @param dpi :: The new dpi value expressed as a float
	 */
	public void setDpi(float dpi) {
		this.dpi = dpi;
	}
}
