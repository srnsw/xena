package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.net.*;
import java.security.Permission;
import java.io.*;
import java.util.Map;

public class URLConnectionProxy extends URLConnection {
	URLConnection c;

	ByteArrayInputStream data;
//	byte[] data;

	protected URLConnectionProxy(URLConnection c) {
		super(c.getURL());
		this.c = c;
	}

	public Permission getPermission() throws java.io.IOException {
		return c.getPermission();
	}

	public void setAllowUserInteraction(boolean allowuserinteraction) {
		c.setAllowUserInteraction(allowUserInteraction);
	}

	public Object getContent(Class[] parm1) throws java.io.IOException {
		return c.getContent(parm1);
	}

	public void connect() throws java.io.IOException {
		c.connect();
	}

	public Object getContent() throws java.io.IOException {
		return c.getContent();
	}

	public String getHeaderFieldKey(int n) {
		return c.getHeaderFieldKey(n);
	}

	public long getExpiration() {
		return c.getExpiration();
	}

	public long getDate() {
		return c.getDate();
	}

	public void setDoInput(boolean doinput) {
		c.setDoInput(doinput);
	}

	public boolean getDoInput() {
		return c.getDoInput();
	}

	public String getHeaderField(int n) {
		return c.getHeaderField(n);
	}

	public void setUseCaches(boolean usecaches) {
		c.setUseCaches(usecaches);
	}

	public void setDefaultUseCaches(boolean defaultusecaches) {
		c.setDefaultUseCaches(defaultusecaches);
	}

	public String getRequestProperty(String key) {
		return c.getRequestProperty(key);
	}

	public URL getURL() {
		return c.getURL();
	}

	public long getLastModified() {
		return c.getLastModified();
	}

	public OutputStream getOutputStream() throws java.io.IOException {
		return c.getOutputStream();
	}

	public void setRequestProperty(String key, String value) {
		c.setRequestProperty(key, value);
	}

	public boolean getAllowUserInteraction() {
		return c.getAllowUserInteraction();
	}

	public void addRequestProperty(String key, String value) {
		c.addRequestProperty(key, value);
	}

	public String getContentEncoding() {
		return c.getContentEncoding();
	}

	public Map getRequestProperties() {
		return c.getRequestProperties();
	}

	public String toString() {
		return c.toString();
	}

	public long getHeaderFieldDate(String name, long Default) {
		return c.getHeaderFieldDate(name, Default);
	}

	public boolean getDoOutput() {
		return c.getDoOutput();
	}

	public int getHeaderFieldInt(String name, int Default) {
		return c.getHeaderFieldInt(name, Default);
	}

	public boolean getUseCaches() {
		return c.getUseCaches();
	}

	public String getHeaderField(String name) {
		return c.getHeaderField(name);
	}

	public InputStream getInputStream() throws java.io.IOException {
		if (data == null) {
			return c.getInputStream();
		} else {
			return data;
		}
	}

	public boolean getDefaultUseCaches() {
		return c.getDefaultUseCaches();
	}

	public long getIfModifiedSince() {
		return c.getIfModifiedSince();
	}

	public int getContentLength() {
		return c.getContentLength();
	}

	public String getContentType() {
		return c.getContentType();
	}

	public Map getHeaderFields() {
		return c.getHeaderFields();
	}

	public void setDoOutput(boolean dooutput) {
		c.setDoOutput(dooutput);
	}

	public void setIfModifiedSince(long ifmodifiedsince) {
		c.setIfModifiedSince(ifmodifiedsince);
	}

	public void setData(byte[] data) {
		this.data = new ByteArrayInputStream(data);
	}
}