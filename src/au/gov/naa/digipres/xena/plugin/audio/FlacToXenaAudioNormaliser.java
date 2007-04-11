/*
 * Created on 22/03/2007
 * justinw5
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
 * @author justinw5
 * created 22/03/2007
 * audio
 * Short desc of class:
 */
public class FlacToXenaAudioNormaliser extends AbstractNormaliser
{
    public String getName() 
    {
        return "Flac";
    }

    public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException 
    {
        ContentHandler ch = getContentHandler();
        AttributesImpl att = new AttributesImpl();
        InputStream is = input.getByteStream();
        ch.startElement(DirectAudioNormaliser.AUDIO_URI, 
                        DirectAudioNormaliser.FLAC_TAG, 
                        DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG, 
                        att);
        InputStreamEncoder.base64Encode(is, ch);
        ch.endElement(DirectAudioNormaliser.AUDIO_URI, 
                      DirectAudioNormaliser.FLAC_TAG, 
                      DirectAudioNormaliser.AUDIO_PREFIX + ":" + DirectAudioNormaliser.FLAC_TAG);
    }
}
