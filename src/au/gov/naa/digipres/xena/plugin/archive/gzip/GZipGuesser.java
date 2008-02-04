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

package au.gov.naa.digipres.xena.plugin.archive.gzip;

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
public class GZipGuesser extends DefaultGuesser {
	// GZip Format
	private static final byte[][] gzipMagic = {{0x1F, (byte) 0x8B, 0x08}};
	private static final String[] gzipExtensions = {"gz", "gzip", "tgz"};
	private static final String[] gzipMime = {"application/gzip"};

	private FileTypeDescriptor[] zipFileDescriptors = {new FileTypeDescriptor(gzipExtensions, gzipMagic, gzipMime)};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public GZipGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(GZipFileType.class);
	}

	@Override
	public String getName() {
		return "GZipGuesser";
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
