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

package au.gov.naa.digipres.xena.plugin.archive.zip;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * 
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class ZipGuesser extends DefaultGuesser {
	// Zip Format
	private static final byte[][] zipMagic = {{0x50, 0x4B, 0x03, 0x04}};
	private static final String[] zipExtensions = {"zip"};
	private static final String[] zipMime = {"application/zip"};

	// JAR format
	private static final byte[][] jarMagic = {{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x08, 0x00, 0x08, 0x00}};
	private static final String[] jarExtensions = {"jar"};
	private static final String[] jarMime = {"application/java-archive"};

	private FileTypeDescriptor[] zipFileDescriptors =
	    {new FileTypeDescriptor(zipExtensions, zipMagic, zipMime), new FileTypeDescriptor(jarExtensions, jarMagic, jarMime)};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public ZipGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(ZipFileType.class);
	}

	@Override
	public String getName() {
		return "ZipGuesser";
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return zipFileDescriptors;
	}

	@Override
	public Type getType() {
		return type;
	}
}
