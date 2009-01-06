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
 * @author Kamaj Jayakantha de Mel
 * @author Matthew Oliver
 * 
 */

package au.gov.naa.digipres.xena.plugin.postscript;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser will identify PostScript files PostScript guesser extends from Guesser
 * 
 * @see au.gov.naa.digipres.xena.kernel.normalise
 */

public class PostscriptGuesser extends DefaultGuesser {

	private static final byte[][] psMagic = {{ '%', '!' }};
	private static final String[] psExtensions = {"ps"};
	private static final String[] psMime = {"application/postscript"};
	
	private FileTypeDescriptor[] descriptorArr = { new FileTypeDescriptor(psExtensions, psMagic, psMime)};

	private Type type;

	/**
	 * Get Guesser Name
	 * @return	String: GuesserName
	 */
	@Override
	public String getName() {
		return "PostscriptGuesser";
	}

	/**
	 * Get Type
	 * @return	type : type
	 */
	@Override
	public Type getType() {
		return type;
	}

	
	/**
	 * Initialize the Guesser
	 * @param	GuesserManager
	 * @throws 	XenaException
	 */
	@Override
	public void initGuesser(GuesserManager guesserManager) throws XenaException {
		this.guesserManager = guesserManager;
		type = getTypeManager().lookup(PostscriptFileType.class);
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}
}
