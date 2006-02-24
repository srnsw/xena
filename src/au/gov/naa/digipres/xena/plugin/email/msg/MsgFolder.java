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

	public boolean exists() throws javax.mail.MessagingException {
//		return file.isFile();
		return true;
	}

	public Message getMessage(int msgnum) throws javax.mail.MessagingException {
		if (msgnum != 1) {
			throw new IndexOutOfBoundsException("Bad Message Number: " + msgnum);
		}
		return new MsgMessage(this, msgnum, inputStream);
	}

/*	public File getTop() {
		MsgStore st = (MsgStore)store;
		return MsgStore.urlToFile(st.urlName);
//		return new File(st.urlName.getFile());
	} */

	static final char SEPARATOR = FileName.STANDARDSEP;

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

	public int getMessageCount() throws javax.mail.MessagingException {
		return 1;
	}

	public boolean delete(boolean parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method delete() not yet implemented.");
	}

	public void appendMessages(Message[] parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method appendMessages() not yet implemented.");
	}

	public void close(boolean parm1) throws javax.mail.MessagingException {
//		file = null;
	}

	public void open(int parm1) throws javax.mail.MessagingException {
	}

	public Message[] expunge() throws javax.mail.MessagingException {
		return new Message[0];
	}

	public Folder getFolder(String name) throws javax.mail.MessagingException {
		return null;
	}

	public int getType() throws javax.mail.MessagingException {
		return HOLDS_FOLDERS | HOLDS_MESSAGES;
	}

	public boolean hasNewMessages() throws javax.mail.MessagingException {
		return false;
	}

	public boolean create(int parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method create() not yet implemented.");
	}

	public boolean isOpen() {
		return true;
	}

	public char getSeparator() throws javax.mail.MessagingException {
		return SEPARATOR;
	}

	public Folder getParent() throws javax.mail.MessagingException {
		String n = getFullName();
		int i = n.lastIndexOf(SEPARATOR);
		if (0 < i) {
			n = n.substring(0, i-1);
			return store.getFolder(n);
		} else {
			return null;
		}
	}

	public String getFullName() {
/*		try {
			if (file != null) {
				return javatools.util.FileName.relativeTo(getTop(), file);
			}
		} catch (IOException x) {
			x.printStackTrace();
			return null;
		}  */
		return "";
	}

	public Flags getPermanentFlags() {
		return new Flags();
	}

	public boolean renameTo(Folder parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method renameTo() not yet implemented.");
	}

	public Folder[] list(String parm1) throws javax.mail.MessagingException {
		return new Folder[0];
	}
}
