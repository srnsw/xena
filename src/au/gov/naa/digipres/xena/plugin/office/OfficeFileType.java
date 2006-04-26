/*
 * Created on 12/04/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.office;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public abstract class OfficeFileType extends FileType
{
	public abstract String getOfficeConverterName();
}
