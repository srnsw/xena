package au.gov.naa.digipres.xena.plugin.email.trim;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Multipart;

public class TrimAttachment extends BodyPart implements TrimPart {
	File file;
	String name;

	public TrimAttachment(File file, String name) {
		this.file = file;
		this.name = name;
	}

	public boolean isMimeType(String type) throws javax.mail.MessagingException {
		return false;
	}

	public void setDisposition(String parm1) throws javax.mail.MessagingException {
	}

	public DataHandler getDataHandler() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method getDataHandler() not yet implemented.");
	}

	public void setContent(Object parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setContent() not yet implemented.");
	}

	public Enumeration getNonMatchingHeaders(String[] parm1) throws javax.mail.MessagingException {
		return new Enumeration() {
			public boolean hasMoreElements() {
				return false;
			}
			public Object nextElement() {
				return null;
			}
		};
	}

	public Object getContent() throws java.io.IOException, javax.mail.MessagingException {
		return new FileInputStream(file);
	}

	public void setFileName(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFileName() not yet implemented.");
	}

	public int getSize() throws javax.mail.MessagingException {
		return (int)file.length();
	}

	public String[] getHeader(String parm1) throws javax.mail.MessagingException {
		return new String[0];
	}

	public Enumeration getMatchingHeaders(String[] parm1) throws javax.mail.MessagingException {
		return new Enumeration() {
			public boolean hasMoreElements() {
				return false;
			}
			public Object nextElement() {
				return null;
			}
		};
	}

	public Enumeration getAllHeaders() throws javax.mail.MessagingException {
		return new Enumeration() {
			public boolean hasMoreElements() {
				return false;
			}
			public Object nextElement() {
				return null;
			}
		};
	}

	public void setDescription(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDescription() not yet implemented.");
	}

	public String getFileName() throws javax.mail.MessagingException {
		return name;
	}

	public File getFile() {
		return file;
	}

	public void setDataHandler(DataHandler parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDataHandler() not yet implemented.");
	}

	public void setText(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setText() not yet implemented.");
	}

	public InputStream getInputStream() throws java.io.IOException, javax.mail.MessagingException {
		return new FileInputStream(file);
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
		InputStream is = getInputStream();
		byte[] buf = new byte[4096];
		int c;
		while (0 <= (c = is.read(buf))) {
			os.write(buf, 0, c);
		}
		is.close();
	}

	public void setHeader(String parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setHeader() not yet implemented.");
	}

	public String getContentType() throws javax.mail.MessagingException {
		return null;
	}

	public int getLineCount() throws javax.mail.MessagingException {
		return -1;
	}

	public void addHeader(String parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method addHeader() not yet implemented.");
	}

	public void removeHeader(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method removeHeader() not yet implemented.");
	}
}
