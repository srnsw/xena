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

package au.gov.naa.digipres.xena.plugin.email.msg;

import java.io.InputStream;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;

import au.gov.naa.digipres.xena.javatools.FileName;

class MsgFolder extends Folder {
	InputStream inputStream;

	public MsgFolder(Store store, InputStream inputStream) {
		super(store);
		this.inputStream = inputStream;
	}

	@Override
    public boolean exists() throws javax.mail.MessagingException {
		// return file.isFile();
		return true;
	}

	@Override
    public Message getMessage(int msgnum) throws javax.mail.MessagingException {
		if (msgnum != 1) {
			throw new IndexOutOfBoundsException("Bad Message Number: " + msgnum);
		}
		return new MsgMessage(this, msgnum, inputStream);
	}

	/*
	 * public File getTop() { MsgStore st = (MsgStore)store; return MsgStore.urlToFile(st.urlName); // return new
	 * File(st.urlName.getFile()); }
	 */

	static final char SEPARATOR = FileName.STANDARDSEP;

	@Override
    public String getName() {
		String fn = getFullName();
		if (fn.charAt(fn.length() - 1) == SEPARATOR) {
			fn = fn.substring(0, fn.length() - 2);
		}
		int i = fn.lastIndexOf('/');
		if (0 <= i) {
			fn = fn.substring(i + 1);
		}
		return fn;
	}

	@Override
    public int getMessageCount() throws javax.mail.MessagingException {
		return 1;
	}

	@Override
    public boolean delete(boolean parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method delete() not yet implemented.");
	}

	@Override
    public void appendMessages(Message[] parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method appendMessages() not yet implemented.");
	}

	@Override
    public void close(boolean parm1) throws javax.mail.MessagingException {
		// file = null;
	}

	@Override
    public void open(int parm1) throws javax.mail.MessagingException {
	}

	@Override
    public Message[] expunge() throws javax.mail.MessagingException {
		return new Message[0];
	}

	@Override
    public Folder getFolder(String name) throws javax.mail.MessagingException {
		return null;
	}

	@Override
    public int getType() throws javax.mail.MessagingException {
		return HOLDS_FOLDERS | HOLDS_MESSAGES;
	}

	@Override
    public boolean hasNewMessages() throws javax.mail.MessagingException {
		return false;
	}

	@Override
    public boolean create(int parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method create() not yet implemented.");
	}

	@Override
    public boolean isOpen() {
		return true;
	}

	@Override
    public char getSeparator() throws javax.mail.MessagingException {
		return SEPARATOR;
	}

	@Override
    public Folder getParent() throws javax.mail.MessagingException {
		String n = getFullName();
		int i = n.lastIndexOf(SEPARATOR);
		if (0 < i) {
			n = n.substring(0, i - 1);
			return store.getFolder(n);
		} else {
			return null;
		}
	}

	@Override
    public String getFullName() {
		/*
		 * try { if (file != null) { return javatools.util.FileName.relativeTo(getTop(), file); } } catch (IOException
		 * x) { x.printStackTrace(); return null; }
		 */
		return "";
	}

	@Override
    public Flags getPermanentFlags() {
		return new Flags();
	}

	@Override
    public boolean renameTo(Folder parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method renameTo() not yet implemented.");
	}

	@Override
    public Folder[] list(String parm1) throws javax.mail.MessagingException {
		return new Folder[0];
	}
}
