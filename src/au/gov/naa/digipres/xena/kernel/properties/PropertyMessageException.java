/*
 * Created on 3/04/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

/**
 * Indicates that an operation on a plugin property has triggered a message
 * which should be displayed, logged etc by the calling application.
 * 
 * @author justinw5
 * created 3/04/2006
 * xena
 * Short desc of class:
 */
public class PropertyMessageException extends Exception
{
	/**
	 * @param message
	 */
	public PropertyMessageException(String message)
	{
		super(message);
	}
}
