/**
 * This file is part of website.
 * 
 * website is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * website is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with website; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.website.webserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * A web server to enable web sites normalised with Xena to be viewed.
 * 
 * Xena normalised web sites by normalising each file of the website separately, and producing
 * an index file which maps the original path and name of the file to the name of the normalised file.
 * 
 * The web server is started when the user first opens a normalised website for viewing. The web server will 
 * receive requests for the original file names from a browser. It will use the index to map to the normalised
 * version of this file, export (unwrap) the file, and then send it to the requesting browser.
 * 
 * @author Justin Waddell
 *
 */
public class WebServer extends Thread {
	private static final int DEFAULT_SERVER_PORT = 9362;
	private static final int MAX_PORTS_TO_ATTEMPT = 50;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private boolean running = true;

	// Where worker threads stand idle
	Vector<WebServerWorker> threads;

	// initial number of worker threads. More workers will be added if there are none available for a request.
	public static int numWorkers = 5;

	private ServerSocket serverSocket;

	private int port = DEFAULT_SERVER_PORT;
	private int timeout = 5000;

	private Xena xena;
	private File exportDir;
	private Map<String, String> linkIndex;
	private File sourceDir;

	public WebServer(Xena xena, Map<String, String> linkIndex, File sourceDir) throws IOException {
		super("Web Server (primary)");

		this.xena = xena;
		this.linkIndex = linkIndex;
		this.sourceDir = sourceDir;

		// Initialise temporary export directory
		exportDir = new File(System.getProperty("java.io.tmpdir"), String.valueOf(System.currentTimeMillis()));
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}

		// Initialise the server socket. If the port is already in use, try more ports until we find one that is free.
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException iex) {
			// Assume that this failed because the port was in use
			boolean portFound = false;
			for (int newPort = port + 1; newPort <= port + MAX_PORTS_TO_ATTEMPT; newPort++) {
				try {
					serverSocket = new ServerSocket(newPort);

					// If this succeeds, set the port to newPort and flag that we have found a socket
					port = newPort;
					portFound = true;
					break;
				} catch (IOException newiex) {
					// Do nothing, just start the loop again to try another port
				}
			}

			// If we have not found a free port to use, give up and throw an exception
			if (!portFound) {
				throw new IOException("Could not find a port to use for the web server - tried ports " + DEFAULT_SERVER_PORT + " to "
				                      + (DEFAULT_SERVER_PORT + MAX_PORTS_TO_ATTEMPT));
			}
		}

		// Start worker threads. They will remain in a wait state until notified by the setSocket method.
		threads = new Vector<WebServerWorker>();
		for (int i = 0; i < numWorkers; ++i) {
			WebServerWorker w = new WebServerWorker(xena, linkIndex, sourceDir, exportDir, threads);
			new Thread(w, "Web Server worker #" + i).start();
			threads.addElement(w);
		}

	}

	@Override
	public void run() {

		try {
			// This loop is only ended when the shutdownServer method is called from an external source.
			while (running) {

				// The socket will listen for a connection for this period of time. 
				serverSocket.setSoTimeout(timeout);

				try {
					// Listen for a connection. If no connection is made,
					// a SocketTimeoutException is thrown and the loop will be restarted.
					Socket clientSocket = serverSocket.accept();

					// Set a timeout on the client socket in case the connection is lost.
					clientSocket.setSoTimeout(timeout);

					WebServerWorker w = null;
					synchronized (threads) {
						// threads will be empty if all the initial workers are busy
						if (threads.isEmpty()) {
							// Create a new worker and start it.
							WebServerWorker ws = new WebServerWorker(xena, linkIndex, sourceDir, exportDir, threads);
							ws.setSocket(clientSocket);
							new Thread(ws, "additional worker").start();
						} else {
							// Get the first worker, and activate it by setting the socket
							w = threads.elementAt(0);
							threads.removeElementAt(0);
							w.setSocket(clientSocket);
						}
					}
				} catch (SocketTimeoutException stex) {
					// Do nothing, just try again
				}
			}

			// If we have reached this point then the server is shutting down.
			logger.log(Level.FINE, "Shutting down server.");
			serverSocket.close();
			stopWorkers();

		} catch (Exception ex) {
			// An unexpected error occurred - log the error and shut down the server
			logger.log(Level.SEVERE, "An unexpected error occurred when initialising the web server. Shutting down server.", ex);
			stopWorkers();
		}
	}

	/**
	 * Shut down the web server. This simply sets the running field to
	 * false, which will cause the main thread loop to end, and the worker
	 * threads to be stopped.
	 */
	public void shutdownServer() {
		running = false;
	}

	/**
	 * Stops any active WebServerWorker threads.
	 */
	private void stopWorkers() {
		for (WebServerWorker workerThread : threads) {
			workerThread.stopWorker();
		}

		// Clear out threads for the next run
		threads.clear();
	}

	public int getPort() {
		return port;
	}

}

