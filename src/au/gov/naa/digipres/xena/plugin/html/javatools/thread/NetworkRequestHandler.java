package au.gov.naa.digipres.xena.plugin.html.javatools.thread;
import java.io.*;
import java.net.*;

public interface NetworkRequestHandler extends Runnable {
	void setSocket(Socket socket);

	void setServer(NetworkServer server);
}