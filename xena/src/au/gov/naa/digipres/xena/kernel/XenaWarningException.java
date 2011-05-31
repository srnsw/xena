package au.gov.naa.digipres.xena.kernel;

/**
 * Exception to represent a blocking event that should be seen by the user as a warning
 * 
 * @author toneill
 *
 */
public class XenaWarningException extends XenaException {
	
	private static final long serialVersionUID = 1L;
	
	/** 
	 * Construct a Xena Warning Exception with the provided message.
	 * @param mesg The error message String to set for the exception.
	 */
	public XenaWarningException(String mesg) {
		super(mesg);
	}

	/**
	 * Construct a Xena Warning Exception with a specific error string and a parent
	 * exception.
	 * 
	 * @param mesg
	 * @param exception
	 */
	public XenaWarningException(String mesg, Exception exception) {
		super(mesg, exception);
	}

	/**
	 * Constructor with parent exception.
	 * @param exception The parent exception that caused this XenaWarningException.
	 */
	public XenaWarningException(Exception exception) {
		super(exception);
	}
}
