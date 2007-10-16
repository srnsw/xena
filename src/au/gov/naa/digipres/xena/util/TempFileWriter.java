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

/*
 * Created on 21/12/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;

public class TempFileWriter {
	private static final int READ_BUFFER_SIZE = 1024;

	/**
	 * Writes the given input source to a temporary file, and returns a reference to the new file.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static File createTempFile(InputSource input) throws IOException {
		File tempFile = File.createTempFile("tempwriter", ".tmp");
		FileOutputStream outStream = new FileOutputStream(tempFile);
		InputStream inStream = input.getByteStream();
		byte[] readBuff = new byte[READ_BUFFER_SIZE];
		int bytesRead = inStream.read(readBuff);
		while (bytesRead > 0) {
			outStream.write(readBuff, 0, bytesRead);
			bytesRead = inStream.read(readBuff);
		}
		return tempFile;
	}

}
