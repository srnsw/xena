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

package au.gov.naa.digipres.xena.plugin.email.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.javatools.IteratorToEnumeration;

class MsgMessage extends Message {
	byte[] buffer = new byte[4096];

	protected File file;

	protected byte[] body;

	protected byte[] htmlbody;

	protected List headers = new ArrayList();

	protected int nlines = 0;

	protected List attachments = new ArrayList();

	protected Date date;

	public MsgMessage() {
	}

	public MsgMessage(Folder folder, int msgnum, InputStream inputStream) throws MessagingException {
		super(folder, msgnum);
		this.file = file;

		POIFSFileSystem fs = null;
		// InputStream inputStream = null;
		try {
			// inputStream = new FileInputStream(file);
			fs = new POIFSFileSystem(inputStream);
			DirectoryEntry root = fs.getRoot();
			// printTree(root, 0);
			doRoot(root);
		} catch (IOException x) {
			// an I/O error occurred, or the InputStream did not provide a compatible
			// POIFS data structure
			throw new MessagingException("Can't open OLE container", x);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException x) {
					x.printStackTrace();
				}
			}
		}
	}

	byte[] getData(DocumentEntry entry) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = new DocumentInputStream(entry);
		int c;
		while (0 <= (c = is.read(buffer))) {
			baos.write(buffer, 0, c);
		}
		return baos.toByteArray();
	}

	String getString(DocumentEntry entry) throws IOException {
		byte[] data = getData(entry);
		StringBuffer buf = new StringBuffer();
		String res = new String(data);
		for (int i = 0; i < res.length(); i++) {
			if (res.charAt(i) != 0) {
				buf.append(res.charAt(i));
			}
		}
		return buf.toString();
	}

	public void doRoot(DirectoryEntry root) throws IOException, MessagingException {
		Map nameMap = new HashMap();
		String from = null;
		String to = null;
		String cc = null;
		String bcc = null;
		String replyTo = null;
		String fromAddress = null;

		for (Iterator iter = root.getEntries(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			if (entry.getName().equals("__nameid_version1.0") || entry.getName().equals("__properties_version1.0")) {
				// skip
			} else if (entry instanceof DirectoryEntry) {
				if (entry.getName().startsWith("__recip_version1.0_#")) {
					String name = null;
					String address = null;
					for (Iterator iter2 = ((DirectoryEntry) entry).getEntries(); iter2.hasNext();) {
						Entry entry2 = (Entry) iter2.next();
						ItemName itemName = parseItemName(entry2);
						if (itemName != null) {
							if (itemName.property == 0x3001) { // content
								name = stripQuotes(getString((DocumentEntry) entry2));
							} else if (itemName.property == 0x39FE) {
								address = getString((DocumentEntry) entry2);
							} else if (itemName.property == 0x3003) {
								if (address == null) {
									address = getString((DocumentEntry) entry2);
								}
							}
						}
					}
					nameMap.put(name, address);
				} else if (entry.getName().startsWith("__attach_version1.0_#")) {
					MsgAttachment att = new MsgAttachment();
					attachments.add(att);
					for (Iterator iter2 = ((DirectoryEntry) entry).getEntries(); iter2.hasNext();) {
						Entry entry2 = (Entry) iter2.next();
						ItemName itemName = parseItemName(entry2);
						if (itemName != null) {
							if (itemName.property == 0x3701) { // content
								if (entry2 instanceof DocumentEntry) {
									att.setBytes(getData((DocumentEntry) entry2));
								} else {
									MsgMessage msg = new MsgMessage();
									msg.doRoot((DirectoryEntry) entry2);
									att.setContent(msg, null);
									// printTree((DirectoryEntry)entry2, 0);
								}
							} else {
								switch (itemName.property) {
								case 0x3001: // Short file name
									att.setName(getString((DocumentEntry) entry2));
									break;
								case 0x3707: // Long file name
									break;
								case 0x370E: // Mime Type
									break;
								case 0x3716: // Disposition
									break;
								}
							}
						}
					}
				} else {
					throw new MessagingException("Unexpected:1");
				}
			} else if (entry instanceof DocumentEntry) {
				ItemName itemName = parseItemName(entry);
				if (itemName != null) {
					if (itemName.property == 0x1000) {
						// body = getData((DocumentEntry)entry);
						body = getString((DocumentEntry) entry).getBytes();
					} else if (itemName.property == 0x1013) {
						htmlbody = getData((DocumentEntry) entry);
					} else if (itemName.property == 0x300B) {
					} else {
						String data = getString((DocumentEntry) entry);
						// Other bits are small enough to always store directly.
						switch (itemName.property) {
						case 0x0037: // Subject
							headers.add(new Header("Subject", data));
							break;
						case 0x0C1A: // Reply To Name
							if (!data.equals("")) {
								replyTo = data;
							}
							break;
						case 0x0C1F: //
							if (!data.equals("")) {
								fromAddress = data;
							}
							break;
						case 0x0042: // From Address Type
							if (!data.equals("")) {
								from = data;
								if (fromAddress != null) {
									nameMap.put(from, fromAddress);
									fromAddress = null;
								}
							}
							break;
						case 0x0E04: // To Names

							if (!data.equals("")) {
								to = data;
							}
							break;
						case 0x0E02: // BCC Names
							if (!data.equals("")) {
								bcc = data;
							}
							break;
						case 0x0E03: // CC Names
							if (!data.equals("")) {
								cc = data;
							}
							break;
						case 0x1035: // Message Id
							headers.add(new Header("Message-ID", data));
							break;
						case 0x0047: // Date
							Pattern pat = Pattern.compile(".*[^0-9]([0-9]{12})[^0-9].*");
							Matcher mat = pat.matcher(data);
							if (mat.matches()) {
								String sdate = mat.group(1);
								SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
								try {
									date = sdf.parse(sdate);
								} catch (ParseException x) {
									x.printStackTrace();
								}
								sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
								String stddate = sdf.format(date);
								headers.add(new Header("Date", stddate));
							}
							break;
						}
					}
				}
			} else {
				// currently, either an Entry is a DirectoryEntry or a DocumentEntry,
				// but in the future, there may be other entry subinterfaces. The
				// internal data structure certainly allows for a lot more entry types.
				throw new MessagingException("Unexpected:2");
			}
		}
		/*
		 * String address = (String)nameMap.get(from); if (from == null) { throw new MessagingException("From field not
		 * found"); } if (address == null) { throw new MessagingException("Address for: " + from + " not found"); }
		 * headers.add(new Header("From", new InternetAddress(address, from).toString()));
		 */
		if (from != null) {
			headers.add(new Header("From", namesToInet(nameMap, from)));
		}
		if (to != null) {
			headers.add(new Header("To", namesToInet(nameMap, to)));
		}
		if (cc != null) {
			headers.add(new Header("CC", namesToInet(nameMap, cc)));
		}
		if (bcc != null) {
			headers.add(new Header("BCC", namesToInet(nameMap, bcc)));
		}
		if (replyTo != null) {
			headers.add(new Header("Reply-To", namesToInet(nameMap, replyTo)));
		}
	}

	String stripQuotes(String name) {
		if (name.charAt(0) == '\'') {
			name = name.substring(1);
		}
		if (name.charAt(name.length() - 1) == '\'') {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}

	String namesToInet(Map nameMap, String names) throws UnsupportedEncodingException {
		StringBuffer toStr = new StringBuffer();
		StringTokenizer st = new StringTokenizer(names, ";");
		for (int i = 0; st.hasMoreTokens(); i++) {
			String one = stripQuotes(st.nextToken().trim());
			String toAddress = (String) nameMap.get(one);
			if (i != 0) {
				toStr.append(", ");
			}
			if (toAddress == null) {
				toStr.append(one);
			} else {
				toStr.append(new InternetAddress(toAddress, one).toString());
			}
		}
		return toStr.toString();
	}

	@Override
    public Date getReceivedDate() {
		return null;
	}

	@Override
    public Date getSentDate() {
		return date;
	}

	public ItemName parseItemName(Entry entry) {
		String name = entry.getName();
		Pattern pat = Pattern.compile("^__substg1\\.0_(....)(....)$");
		Matcher mat = pat.matcher(entry.getName());
		if (!mat.matches() || mat.groupCount() != 2) {
			return null;
		}
		return new ItemName(Integer.parseInt(mat.group(1), 16), Integer.parseInt(mat.group(2), 16));
	}

	static class ItemName {
		public ItemName(int property, int encoding) {
			this.property = property;
			this.encoding = encoding;
		}

		int property;

		int encoding;
	}

	public static void main(String[] args) throws Exception {
		InputStream inputStream = new FileInputStream(args[0]);
		POIFSFileSystem fs = new POIFSFileSystem(inputStream);
		DirectoryEntry root = fs.getRoot();
		printTree(root, 0);
	}

	static public void printTree(DirectoryEntry root, int level) throws IOException {
		for (Iterator iter = root.getEntries(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			for (int i = 0; i < level; i++) {
				System.out.print("  ");
			}
			if (entry instanceof DirectoryEntry) {
				System.out.print("D: ");
				System.out.println("found entry: " + entry.getName());
				printTree((DirectoryEntry) entry, level + 1);
				// .. recurse into this directory
			} else if (entry instanceof DocumentEntry) {
				byte[] bytes = new byte[1024];
				InputStream is = new DocumentInputStream((DocumentEntry) entry);
				System.out.print(entry.getName() + ":");
				int c = is.read(bytes, 0, bytes.length);
				System.out.print("E: " + Integer.toHexString(bytes[0]) + " " + Integer.toHexString(bytes[1]) + " " + Integer.toHexString(bytes[15])
				                 + "     ");
				if (0 < c) {
					System.out.write(bytes, 0, c);
					System.out.println();
				}
				// entry is a document, which you can read
			}
		}

	}

	@Override
    public void setSentDate(Date parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setSentDate() not yet implemented.");
	}

	public void setDisposition(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDisposition() not yet implemented.");
	}

	public boolean isMimeType(String parm1) throws javax.mail.MessagingException {
		return false;
	}

	public DataHandler getDataHandler() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method getDataHandler() not yet implemented.");
	}

	public void setContent(Object parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setContent() not yet implemented.");
	}

	@Override
    public Address[] getFrom() throws javax.mail.MessagingException {
		String[] res = getHeader("From");
		return strToAdd(res);
	}

	public Enumeration getNonMatchingHeaders(String[] names) throws javax.mail.MessagingException {
		String[] lnames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			lnames[i] = names[i].toLowerCase();
		}
		List ls = new ArrayList();
		Iterator it = headers.iterator();
		while (it.hasNext()) {
			Header h = (Header) it.next();
			boolean match = false;
			for (int i = 0; i < lnames.length; i++) {
				if (h.getName().toLowerCase().equals(lnames[i])) {
					match = true;
					break;
				}
			}
			if (!match) {
				ls.add(h);
			}
		}
		return new IteratorToEnumeration(ls.iterator());
	}

	public Object getContent() throws java.io.IOException, javax.mail.MessagingException {
		if (body == null) {
			throw new MessagingException("Message body is empty. Perhaps this isn't really a MSG file?");
		}
		if (attachments.size() == 0) {
			return new ByteArrayInputStream(body);
		} else {
			MsgMultiPart mp = new MsgMultiPart();
			mp.addBodyPart(new MsgBody(body, nlines));
			Iterator it = attachments.iterator();
			while (it.hasNext()) {
				BodyPart att = (BodyPart) it.next();
				mp.addBodyPart(att);
			}
			return mp;
		}
	}

	@Override
    public void addRecipients(Message.RecipientType parm1, Address[] parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method addRecipients() not yet implemented.");
	}

	@Override
    public void saveChanges() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method saveChanges() not yet implemented.");
	}

	@Override
    public Flags getFlags() throws javax.mail.MessagingException {
		return new Flags();
	}

	public void setFileName(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFileName() not yet implemented.");
	}

	protected Address[] strToAdd(String[] res) {
		Address[] rtn = new Address[res.length];
		for (int i = 0; i < res.length; i++) {
			StringTokenizer st = new StringTokenizer(res[i], "[]:");
			String name = null;
			String protocol = null;
			String address = null;
			if (st.hasMoreTokens()) {
				name = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				protocol = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				address = st.nextToken();
			}
			if (protocol != null && protocol.toUpperCase().equals("SMTP")) {
				try {
					rtn[i] = new InternetAddress(address, name);
				} catch (UnsupportedEncodingException ex) {
					rtn[i] = new MsgAddress(res[i]);
				}
			} else {
				rtn[i] = new MsgAddress(res[i]);
			}
		}
		return rtn;
	}

	@Override
    public Address[] getRecipients(Message.RecipientType type) throws javax.mail.MessagingException {
		String[] res = null;
		if (type == Message.RecipientType.TO) {
			res = getHeader("To");
		} else if (type == Message.RecipientType.CC) {
			res = getHeader("Cc");
		} else if (type == Message.RecipientType.BCC) {
			res = getHeader("Bcc");
		}
		return strToAdd(res);
	}

	public int getSize() throws javax.mail.MessagingException {
		return body.length;
	}

	public String[] getHeader(String name) throws javax.mail.MessagingException {
		name = name.toLowerCase();
		List ls = new ArrayList();
		Iterator it = headers.iterator();
		while (it.hasNext()) {
			Header h = (Header) it.next();
			if (h.getName().toLowerCase().equals(name)) {
				ls.add(h.getValue());
			}
		}
		String[] rtn = new String[ls.size()];
		ls.toArray(rtn);
		return rtn;
	}

	@Override
    public Message reply(boolean parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method reply() not yet implemented.");
	}

	@Override
    public void setRecipients(Message.RecipientType parm1, Address[] parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setRecipients() not yet implemented.");
	}

	@Override
    public void addFrom(Address[] parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method addFrom() not yet implemented.");
	}

	@Override
    public void setFrom(Address parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFrom() not yet implemented.");
	}

	public Enumeration getMatchingHeaders(String[] names) throws javax.mail.MessagingException {
		String[] lnames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			lnames[i] = names[i].toLowerCase();
		}
		List ls = new ArrayList();
		Iterator it = headers.iterator();
		while (it.hasNext()) {
			Header h = (Header) it.next();
			for (int i = 0; i < lnames.length; i++) {
				if (h.getName().toLowerCase().equals(lnames[i])) {
					ls.add(h);
					break;
				}
			}
		}
		return new IteratorToEnumeration(ls.iterator());
	}

	@Override
    public void setFlags(Flags parm1, boolean parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFlags() not yet implemented.");
	}

	@Override
    public String getSubject() throws javax.mail.MessagingException {
		String[] v = getHeader("Subject");
		if (v.length <= 0) {
			return null;
		}
		return v[0];
	}

	public Enumeration getAllHeaders() throws javax.mail.MessagingException {
		return new IteratorToEnumeration(headers.iterator());
	}

	public void setDescription(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDescription() not yet implemented.");
	}

	public String getFileName() throws javax.mail.MessagingException {
		return null;
	}

	public void setDataHandler(DataHandler parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDataHandler() not yet implemented.");
	}

	public void setText(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setText() not yet implemented.");
	}

	public InputStream getInputStream() throws java.io.IOException, javax.mail.MessagingException {
		return new ByteArrayInputStream(body);
	}

	public String getDescription() throws javax.mail.MessagingException {
		return null;
	}

	public String getDisposition() throws javax.mail.MessagingException {
		return null;
	}

	public void setContent(Multipart parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setContent() not yet implemented.");
	}

	public void writeTo(OutputStream os) throws java.io.IOException, javax.mail.MessagingException {
		os.write(body);
	}

	/*
	 * protected Date headToDate(String date, String time) throws MessagingException { SimpleDateFormat fm = new
	 * SimpleDateFormat("yyyyMMddhh:mm:ss aa"); String[] datehs = getHeader(date); String[] timehs = getHeader(time); if
	 * (datehs.length <= 0 || timehs.length <= 0) { return null; } try { return fm.parse(datehs[0].trim() +
	 * timehs[0].trim()); } catch (java.text.ParseException x) { throw new MessagingException("Cannot parse " + date + "
	 * or " + time, x); } }
	 */

	public void setHeader(String parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setHeader() not yet implemented.");
	}

	@Override
    public void setSubject(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setSubject() not yet implemented.");
	}

	public String getContentType() throws javax.mail.MessagingException {
		return null;
	}

	@Override
    public void setFrom() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFrom() not yet implemented.");
	}

	public int getLineCount() throws javax.mail.MessagingException {
		return nlines;
	}

	public void addHeader(String name, String value) throws javax.mail.MessagingException {
		headers.add(new Header(name, value));
	}

	public void removeHeader(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method removeHeader() not yet implemented.");
	}

	/*
	 * protected MsgAttachment newAttachment(String s) throws IOException { StringTokenizer st = new StringTokenizer(s,
	 * ","); String name = null; String path = null; String extension = null; while (st.hasMoreTokens()) { String tok =
	 * st.nextToken(); String n = nameOfAttachment(tok); String v = valueOfAttachment(tok); if (n.equals("Name")) { name =
	 * v; } else if (n.equals("Path")) { path = v; } else if (n.equals("Extension")) { extension = v; } } File ifile =
	 * new File(path); File nfile = new File(file.getParent(), ifile.getName()); return new MsgAttachment(nfile, name); }
	 */

	protected String nameOfAttachment(String tok) {
		int ind = tok.indexOf('=');
		if (0 < ind) {
			return tok.substring(0, ind);
		} else {
			return null;
		}
	}

	protected String valueOfAttachment(String tok) {
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
