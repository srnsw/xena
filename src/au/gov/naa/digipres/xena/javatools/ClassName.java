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

package au.gov.naa.digipres.xena.javatools;

/**
 *  Splits the classname and package name from a Java class name.
 *
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
		if (!"".equals(pkg) && pkg.charAt(pkg.length() - 1) != '/') {
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

	@Override
    public String toString() {
		return name;
	}
}
