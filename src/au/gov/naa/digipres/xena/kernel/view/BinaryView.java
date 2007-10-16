/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.kernel.view;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.XenaBinaryFileType;

/**
 * View for random binary files. Due to the nature of binary files, there isn't
 * any particularly useful view for them, so this view is very basic. It would
 * be nice if it was enhanced to perhaps show octal values or something like
 * Unix od.
 *
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

	@Override
    public boolean canShowTag(String tag) throws XenaException {
		return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaBinaryFileType.class).getTag());
	}

	private void jbInit() throws Exception {
		jLabel1.setText("Binary Data");
		this.add(jLabel1, BorderLayout.NORTH);
	}

	@Override
    public String getViewName() {
		return "Binary View";
	}

}
