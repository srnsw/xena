package au.gov.naa.digipres.xena.plugin.multipage;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Configure the MultiPageNormaliser
 *
 * @author Chris Bitmead
 */
public class MultiPageGuiConfigure extends AbstractGuiConfigureNormaliser {
	public MultiPageGuiConfigure() {
		addPanel("MultiPage Configure", new MultiPageConfigurePanel0(this));
	}

	public Class normaliserClass() {
		return MultiPageNormaliser.class;
	}

	public Component start() throws XenaException {
		nextOk(true);
		return super.start();
	}
}
