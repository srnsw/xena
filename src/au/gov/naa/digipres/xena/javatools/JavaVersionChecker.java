package au.gov.naa.digipres.xena.javatools;
import java.util.StringTokenizer;

public class JavaVersionChecker {
	final static int TYPE = 0;

	final static int MAJOR = 1;

	final static int MINOR = 2;

	final static int PATCH = 3;

	String version;

	/*
	 *  int type = -1;
	 *  int major = -1;
	 *  int minor = -1;
	 *  int patch = -1;
	 */

	public JavaVersionChecker() {
		version = System.getProperty("java.version");
	}

	public String getVersion() {
		return version;
	}

	public boolean checkMinimum(String ver) {
		int[] n = stringToVersion(ver);
		return checkMinimum(n);
	}

	public boolean checkMinimum(int[] cn) {
		try {
			int[] n = stringToVersion(version);
			checkOne(cn[TYPE], n[TYPE]);
			checkOne(cn[MAJOR], n[MAJOR]);
			checkOne(cn[MINOR], n[MINOR]);
			checkOne(cn[PATCH], n[PATCH]);
		} catch (OkException x) {
			return true;
		} catch (FailException x) {
			return false;
		}
		return true;
	}

	int oneToString(StringTokenizer st) {
		int rtn = -1;
		if (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s != null) {
				try {
					rtn = Integer.parseInt(s);
				} catch (NumberFormatException x) {
					// Probably a beta build or something
					rtn = -1;
				}
			}
		}
		return rtn;
	}

	int[] stringToVersion(String ver) {
		int[] rtn = new int[4];
		StringTokenizer st = new StringTokenizer(ver, "._-");
		rtn[TYPE] = oneToString(st);
		rtn[MAJOR] = oneToString(st);
		rtn[MINOR] = oneToString(st);
		rtn[PATCH] = oneToString(st);
		return rtn;
	}

	void checkOne(int standard, int value) throws OkException, FailException {
		if (standard == value) {
			return;
		} else if (standard < 0 || standard < value) {
			throw new OkException();
		} else {
			throw new FailException();
		}
	}

	class OkException extends Exception {
	}

	class FailException extends Exception {
	}
}
