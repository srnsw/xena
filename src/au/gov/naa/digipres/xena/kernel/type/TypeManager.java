package au.gov.naa.digipres.xena.kernel.type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 *  Manages instances of Type instances.
 *
 * @see Type
 * @author     Chris Bitmead
 * @created    6 May 2002
 */
public class TypeManager implements LoadManager {
	static TypeManager theSingleton = new TypeManager();

	protected Map<String, Type> nameMap = new HashMap<String, Type>();

	protected Map<Class, Type> clsMap = new HashMap<Class, Type>();

	protected Map<String, Type> clsNameMap = new HashMap<String, Type>();

	protected Map<String, Type> tagMap = new HashMap<String, Type>();

	protected List<Type> allTypes = new ArrayList<Type>();

	/**
	 *  Class constructor
	 */
	protected TypeManager() {
	}

	public static TypeManager singleton() {
		return theSingleton;
	}

	public Set getAllTags() {
		return tagMap.keySet();
	}

	public boolean load(JarPreferences pp) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(pp);
			List types = loader.loadInstances("types");

			Iterator it = types.iterator();
			while (it.hasNext()) {
				Type type = (Type)it.next();
				clsMap.put(type.getClass(), type);
				nameMap.put(type.getName(), type);
				clsNameMap.put(type.getClass().getName(), type);
				allTypes.add(type);
				if (type instanceof XenaFileType) {
					tagMap.put(((XenaFileType)type).getTag(), type);
				}
			}
			return!types.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	/**
	 *  Returns List of all Types avalable at runtime
	 *
	 * @return    List of Types
	 */
	public List<Type> allTypes() {
		return new ArrayList<Type>(clsMap.values());
	}

	/**
	 * Return only the FileType types.
	 */
	public List<FileType> allFileTypes() {
		List<FileType> rtn = new ArrayList<FileType>();
		Iterator it = allTypes.iterator();
		while (it.hasNext()) {
			Object type = it.next();
			if (type instanceof FileType) {
				rtn.add((FileType)type);
			}
		}
		return rtn;
	}

	/**
	 * Return only the non-Xena File Types
	 */
	public List<FileType> allNonXenaFileTypes() {
		List<FileType> rtn = new ArrayList<FileType>();
		Iterator it = allTypes.iterator();
		while (it.hasNext()) {
			Object type = it.next();
			if (type instanceof FileType && !(type instanceof XenaFileType)) {
				rtn.add((FileType)type);
			}
		}
		return rtn;
	}

	/**
	 * Return only the non-File misc types.
	 */
	public List<MiscType> allMiscTypes() {
		List<MiscType> rtn = new ArrayList<MiscType>();
		Iterator it = allTypes.iterator();
		while (it.hasNext()) {
			Object type = it.next();
			if (type instanceof MiscType) {
				rtn.add((MiscType)type);
			}
		}
		return rtn;
	}

	/**
	 *  Return all Xena File Types
	 */
	public List<XenaFileType> allXenaFileTypes() {
		List<XenaFileType> rtn = new ArrayList<XenaFileType>();
		Iterator it = allTypes.iterator();
		while (it.hasNext()) {
			Object type = it.next();
			if (type instanceof XenaFileType) {
				rtn.add((XenaFileType)type);
			}
		}
		return rtn;
	}

	/**
	 *  Resolves a Class into its corresponding FileType
	 *
	 * @param  cls  Class to be resolved
	 * @return      FileType corresponding to class
	 */
	public Type lookup(Class cls) throws XenaException {
		Type rtn = (Type)clsMap.get(cls);
		if (rtn == null) {
			throw new XenaException("Type Not Found: " + cls.getName());
		}
		return rtn;
	}

	/**
	 * Resolves a String name into its corresponding FileType
	 * Avoid use of this method, because type names tend to change!
	 *
	 * @param  name  name of required Type
	 * @return Type corresponding to name
	 */
	public Type lookup(String name) throws XenaException {
		Type rtn = (Type)nameMap.get(name);
		if (rtn == null) {
			throw new XenaException("Type Not Found: " + name);
		}
		return rtn;
	}

	public Type lookupByClassName(String name) throws XenaException {
		Type rtn = (Type)clsNameMap.get(name);
		if (rtn == null) {
			throw new XenaException("Type Not Found: " + name);
		}
		return rtn;
	}

	/**
	 * Resolves a Class into its corresponding XenaFileType
	 *
	 * @param  cls  Class to be resolved
	 * @return XenaFileType corresponding to class
	 */
	public XenaFileType lookupXenaFileType(Class cls) throws XenaException {
		XenaFileType rtn = (XenaFileType)clsMap.get(cls);
		if (rtn == null) {
			throw new XenaException("Type Not Found: " + cls.getName());
		}
		return rtn;
	}

	/**
	 * Resolves a String into its corresponding XenaFileType
	 * Avoid use of this method, because type names tend to change!
	 *
	 * @param  name  name of required Class
	 * @return XenaFileType corresponding to name
	 */
	public XenaFileType lookupXenaFileType(String name) throws XenaException {
		XenaFileType rtn = (XenaFileType)nameMap.get(name);
		if (rtn == null) {
			throw new XenaException("Type Not Found: " + name);
		}
		return rtn;
	}

	/**
	 *  Resolves a String representing an XML tag name into its corresponding
	 *  XenaFileType
	 *
	 * @param  tagName  Description of Parameter
	 * @return          XenaFileType corresponding to XML tag name
	 */
	public XenaFileType lookupXenaTag(String tagName) throws XenaException {
		XenaFileType rtn = (XenaFileType)tagMap.get(tagName);
		if (rtn == null) {
			throw new XenaException("Tag Not Found: " + tagName);
		}
		return rtn;
	}

	public void complete() {}
}
