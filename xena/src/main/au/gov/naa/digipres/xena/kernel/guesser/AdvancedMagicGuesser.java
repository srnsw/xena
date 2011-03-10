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
 *  @author Matthew Oliver
 */
package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.Type;

public abstract class AdvancedMagicGuesser extends Guesser {

	private final int IS_PSTRING = -1;

	public enum MagicType {
		BYTE, //A one-byte value 
		SHORT, //A two-byte value 
		LONG, //A four-byte value 
		QUAD, //An eight-byte value 
		FLOAT, DOUBLE, STRING,
		// BE stands for Big Endian
		BEID3, BESHORT, BELONG, BEQUAD, BEFLOAT, BEDOUBLE, BESTRING16,
		// LE stands for Little Endian		
		LEID3, LESHORT, LELONG, LEQUAD, LEFLOAT, LEDOUBLE, LESTRING16,
		// ME stands for Middle Endian
		MELONG,

		// Not trying to implement these, although we only do a byte to byte check, so they might actually work. 
		PSTRING, DATE, QDATE, LDATE, QLDATE, BEDATE, BEQDATE, BELDATE, BEQLDATE, LEDATE, LEQDATE, LELDATE, LEQLDATE, MEDATE, MELDATE, INDIRECT, REGEX, SEARCH
	}

	public AdvancedMagicGuesser(GuesserManager guesserManager) {
		super(guesserManager);
	}

