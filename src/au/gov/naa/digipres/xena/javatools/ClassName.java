package au.gov.naa.digipres.xena.javatools;

/**
 *  Splits the classname and package name from a Java class name.
 *
 * @author     Chris Bitmead
 * @created    8 February 2003
 */

public class ClassName {
	String name;

	public ClassName(String name) {
		this.name = name;
	}

	/**
	 *  Return the class name component from a fully qualified Java class name.
	 *
	 * @param  fullClassName  Description of Parameter
	 * @return                Description of the Returned Value
	 */
	public static String classNameComponent(String fullClassName) {
		int lastInd = fullClassName.lastIndexOf('.');
		if (lastInd < 0) {
			return fullClassName;
		} else {
			return fullClassName.substring(lastInd + 1);
		}
	}

	public static String joinPath(String pkg, String cls) {
		if (!"".equals( pkg ) && pkg.charAt(pkg.length() - 1) != '/') {
			pkg += "/";
		}
		return pkg + cls;
	}

	public static String makeRelativePath(String path) {
		if (0 < path.length() && path.charAt(0) == '/') {
			path = path.substring(1);
		}
		return path;
	}

	/**
	 *  Return the package name component from a fully qualified Java class name.
	 *
	 * @param  fullClassName  Description of Parameter
	 * @return                Description of the Returned Value
	 */
	public static String packageComponent(String fullClassName) {
		int lastInd = fullClassName.lastIndexOf('.');
		if (lastInd < 0) {
			return fullClassName;
		} else {
			return fullClassName.substring(0, lastInd);
		}
	}

	public static String classToPath(String cls) {
		return cls.replace('.', '/');
	}

	public String getPackage() {
		return packageComponent(name);
	}

	public String getClassName() {
		return classNameComponent(name);
	}

	public String toString() {
		return name;
	}
}
