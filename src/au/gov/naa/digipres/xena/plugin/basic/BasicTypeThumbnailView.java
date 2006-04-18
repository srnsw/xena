package au.gov.naa.digipres.xena.plugin.basic;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.helper.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * A simple view for basic types.
 *
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public class BasicTypeThumbnailView extends XenaView {
	JPopupMenu popup = new JPopupMenu();

	JLabel textArea = new JLabel();

	JScrollPane scrollPane = new JScrollPane();

	private BorderLayout borderLayout1 = new BorderLayout();

	public BasicTypeThumbnailView() {
		setViewType(THUMBNAIL_VIEW);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getViewName() {
		return "Basic Type Thumbnail View";
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaStringFileType.class).getTag()) ||
			tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaIntegerFileType.class).getTag()) ||
			tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaDateTimeFileType.class).getTag());
	}

/*	public void updateViewFromElement() {
		String linetext = getElement().getText();
		textArea.setText(linetext);
	} */

	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(getTmpMemContentHandler());
		splitter.addContentHandler(new XMLFilterImpl() {
			StringBuffer sb = new StringBuffer();
			public void endDocument() {
				textArea.setText(sb.toString());
			}
			public void characters(char[] ch, int start, int length) throws SAXException {
				sb.append(ch, start, length);
			}
		});
		return splitter;
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		scrollPane.getViewport().add(textArea);
		this.add(scrollPane, BorderLayout.CENTER);
	}
}
