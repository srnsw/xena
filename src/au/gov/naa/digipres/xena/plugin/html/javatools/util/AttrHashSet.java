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