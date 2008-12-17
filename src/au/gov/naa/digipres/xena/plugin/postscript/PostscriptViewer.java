package au.gov.naa.digipres.xena.plugin.postscript;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.XmlContentHandlerSplitter;

import com.softhub.ts.ViewFrame;

/**
 * View for Postscript files.
 * 
 * @see au.gov.naa.digipres.xena.kernel.view
 * 
 * @authors Kamaj Jayakantha de Mel and Quang Phuc Tran(Eric)
 * 
 * @since 14-Feb-2007
 * @version 1.3
 * 
 */
public class PostscriptViewer extends XenaView {

	/**
	 * genarated by eclise
	 */
	private static final long serialVersionUID = 1L;

	//Declare the launch button 
	private JButton launchPosttscriptViewButton = new JButton();

	//A byte array to keep postscript data
	byte[] result;

	//Declare a frame to put the view of Postscript file in
	static ViewFrame frameView = new ViewFrame();

	public PostscriptViewer() {
		try {
			buttonInitialise();

		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	/**
	 * Get the input file type
	 * @return Boolean tag
	 */
	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPostscriptFileType.class).getTag());
	}

	/**
	 * Get viewer name
	 * @return String: Viewer Name  	 
	 */
	public String getViewName() {
		return "Postscript Viewer";
	}

	/**
	 * Use Content handler to split the xena postscript file
	 */
	public ContentHandler getContentHandler() throws XenaException {
		XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
		splitter.addContentHandler(new XMLFilterImpl() {
			StringBuffer stringBuffer = new StringBuffer();

			//Decode input Postscript file
			public void endDocument() {
				sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
				byte[] bytes = null;
				try {
					bytes = decoder.decodeBuffer(stringBuffer.toString());
					result = bytes;

				} catch (IOException x) {
					JOptionPane.showMessageDialog(PostscriptViewer.this, x);
				}
			}

			//Append Postscript content into a buffer
			public void characters(char[] ch, int start, int length)
					throws SAXException {
				stringBuffer.append(ch, start, length);
			}

		});
		return splitter;
	}

	/**
	 * (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.view.XenaView#doClose()
	 */
	@Override
	public void doClose() {
		super.doClose();

	}

	/**
	 * Initialise the launch button
	 * @throws Exception
	 */
	private void buttonInitialise() throws Exception {
		launchPosttscriptViewButton.setText("View in External Window");
		launchPosttscriptViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchPostscriptButton_actionPerformed(e);
			}
		});
		this.add(launchPosttscriptViewButton, java.awt.BorderLayout.CENTER);
	}

	/**
	 * Launch the Postscript viewer
	 * @param e
	 */
	public void launchPostscriptButton_actionPerformed(ActionEvent e) {
		try {
			// Creates a temperrary Postscript file for viewing
			File tmpFile = File.createTempFile("postScript", ".ps");
			//Delete the temporary file when the application is closed
			tmpFile.deleteOnExit();
			//Create output stream to write to temporary file
			FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
			//Writes the output stream to the tempFile
			fileOutputStream.write(result);
			fileOutputStream.close();

			// Call open action of the toastScript viwer to display the Popstscript file 
			frameView.openAction(e, tmpFile.getPath());
			frameView.setVisible(true);

		} catch (IOException x) {
			JOptionPane.showMessageDialog(this, x.getMessage());
		}
	}

}