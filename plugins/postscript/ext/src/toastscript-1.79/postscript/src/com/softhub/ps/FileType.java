
package com.softhub.ps;

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

import com.softhub.ps.util.Archive;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

public class FileType extends CharSequenceType {

	protected static Vector nodes = new Vector();

	/**
	 * The shared data.
	 */
	protected FileNode node;

	public FileType(VM vm, FileType parent, URL base, StringType name, int mode) {
		super(vm);
		switch (mode) {
		case READ_MODE:
		    node = new ReadFileNode(vm, parent.node, base, name.toString());
			break;
		case WRITE_MODE:
		    node = new WriteFileNode(vm, parent.node, base, name.toString());
			break;
		default:
			throw new Stop(INTERNALERROR);
		}
		nodes.addElement(node);
	}

	protected FileType(VM vm, String name, InputStream stream) {
		super(vm);
		node = new ReadFileNode(vm, name, stream);
		nodes.addElement(node);
	}

	protected FileType(VM vm, String name, OutputStream stream) {
		super(vm);
		node = new WriteFileNode(vm, name, stream);
		nodes.addElement(node);
	}

	protected FileType(VM vm, FileNode node) {
		super(vm);
		this.node = node;
		nodes.addElement(node);
	}

	public int typeCode() {
		return FILE;
	}

	public String typeName() {
		return "filetype";
	}

	public String getName() {
		return node.name;
	}

	protected boolean isGlobal() {
		return true;
	}

	public int getLineNo() {
		return node.lineno;
	}

	public int getchar() throws IOException {
		return node.getchar();
	}

	public void putchar(int c) throws IOException {
		node.putchar(c);
	}

	public void ungetchar(int c) throws IOException {
		node.ungetchar(c);
	}

	public void exec(Interpreter ip) {
		if (isLiteral()) {
			ip.ostack.pushRef(this);
		} else {
			if (node.isWriteMode())
				throw new Stop(INVALIDFILEACCESS);
			if (node.scanner == null) {
				node.scanner = new Scanner();
			}
			ip.estack.pushRef(this);
			if (ip.scan(this, node.scanner, true) == Scanner.EOF) {
				ip.estack.pop();
				close();
			}
		}
	}

