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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

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
	File tmpdir;
	Store mbox;

	public PstStore(Session session, URLName urlname) throws MessagingException, IOException {
		super(session, urlname);
	}

	@Override
    protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
		try {
			tmpdir = File.createTempFile("readpst", null);
			tmpdir.delete();
			tmpdir.mkdir();
			String prog = session.getProperties().getProperty("xena.util.pst.bin");

			// Check that we have a valid location for the readpst executable
			if (prog == null || prog.equals("")) {
				throw new MessagingException("Cannot find the readpst executable. Please check its location in the email plugin settings.");
			}

			List<String> args = new ArrayList<String>();
			args.add(prog);
			args.add("-r");
			args.add("-w");
			args.add("-o");
			args.add(tmpdir.toString());

			String nf = URLDecoder.decode(url.getFile(), "US-ASCII");
			URI uri = new URI("file", null, "/" + nf, null);
			File file = new File(uri);

			args.add(file.toString());
			String[] arga = new String[args.size()];
			args.toArray(arga);
			Process pr = Runtime.getRuntime().exec(arga);

			final InputStream eis = pr.getErrorStream();
			final InputStream ois = pr.getInputStream();
			Thread et = new Thread() {
				@Override
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
				@Override
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

	@Override
    public synchronized void close() throws javax.mail.MessagingException {
		mbox.close();
		if (tmpdir != null) {
			doDel(tmpdir);
		}
	}

	@Override
    protected void finalize() throws java.lang.Throwable {
		close();
		super.finalize();
	}

	@Override
    public Folder getFolder(String parm1) throws javax.mail.MessagingException {
		return mbox.getFolder(parm1);
	}

	@Override
    public Folder getDefaultFolder() throws javax.mail.MessagingException {
		return mbox.getDefaultFolder();
	}

	@Override
    public Folder[] getSharedNamespaces() throws javax.mail.MessagingException {
		return mbox.getSharedNamespaces();
	}

	@Override
    public Folder[] getUserNamespaces(String parm1) throws javax.mail.MessagingException {
		return mbox.getUserNamespaces(parm1);
	}

	@Override
    public Folder[] getPersonalNamespaces() throws javax.mail.MessagingException {
		return mbox.getPersonalNamespaces();
	}

	@Override
    public Folder getFolder(URLName parm1) throws javax.mail.MessagingException {
		return mbox.getFolder(parm1);
	}

	@Override
    public String toString() {
		return tmpdir.toString() + " " + super.toString();
	}
}
