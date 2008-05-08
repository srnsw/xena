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

public class SpreadsheetGuesser extends OfficeGuesser {

	private static final String SPREADSHEET_TYPE_STRING = "Microsoft Excel";

	private static byte[][] sxcMagic = {{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00}};
	private static final String[] sxcExtensions = {"sxc"};
	private static final String[] sxcMime = {"application/vnd.sun.xml.calc"};

	private static final String[] xlExtensions = {"xls", "xlt", "xlw"};
	private static final String[] xlMime = {"application/ms-excel"};

	private static byte[][] odsMagic = {{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00}};
	private static final String[] odsExtensions = {"ods"};
	private static final String[] odsMime = {"application/vnd.oasis.opendocument.spreadsheet"};

	private Type type;

	private FileTypeDescriptor[] fileTypeDescriptors =
	    {new FileTypeDescriptor(xlExtensions, officeMagic, xlMime), new FileTypeDescriptor(sxcExtensions, sxcMagic, sxcMime),
	     new FileTypeDescriptor(odsExtensions, odsMagic, odsMime),};

	/**
	 * @throws XenaException 
	 * 
	 */
	public SpreadsheetGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(SpreadsheetFileType.class);
	}

	@Override
	public String getName() {
		return "SpreadsheetGuesser";
	}

	@Override
	public Guess guess(XenaInputSource source) throws XenaException, IOException {

		Guess guess = guess(source, type);
		guess.setPriority(GuessPriority.DEFAULT);

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
		return SPREADSHEET_TYPE_STRING;
	}

}
