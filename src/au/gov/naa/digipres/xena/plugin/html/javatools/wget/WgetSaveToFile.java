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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html.javatools.wget;

import java.io.*;
import java.net.*;

public class WgetSaveToFile implements WgetProcessUrl {
	File dir;

	public WgetSaveToFile(File dir) {
		this.dir = dir;
	}

	public void process(URLConnection connection) throws IOException, UnknownServiceException {
		if (Wget.isRedirected(connection)) {
			return;
		}
		URL url = connection.getURL();
		// System.out.println("URL: " + url);
		// Figure out the file name to save as.
		String path = url.getPath();
		if (url.getQuery() != null) {
			path += "?" + url.getQuery();
		}
		if (path.equals("") || path.endsWith("/")) {
			path = path + "index.html";
		}
		File newDir = new File(dir, url.getHost());
		File file = new File(newDir, path);
		// Make sure directory exists
		if (file.getParentFile().exists()) {
			if (!file.getParentFile().isDirectory()) {
				throw new IOException(file.getParent() + " is not a directory");
			}
		} else {
			file.getParentFile().mkdirs();
		}
		// Save the file
		BufferedOutputStream bos = null;
		try {
			InputStream is = connection.getInputStream();
			bos = new BufferedOutputStream(new FileOutputStream(file));
			byte[] buf = new byte[4096];
			int len;
			while (0 <= (len = is.read(buf))) {
				bos.write(buf, 0, len);
			}
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
	}
}
