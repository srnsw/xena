package au.gov.naa.digipres.xena.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Simulates a URLConnection from a byte array rather than the real genuine
 * location of the data.
 *
 * @author Chris Bitmead
 */
public class ByteArrayURLConnection extends URLConnection {
	byte[] bytes;

	String[] headerKeys = new String[0];

	String[] headerValues = new String[0];

	public void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		int len = stream.readInt();
		bytes = new byte[len];
		stream.readFully(bytes);
		len = stream.readInt();
		headerKeys = new String[len];
		headerValues = new String[len];
		for (int i = 0; i < len; i++) {
			headerKeys[i] = (String)stream.readObject();
			headerValues[i] = (String)stream.readObject();
		}
	}

	public void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeInt(bytes.length);
		stream.write(bytes);
		stream.writeInt(headerKeys.length);
		for (int i = 0; i < headerKeys.length; i++) {
			stream.writeObject(headerKeys[i]);
			stream.writeObject(headerValues[i]);
		}
	}

	public ByteArrayURLConnection(URL url) throws IOException {
		super(url);
	}

	public ByteArrayURLConnection(URLConnection connection) throws IOException {
		super(connection.getURL());
		// Headers
		String key;
		String value;
		// Yep the "&" is correct
		int c;
		for (c = 0; !((key = connection.getHeaderFieldKey(c)) == null & (value = connection.getHeaderField(c)) == null); c++) {
		}
		headerKeys = new String[c];
		headerValues = new String[c];
		for (int i = 0; !((key = connection.getHeaderFieldKey(i)) == null & (value = connection.getHeaderField(i)) == null); i++) {
			headerKeys[i] = key;
			headerValues[i] = value;
		}
		// Data
		int len = connection.getContentLength();
		InputStream is = connection.getInputStream();
		ByteArrayOutputStream baos;
		if (0 < len) {
			baos = new ByteArrayOutputStream(len);
		} else {
			baos = new ByteArrayOutputStream();
		}
		byte[] buf = new byte[4096];
		while (0 <= (len = is.read(buf))) {
			baos.write(buf, 0, len);
		}
		is.close();
		baos.close();
		bytes = baos.toByteArray();

	}

	public void connect() throws java.io.IOException {
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(bytes);
	}

	public String getHeaderFieldKey(int n) {
		if (headerKeys.length <= n) {
			return null;
		} else {
			return headerKeys[n];
		}
	}

	public String getHeaderField(int n) {
		if (headerValues.length <= n) {
			return null;
		} else {
			return headerValues[n];
		}
	}

	public String getHeaderField(String name) {
		for (int i = 0; i < headerKeys.length; i++) {
			if (name != null && headerKeys[i] != null && name.toLowerCase().equals(headerKeys[i].toLowerCase())) {
				return headerValues[i];
			}
		}
		return null;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}
