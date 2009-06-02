/**
 * This file is part of image.
 * 
 * image is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * image is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with image; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.image.tiff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.plugin.image.ImageProperties;
import au.gov.naa.digipres.xena.plugin.image.ReleaseInfo;

/**
 * @author Justin Waddell
 *
 */
public class TiffTextNormaliser extends AbstractTextNormaliser {

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#getName()
	 */
	@Override
	public String getName() {
		return "TIFF Text Normaliser";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser#parse(org.xml.sax.InputSource, au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults)
	 */
	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		PluginManager pluginManager = normaliserManager.getPluginManager();
		PropertiesManager propManager = pluginManager.getPropertiesManager();
		String tesseractPath = propManager.getPropertyValue(ImageProperties.IMAGE_PLUGIN_NAME, ImageProperties.TESSERACT_LOCATION_PROP_NAME);

		if (tesseractPath == null || tesseractPath.equals("")) {
			throw new IOException("Path to tesseract has not been defined in the properties for the image plugin.");
		}

		XenaInputSource xis = (XenaInputSource) input;

		// Tesseract only accepts an input file, so is the input source is a stream we will need to write it out.
		// Tesseract also only accepts files with a ".tif" extension, so we will need to copy the file if it does not
		// have this extension.
		File originalFile;
		if (xis.getFile() == null || !"tif".equals(xis.getFileNameExtension().toLowerCase())) {
			originalFile = File.createTempFile("savedstream", ".tif");
			originalFile.deleteOnExit();
			InputStream inStream = xis.getByteStream();
			FileOutputStream outStream = new FileOutputStream(originalFile);
			byte[] buffer = new byte[10 * 1024];
			int bytesRead = inStream.read(buffer);
			while (bytesRead > 0) {
				outStream.write(buffer, 0, bytesRead);
				bytesRead = inStream.read(buffer);
			}
			outStream.flush();
			outStream.close();
			inStream.close();
		} else {
			originalFile = xis.getFile();
		}

		File outputFile = File.createTempFile("tesseract-output", "");

		// Have to split up the command into array elements, as for some reason a single command string doesn't work
		// on OS X...
		List<String> commandList = new ArrayList<String>();
		commandList.add(tesseractPath);
		commandList.add(originalFile.getAbsolutePath()); // source filename
		commandList.add(outputFile.getAbsolutePath()); // output filename
		String[] commandArr = commandList.toArray(new String[0]);

		// Run the tesseract process
		Process pr;
		final StringBuilder errorBuff = new StringBuilder();
		try {
			pr = Runtime.getRuntime().exec(commandArr);

			final InputStream procErrorStream = pr.getErrorStream();
			final InputStream procInputStream = pr.getInputStream();

			Thread et = new Thread() {
				@Override
				public void run() {
					try {
						int c;
						while (0 <= (c = procErrorStream.read())) {
							errorBuff.append((char) c);
						}
					} catch (IOException x) {
						// Nothing
					}
				}
			};
			et.start();

			Thread ot = new Thread() {
				@Override
				public void run() {
					int c;
					try {
						while (0 <= (c = procInputStream.read())) {
							System.err.print((char) c);
						}
					} catch (IOException x) {
						// Nothing
					}
				}
			};
			ot.start();
			pr.waitFor();
		} catch (Exception ex) {
			throw new IOException("An error occured in the tesseract process. Please ensure you are using tesseract version 2.0.3 or later." + ex);
		}

		if (pr.exitValue() == 1) {
			throw new IOException("An error occured in the tesseract process. Please ensure you are using tesseract version 2.0.3 or later."
			                      + errorBuff);
		}

		// Tesseract automatically appends ".txt" to the end of the output file
		outputFile = new File(outputFile.getAbsolutePath() + ".txt");
		outputFile.deleteOnExit();

		// The text version of the tiff is in outputFile. We now need to write this out to the content handler.
		ContentHandler contentHandler = getContentHandler();
		Reader reader = new FileReader(outputFile);
		char[] buffer = new char[10 * 1024];
		int charsRead = reader.read(buffer);
		while (charsRead > 0) {
			contentHandler.characters(buffer, 0, charsRead);
			charsRead = reader.read(buffer);
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractTextNormaliser#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		// This normaliser converts an scanned image into text using OCR, so output is a .txt file
		return "txt";
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
