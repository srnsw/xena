package au.gov.naa.digipres.xena.plugin.html.javatools.util;

/**
 * A utility class for substituting parts of Strings
 * e.g.
 * SubstituteVariable("${xx} brown fox jumped over ${xx} lazy dog",
 *      "${xx}", "the", 1); =>>
 * "the quick brown fox jumped over ${xx} lazy dog"
 * SubstituteVariable("${xx} brown fox jumped over ${xx} lazy dog",
 *      "${xx}", "the"); =>>
 * "the quick brown fox jumped over the lazy dog"
 * @author Chris Bitmead
 */
public class SubstituteVariable {

	/**
	 * @param str String in which to do the substitutions
	 * @param variable The pattern to match and replace
	 * @param value The string to substitute into the string
	 */
	public static String substitute(String str, String variable, String value) {
		return substitute(str, variable, value, -1);
	}
	/**
	 * @param str String in which to do the substitutions
	 * @param variable The pattern to match and replace
	 * @param value The string to substitute into the string
	 * @param The maximum number of times to do the substitution. -1 = unlimited.
	 */
	public static String substitute(String str, String variable, String value, int num) {
		StringBuffer buf = new StringBuffer(str);
		int ind = str.indexOf(variable);
		while (ind >= 0 && num-- != 0) {
			buf.replace(ind, ind + variable.length(), value);
			ind = buf.toString().indexOf(variable);
		}
		return buf.toString();
	}
}
