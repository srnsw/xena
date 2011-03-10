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
 * @author Chris Bitmead
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.kernel.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 *  Manages Xena Types. Types are primarily used to identify the various file types that Xena can
 *  normalise, but there are also some non-file types representing databases and primitve data types
 *  such as strings, numbers etc.
 *  
 *  Types are loaded into the TypeManager when plugins are loaded using the load method. Types can
 *  be retrieved using the name of the type, the class of the type, or the class name of the type.
 *  Types may be retrieved in generic Type form, or the more specific XenaFileType form.
 *
 * @see Type
 * @created    6 May 2002
 */
public class TypeManager {

	protected Map<String, Type> nameMap = new HashMap<String, Type>();

	protected Map<Class<?>, Type> clsMap = new HashMap<Class<?>, Type>();

	protected Map<String, Type> clsNameMap = new HashMap<String, Type>();

	protected Map<String, Type> tagMap = new HashMap<String, Type>();

	protected List<Type> allTypes = new ArrayList<Type>();

	private PluginManager pluginManager;

	/**
	 * Construct a new TypeManager, using the given PluginManager
	 * 
	 * @param pluginManager
	 */
	public TypeManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;

		// add the default built in xena types.
		List<Type> builtinTypeList = new ArrayList<Type>();
		builtinTypeList.add(new BinaryFileType());
		builtinTypeList.add(new XenaBinaryFileType());

		for (Object element : builtinTypeList) {
			Type type = (Type) element;
			clsMap.put(type.getClass(), type);
			nameMap.put(type.getName(), type);
			clsNameMap.put(type.getClass().getName(), type);
			allTypes.add(type);
			if (type instanceof XenaFileType) {
				tagMap.put(((XenaFileType) type).getTag(), type);
			}
		}
	}

	/**
	 * @return Returns the pluginManager.
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * @param pluginManager The new value to set pluginManager to.
	 */
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void addTypes(List<Type> typeList) {
		for (Type type : typeList) {
			clsMap.put(type.getClass(), type);
			nameMap.put(type.getName(), type);
			clsNameMap.put(type.getClass().getName(), type);
			allTypes.add(type);
			if (type instanceof XenaFileType) {
				tagMap.put(((XenaFileType) type).getTag(), type);
			}
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
		for (Type type : allTypes) {
			if (type instanceof FileType) {
				rtn.add((FileType) type);
			}
		}
		return rtn;
	}

	/**
	 * Return only the non-Xena File Types
	 */
	public List<FileType> allNonXenaFileTypes() {
		List<FileType> rtn = new ArrayList<FileType>();
		for (Type type : allTypes) {
			if (type instanceof FileType && !(type instanceof XenaFileType)) {
				rtn.add((FileType) type);
			}
		}
		return rtn;
	}

	/**
	 * Return only the non-File misc types.
	 */
	public List<MiscType> allMiscTypes() {
		List<MiscType> rtn = new ArrayList<MiscType>();
		for (Type type : allTypes) {
			if (type instanceof MiscType) {
				rtn.add((MiscType) type);
			}
		}
		return rtn;
	}

	/**
	 *  Return all Xena File Types
	 */
	public List<XenaFileType> allXenaFileTypes() {
		List<XenaFileType> rtn = new ArrayList<XenaFileType>();
		for (Type type : allTypes) {
			if (type instanceof XenaFileType) {
				rtn.add((XenaFileType) type);
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
	public Type lookup(Class<?> cls) throws XenaException {
		Type rtn = clsMap.get(cls);
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
		Type rtn = nameMap.get(name);
		if (rtn == null) {
			throw new XenaException("Type Not Found: " + name);
		}
		return rtn;
	}

	/**
	 * Resolves a class name into its corresponding FileType
	 *
	 * @param  name  name of required Type
	 * @return Type corresponding to name
	 */
	public Type lookupByClassName(String name) throws XenaException {
		Type rtn = clsNameMap.get(name);
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
	public XenaFileType lookupXenaFileType(Class<?> cls) throws XenaException {
		XenaFileType rtn = (XenaFileType) clsMap.get(cls);
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
		XenaFileType rtn = (XenaFileType) nameMap.get(name);
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
		XenaFileType rtn = (XenaFileType) tagMap.get(tagName);
		if (rtn == null) {
			throw new XenaException("Tag Not Found: " + tagName);
		}
		return rtn;
	}

}
