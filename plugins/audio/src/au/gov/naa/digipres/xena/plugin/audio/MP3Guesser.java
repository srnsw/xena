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
 * @author Matthew Oliver
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.AdvancedMagicGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class MP3Guesser extends AdvancedMagicGuesser {

	public static final String GUESSER_NAME = "MP3 Guesser";
	private Type type;

	private AdvancedMagicFileTypeDescriptor[] descriptorArr;

	private static final String[] mp3Extensions = {"mp3"};
	private static final String[] mp3Mime = {"audio/mp3", "audio/mpg", "audio/mpeg3", "audio/mpeg"};
	private static MagicItem[] mp3Magic;

	private void setupMagic() {
		List<MagicItem> magicList = new ArrayList<MagicItem>();

		// Create magic items.

		// MP3, M1A
		byte[] m1aTarget = {(byte) 0xFF, (byte) 0xFA};
		byte[] testArr = {(byte) 0xFF, (byte) 0xFE};
		MagicTest test = new MagicTest(MagicTest.AND_OPERATOR, testArr);
		MagicItem item = new MagicItem(0, MagicType.BESHORT, test, m1aTarget);
		magicList.add(item);

		// MP3, M2A
		// NOTE: It uses the same test 0xFFFE
		byte[] m2aTarget = {(byte) 0xFF, (byte) 0xFA};
		test = new MagicTest(MagicTest.AND_OPERATOR, testArr);
		item = new MagicItem(0, MagicType.BESHORT, test, m2aTarget);
		magicList.add(item);

		// MP3, M25A
		// NOTE: It uses the same test 0xFFFE
		byte[] m25aTarget = {(byte) 0xFF, (byte) 0xE2};
		test = new MagicTest(MagicTest.AND_OPERATOR, testArr);
		item = new MagicItem(0, MagicType.BESHORT, test, m25aTarget);
		magicList.add(item);

		// # MP3 (archiver, not lossy audio compression)
		byte[] mp3ArchiverTarget = {'M', 'P', '3', (byte) 0x1A};
		item = new MagicItem(0, MagicType.STRING, mp3ArchiverTarget);
		magicList.add(item);

		// ID3
		byte[] id3Target = {'I', 'D', '3'};
		item = new MagicItem(0, MagicType.STRING, id3Target);
		magicList.add(item);

		// Extra magic from old MP3Guesser
		byte[] oldTarget = {(byte) 0xFF, (byte) 0xFB, (byte) 0x30};
		item = new MagicItem(0, MagicType.STRING, oldTarget);
		magicList.add(item);

		// Turn it into an array
		mp3Magic = new MagicItem[magicList.size()];
		magicList.toArray(mp3Magic);
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(MP3Type.class);
		setupMagic();
		AdvancedMagicFileTypeDescriptor[] tempFileDescriptors = {new AdvancedMagicFileTypeDescriptor(mp3Extensions, mp3Magic, mp3Mime, type)};
		descriptorArr = tempFileDescriptors;
	}

	public MP3Guesser() {
		super();
	}

	@Override
	protected AdvancedMagicFileTypeDescriptor[] getAdvancedMagicFileTypeDescriptors() {
		return descriptorArr;
	}

	@Override
	public String getName() {
		return GUESSER_NAME;
	}

	@Override
	public Type getType() {
		return type;
	}

}
