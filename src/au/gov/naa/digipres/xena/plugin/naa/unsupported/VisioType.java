/*
 * Created on 09/05/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class VisioType extends FileType
{
	@Override
	public String getMimeType()
	{
		return "application/visio";
	}
	
	@Override
	public String getName()
	{
		return "Visio Type";
	}

}
