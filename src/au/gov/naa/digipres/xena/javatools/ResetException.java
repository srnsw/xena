package au.gov.naa.digipres.xena.javatools;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class ResetException extends Exception {
	public ResetException(String mesg) {
		super(mesg);
	}
	public ResetException(String mesg, Throwable exception) {
		super(mesg, exception);
	}
	public ResetException(Throwable exception) {
		super(exception);
	}
}