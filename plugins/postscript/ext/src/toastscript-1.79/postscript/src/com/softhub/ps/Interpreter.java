
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
 *
 * To instanciate the interpreter, pass the stdin
 * and stdout to the constructor and call init when
 * ready to begin execution. Once init is called, the
 * server is read from the file "server.ps". The interpreter
 * runs in its own thread and starts listening on stdin.
 * This behavour can be customized in the "server.ps"
 * script. To communicate with the interpreter, write
 * PostScript code to stdin. Currently, the job server
 * is part of the statusdict (see server.ps). If you do
 * not use it, there will be no save/restore between jobs.
 * The client code should create a page device and implement
 * the PageEventListener interface to get notified of changes
 * to the page device. The client can keep a list of pages
 * and draw the appropriate page.
 */

import com.softhub.ps.image.ImageDataProducer;
import com.softhub.ps.device.PageDevice;
import com.softhub.ps.util.CharStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;

public class Interpreter implements Runnable, Stoppable, Types, ImageDataProducer {

	/**
	 * Debug flag.
	 */
	private boolean debug;

	/**
	 * Interpreter thread
	 */
	private Thread thread;

	/**
	 * The base time.
	 */
	private long basetime;

	/**
	 * The resource dictionary.
	 */
	private DictType resources;

	/**
	 * Virtual Memory
	 */
	VM vm = new VM();

	/**
	 * System dictionary.
	 */
	DictType systemdict = new DictType(vm, 300, true);

	/**
	 * Operand Stack
	 */
	Stack ostack = new OpStack();

	/**
	 * Execution Stack
	 */
	ExStack estack = new ExStack(250);

	/**
	 * Dictionary Stack
	 */
	DictStack dstack = new DictStack(20);

	/**
	 * Graphics State Stack
	 */
	GStack gstack = new GStack(32);

	/**
	 * Current Packing Mode
	 */
	boolean arraypacking;

	/**
	 * %stdin
	 */
	FileType stdin;

	/**
	 * %stdin
	 */
	FileType stdout;

	/**
	 * %stdin
	 */
	FileType stderr;

	/**
	 * %lineedit
	 */
	FileType lineedit;

	/**
	 * The current line number.
	 */
	int lineno;

	/**
	 * Construct a ps-interpreter.
	 */
	public Interpreter() {
		this(System.in, System.out);
	}

	/**
	 * Construct a ps-interpreter.
	 * @param in the standard input stream
	 * @param out the standard output stream
	 */
	public Interpreter(InputStream in, OutputStream out) {
		this(in, out, System.err);
	}

	/**
	 * Construct a ps-interpreter.
	 * @param in the standard input stream
	 * @param out the standard output stream
	 * @param err the standard error stream
	 */
	public Interpreter(InputStream in, OutputStream out, OutputStream err) {
		stdin = new SpecialFileType(vm, "%stdin", in);
		stdout = new SpecialFileType(vm, "%stdout", out);
		stderr = new SpecialFileType(vm, "%stderr", err);
		lineedit = new SpecialFileType(vm, "%lineedit", System.in);
		dstack.push(systemdict);
		GStateType gstate = new GraphicsState();
		gstack.pushRef(gstate);
		gstate.save(vm.getSaveLevel());
		initResources();
		installOp();
	}

