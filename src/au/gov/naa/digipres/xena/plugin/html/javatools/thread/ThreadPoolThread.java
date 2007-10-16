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

package au.gov.naa.digipres.xena.plugin.html.javatools.thread;

/**
 *  An internal class only for use with ThreadPool. It will notify the
 *  ThreadPool when it is done so that ThreadPool can get another piece of work
 *  going.
 *
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
	@Override
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
