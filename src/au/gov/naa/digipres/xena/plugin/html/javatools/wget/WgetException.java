package au.gov.naa.digipres.xena.plugin.html.javatools.wget;

public class WgetException extends Exception {
	public WgetException() {
		super();
	}

	public WgetException(String message) {
		super(message);
	}

	public WgetException(String message, Throwable cause) {
		super(message, cause);
	}

	public WgetException(Throwable cause) {
		super(cause);
	}
}