package au.gov.naa.digipres.xena.plugin.html.javatools.http;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Date;

public abstract class AbstractHttpResponseHandler extends HttpResponseHandler {
	URL url;

	Socket socket;

	public AbstractHttpResponseHandler(URL url, Socket socket) {
		this.url = url;
		this.socket = socket;
	}

	public void processURL(PrintStream ps, boolean sendFile) throws IOException {
		boolean exists = exists();
		printHeaders(ps, exists);
		if (sendFile) {
			printBody(ps, exists);
		}
	}

	public void printBody(PrintStream ps, boolean exists) throws IOException {
		if (exists) {
			sendFile(ps);
		} else {
			send404(ps);
		}
	}

	public void print(PrintStream ps, String s) {
		ps.print(s);
//		System.out.print(s);
	}

	public void printHeaders(PrintStream ps, boolean exists) throws IOException {
//		int rCode = 0;
		if (!exists) {
//			rCode = HttpURLConnection.HTTP_NOT_FOUND;
			print(ps, "HTTP/1.1 " + HttpURLConnection.HTTP_NOT_FOUND + " not found");
			print(ps, EOL);
		} else {
//			rCode = HttpURLConnection.HTTP_OK;
			print(ps, "HTTP/1.1 " + HttpURLConnection.HTTP_OK + " OK");
			print(ps, EOL);
		}
//		server.log("From " + s.getInetAddress().getHostAddress() + ": GET " +
//				   targ.getAbsolutePath() + "-->" + rCode);
		print(ps, "Server: Simple java");
		print(ps, EOL);
		print(ps, "Date: " + (new Date()));
		print(ps, EOL);
		if (exists) {
			String mimeType = getMimeType();
			print(ps, "Content-type: " + mimeType);
			print(ps, EOL);
			long length = getLength();
			if (0 <= length) {
				print(ps, "Content-length: " + length);
				print(ps, EOL);
			}
			long lastMod = lastModified();
			if (0 <= lastMod) {
				print(ps, "Last Modified: " + new Date(lastMod));
				print(ps, EOL);
			}
		}
		print(ps, EOL);
	}

	abstract public boolean exists();

	abstract public long lastModified();

	abstract public long getLength();

	abstract public String getMimeType();

	public void send404(PrintStream ps) throws IOException {
		print(ps, "Not Found");
		print(ps, EOL);
		print(ps, EOL);
		print(ps, "The requested resource was not found.");
		print(ps, EOL);
	}

	abstract public void sendFile(PrintStream ps) throws IOException;

	public URL getUrl() {
		return url;
	}

	public Socket getSocket() {
		return socket;
	}
}