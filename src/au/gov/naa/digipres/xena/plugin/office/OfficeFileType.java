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

/*
 * Created on 12/04/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Class to indicate that a file is an Office file type.
 * Subclasses must implement two methods, one to provide the name of the OpenOffice.org converter
 * to convert this file type to ODF, and the other to provide the name of the OpenOffice.org converter
 * to convert this file type to plain text.
 * 
 * @author Justin Waddell
 *
 */
public abstract class OfficeFileType extends FileType {

	/**
	 * Return the name of the OpenOffice.org converter to convert this file type to ODF.
	 * @return OpenOffice.org converter name
	 */
	public abstract String getOfficeConverterName();

	/**
	 * Return the name of the OpenOffice.org converter to convert this file type to plain text.
	 * @return OpenOffice.org converter name
	 */
	public abstract String getTextConverterName();
}
