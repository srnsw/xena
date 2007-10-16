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

package au.gov.naa.digipres.xena.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Handling spaces in URLs is a particularly messy and gruesome business and has
 * caused untold strife. These utility functions, as ugly as they may be, seem to
 * solve the problem. Perhaps a big re-work of handling of URLs in Xena is in order.
 */
public class UrlEncoder {
	public static String encode(String url) throws UnsupportedEncodingException {
		String relpath = URLEncoder.encode(url, "UTF-8");
		relpath = substitute(relpath, "+", "%20");
		relpath = substitute(relpath, "%2F", "/");
		relpath = substitute(relpath, "%3A", ":");
		return relpath;
	}

	public static String decode(String url) throws UnsupportedEncodingException {
		String relpath = URLDecoder.decode(url, "UTF-8");
		/*
		 * relpath = javatools.util.SubstituteVariable.substitute( relpath, "+", "%20"); relpath =
		 * javatools.util.SubstituteVariable.substitute( relpath, "%2F", "/");
		 */
		return relpath;
	}

	/**
	 * @param str String in which to do the substitutions
	 * @param variable The pattern to match and replace
	 * @param value The string to substitute into the string
	 */
	public static String substitute(String str, String variable, String value) {
		StringBuffer buf = new StringBuffer(str);
		int ind = str.indexOf(variable);
		while (ind >= 0) {
			buf.replace(ind, ind + variable.length(), value);
			ind = buf.toString().indexOf(variable);
		}
		return buf.toString();
	}

}
