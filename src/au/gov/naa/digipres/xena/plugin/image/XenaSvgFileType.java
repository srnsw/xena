package au.gov.naa.digipres.xena.plugin.image;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type representing Xena XHTML file type.
 *
 * @author Chris Bitmead
 */
public class XenaSvgFileType extends XenaFileType 
{
	public String getTag() 
	{
		return "svg";
	}

	public String getNamespaceUri() 
	{
		return "http://preservation.naa.gov.au/svg/1.0";
	}
}
