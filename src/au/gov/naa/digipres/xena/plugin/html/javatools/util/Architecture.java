package au.gov.naa.digipres.xena.plugin.html.javatools.util;

public class Architecture {
//	public static final String WINDOWS = "win";
//	public static final String LINUX = "lin";

	static public String os;
	static public String machine;

	static {
//		String jversion = System.getProperty("java.version");
		machine = System.getProperty("os.arch").toLowerCase();
		if (4 < machine.length()) {
			machine = machine.substring(0, 4);
		}
		os = System.getProperty("os.name").toLowerCase();
		if (3 < os.length()) {
			os = os.substring(0, 3);
		}
/*		if (farch.matches("i386")) {
			farch = "x86";
		} */
/*		if (fos.matches("windows.*")) {
			os = WINDOWS;
		}
		if (fos.matches("linux.*")) {
			os = LINUX;
		} */
	}

	public static String getOs() {
		return os;
	}

	public static String getMachine() {
		return machine;
	}

	public static String get() {
		return os + machine;
	}

	public static void main(String[] args) {
		System.out.println("arch = "+ System.getProperty("os.arch"));
		System.out.println("os = "+ System.getProperty("os.name"));
		System.out.println("aall = " + Architecture.get());

//		System.out.println(Architecture.get());
	}
}