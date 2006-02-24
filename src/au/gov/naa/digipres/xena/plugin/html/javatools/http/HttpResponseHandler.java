package au.gov.naa.digipres.xena.plugin.html.javatools.http;
import java.io.*;

import au.gov.naa.digipres.xena.plugin.html.javatools.thread.*;

abstract public class HttpResponseHandler  {
//	public static final byte[] EOL = {
//		(byte)'\r', (byte)'\n'};
	public static String EOL = "\r\n";
	public abstract void processURL(PrintStream ps, boolean sendFile) throws IOException;
}