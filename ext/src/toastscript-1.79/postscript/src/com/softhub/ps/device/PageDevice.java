
package com.softhub.ps.device;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import com.softhub.ps.graphics.Drawable;

public interface PageDevice extends Device {

	/**
	 * Set the page size.
	 * @param w the width in 1/72 of an inch
	 * @param h the height in 1/72 of an inch
	 */
	void setPageSize(float size[/* 2 */]);

	/**
	 * Get the page size.
	 * @return size array of 2 elements {width, height}
	 */
	float[/* 2 */] getPageSize();

	/**
	 * @return the page width in 1/72 inch
	 */
	float getPageWidth();

	/**
	 * @return the page height in 1/72 inch
	 */
	float getPageHeight();

	/**
	 * Set the page orientation.
	 * @param mode [0..3]
	 */
	void setOrientation(int mode);

	/**
	 * Get the page orientation.
	 * @return orientation [0..3]
	 */
	int getOrientation();

	/**
	 * Set the scale factor.
	 * @param the absolute scale factor
	 */
	void setScale(float scale);

	/**
	 * @return the scale factor
	 */
	float getScale();

	/**
	 * Set the number of copies for showpage to print.
	 * @param the number of copies
	 */
	void setNumCopies(int copies);

	/**
	 * @return the number of copies
	 */
	int getNumCopies();

	/**
	 * @return the current page
	 */
	Drawable getContent();

	/**
	 * Add a page event listener.
	 * @param the listener to add
	 */
	void addPageEventListener(PageEventListener listener);

	/**
	 * Remove a page event listener.
	 * @param the listener to remove
	 */
	void removePageEventListener(PageEventListener listener);

}
