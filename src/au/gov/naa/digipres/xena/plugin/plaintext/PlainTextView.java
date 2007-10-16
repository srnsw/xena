/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.plaintext;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.TextView;

/**
 * View for Xena plaintext instances.
 *
 */
public class PlainTextView extends TextView {

	public PlainTextView() {
	}

	/*
	 * public void updateViewFromElement() throws XenaException { Namespace ns = getElement().getNamespace(); String
	 * tabs = getElement().getAttributeValue("tabsize"); if (tabs != null) {
	 * textArea.setTabSize(Integer.parseInt(tabs)); } super.updateViewFromElement(); }
	 * 
	 * public void updateText() throws XenaException { Namespace ns = getElement().getNamespace(); java.util.List list =
	 * getElement().getChildren("line", ns); Iterator it = list.iterator(); while (it.hasNext()) { Element line =
	 * (Element)it.next(); String linetext = line.getText(); appendLine(linetext); } textArea.setFont(myFont);
	 * XenaMenu.syncAll(menus); }
	 */

	@Override
    public void PrintView() {
		textArea.doPrintActions();
	}

	@Override
    public String getViewName() {
		return "Plain Text View";
	}

	@Override
    public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPlainTextFileType.class).getTag());
	}

	@Override
    public ChunkedContentHandler getTextHandler() throws XenaException {
		ChunkedContentHandler ch = super.getTextHandler();
		ch.setTagName("plaintext:line");
		return ch;
	}
}