class WebServerWorker extends Thread {
	// HTTP codes
	/** 2XX: generally "OK" */
	public static final int HTTP_OK = 200;

	/** 4XX: client error */
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	public static final int HTTP_PAYMENT_REQUIRED = 402;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_BAD_METHOD = 405;
	public static final int HTTP_NOT_ACCEPTABLE = 406;
	public static final int HTTP_PROXY_AUTH = 407;
	public static final int HTTP_CLIENT_TIMEOUT = 408;
	public static final int HTTP_CONFLICT = 409;
	public static final int HTTP_GONE = 410;
	public static final int HTTP_LENGTH_REQUIRED = 411;
	public static final int HTTP_PRECON_FAILED = 412;
	public static final int HTTP_ENTITY_TOO_LARGE = 413;
	public static final int HTTP_REQ_TOO_LONG = 414;
	public static final int HTTP_UNSUPPORTED_TYPE = 415;

	/** 5XX: server error */
	public static final int HTTP_SERVER_ERROR = 500;
	public static final int HTTP_INTERNAL_ERROR = 501;
	public static final int HTTP_BAD_GATEWAY = 502;
	public static final int HTTP_UNAVAILABLE = 503;
	public static final int HTTP_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_VERSION = 505;

	final static int BUF_SIZE = 2048;
	static final byte[] EOL = {(byte) '\r', (byte) '\n'};

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	// Socket to client we're handling
	private Socket socket;

	private Vector<WebServerWorker> threads;

	private Xena xena;
	private Map<String, String> linkIndex;
	private File exportDir;
	private File sourceDir;

	private boolean workerRunning = true;

	private Logger workerLogger = Logger.getLogger(this.getClass().getName());

	public WebServerWorker(Xena xena, Map<String, String> linkIndex, File sourceDir, File exportDir, Vector<WebServerWorker> threads) {
		socket = null;
		this.xena = xena;
		this.linkIndex = linkIndex;
		this.sourceDir = sourceDir;
		this.exportDir = exportDir;
		this.threads = threads;
	}

	/**
	 * Set the client socket with which this worker will communicate.
	 * This method will activate the worker.
	 * @param socket
	 */
	synchronized void setSocket(Socket socket) {
		this.socket = socket;
		notify();
	}

	/**
	 * Completely shut down this worker.
	 */
	synchronized void stopWorker() {
		workerRunning = false;
		notify();
	}

	@Override
	public synchronized void run() {
		// This loop is only ended when the stopWorker method is called from an external source.
		while (workerRunning) {
			// The worker is initialised without a reference to a socket, and thus it will enter a waiting state.
			// Once it is given a socket it be notified and will begin execution.
			if (socket == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}
			}

			// If the thread has been closed, do nothing further
			if (workerRunning) {
				try {
					handleClient();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Go back in wait queue if there's fewer than numHandler connections.
				socket = null;
				synchronized (threads) {
					if (threads.size() >= WebServer.numWorkers) {
						// Too many threads, exit this one
						return;
					}
					threads.addElement(this);
				}
			}
		}
	}

