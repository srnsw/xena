package au.gov.naa.digipres.xena.plugin.office;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a flat XML OpenOffice.org file.
 *
 * @author Chris Bitmead
 */
public class FlatOOoFileType extends FileType {
	public String getName() {
		return "Flat OpenOffice.org";
	}

	public String fileExtension() {
		return "xml";
	}
    
    public String getMimeType() {
        return "text/xml";
    }
}
