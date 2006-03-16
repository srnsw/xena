package au.gov.naa.digipres.xena.plugin.email;
import java.awt.Component;

import au.gov.naa.digipres.xena.gui.AbstractGuiConfigureNormaliser;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Configure the EmailToXenaEmailNormaliser for IMAP emails.
 *
 * @author Chris Bitmead
 */
public class ImapToXenaEmailGuiConfigure extends AbstractGuiConfigureNormaliser {
	public ImapToXenaEmailGuiConfigure() {
		addPanel("Imap Email Configure Connection", new ImapConfigurePanel0(this));
		addPanel("Imap Email Select Folder", new ImapConfigurePanel1(this));
	}

	public Class normaliserClass() {
		return EmailToXenaEmailNormaliser.class;
	}

	public Type normaliserType() throws XenaException {
		return PluginManager.singleton().getTypeManager().lookup(ImapType.class);
	}

	public Component start() throws XenaException {
		return super.start();
	}
}
