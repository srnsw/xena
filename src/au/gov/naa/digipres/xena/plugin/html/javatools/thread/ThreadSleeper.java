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

/**
 *  Allows a thread to sleep when there is nothing to do. This class is designed
 *  to be used by threads which loop around doing work but which need to sleep
 *  when there is nothing to do. Other threads need to notify this worker thread
 *  when work is available. This is a tricky threading problem which is why it
 *  is made into a separate class. The reason it is tricky is that there are
 *  potential race conditions between another thread setting a flag that there
 *  is something to do, checking the flag and going to sleep if there is nothing
 *  to do. The java synchronized keyword doesn't seem to be of help here - We
 *  can't put a synchronized block around code to check a flag and go to sleep,
 *  because the sleep() would cause the synchronized lock to block other threads
 *  trying to notify us of new work. <P>
 *
 *  The semantics of waitForWork() is this: waitForWork() will sleep if
 *  notifyOfWork() has not been called anytime between the end of the previous
 *  call to waitForWork and now. Depending on when wakeup is called it is
 *  possible that occasionally waitForwork might not sleep when it is supposed
 *  to, but it is guaranteed that waitForWork will never sleep when it is not
 *  supposed to. So the calling program must handle the case of there being
 *  nothing to do but it is guaranteed that we won't sleep forever when there is
 *  something to do. The goal is to sleep forever when there is nothing to do,
 *  but we are prepared to accept the occasional case of not sleeping - briefly
 *  - when there is nothing to do. That's ok, the next time through your
 *  application's main loop the sleep will activate correctly if there is
 *  nothing to do. That is a minor race condition we can't avoid, but sleeping
 *  when there is work to do is a flaw we MUST avoid. <P>
 *
 *  The interesting thing about the class is that it coordinates threads without
 *  the use of the synchronized java keyword. The reason is that using
 *  synchronized with methods that sleep must be a bad thing - it would hang any
 *  other thread trying to synchronize on the same lock. So we use the following
 *  method. It is quite hard (even for the author) to prove in your mind that
 *  this code works because the threading problems posed are tricky. The key
 *  part is that while waitForWork sets the doSleep flag before checking the
 *  wakeupTicker, the notifyOfWork method does the reverse - it increments the
 *  wakeupTicker before checking the doSleep flag. Then the notifyOfWork will
 *  keep looping till its job is done. However because it calls yield() it is
 *  unlikely to loop many times.
 *
 * BUGS:
 * I think I shouldn't have written this class. It performs the same behaviour as
 * Object.wait() and Object.notify. Needs further investigation whether to
 * obliterate this class.
 *
 * @created    November 22, 2001
 */
public class ThreadSleeper {
	/**
	 *  The thread that may need to sleep.
	 */
	Thread t;
	volatile boolean doSleep = false;
	/**
	 *  This is a running counter that gets incremented when work becomes
	 *  available. The precise value of this variable is irrelevant. What is
	 *  important is whether the number is bigger than the last time we checked.
	 *  The last value when checked is stored in oldWakeupTicker. wakeupTicker is
	 *  always >= oldWakeupTicker.
	 */
	volatile long wakeupTicker = 0;
	volatile long oldWakeupTicker = 0;

	/**
	 *  Create a ThreadSleeper
	 */
	public ThreadSleeper() {
	}

	/**
	 *  Same as
	 *
	 * @see    waitForWork(long) except we potentially sleep forever.
	 */
	public void waitForWork() {
		waitForWork(Long.MAX_VALUE);
	}

	/**
	 *  Possibly sleep until new work is available. Don't sleep if work is
	 *  available. waitForWork is guaranteed not to sleep when there is work to do,
	 *  but it is possible it might occasionally not sleep when there is no work to
	 *  do. Caller must handle the case of nothing to do. The current thread at the
	 *  time of calling this function is stored internally, and it will be the
	 *  thread that wakes up when notifyOfWork is eventually called.
	 *
	 * @param  time  Description of Parameter
	 */
	public void waitForWork(long time) {
		if (t != null) {
			throw new RuntimeException("More than one thread trying to waitForWork in ThreadSleeper");
		}
		try {
			t = Thread.currentThread();
			doSleep = true;
			if (wakeupTicker == oldWakeupTicker) {
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					// Keep Going
				}
			}
		} finally {
			// Using a finally { } clause is probably overkill, because I can't
			// imagine anything throwing an exception. However, who really
			// knows what APIs throw RuntimeExceptions?
			oldWakeupTicker = wakeupTicker;
			doSleep = false;
			t = null;
		}
	}

	/**
	 *  Notify the thread that work is available. Any thread can call this.
	 */
	public void notifyOfWork() {
		// We need to loop around until we are sure that the thread is awake
		// i.e. not between the doSleep = true / doSleep = false barriers.
		// First we increment the ticker which says that there's more work.
		// The loop is because this method could run in between the if()
		// and the sleep() in waitForWork. In that case we have to keep
		// interrupting until we are sure we have entered and exited the
		// sleep() call. The yield() is because once we have interrupt()ed,
		// CPU time is best devoted to making sure the other thread wakes
		// up instead of a fruitless busy loop of re-interrupting.
		long tick = ++wakeupTicker;
		while (doSleep && oldWakeupTicker < tick) {
			t.interrupt();
			Thread.yield();
		}
	}
}
