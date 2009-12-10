
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

import java.io.File;
import com.softhub.ps.filter.Codec;
import com.softhub.ps.util.CharStream;

final class FileOp implements Stoppable, Types {

	private final static String PACKAGE = "com.softhub.ps";

	private final static String OPNAMES[] = {
		"file", "currentfile", "flush", "flushfile", "bytesavailable",
		"resetfile", "closefile", "read", "write", "readstring",
		"writestring", "readhexstring", "writehexstring", "readline",
		"run", "token", "status", "filter", "deletefile", "renamefile",
		"fileposition", "setfileposition"
	};

	static void install(Interpreter ip) {
		ip.installOp(OPNAMES, FileOp.class);
	}

	static void file(Interpreter ip) {
		StringType access = (StringType) ip.ostack.pop(STRING);
		StringType name = (StringType) ip.ostack.pop(STRING);
		if (access.length() != 1)
			throw new Stop(INVALIDFILEACCESS);
		int mode, m = access.get(0);
		switch (m) {
		case 'r':
			mode = FileType.READ_MODE;
			break;
		case 'w':
			mode = FileType.WRITE_MODE;
			break;
		default:
			throw new Stop(INVALIDFILEACCESS);
		}
		FileType file;
		if (name.equals("%stdin")) {
			if (m != 'r')
				throw new Stop(INVALIDFILEACCESS);
			file = ip.stdin;
		} else if (name.equals("%stdout")) {
			if (m != 'w')
				throw new Stop(INVALIDFILEACCESS);
			file = ip.stdout;
		} else if (name.equals("%stderr")) {
			if (m != 'w')
				throw new Stop(INVALIDFILEACCESS);
			file = ip.stderr;
		} else if (name.equals("%lineedit")) {
			if (m != 'r')
				throw new Stop(INVALIDFILEACCESS);
			file = ip.lineedit;
		} else {
			FileType currentFile = getCurrentFile(ip);
			file = new FileType(ip.vm, currentFile, ip.getCodeBase(), name, mode);
		}
		ip.ostack.pushRef(file);
	}

	static void currentfile(Interpreter ip) {
		ip.ostack.push(getCurrentFile(ip));
		ip.ostack.top().cvlit();
	}

	static FileType getCurrentFile(Interpreter ip) {
		int n = ip.estack.countto(FILE);
		FileType file;
		if (n >= ip.estack.count()) {
			file = ip.stdin;
		} else {
			file = (FileType) ip.estack.index(n);
		}
		return file;
	}

	static void flush(Interpreter ip) {
		ip.stdout.flush();
	}

	static void flushfile(Interpreter ip) {
		((FileType) ip.ostack.pop(FILE)).flush();
	}

	static void bytesavailable(Interpreter ip) {
		int count = ((FileType) ip.ostack.pop(FILE)).bytesavailable();
		ip.ostack.pushRef(new IntegerType(count));
	}

	static void resetfile(Interpreter ip) {
		((FileType) ip.ostack.pop(FILE)).resetfile(ip.getCodeBase());
	}

	static void closefile(Interpreter ip) {
		((FileType) ip.ostack.pop(FILE)).close();
	}

	static void read(Interpreter ip) {
		FileType file = (FileType) ip.ostack.pop(FILE);
		int c = file.read();
		if (c >= 0) {
			ip.ostack.pushRef(new IntegerType(c));
			ip.ostack.push(BoolType.TRUE);
		} else {
			ip.ostack.push(BoolType.FALSE);
		}
	}

	static void write(Interpreter ip) {
		int c = ip.ostack.popInteger();
		FileType file = (FileType) ip.ostack.pop(FILE);
		file.write(c);
	}

	static void readstring(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		FileType file = (FileType) ip.ostack.pop(FILE);
		StringType result[] = new StringType[1];
		boolean not_eof = file.read(ip.vm, s, result);
		ip.ostack.pushRef(result[0]);
		ip.ostack.pushRef(new BoolType(not_eof));
	}

	static void writestring(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		FileType file = (FileType) ip.ostack.pop(FILE);
		file.write(s);
	}

	static void readhexstring(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		FileType file = (FileType) ip.ostack.pop(FILE);
		StringType result[] = new StringType[1];
		boolean not_eof = file.readhex(ip.vm, s, result);
		ip.ostack.pushRef(result[0]);
		ip.ostack.pushRef(new BoolType(not_eof));
	}

	static void writehexstring(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		FileType file = (FileType) ip.ostack.pop(FILE);
		file.writehex(s);
	}

