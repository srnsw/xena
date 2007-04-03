package au.gov.naa.digipres.xena.plugin.archive;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Type to represent a Xena multipage instance.
 *
 * @author Chris Bitmead
 */
public class XenaArchiveFileType extends XenaFileType 
{
	public String getTag() {
		return "archive:archive";
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/archive/1.0";
	}
}
