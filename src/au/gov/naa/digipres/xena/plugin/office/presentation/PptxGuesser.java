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
package au.gov.naa.digipres.xena.plugin.office.presentation;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.OOXMLGuesser;

public class PptxGuesser extends OOXMLGuesser {

	private static final String[] ooxmlExtensions = {"pptx"};
	private static final String[] ooxmlMime = {"application/powerpoint"};

	private static final String PPTX_IDENTIFYING_FILENAME = "ppt/presentation.xml";

	private Type type;
	private FileTypeDescriptor[] fileTypeDescriptors;

	/**
	 * @throws XenaException 
	 * 
	 */
	public PptxGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(PptxFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(ooxmlExtensions, ZIP_MAGIC, ooxmlMime, type)};
		fileTypeDescriptors = tempFileDescriptors;
	}

	@Override
	public String getName() {
		return "PptxGuesser";
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

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OOXMLGuesser#getIdentifyingFilename()
	 */
	@Override
	protected String getIdentifyingFilename() {
		return PPTX_IDENTIFYING_FILENAME;
	}

}
