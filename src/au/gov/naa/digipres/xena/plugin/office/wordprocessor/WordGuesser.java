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
package au.gov.naa.digipres.xena.plugin.office.wordprocessor;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.MicrosoftOfficeGuesser;

public class WordGuesser extends MicrosoftOfficeGuesser {

	private static final String WORD_TYPE_STRING = "Microsoft Word";

	// OpenOffice.org does not currently support import of .wri files
	//	private static byte[][] wriMagic = {{0x31, (byte) 0xBE, 0x00, 0x00, 0x00, (byte) 0xAB, 0x00, 0x00}};
	//	private static final String[] wriExtensions = {"wri"};
	//	private static final String[] wriMime = {};

	private static final String[] mswordExtensions = {"doc", "dot"};
	private static final String[] mswordMime = {"application/msword"};

	private Type type;

	private FileTypeDescriptor[] fileTypeDescriptors = {new FileTypeDescriptor(mswordExtensions, officeMagic, mswordMime)};

	/**
	 * @throws XenaException 
	 * 
	 */
	public WordGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(WordFileType.class);
	}

	@Override
	public String getName() {
		return "Word Guesser";
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
