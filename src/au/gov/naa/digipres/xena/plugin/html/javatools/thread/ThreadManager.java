package au.gov.naa.digipres.xena.plugin.html.javatools.thread;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;

import au.gov.naa.digipres.xena.plugin.html.javatools.util.*;

/**
 *  A class which starts off and manages a number of child threads. The child
 *  threads are located from class names in the "thread" properties file. The
 *  "threads" property in this properties file is a comma separated list of java
 *  class names. Each class must implement ManagedThread interface and have a
 *  constructor that takes ThreadManager as its first argument. This class will
 *  then have a new instance created. Once all these objects are created you
 *  will then be able to control them all together either by running each as a
 *  thread or shutting them all down together etc.
 *
 * @author     Chris Bitmead
 * @created    22 August 2001
 */
public class ThreadManager extends HashMap implements ManagedThread {
	Properties props;
	ThreadGroup threadGroup = new ThreadGroup("ManagedThreads");


	/**
	 *  Start up all the sub-threads. After that, the main thread just dies. It has
	 *  no further use.
	 *
	 * @exception  IOException  Description of Exception
	 */
	public ThreadManager() throws IOException {
		props = Props.singleton("thread");
		String managedThreadsProp = props.getProperty("threads");
		if (managedThreadsProp == null) {
			FileLog.singleton().severe("ThreadManager", "No ManagedThreads found");
		} else {
			StringTokenizer st = new StringTokenizer(managedThreadsProp, ",");
			while (st.hasMoreTokens()) {
				String className = st.nextToken();
				try {
					Class cls = Class.forName(className);
					Class[] cargs = {ThreadManager.class};
					Constructor con = cls.getConstructor(cargs);
					Object[] args = {this};
					ManagedThread g = (ManagedThread) con.newInstance(args);
					put(g.getName(), g);
				} catch (Exception e) {
					FileLog.singleton().severe("ThreadManager", e.toString());
				}
			}
		}
	}


	/**
	 *  This is the main entry point when debugging, and probably when run on UNIX.
	 *  When run as a MS-Windows service this doesn't get called however.
	 *
	 * @param  args             Description of Parameter
	 * @exception  IOException  Description of Exception
	 */
	public static void main(String args[]) throws IOException {
		Thread.currentThread().setName("main");
		new ThreadManager().run();
	}


	/**
	 *  Gets the name attribute of the ThreadManager object
	 *
	 * @return    The name value
	 */
	public String getName() {
		return "ThreadManager";
	}

	/**
	 *  Main processing method for the ThreadManager object
	 */
	public void run() {
		Iterator i = values().iterator();
		while (i.hasNext()) {
			ManagedThread mt = (ManagedThread) i.next();
			Thread t = new Thread(threadGroup, mt, mt.getName());
			t.start();
		}
	}


	/**
	 *  Clear all Caches
	 *
	 * @exception  ResetException  Description of Exception
	 */
	public void reset() throws ResetException {
		Iterator i = values().iterator();
		while (i.hasNext()) {
			Resetable c = (Resetable) i.next();
			c.reset();
		}
	}


	/**
	 *  Shutdown
	 */
	public void shutdown() {
		Iterator i = values().iterator();
		while (i.hasNext()) {
			Server s = (Server) i.next();
			s.shutdown();
		}
	}
}

