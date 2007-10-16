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

package au.gov.naa.digipres.xena.plugin.pdf;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

/**
 * View for PDF files.
 *
 */
public class PdfView extends XenaView {
	PdfViewer viewer;

	public PdfView() {
	}

	@Override
    public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPdfFileType.class).getTag());
	}

	@Override
    public String getViewName() {
		return "";
	}

	@Override
    public ContentHandler getContentHandler() throws XenaException {
		// PdfViewer doesn't like getting created in the constructor

		// TODO: Fix this so that the entire file does not need to be decoded into a single
		// byte array - not particularly scalable! The problem is that the JPedal PdfViewer does
		// not accept chunks of bytes, only a single byte array.
		viewer = new PdfViewer(new JFrame());
		this.add(viewer);
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(new XMLFilterImpl() {
			StringBuffer sb = new StringBuffer();

			@Override
            public void endDocument() {
				sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
				byte[] bytes = null;
				try {
					bytes = decoder.decodeBuffer(sb.toString());
				} catch (IOException x) {
					JOptionPane.showMessageDialog(PdfView.this, x);
				}
				viewer.openFile(bytes);
			}

			@Override
            public void characters(char[] ch, int start, int length) throws SAXException {
				sb.append(ch, start, length);
			}

		});
		return splitter;
	}

	/*
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.view.XenaView#doClose()
	 */
	@Override
	public void doClose() {
		super.doClose();

	}

}
