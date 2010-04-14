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
 * Created on 22/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * Normalises Flac files. Since flac is the preservation format for audio files, just need to base64 the original flac file.
 * created 22/03/2007
 * audio
 * Short desc of class:
 */
public class FlacToXenaAudioNormaliser extends AbstractNormaliser {
	@Override
	public String getName() {
		return "Flac";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {
		ContentHandler ch = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		InputStream is = input.getByteStream();
		ch.startElement(DirectAudioNormaliser.AUDIO_URI, DirectAudioNormaliser.FLAC_TAG, DirectAudioNormaliser.AUDIO_PREFIX + ":"
		                                                                                 + DirectAudioNormaliser.FLAC_TAG, att);
		InputStreamEncoder.base64Encode(is, ch);
		ch.endElement(DirectAudioNormaliser.AUDIO_URI, DirectAudioNormaliser.FLAC_TAG, DirectAudioNormaliser.AUDIO_PREFIX + ":"
		                                                                               + DirectAudioNormaliser.FLAC_TAG);
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
