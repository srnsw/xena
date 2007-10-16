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

package au.gov.naa.digipres.xena.plugin.html.javatools.wget;

import java.net.*;
import java.security.Permission;
import java.io.*;
import java.util.Map;

public class URLConnectionProxy extends URLConnection {
	URLConnection c;

	ByteArrayInputStream data;

	// byte[] data;

	protected URLConnectionProxy(URLConnection c) {
		super(c.getURL());
		this.c = c;
	}

	@Override
    public Permission getPermission() throws java.io.IOException {
		return c.getPermission();
	}

	@Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
		c.setAllowUserInteraction(allowUserInteraction);
	}

	@Override
    public Object getContent(Class[] parm1) throws java.io.IOException {
		return c.getContent(parm1);
	}

	@Override
    public void connect() throws java.io.IOException {
		c.connect();
	}

	@Override
    public Object getContent() throws java.io.IOException {
		return c.getContent();
	}

	@Override
    public String getHeaderFieldKey(int n) {
		return c.getHeaderFieldKey(n);
	}

	@Override
    public long getExpiration() {
		return c.getExpiration();
	}

	@Override
    public long getDate() {
		return c.getDate();
	}

	@Override
    public void setDoInput(boolean doinput) {
		c.setDoInput(doinput);
	}

	@Override
    public boolean getDoInput() {
		return c.getDoInput();
	}

	@Override
    public String getHeaderField(int n) {
		return c.getHeaderField(n);
	}

	@Override
    public void setUseCaches(boolean usecaches) {
		c.setUseCaches(usecaches);
	}

	@Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
		c.setDefaultUseCaches(defaultusecaches);
	}

	@Override
    public String getRequestProperty(String key) {
		return c.getRequestProperty(key);
	}

	@Override
    public URL getURL() {
		return c.getURL();
	}

	@Override
    public long getLastModified() {
		return c.getLastModified();
	}

	@Override
    public OutputStream getOutputStream() throws java.io.IOException {
		return c.getOutputStream();
	}

	@Override
    public void setRequestProperty(String key, String value) {
		c.setRequestProperty(key, value);
	}

	@Override
    public boolean getAllowUserInteraction() {
		return c.getAllowUserInteraction();
	}

	@Override
    public void addRequestProperty(String key, String value) {
		c.addRequestProperty(key, value);
	}

	@Override
    public String getContentEncoding() {
		return c.getContentEncoding();
	}

	@Override
    public Map getRequestProperties() {
		return c.getRequestProperties();
	}

	@Override
    public String toString() {
		return c.toString();
	}

	@Override
    public long getHeaderFieldDate(String name, long Default) {
		return c.getHeaderFieldDate(name, Default);
	}

	@Override
    public boolean getDoOutput() {
		return c.getDoOutput();
	}

	@Override
    public int getHeaderFieldInt(String name, int Default) {
		return c.getHeaderFieldInt(name, Default);
	}

	@Override
    public boolean getUseCaches() {
		return c.getUseCaches();
	}

	@Override
    public String getHeaderField(String name) {
		return c.getHeaderField(name);
	}

	@Override
    public InputStream getInputStream() throws java.io.IOException {
		if (data == null) {
			return c.getInputStream();
		} else {
			return data;
		}
	}

	@Override
    public boolean getDefaultUseCaches() {
		return c.getDefaultUseCaches();
	}

	@Override
    public long getIfModifiedSince() {
		return c.getIfModifiedSince();
	}

	@Override
    public int getContentLength() {
		return c.getContentLength();
	}

	@Override
    public String getContentType() {
		return c.getContentType();
	}

	@Override
    public Map getHeaderFields() {
		return c.getHeaderFields();
	}

	@Override
    public void setDoOutput(boolean dooutput) {
		c.setDoOutput(dooutput);
	}

	@Override
    public void setIfModifiedSince(long ifmodifiedsince) {
		c.setIfModifiedSince(ifmodifiedsince);
	}

	public void setData(byte[] data) {
		this.data = new ByteArrayInputStream(data);
	}
}
