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

import java.lang.reflect.*;
import java.io.*;
import java.net.*;

import au.gov.naa.digipres.xena.plugin.html.javatools.thread.*;
import au.gov.naa.digipres.xena.plugin.html.javatools.util.FileLog;
import au.gov.naa.digipres.xena.plugin.html.javatools.util.Resetable;

/**
 *  A class which takes commands via a http port. The idea is to start this
 *  class as a thread in your server class and it will allow you to communicate
 *  with your server via a http port. e.g. shutdown your server, ping it, get it
 *  to re-read config files. etc. <P>
 *
 *  You pass a port number and a Java object to the constructor. Any method in
 *  that command object that has a HttpWriter as the first argument will be able
 *  to be called remotely via http and receive a response. This can be done from
 *  a browser. <P>
 *
 *  e.g. If you have a class... <PRE>
 * public class Foo() {
 *     public void ping(HttpWriter w) {
 *         w.println("Server is alive");
 *     }
 * }
 * ...
 * new HttpCommander(5000, new Foo()).start();
 * </PRE> Now if we visit http://localhost:5000/ping we get returned the message
 *  in our browser "Server is alive". We can also achieve the same thing by
 *  telnetting to localhost 5000 and entering "GET /ping". <P>
 *
 *  A useful extension would be to take http arguments via the
 *  ?arg=value&arg=value syntax and translate that into method arguments.
 *
 * @created    December 13, 2001
 */
public class HttpCommander extends Thread implements Server, Resetable {
	public final static String NAME = "HttpCommander";
	Object commands;
	Class commandsClass;
	int serverSocketPort;
	ServerSocket serverSocket;
	BufferedReader in;
	PrintWriter out;
	IOException exception;

	public HttpCommander() {
		super(NAME);
	}

	/**
	 *  Create a new HttpCommander.
	 *
	 * @param  serverSocketPort  the http socket to wait for connections
	 * @param  commands          the Java object containing http methods
	 * @exception  IOException   Description of Exception
	 */
	public HttpCommander(int serverSocketPort, Object commands) throws IOException {
		super(NAME);
		setServerSocketPort(serverSocketPort);
		setCommandObject(commands);
	}

	public void setServerSocketPort(int p) {
		serverSocketPort = p;
	}

	public void setCommandObject(Object c) {
		commands = c;
		commandsClass = commands.getClass();
	}

	public void reset() {
		// While it may be tempting to reopen the socket on reset,
		// this would be problematic if we reset ourselves via
		// a http command. This is because, at least on NT, NT won't
		// allow a port to be rebound even after the socket is closed
		// until all the child sockets that were accepted() from that
		// listener are also closed.
	}

	/**
	 *  Run the HttpCommander thread loop. Wait for connections on the given
	 *  socket.
	 */
	@Override
    public void run() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new ServerSocket(serverSocketPort);
		} catch (IOException e) {
			FileLog.singleton().error("HttpCommander", e.toString());
			exception = e;
		}
		for (;;) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				String sInput = null;
				sInput = in.readLine();
				if (sInput != null) {
					handleCommand(out, sInput);
				}
				out.close();
				in.close();
				socket.close();
			} catch (Throwable e) {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e2) {
						FileLog.singleton().error("HttpCommander", "Socket Close: :" + e2.toString());
					}
				}
				FileLog.singleton().error("HttpCommander", e.toString());
				reset();
			}
		}
	}

	/**
	 *  Shutdown the HttpCommander.
	 */
	public void shutdown() {
		try {
			serverSocket.close();
			serverSocket = null;
		} catch (IOException e) {
			FileLog.singleton().error("HttpCommander", "Can't close commander server socket");
		}
	}

	/**
	 *  Handle a http request.
	 *
	 * @param  out    the PrintWriter connected to the output port
	 * @param  input  the http command string
	 */
	private void handleCommand(PrintWriter out, String input) {
		FileLog.singleton().debug("HttpCommander", "input: " + input);
		String getCommand = "GET /";
		if (input.toUpperCase().startsWith(getCommand)) {
			int comstart = getCommand.length();
			int comend = input.indexOf(' ', comstart);
			if (comend < 0) {
				comend = input.length();
			}
			FileLog.singleton().debug("HttpCommander", "comstart: " + comstart + " comend: " + comend);
			String com = input.substring(getCommand.length(), comend);
			HttpWriter writer = new HttpWriter(out);
			Class[] classes = new Class[1];
			classes[0] = writer.getClass();
			try {
				Method method = commandsClass.getMethod(com, classes);
				Object[] objects = new Object[1];
				objects[0] = writer;
				method.invoke(commands, objects);
			} catch (Exception e) {
				writer.setFailure(e.toString());
				writer.println(e);
				e.printStackTrace(writer);
				FileLog.singleton().error("HttpCommander", e.toString());
			}
			writer.close();
		} else {
			FileLog.singleton().error("HttpCommander", "unrecognised command: " + input);
		}
	}
}
