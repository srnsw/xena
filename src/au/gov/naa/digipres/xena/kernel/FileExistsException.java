/*
 * Created on 22/02/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel;

import java.io.File;

public class FileExistsException extends XenaException
{
	public FileExistsException(File file)
	{
		super("The file " + file + " already exists");
	}
}
