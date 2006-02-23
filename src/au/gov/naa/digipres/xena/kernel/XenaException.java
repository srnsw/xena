package au.gov.naa.digipres.xena.kernel;

/**
 * Exception class for Xena specific errors.
 *
 * @author Chris Bitmead
 * @version 1.0
 */
public class XenaException extends Exception {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public XenaException(String mesg) {
		super(mesg);
	}

	public XenaException(String mesg, Exception exception) {
		super(mesg, exception);
	}

	public XenaException(Exception exception) {
		super(exception);
	}
}