	/**
	 * Handle a HTTP request from a client socket. This method extracts the requested file from the
	 * first line of the request, uses the link index to retrieve the normalised version of the file,
	 * and calls sendFile to send the response to the client.
	 * @throws IOException
	 * @throws XenaException
	 */
	private void handleClient() throws IOException, XenaException {
		InputStream is = new BufferedInputStream(socket.getInputStream());
		PrintStream ps = new PrintStream(socket.getOutputStream());
		socket.setTcpNoDelay(true);

		byte[] requestBytes = new byte[BUF_SIZE];
		try {
			int totalCharsRead = 0, currentCharsRead = 0, firstLineCharCount = 0;

			// Copy the request into the requestBuffer, making note of how many characters are in the first line.
			outerloop: while (totalCharsRead < BUF_SIZE) {
				currentCharsRead = is.read(requestBytes, totalCharsRead, BUF_SIZE - totalCharsRead);
				if (currentCharsRead == -1) {
					/* EOF */
					return;
				}
				int i = totalCharsRead;
				totalCharsRead += currentCharsRead;
				for (; i < totalCharsRead; i++) {
					if (requestBytes[i] == (byte) '\n' || requestBytes[i] == (byte) '\r') {
						/* read one line */
						firstLineCharCount = i;
						break outerloop;
					}
				}
				firstLineCharCount = totalCharsRead;
			}

			String fullRequest = new String(requestBytes).trim();
			String firstLineOfRequest = new String(requestBytes, 0, firstLineCharCount);

			// Print the request
			workerLogger.finer("HTTP Request: " + firstLineOfRequest);
			System.out.println("\n******************************************************");
			System.out.println("***********");
			System.out.println("* Request *");
			System.out.println("***********");
			System.out.println("[" + new Date() + "]\n" + fullRequest);

			// Split the first line of the request into its components - request type, file name and HTTP version
			StringTokenizer tokenizer = new StringTokenizer(firstLineOfRequest, " ");
			if (tokenizer.countTokens() != 3) {
				// Request is not valid
				ps.print("HTTP/1.0 " + HTTP_BAD_METHOD + " invalid request: ");
				ps.print(firstLineOfRequest);
				ps.write(EOL);
				ps.flush();
				socket.close();
				return;
			}
			String requestType = tokenizer.nextToken();
			String linkName = tokenizer.nextToken();
			//			String httpVersion = tokenizer.nextToken();

			// Check for valid request type
			if (!requestType.equals("GET") && !requestType.equals("HEAD")) {
				// we don't support this method
				ps.print("HTTP/1.0 " + HTTP_BAD_METHOD + " unsupported method type: ");
				ps.print(requestType);
				ps.write(EOL);
				ps.flush();
				socket.close();
				return;
			}

			if (linkName.startsWith("/")) {
				linkName = linkName.substring(1);
			}

			// URL Decode the link name to get a filename
			String fileName = URLDecoder.decode(linkName, "UTF-8");

			// Check that this is a valid file name request. If so, send the response. If not, send an error.
			if (linkIndex.containsKey(fileName)) {
				workerLogger.finest("Found xena file " + linkIndex.get(fileName));
				sendFile(fileName, linkIndex.get(fileName), ps);
			} else {
				errorReport(ps, HTTP_NOT_FOUND, "Resource Not Found", "The requested resource was not found inside this normalised web site: "
				                                                      + linkName);
			}

			System.out.flush();

		} finally {
			// Ensure the socket is always closed.
			socket.close();
		}
	}

	/**
	 * Send the requested file via HTTP to the requesting client. 
	 * This method uses Xena to export (unwrap) the file, calls sendHeaders to
	 * produce and send the correct HTTP headers for this file, and then writes
	 * the file to the output stream connected to the client.
	 * @param originalLink
	 * @param xenaFileName
	 * @param outputStream
	 * @throws IOException
	 * @throws XenaException
	 */
	void sendFile(String originalLink, String xenaFileName, PrintStream outputStream) throws IOException, XenaException {
		System.out.println("\n***********");
		System.out.println("* Mapping *");
		System.out.println("***********");
		System.out.println("Original link: " + originalLink);
		System.out.println("Xena file name: " + xenaFileName);

		File xenaFile = new File(sourceDir, xenaFileName);
		if (!xenaFile.exists()) {
			throw new FileNotFoundException("File normalised by Xena could not be found: " + xenaFileName);
		}
		XenaInputSource xis = new XenaInputSource(xenaFile);

		// Perform the export
		ExportResult exportResult = xena.export(xis, exportDir, true);

		// Check that the export was successful
		if (!exportResult.isExportSuccessful() || !exportResult.getOutputFile().exists()) {
			throw new IOException("Xena file could not be exported: " + xenaFileName);
		}

		File exportFile = exportResult.getOutputFile();
		System.out.println("Export file name: " + exportFile);
		workerLogger.finer("Sending file " + exportFile);

		// Write the HTTP headers
		sendHeaders(exportFile, outputStream);

		// Send the exported file to the client
		outputStream.write(EOL);
		InputStream is = new FileInputStream(exportFile);
		byte[] sendBuffer = new byte[10 * 1024];
		try {
			int n;
			while ((n = is.read(sendBuffer)) > 0) {
				outputStream.write(sendBuffer, 0, n);
			}
		} finally {
			is.close();
		}
	}

