
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

import java.awt.Dimension;
import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

public class Console extends Frame implements Runnable {

	public final static String SHOW = "Show Console";
	public final static String HIDE = "Hide Console";

	private static String EOL = "\n";

	private Thread thread;
	private InputStream in;
	private PrintStream out;
	private TextArea textarea = new TextArea();
	private StringBuffer buffer = new StringBuffer();
	private int bufferindex;
	private Vector listeners = new Vector();

	public Console(InputStream in, PrintStream out) {
		super("Console");
		this.in = in;
		this.out = out;
		this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setLayout(new GridLayout(1,1));
		textarea.setEditable(true);
		add(textarea);
		Dimension d = getToolkit().getScreenSize();
		setLocation(d.width - 480, d.height - 288);
		setSize(460, 288);
	}

	public void addNotify() {
		super.addNotify();
		thread = new Thread(this, "ps-console");
		thread.start();
	}

	public void addActionListener(ActionListener listener) {
		listeners.addElement(listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.removeElement(listener);
	}

	protected void fireEvent(ActionEvent evt) {
		Enumeration e = listeners.elements();
		while (e.hasMoreElements()) {
			((ActionListener) e.nextElement()).actionPerformed(evt);
		}
	}

	public void setVisible(boolean state) {
		super.setVisible(state);
		String arg = state ? SHOW : HIDE;
		fireEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, arg));
	}

	public void run() {
		try {
			while (true) {
				int c = in.read();
				if (c >= 0) {
					buffer.append((char) c);
					if (c == '\r' || c == '\n') {
						flush();
					}
				}
			}
		} catch (IOException ex) {
			System.err.println("i/o ex. in console: " + ex);
		}
	}

	public void flush() {
		String text = new String(buffer);
		int textlen = textarea.getText().length();
		if (bufferindex > textlen) {
			textarea.append(text);
		} else {
			textarea.insert(text, bufferindex);
			bufferindex += text.length();
		}
		buffer = new StringBuffer();
	}

	public boolean keyDown(Event evt, int key) {
		if (key != '\n' && key != '\r')
			return false;
		int selstart = textarea.getSelectionStart();
		int selend = textarea.getSelectionEnd();
		String text = textarea.getText();
		textarea.append(EOL);
		int startindex = text.lastIndexOf(EOL, selstart-1);
		int endindex = text.indexOf(EOL, selstart);
		int si = startindex < 0 ? 0 : startindex;
		int ei = endindex < 0 ? text.length() : endindex;
		bufferindex = ei+1;
		out.println(text.substring(si, ei));
		out.flush();
		return true;
	}

	public void processEvent(AWTEvent event) {
		switch (event.getID()) {
		case Event.WINDOW_DESTROY:
			setVisible(false);
			break;
		default:
			break;
		}
		super.processEvent(event);
	}

}
