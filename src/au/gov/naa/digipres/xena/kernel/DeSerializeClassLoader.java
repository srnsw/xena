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

import java.net.URL;
import java.net.URLClassLoader;

/**
 *  A ClassLoader that can load classes from a specific location.
 *
 * @created    April 24, 2002
 */
public class DeSerializeClassLoader extends URLClassLoader {

	public DeSerializeClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);
	}

	@Override
    public void addURL(URL url) {
		super.addURL(url);
	}

	@Override
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
