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

package au.gov.naa.digipres.xena.plugin.office.drawing;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.ODFGuesser;

/**
 * Guesser for the ODG file type (ODF drawing format in later versions of OpenOffice.org)
 * 
 */
public class OdgGuesser extends ODFGuesser {
	private static final String[] odgExtensions = {"odg", "otg"};
	private static final String[] odgMime = {"application/vnd.oasis.opendocument.graphics", "application/vnd.oasis.opendocument.graphics-template"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public OdgGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(OdgFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(odgExtensions, ZIP_MAGIC, odgMime, type)};
		descriptorArr = tempFileDescriptors;
	}

	@Override
	public String getName() {
		return "ODG Guesser";
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

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OOXMLGuesser#getIdentifyingFilename()
	 */
	@Override
	protected String[] getIdentifyingMimetypes() {
		return odgMime;
	}
}
