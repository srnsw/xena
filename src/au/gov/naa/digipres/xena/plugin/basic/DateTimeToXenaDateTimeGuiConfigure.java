package au.gov.naa.digipres.xena.plugin.basic;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Configure the DateTimeToXenaDateTimeNormaliser class.
 *
 * @author Chris Bitmead
 */
public class DateTimeToXenaDateTimeGuiConfigure extends AbstractGuiConfigureNormaliser {
	public DateTimeToXenaDateTimeGuiConfigure() {
		addPanel("DateTime Configure", new DateTimeToXenaDateTimePanel0(this));
	}

	public Component start() throws XenaException {
		nextOk(true);
		return super.start();
	}

	public Class normaliserClass() {
		return DateTimeToXenaDateTimeNormaliser.class;
	}
}
