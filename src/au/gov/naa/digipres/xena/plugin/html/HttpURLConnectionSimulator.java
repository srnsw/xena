package au.gov.naa.digipres.xena.plugin.html;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.helper.JdomUtil;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;

/**
 * Simulate a HTTP connection from files. Xena allows you to restart a HTTP
 * web site download after stopping half way through.  This saves the long
 * process of downloading from scratch. This is difficult without
 * starting from the beginning, but we achieve it by creating this connection
 * simulation class and get the data from the Xena files.
 *
 * @author Chris Bitmead
 */
public class HttpURLConnectionSimulator extends HttpURLConnection {
	Element http;

	Element headers;

	Element data;

	Element content;

	File file;

	static URL elementToURL(Element http) throws MalformedURLException {
		Element url = http.getChild("url");
		URL rtn = new URL(url.getText());
		return rtn;
	}

	public HttpURLConnectionSimulator(URL url, URL file) throws XenaException, IOException, JDOMException {
		super(url);
		http = JdomUtil.loadUnwrapXml(file);

		this.http = http;
		headers = http.getChild("headers");
		data = http.getChild("data");
		content = (Element)data.getChildren().get(0);
	}

	public String getHeaderFieldKey(int n) {
		if (headers.getChildren().size() <= n) {
			return null;
		} else {
			Element header = (Element)headers.getChildren().get(n);
			return header.getAttributeValue("name");
		}
	}

	public String getHeaderField(int n) {
		if (headers.getChildren().size() <= n) {
			return null;
		} else {
			Element header = (Element)headers.getChildren().get(n);
			return header.getText();
		}
	}

	public void setInstanceFollowRedirects(boolean followRedirects) {
		// nothing. Not implemented.
	}

	public boolean getInstanceFollowRedirects() {
		return false;
	}

	public void setRequestMethod(String method) throws ProtocolException {
		// nothing. Not implemented.
	}

	public String getRequestMethod() {
		return "GET";
	}

	public String getHeaderField(String name) {
		List children = headers.getChildren();
		int size = children.size();
		name = name.toLowerCase();
		for (int i = 0; i < size; i++) {
			String key = getHeaderFieldKey(i);
			if (key != null && key.toLowerCase().equals(name)) {
				return getHeaderField(i);
			}
		}
		return null;
	}

	public void disconnect() {
		// Nothing
	}

	public boolean usingProxy() {
		return false;
	}

	public Permission getPermission() throws IOException {
		// Not implemented
		return null;
	}

	public InputStream getErrorStream() {
		// Not implemented
		return null;
	}

	public InputStream getInputStream() throws IOException {
		try {
			if (file == null) {
				TransformerHandler dn = NormaliserManager.singleton().lookupDeNormaliser(content.getName());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				StreamResult sr = new StreamResult(baos);
				dn.setResult(sr);
				JdomUtil.writeElement(dn, content);
				return new ByteArrayInputStream(baos.toByteArray());
			} else {
				return new FileInputStream(file);
			}
		} catch (SAXException x) {
			throw new IOException(x.toString());
		} catch (JDOMException x) {
			throw new IOException(x.toString());
		}
	}

	public void connect() {
		// Nothing
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
