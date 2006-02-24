package au.gov.naa.digipres.xena.plugin.email.trim;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;

import au.gov.naa.digipres.xena.javatools.FileName;

class TrimFolder extends Folder {

	File dir;

	File[] files;

	File[] folderdirs;

	Folder[] folders;

	public TrimFolder(Store store, File dir) {
		super(store);
		this.dir = dir;
	}

	public boolean exists() throws javax.mail.MessagingException {
//		return dir.isDirectory();
		return dir.exists();
	}

	public Message getMessage(int msgnum) throws javax.mail.MessagingException {
		if (msgnum <= 0 || files.length < msgnum) {
			throw new IndexOutOfBoundsException("Bad Message Number: " + msgnum);
		}
		return new TrimMessage(this, msgnum, files[msgnum - 1]);
	}

	public File getTop() {
		TrimStore st = (TrimStore)store;
		return TrimStore.urlToFile(st.urlName);
	}

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
		return files.length;
	}

	public boolean delete(boolean parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method delete() not yet implemented.");
	}

	public void appendMessages(Message[] parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method appendMessages() not yet implemented.");
	}

	public void close(boolean parm1) throws javax.mail.MessagingException {
		files = null;
	}

	public void open(int parm1) throws javax.mail.MessagingException {
		if (dir.isDirectory()) {
			files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".mbx");
				}
			});
			folderdirs = dir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory();
				}
			});
			folders = new Folder[folderdirs.length];
			for (int i = 0; i < folderdirs.length; i++) {
				folders[i] = new TrimFolder(store, folderdirs[i]);
			}
		} else {
			files = new File[] { dir };
			folderdirs = new File[0];
			folders = new Folder[0];
//			folders[0] = new TrimFolder(store, null);
		}
	}

	public Message[] expunge() throws javax.mail.MessagingException {
		return new Message[0];
	}

	public Folder getFolder(String name) throws javax.mail.MessagingException {
		for (int i = 0; i < folders.length; i++) {
			TrimFolder tf = (TrimFolder)folders[i];
			if (tf.getFullName().equals(name)) {
				return tf;
			}
		}
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
		return files != null;
	}

	public char getSeparator() throws javax.mail.MessagingException {
		return SEPARATOR;
	}

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

	public String getFullName() {
		try {
			return FileName.relativeTo(getTop(), dir);
		} catch (IOException x) {
			x.printStackTrace();
			return null;
		}
	}

	public Flags getPermanentFlags() {
		return new Flags();
	}

	public boolean renameTo(Folder parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method renameTo() not yet implemented.");
	}

	public Folder[] list(String parm1) throws javax.mail.MessagingException {
		return folders;
	}
}
