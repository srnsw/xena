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

/*
 * Created on 9/06/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.properties.PropertiesManager;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * The direct audio normaliser is used for files that can be directly handled by the flac encoder. The source file
 * is streamed directly to the flac encoder, which will produce a temporary flac file, which is then encoded with 
 * base64 to produce our final output.
 * 
 * created 11/04/2007
 * audio
 * Short desc of class:
 */
public class DirectAudioNormaliser extends AbstractNormaliser {
	public final static String AUDIO_PREFIX = "audio";
	public final static String FLAC_TAG = "flac";
	public final static String AUDIO_URI = "http://preservation.naa.gov.au/audio/1.0";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/** Endianess value to use in conversion.
	 * If a conversion of the AudioInputStream is done,
	 * this values is used as endianess in the target AudioFormat.
	 * The default value can be altered by the command line
	 * option "-B".
	 */
	boolean bBigEndian = false;

	/** Sample size value to use in conversion.
	 * If a conversion of the AudioInputStream is done,
	 * this values is used as sample size in the target
	 * AudioFormat.
	 * The default value can be altered by the command line
	 * option "-S".
	 */
	int nSampleSizeInBits = 16;

	public DirectAudioNormaliser() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		try {
			// TODO: The parse method should ONLY accept xena input sources. The Abstract normaliser should handle this
			// appropriately.
			// ie - this method should be parse(XenaInputSource xis)
			if (!(input instanceof XenaInputSource)) {
				throw new XenaException("Can only normalise XenaInputSource objects.");
			}

			XenaInputSource xis = (XenaInputSource) input;

			// There are some problems with normalising via streaming to the flac process's stdin, so we're going to
			// pass a reference to the file to the flac process instead. If the XIS does not have a reference to a file,
			// write out the bytestream to a file.
			File originalFile;
			if (xis.getFile() == null) {
				originalFile = File.createTempFile("savedstream", ".tmp");
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

			// Temporarily using binary flac encoder until a Java version exists.
			// Although it may be better to always use a binary encoder for performance reasons...

			// Encode input file with binary flac encoder
			File tmpFlacFile = File.createTempFile("flacoutput", ".tmp");

			PluginManager pluginManager = normaliserManager.getPluginManager();
			PropertiesManager propManager = pluginManager.getPropertiesManager();
			String flacEncoderProg = propManager.getPropertyValue(AudioProperties.AUDIO_PLUGIN_NAME, AudioProperties.FLAC_LOCATION_PROP_NAME);
			// Check that we have a valid location for the flac executable
			if (flacEncoderProg == null || flacEncoderProg.equals("")) {
				throw new IOException("Cannot find the flac executable. Please check its location in the audio plugin settings.");
			}

			// Have to split up the command into array elements, as for some reason a single command string doesn't work
			// on OS X...
			List<String> commandList = new ArrayList<String>();
			commandList.add(flacEncoderProg);
			commandList.add("--output-name");
			commandList.add(tmpFlacFile.getAbsolutePath()); // output filename
			commandList.add("-f"); // force overwrite of output file
			commandList.add(originalFile.getAbsolutePath()); // source filename
			String[] commandArr = commandList.toArray(new String[0]);

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
			} catch (Exception flacEx) {
				throw new IOException("An error occured in the flac normaliser: " + flacEx);
			}

			if (pr.exitValue() == 1) {
				throw new IOException("An error occured in the flac normaliser: " + errorBuff);
			}

			// Base64-encode FLAC stream
			InputStream flacStream = new FileInputStream(tmpFlacFile);
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(AUDIO_URI, FLAC_TAG, AUDIO_PREFIX + ":" + FLAC_TAG, att);
			InputStreamEncoder.base64Encode(flacStream, ch);
			ch.endElement(AUDIO_URI, FLAC_TAG, AUDIO_PREFIX + ":" + FLAC_TAG);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	@Override
	public String getName() {
		return "Direct Audio";
	}

}
