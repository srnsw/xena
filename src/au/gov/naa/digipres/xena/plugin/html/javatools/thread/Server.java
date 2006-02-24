package au.gov.naa.digipres.xena.plugin.html.javatools.thread;

/**
 * An interface that all server classes must implement. i.e all servers need
 * a method to be able to shutdown. For a server with multiple threads,
 * ideally each thread will implement Server, and we will call shutdown() on
 * each thread in turn.
 * @author Chris Bitmead
 */
public interface Server extends Runnable {
	/**
	 * Shutdown this server thread.
	 */
	void shutdown();
}
