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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.util;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Base class for views of text. Many views have plain text, and by putting this
 * in a common place, it makes it easier for all sorts of other views to have
 * common code. The class inherits ChunkedView, so that if the text is particularly
 * large, it won't blow up memory.
 * @created    1 July 2002
 */

abstract public class TextView extends ChunkedView {
	protected JPopupMenu popup = new JPopupMenu();

	protected PrintableTextArea textArea = new PrintableTextArea();

	// protected MyMenu popupItems = new MyMenu();
	//
	// protected MyMenu customItems = new MyMenu();
	//
	// protected MyMenu menus[];

	protected int DEFAULT_TAB_SIZE = 2;

	protected JScrollPane scrollPane = new JScrollPane();

	protected final Font monoFont = new java.awt.Font("Monospaced", 0, 12);

	protected final Font unicodeFont = new Font("Arial Unicode MS", Font.PLAIN, 12);

	protected Font myFont = monoFont;

	public TextView() {
		try {
			tjbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * @param node Description of Parameter @exception XenaException Description of Exception
	 */
	/*
	 * public void updateViewFromElement() throws XenaException { textArea.setText(""); updateText();
	 * textArea.setFont(myFont); XenaMenu.syncAll(menus); textArea.setCaretPosition(0); }
	 * 
	 * abstract public void updateText() throws XenaException;
	 */

	public void appendLine(String line) {
		textArea.append(line);
		textArea.append("\n");
		if (myFont == monoFont && 0 <= monoFont.canDisplayUpTo(line)) {
			myFont = unicodeFont;
		}
	}

	@Override
    public void PrintView() {
		textArea.doPrintActions();
	}

	@Override
    public void initListeners() {
		addPopupListener(popup, textArea);
		// XenaMenu.initListenersAll(menus);
	}

	/**
	 * @param  menu  Description of Parameter
	 */

	// public void makeMenu(JMenu menu) {
	// customItems.makeMenu(menu);
	// }
	protected void tjbInit() throws Exception {
		textArea.setTabSize(DEFAULT_TAB_SIZE);
		textArea.setEditable(false);
		Font font = new java.awt.Font("Monospaced", 0, 12);
		// The "/2" is arbitrary, but seems to give a pleasing result.
		// textArea.setRows((MainFrame.singleton().getDesktopPane().getHeight() / 2) / font.getSize());
		textArea.setFont(font);
		// menus = new MyMenu[] {
		// popupItems, customItems};
		// popupItems.makeMenu(popup);
		// XenaMenu.syncAll(menus);
		// FIX Help thingy
		// CSH.setHelpIDString(textArea, "plaintext.overview");
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(scrollPane);
		scrollPane.getViewport().add(textArea);
	}

	// Might be redone at some stage in the future
	// /**
	// * @created 1 July 2002
	// */
	// class MyMenu extends XenaMenu {
	// public JRadioButtonMenuItem none;
	//
	// public JRadioButtonMenuItem line;
	//
	// public JRadioButtonMenuItem word;
	//
	// public JMenuItem tabSize;
	//
	// JMenu wrapMenu;
	//
	// // TextView view;
	//
	// public JRadioButtonMenuItem mono;
	//
	// public JRadioButtonMenuItem unicode;
	//
	// JMenu fontMenu;
	//
	// MyMenu() {
	// // this.view = view;
	// wrapMenu = new JMenu("Wrap");
	// ButtonGroup group = new ButtonGroup();
	// none = new JRadioButtonMenuItem("No Wrap");
	// line = new JRadioButtonMenuItem("Wrap at Character");
	// word = new JRadioButtonMenuItem("Wrap at Word");
	// tabSize = new JMenuItem("Set Tab Size");
	// group.add(none);
	// group.add(line);
	// group.add(word);
	//
	// fontMenu = new JMenu("Font");
	// group = new ButtonGroup();
	// mono = new JRadioButtonMenuItem("Monospaced");
	// unicode = new JRadioButtonMenuItem("Unicode");
	// group.add(mono);
	// group.add(unicode);
	//
	// }
	//
	// public void sync() {
	// if (TextView.this.textArea.getWrapStyleWord()) {
	// word.setSelected(true);
	// } else if (TextView.this.textArea.getLineWrap()) {
	// line.setSelected(true);
	// } else {
	// none.setSelected(true);
	// }
	// if (textArea.getFont() == monoFont) {
	// mono.setSelected(true);
	// } else {
	// unicode.setSelected(true);
	// }
	// }
	//
	// public void makeMenu(Container component) {
	// component.add(wrapMenu);
	// wrapMenu.add(none);
	// wrapMenu.add(line);
	// wrapMenu.add(word);
	// fontMenu.add(mono);
	// fontMenu.add(unicode);
	// component.add(tabSize);
	// component.add(fontMenu);
	// }
	//
	// public void initListeners() {
	// word.addActionListener(
	// new ActionListener() {
	//
	// public void actionPerformed(ActionEvent e) {
	// textArea.setWrapStyleWord(true);
	// textArea.setLineWrap(true);
	// XenaMenu.syncAll(menus);
	// }
	// });
	// line.addActionListener(
	// new ActionListener() {
	//
	// public void actionPerformed(ActionEvent e) {
	// textArea.setWrapStyleWord(false);
	// textArea.setLineWrap(true);
	// XenaMenu.syncAll(menus);
	// }
	// });
	// none.addActionListener(
	// new ActionListener() {
	//
	// public void actionPerformed(ActionEvent e) {
	// textArea.setWrapStyleWord(false);
	// textArea.setLineWrap(false);
	// XenaMenu.syncAll(menus);
	// }
	// });
	// mono.addActionListener(
	// new ActionListener() {
	//
	// public void actionPerformed(ActionEvent e) {
	// textArea.setFont(monoFont);
	// XenaMenu.syncAll(menus);
	// }
	// });
	// unicode.addActionListener(
	// new ActionListener() {
	//
	// public void actionPerformed(ActionEvent e) {
	// textArea.setFont(unicodeFont);
	// XenaMenu.syncAll(menus);
	// }
	// });
	// tabSize.addActionListener(
	// new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// String tabs = (String)JOptionPane.showInputDialog(MainFrame.singleton(), "Enter Number of Charaters per Tab
	// Stop",
	// "Enter Number of Charaters per Tab Stop",
	// JOptionPane.QUESTION_MESSAGE, null, null,
	// Integer.toString(textArea.getTabSize()));
	// if (tabs != null) {
	// try {
	// int itabs = Integer.parseInt(tabs);
	// if (itabs < 1) {
	// throw new NumberFormatException("Cannot have zero or negative number");
	// }
	// textArea.setTabSize(itabs);
	// textArea.updateUI();
	// } catch (NumberFormatException ex) {
	// MainFrame.singleton().showError(ex);
	// }
	// }
	// }
	// });
	// }
	// }

	public ChunkedContentHandler getTextHandler() throws XenaException {
		ChunkedContentHandler ch = new ChunkedContentHandler() {
			StringBuffer buf = null;

			@Override
            public void characters(char[] ch, int start, int length) throws SAXException {
				if (buf != null) {
					buf.append(ch, start, length);
				}
			}

			@Override
            public void doStart(String namespaceURI, String localName, String qName, Attributes atts) {
				buf = new StringBuffer();
			}

			@Override
            public void doEnd(String namespaceURI, String localName, String qName) {
				appendLine(buf.toString());
				buf = null;
				textArea.setCaretPosition(0);
			}

		};
		return ch;
	}

	@Override
    public ContentHandler getContentHandler() throws XenaException {
		textArea.setText("");
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpFileContentHandler());
		ContentHandler ch = getTextHandler();
		splitter.addContentHandler(ch);
		return splitter;
	}
}
