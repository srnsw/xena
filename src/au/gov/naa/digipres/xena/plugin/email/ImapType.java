package au.gov.naa.digipres.xena.plugin.email;
import au.gov.naa.digipres.xena.kernel.type.MiscType;

/**
 * Type to represent an IMAP data source input
 *
 * @author Chris Bitmead
 */
public class ImapType extends MiscType {
	public String getName() {
		return "IMAP Mail Box";
	}
}
