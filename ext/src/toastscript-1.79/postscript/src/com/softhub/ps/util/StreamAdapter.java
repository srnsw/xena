
package com.softhub.ps.util;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class StreamAdapter {

	public InputStream in;
	public PrintStream out;

	public void connect(StreamAdapter adapter) throws IOException {
		PipedOutputStream pout = new PipedOutputStream();
		PipedInputStream pin = new PipedInputStream(pout);
		PipedOutputStream pout2 = new PipedOutputStream();
		PipedInputStream pin2 = new PipedInputStream(pout2);
		this.in = pin2;
		this.out = new PrintStream(pout, true);
		adapter.in = pin;
		adapter.out =  new PrintStream(pout2, true);
	}
/*
	public class Protocol implements Runnable {

		private PrintStream out;
		private Thread thread;

		public Protocol(PrintStream out) {
			this.out = out;
			thread = new Thread(this);
			thread.start();
		}

		public void dispose() {
			thread = null;
		}

		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				while (thread == Thread.currentThread()) {
					out.println(reader.readLine());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
*/
}
