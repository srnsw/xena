/*
 * Created on 29/11/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.viewer;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Simple class which ensures only .xena files will be visible
 * in a FileChooser.
 * 
 * @author justinw5
 * created 29/11/2005
 * xena
 * Short desc of class: .xena file filter
 */
public class XenaFileFilter extends FileFilter
{
	public static final String XENA_EXT = "XENA";

	public XenaFileFilter()
	{
		super();
	}

	@Override
	/**
	 * Only accepts file extensions matching XENA_EXT
	 */
	public boolean accept(File f)
	{
		// Show directories
		if (f.isDirectory())
		{
			return true;
		}
		else
		{
			String extension = getExtension(f);
			if (extension != null && XENA_EXT.equals( extension ))
			{
				return true;
			}
			else
			{
				return false;
			}		
		}
	}

	@Override
	public String getDescription()
	{
		return "Xena Files (*.xena)";
	}

	/**
	 * Gets the file extension for the given file - 
	 * all characters after the final "."
	 * 
	 * @param f File of which to return the extension
	 * @return
	 */
    private String getExtension(File f) {
    	if(f != null) {
    	    String filename = f.getName();
    	    int i = filename.lastIndexOf('.');
    	    if(i > 0 && i < filename.length()-1) 
    	    {
    	    	return filename.substring(i+1).toUpperCase();
    	    }
    	}
    	return null;
    }

}