	public AdvancedMagicGuesser() {
		super();
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	final protected FileTypeDescriptor[] getFileTypeDescriptors() {
		// We need to make our own version of this method, so the guesser will work in the same way everyone is accustomed to.
		// We cannot use the normal FileTypeDescriptor, because it stores the magic numbers as byte[][].

		// But we cannot return null, as this causes a null pointer exception, and we can't convert the AdvancedMagicFileTypeDescriptors 
		// into FileTypeDescripters as we need to force Xena into running the guess method. 
		FileTypeDescriptor[] descriptors = new FileTypeDescriptor[0];

		return descriptors;
	}

	abstract protected AdvancedMagicFileTypeDescriptor[] getAdvancedMagicFileTypeDescriptors();

	@Override
	public Guess guess(XenaInputSource xenaInputSource) throws XenaException, IOException {
		Guess guess = new Guess(getType());
		String mimeType = xenaInputSource.getMimeType();

		// get the mime type...
		if (mimeType != null && !mimeType.equals("")) {
			for (AdvancedMagicFileTypeDescriptor element : getAdvancedMagicFileTypeDescriptors()) {
				if (element.mimeTypeMatch(mimeType)) {
					guess.setMimeMatch(true);
					break;
				}
			}
		}

		// Get the extension...
		FileName name = new FileName(xenaInputSource.getSystemId());
		String extension = name.extenstionNotNull();

		boolean extMatch = false;
		if (!extension.equals("")) {
			for (AdvancedMagicFileTypeDescriptor element : getAdvancedMagicFileTypeDescriptors()) {
				if (element.extensionMatch(extension)) {
					extMatch = true;
					break;
				}
			}
		}
		guess.setExtensionMatch(extMatch);

		// Here is where we do things very differently.
		boolean isMagicMatch = false;
		for (AdvancedMagicFileTypeDescriptor element : getAdvancedMagicFileTypeDescriptors()) {
			for (MagicItem mi : element.getMagicItems()) {
				if (magicMatch(mi, xenaInputSource)) {
					isMagicMatch = true;
					break;
				}
			}
		}
		guess.setMagicNumber(isMagicMatch);

		return guess;
	}

	private boolean magicMatch(MagicItem item, XenaInputSource source) {
		try {
			//First we create a stream and move to the offset.
			InputStream byteStream = source.getByteStream();
			byteStream.skip(item.getOffset());

			byte[] magic;

			//Now we need to create a byte array of the size of the magic number.
			int bufferSize = getMagicBufferSize(item);
			if (bufferSize > 0) {
				magic = new byte[bufferSize];
			} else if (bufferSize == IS_PSTRING) {
				// Calculate the size of the PSTRING, a PSTRING is a pascal string, the first byte is actually an unsigned int 
				// Specifying the length of the string. We can cheat, we have the lenth of the target to match so we know the length,
				// but we need to shift the InputStream forward one byte, so we are sitting at the start of the actual string.
				byteStream.skip(1);
				magic = new byte[item.getTarget().length];
			} else {
				// Couldn't calculate so end the magic match here.
				return false;
			}

			// Now that we have a buffer, we need to grab the magic number, then if it's Big Endian or Little Endian we need to process it.
			byteStream.read(magic);
			byteStream.close();

			// Once processed the magic number should be read to match.
			magic = process(magic, item);

			// Compare the magic numbers.
			if (GuesserUtils.compareByteArrays(magic, item.target)) {
				return true;
			}

			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private byte[] process(byte[] magic, MagicItem item) {

		// If the file is in BE, LE, or ME, we need to make sure it is in the right order.
		switch (item.getType()) {
		case BESHORT:
		case BELONG:
		case BEFLOAT:
		case BEDOUBLE:
		case BEDATE:
		case BELDATE:
		case BEQDATE:
		case BEQLDATE:
		case BEID3:
		case BEQUAD:
		case BESTRING16:
			// Should already be in Big Endian - But might have to be careful here on other systems 
			// although the JVM is always in BE format.
			break;
		case LEDATE:
		case LEDOUBLE:
		case LEFLOAT:
		case LEID3:
		case LELDATE:
		case LELONG:
		case LEQDATE:
		case LEQLDATE:
		case LEQUAD:
		case LESHORT:
		case LESTRING16:
			magic = LittleEndianToBigEndian(magic);
			break;
		case MEDATE:
		case MELDATE:
		case MELONG:
			magic = MiddleEndianToBigEndian(magic);
			break;
		default:
			break;
		}

		// If a test needs to be run then test it.
		if (item.getTest() != null) {
			magic = item.getTest().runTest(magic);
		}

		return magic;
	}

	private byte[] LittleEndianToBigEndian(byte[] src) {
		byte[] res = new byte[src.length];

		for (int i = 0; i < src.length; i++) {
			res[src.length - (i + 1)] = src[i];
		}

		return res;
	}

	private byte[] MiddleEndianToBigEndian(byte[] src) {
		byte[] res = new byte[src.length];

		// The src lenth has to be a multiple of two.
		if (src.length % 2 == 0) {
			for (int i = 0; i < src.length; i += 2) {
				res[i] = src[i + 1];
				res[i + 1] = src[i];
			}
		} else {
			//Error, just return the src as it is.
			return src;
		}
		return res;
	}

	private int getMagicBufferSize(MagicItem magicItem) {
		int size = 0;

		switch (magicItem.getType()) {
		case BYTE:
			size = 1;
			break;
		case SHORT:
		case BESHORT:
		case LESHORT:
		case BESTRING16:
		case LESTRING16:
			size = 2;
			break;
		case LONG:
		case BELONG:
		case LELONG:
		case MELONG:
		case FLOAT:
		case BEFLOAT:
		case LEFLOAT:
		case DATE:
		case LDATE:
		case BEDATE:
		case BELDATE:
		case LEDATE:
		case LELDATE:
		case MEDATE:
		case MELDATE:
		case BEID3:
		case LEID3:
			size = 4;
			break;
		case QUAD:
		case BEQUAD:
		case LEQUAD:
		case DOUBLE:
		case BEDOUBLE:
		case LEDOUBLE:
		case QDATE:
		case QLDATE:
		case BEQDATE:
		case BEQLDATE:
		case LEQDATE:
		case LEQLDATE:
			size = 8;
			break;
		case STRING:
			// Size is the length of the sent in target.
			size = magicItem.getTarget().length;
			break;
		case PSTRING:
			// We can calculate size here, kind of, by cheating but it will involve moving a byte forward, so return a IS_PSTRING to indicate it's a PSTRING and let the calling method
			// do it as it _should_ have the input stream.
			size = IS_PSTRING;
			break;
		default:
			size = 0;
		}

		return size;
	}

	/**
	 * The MagicItem class represents a single magic number, or a single row in the magic file (roughly speaking).
	 * 
	 * It stores the offset to the magic number, the type of data, a test object in case something has to be run on the raw magic number,
	 * and finally the target, which is what the magic number should be (after a test if there is one) to indicate a match. 
	 */
	public class MagicItem {
		private long offset;
		private MagicType type;
		private MagicTest test;
		private byte[] target;

		public MagicItem() {
			offset = -1;
			type = MagicType.STRING;
			test = null;
			target = null;
		}

		/**
		 * Constructor with test.
		 * @param offset The byte offset to the magic number.
		 * @param type The type of the magic number.
		 * @param test The test that need to be run on the magic number for it to get to the target (if there is a test).
		 * @param target That required magic number.
		 */
		public MagicItem(long offset, MagicType type, MagicTest test, byte[] target) {
			this.offset = offset;
			this.type = type;
			this.test = test;
			this.target = target;
		}

		/**
		 * Constructor without test, most common.
		 * @param offset The byte offset to the magic number.
		 * @param type The type of the magic number.
		 * @param target That required magic number.
		 */
		public MagicItem(long offset, MagicType type, byte[] target) {
			this.offset = offset;
			this.type = type;
			this.target = target;
			this.test = null;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public MagicType getType() {
			return type;
		}

		public void setType(MagicType type) {
			this.type = type;
		}

		public MagicTest getTest() {
			return test;
		}

		public void setTest(MagicTest test) {
			this.test = test;
		}

		public byte[] getTarget() {
			return target;
		}

		public void setTarget(byte[] target) {
			this.target = target;
		}
	}

	/**
	 * The MagicTest class represents a test or an operation that needs to be run on the
	 * raw magic number of the file in order for it to match the target. 
	 */
	public static class MagicTest {
		public static final int AND_OPERATOR = 0;
		public static final int OR_OPERATOR = 1;

		private int operator;
		private byte[] test;

		public MagicTest(int operator, byte[] test) {
			this.operator = operator;
			this.test = test;
		}

		public int getOperator() {
			return operator;
		}

		public void setOperator(int operator) {
			this.operator = operator;
		}

		public byte[] getTest() {
			return test;
		}

		public void setTest(byte[] test) {
			this.test = test;
		}

		public byte[] runTest(byte[] source) {
			if (test.length == source.length) {

				byte[] result = new byte[source.length];

				for (int i = 0; i < source.length; i++) {
					if (operator == AND_OPERATOR) {
						result[i] = (byte) (source[i] & test[i]);
					} else {
						result[i] = (byte) (source[i] | test[i]);
					}
				}

				return result;
			}

			return null;
		}
	}

	public class AdvancedMagicFileTypeDescriptor extends FileTypeDescriptor {
		private MagicItem[] magicItems;

		public AdvancedMagicFileTypeDescriptor(String[] extension, MagicItem[] magicItemArr, String[] typeArr, Type type) {
			super(extension, null, typeArr, type);

			this.setMagicItems(magicItemArr);
		}

		public void setMagicItems(MagicItem[] magicItems) {
			this.magicItems = magicItems;
		}

		public MagicItem[] getMagicItems() {
			return magicItems;
		}
	}

}
