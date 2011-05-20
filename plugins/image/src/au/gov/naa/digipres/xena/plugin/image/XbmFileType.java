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
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent the XBM (X11 BitMap) image format
 *
 */
public class XbmFileType extends FileType {

	@Override
	public String getName() {
		return "XBM";
	}

	@Override
	public String getMimeType() {
		return "image/x-xbitmap";
		//TODO: How do we handle 2 mime types - image/x-xbitmap AND image/x-xbm
	}
}
