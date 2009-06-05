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
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.office.presentation;

import au.gov.naa.digipres.xena.plugin.office.OfficeFileType;

/**
 * Type to represent the SXI file type (presentation format in early versions of OpenOffice.org)
 *
 */
public class SxiFileType extends OfficeFileType {

	@Override
	public String getName() {
		return "OpenOffice.org v1 Presentation";
	}

	@Override
	public String getMimeType() {
		return "application/vnd.sun.xml.impress";
	}

	@Override
	public String getOfficeConverterName() {
		return "impress8";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.FileType#fileExtension()
	 */
	@Override
	public String fileExtension() {
		return "sxi";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OfficeFileType#getTextConverterName()
	 */
	@Override
	public String getTextConverterName() {
		throw new IllegalStateException("OpenOffice.org does not have a plain text converter for presentations. "
		                                + "This file type should not have been linked to a TextNormaliser in the OfficePlugin!");
	}

}
