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

import java.io.*;
import java.util.*;

// Pretty hackish, but what alternative is there?
// Dumb Java won't let you save the newly created
// ByteArrayOutputStream before calling super().
// Can someone find the designers of Java and take
// them out and have them flogged?
class HttpDummy extends PrintWriter {
	ByteArrayOutputStream baos;

	HttpDummy(ByteArrayOutputStream baos) {
		super(baos);
		this.baos = baos;
	}
}

/**
 * Utility class for writing Http protocol and headers.
 */
public class HttpWriter extends HttpDummy {
	PrintWriter out;
	String httpCode = "200";
	String httpMesg = "OK";
	String server = "Custom";
	String type = "text/text";

	/**
	 * Create a HttpWriter connected to a port.
	 * @param out an output writer connected to an open http port.
	 */
	public HttpWriter(PrintWriter out) {
		super(new ByteArrayOutputStream());
		this.out = out;
	}

	/**
	 * Set the header to return success.
	 */
	public void setSuccess() {
		httpCode = "200";
		httpMesg = "OK";
	}

	/**
	 * Set the header to return failure.
	 * @param mesg an error message
	 */
	public void setFailure(String mesg) {
		httpCode = "500";
		httpMesg = mesg;
	}

	/**
	 * Set the name of the server in the http header.
	 */
	public void setServer(String v) {
		server = v;
	}

	/**
	 * Set the mime type in the http header.
	 */
	public void setType(String v) {
		type = v;
	}

	/**
	 * Close this HttpWriter. This will cause the http document to be written.
	 * It is up to the caller to close the original port.
	 */
	@Override
    public void close() {
		out.print("HTTP/1.1 " + httpCode + " " + httpMesg + "\n");
		out.print("Date: " + new Date() + "\n");
		out.print("Server: " + server + "\n");
		out.print("mimetype: " + type + "\n");
		super.close();
		String mesg = baos.toString();
		out.print("Content-length: " + mesg.length() + "\n\n");
		out.print(mesg);
	}
}
