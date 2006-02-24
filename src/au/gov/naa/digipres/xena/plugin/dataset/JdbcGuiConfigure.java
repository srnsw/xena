package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Class for configuring hte JdbcNormaliser class
 *
 * @author Chris Bitmead
 */
public class JdbcGuiConfigure extends AbstractGuiConfigureNormaliser {
	public JdbcGuiConfigure() {
		addPanel("Configure Database Connection", new JdbcGuiConfigurePanel0(this));
		addPanel("List Tables to Export", new JdbcGuiConfigurePanel1(this));
	}

	public Class normaliserClass() {
		return JdbcNormaliser.class;
	}

	public Component start() throws XenaException {
		return super.start();
	}
}
