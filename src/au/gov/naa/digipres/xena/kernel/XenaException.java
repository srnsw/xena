package au.gov.naa.digipres.xena.kernel;

/**
 * Exception class for Xena specific errors. Extends the Exception class.
 * 
 * @author Chris Bitmead
 * @author Andrew Keeling
 * @author Justin Waddell
 * @version 2.0
 * @see Exception
 */
public class XenaException extends Exception {
	/**
     * Default serial.
     */
    private static final long serialVersionUID = 1L;

    /** 
     * Construct a xena exception with the provided message.
     * @param mesg The error message String to set for the exception.
     */
    public XenaException(String mesg) {
		super(mesg);
	}

    /**
     * Construct a Xean Exception with a specific error string and a parent
     * exception.
     * 
     * @param mesg
     * @param exception
     */
	public XenaException(String mesg, Exception exception) {
		super(mesg, exception);
	}

    /**
     * Constructor with parent exception.
     * @param exception The parent exception that caused this XenaException.
     */
	public XenaException(Exception exception) {
		super(exception);
	}
}
