package au.gov.naa.digipres.xena.plugin.dataset;

/**
 * This class returns the bit that you tag onto the end of number. e.g. if you
 * pass in the number "1" it will return "st", as in 1st. If you pass in
 * "2" it will return "nd", as in "2nd". etc.
 * Yes, the name is pretty wierd, but what can you name something like this?
 * @author Chris Bitmead
 */
public class OrdinalPostfix {
	final static String st = "st";
	final static String nd = "nd";
	final static String rd = "rd";
	final static String th = "th";

	public static String postfix(int number) {
		int mod100 = number % 100;
		int mod10 = number % 10;
		if (mod100 == 11 || mod100 == 12 || mod100 == 13) {
			return th;
		} else if (mod10 == 1) {
			return st;
		} else if (mod10 == 2) {
			return nd;
		} else if (mod10 == 3) {
			return rd;
		} else {
			return th;
		}
	}
}