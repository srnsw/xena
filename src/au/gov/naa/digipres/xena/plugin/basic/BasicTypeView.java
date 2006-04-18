package au.gov.naa.digipres.xena.plugin.basic;
import java.awt.BorderLayout;

import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * Standard view for basic types.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public class BasicTypeView extends XenaView {
	JPopupMenu popup = new JPopupMenu();

	JTextArea textArea = new JTextArea();

//	MyMenu popupItems = new MyMenu(this);
//
//	MyMenu customItems = new MyMenu(this);
//
//	MyMenu menus[];

	JScrollPane scrollPane = new JScrollPane();

	private BorderLayout borderLayout1 = new BorderLayout();

	public BasicTypeView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getViewName() {
		return "Basic Type View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaStringFileType.class).getTag()) ||
			tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaIntegerFileType.class).getTag()) ||
			tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaDateTimeFileType.class).getTag());
	}

	public void initListeners() {
		addPopupListener(popup, textArea);
//		XenaMenu.initListenersAll(menus);
	}

/*	public void updateViewFromElement() {
		menus = new MyMenu[] {
			popupItems, customItems};
		textArea.setEditable(false);
		textArea.setFont(new java.awt.Font("Monospaced", 0, 12));
		String linetext = getElement().getText();
		textArea.setText("");
		textArea.append(linetext);
		//textArea.append("\n");
		popupItems.makeMenu(popup);
		XenaMenu.syncAll(menus);
	} */

//	public void makeMenu(JMenu menu) {
//		customItems.makeMenu(menu);
//	}

//	class MyMenu extends XenaMenu {
//		public JRadioButtonMenuItem none;
//
//		public JRadioButtonMenuItem line;
//
//		public JRadioButtonMenuItem word;
//
//		BasicTypeView view;
//
//		MyMenu(BasicTypeView view) {
//			this.view = view;
//			ButtonGroup group = new ButtonGroup();
//			none = new JRadioButtonMenuItem("No Wrap");
//			line = new JRadioButtonMenuItem("Wrap at Character");
//			word = new JRadioButtonMenuItem("Wrap at Word");
//			group.add(none);
//			group.add(line);
//			group.add(word);
//		}
//
//		public void sync() {
//			if (view.textArea.getWrapStyleWord()) {
//				word.setSelected(true);
//			} else if (view.textArea.getLineWrap()) {
//				line.setSelected(true);
//			} else {
//				none.setSelected(true);
//			}
//		}
//
//		public void makeMenu(Container component) {
//			component.add(none);
//			component.add(line);
//			component.add(word);
//		}
//
//		public void initListeners() {
//			word.addActionListener(
//				new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					textArea.setWrapStyleWord(true);
//					textArea.setLineWrap(true);
//					XenaMenu.syncAll(menus);
//				}
//			});
//			line.addActionListener(
//				new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					textArea.setWrapStyleWord(false);
//					textArea.setLineWrap(true);
//					XenaMenu.syncAll(menus);
//				}
//			});
//			none.addActionListener(
//				new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					textArea.setWrapStyleWord(false);
//					textArea.setLineWrap(false);
//					XenaMenu.syncAll(menus);
//				}
//			});
//		}
//	}

	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpMemContentHandler());
		splitter.addContentHandler(new XMLFilterImpl() {
			StringBuffer sb = new StringBuffer();

			public void endDocument() {
//				menus = new MyMenu[] {
//					popupItems, customItems};
//				textArea.setEditable(false);
//				textArea.setFont(new java.awt.Font("Monospaced", 0, 12));
////				String linetext = getElement().getText();
//				textArea.setText("");
//				textArea.append(sb.toString());
//				//textArea.append("\n");
//				popupItems.makeMenu(popup);
//				XenaMenu.syncAll(menus);
			}

			public void characters(char[] ch, int start, int length) throws SAXException {
				sb.append(ch, start, length);
			}

		});
		return splitter;
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		textArea.setEditable(false);
		textArea.setRows(1);
		scrollPane.getViewport().add(textArea);
		this.add(scrollPane, BorderLayout.CENTER);
	}
}
