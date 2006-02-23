package au.gov.naa.digipres.xena.kernel;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * InputSource that has multiple input sources or multiple files.
 *
 * @author Chris Bitmead
 */
public class MultiInputSource extends XenaInputSource {
	List<String> systemIds;

//	List<File> files;

	public MultiInputSource(List<String> systemids, Type type) {
		super("", type);
		this.systemIds = systemids;
		makeMultiId();
	}

	protected void makeMultiId() {
		StringBuffer lst = new StringBuffer("[");
		Iterator it = systemIds.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (!first) {
				lst.append(",");
			} else {
				first = false;
			}
			lst.append(it.next().toString());
		}
		lst.append("]");
		super.setSystemId(lst.toString());
	}


	public MultiInputSource(File[] files, Type type) {
		super("", type);
//		this.files = files;
		this.file = files[0];
		systemIds = new ArrayList<String>();
		for (File file : files) {
			try {
				systemIds.add(file.toURI().toURL().toExternalForm());
			} catch (MalformedURLException x) {
				x.printStackTrace();
			}
		}
		makeMultiId();
	}

	public int size() {
		return systemIds.size();
	}

	public Reader getCharacterStream() {
		return new InputStreamReader(getByteStream());
	}

	public InputStream getByteStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	public List<String> getSystemIds() {
		return systemIds;
	}

	public String getSystemId(int i) {
		return (String)systemIds.get(i);
	}

	public Reader getCharacterStream(int i) {
		return new InputStreamReader(getByteStream(i));
	}

	public InputStream getByteStream(int i) {
		try {
			URL url = new URL(getSystemId(i));
			return new BufferedInputStream(url.openStream());
		} catch (MalformedURLException x) {
			return null;
		} catch (IOException ex) {
			return null;
		}
	}

}
