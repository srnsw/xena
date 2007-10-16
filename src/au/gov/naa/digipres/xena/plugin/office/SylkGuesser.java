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

public class SylkGuesser extends OfficeGuesser {

	private static final String SYLK_TYPE_STRING = "Symbolic Link (SYLK)";

	private static byte[][] sylkMagic = {};
	private static final String[] sylkExtensions = {"slk", "sylk"};
	private static final String[] sylkMime = {"application/excel"};

	private Type type;

	private FileTypeDescriptor[] fileTypeDescriptors = {new FileTypeDescriptor(sylkExtensions, sylkMagic, sylkMime)};

	/**
	 * @throws XenaException 
	 * 
	 */
	public SylkGuesser() throws XenaException {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(SylkFileType.class);
	}

	@Override
    public String getName() {
		return "SylkGuesser";
	}

	@Override
    public Guess guess(XenaInputSource source) throws XenaException, IOException {

		Guess guess = guess(source, type);
		guess.setPriority(GuessPriority.LOW);

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
		return SYLK_TYPE_STRING;
	}

}
