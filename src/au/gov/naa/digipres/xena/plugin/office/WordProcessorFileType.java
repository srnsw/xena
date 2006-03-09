package au.gov.naa.digipres.xena.plugin.office;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a word processor file.
 *
 * @author Chris Bitmead
 */
public class WordProcessorFileType extends FileType {

	public WordProcessorFileType() {
	}

	public String getName() {
		return "Word Processor";
	}
    
    public String getMimeType() {
        return "application/vnd.ms-word";
    }
}
