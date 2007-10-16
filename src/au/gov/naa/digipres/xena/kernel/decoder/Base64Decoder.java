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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.kernel.decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Decoder for the base64 type
 *
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
	@Override
    public String getName() {
		return "Base64";
	}

	/**
	 * @param  source  stream for decoding
	 * @return         decoded stream
	 */

	@Override
    public XenaInputSource decode(XenaInputSource source) throws IOException {
		sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
		File tmpFile = File.createTempFile("xenadec", "dat");
		XenaInputSource rtn = new XenaInputSource(tmpFile);
		rtn.setTmpFile(true);
		FileOutputStream os = new FileOutputStream(tmpFile);
		decoder.decodeBuffer(source.getByteStream(), os);
		return rtn;
	}
}
