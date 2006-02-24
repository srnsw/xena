package au.gov.naa.digipres.xena.plugin.html.javatools.thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import javax.activation.*;

import java.util.prefs.*;
import au.gov.naa.digipres.xena.plugin.html.javatools.http.*;

/*
 * An example of a very simple, multi-threaded HTTP server.
 * Implementation notes are in WebServer.html, and also
 * as comments in the source code.
 */
public class NetworkServer extends Thread implements Server {
	protected ThreadPool pool = new ThreadPool("NetServer", 10);

	protected Exception exception;

	protected Class requestProcessorCls;

	protected PrintStream log = null;

	protected boolean keepGoing;

	protected ServerSocket ss = null;

	/* timeout on client connections */

	protected int timeout = 0;

	protected int port = 8080;

	public static void main(String[] a) throws ClassNotFoundException {
		NetworkServer server = new NetworkServer("javatools.http.WebServerRequestHandler");
		server.run();
	}

	public NetworkServer() {
	}

	public NetworkServer(String requestProcessor) throws ClassNotFoundException {
		requestProcessorCls = Class.forName(requestProcessor);
	}

	public NetworkServer(Class requestProcessorCls) {
		this.requestProcessorCls = requestProcessorCls;
	}

	/* print to stdout */
	protected void p(String s) {
		System.out.println(s);
	}

	/* print to the log file */
	protected void log(String s) {
		synchronized (log) {
			log.println(s);
			log.flush();
		}
	}

	/* load www-server.properties from java.home */
	void loadProps() {
		Preferences prefs = Preferences.userRoot().userNodeForPackage(NetworkServer.class);
		try {

			timeout = prefs.getInt("timeout", timeout);
			String logPath = prefs.get("log", null);
			if (logPath == null) {
				log = System.out;
			} else {
				log = new PrintStream(new BufferedOutputStream(new FileOutputStream(logPath)));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

/*	void printProps() {
		p("timeout=" + timeout);
	}*/

	public NetworkRequestHandler getRequestHandler(Socket socket) {
		try {
			NetworkRequestHandler rtn = (NetworkRequestHandler)requestProcessorCls.newInstance();
			return rtn;
		} catch (IllegalAccessException ex) {
			throw new Error(ex);
		} catch (InstantiationException ex) {
			throw new Error(ex);
		}
	}

	public void run() {
		try {
			go();
		} catch (IOException e) {
			exception = e;
		}
	}

	public void go() throws IOException {
//		exception = null;
		keepGoing = true;
		loadProps();
//		printProps();

		ss = null;
		try {
			ss = new ServerSocket(port);
			while (keepGoing) {
//				System.out.println("pre-accept");
				try {
					Socket socket = ss.accept();
//					System.out.println("accept");
					System.out.flush();
					socket.setSoTimeout(timeout);
					socket.setTcpNoDelay(true);
					NetworkRequestHandler req = getRequestHandler(socket);
					req.setSocket(socket);
					req.setServer(this);
					pool.queueJob(req);
//					WebServerRequest w = new WebServerRequest(this, s);
//			w.run();
//					System.out.println("done");
					System.out.flush();
				} catch (IOException e) {
					// this happens when we shutdown
				}
			}
		} finally {
			if (ss != null) {
				try {
					ss.close();
					ss = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void shutdown() {
		keepGoing = false;
		pool.shutdown();
//		interrupt();
		try {
			ss.close();
			ss = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Exception getException() {
		return exception;
	}
}
