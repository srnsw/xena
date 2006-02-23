package au.gov.naa.digipres.xena.helper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import au.gov.naa.digipres.xena.javatools.SubstituteVariable;

/**
 * Handling spaces in URLs is a particularly messy and gruesome business and has
 * caused untold strife. These utility functions, as ugly as they may be, seem to
 * solve the problem. Perhaps a big re-work of handling of URLs in Xena is in order.
 */
public class UrlEncoder {
	public static String encode(String url) throws UnsupportedEncodingException {
		String relpath = URLEncoder.encode(
			url,
			"UTF-8");
		relpath = SubstituteVariable.substitute(
			relpath, "+", "%20");
		relpath = SubstituteVariable.substitute(
			relpath, "%2F", "/");
		relpath = SubstituteVariable.substitute(
			relpath, "%3A", ":");
		return relpath;
	}

	public static String decode(String url) throws UnsupportedEncodingException {
		String relpath = URLDecoder.decode(
			url,
			"UTF-8");
		/*		relpath = javatools.util.SubstituteVariable.substitute(
		   relpath, "+", "%20");
		  relpath = javatools.util.SubstituteVariable.substitute(
		   relpath, "%2F", "/"); */
		return relpath;
	}
}
