package au.gov.naa.digipres.xena.plugin.pdf;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent Xena PDF files.
 *
 * @author Chris Bitmead
 */
public class XenaPdfFileType extends XenaFileType {
    public String getTag() {
        return "pdf:pdf";
    }

    public String getNamespaceUri() {
        return "http://preservation.naa.gov.au/pdf/1.0";
    }
}
