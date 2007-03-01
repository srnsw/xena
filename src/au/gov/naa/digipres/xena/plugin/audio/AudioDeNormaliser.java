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
		String elementName = AudioNormaliser.AUDIO_PREFIX + ":" + AudioNormaliser.AUDIO_PREFIX;
		if (elementName.equals(qName))
		{
			start();
		}
	}

	public String toString()
	{
		return "Audio";
	}

}
