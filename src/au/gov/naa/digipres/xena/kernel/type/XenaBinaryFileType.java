package au.gov.naa.digipres.xena.kernel.type;

/**
 * Class for representing the Xena binary-object output type.
 *
 * @author Chris Bitmead
 */
public class XenaBinaryFileType extends XenaFileType {
	public String getTag() {
		return "binary-object:binary-object";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/binary-object/1.0";
	}
}
