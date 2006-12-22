/*
 * Created on 21/12/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;

public class TempFileWriter
{
	private static final int READ_BUFFER_SIZE = 1024;

	/**
	 * Writes the given input source to a temporary file, and returns a reference to the new file.
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static File createTempFile(InputSource input) throws IOException
	{
		File tempFile = File.createTempFile("tempwriter", ".tmp");
		FileOutputStream outStream = new FileOutputStream(tempFile);
		InputStream inStream = input.getByteStream();
		byte[] readBuff = new byte[READ_BUFFER_SIZE];
		int bytesRead = inStream.read(readBuff);
		while (bytesRead > 0)
		{
			outStream.write(readBuff, 0, bytesRead);
			bytesRead = inStream.read(readBuff);			
		}
		return tempFile;
	}
	
}
