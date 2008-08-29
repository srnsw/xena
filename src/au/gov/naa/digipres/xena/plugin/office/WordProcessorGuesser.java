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
 * Created on 11/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class WordProcessorGuesser extends OfficeGuesser {

	private static final String WORD_TYPE_STRING = "Microsoft Word";

	private static byte[][] rtfMagic = {{0x7B, 0x5c, 0x72, 0x74, 0x66, 0x31}};
	private static final String[] rtfExtensions = {"rtf"};
	private static final String[] rtfMime = {"application/rtf", "text/rtf"};

	private static byte[][] odtMagic = {{0x50, 0x4B, 0x03, 0x04}};
	private static final String[] odtExtensions = {"odt"};
	private static final String[] odtMime = {"application/vnd.oasis.opendocument.text"};

	// OpenOffice does not support import of .wri files
	//	private static byte[][] wriMagic = {{0x31, (byte) 0xBE, 0x00, 0x00, 0x00, (byte) 0xAB, 0x00, 0x00}};
	//	private static final String[] wriExtensions = {"wri"};
	//	private static final String[] wriMime = {};

	private static final String[] mswordExtensions = {"doc", "dot"};
	private static final String[] mswordMime = {"application/msword"};

	private static byte[][] sxwMagic = {{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00}};
	private static final String[] sxwExtensions = {"sxw"};
	private static final String[] sxwMime = {"application/vnd.sun.xml.writer"};

	private Type type;

	private FileTypeDescriptor[] fileTypeDescriptors =
	    {new FileTypeDescriptor(rtfExtensions, rtfMagic, rtfMime), new FileTypeDescriptor(odtExtensions, odtMagic, odtMime),
	     new FileTypeDescriptor(mswordExtensions, officeMagic, mswordMime), new FileTypeDescriptor(sxwExtensions, sxwMagic, sxwMime)};

	/**
	 * @throws XenaException 
	 * 
	 */
	public WordProcessorGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(WordProcessorFileType.class);
	}

	@Override
	public String getName() {
		return "WordGuesser";
	}

	@Override
	public Guess guess(XenaInputSource xis) throws IOException, XenaException {

		Guess guess = guess(xis, type);
		guess.setPriority(GuessPriority.HIGH);

		return guess;
	}

	/**
	 * @return Returns the fileTypeDescriptors.
	 */
	@Override
	public FileTypeDescriptor[] getFileTypeDescriptors() {
		return fileTypeDescriptors;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	protected String getOfficeTypeString() {
		return WORD_TYPE_STRING;
	}

}
