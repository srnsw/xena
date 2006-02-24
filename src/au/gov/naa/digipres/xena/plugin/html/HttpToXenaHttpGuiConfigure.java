package au.gov.naa.digipres.xena.plugin.html;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Configure the HttpToXenaHttpNormaliser.
 *
 * @author Chris Bitmead
 */
public class HttpToXenaHttpGuiConfigure extends AbstractGuiConfigureNormaliser {
	public HttpToXenaHttpGuiConfigure() {
		addPanel("Http Configure", new HttpToXenaHttpGuiConfigurePanel0(this));
	}

	public Class normaliserClass() {
		return HttpToXenaHttpNormaliser.class;
	}

	public Component start() throws XenaException {
		nextOk(true);
		return super.start();
	}
}
