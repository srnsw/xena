package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.net.*;
import java.io.*;

public interface WgetProcessUrl {
	void process(URLConnection connection) throws IOException, WgetException;
}