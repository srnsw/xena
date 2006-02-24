package au.gov.naa.digipres.xena.plugin.html.javatools.util;
import java.io.*;
import java.net.*;

/**
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 * @author
 * @created    May 20, 2002
 * @version    1.0
 */

public class FileName {
	public static char STANDARDSEP = '/';
	public static char EXTENSIONSEP = '.';

	String name;

	public FileName(String name) {
		this.name = name;
	}

	public FileName(URL name) {
		this.name = name.getPath();
	}

	public FileName(File name) {
		this.name = name.toString();
	}

	public static String relativeTo(File d, File f) throws IOException {
		String rtn = relativeToHelper(d, f, "");
		if (0 < rtn.length() && rtn.charAt(0) == STANDARDSEP) {
			rtn = rtn.substring(1);
		}
		return rtn;
	}

	public static String relativeTo(String dirName, String fileName) throws
		IOException {
		File f = new File(fileName);
		File d = new File(dirName);
		return relativeTo(d, f).toString();
	}

	private static String relativeToHelper(File d, File f, String progress) throws
		IOException {
		if (f == null) {
			throw new IOException(d + " is not a parent");
		} else {
			if (f.equals(d)) {
				return progress;
			} else {
				return relativeToHelper(d, f.getParentFile(), progress) +
					STANDARDSEP + f.getName();
			}
		}
	}

	public static File changeRelative(File file, File origDir, File newDir) throws IOException {
		String relativePath = FileName.relativeTo(origDir, file);
		return new File(newDir, relativePath);
	}

	public static String dirName(String path) {
		StringBuffer buf = new StringBuffer(path);
		try {
			while (buf.charAt(buf.length() - 1) == STANDARDSEP) {
				buf.deleteCharAt(buf.length() - 1);
			} while (buf.charAt(buf.length() - 1) != STANDARDSEP) {
				buf.deleteCharAt(buf.length() - 1);
			} while (buf.charAt(buf.length() - 1) == STANDARDSEP) {
				buf.deleteCharAt(buf.length() - 1);
			}
			return buf.toString();
		} catch (StringIndexOutOfBoundsException x) {
			return "";
		}
	}

	public String extenstionNotNull() {
		String rtn = extension();
		if (rtn == null) {
			rtn = "";
		}
		return rtn;
	}

	public String noExtension() {
		String rtn = name;
		if (name != null) {
			int ind = name.lastIndexOf(EXTENSIONSEP);
			if (0 < ind) {
				rtn = name.substring(0, ind - 1);
			}
		}
		return rtn;
	}

	public String extension() {
		String rtn = null;
		if (name != null) {
			int ind = name.lastIndexOf(EXTENSIONSEP);
			if (0 < ind) {
				rtn = name.substring(ind + 1);
			}
		}
		return rtn;
	}
}
