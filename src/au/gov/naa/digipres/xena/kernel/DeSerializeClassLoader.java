package au.gov.naa.digipres.xena.kernel;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *  A ClassLoader that can load classes from a specific location.
 *
 * @author     Chris Bitmead
 * @created    April 24, 2002
 */
public class DeSerializeClassLoader extends URLClassLoader {

	public DeSerializeClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);
	}

	public void addURL(URL url) {
		super.addURL(url);
	}

	public String toString() {
		StringBuffer rtn = new StringBuffer();
		for (int i = 0; i < this.getURLs().length; i++) {
			if (i != 0) {
				rtn.append(", ");
			}
			rtn.append(this.getURLs()[i]);
		}
		return rtn.toString();
	}
}