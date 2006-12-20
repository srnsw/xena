package au.gov.naa.digipres.xena.javatools;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class JarPreferences implements Comparable {
	Preferences preferences;

	Properties properties = new Properties();

	String path;

	ClassLoader cl;

	boolean useJarOnly = false;

	protected JarPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public static JarPreferences userNodeForPackage(Class c) {
		JarPreferences rtn = new JarPreferences(Preferences.userNodeForPackage(c));
		rtn.loadJarProperties(classToPackagePath(c), c.getClassLoader());
		return rtn;
	}

	public static JarPreferences systemNodeForPackage(Class c) {
		JarPreferences rtn = new JarPreferences(Preferences.
												systemNodeForPackage(c));
		rtn.loadJarProperties(classToPackagePath(c), c.getClassLoader());
		return rtn;
	}

	public static JarPreferences userRoot() {
		JarPreferences rtn = new JarPreferences(Preferences.userRoot());
		rtn.loadJarProperties("");
		return rtn;
	}

	public static JarPreferences systemRoot() {
		JarPreferences rtn = new JarPreferences(Preferences.systemRoot());
		rtn.loadJarProperties("");
		return rtn;
	}

	public static void importPreferences(InputStream is) throws IOException,
		InvalidPreferencesFormatException {
		Preferences.importPreferences(is);
	}

	public static void main(String[] args) throws BackingStoreException {
		JarPreferences p = JarPreferences.userRoot().node("javatools/util");
		// userNodeForPackage(JarPreferences.class);
		String s = p.get("foo", null);
		System.out.println(s);
		System.out.println("NODE: " + p.nodeExists("foo"));
		System.out.println("INT: " + p.getInt("baz", 12));
	}

	static String classToPackagePath(Class cls) {
		return ClassName.classToPath(ClassName.packageComponent(cls.getName()));
	}

	public ClassLoader getClassLoader() {
		if (cl == null) {
			return getClass().getClassLoader();
		} else {
			return cl;
		}

	}

	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	public String get(String key, String def) {
		String rtn = null;
		if (!useJarOnly) {
			rtn = preferences.get(key, null);
		}
		if (rtn == null) {
			rtn = properties.getProperty(key);
		}
		if (rtn == null) {
			rtn = def;
		}
		return rtn;
	}

	public void setUseJarOnly(boolean v) {
		useJarOnly = v;
	}

	public static final String DEFAULT_LIST_SEP = " \t\n\r\f";

	public List<String> getList(String propertyName, List<String> def) {
		return getList(propertyName, def, DEFAULT_LIST_SEP);
	}

	public List<String> getList(String propertyName, List<String> def, String separator) {
		String value = get(propertyName, null);
		if (value == null) {
			return def;
		}
		List<String> rtn = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(value, separator);
		while (st.hasMoreTokens()) {
			rtn.add(st.nextToken());
		}
		return rtn;
	}

	public void putList(String propertyName, List v) {
		putList(propertyName, v, DEFAULT_LIST_SEP.charAt(0));
	}

	public void putList(String propertyName, List v, char separator) {
		StringBuffer sb = new StringBuffer();
		Iterator it = v.iterator();
		while (it.hasNext()) {
			sb.append(it.next().toString());
			if (it.hasNext()) {
				sb.append(separator);
			}
		}
		put(propertyName, sb.toString());
	}

	public int getInt(String key, int def) {
		try {
			return Integer.parseInt(get(key, ""));
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public long getLong(String key, long def) {
		try {
			return Long.parseLong(get(key, ""));
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public boolean getBoolean(String key, boolean def) {
		String v = get(key, "");
		if ("true".equals( v )) {
			return true;
		} else {
			if ("false".equals( v )) {
				return false;
			} else {
				return def;
			}
		}
	}

	public float getFloat(String key, float def) {
		try {
			return Float.parseFloat(get(key, ""));
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public double getDouble(String key, double def) {
		try {
			return Double.parseDouble(get(key, ""));
		} catch (NumberFormatException ex) {
			return def;
		}
	}

	public byte[] getByteArray(String key, byte[] def) {
		String v = get(key, null);
		if (v != null) {
			return v.getBytes();
		}
		return def;
	}

	public boolean isUserNode() {
		return preferences.isUserNode();
	}

	public String toString() {
		String rtn = "";
		Enumeration en = properties.keys();
		while (en.hasMoreElements()) {
			String key = (String)en.nextElement();
			String value = properties.getProperty(key);
			rtn += key + "=" + value + "\n";
		}
		rtn += preferences.toString();
		return rtn;
	}

	public void put(String key, String value) {
		preferences.put(key, value);
	}

	public void remove(String key) {
		preferences.remove(key);
	}

	public void clear() throws BackingStoreException {
		preferences.clear();
	}

	public void putInt(String key, int value) {
		preferences.putInt(key, value);
	}

	public void putLong(String key, long value) {
		preferences.putLong(key, value);
	}

	public void putBoolean(String key, boolean value) {
		preferences.putBoolean(key, value);
	}

	public void putFloat(String key, float value) {
		preferences.putFloat(key, value);
	}

	public void putDouble(String key, double value) {
		preferences.putDouble(key, value);
	}

	public void putByteArray(String key, byte[] value) {
		preferences.putByteArray(key, value);
	}

	public String[] keys() throws BackingStoreException {
		return preferences.keys();
	}

	public String[] childrenNames() throws BackingStoreException {
		return preferences.childrenNames();
	}

	public Preferences parent() {
		return preferences.parent();
	}

	public JarPreferences node(String pathName) {
		return node(pathName, getClass().getClassLoader());
	}

	public JarPreferences node(String pathName, ClassLoader cl) {
		JarPreferences rtn = new JarPreferences(preferences.node(pathName));
		String p = preferences.absolutePath();
		rtn.loadJarProperties(ClassName.makeRelativePath(ClassName.joinPath(p,
																			pathName)), cl);
		return rtn;
	}

	public boolean nodeExists(String pathName) throws BackingStoreException {
		return nodeExists(pathName, getClass().getClassLoader());
	}

	public boolean jarNodeExists(String pathName, ClassLoader cl) throws
		BackingStoreException {
		return loadJarProperties(pathName, cl);
	}

	public boolean nodeExists(String pathName, ClassLoader cl) throws
		BackingStoreException {
		if (loadJarProperties(pathName, cl)) {
			return true;
		}
		return preferences.nodeExists(pathName);
	}

	public Properties toProperties() throws BackingStoreException {
		Properties rtn = new Properties();
		String[] keys = this.keys();
		for (int i = 0; i < keys.length; i++) {
			rtn.setProperty(keys[i], get(keys[i], null));
		}
		return rtn;
	}

	public void removeNode() throws BackingStoreException {
		preferences.removeNode();
	}

	public String name() {
		return preferences.name();
	}

	public String absolutePath() {
		return preferences.absolutePath();
	}

	public void flush() throws BackingStoreException {
		preferences.flush();
	}

	public void sync() throws BackingStoreException {
		preferences.sync();
	}

	public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
		preferences.addPreferenceChangeListener(pcl);
	}

	public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
		preferences.removePreferenceChangeListener(pcl);
	}

	public void addNodeChangeListener(NodeChangeListener ncl) {
		preferences.addNodeChangeListener(ncl);
	}

	public void removeNodeChangeListener(NodeChangeListener ncl) {
		preferences.removeNodeChangeListener(ncl);
	}

	public void exportNode(OutputStream os) throws IOException,
		BackingStoreException {
		preferences.exportNode(os);
	}

	public void exportSubtree(OutputStream os) throws IOException,
		BackingStoreException {
		preferences.exportSubtree(os);
	}

	boolean loadJarProperties(String pathName) {
		return loadJarProperties(pathName, getClass().getClassLoader());
	}

	boolean loadJarProperties(String pathName, ClassLoader cl) {
		this.cl = cl;
		boolean rtn = false;
		try {
			InputStream is = cl.getResourceAsStream(ClassName.joinPath(pathName,
																	   "preferences.properties"));
			if (is != null) {
				properties.load(is);
				is.close();
				rtn = true;
			}
		} catch (Exception ex) {
			System.err.println(ex);
		}
		return rtn;
	}

	public int compareTo(Object o)
	{
		int retVal;
		if (o instanceof JarPreferences)
		{
			JarPreferences compPrefs = (JarPreferences)o;
			retVal = this.name().compareTo(compPrefs.name());
		}
		else
		{
			retVal = 1;
		}
		return retVal;
	}
}
