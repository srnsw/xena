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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.kernel.type;

import java.io.File;

import javax.print.Doc;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Class used to provide a mechanism to produce javax.print.Doc objects from a File, specific to each Xena Type.
 * This may eventually form part of a printing system to print out Xena files, but is currently not in use.
 * created 5/05/2006
 * xena
 * Short desc of class:
 */
abstract public class TypePrinter {

	protected TypePrinterManager typePrinterManager;

	/**
	 * @return Returns the typePrinterManager.
	 */
	public TypePrinterManager getTypePrinterManager() {
		return typePrinterManager;
	}

	/**
	 * @param typePrinterManager The new value to set typePrinterManager to.
	 */
	public void setTypePrinterManager(TypePrinterManager typePrinterManager) {
		this.typePrinterManager = typePrinterManager;
	}

	/**
	 * Return the specfic XenaFileType for this TypePrinter.
	 * @return XenaFileType
	 * @throws XenaException
	 */
	abstract public XenaFileType getType() throws XenaException;

	/**
	 * Return a Doc object representing the given File, assuming the file is the same type as that of this TypePrinter.
	 * @param file - input File
	 * @return Doc representing the given File
	 * @throws XenaException
	 */
	abstract public Doc getDoc(File file) throws XenaException;
}
