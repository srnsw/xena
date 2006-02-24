package au.gov.naa.digipres.xena.plugin.basic;
import au.gov.naa.digipres.xena.kernel.Decoder;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * Standard decoder. Merely passes its input to the output. This is for when
 * data is not encoded at all.
 *
 * @author Chris Bitmead
 */
public class RawDecoder extends Decoder {
	public RawDecoder() {
	}

	public String getName() {
		return "Raw";
	}

	public XenaInputSource decode(XenaInputSource inputSource) {
		return inputSource;
	}
}