	/**
	 * Initialize the interpreter.
	 * @param device the default output device
	 */
	public void init(PageDevice device) {
		basetime = System.currentTimeMillis();
		GraphicsState gstate = getGraphicsState();
		device.addPageEventListener(gstate);
		gstate.install(device);
		String server = getServer();
		// read the server loop
		try {
			run(server);
		} catch (Stop ex) {
			System.err.println("Error in job server: " + ex + " line: " + lineno);
		} catch (Throwable ex) {
			System.err.println("init failed " + server);
			ex.printStackTrace();
		}
		// set the dict stack bottom
		dstack.setBottom();
		DictType statusdict = getStatusDict();
		Any key = statusdict.get("debug");
		debug = key instanceof BoolType && ((BoolType) key).booleanValue();
		// start the interpreter
		systemdict.readonly();
		estack.pushRef(new NameType("start").cvx());
		thread = new Thread(this, "ps-interpreter");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	/**
	 * Initialize the resource dictionary.
	 */
	private void initResources() {
		resources = new DictType(vm, 22);
		resources.put(vm, "Font", new DictType(vm, 35));
		resources.put(vm, "Encoding", new DictType(vm, 5));
		resources.put(vm, "Form", new DictType(vm, 5));
		resources.put(vm, "Pattern", new DictType(vm, 5));
		resources.put(vm, "ProcSet", new DictType(vm, 5));
		resources.put(vm, "ColorSpace", new DictType(vm, 5));
		resources.put(vm, "Halftone", new DictType(vm, 5));
		resources.put(vm, "ColorRendering", new DictType(vm, 5));
		resources.put(vm, "Filter", new DictType(vm, 5));
		resources.put(vm, "ColorSpaceFamily", new DictType(vm, 5));
		resources.put(vm, "Emulator", new DictType(vm, 5));
		resources.put(vm, "IODevice", new DictType(vm, 5));
		resources.put(vm, "ColorRenderingType", new DictType(vm, 5));
		resources.put(vm, "FMapType", new DictType(vm, 5));
		resources.put(vm, "FontType", new DictType(vm, 5));
		resources.put(vm, "FormType", new DictType(vm, 5));
		resources.put(vm, "HalftonType", new DictType(vm, 5));
		resources.put(vm, "ImageType", new DictType(vm, 5));
		resources.put(vm, "PatternType", new DictType(vm, 5));
		DictType catdict = new DictType(vm, 22);
		catdict.put(vm, "Generic", new DictType(vm, 10));
		resources.put(vm, "Category", catdict);
	}

	/**
	 * Save the graphics context.
	 */
	void gsave() {
		GStateType gstate = (GStateType) gstack.dup();
		int savelevel = vm.getSaveLevel();
		gstate.save(savelevel);
	}

	/**
	 * Restore the graphics context.
	 */
	void grestore() {
		if (gstack.count() < 1)
			throw new Stop(INTERNALERROR, "grestore: no initial gstate");
		if (gstack.count() >= 2) {
			GStateType gstate = (GStateType) gstack.pop();
			GStateType base = (GStateType) gstack.top();
			int savelevel = gstate.getSaveLevel();
			if (base.getSaveLevel() < savelevel) {
				base = (GStateType) gstack.dup();
				base.setSaveLevel(savelevel);
			}
			base.restore(gstate);
		}
	}

	/**
	 * Restore the graphics context down to the initial state, which
	 * is the state of the last recent save.
	 */
	void grestoreAll() {
		int count = gstack.count();
		if (count < 1)
			throw new Stop(INTERNALERROR, "grestoreall: no initial gstate");
		if (count >= 2) {
			int savelevel = vm.getSaveLevel();
			GStateType currentstate = (GStateType) gstack.pop();
			GStateType gstate = null;
			do {
				gstate = (GStateType) gstack.pop();
			} while (--count > 0 && gstate.getSaveLevel() > savelevel);
			gstack.pushRef(gstate);
			if (savelevel > gstate.getSaveLevel()) {
				gstate = (GStateType) gstack.dup();
				gstate.setSaveLevel(savelevel);
			}
			gstate.restore(currentstate);
		}
	}

	/**
	 * @return the graphics context
	 */
	public GraphicsState getGraphicsState() {
		return (GraphicsState) gstack.top();
	}

	/**
	 * @return the debug mode as defined in statusdict
	 */
	public boolean isDebugMode() {
		return debug;
	}

	/**
	 * @return the server name
	 */
	public String getServer() {
		return "server.ps";
	}

	/**
	 * @return the code base
	 */
	public URL getCodeBase() {
		Any any = getStatusDict().get("codebase");
		if (any != null) {
			try {
				return new URL(any.toString());
			} catch (MalformedURLException ex) {}
		}
		return null;
	}

	/**
	 * Install operators in systemdict
	 */
	private void installOp() {
		systemdict.put(vm, "null", new NullType());
		systemdict.put(vm, "true", BoolType.TRUE);
		systemdict.put(vm, "false", BoolType.FALSE);
		systemdict.put(vm, "systemdict", systemdict);
		systemdict.put(vm, "globaldict", new DictType(vm, 20, true));
		systemdict.put(vm, "GlobalFontDirectory", new DictType(vm, 35, true));
		systemdict.put(vm, "userdict", new DictType(vm, 200));
		systemdict.put(vm, "errordict", new DictType(vm, 20));
		systemdict.put(vm, "$error", new DictType(vm, 20));
		systemdict.put(vm, "FontDirectory", new DictType(vm, 35));
		systemdict.put(vm, "statusdict", new DictType(vm, 10));
		ArithOp.install(this);
		ArrayOp.install(this);
		BoolOp.install(this);
		ControlOp.install(this);
		DictOp.install(this);
		FileOp.install(this);
		FontOp.install(this);
		GStateOp.install(this);
		ImageOp.install(this);
		MatrixOp.install(this);
		MiscOp.install(this);
		PathOp.install(this);
		ResourceOp.install(this);
		StackOp.install(this);
		StringOp.install(this);
		TypeOp.install(this);
		VMOp.install(this);
	}

	/**
	 * Install operators in systemdict
	 * @param op the operator
	 */
	public void installOp(OperatorType op) {
		installOp(op, systemdict);
	}

	/**
	 * Install operator into some dictionary
	 * @param op the operator
	 * @param dict the dictionary
	 */
	public void installOp(OperatorType op, DictType dict) {
		dict.put(vm, op.toString(), op);
	}

	/**
	 * Install operators in systemdict
	 * @param names the names of the operators
	 * @param clazz the implementing class
	 */
	public void installOp(String names[], Class clazz) {
		installOp(names, clazz, systemdict);
	}

	/**
	 * Install operators into dict
	 * @param names the names of the operators
	 * @param clazz the implementing class
	 * @param dict the dictionary to install operators into
	 */
	public void installOp(String names[], Class clazz, DictType dict) {
		for (int i = 0; i < names.length; i++) {
			try {
				dict.put(vm, names[i], new ReflectionOperator(names[i], clazz));
			} catch (Exception ex) {
				System.err.println("Interpreter.installOp(" + names[i] + ") " + ex);
			}
		}
	}

	/**
	 * @return the status dictionary
	 */
	public DictType getStatusDict() {
		return (DictType) systemdict.get("statusdict");
	}

	/**
	 * @return the status dictionary
	 */
	public DictType getResources() {
		return resources;
	}

	/**
	 * Interrupt the execution as soon as possible.
	 * @param state if true halt else clear pending interrupt
	 */
	public void interrupt(boolean state) {
		estack.interrupt(state);
	}

	/**
	 * @return the time we are running im milliseconds
	 */
	public int usertime() {
		return (int) (System.currentTimeMillis() - basetime);
	}

	/**
	 * Run until the execution stack is empty.
	 */
	public void run() {
		estack.run(this);
	}

	/**
	 * Run a file.
	 * @param filename the file to run
	 */
	public void run(String filename) {
		ostack.pushRef(new StringType(vm, filename));
		estack.push(systemdict.get("run"));
		estack.run(this);
	}

	/**
	 * Load object in the context of the dict stack
	 * and return the object if found, null otherwise.
	 * @param name the name of the object to look up
	 * @return the value bound to name
	 */
	public Any load(String name) {
		return dstack.load(name);
	}

	/**
	 * Execute the string parameter.
	 * @param s some ps commands
	 */
	public void exec(String s) {
		estack.run(this, new StringType(vm, s).cvx());
	}

	/**
	 * Execute the image source procedure.
	 * @return the result of the image procedure
	 */
	public CharStream getImageData(Object proc) {
		estack.run(this, (ArrayType) proc);
		return (CharStream) ostack.pop(STRING);
	}

	/**
	 * Parse input for next token.
	 * @param scanner the scanner object.
	 * @param execute flag which indicates if we do "token" or "exec".
	 * @return token.
	 */
	int scan(CharSequenceType cs, Scanner scanner, boolean execute) {
		int token;
		boolean defered = false;
		do {
			this.lineno = cs.getLineNo();
			token = scanner.token(cs);
			defered = scanner.defered();
			switch (token) {
			case Scanner.EOF:
				if (defered)
					throw new Stop(SYNTAXERROR, "scan");
				break;
			case Scanner.LITERAL:
				pushLiteral(scanner);
				break;
			case Scanner.IDENT:
				pushIdent(scanner, defered || !execute);
				break;
			case Scanner.IMMEDIATE:
				immediateToken(scanner);
				break;
			case Scanner.PROC_BEGIN:
				ostack.pushRef(new MarkType());
				break;
			case Scanner.PROC_END:
				pushProc(ostack);
				break;
			case Scanner.NUMBER:
				pushNumber(scanner.getNumber());
				break;
			case Scanner.STRING:
				pushString(scanner);
				break;
			default:
				throw new Stop(INTERNALERROR, "error in scanner -> " + token);
			}
		} while (defered);
		return token;
	}

	/**
	 * Push a literal onto the stack.
	 */
	private void pushLiteral(Scanner scanner) {
		NameType name = new NameType(scanner.getString());
		name.setLineNo(lineno);
		ostack.pushRef(name);
	}

	/**
	 * Push an identifier onto the stack.
	 * @param mode the execution mode
	 */
	private void pushIdent(Scanner scanner, boolean deferred) {
		Any name = new NameType(scanner.getString()).cvx();
		name.setLineNo(lineno);
		if (deferred) {
			ostack.pushRef(name);
		} else {
			estack.pushRef(name);
		}
	}

	/**
	 * Make a procedure from objects on the stack.
	 * @param stack the stack which holds the elements for procedure.
	 */
	private void pushProc(Stack stack) {
		int n = stack.counttomark();
		ArrayType array = new ArrayType(vm, n, stack);
		array.setPacked(arraypacking);
		array.setLineNo(lineno);
		stack.remove(n+1);
		ostack.pushRef(array.cvx());
	}

	/**
	 * Immediatly load a token.
	 */
	private void immediateToken(Scanner scanner) {
		NameType key = new NameType(scanner.getString());
		Any val = dstack.load(key);
		if (val == null)
			throw new Stop(UNDEFINED, key.toString());
		val.setLineNo(lineno);
		ostack.push(val);
	}

	/**
	 * Push a NumberType onto operand stack.
	 * @param val the number to push.
	 */
	private void pushNumber(Number val) {
		NumberType num;
		if (val instanceof Integer) {
			num = new IntegerType(val.intValue());
		} else {
			num = new RealType(val.doubleValue());
		}
		num.setLineNo(lineno);
		ostack.pushRef(num);
	}

	/**
	 * Push a string onto operand stack.
	 */
	private void pushString(Scanner scanner) {
		StringType s = new StringType(vm, scanner.getString());
		s.setLineNo(lineno);
		ostack.pushRef(s);
	}

}
