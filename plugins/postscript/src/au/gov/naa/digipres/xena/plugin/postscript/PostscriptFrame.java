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
 * @author Matthew Oliver
 */

package au.gov.naa.digipres.xena.plugin.postscript;

import java.io.File;

import com.softhub.ts.ViewFrame;

/**
 * A frame that can render a postscript file.
 */
public class PostscriptFrame extends ViewFrame {
	
	private static final long serialVersionUID = 1L;

	public PostscriptFrame() {
		super();
	}
	
	/**
	 * Attempts to load a postscript file into the frame.
	 * 
	 * @param File psfile
	 * @return Boolean successful
	 */
	public boolean loadPSFile(File psfile) {
		try {
			if (psfile.exists()) {
				this.getPostScriptPane().run(psfile);
				return true;
			}
			else
				return false;
		} catch (Exception ioe)
		{
			return false;
		}
	}

}
