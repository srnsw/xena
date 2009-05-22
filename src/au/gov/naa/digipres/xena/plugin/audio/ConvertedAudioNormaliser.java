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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

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
 * The converted audio normaliser is used for audio files which are not directly handled by the flac encoder.
 * An appropriate java sound plugin is used to convert the input audio to a raw format
 * which can be understood by the flac encoder. The converted audio stream is then streamed to the flac encoder, which
 * will produce a temporary flac file, which is then encoded with base64 to produce our final output.
 * 
 * created 11/04/2007
 * audio
 * Short desc of class:
 */
public class ConvertedAudioNormaliser extends AbstractNormaliser {
	public final static String AUDIO_PREFIX = "audio";
	public final static String FLAC_TAG = "flac";
	public final static String AUDIO_URI = "http://preservation.naa.gov.au/audio/1.0";

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

	public ConvertedAudioNormaliser() {
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

			// This is where the difficult bit goes! :)

			// Convert source audio stream to raw format
			AudioInputStream audioIS;

			if (xis.getFile() == null) {
				audioIS = AudioSystem.getAudioInputStream(xis.getByteStream());
			} else {
				audioIS = AudioSystem.getAudioInputStream(xis.getFile());
			}

			AudioFormat sourceFormat = audioIS.getFormat();

			InputStream flacStream;
			File tmpFlacFile = null;
			if (sourceFormat.getEncoding().toString().equals("FLAC")) {
				flacStream = audioIS;
			} else {
				AudioFormat targetFormat =
				    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), nSampleSizeInBits, sourceFormat.getChannels(),
				                    sourceFormat.getChannels() * nSampleSizeInBits / 8, sourceFormat.getSampleRate(), bBigEndian);

				AudioInputStream rawIS = AudioSystem.getAudioInputStream(targetFormat, audioIS);
				AudioFormat rawFormat = rawIS.getFormat();
				System.out.println("Channels: " + rawFormat.getChannels() + "\nbig endian: " + rawFormat.isBigEndian() + "\nsample rate: "
				                   + rawFormat.getSampleRate() + "\nbps: " + rawFormat.getSampleRate() * rawFormat.getSampleSizeInBits());

				String endianStr = rawFormat.isBigEndian() ? "big" : "little";

				// Temporarily using binary flac encoder until a Java version exists.

				// Encode input file with binary flac encoder
				tmpFlacFile = File.createTempFile("flacoutput", ".tmp");
				tmpFlacFile.deleteOnExit();

				PluginManager pluginManager = normaliserManager.getPluginManager();
				PropertiesManager propManager = pluginManager.getPropertiesManager();
				String flacEncoderProg = propManager.getPropertyValue(AudioProperties.AUDIO_PLUGIN_NAME, AudioProperties.FLAC_LOCATION_PROP_NAME);

				// Check that we have a valid location for the flac executable
				if (flacEncoderProg == null || flacEncoderProg.equals("")) {
					throw new IOException("Cannot find the flac executable. Please check its location in the audio plugin settings.");
				}

				String signStr;
				Encoding encodingType = rawFormat.getEncoding();
				if (encodingType.equals(Encoding.PCM_SIGNED)) {
					signStr = "signed";
				} else if (encodingType.equals(Encoding.PCM_UNSIGNED)) {
					signStr = "unsigned";
				} else {
					throw new IOException("Invalid raw encoding type: " + encodingType);
				}

				List<String> commandList = new ArrayList<String>();
				commandList.add(flacEncoderProg);
				commandList.add("--output-name");
				commandList.add(tmpFlacFile.getAbsolutePath()); // output filename
				commandList.add("--force"); // force overwrite of output file
				commandList.add("--endian");
				commandList.add(endianStr);
				commandList.add("--channels");
				commandList.add(String.valueOf(rawFormat.getChannels()));
				commandList.add("--sample-rate");
				commandList.add(String.valueOf(rawFormat.getSampleRate()));
				commandList.add("--sign");
				commandList.add(signStr);
				commandList.add("--bps");
				commandList.add(String.valueOf(rawFormat.getSampleSizeInBits()));
				commandList.add("-"); // Forces read from stdin
				String[] commandArr = commandList.toArray(new String[0]);

				Process pr;
				final StringBuilder errorBuff = new StringBuilder();
				try {
					pr = Runtime.getRuntime().exec(commandArr);

					final InputStream eis = pr.getErrorStream();
					final InputStream ois = pr.getInputStream();

					Thread et = new Thread() {
						@Override
						public void run() {
							try {
								int c;
								while (0 <= (c = eis.read())) {
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
								while (0 <= (c = ois.read())) {
									System.err.print((char) c);
								}
							} catch (IOException x) {
								// Nothing
							}
						}
					};
					ot.start();

					OutputStream procOS = new BufferedOutputStream(pr.getOutputStream());

					// read 10k at a time
					byte[] buffer = new byte[10 * 1024];

					int bytesRead;
					while (0 < (bytesRead = rawIS.read(buffer))) {
						procOS.write(buffer, 0, bytesRead);
					}
					procOS.flush();
					procOS.close();
					pr.waitFor();
				} catch (Exception flacEx) {
					throw new IOException("An error occured in the flac normaliser. Please ensure you are using Flac version 1.2.1 or later."
					                      + flacEx);
				}

				if (pr.exitValue() == 1) {
					throw new IOException("An error occured in the flac normaliser. Please ensure you are using Flac version 1.2.1 or later."
					                      + errorBuff);
				}

				flacStream = new FileInputStream(tmpFlacFile);
			}

			// Base64-encode FLAC stream
			ContentHandler ch = getContentHandler();
			AttributesImpl att = new AttributesImpl();
			ch.startElement(AUDIO_URI, FLAC_TAG, AUDIO_PREFIX + ":" + FLAC_TAG, att);
			InputStreamEncoder.base64Encode(flacStream, ch);
			ch.endElement(AUDIO_URI, FLAC_TAG, AUDIO_PREFIX + ":" + FLAC_TAG);

			flacStream.close();
			if (tmpFlacFile != null) {
				tmpFlacFile.delete();
			}
		} catch (XenaException x) {
			throw new SAXException(x);
		} catch (UnsupportedAudioFileException e) {
			throw new IOException("Xena does not handle this particular audio format. " + e);
		}
	}

	@Override
	public String getName() {
		return "Converted Audio";
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
