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
 * @author Matthew Oliver
 */
package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class IcoGuesser extends DefaultGuesser {
	public static final String ICO_GUESSER_NAME = "ICO Guesser";

	private static final byte[][] icoMagic = {
	// MS Windows icon resource <- removed this magic, as it seems Image Magic doesn't actually support this version.
	    // {(byte) 0000, (byte) 0000, (byte) 0001, (byte) 0000},
	    // Icon for MS Windows
	    {(byte) 0102, (byte) 0101, (byte) 0050, (byte) 0000, (byte) 0000, (byte) 0000, (byte) 0056, (byte) 0000, (byte) 0000, (byte) 0000,
	     (byte) 0000, (byte) 0000, (byte) 0000, (byte) 0000}};

	private static final String[] icoExtensions = {"ico"};
	private static final String[] icoMime = {"image/x-ico"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	public IcoGuesser() {
		super();
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

	@Override
	public String getName() {
		return ICO_GUESSER_NAME;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(IcoFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(icoExtensions, icoMagic, icoMime, type)};
		descriptorArr = tempFileDescriptors;
	}

}
