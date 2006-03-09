package au.gov.naa.digipres.xena.kernel.type;

/**
 * Base class for miscellaneous non-File types of data input.
 */
abstract public class MiscType extends Type {
	abstract public String getName();

    public String getMimeType() {
        return "unknown/unknown";
    }
}
