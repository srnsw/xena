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
 */
public class MultiInputSource extends XenaInputSource {
	List<String> systemIds;

	// List<File> files;

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
			if (first) {
				first = false;
			} else {
				lst.append(",");
			}
			lst.append(it.next().toString());
		}
		lst.append("]");
		super.setSystemId(lst.toString());
	}

	public MultiInputSource(File[] files, Type type) {
		super("", type);
		// this.files = files;
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

	@Override
    public Reader getCharacterStream() {
		return new InputStreamReader(getByteStream());
	}

	@Override
    public InputStream getByteStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	public List<String> getSystemIds() {
		return systemIds;
	}

	public String getSystemId(int i) {
		return systemIds.get(i);
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
