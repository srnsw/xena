package au.gov.naa.digipres.xena.plugin.email;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Class to configure the EmailToXenaEmailNormaliser for use with PST files.
 *
 * @author Chris Bitmead
 */
public class PstToXenaEmailGuiConfigure extends AbstractGuiConfigureNormaliser {
	public PstToXenaEmailGuiConfigure() {
		addPanel("Mbox Dir Select Folder", new ImapConfigurePanel1(this));
	}

	public Class normaliserClass() {
		return EmailToXenaEmailNormaliser.class;
	}

	public Type normaliserType() throws XenaException {
		return TypeManager.singleton().lookup(PstFileType.class);
	}

	public Component start() throws XenaException {
		return super.start();
	}

}
