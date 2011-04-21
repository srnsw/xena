/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Matthew Oliver
 * @author Jeff Stiff
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.BinaryToXenaBinaryNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * The ogg audio normaliser is used for files that are wrapped in the ogg container, the audio could be vorbis, flac, or speex. 
 * This normaliser, figures out the audio codec and then deals with it accordingly, vorbis then uses the vorbis audio normaliser, flac the flac normaliser, etc.
 * 
 * created 11/09/2009
 * audio
 * Short desc of class:
 */
public class OggAudioNormaliser extends AbstractNormaliser {
	public final static String AUDIO_PREFIX = "audio";
	public final static String FLAC_TAG = "flac";
	public final static String AUDIO_URI = "http://preservation.naa.gov.au/audio/1.0";
	public final static String OGG_NAME = "Ogg Audio";

	private static final int oggAudioOffset = 28;
	private static final int MAX_MAGIC_LENGTH = 8;
	private static final byte vorbisMagic[] = {(byte) 0x01, 'v', 'o', 'r', 'b', 'i', 's'};
	private static final byte flacMagic[] = {(byte) 0x7F, 'F', 'L', 'A', 'C'};
	private static final byte speexMagic[] = {'S', 'p', 'e', 'e', 'x', ' ', ' ', ' '};

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

	public OggAudioNormaliser() {
		super();
	}

	@Override
	public String getName() {
		return OGG_NAME;
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean migrateOnly) throws IOException, SAXException {
		try {
			// TODO: The parse method should ONLY accept xena input sources. The Abstract normaliser should handle this
			// appropriately.
			// ie - this method should be parse(XenaInputSource xis)
			if (!(input instanceof XenaInputSource)) {
				throw new XenaException("Can only normalise XenaInputSource objects.");
			}

			XenaInputSource xis = (XenaInputSource) input;

			// First we need to determine what type of audio exists inside the ogg file.
			InputStream byteStream = xis.getByteStream();
			byteStream.skip(oggAudioOffset);

			byte possbileMagic[] = new byte[MAX_MAGIC_LENGTH];

			byteStream.read(possbileMagic);
			byteStream.close();

			AbstractNormaliser normaliser;

			// Check which codec it matches
			if (GuesserUtils.compareByteArrays(vorbisMagic, possbileMagic)) {
				// We have vorbis
				normaliser = new VorbisAudioNormaliser();
			} else if (GuesserUtils.compareByteArrays(flacMagic, possbileMagic)) {
				// We have flac, and the flac encoder we use, supports the ogg file containing flac.
				normaliser = new DirectAudioNormaliser();
			} else if (GuesserUtils.compareByteArrays(speexMagic, possbileMagic)) {
				// We Have speex
				normaliser = new SpeexAudioNormaliser();
			} else {
				// We don't know so binary normalise.
				normaliser = new BinaryToXenaBinaryNormaliser();
			}
			normaliser.setNormaliserManager(normaliserManager);
			normaliser.setContentHandler(getContentHandler());
			normaliser.parse(input, results, migrateOnly);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public String getOutputFileExtension() {
		return "flac";
	}

	@Override
	public boolean isConvertible() {
		return true;
	}

}
