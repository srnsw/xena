package au.gov.naa.digipres.xena.plugin.plaintext;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.util.TextView;

/**
 * View for Xena plaintext instances.
 *
 * @author Chris Bitmead
 */
public class PlainTextView extends TextView {

	public PlainTextView() {
	}

	/*	public void updateViewFromElement() throws XenaException {
	  Namespace ns = getElement().getNamespace();
	  String tabs = getElement().getAttributeValue("tabsize");
	  if (tabs != null) {
	   textArea.setTabSize(Integer.parseInt(tabs));
	  }
	  super.updateViewFromElement();
	 }

	 public void updateText() throws XenaException {
	  Namespace ns = getElement().getNamespace();
	  java.util.List list = getElement().getChildren("line", ns);
	  Iterator it = list.iterator();
	  while (it.hasNext()) {
	   Element line = (Element)it.next();
	   String linetext = line.getText();
	   appendLine(linetext);
	  }
	  textArea.setFont(myFont);
	  XenaMenu.syncAll(menus);
	 } */

	public void PrintView() {
		textArea.doPrintActions();
	}

	public String getViewName() {
		return "Plain Text View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPlainTextFileType.class).getTag());
	}

	public ChunkedContentHandler getTextHandler() throws XenaException {
		ChunkedContentHandler ch = super.getTextHandler();
		ch.setTagName("plaintext:line");
		return ch;
	}
}
