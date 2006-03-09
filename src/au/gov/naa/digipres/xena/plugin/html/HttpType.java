package au.gov.naa.digipres.xena.plugin.html;
import au.gov.naa.digipres.xena.kernel.type.MiscType;

/**
 * Type to represent a HTTP web site data source.
 *
 * @author Chris Bitmead
 */
public class HttpType extends MiscType {
	public String getName() {
		return "HTTP Web Site";
	}
    


    public String getMimeType() {
        return "application/http";
    }
}
