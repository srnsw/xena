/*
 * Created on 09/05/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class MovType extends FileType
{
	@Override
	public String getMimeType()
	{
		return "video/quicktime";
	}
	
	@Override
	public String getName()
	{
		return "MOV (Unsupported)";
	}

}
