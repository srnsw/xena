package au.gov.naa.digipres.xena.plugin.email.trim;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class TrimStore extends Store {
	URLName urlName;

	public TrimStore(Session session, URLName urlName) throws MessagingException, IOException {
		super(session, urlName);
		this.urlName = urlName;
	}

	public static void main(String[] argv) throws Exception {
	}

	protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
		return true;
	}

	public synchronized void close() throws javax.mail.MessagingException {
	}

	protected void finalize() throws java.lang.Throwable {
		close();
		super.finalize();
	}

	public Folder getFolder(String name) throws javax.mail.MessagingException {
		File ndir = new File(urlToFile(url), name);
		return new TrimFolder(this, ndir);
	}

	public Folder getDefaultFolder() throws javax.mail.MessagingException {
		return new TrimFolder(this, urlToFile(url));
	}

	public Folder[] getSharedNamespaces() throws javax.mail.MessagingException {
		Folder[] rtn = new Folder[1];
		rtn[0] = getDefaultFolder();
		return rtn;
	}

	public Folder[] getUserNamespaces(String parm1) throws javax.mail.MessagingException {
		return new Folder[0];
	}

	public Folder[] getPersonalNamespaces() throws javax.mail.MessagingException {
		return new Folder[0];
	}

	public Folder getFolder(URLName url) throws javax.mail.MessagingException {
		return getFolder(urlToFile(url).toString());
	}

	static File urlToFile(URLName urln) {
		String nf = null;
		try {
			nf = URLDecoder.decode(urln.getFile(), "US-ASCII");
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
