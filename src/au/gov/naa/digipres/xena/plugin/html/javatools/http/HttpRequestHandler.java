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

package au.gov.naa.digipres.xena.plugin.html.javatools.http;

import java.io.*;
import java.net.*;
import au.gov.naa.digipres.xena.plugin.html.javatools.thread.*;

public class HttpRequestHandler extends AbstractNetworkRequestHandler {
	final static int BUF_SIZE = 2048;

	/* buffer to use for requests */
	byte[] buf = new byte[BUF_SIZE];

	static final byte[] EOL = {(byte) '\r', (byte) '\n'};

	public HttpRequestHandler() {
	}

	/*
	 * synchronized void setSocket(Socket s) { this.s = s; notify(); }
	 */

	public synchronized void run() {
		/*
		 * try { Thread.currentThread().sleep(5000); }catch (InterruptedException e) {e.printStackTrace();}
		 */
		try {
			handleHttpRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void handleHttpRequest() throws IOException {
		try {
			InputStream is = new BufferedInputStream(socket.getInputStream());
			PrintStream ps = new PrintStream(socket.getOutputStream());
			/*
			 * we will only block in read for this many milliseconds before we fail with java.io.InterruptedIOException,
			 * at which point we will abandon the connection.
			 */

			/* zero out the buffer from last time 4 */
			for (int i = 0; i < BUF_SIZE; i++) {
				buf[i] = 0;
			}
			/*
			 * We only support HTTP GET/HEAD, and don't support any fancy HTTP options, so we're only interested really
			 * in the first line.
			 */
			int nread = 0, r = 0;

			outerloop: while (nread < BUF_SIZE) {
				r = is.read(buf, nread, BUF_SIZE - nread);
				if (r == -1) {
					/* EOF */
					return;
				}
				int i = nread;
				nread += r;
				for (; i < nread; i++) {
					if (buf[i] == (byte) '\n' || buf[i] == (byte) '\r') {
						/* read one line */
						break outerloop;
					}
				}
			}
			/* are we doing a GET or just a HEAD */
			boolean doingGet;
			/* beginning of file name */
			int index;
			if (buf[0] == (byte) 'G' && buf[1] == (byte) 'E' && buf[2] == (byte) 'T' && buf[3] == (byte) ' ') {
				doingGet = true;
				index = 4;
			} else if (buf[0] == (byte) 'H' && buf[1] == (byte) 'E' && buf[2] == (byte) 'A' && buf[3] == (byte) 'D' && buf[4] == (byte) ' ') {
				doingGet = false;
				index = 5;
			} else {
				/* we don't support this method */
				ps.print("HTTP/1.0 " + HttpURLConnection.HTTP_BAD_METHOD + " unsupported method type: ");
				ps.write(buf, 0, 5);
				ps.write(EOL);
				ps.flush();
				socket.close();
				return;
			}

			int i = 0;
			/*
			 * find the file name, from: GET /foo/bar.html HTTP/1.0 extract "/foo/bar.html"
			 */
			for (i = index; i < nread; i++) {
				if (buf[i] == (byte) ' ') {
					break;
				}
			}
			String str = new String(buf, index, i - index);
			// server.p("S: " + s);
			URL url;
			try {
				url = new URL(str);
			} catch (MalformedURLException e) {
				url = new URL("http", "localhost", str);
			}
			HttpResponseHandler response = getResponseHandler(url, socket);
			response.processURL(ps, doingGet);
		} finally {
			socket.close();
		}
	}

	public HttpResponseHandler getResponseHandler(URL url, Socket socket) {
		return new FileHttpResponseHandler(url, socket);
	}
}
