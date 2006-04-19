/*
 * Created on 19/04/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.image.pcx;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class PcxFileType extends FileType
{

	public PcxFileType()
	{
		super();
	}

	@Override
	public String getName()
	{
		return "PCX";
	}
	
    public String getMimeType() {
        return "image/pcx";
    }
	

}
