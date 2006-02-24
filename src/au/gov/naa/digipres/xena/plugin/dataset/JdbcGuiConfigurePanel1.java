package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;

import javax.swing.JPanel;

import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.javatools.ListEditor;

/**
 * Panel 1 for configuring hte JdbcNormaliser class
 *
 * @author Chris Bitmead
 */
public class JdbcGuiConfigurePanel1 extends JPanel implements GuiConfigureSubPanel {
	BorderLayout borderLayout1 = new BorderLayout();

	ListEditor listEditor = new ListEditor();

	JdbcGuiConfigure configure;

	public JdbcGuiConfigurePanel1(JdbcGuiConfigure configure) {
		this.configure = configure;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void start() {
		JdbcNormaliser n = (JdbcNormaliser)configure.getNormaliser();
		listEditor.setItems(n.getQueries());
	}

	public void finish() {
		JdbcNormaliser n = (JdbcNormaliser)configure.getNormaliser();
		n.setQueries(listEditor.getItems());
	}

	public void activate() {
		configure.nextOk(0 < listEditor.getItems().size());
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		this.add(listEditor, BorderLayout.CENTER);
		listEditor.addModifiedListener(
			new ListEditor.ListModifiedListener() {
			public void modified() {
				configure.nextOk(0 < listEditor.getItems().size());
			}
		});
	}

}
