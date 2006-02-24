package au.gov.naa.digipres.xena.plugin.basic;
import java.awt.BorderLayout;

import javax.swing.JLabel;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * View for random binary files. Due to the nature of binary files, there isn't
 * any particularly useful view for them, so this view is very basic. It would
 * be nice if it was enhanced to perhaps show octal values or something like
 * Unix od.
 *
 * @author Chris Bitmead
 */
public class BinaryView extends XenaView {
	private JLabel jLabel1 = new JLabel();

	public BinaryView() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(TypeManager.singleton().lookupXenaFileType(XenaBinaryFileType.class).getTag());
	}

	private void jbInit() throws Exception {
		jLabel1.setText("Binary Data");
		this.add(jLabel1, BorderLayout.NORTH);
	}

	public String getViewName() {
		return "Binary View";
	}

}