	/**
	 * Writes the appropriate HTTP headers for the file to be sent to the client.
	 * The most important is the content-type header, which helps to control how browsers
	 * will display the file. This is determined by looking at the extension, or possibly by
	 * letting the GuesserManager try and determine the type.
	 * @param fileToSend
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	void sendHeaders(File fileToSend, PrintStream outputStream) throws IOException {
		System.out.println("\n************");
		System.out.println("* Response *");
		System.out.println("************");

		outputStream.print("HTTP/1.0 " + HTTP_OK + " OK");
		System.out.println("[Output] " + "HTTP/1.0 " + HTTP_OK + " OK");
		outputStream.write(EOL);
		outputStream.print("Server: Xena Website Server");
		System.out.println("[Output] " + "Server: Xena Website Server");
		outputStream.write(EOL);
		outputStream.print("Date: " + new Date());
		System.out.println("[Output] " + "Date: " + new Date());
		outputStream.write(EOL);

		// Determine the content type of the file. First, check for some common web site file types.
		String contentType = findContentType(fileToSend);

		// If this fails we'll try the GuesserManager getExactMatch method, which will only look at the extension and magic number.
		if (contentType == null) {
			GuesserManager guesserManager = xena.getPluginManager().getGuesserManager();
			XenaInputSource xis = new XenaInputSource(fileToSend);
			Type matchedType = guesserManager.getExactMatch(xis);

			// If the matched type is null, just check the extension
			if (matchedType == null) {
				matchedType = guesserManager.getExtensionMatch(xis);
			}

			// If GuesserManager couldn't find a match, just use the default content-type
			if (matchedType == null) {
				contentType = DEFAULT_CONTENT_TYPE;
			} else {
				contentType = matchedType.getMimeType();
			}
		}

		outputStream.print("Content-length: " + fileToSend.length());
		System.out.println("[Output] " + "Content-length: " + fileToSend.length());
		outputStream.write(EOL);
		outputStream.print("Last Modified: " + new Date(fileToSend.lastModified()));
		System.out.println("[Output] " + "Last Modified: " + new Date(fileToSend.lastModified()));
		outputStream.write(EOL);
		outputStream.print("Content-type: " + contentType);
		System.out.println("[Output] " + "Content-type: " + contentType);
		outputStream.write(EOL);

	}

	/**
	 * Return the content type of the given file, based on its file extension.
	 * Only common web site content types are handled by this method.
	 * @param fileToSend
	 * @return
	 */
	private String findContentType(File fileToSend) {
		String contentType = null;
		String fileName = fileToSend.getName().toLowerCase();

		if (fileName.endsWith(".txt")) {
			contentType = "text/plain";
		} else if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			contentType = "text/html";
		} else if (fileName.endsWith(".js")) {
			contentType = "text/javascript";
		} else if (fileName.endsWith(".css")) {
			contentType = "text/css";
		} else if (fileName.endsWith(".xml") || fileName.endsWith(".xsl") || fileName.endsWith(".xslt")) {
			contentType = "text/xml";
		}

		return contentType;
	}

	/**
	 * Send an HTML error page to the client
	 * @param pout
	 * @param code
	 * @param title
	 * @param msg
	 */
	private void errorReport(PrintStream pout, int code, String title, String msg) {
		StringBuilder errorBuilder = new StringBuilder();
		errorBuilder.append("HTTP/1.0 " + code);
		errorBuilder.append(" " + title);
		errorBuilder.append("\r\n" + "\r\n" + "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n");
		errorBuilder.append("<HEAD><TITLE>" + code + " " + title + "</TITLE>\r\n");
		errorBuilder.append("</HEAD><BODY>\r\n" + "<H1>" + title + "</H1>\r\n");
		errorBuilder.append(msg + "<P>\r\n");
		errorBuilder.append("<HR><ADDRESS>FileServer 1.0 at ");
		errorBuilder.append(socket.getLocalAddress().getHostName());
		errorBuilder.append(" Port ");
		errorBuilder.append(socket.getLocalPort());
		errorBuilder.append("</ADDRESS>\r\n" + "</BODY></HTML>\r\n");

		System.out.println("[Output] " + errorBuilder.toString());
		pout.print(errorBuilder.toString());
	}

}
