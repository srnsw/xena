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

package au.gov.naa.digipres.xena.plugin.html.javatools.util;

import java.io.*;

public class CopyFile {

	public static void recursiveCopyFile(File from, File to) throws IOException {
		recursiveCopyFile(from, to, null);
	}

	public static void recursiveCopyFile(File from, File to, CopyFilter filter) throws IOException {
		if (from.isDirectory()) {
			File[] listing = from.listFiles();
			for (int i = 0; i < listing.length; i++) {
				File newTo = new File(to, listing[i].getName());
				recursiveCopyFile(listing[i], newTo);
			}
		} else {
			FileInputStream is = new FileInputStream(from);
			if (filter != null) {
				to = filter.filter(from, to);
			}
			to.getParentFile().mkdirs();
			FileOutputStream os = new FileOutputStream(to);
			try {
				byte[] buf = new byte[4096];
				int n;
				while (0 < (n = is.read(buf))) {
					os.write(buf, 0, n);
				}
			} finally {
				is.close();
				os.close();
			}
		}
	}

	public interface CopyFilter {
		public File filter(File from, File suggestedTo);
	}
}
