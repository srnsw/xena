package au.gov.naa.digipres.xena.plugin.csv;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.util.TextView;

/**
 * View for Xena plaintext instances.
 *
 * @author Chris Bitmead
 */
public class CsvView extends TextView {
    
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CsvView() {
	}

	public void PrintView() {
		textArea.doPrintActions();
	}

	public String getViewName() {
		return "Simple CSV View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaCsvFileType.class).getTag());
	}

	public ChunkedContentHandler getTextHandler() throws XenaException {
		ChunkedContentHandler ch = super.getTextHandler();
		ch.setTagName("csv:line");
		return ch;
	}
}
