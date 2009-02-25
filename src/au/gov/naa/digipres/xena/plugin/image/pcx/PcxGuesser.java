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
 * Created on 19/04/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.image.pcx;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class PcxGuesser extends DefaultGuesser {
	private Type pcxType;

	// PCX Format
	private static final byte[][] pcxMagic = { {0x0A, 0x00, 0x01}, {0x0A, 0x02, 0x01}, {0x0A, 0x03, 0x01}, {0x0A, 0x04, 0x01}, {0x0A, 0x05, 0x01}};
	private static final String[] pcxExtensions = {"pcx"};
	private static final String[] pcxMime = {"image/pcx", "image/x-pc-paintbrush", "image/x-pcx"};

	public PcxGuesser() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#initGuesser(au.gov.naa.digipres.xena.kernel.guesser.GuesserManager)
	 */
	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		pcxType = getTypeManager().lookup(PcxFileType.class);
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		FileTypeDescriptor[] descArr = {new FileTypeDescriptor(pcxExtensions, pcxMagic, pcxMime)};
		return descArr;
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#getName()
	 */
	@Override
	public String getName() {
		return "PCXGuesser";
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.image.LegacyImageGuesser#getType()
	 */
	@Override
	public Type getType() {
		return pcxType;
	}

}
