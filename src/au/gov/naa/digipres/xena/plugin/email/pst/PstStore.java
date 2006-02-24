package au.gov.naa.digipres.xena.plugin.email.pst;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class PstStore extends Store {
// ./readpst -o out -r -w  test1.pst
	File tmpdir;

	Store mbox;

	public PstStore(Session session, URLName urlname) throws MessagingException, IOException {
		super(session, urlname);
//		System.out.println("open: " + "@" + Integer.toHexString(hashCode()) + urlname);
		//	this.urln = urlname;

	}

	public static void main(String[] argv) throws Exception {
//		ar[0] = "c:\\Program Files\\OpenOffice.org1.1.0\\program\\soffice";
//		ar[0] = "c:/Program Files/OpenOffice.org1.1.0\\program\\soffice.exe";
		List args = new ArrayList();
//		args.add("c:/Documents and Settings/chrisbit/readpst");
		args.add("c:/tmp/tmp/readpst");
		args.add("-r");
		args.add("-w");
		args.add("-o");
		args.add("c:/tmp/tmp");
		args.add("c:/tmp/tmp/test1.pst");
		String[] arga = new String[args.size()];
		args.toArray(arga);
		Process pr = Runtime.getRuntime().exec(arga, new String[] { }, new File("c:/tmp/tmp"));
		System.out.println(pr.waitFor());
		pr = Runtime.getRuntime().exec(new String[] { "c:/cygwin/bin/touch", "foo" }, new String[] { }, new File("c:/tmp/tmp"));
		System.out.println(pr.waitFor());
		/*		Properties props = new Properties();
		  Session session = Session.getInstance(props);
		  PstStore store = new PstStore(session, new URLName("file:///c:/cvs/xenaplugin/email/test/file/test1.pst"));
		  store.connect(); */
	}

	protected boolean protocolConnect(String host, int port, String user, String password) throws javax.mail.MessagingException {
		try {
			tmpdir = File.createTempFile("readpst", null);
			tmpdir.delete();
			tmpdir.mkdir();
//			System.out.println("connect: " + "@" + Integer.toHexString(hashCode()) + tmpdir + " " + this + " " + mbox);
			String bins = session.getProperties().getProperty("xena.util.pst.bin");
			String prog = "readpst";
			if (bins != null) {
				File fprog = new File(bins, prog);
				prog = fprog.toString();
			}

			List args = new ArrayList();
//			args.add("c:\\Program Files\\OpenOffice.org\\1.1.0\\program\\soffice.exe");
			args.add(prog);
//			args.add("c:/tmp/tmp/readpst.exe");
			args.add("-r");
			args.add("-w");
			args.add("-o");
			args.add(tmpdir.toString());
//			args.add("c:/tmp/tmp");


//			URL myurl = new URL("file", null, "/" + url.getFile());
			String nf = URLDecoder.decode(url.getFile(), "US-ASCII");
			URI uri = new URI("file" , null, "/" + nf, null);
			File file = new File(uri);

			args.add(file.toString());
//			args.add("c:/tmp/tmp/test1.pst");
			String[] arga = new String[args.size()];
			args.toArray(arga);
/*			for (int i = 0; i < arga.length; i++) {
				System.out.print(arga[i] + " ");
			}
			System.out.println(); */
			Process pr = Runtime.getRuntime().exec(arga);

			final InputStream eis = pr.getErrorStream();
			final InputStream ois = pr.getInputStream();
//			ByteArrayOutputStream err = new ByteArrayOutputStream();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Thread et = new Thread() {
				public void run() {
					try {
						int c;
						while (0 <= (c = eis.read())) {
						}
					} catch (IOException x) {
						// Nothing
					}
				}
			};
			et.start();
			Thread ot = new Thread() {
				public void run() {
					int c;
					try {
						while (0 <= (c = ois.read())) {
						}
					} catch (IOException x) {
						// Nothing
					}
				}
			};
			ot.start();

			pr.waitFor();
			mbox = session.getStore(new URLName("mbox", null, -1, tmpdir.toURL().getPath(), null, null));
			mbox.connect(host, port, user, password);
			return true;
		} catch (IOException x) {
			throw new MessagingException("connect: ", x);
		} catch (InterruptedException x) {
			throw new MessagingException("connect: ", x);
		} catch (URISyntaxException x) {
			throw new MessagingException("connect: ", x);
		}
	}

	protected void doDel(File f) {
		if (f.isDirectory()) {
			File[] ls = f.listFiles();
			for (int i = 0; i < ls.length; i++) {
				doDel(ls[i]);
			}
			f.delete();
		} else {
			f.delete();
		}
	}

	public synchronized void close() throws javax.mail.MessagingException {
//		System.out.println("close: " + "@" + Integer.toHexString(hashCode()) + tmpdir + " " + this + " " + mbox);
		mbox.close();
		if (tmpdir != null) {
			doDel(tmpdir);
		}
	}

	protected void finalize() throws java.lang.Throwable {
		close();
		super.finalize();
	}

	public Folder getFolder(String parm1) throws javax.mail.MessagingException {
		return mbox.getFolder(parm1);
	}

	public Folder getDefaultFolder() throws javax.mail.MessagingException {
		return mbox.getDefaultFolder();
	}

	public Folder[] getSharedNamespaces() throws javax.mail.MessagingException {
		return mbox.getSharedNamespaces();
	}

	public Folder[] getUserNamespaces(String parm1) throws javax.mail.MessagingException {
		return mbox.getUserNamespaces(parm1);
	}

	public Folder[] getPersonalNamespaces() throws javax.mail.MessagingException {
		return mbox.getPersonalNamespaces();
	}

	public Folder getFolder(URLName parm1) throws javax.mail.MessagingException {
		return mbox.getFolder(parm1);
	}
	public String toString() {
		return tmpdir.toString() + " " + super.toString();
	}
}
