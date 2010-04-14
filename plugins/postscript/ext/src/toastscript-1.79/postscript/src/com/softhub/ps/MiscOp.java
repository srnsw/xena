
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

import java.util.Enumeration;
import java.util.Properties;
import com.softhub.ps.device.CacheDevice;
import com.softhub.ps.device.Device;

final class MiscOp implements Stoppable, Types {

	private final static String COPYRIGHT = "Copyright 1998 - 2005 by Christian Lehner";

	private final static String OPNAMES[] = {
		"handleerror", "executive", "usertime", "realtime",
		"setsystemparams", "currentsystemparams", "setuserparams",
		"currentuserparams", "showpage", "copypage", "erasepage",
		"print", "internaldict", "quit", "$$print", "$$println",
		"$$lineno", "$$currentlineno", "$$break"
	};

	private final static String STATUSDICT_OPNAMES[] = {
		"beginjob", "endjob"
//		"setduplexmode"
	};

	private static DictType systemparams;
	private static DictType userparams;
	private static DictType internals;

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, MiscOp.class);
		ip.installOp(STATUSDICT_OPNAMES, MiscOp.class, ip.getStatusDict());
		systemparams = new DictType(ip.vm, 10);
		userparams = new DictType(ip.vm, 10);
		internals = new DictType(ip.vm, 10);
	}

	static void handleerror(Interpreter ip) {
		// tell the device that an error occured
		DictType serror = (DictType) ip.systemdict.get("$error");
		String msg = serror != null ? serror.get("errorname").toString() : "unknown";
		Device device = ip.getGraphicsState().currentdevice();
		device.error(msg);
		// call the error handler defined in server script
		DictType errordict = (DictType) ip.systemdict.get("errordict");
		Any val = errordict.get("handleerror");
		if (val == null)
			throw new Stop(INTERNALERROR, "handleerror undefined");
		ip.estack.push(val);
	}

	static void executive(Interpreter ip) {
		// TODO: not yet implemented
	}

	static void usertime(Interpreter ip) {
		ip.ostack.push(new IntegerType(ip.usertime()));
	}

	static void realtime(Interpreter ip) {
		ip.ostack.push(new IntegerType(ip.usertime()));
	}

	static void setsystemparams(Interpreter ip) {
		DictType dict = (DictType) ip.ostack.pop(DICT);
		GraphicsState gstate = ip.getGraphicsState();
		Enumeration keys = dict.keys();
		Enumeration vals = dict.elements();
		while (keys.hasMoreElements()) {
			Any key = (Any) keys.nextElement();
			Any val = (Any) vals.nextElement();
			if (key instanceof NameType) {
				NameType name = (NameType) key;
				if (name.equals("vm.string.bug")) {
					if (val instanceof BoolType) {
						ip.vm.setStringBug(((BoolType) val).booleanValue());
					}
				} else if (name.equals("debug")) {
					debug(val, true);
				} else if (name.equals("nodebug")) {
					debug(val, false);
				} else if (name.equals("bitmapwidths")) {
					gstate.setRequestBitmapWidths(ip, val);
				} else if (name.equals("systemfontdict")) {
					gstate.setSystemFonts(ip, val);
				}
			}
		}
	}

	static void currentsystemparams(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		StringType server = new StringType(ip.vm, ip.getServer());
		systemparams.put(ip.vm, "server", server);
		systemparams.put(ip.vm, "bitmapwidths", new BoolType(gstate.getRequestBitmapWidths()));
		DictType sysfonts = gstate.getSystemFonts();
		if (sysfonts != null) {
			systemparams.put(ip.vm, "systemfontdict", sysfonts);
		}
		systemparams.put(ip.vm, "vm.string.bug", new BoolType(ip.vm.getStringBug()));
		try {
			Properties prop = System.getProperties();
			Enumeration e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String val = prop.getProperty(key);
				systemparams.put(ip.vm, key, new StringType(ip.vm, val));
			}
		} catch (SecurityException ex) {}
		systemparams.put(ip.vm, "copyright", new StringType(ip.vm, COPYRIGHT));
		Runtime runtime = Runtime.getRuntime();
		systemparams.put(ip.vm, "free.memory", new IntegerType((int) runtime.freeMemory()));
		systemparams.put(ip.vm, "total.memory", new IntegerType((int) runtime.totalMemory()));
		ip.ostack.pushRef(systemparams);
	}

	static void setuserparams(Interpreter ip) {
		DictType dict = (DictType) ip.ostack.pop(DICT);
		Enumeration keys = dict.keys();
		Enumeration vals = dict.elements();
		while (keys.hasMoreElements()) {
			Any key = (Any) keys.nextElement();
			Any val = (Any) vals.nextElement();
			if (key instanceof NameType) {
				NameType name = (NameType) key;
				if (name.equals("placeholder")) {
					// There are no userparams yet; the value of
					// "placeholder" would be set here.
				}
			}
		}
	}

	static void currentuserparams(Interpreter ip) {
		ip.ostack.pushRef(userparams);
	}

	static void internaldict(Interpreter ip) {
		Any magic = ip.ostack.pop(INTEGER);
		// TODO: do we need to fail for wrong number?
		ip.ostack.pushRef(internals);
	}

	static void showpage(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		Device device = gstate.currentdevice();
		Any languagelevel = ip.dstack.load("languagelevel");
		int level = 0;
		if (languagelevel instanceof IntegerType) {
			level = ((IntegerType) languagelevel).intValue();
		}
		if (level < 2) {
			int copies = 1;
			Any any = ip.dstack.load("#copies");
			if (any instanceof IntegerType) {
				copies = ((IntegerType) any).intValue();
			}
			device.showpage();
			for (int i = 1; i < copies; i++) {
				device.copypage();
			}
		} else {
		    device.showpage();
		}
		gstate.initgraphics();
	}

	static void copypage(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		Device device = gstate.currentdevice();
		device.copypage();
	}

	static void erasepage(Interpreter ip) {
		GraphicsState gstate = ip.getGraphicsState();
		Device device = gstate.currentdevice();
		device.erasepage();
	}

	static void setduplexmode(Interpreter ip) {
		boolean mode = ip.ostack.popBoolean();
	}

	static void print(Interpreter ip) {
		ip.stdout.print((StringType) ip.ostack.pop(STRING));
	}

	static void $$print(Interpreter ip) {
		System.out.print(ip.ostack.pop());
	}

	static void $$println(Interpreter ip) {
		System.out.println(ip.ostack.pop());
	}

	static void $$lineno(Interpreter ip) {
		Any any = ip.ostack.pop();
		ip.ostack.pushRef(new IntegerType(any.getLineNo()));
	}

	static void $$currentlineno(Interpreter ip) {
		ip.ostack.pushRef(new IntegerType(ip.lineno));
	}

	static void $$break(Interpreter ip) {
		try {
			byte cmd[] = new byte[64];
			do {
				print("break at " + ip.estack.top() + "\n");
				print("debug> ");
				int len = System.in.read(cmd);
				switch (cmd[0]) {
				case 'o':
					printStack("ostack", ip, ip.ostack, cmd, len);
					break;
				case 'd':
					printStack("dstack", ip, ip.dstack, cmd, len);
					break;
				case 'e':
					printStack("estack", ip, ip.estack, cmd, len);
					break;
				case 's':
					Any any = ip.estack.pop();
					println("step: " + any);
					ip.estack.run(ip, any);
					break;
				case 'n':
					Any next = ip.estack.pop();
					if (next.typeID() == ARRAY && next.isExecutable()) {
						ArrayType proc = (ArrayType) next;
					    println("proc: " + proc);
						int n = proc.length();
						if (n > 0) {
							if (n > 1) {
							    ip.estack.pushRef(proc.getinterval(1, n-1));
							}
						    next = proc.get(0);
							println("next: " + next);
							if (next.typeID() == ARRAY && next.isExecutable()) {
								ip.ostack.pushRef(next);
							} else {
							    next.exec(ip);
							}
						}
					} else {
						next.exec(ip);
					}
					break;
				case 'c':
					println("continue");
					cmd = null;
					break;
				case 'h':
					printDebugHelp();
					break;
				case 'q':
					println("quit");
					System.exit(0);
					break;
				}
			} while (cmd != null);
		} catch (Exception ex) {
			println("error: " + ex);
		}
	}

	private static void printStack(String msg, Interpreter ip, Stack stack, byte cmd[], int len) {
		int index = -1;
		String exe = null;
		if (len >= 2) {
			int n = len-2, m;
			String s = new String(cmd, 1, n);
			m = s.indexOf(",");
			if (m < 0) {
				m = n;
			} else {
				exe = s.substring(m+1, n);
				s = s.substring(0, m);
			}
			try {
			    index = Integer.valueOf(s).intValue();
			} catch (NumberFormatException ex) {}
		}
		if (index >= 0) {
			Any any = stack.index(index);
			println(msg + "[" + index + "] " + any);
			if (exe != null) {
				ip.ostack.push(any);
				ip.estack.run(ip, new StringType(ip.vm, exe).cvx());
			}
		} else {
			print(msg + ":");
			if (stack.count() > 0) {
				println("");
			    stack.print(System.out);
			} else {
				println(" empty");
			}
		}
	}

	private static void printDebugHelp() {
		println("[ode]<index> : print element at index");
		println("[ode]<index>,ps-code : push and exec ps-code");
		println("d0,{== ==} forall : dump current dictionary");
		println("o : dump operand stack");
		println("d : dump dictionary stack");
		println("e : dump execution stack");
		println("s : single step (over)");
		println("n : next step (into)");
		println("c : continue");
		println("h : help");
		println("q : quit");
	}

	private static void println(String msg) {
		print(msg + "\n");
	}

	private static void print(String msg) {
		System.out.print(msg);
	}

	static void beginjob(Interpreter ip) {
		Device device = ip.getGraphicsState().currentdevice();
		device.beginJob();
	}

	static void endjob(Interpreter ip) {
		Device device = ip.getGraphicsState().currentdevice();
		device.endJob();
		CacheDevice cachedevice = ip.getGraphicsState().cachedevice();
		if (cachedevice != null) {
			cachedevice.clearCache();
		}
		if (ip.isDebugMode()) {
			NameType.printStatistics();
		}
		System.gc();
	}

	static void quit(Interpreter ip) {
		FileType.flushAllOpenFiles();
		System.exit(0);
	}

	private static void debug(Any classname, boolean state) {
		try {
			Class clazz = Class.forName(classname.toString());
			clazz.getField("debug").setBoolean(clazz, state);
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}

}
