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

package au.gov.naa.digipres.xena.plugin.html.javatools.util;

import java.util.*;
import java.io.*;

/**
 *  An extended version of Properties class. It allows reading Properties files
 *  from the classpath or resource file. It uses ClassLoader.getResourceAsStream
 *  to find a properties file, so that means it either has to be on the
 *  classpath or bundled into the jar. <P>
 *
 *  It is intended that you only pass a single string to find the properties
 *  like "foo". That will cause Props to search for a file called
 *  foo.properties. The directory it searches in will be by default the "res/"
 *  directory. That means that it will search your classpath or jar file for a
 *  file res/foo.properties. If you run your software in different environments
 *  you can pass a system property to the java virtual machine called
 *  "environment". In that case the searching will descend another directory
 *  level. e.g. if you pass -Denvironment=development, then it will first look
 *  for a file res/development/foo.properties. If you are using servlets your
 *  servlet engine should have a means to pass in system variables. <P>
 *
 *  This makes it easy to integrate properties into your code. All you do is use
 *  Props to find a certain class of property, like "foo" and it will take care
 *  of where to physically find the file. And you can set up different
 *  properties for different environments. <P>
 *
 *  TODO: make it combine the environment and non-environment specific
 *  properties into the one set.
 *
 * @created    November 23, 2001
 */

public class Props extends Properties implements Resetable {
	// Hashtable because it's synchronized
	static Hashtable map = new Hashtable();
	static File localConfigDirFile = new File(getLocalConfigDir());
	File localConfigFile;
	String name;
	String fullName;
	boolean valid = true;
	ClassLoader cl;

	private Props(String name, ClassLoader cl) throws IOException {
		this.name = name;
		this.cl = cl;
		localConfigFile = new File(localConfigDirFile, name + ".properties");
	}

	public static Props singleton(String name) throws IOException {
		return singleton(name, Props.class.getClassLoader());
	}

	public static Props singleton(String name, ClassLoader cl) throws IOException {
		synchronized (Props.class) {
			Key key = new Key(name, cl);
			Props rtn = (Props) map.get(key);
			if (rtn == null) {
				rtn = new Props(name, cl);
				rtn.load();
				map.put(key, rtn);
			}
			return rtn;
		}
	}

	public static StringTokenizer makeTokenizer(String names) {
		if (names == null) {
			names = "";
		}
		return new StringTokenizer(names, ", \t");
	}

	static String getLocalConfigDir() {
		try {
			Props props = new Props("props", Props.class.getClassLoader());
			props.loadResources();
			return props.getProperty("localConfigDir");
		} catch (IOException e) {
			System.out.println("No local Config Dir specified");
		}
		return null;
	}

	public ClassLoader getClassLoader() {
		return cl;
	}

	public String getFullName() {
		return fullName;
	}

	/**
	 *  Assumes that there is a property containing a list of things (probably, but
	 *  not necessarily classes to load). This returns a StringTokenizer for
	 *  parsing that list.
	 *
	 * @param  propertyName  Description of Parameter
	 * @return               The tokenizer value
	 */
	public StringTokenizer getTokenizer(String propertyName) {
		String classNames = getProperty(propertyName);
		return makeTokenizer(classNames);
	}

	public List getPropertyList(String propertyName) {
		List rtn = new ArrayList();
		StringTokenizer st = getTokenizer(propertyName);
		while (st.hasMoreTokens()) {
			rtn.add(st.nextToken());
		}
		return rtn;
	}

	public void reset() throws ResetException {
		try {
			load();
		} catch (IOException e) {
			throw new ResetException(e);
		}
	}

	public void load() throws IOException {
		loadResources();
		loadLocalConfig();
	}

	public void store() throws IOException {
		Props resProps = new Props(name, cl);
		resProps.loadResources();
		Properties localConfigProps = new Properties();
		Iterator it = entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String value = resProps.getProperty((String) entry.getKey());
			if (value == null || !value.equals(entry.getValue())) {
				localConfigProps.setProperty((String) entry.getKey(), (String) entry.getValue());
			}
		}
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(localConfigFile);
			localConfigProps.store(fos, localConfigFile.getName());
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	public void loadLocalConfig() throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(localConfigFile);
			load(fis);
		} catch (FileNotFoundException e) {
			// Don't worry about it.
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public void loadResources() throws IOException {
		String pname = "res/";
		String pname2 = pname;
		try {
			String env = System.getProperty("environment");
			if (env != null) {
				pname2 += env + "/";
			}
			pname += name + ".properties";
			pname2 += name + ".properties";
			fullName = pname + " or " + pname2;
			// The single '|' below is correct.
			if (!(loadOne(cl, pname) | loadOne(cl, pname2))) {
				throw new IOException("Can't find either " + fullName);
			}
		} catch (IOException e) {
			String error = "Error loading properties file: '" + fullName + "' : " + e;
			// Don't use FileLog here. That uses Props, which would be recursive.
			System.out.println(error);
			throw new IOException(error);
		}
	}

	boolean loadOne(ClassLoader cl, String props) throws IOException {
		boolean rtn = false;
		InputStream is = cl.getResourceAsStream(props);
		if (is != null) {
			load(is);
			is.close();
			rtn = true;
		}
		return rtn;
	}

	static class Key {
		String name;
		ClassLoader cl;

		Key(String name, ClassLoader cl) {
			this.name = name;
			this.cl = cl;
		}

		@Override
        public boolean equals(Object o) {
			return name.equals(((Key) o).name) && cl.equals(((Key) o).cl);
		}

		@Override
        public int hashCode() {
			return name.hashCode() + cl.hashCode();
		}
	}
}
