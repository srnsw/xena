package au.gov.naa.digipres.xena.kernel.type;

/**
 * Base class for miscellaneous non-File types of data input.
 */
abstract public class MiscType extends Type {
	
	@Override
    public String getMimeType() {
        return "unknown/unknown";
    }
}
