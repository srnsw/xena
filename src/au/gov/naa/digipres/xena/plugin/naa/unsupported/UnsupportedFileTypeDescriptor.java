/*
 * Created on 21/05/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * This class is used by UnsupportedTypeGuesser to easily group a Xena Type 
 * with the information used to identify that file.
 * 
 * @author justinw5
 * created 24/05/2007
 * naa
 * Short desc of class:
 */
public class UnsupportedFileTypeDescriptor extends FileTypeDescriptor
{
	private Type type;
	
	public UnsupportedFileTypeDescriptor(String[] extensions, byte[][] magicNumbers, String[] mimeTypes, Type type)
	{
		super(extensions, magicNumbers, mimeTypes);
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type)
	{
		this.type = type;
	}
	
	
}
