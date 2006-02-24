package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.net.*;

public interface WgetErrorListener {
	public void errorEvent(URL subUrl, String urlString, Exception ex) throws WgetException;
}