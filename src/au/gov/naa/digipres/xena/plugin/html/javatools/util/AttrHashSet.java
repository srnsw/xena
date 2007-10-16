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
import java.lang.reflect.*;
import java.lang.reflect.InvocationTargetException;

public class AttrHashSet extends HashSet {
	public AttrHashSet(Collection c, Method method) throws InvocationTargetException, IllegalAccessException {
		this(c, method, null);
	}

	public AttrHashSet(Collection c, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		if (args == null) {
			args = new Object[0];
		}
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			Object key = method.invoke(o, args);
			this.add(key);
		}
	}
}
