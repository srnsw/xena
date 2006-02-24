package au.gov.naa.digipres.xena.plugin.html.javatools.util;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class ReflectException extends Exception {

	public ReflectException(String mesg) {
		super(mesg);
	}
	public ReflectException(String mesg, Throwable exception) {
		super(mesg, exception);
	}
	public ReflectException(Throwable exception) {
		super(exception);
	}
}