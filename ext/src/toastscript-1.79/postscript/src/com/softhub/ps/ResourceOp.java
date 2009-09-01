
package com.softhub.ps;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

final class ResourceOp implements Stoppable, Types {

	private final static String OPNAMES[] = {
		"defineresource", "undefineresource", "findresource",
		"resourcestatus", "resourceforall", "findencoding"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, ResourceOp.class);
	}

	static void defineresource(Interpreter ip) {
		Any category = ip.ostack.pop(STRING | NAME);
		Any instance = popInstance(ip, category.toString());
		Any key = ip.ostack.pop();
		DictType resources = ip.getResources();
		DictType dict = (DictType) resources.get(category);
		if (dict == null) {
			DictType catdict = (DictType) resources.get("Category");
			dict = (DictType) catdict.get("Generic");
		}
		DictType statusdict = ip.getStatusDict();
		Any handler = statusdict.get("define" + category + "Resource");
		if (handler != null) {
			handler.cvx();
			ip.ostack.pushRef(instance);
			ip.ostack.pushRef(key);
			ip.estack.run(ip, handler);
			if (ip.ostack.popBoolean()) {
				ip.ostack.pushRef(category);
				ip.ostack.pushRef(instance);
				ip.ostack.pushRef(key);
				throw new Stop(UNDEFINEDRESOURCE, category.toString());
			}
		}
		dict.put(ip.vm, key, instance);
		ip.ostack.push(instance);
	}

	static void undefineresource(Interpreter ip) {
		String category = ip.ostack.popString();
		Any key = ip.ostack.pop();
		DictType resources = ip.getResources();
		resources.remove(ip.vm, key);
	}

	static void findresource(Interpreter ip) {
		Any category = ip.ostack.pop(STRING | NAME);
		Any key = ip.ostack.pop();
		DictType resources = ip.getResources();
		DictType cat = (DictType) resources.get(category);
		if (cat == null)
			throw new Stop(UNDEFINEDRESOURCE, category.toString());
		Any instance = cat.get(key);
		if (instance != null) {
			ip.ostack.push(instance);
		} else if (!handleFindResource(ip, category.toString(), key)) {
			// handler has altered the stack -> restore
			ip.ostack.pushRef(key);
			ip.ostack.pushRef(category);
			throw new Stop(UNDEFINEDRESOURCE, category.toString());
		}
	}

	static void findencoding(Interpreter ip) {
		Any encoding = ip.ostack.pop(STRING | NAME);
		DictType resources = ip.getResources();
		DictType cat = (DictType) resources.get("Encoding");
		ArrayType array = (ArrayType) cat.get(encoding, ARRAY);
		if (array != null) {
			ip.ostack.push(array);
		} else if (!handleFindResource(ip, "Encoding", encoding)) {
			// handler has altered the stack -> restore
			ip.ostack.pushRef(encoding);
			throw new Stop(UNDEFINEDRESOURCE, encoding.toString());
		}
	}

	static void resourceforall(Interpreter ip) {
		String category = ip.ostack.popString();
		Any scratch = ip.ostack.pop();
		ArrayType proc = (ArrayType) ip.ostack.pop(ARRAY);
		Any template = ip.ostack.pop();
	}

	static void resourcestatus(Interpreter ip) {
		Any category = ip.ostack.pop(STRING | NAME);
		Any key = ip.ostack.pop();
		DictType resources = ip.getResources();
		DictType cat = (DictType) resources.get(category);
		if (cat == null)
			throw new Stop(UNDEFINED, category.toString());
		Any rsrc = cat.get(category);
		if (rsrc != null) {
			// TODO: implement correct status
			ip.ostack.pushRef(new IntegerType(0));	// status
			ip.ostack.pushRef(new IntegerType(0));	// size
			ip.ostack.push(BoolType.TRUE);
		} else {
			ip.ostack.push(BoolType.FALSE);
		}
	}

	private static Any popInstance(Interpreter ip, String category) {
		Any instance;
		if ("Font".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else if ("Encoding".equals(category)) {
			instance = ip.ostack.pop(ARRAY);
		} else if ("Form".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else if ("Pattern".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else if ("ProcSet".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else if ("ColorSpace".equals(category)) {
			instance = ip.ostack.pop(ARRAY);
		} else if ("Halftone".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else if ("ColorRendering".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else if ("Filter".equals(category)) {
			instance = ip.ostack.pop(NAME | STRING);
		} else if ("ColorSpaceFamily".equals(category)) {
			instance = ip.ostack.pop(NAME | STRING);
		} else if ("Emulator".equals(category)) {
			instance = ip.ostack.pop(NAME | STRING);
		} else if ("IODevice".equals(category)) {
			instance = ip.ostack.pop(STRING);
		} else if ("ColorRenderingType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("FMapType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("FontType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("FormType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("HalftoneType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("ImageType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("PatternType".equals(category)) {
			instance = ip.ostack.pop(INTEGER);
		} else if ("Category".equals(category)) {
			instance = ip.ostack.pop(DICT);
		} else /* Generic */ {
			instance = ip.ostack.pop();
		}
		return instance;
	}

	static boolean handleFindResource(Interpreter ip, String category, Any key) {
		DictType statusdict = ip.getStatusDict();
		Any handler = statusdict.get("find" + category + "Resource");
		boolean found = false;
		if (handler != null) {
			handler.cvx();
			ip.ostack.pushRef(key);
			ip.estack.run(ip, handler);
			found = ip.ostack.popBoolean();
		}
		return found;
	}

}