	public boolean token(Interpreter ip) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS, "token(file,rcheck)");
		if (node.isWriteMode())
			throw new Stop(INVALIDFILEACCESS);
		if (node.scanner == null) {
			node.scanner = new Scanner();
		}
		return ip.scan(this, node.scanner, false) != Scanner.EOF;
	}

	public void flush() {
		try {
		    node.flush();
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.toString());
		}
	}

	public int bytesavailable() {
		try {
		    return node.bytesavailable();
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.toString());
		}
	}

	public void setFilePosition(int pos) {
		try {
	    	node.setFilePosition(pos);
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.toString());
		}
	}

	public int getFilePosition() {
		return node.getFilePosition();
	}

	public void resetfile(URL base) {
		try {
		    node.resetfile(base);
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.toString());
		}
	}

	public int read() {
		if (!rcheck())
			throw new Stop(INVALIDACCESS, "read(file,rcheck)");
		try {
		    return node.getchar();
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	public void write(int c) {
		if (!wcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    node.putchar(c);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	public boolean read(VM vm, StringType s, StringType result[/* 1 */]) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    return node.read(vm, s, result);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Stop(IOERROR);
		}
	}

	public void write(StringType s) {
		if (!wcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    node.write(s);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	public boolean readhex(VM vm, StringType s, StringType result[/* 1 */]) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    return node.readhex(vm, s, result);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	public void writehex(StringType s) {
		if (!wcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    node.writehex(s);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	public boolean readline(VM vm, StringType s, StringType result[/* 1 */]) {
		if (!rcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    return node.readline(vm, s, result);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	public void close() {
		try {
			node.close();
		    nodes.removeElement(node);
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.toString());
		}
	}

	public void print(StringType string) {
		if (!wcheck())
			throw new Stop(INVALIDACCESS);
		try {
		    node.print(string);
		} catch (IOException ex) {
			throw new Stop(IOERROR);
		}
	}

	boolean isClosed() {
		return node.isClosed();
	}

	/**
	 * @return the shared data's save level
	 */
	public int getSaveLevel() {
		return node.getSaveLevel();
	}

	/**
	 * Compare file to another object.
	 * @param obj the other object
	 * @return true if equal; false otherwise
	 */
	public boolean equals(Object obj) {
		return obj instanceof FileType && node.name.equals(((FileType) obj).node.name);
	}

	/**
	 * @return a string representation
	 */
	public String toString() {
		return "file<" + node.name + ">";
	}

	/**
	 * @return a hash code for this file
	 */
	public int hashCode() {
		return node.name.hashCode();
	}

	public static void flushAllOpenFiles() {
		int i, n = nodes.size();
		for (i = n-1; i >= 0; i--) {
			// Backwards: Close new files before old ones.
			FileNode node = (FileNode) nodes.elementAt(i);
			try {
			    node.close();
			} catch (IOException ex) {}
		}
	}

	static abstract class FileNode extends Node {

		/**
		 * The file name.
		 */
		protected String name;

		/**
		 * The file.
		 */
		protected File file;

		/**
		 * The parent file, if any.
		 */
		protected File parent;

		/**
		 * The number of chars read/written.
		 */
		protected int charCount;

		/**
		 * Close flag.
		 */
		protected boolean closed;

		/**
		 * The line number.
		 */
		protected int lineno = 1;

		/**
		 * The scanner.
		 */
		protected Scanner scanner;

		protected FileNode(VM vm, String name) {
			super(vm);
			this.name = name;
		}

		protected FileNode(VM vm, FileNode parent, URL base, String name) {
			this(vm, name);
			this.parent = parent.file;
		}

		protected File createFile(String name) {
			File file = new File(name);
			if (!file.isAbsolute()) {
				String parentName = parent == null ? null : parent.getParent();
				file = new File(parentName, name);
			}
			return file;
		}

		protected int getFilePosition() {
			return charCount;
		}

		protected boolean isClosed() {
			return closed;
		}

		protected abstract int getchar() throws IOException;

		protected abstract void putchar(int c) throws IOException;

		protected abstract void ungetchar(int c) throws IOException;

		protected abstract int rawRead() throws IOException;

		protected abstract void rawWrite(int c) throws IOException;

		protected abstract boolean isReadMode();

		protected abstract boolean isWriteMode();

		protected abstract void flush() throws IOException;

		protected abstract int bytesavailable() throws IOException;

		protected abstract void setFilePosition(int pos) throws IOException;

		protected abstract void resetfile(URL base) throws IOException;

		protected abstract boolean read(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException;

		protected abstract void write(StringType s) throws IOException;

		protected abstract boolean readhex(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException;

		protected abstract void writehex(StringType s) throws IOException;

		protected abstract boolean readline(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException;

		protected abstract void close() throws IOException;

		protected abstract void print(StringType string) throws IOException;

	}

	static class ReadFileNode extends FileNode {

		/**
		 * The input stream.
		 */
		private InputStream istream;

		/**
		 * The push back flag.
		 */
		private boolean pushed;

		/**
		 * The push back character.
		 */
		protected int pushedchar;

		ReadFileNode(VM vm, String name) {
			super(vm, name);
		}

		ReadFileNode(VM vm, FileNode parent, URL base, String name) {
			super(vm, parent, base, name);
			try {
				openForRead(base);
			} catch (IOException ex) {
				throw new Stop(IOERROR, "open " + name + " " + ex);
			}
		}

		ReadFileNode(VM vm, String name, InputStream istream) {
			this(vm, name);
			this.istream = istream;
		}

		private void openForRead(URL base) throws IOException {
			InputStream in = null;
			try {
				// open as resource from jar file
				in = getClass().getClassLoader().getResource(name).openStream();
			} catch (Exception ex) {
				try {
					// open stream from URL
					in = new URL(base, name).openStream();
				} catch (Exception ex1) {}
				if (in == null) {
					try {
						// open from archive
						in = new Archive(name).openStream();
					} catch (Exception ex2) {
						try {
							// open as file
							file = createFile(name);
							in = new FileInputStream(file);
						} catch (Exception ex3) {}
					}
				}
			}
			if (in == null) {
				throw new Stop(UNDEFINEDFILENAME, parent + ", " + name);
			}
			istream = new BufferedInputStream(in);
		}

		protected int getchar() throws IOException {
			if (closed)
				return -1;
			charCount++;
			if (pushed) {
				pushed = false;
				return pushedchar;
			}
			int c = rawRead();
			switch (c) {
			case '\r':
				lineno++;
				// fall through
			case '\n':
				break;
			}
			return c;
		}

		protected void putchar(int c) throws IOException {
			throw new IOException();
		}

		protected void ungetchar(int c) throws IOException {
			pushed = true;
			pushedchar = c;
		}

		protected boolean isReadMode() {
			return true;
		}

		protected boolean isWriteMode() {
			return false;
		}

		protected int rawRead() throws IOException {
			return istream.read();
		}

		protected void rawWrite(int c) throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

		protected void flush() throws IOException {
			int i, n, c = 0;
			while ((n = bytesavailable()) > 0) {
				for (i = 0; i < n && c >= 0; i++) {
					c = getchar();
				}
			}
		}

		protected int bytesavailable() throws IOException {
			int n = istream.available();
			return n == 0 ? -1 : n;
		}

		protected void setFilePosition(int pos) throws IOException {
			resetfile(null);
			if (pos > 0) {
				istream.skip(pos);
				charCount = pos;
			}
			pushed = false;
		}

		protected void resetfile(URL base) throws IOException {
			istream.close();
			openForRead(base);
			pushed = false;
		    charCount = 0;
		}

		protected boolean read(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException
		{
			int i = 0, c = 0, len = s.length();
			if (len <= 0)
				throw new Stop(RANGECHECK);
			while (c >= 0 && i < len) {
				if ((c = getchar()) >= 0) {
					s.put(vm, i++, c);
				}
			}
			result[0] = new StringType(s, 0, i);
			return c >= 0;
		}

		protected void write(StringType s) throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

		protected boolean readhex(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException
		{
			int buffer[] = new int[2];
			boolean eof = false;
			int i = 0, j = 0, k = 1, c, len = s.length();
			if (len > 0) {
				while (!eof && i < len) {
					int cc = getchar();
					if (cc >= 0) {
						if ((c = Scanner.hexValue(cc)) >= 0) {
							k = j++ % 2;
							buffer[k] = c;
							if (k != 0) {
								s.put(vm, i++, 16 * buffer[0] + buffer[1]);
							}
						}
					} else {
						eof = true;
					}
				}
				if (k == 0) {
					s.put(vm, i++, 16 * buffer[0]);
				}
			}
			result[0] = new StringType(s, 0, i);
			return !eof;
		}

		protected void writehex(StringType s) throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

		protected boolean readline(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException
		{
			int i = 0, c = 0, len = s.length();
			boolean eol = false;
			while (c >= 0 && !eol) {
				if (i >= len)
					throw new Stop(RANGECHECK);
				if ((c = getchar()) >= 0) {
					if (c != '\r' && c != '\n') {
						s.put(vm, i++, c);
					} else {
						eol = true;
					}
				}
			}
			s.setLineNo(lineno);
			result[0] = new StringType(s, 0, i);
			return c >= 0;
		}

		protected void close() throws IOException {
			istream.close();
			closed = true;
		}

		protected void print(StringType string) throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

	}

	static class WriteFileNode extends FileNode {

		/**
		 * The output stream.
		 */
		private OutputStream ostream;

		WriteFileNode(VM vm, String name) {
			super(vm, name);
		}

		WriteFileNode(VM vm, FileNode parent, URL base, String name) {
			super(vm, parent, base, name);
			try {
				openForWrite(base);
			} catch (IOException ex) {
				throw new Stop(IOERROR, "open " + name + " " + ex);
			}
		}

		WriteFileNode(VM vm, String name, OutputStream ostream) {
			this(vm, name);
			this.ostream = new BufferedOutputStream(ostream);
		}

		private void openForWrite(URL base) throws IOException {
			try {
				OutputStream out;
				if (base == null) {
					file = createFile(name);
					out = new FileOutputStream(file);
				} else {
					URL url = new URL(base, name);
					URLConnection connection = url.openConnection();
					connection.setDoOutput(true);
					out = connection.getOutputStream();
				}
				ostream = new BufferedOutputStream(out);
			} catch (IOException ex) {
				throw new Stop(UNDEFINEDFILENAME, ex.getMessage());
			} catch (SecurityException ex) {
				throw new Stop(SECURITYCHECK, ex.getMessage());
			}
		}

		protected boolean isReadMode() {
			return false;
		}

		protected boolean isWriteMode() {
			return true;
		}

		protected int getchar() throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

		protected void putchar(int c) throws IOException {
			charCount++;
			rawWrite(c);
		}

		protected void ungetchar(int c) throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

		protected int rawRead() throws IOException {
			throw new Stop(INVALIDFILEACCESS);
		}

		protected void rawWrite(int c) throws IOException {
			ostream.write(c);
		}

		protected void flush() throws IOException {
			ostream.flush();
		}

		protected int bytesavailable() throws IOException {
			return -1;
		}

		protected void setFilePosition(int pos) throws IOException {
			ostream.flush();
			// TODO: support setFilePosition
			throw new Stop(INTERNALERROR, "not supported");
		}

		protected void resetfile(URL base) throws IOException {
			ostream.close();
			openForWrite(base);
		    charCount = 0;
		}

		protected boolean read(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException
		{
			throw new Stop(INVALIDFILEACCESS);
		}

		protected void write(StringType s) throws IOException {
			int len = s.length();
			for (int i = 0; i < len; i++) {
				putchar(s.get(i));
			}
		}

		protected boolean readhex(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException
		{
			throw new Stop(INVALIDFILEACCESS);
		}

		protected void writehex(StringType s) throws IOException {
			int c, c0, c1, len = s.length();
			for (int i = 0; i < len; i++) {
				c = s.get(i);
				c0 = c / 16;
				c1 = c % 16;
				putchar(c0 >= 0 && c0 <= 9 ? c0 + '0' : c0 + 'a');
				putchar(c1 >= 0 && c1 <= 9 ? c1 + '0' : c1 + 'a');
			}
		}

		protected boolean readline(VM vm, StringType s, StringType result[/* 1 */])
			throws IOException
		{
			throw new Stop(INVALIDFILEACCESS);
		}

		protected void close() throws IOException {
			ostream.flush();
			ostream.close();
			closed = true;
		}

		protected void print(StringType string) throws IOException {
			String s = string.toString();
			for (int i = 0; i < s.length(); i++) {
				putchar(s.charAt(i));
			}
		}

	}

}
