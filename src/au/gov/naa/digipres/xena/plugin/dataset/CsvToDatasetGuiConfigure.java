package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Configure the CsvToXenaDatasetNormaliser.
 *
 * @author Chris Bitmead
 */
public class CsvToDatasetGuiConfigure extends AbstractGuiConfigureNormaliser {
	public CsvToDatasetGuiConfigure() {
	}

	public Class normaliserClass() {
		return CsvToXenaDatasetNormaliser.class;
	}

	public Component start() throws XenaException {
		addPanel("Dataset Configure", new CsvToDatasetGuiConfigurePanel0(this, (CsvToXenaDatasetNormaliser)getNormaliser(), getInputSource()));
		addPanel("Dataset Configure", new CsvToDatasetGuiConfigurePanel1(this, (CsvToXenaDatasetNormaliser)getNormaliser(), getInputSource()));
		return super.start();
	}
}
