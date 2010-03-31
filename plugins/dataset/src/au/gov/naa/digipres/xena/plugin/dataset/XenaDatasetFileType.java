package au.gov.naa.digipres.xena.plugin.dataset;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

/**
 * Class representing a Xena dataset file.
 *
 * @author Chris Bitmead.
 */
public class XenaDatasetFileType extends XenaFileType {
	public String getTag() {
		return ("dataset:dataset");
	}

	public String getNamespaceUri() {
		return "http://preservation.naa.gov.au/dataset/1.0";
	}
}
