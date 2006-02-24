package au.gov.naa.digipres.xena.plugin.html.javatools.util;
import java.lang.reflect.*;
import java.util.*;

public class Reflect {
	/**
	 *  Set the value of a field using reflection. For a field name that we call
	 *  "foo" there must exist a method called setFoo(T t);
	 *
	 * @param  obj                   The object containing the field to change.
	 * @param  fieldName             The string name of the field (must have a set
	 *      method).
	 * @param  value                 The new value for the field.
	 * @exception  ReflectException  Description of Exception
	 */
	public static void setValueUsingSetter(Object obj, String fieldName, Object value) throws ReflectException {
		String methodName = null;
		methodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		try {
			Method method = findMethod(obj.getClass().getMethods(), methodName);
			Object args[] = {value};
			method.invoke(obj, args);
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	public static void setValueUsingField(Object obj, String fieldName, Object value) throws ReflectException {
		try {
			Field f = obj.getClass().getField(fieldName);
			f.set(obj, value);
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	public static Object getValueUsingField(Object obj, String fieldName) throws ReflectException {
		try {
			Field f = obj.getClass().getField(fieldName);
			return f.get(obj);
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	public static Object getValueUsingGetter(Object obj, String fieldName) throws ReflectException {
		String methodName = null;
		methodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		Class classes[] = {};
		try {
			Method method = obj.getClass().getMethod(methodName, classes);
			Object args[] = {};
			return method.invoke(obj, args);
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	public static boolean isBasicType(Class c) {
		return Reflect.conformsTo(c, Number.class)
				 || Reflect.conformsTo(c, String.class)
				 || Reflect.conformsTo(c, Character.class);
	}

	public static void copyUsingGettersAndSetters(Object from, Object to) throws ReflectException, ReflectException {
		Class cFrom = from.getClass();
		Class cTo = to.getClass();
		Method[] getters = cFrom.getMethods();
		Method[] toMethods = cTo.getMethods();
		for (int i = 0; i < getters.length; i++) {
			String name = getters[i].getName();
			if (name.substring(0, 3).equals("get") && Character.isUpperCase(name.charAt(3))) {
				Method setter = findMethod(toMethods, "set" + name.substring(3, name.length()), getters[i].getReturnType());
				if (setter != null) {
					Object[] empty = {};
					try {
						Object[] o = {getters[i].invoke(from, empty)};
						setter.invoke(to, o);
					} catch (Exception e) {
						throw new ReflectException(e);
					}
				}
			}
		}
	}

	public static void copyUsingFields(Object from, Object to) throws ReflectException, ReflectException {
		Class cFrom = from.getClass();
		Class cTo = to.getClass();
		Field[] getters = cFrom.getFields();
		for (int i = 0; i < getters.length; i++) {
			try {
				String name = getters[i].getName();
				Object o = getValueUsingField(from, name);
				setValueUsingField(to, name, o);
			} catch (Exception e) {
			}
		}
	}

	public static Class classObjOf(Class c, String fieldName) throws ReflectException {
		Class rtn = null;
		try {
			Field field = c.getDeclaredField(fieldName);
			rtn = field.getType();
		} catch (NoSuchFieldException e) {
			Class csuper = c.getSuperclass();
			if (csuper == null) {
				throw new ReflectException("No field: " + fieldName, e);
			} else {
				rtn = classObjOf(csuper, fieldName);
			}
		}
		return rtn;
	}

	public static boolean conformsTo(Class c, Class conformsTo) {
		if (c == null) {
			return false;
		}
		if (c.equals(conformsTo) || conformsTo(c.getSuperclass(), conformsTo)) {
			return true;
		}
		Class[] classes = c.getInterfaces();
		for (int i = 0; i < classes.length; i++) {
			if (conformsTo(classes[i], conformsTo)) {
				return true;
			}
		}
		return false;
	}

	public static Collection allSuperInterfaces(Class c) {
		List rtn = new LinkedList();
		Class[] cs = c.getInterfaces();
		for (int i = 0; i < cs.length; i++) {
			rtn.add(allSuperInterfaces(cs[i]));
		}
		return rtn;
	}

	public static Collection allSuperClasses(Class c) {
		Collection rtn = null;
		Class cs = c.getSuperclass();
		if (cs != null) {
			rtn = allSuperClasses(cs);
			if (rtn != null) {
				rtn.add(cs);
			}
		}
		if (rtn == null) {
			rtn = new LinkedList();
		}
		return rtn;
	}

	public static Collection allSuper(Class c) {
		Collection rtn = allSuperInterfaces(c);
		rtn.addAll(allSuperClasses(c));
		return rtn;
	}

	static Method findMethod(Method[] methods, String name, Class type) {
		for (int i = 0; i < methods.length; i++) {
			Class[] types = methods[i].getParameterTypes();
			if (methods[i].getName().equals(name) && types.length == 1 && types[0].equals(type)) {
				return methods[i];
			}
		}
		return null;
	}

	static Method findMethod(Method[] methods, String name) {
		for (int i = 0; i < methods.length; i++) {
			Class[] types = methods[i].getParameterTypes();
			if (methods[i].getName().equals(name)) {
				return methods[i];
			}
		}
		return null;
	}
}
