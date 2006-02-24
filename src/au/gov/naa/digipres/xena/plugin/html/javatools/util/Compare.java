package au.gov.naa.digipres.xena.plugin.html.javatools.util;

public class Compare {
	public static boolean eq(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		} else {
			return o1.equals(o2);
		}
	}

	public static int hashOne(Object o) {
		if (o == null) {
			return 0;
		} else {
			return o.hashCode();
		}
	}
}