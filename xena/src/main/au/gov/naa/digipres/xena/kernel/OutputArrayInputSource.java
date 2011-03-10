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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * An InputSource where the the data comes from the result of writing to memory.
 *
 */
public class OutputArrayInputSource extends XenaInputSource {
	ByteArrayOutputStream os;

	/**
	 * @param byteArrayOutputStream the stream the data will be written to
	 * @param type Xena file type
	 * @param mimeType the mime-type
	 * @param encoding the character encoding
	 */
	public OutputArrayInputSource(ByteArrayOutputStream byteArrayOutputStream, Type type, String mimeType, String encoding) {
		super("", type);
		this.os = byteArrayOutputStream;
		setEncoding(encoding);
		setMimeType(mimeType);
	}

	@Override
    public InputStream getByteStream() {
		InputStream rtn = new ByteArrayInputStream(os.toByteArray());
		return rtn;
	}

	@Override
    public Reader getCharacterStream() {
		try {
			return new InputStreamReader(getByteStream(), getEncoding());
		} catch (UnsupportedEncodingException x) {
			x.printStackTrace();
			throw new RuntimeException("Unsupported Encoding");
		}
	}
}
