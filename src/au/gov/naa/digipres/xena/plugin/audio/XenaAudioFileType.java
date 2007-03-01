/*
 * Created on 28/02/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

public class XenaAudioFileType extends XenaFileType
{
    public String getTag() {
        return "audio:flac";
    }

    public String getNamespaceUri() {
        return "http://preservation.naa.gov.au/audio/1.0";
    }
}
