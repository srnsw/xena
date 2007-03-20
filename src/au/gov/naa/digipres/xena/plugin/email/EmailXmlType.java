/*
 * Created on 14/03/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class EmailXmlType extends FileType
{
	public String getName() 
	{
		return "Email XML";
	}
    
    public String getMimeType() {
        return "unknown/unknown";
    }

}
