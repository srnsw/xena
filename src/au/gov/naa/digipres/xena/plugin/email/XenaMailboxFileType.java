package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena mailbox summary files.
 *
 * @author Chris Bitmead
 */
public class XenaMailboxFileType extends XenaFileType {
	public String getTag() {
		return "mailbox:mailbox";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/mailbox/1.0";
	}
}
