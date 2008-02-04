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

package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for JAI supported image types other than the core JPEG and PNG
 * 
 */
public class ImageGuesser extends DefaultGuesser {
	private static final byte[][] gifMagic = {{'G', 'I', 'F'}};
	private static final String[] gifExtensions = {"gif"};
	private static final String[] gifMime = {"image/gif"};

	private static final byte[][] bmpMagic = {{'B', 'M'}};
	private static final String[] bmpExtensions = {"bmp"};
	private static final String[] bmpMime = {"image/bmp"};

	private FileTypeDescriptor[] descriptorArr =
	    {new FileTypeDescriptor(gifExtensions, gifMagic, gifMime), new FileTypeDescriptor(bmpExtensions, bmpMagic, bmpMime)};

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public ImageGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(ImageFileType.class);
	}

	@Override
	public String getName() {
		return "ImageGuesser";
	}

	@Override
	public Type getType() {
		return type;
	}

	/**
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

}
