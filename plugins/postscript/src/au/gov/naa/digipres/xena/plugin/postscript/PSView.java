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
 * @author Matthew Oliver
 */

package au.gov.naa.digipres.xena.plugin.postscript;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

import javax.xml.transform.stream.StreamResult;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;
import org.xml.sax.ContentHandler;

/**
* XenaView for Postscript files.psfile
* 
* @see au.gov.naa.digipres.xena.kernel.view
* 
*/
public class PSView extends XenaView {
	
	
	private static final long serialVersionUID = 1L;
	public static String PSVIEW_VIEW_NAME = "Postscript Viewer";
	
	//We need a ViewFrame object that we will place the Postscript data into
	private PostscriptFrame viewFrame;
	
	private File psFile;

	@Override
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPostscriptFileType.class).getTag());
	}

	@Override
	public String getViewName() {
		return PSVIEW_VIEW_NAME;
	}

	public PSView() {
		super();
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Private method that will initialise the view.
	 */
	private void init() {		
		/*
		 * The function is currently empty because creation of the PostscriptFrame object (or more specifically ViewFrame) 
		 * sometimes causes an infinite loop! 
		 * 
		 * see the comment for the loadPSFile method.
		 */
	}
	
	/**
	 * This private function is a work around to stop xena starting an infinite looping when this plugin is loaded into xena. 
	 * Instead it may only infinite loop if you attempt to view a PostScript document. 
	 * Thank softhub's toastscript (postscript) implementation! 
	 */
	private void loadPSFile() {
		viewFrame = new PostscriptFrame();
		viewFrame.loadPSFile(psFile);
		add(viewFrame.getContentPane());
	}
	
	/**
	 * Use Content handler to split the xena postscript file
	 */
	public ContentHandler getContentHandler() throws XenaException {
		FileOutputStream xenaTempOS = null;
		try {
			psFile = File.createTempFile("psfile", ".ps");
			psFile.deleteOnExit();
			xenaTempOS = new FileOutputStream(psFile);
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
				if (psFile.exists()) {					
					loadPSFile();
				}
			}
		};

		StreamResult result = new StreamResult(xenaTempOS);
		base64Handler.setResult(result);
		return base64Handler;
	}
}
