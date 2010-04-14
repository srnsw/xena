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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

/**
 * View for PDF files.
 *
 */
public class PdfView extends XenaView {
	public static final String PDFVIEW_VIEW_NAME = "PDF Viewer";
	private static final long serialVersionUID = 1L;
	private SimpleViewer simpleViewer;
	private File pdfFile;

	public PdfView() {
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPdfFileType.class).getTag());
	}

	@Override
	public String getViewName() {
		return PDFVIEW_VIEW_NAME;
	}

	@Override
	public ContentHandler getContentHandler() throws XenaException {

		FileOutputStream xenaTempOS = null;
		try {
			pdfFile = File.createTempFile("pdffile", ".pdf");
			pdfFile.deleteOnExit();
			xenaTempOS = new FileOutputStream(pdfFile);
		} catch (IOException e) {
			throw new XenaException("Problem creating temporary xena output file", e);
		}

		BinaryDeNormaliser base64Handler = new BinaryDeNormaliser() {
			/*
			 * (non-Javadoc)
			 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser#endDocument()
			 */
			@Override
			public void endDocument() {
				if (pdfFile.exists()) {
					simpleViewer = new SimpleViewer();
					simpleViewer.setRootContainer(PdfView.this);
					simpleViewer.setupViewer();
					simpleViewer.openDefaultFile(pdfFile.getAbsolutePath());
				}
			}
		};

		StreamResult result = new StreamResult(xenaTempOS);
		base64Handler.setResult(result);
		return base64Handler;
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
