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

package au.gov.naa.digipres.xena.kernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * An input source that comes from in-memory rather than an external file.
 *
 */
public class ByteArrayInputSource extends XenaInputSource {
	byte[] bytes;

	int offset;

	int length;

	/**
	 * @param bytes raw data to use as input
	 * @param offset offset within bytes
	 * @param length length of data
	 * @param type the type of data expressed as a Xena FileType
	 * @param mimeType the mime-type of the data
	 * @param encoding the character encoding of the data
	 */
	public ByteArrayInputSource(byte[] bytes, int offset, int length, Type type, String mimeType, String encoding) {
		super("", type);
		this.bytes = bytes;
		this.offset = offset;
		this.length = length;
		setEncoding(encoding);
		setMimeType(mimeType);
	}

	/**
	 * @param bytes raw data to use as input
	 * @param type the type of data expressed as a Xena FileType
	 */
	public ByteArrayInputSource(byte[] bytes, Type type) {
		super("", type);
		this.bytes = bytes;
		offset = 0;
		length = bytes.length;
	}

	/**
	 * @param str string data to use as input
	 * @param type the type of data expressed as a Xena FileType
	 */
	public ByteArrayInputSource(String str, Type type) {
		super("", type);
		this.bytes = str.getBytes();
		offset = 0;
		length = bytes.length;
	}

	/**
	 * Construct a source from a given input stream. First we read the stream
	 * and cache the result (in case we need to open it multiple times) and
	 * then we can stream it back again.
	 * @param inputStream inputStream source of data
	 * @param type the type of data expressed as a Xena FileType
	 */
	public ByteArrayInputSource(InputStream inputStream, Type type) throws IOException {
		super("", type);
		// We have to cache the data, because Xena assumes that data streams
		// can be rewinded as many times as necessary.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int c;
		while (0 < (c = inputStream.read(buf))) {
			baos.write(buf, 0, c);
		}
		bytes = baos.toByteArray();
		int offset = 0;
		length = bytes.length;
	}

	/**
	 * Get a raw data byte stream
	 */
	@Override
    public InputStream getByteStream() {
		InputStream rtn = new ByteArrayInputStream(bytes, offset, length);
		openedFiles.add(rtn);
		return rtn;
	}

	/**
	 * Get a data stream using proper character encodings
	 */
	@Override
    public Reader getCharacterStream() {
		if (getEncoding() == null) {
			return new InputStreamReader(getByteStream());
		} else {
			try {
				return new InputStreamReader(getByteStream(), getEncoding());
			} catch (UnsupportedEncodingException x) {
				x.printStackTrace();
				return null;
			}
		}
	}
}
