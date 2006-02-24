package au.gov.naa.digipres.xena.plugin.html.javatools.thread;
import java.io.*;
import java.net.*;

abstract public class AbstractNetworkRequestHandler implements NetworkRequestHandler {
	protected Socket socket;

	protected NetworkServer server;

	public AbstractNetworkRequestHandler() {
	}

	public NetworkServer getServer() {
		return server;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public void setServer(NetworkServer server) {
		this.server = server;
	}

}