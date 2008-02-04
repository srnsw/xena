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
 * Guesser for Java supported image types other than the core JPEG and PNG
 * 
 */
public class LegacyImageGuesser extends DefaultGuesser {
	// Leaving these in existing Image functionality for now
	// // GIF Format
	// private static final byte[][] gifMagic = {{ 'G', 'I', 'F' }};
	// private static final String[] gifExtensions = {".gif"};
	// private static final String[] gifMime = {"image/gif"};
	//
	// // TIFF Format
	// private static final byte[][] tiffMagic = {{ 'M', 'M' },
	// { 'I', 'I' }};
	// private static final String[] tiffExtensions = {".tif", ".tiff"};
	// private static final String[] tiffMime = {"image/tiff"};
	//
	// // BMP Format
	// private static final byte[][] bmpMagic = {{ 'B', 'M' }};
	// private static final String[] bmpExtensions = {".bmp"};
	// private static final String[] bmpMime = {"image/bmp"};

	// Macintosh PICT Format
	// PICT does not have a magic number, which means JIMI can't
	// recognise it without the file extension (which is unavailable
	// when data is passed in a stream).
	// Unlikely to be getting many PICTs anyway...
	// private static final byte[][] pictMagic = {{}};
	// private static final String[] pictExtensions = {".pict", ".pct"};
	// private static final String[] pictMime = {};

	// Photoshop PSD Format
	private static final byte[][] psdMagic = {{'8', 'B', 'P', 'S'}};
	private static final String[] psdExtensions = {"psd"};
	private static final String[] psdMime = {"image/photoshop", "image/x-photoshop", "image/psd"};

	// // TARGA TGA Format
	// TGA does not have a magic number, which means JIMI can't
	// recognise it without the file extension (which is unavailable
	// when data is passed in a stream).
	// Unlikely to be getting many TGAs anyway...
	// private static final byte[][] tgaMagic = {{}};
	// private static final String[] tgaExtensions = {".tga", ".targa"};
	// private static final String[] tgaMime = {"image/tga",
	// "image/x-tga",
	// "image/targa",
	// "image/x-targa"};
	// 
	// Windows Icon Format
	// For ICO files JIMI attempts to create an array of colour objects
	// for each possible colour... so for 32-bit images it tries to create
	// a > 2 billion object array... which understandably causes an
	// out of memory error. So ICO files are disabled for now.
	// private static final byte[][] icoMagic = {{0x00, 0x00, 0x01, 0x00}};
	// private static final String[] icoExtensions = {".ico"};
	// private static final String[] icoMime = {"image/ico", "image/x-icon"};

	// Windows Cursor Format
	private static final byte[][] curMagic = {{0x00, 0x00, 0x02, 0x00}};
	private static final String[] curExtensions = {"cur"};
	private static final String[] curMime = {"image/x-win-bitmap"};

	// Sun Raster Format
	private static final byte[][] rasMagic = {{0x59, (byte) 0xA6, 0x6A, (byte) 0x95}};
	private static final String[] rasExtensions = {"ras", "rs", "sun"};
	private static final String[] rasMime = {"image/ras"};

	// XBM Format
	// XBM does not have a magic number, which means JIMI can't
	// recognise it without the file extension (which is unavailable
	// when data is passed in a stream).
	// Unlikely to be getting many XBMs anyway...
	// private static final byte[][] xbmMagic = {{}};
	// private static final String[] xbmExtensions = {".xbm"};
	// private static final String[] xbmMime = {"image/x-xbitmap"};

	// XPM Format
	private static final byte[][] xpmMagic = {{0x2F, 0x2A, 0x20, 0x58, 0x50, 0x4D, 0x20, 0x2A, 0x2F, 0x0A}};
	private static final String[] xpmExtensions = {"xpm"};
	private static final String[] xpmMime = {"image/x-xpixmap", "image/xpm", "image/x-xpm"};

	// Unlikely to be ever used
	// // DCX Format
	// private static final byte[][] dcxMagic = {{(byte)0xB1, 0x68, (byte)0xDE, 0x3A}};
	// private static final String[] dcxExtensions = {".dcx"};
	// private static final String[] dcxMime = {"image/dcx",
	// "image/x-dcx",
	// "image/vnd.swiftview-pcx"};

	private FileTypeDescriptor[] legacyFileDescriptors =
	    {
	    // new ImageDescriptor(gifExtensions, gifMagic, gifMime),
	     // new ImageDescriptor(tiffExtensions, tiffMagic, tiffMime),
	     // new ImageDescriptor(bmpExtensions, bmpMagic, bmpMime),
	     // new ImageDescriptor(pictExtensions, pictMagic, pictMime),
	     new FileTypeDescriptor(psdExtensions, psdMagic, psdMime),
	     // new ImageDescriptor(tgaExtensions, tgaMagic, tgaMime),
	     // new ImageDescriptor(icoExtensions, icoMagic, icoMime),
	     new FileTypeDescriptor(curExtensions, curMagic, curMime), new FileTypeDescriptor(rasExtensions, rasMagic, rasMime),
	     // new ImageDescriptor(xbmExtensions, xbmMagic, xbmMime),
	     new FileTypeDescriptor(xpmExtensions, xpmMagic, xpmMime)
	    // new ImageDescriptor(dcxExtensions, dcxMagic, dcxMime)
	    };

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public LegacyImageGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(LegacyImageFileType.class);
	}

	@Override
	public String getName() {
		return "Legacy ImageGuesser";
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
