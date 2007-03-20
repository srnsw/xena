package au.gov.naa.digipres.xena.util;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Handling spaces in URLs is a particularly messy and gruesome business and has
 * caused untold strife. These utility functions, as ugly as they may be, seem to
 * solve the problem. Perhaps a big re-work of handling of URLs in Xena is in order.
 */
public class FilenameEncoder 
{
	public static String encode(String url) throws UnsupportedEncodingException 
	{
//		String relpath = URLEncoder.encode(url, "UTF-8");
		String relpath = url;
		relpath = substitute(relpath, "/", "%2F");
		relpath = substitute(relpath, ":", "%3A");
		relpath = substitute(relpath, "\\", "%5C");
		relpath = substitute(relpath, "*", "%2A");
		relpath = substitute(relpath, "<", "%3C");
		relpath = substitute(relpath, ">", "%3E");
		relpath = substitute(relpath, "|", "%7C");
		relpath = substitute(relpath, "?", "%3F");
		relpath = substitute(relpath, "\"", "%22");
		return relpath;
	}

	public static String decode(String url) throws UnsupportedEncodingException 
	{
		String relpath = URLDecoder.decode(url, "UTF-8");
		return relpath;
	}
    
    /**
     * @param str String in which to do the substitutions
     * @param variable The pattern to match and replace
     * @param value The string to substitute into the string
     */
    public static String substitute(String str, String variable, String value) 
    {
        StringBuffer buf = new StringBuffer(str);
        int ind = str.indexOf(variable);
        while (ind >= 0) 
        {
            buf.replace(ind, ind + variable.length(), value);
            ind = buf.toString().indexOf(variable);
        }
        return buf.toString();
    }
    
}
