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
 * Created on 28/02/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

public class AudioDeNormaliser extends BinaryDeNormaliser {
	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		String elementName = DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG;
		if (elementName.equals(qName)) {
			start();
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		String elementName = DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG;
		if (elementName.equals(qName)) {
			end();
		}
	}

	@Override
    public String toString() {
		return "Audio";
	}

}
