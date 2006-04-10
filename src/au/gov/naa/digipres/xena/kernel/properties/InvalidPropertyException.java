/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

/**
 * Exception class to indicate that an attempt has been made to set an invalid property.
 * @author justinw5
 * created 10/04/2006
 * xena
 * Short desc of class:
 */
public class InvalidPropertyException extends Exception
{

	public InvalidPropertyException(String message)
	{
		super(message);
	}

	public InvalidPropertyException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidPropertyException(Throwable cause)
	{
		super(cause);
	}

}
