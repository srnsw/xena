/*
 * Created on 28/02/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

public class AudioDeNormaliser extends BinaryDeNormaliser
{
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		String elementName = DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG;
		if (elementName.equals(qName))
		{
			start();
		}
	}
	
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		String elementName = DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG;
		if (elementName.equals(qName))
		{
			end();
		}
	}

	public String toString()
	{
		return "Audio";
	}

}
