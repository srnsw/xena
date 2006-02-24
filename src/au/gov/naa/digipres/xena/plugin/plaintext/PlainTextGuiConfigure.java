package au.gov.naa.digipres.xena.plugin.plaintext;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Configure the PlainTextToXenaPlainTextNormaliser.
 * @author Chris Bitmead
 */
public class PlainTextGuiConfigure extends AbstractGuiConfigureNormaliser {
	public PlainTextGuiConfigure() {
		addPanel("PlainText Configure", new PlainTextConfigurePanel0(this));
	}

	public Class normaliserClass() {
		return PlainTextToXenaPlainTextNormaliser.class;
	}

	public Component start() throws XenaException {
		nextOk(true);
		return super.start();
	}
}
