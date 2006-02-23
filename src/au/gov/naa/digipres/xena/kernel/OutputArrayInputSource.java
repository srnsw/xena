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
 * @author Chris Bitmead
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

	public InputStream getByteStream() {
		InputStream rtn = new ByteArrayInputStream(os.toByteArray());
		return rtn;
	}

	public Reader getCharacterStream() {
		try {
			return new InputStreamReader(getByteStream(), getEncoding());
		} catch (UnsupportedEncodingException x) {
			x.printStackTrace();
			throw new RuntimeException("Unsupported Encoding");
		}
	}
}
