package au.gov.naa.digipres.xena.plugin.multipage;
import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.javatools.ListEditor;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Panel 0 to configure the MultiPageNormaliser
 *
 * @author Chris Bitmead
 */
public class MultiPageConfigurePanel0 extends ListEditor implements GuiConfigureSubPanel {
	MultiPageGuiConfigure configure;

	MultiPageConfigurePanel0(MultiPageGuiConfigure configure) {
		this.configure = configure;
		this.setEditable(false);
	}

	public void finish() throws XenaException {
		MultiPageNormaliser normaliser = (MultiPageNormaliser)configure.getNormaliser();
		MultiInputSource xis = (MultiInputSource)configure.getInputSource();
	}

	public void start() throws XenaException {

	}

	public void activate() throws XenaException {
		MultiPageNormaliser normaliser = (MultiPageNormaliser)configure.getNormaliser();
		MultiInputSource xis = (MultiInputSource)configure.getInputSource();
		this.setItems(xis.getSystemIds());
	}
}
