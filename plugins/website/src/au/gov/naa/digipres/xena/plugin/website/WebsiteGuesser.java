package au.gov.naa.digipres.xena.plugin.website;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * This file is part of website.
 * 
 * website is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * website is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with website; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

/**
 * @author Justin Waddell
 *
 */
public class WebsiteGuesser extends DefaultGuesser {

	// Website format is a normal zip file with a custom extension
	private static final byte[][] websiteMagic = {{0x50, 0x4B, 0x03, 0x04}};
	private static final String[] websiteExtensions = {"wsx"};
	private static final String[] websiteMime = {"application/zip"};

	private FileTypeDescriptor[] websiteFileDescriptors;

	private Type type;

	/**
	 * 
	 */
	public WebsiteGuesser() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return websiteFileDescriptors;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Website Guesser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getType()
	 */
	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return type;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#initGuesser(au.gov.naa.digipres.xena.kernel.guesser.GuesserManager)
	 */
	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(WebsiteFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(websiteExtensions, websiteMagic, websiteMime, type)};
		websiteFileDescriptors = tempFileDescriptors;
	}

}
