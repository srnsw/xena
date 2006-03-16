package au.gov.naa.digipres.xena.kernel.decoder;
import java.io.IOException;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.ToXml;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XmlSerializable;

/**
 *  Super-class for decoders. Allows a source to be decoded into another source
 *  typically Ascii.
 *
 * @see xena.kernel.DecoderManager
 * @author     Chris Bitmead
 * @created    2 July 2002
 */

public abstract class Decoder implements XmlSerializable {
	/**
	 * @return    The name of the decoder
	 */
	public abstract String getName();

	/**
	 * @param  source           Source to be decoded
	 * @return                  decoded Source
	 */
	public abstract XenaInputSource decode(XenaInputSource source) throws IOException;

	public String toString() {
		return getName();
	}

	public Element toXml() {
		return ToXml.toXmlBasic(this);
	}

	public void fromXml(Element element) {
		// Nothing
	}
}
