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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

package au.gov.naa.digipres.xena.plugin.html.javatools.util;

public class Architecture {
	// public static final String WINDOWS = "win";
	// public static final String LINUX = "lin";

	static public String os;
	static public String machine;

	static {
		// String jversion = System.getProperty("java.version");
		machine = System.getProperty("os.arch").toLowerCase();
		if (4 < machine.length()) {
			machine = machine.substring(0, 4);
		}
		os = System.getProperty("os.name").toLowerCase();
		if (3 < os.length()) {
			os = os.substring(0, 3);
		}
		/*
		 * if (farch.matches("i386")) { farch = "x86"; }
		 */
		/*
		 * if (fos.matches("windows.*")) { os = WINDOWS; } if (fos.matches("linux.*")) { os = LINUX; }
		 */
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
		System.out.println("arch = " + System.getProperty("os.arch"));
		System.out.println("os = " + System.getProperty("os.name"));
		System.out.println("aall = " + Architecture.get());

		// System.out.println(Architecture.get());
	}
}
