package au.gov.naa.digipres.xena.plugin.basic;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.Decoder;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Decoder for the base64 type
 *
 * @author  Chris Bitmead
 */
public class Base64Decoder extends Decoder {
	/**
	 *
	 */
	public Base64Decoder() {
	}

	/**
	 *
	 */
	public String getName() {
		return "Base64";
	}

	/**
	 * @param  source  stream for decoding
	 * @return         decoded stream
	 */

	public XenaInputSource decode(XenaInputSource source) throws IOException {
		sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
		File tmpFile = File.createTempFile("xenadec", "dat");
        XenaInputSource rtn = new XenaInputSource(tmpFile);
		rtn.setTmpFile(true);
		FileOutputStream os = new FileOutputStream(tmpFile);
		decoder.decodeBuffer(source.getByteStream(), os);
		// XXXXXXXXXXXXXXXXXXXXXXXXXXXX
		//		return ByteArrayURLStreamHandler.makeByteURL(decoder.decodeBuffer(source.openStream()));
		return rtn;
	}
}
