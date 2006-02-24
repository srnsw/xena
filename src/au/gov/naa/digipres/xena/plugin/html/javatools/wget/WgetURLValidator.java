package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.net.*;

abstract interface WgetURLValidator {
	boolean isURLValid(URL url);
}