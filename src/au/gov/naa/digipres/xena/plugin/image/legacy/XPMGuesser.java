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
 * Guesser for the BMC Software Patrol UNIX Icon file format
 * 
 */
public class XPMGuesser extends DefaultGuesser {
	// XPM Format
	private static final byte[][] xpmMagic = {{0x2F, 0x2A, 0x20, 0x58, 0x50, 0x4D, 0x20, 0x2A, 0x2F, 0x0A}};
	private static final String[] xpmExtensions = {"xpm"};
	private static final String[] xpmMime = {"image/x-xpixmap", "image/xpm", "image/x-xpm"};

	private FileTypeDescriptor[] legacyFileDescriptors = {new FileTypeDescriptor(xpmExtensions, xpmMagic, xpmMime)};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public XPMGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(XPMFileType.class);
	}

	@Override
	public String getName() {
		return "XPM Guesser";
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return legacyFileDescriptors;
	}

	@Override
	public Type getType() {
		return type;
	}

}
