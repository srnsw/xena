
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

import java.lang.reflect.*;

public class ReflectionOperator extends OperatorType {

	private static Object param[] = new Object[1];

	private Class clazz;
	private Method method;

	public ReflectionOperator(String name, Class clazz) throws NoSuchMethodException {
		super(name);
		this.clazz = clazz;
		Class paramTypes[] = new Class[1];
		paramTypes[0] = Interpreter.class;
		this.method = clazz.getDeclaredMethod(name, paramTypes);
	}

	public void exec(Interpreter ip) {
		try {
			synchronized (param) {
				param[0] = ip;
				method.invoke(clazz, param);
			}
		} catch (InvocationTargetException ex) {
			Throwable tex = ex.getTargetException();
			if (tex instanceof Stop)
				throw (Stop) tex;
			System.err.println("internal error in " + method);
			tex.printStackTrace();
			throw new Stop(INTERNALERROR, ex + " target: " + tex + " method: " + method);
		} catch (IllegalAccessException ex) {
			throw new Stop(INTERNALERROR, "exec failed: " + ex.getMessage());
		}
	}

}
