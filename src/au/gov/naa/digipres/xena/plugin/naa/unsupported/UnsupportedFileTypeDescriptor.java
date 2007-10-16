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

/*
 * Created on 21/05/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * This class is used by UnsupportedTypeGuesser to easily group a Xena Type 
 * with the information used to identify that file.
 * 
 * created 24/05/2007
 * naa
 * Short desc of class:
 */
public class UnsupportedFileTypeDescriptor extends FileTypeDescriptor {
	private Type type;

	public UnsupportedFileTypeDescriptor(String[] extensions, byte[][] magicNumbers, String[] mimeTypes, Type type) {
		super(extensions, magicNumbers, mimeTypes);
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

}
