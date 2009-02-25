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

package au.gov.naa.digipres.xena.plugin.image.legacy;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for the Windows Cursor image format
 */
public class CURGuesser extends DefaultGuesser {
	// Windows Cursor Format
	private static final byte[][] curMagic = {{0x00, 0x00, 0x02, 0x00}};
	private static final String[] curExtensions = {"cur"};
	private static final String[] curMime = {"image/x-win-bitmap"};

	private FileTypeDescriptor[] fileDescriptors = {new FileTypeDescriptor(curExtensions, curMagic, curMime)};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public CURGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(CURFileType.class);
	}

	@Override
	public String getName() {
		return "Windows Cursor Guesser";
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return fileDescriptors;
	}

	@Override
	public Type getType() {
		return type;
	}

}
