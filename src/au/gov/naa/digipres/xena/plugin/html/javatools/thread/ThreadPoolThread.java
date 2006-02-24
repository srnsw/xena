package au.gov.naa.digipres.xena.plugin.html.javatools.thread;

/**
 *  An internal class only for use with ThreadPool. It will notify the
 *  ThreadPool when it is done so that ThreadPool can get another piece of work
 *  going.
 *
 * @author     Chris Bitmead
 * @created    19 September 2001
 */
public class ThreadPoolThread extends Thread {
	ThreadPool pool;

	boolean busy = false;

	boolean keepGoing = true;

	/**
	 *  Create a thread belonging to a particular ThreadPool.
	 *
	 * @param  pool the Threadpool this thread belongs to
	 * @param  threadGroup the ThreadGroup this thread belongs to
	 * @param  name the name of this thread
	 */
	public ThreadPoolThread(ThreadPool pool, ThreadGroup threadGroup, String name) {
		super(threadGroup, name);
		this.pool = pool;
	}

	/**
	 *  Loop forever, waiting for work to do.
	 */
	public void run() {
		while (keepGoing) {
			Runnable runnable = null;
			synchronized (pool) {
				try {
					runnable = pool.getJob();
					if (runnable == null) {
						pool.wait();
						runnable = pool.getJob();
					}
				} catch (InterruptedException e) {
					// shouldn't happen
					continue;
				}
			}
			try {
				if (runnable != null) {
					busy = true;
					runnable.run();
				}
			} catch (Throwable t) {
				// Nothing. Keep the thread going
			} finally {
				busy = false;
			}
		}
	}

	/**
	 * Return whether this thread is currently busy. Of course in a threading
	 * environment, this condition can change at any time, so the result should
	 * only be considered indicative.
	 * @return
	 */
	boolean isBusy() {
		return busy;
	}

	/**
	 * Set the shutdown flag, and stop the thread the next time through the loop.
	 * This does not call pool.notify, so it's up to the caller to do that.
	 */
	void shutdown() {
		keepGoing = false;
	}
}