	static void readline(Interpreter ip) {
		StringType s = (StringType) ip.ostack.pop(STRING);
		FileType file = (FileType) ip.ostack.pop(FILE);
		StringType result[] = new StringType[1];
		boolean not_eof = file.readline(ip.vm, s, result);
		ip.ostack.pushRef(result[0]);
		ip.ostack.pushRef(new BoolType(not_eof));
	}

	static void run(Interpreter ip) {
		ip.ostack.top(STRING);
		ip.ostack.pushRef(new StringType(ip.vm, "r"));
		ip.estack.pushRef(ip.systemdict.get("exec"));
		ip.estack.pushRef(ip.systemdict.get("cvx"));
		ip.estack.pushRef(ip.systemdict.get("file"));
	}

	static void token(Interpreter ip) {
		CharSequenceType cs = (CharSequenceType) ip.ostack.pop(FILE | STRING);
		ip.ostack.pushRef(new BoolType(cs.token(ip)));
	}

	static void status(Interpreter ip) {
		Any any = ip.ostack.pop(FILE | STRING);
		if (any instanceof FileType) {
			FileType file = (FileType) any;
			ip.ostack.pushRef(new BoolType(!file.isClosed()));
		} else {
			StringType name = (StringType) any;
			File file = new File(name.toString());
			if (file.isFile()) {
				int len = (int) file.length();
				ip.ostack.pushRef(new IntegerType((len + 1023) / 1024));
				ip.ostack.pushRef(new IntegerType(len));
				ip.ostack.pushRef(new IntegerType((int) file.lastModified()));
				ip.ostack.pushRef(new IntegerType((int) file.lastModified())); // TODO: date created!
				ip.ostack.pushRef(new BoolType(true));
			} else {
				ip.ostack.pushRef(new BoolType(false));
			}
		}
	}

	static void fileposition(Interpreter ip) {
		FileType file = (FileType) ip.ostack.pop(FILE);
		int pos = file.getFilePosition();
		ip.ostack.pushRef(new IntegerType(pos));
	}

	static void setfileposition(Interpreter ip) {
		int pos = ip.ostack.popInteger();
		FileType file = (FileType) ip.ostack.pop(FILE);
		file.setFilePosition(pos);
	}

	static void filter(Interpreter ip) {
		String name = ip.ostack.pop(STRING | NAME).toString();
		int len = name.length();
		String codecName;
		int mode;
		if (name.endsWith("Encode")) {
			codecName = name.substring(0, len - "Encode".length());
			mode = CharStream.WRITE_MODE;
		} else if (name.endsWith("Decode")) {
			codecName = name.substring(0, len - "Decode".length());
			mode = CharStream.READ_MODE;
		} else {
			throw new Stop(UNDEFINED, name);
		}
		Codec codec = null;
		try {
			Class clazz = Class.forName(PACKAGE + ".filter." + codecName + "Codec");
			codec = (Codec) clazz.newInstance();
		} catch (Exception ex) {
			throw new Stop(UNDEFINED, codecName + " not found");
		}
		// pop optional parameters
		Class types[] = codec.getOptionalParameterTypes();
		if (types != null) {
			int i, n = types.length;
			Object parameter[] = new Object[n];
			for (i = n-1; i >= 0; i--) {
				parameter[i] = ip.ostack.pop().cvj();
			}
			try {
				codec.setOptionalParameters(parameter);
			} catch (ClassCastException ex) {
				throw new Stop(TYPECHECK, ex.getMessage());
			}
		}
		Any any = ip.ostack.pop(FILE | STRING | ARRAY);
		if (any instanceof StringType)
			throw new Stop(INTERNALERROR, "not yet implemented: " + any.typeName());
		if (any instanceof ArrayType)
			throw new Stop(ARRAY, "not yet implemented: " + any.typeName());
		FileType file = (FileType) any;
		ip.ostack.pushRef(new FilterType(ip.vm, file, codec, mode));
	}

	static void deletefile(Interpreter ip) {
		String fileName = ip.ostack.popString();
		File file = new File(fileName);
		try {
			if (!file.delete()) {
				throw new Stop(UNDEFINEDFILENAME, fileName);
			}
		} catch (SecurityException ex) {
			throw new Stop(INVALIDFILEACCESS, fileName);
		}
	}

	static void renamefile(Interpreter ip) {
		String newName = ip.ostack.popString();
		String oldName = ip.ostack.popString();
		File oldFile = new File(oldName);
		File newFile = new File(newName);
		try {
			if (!oldFile.renameTo(newFile)) {
				throw new Stop(UNDEFINEDFILENAME, oldName + " " + newName);
			}
		} catch (SecurityException ex) {
			throw new Stop(INVALIDFILEACCESS, oldName + " " + newName);
		}
	}

}
