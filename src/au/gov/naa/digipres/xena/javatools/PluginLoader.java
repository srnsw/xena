package au.gov.naa.digipres.xena.javatools;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class PluginLoader {
	JarPreferences props;

	ClassLoader cl;

	public PluginLoader(JarPreferences props) {
		this.props = props;
		this.cl = props.getClassLoader();
	}

	public PluginLoader(JarPreferences props, ClassLoader cl) {
		this.props = props;
		this.cl = cl;
	}
    
	/**
	 * Assumes that there is a property containing a list of things
	 * (probably, but not necessarily classes to load). This returns a
	 * StringTokenizer for parsing that list.
	 */
	public static StringTokenizer makeTokenizer(String names) {
		if (names == null) {
			names = "";
		}
		return new StringTokenizer(names, ", \t");
	}

	public List<Class> loadClasses(String propertyName) throws ClassNotFoundException {
		String classNames = props.get(propertyName, "");
		List<Class> rtn = new ArrayList<Class>();
		StringTokenizer st = makeTokenizer(classNames);
		while (st.hasMoreTokens()) {
			String className = st.nextToken();
			Class cls = Class.forName(className, true, cl);
			rtn.add(cls);
		}
		return rtn;
	}

	public List loadInstances(String propertyName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		List classList = loadClasses(propertyName);
		Iterator it = classList.iterator();
		List<Object> rtn = new ArrayList<Object>();
		while (it.hasNext()) {
			Class cls = (Class)it.next();
			Object o = cls.newInstance();
			rtn.add(o);
		}
		return rtn;
	}
}
