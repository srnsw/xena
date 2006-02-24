package au.gov.naa.digipres.xena.plugin.email;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Gui configure the EmailToXenaEmailNormaliser for a directory of MBOXes.
 *
 * @author Chris Bitmead
 */
public class MboxDirToXenaEmailGuiConfigure extends AbstractGuiConfigureNormaliser {
	public MboxDirToXenaEmailGuiConfigure() {
		addPanel("Mbox Dir Select Folder", new ImapConfigurePanel1(this));
	}

	public Class normaliserClass() {
		return EmailToXenaEmailNormaliser.class;
	}

	public Type normaliserType() throws XenaException {
		return TypeManager.singleton().lookup(MboxDirFileType.class);
	}

	public Component start() throws XenaException {
		return super.start();
	}

}
