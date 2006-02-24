package au.gov.naa.digipres.xena.plugin.email.msg;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class MsgStore extends Store {
	URLName urlName;

	InputStream inputStream;

	public MsgStore(Session session, URLName urlName) throws MessagingException, IOException {
		super(session, urlName);
		// While this seems a duplicate of the Store one,
		// the Store one does wierd stuff, so we have our own copy.
		this.urlName = urlName;
	}

	public static void main(String[] argv) throws Exception {
	}

	protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
		return true;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public synchronized void close() throws javax.mail.MessagingException {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException x) {
				throw new MessagingException("Close failed", x);
			}
			inputStream = null;
		}
	}

	protected void finalize() throws java.lang.Throwable {
		close();
		super.finalize();
	}

	public Folder getFolder(String name) throws javax.mail.MessagingException {
		if (name.equals("")) {
			return getDefaultFolder();
		} else {
			return null;
		}
	}

	public Folder getDefaultFolder() throws javax.mail.MessagingException {
		if (inputStream == null) {

			File file = urlToFile(urlName);
			if (file != null) {
				URI uri = file.toURI();
				file = new File(uri);
			}
//			URI uri = new URI("file:///" + urlName.getFile());
//			System.out.println(new File(uri).toString());
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException x) {
				throw new MessagingException("File Not Found", x);
			}
		}
		return new MsgFolder(this, inputStream);
	}

	public Folder[] getSharedNamespaces() throws javax.mail.MessagingException {
		Folder[] rtn = new Folder[1];
		rtn[0] = getDefaultFolder();
		return rtn;
	}

	static File urlToFile(URLName urln) {
//		String nf = "non-file";
		String nf = null;
		try {
			if (urln.getFile() == null) {
				return null;
			} else {
				nf = URLDecoder.decode(urln.getFile(), "US-ASCII");
			}
		} catch (UnsupportedEncodingException x) {
			x.printStackTrace();
			return null;
		}
		URI uri = null;
		try {
			uri = new URI("file", null, "/" + nf, null);
		} catch (URISyntaxException x) {
			x.printStackTrace();
			return null;
		}
		return new File(uri);

	}

	public Folder[] getUserNamespaces(String parm1) throws javax.mail.MessagingException {
		return new Folder[0];
	}

	public Folder[] getPersonalNamespaces() throws javax.mail.MessagingException {
		return new Folder[0];
	}

	public Folder getFolder(URLName url) throws javax.mail.MessagingException {
		return getFolder(urlToFile(urlName).toString());
	}

	String nameOfAttachment(String tok) {
		int ind = tok.indexOf('=');
		if (0 < ind) {
			return tok.substring(0, ind);
		} else {
			return null;
		}
	}

	String valueOfAttachment(String tok) {
		int ind = tok.indexOf('=');
		if (0 < ind) {
			String s = tok.substring(ind + 1);
			if (s.charAt(0) == '"') {
				s = s.substring(1, s.length() - 1);
			}
			return s;
		} else {
			return null;
		}
	}
}
