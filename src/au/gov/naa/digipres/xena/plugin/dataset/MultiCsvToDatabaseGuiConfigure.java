package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Configure the MultiCsvToXenaDatabaseNormaliser.
 *
 * @author Chris Bitmead
 */
public class MultiCsvToDatabaseGuiConfigure extends AbstractGuiConfigureNormaliser {
	public MultiCsvToDatabaseGuiConfigure() {
	}

	public Class normaliserClass() {
		return MultiCsvToXenaDatabaseNormaliser.class;
	}

	public Component start() throws XenaException {
		MultiInputSource minput = (MultiInputSource)getInputSource();
		for (int i = 0; i < minput.size(); i++) {
			CsvToXenaDatasetNormaliser normaliser = ((MultiCsvToXenaDatabaseNormaliser)getNormaliser()).getNormaliser(i);
			XenaInputSource input = new XenaInputSource(minput.getSystemId(i), PluginManager.singleton().getTypeManager().lookup(CsvFileType.class));
			addPanel(minput.getSystemId(i), new CsvToDatasetGuiConfigurePanel0(this, normaliser, input));
			addPanel(minput.getSystemId(i), new CsvToDatasetGuiConfigurePanel1(this, normaliser, input));
		}
		return super.start();
	}

}
