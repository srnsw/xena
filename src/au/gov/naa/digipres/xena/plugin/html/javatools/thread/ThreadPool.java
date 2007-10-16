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

package au.gov.naa.digipres.xena.plugin.html.javatools.thread;

import java.util.*;

/**
 *  Implements a pool of threads. Only a limited number of threads can execute
 *  at once. If you try to exceed the thread limit you will block waiting. This
 *  avoids overloading a machine with threads.
 *
 * @created    6 September 2001
 */
public class ThreadPool {
	List threads = new ArrayList();

	ThreadGroup threadGroup;

	List jobs = new ArrayList();

	/**
	 * @param  name        the name of the ThreadGroup
	 * @param  maxThreads  the maximum number of threads we allow at once.
	 */

	public ThreadPool(String name, int maxThreads) {
		threadGroup = new ThreadGroup(name + "-Pool");
		for (int i = 0; i < maxThreads; i++) {
			threads.add(new ThreadPoolThread(this, threadGroup, "Worker" + i));
		}
	}

	/**
	 * Get a job to execute and remove it from the job queue.
	 * @return Job that needs executing.
	 */
	synchronized Runnable getJob() {
		if (jobs.size() < 1) {
			return null;
		} else {
			Runnable rtn = (Runnable) jobs.get(0);
			jobs.remove(rtn);
			return rtn;
		}
	}

	/**
	 * Start a job now. If no threads are available, start a new thread to make
	 * sure it gets going now.
	 * @param runnable
	 */
	synchronized public void startNow(Runnable runnable) {
		if (!anyReadyThreads()) {
			Thread thread = new Thread(threadGroup, runnable);
			thread.start();
		}
	}

	/**
	 *  Execute the given procedure when a thread slot becomes available.
	 *
	 * @param  runnable  the runnable that represents the work that shall be done.
	 * @return           false if there was no slot available. Try again later.
	 */
	synchronized public void queueJob(Runnable runnable) {
		anyReadyThreads();
		jobs.add(runnable);
		notify();
	}

	/**
	 * Wait for all threads to complete.
	 * Make sure you call shutdown() first.
	 */
	synchronized public void join() {
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			Thread thread = (Thread) it.next();
			try {
				thread.join();
			} catch (InterruptedException e) {
				// Doesn't matter
			}
		}
	}

	/**
	 *  How many threads are active right now? This number can change at any time,
	 *  so it is not particularly useful except for providing interesting
	 *  diagnostics.
	 *
	 * @return    Description of the Returned Value
	 */
	synchronized public int numberOfActiveThreads() {
		int rtn = 0;
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			ThreadPoolThread thread = (ThreadPoolThread) it.next();
			if (thread.isAlive() && thread.isBusy()) {
				rtn++;
			}
		}
		return rtn;
	}

	/**
	 * Check whether any pool threads are currently available for use. If the
	 * next available thread is unstarted we start it. This allows us to start
	 * the pool on an as-needed basis. If true is returned there is a thread
	 * available. If false is returned there is not a thread available unless
	 * one finished half-way through the function.
	 * @return whether there are currently available threads.
	 */
	synchronized public boolean anyReadyThreads() {
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			ThreadPoolThread thread = (ThreadPoolThread) it.next();
			if (!thread.isBusy()) {
				if (!thread.isAlive()) {
					thread.start();
				}
				return true;
			}
		}
		return false;
	}

	synchronized public int numberOfReadyThreads() {
		int rtn = 0;
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			ThreadPoolThread thread = (ThreadPoolThread) it.next();
			if (thread.isAlive() && !thread.isBusy()) {
				rtn++;
			}
		}
		return rtn;
	}

	/**
	 * Shutdown all pool threads safely. This does not wait for them to
	 * complete. Call join() if you want that.
	 */
	synchronized public void shutdown() {
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			ThreadPoolThread thread = (ThreadPoolThread) it.next();
			thread.shutdown();
		}
		notifyAll();
	}

	@Override
    protected void finalize() throws java.lang.Throwable {
		super.finalize();
		shutdown();
	}
}
