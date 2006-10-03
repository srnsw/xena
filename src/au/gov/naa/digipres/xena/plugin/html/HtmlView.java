package au.gov.naa.digipres.xena.plugin.html;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.gov.naa.digipres.xena.kernel.PrintXml;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.util.JdomXenaView;

/**
 * View to display HTML. We use the Java internal HTML widget to display the HTML,
 * but the fact is the Java HTML viewer is pathetic, so we provide a button
 * to open it in an external browser.
 *
 * @author Chris Bitmead
 */
public class HtmlView extends JdomXenaView {
	JScrollPane scrollPane = new JScrollPane();

	HTMLEditorKit htmlKit = new HTMLEditorKit();

	JEditorPane ep = new JEditorPane();

	private JButton externalButton = new JButton();

	public HtmlView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getViewName() {
		return "HTML View";
	}

	public void initListeners() {

	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaHtmlFileType.class).getTag());
	}

	public void updateViewFromElement() throws XenaException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new HackPrintXml().printXml(getElement(), os);
			ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
			try {
				htmlKit.read(in, ep.getDocument(), 0);
			} catch (Exception x) {
				// Sometimes wierd HTML freaks it out.
				throw new XenaException(x);
			}
			ep.setCaretPosition(0);
		} catch (IOException e) {
			throw new XenaException(e);
		}
	}

	private void jbInit() throws Exception {
		ep.setEditorKit(htmlKit);
		ep.setContentType("text/html; charset=" + PrintXml.singleton().ENCODING);
		ep.getDocument().putProperty("IgnoreCharsetDirective", new Boolean(true));
		scrollPane.getViewport().add(ep);
		this.add(scrollPane, BorderLayout.CENTER);
		externalButton.setToolTipText("");
		externalButton.setText("Show in Browser Window");
		externalButton.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				externalButton_actionPerformed(e);
			}
		});
		this.add(externalButton, BorderLayout.NORTH);

	}

	/**
	 * This fixes a bug in Internet Explorer's rendering, specifically
	 * it allows META REFRESH to work.
	 */
	public static class HackPrintXml extends PrintXml {
		public Format getFormatter() {
			Format format = super.getFormatter();
			format.setExpandEmptyElements(true);
			return format;
		}
	}

	void externalButton_actionPerformed(ActionEvent e) {
		File output = null;
		try {
			output = File.createTempFile("output", ".html");
			output.deleteOnExit();
			String ENCODING = "UTF-8";
			OutputStream os = new FileOutputStream(output);
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			OutputStreamWriter out = new OutputStreamWriter(os, ENCODING);
			outputter.output(getElement(), out);
			out.close();
			os.close();
			BrowserLauncher.openURL(output.toURL().toString());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex);
		} finally {
			// If we don't sleep the file disappears before the browser
			// has time to start.
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				// Nothing.
			}
			if (output != null) {
				output.delete();
			}
		}
	}
}
