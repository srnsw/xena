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

package au.gov.naa.digipres.xena.plugin.html.javatools.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.activation.MimetypesFileTypeMap;

public class FileHttpResponseHandler extends AbstractHttpResponseHandler {
	File file;

	/* the web server's virtual root */

	File root;

	static MimetypesFileTypeMap mimeMap;

	static final byte[] EOL = {(byte) '\r', (byte) '\n'};

	final static int BUF_SIZE = 2048;

	/* buffer to use for requests */
	byte[] buf = new byte[BUF_SIZE];

	{
		InputStream mis = getClass().getResourceAsStream("/javatools/http/mime.types");
		mimeMap = new MimetypesFileTypeMap(mis);
	}

	public FileHttpResponseHandler(URL url, Socket s) {
		super(url, s);
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		root = new File(prefs.get("root", System.getProperty("user.dir")));
		if (!root.exists()) {
			throw new Error(root + " doesn't exist as server root");
		}
		file = urlToFile();
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public long getLength() {
		if (file.isDirectory()) {
			return -1;
		} else {
			return file.length();
		}
	}

	File urlToFile() {
		File rtn = new File(root, url.getPath());
		if (rtn.isDirectory()) {
			File index = new File(rtn, "index.html");
			if (index.exists()) {
				rtn = index;
			}
		}
		return rtn;
	}

	@Override
	public String getMimeType() {
		String rtn = null;
		if (file.isDirectory()) {
			rtn = "text/html";
		} else {
			rtn = mimeMap.getContentType(file);
			if (rtn == null) {
				rtn = "unknown/unknown";
			}
		}
		return rtn;
	}

	@Override
	public void sendFile(PrintStream ps) throws IOException {
		InputStream is = null;
		if (file.isDirectory()) {
			listDirectory(ps);
			return;
		} else {
			is = new FileInputStream(file);
		}
		try {
			int n;
			while ((n = is.read(buf)) > 0) {
				ps.write(buf, 0, n);
			}
		} finally {
			is.close();
		}
	}

	void listDirectory(PrintStream ps) throws IOException {
		ps.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		ps.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
		ps.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		ps.println(" <head>");
		ps.println("  <title>Index of " + url.getPath() + "</title>");
		ps.println(" </head>");
		ps.println(" <body>");
		ps.println("  <h1>Index of " + url.getPath() + "</h1>");
		ps.println("  <pre>");
		ps.println("Name                    Last modified      Size  Description");
		ps.println("<hr />");
		ps.println("<a href=\"..\">Parent Directory</a>                             -");
		String[] list = file.list();
		for (int i = 0; list != null && i < list.length; i++) {
			File f = new File(file, list[i]);
			Date date = new Date(file.lastModified());
			String name = list[i];
			int space = 24 - name.length();
			if (f.isDirectory()) {
				space--;
				ps.print("<a href=\"" + list[i] + "/\">" + list[i] + "/</a>");
			} else {
				ps.print("<a href=\"" + list[i] + "\">" + list[i] + "</a>");
			}
			for (int s = 0; s < space; s++) {
				ps.print(" ");
			}
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
			ps.print(sdf.format(new Date(file.lastModified())));
			if (f.isDirectory()) {
				ps.print("    " + file.length());
			} else {
				ps.print("    -");
			}
			ps.println();
		}
		ps.println("  </pre>");
		ps.println("<hr /><i>" + new Date() + "</i>");
		ps.println(" </body>");
		ps.println("</html>");
	}
}
