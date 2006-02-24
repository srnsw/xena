package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.net.*;
import java.util.regex.*;

public class WgetPatternURLValidator implements WgetURLValidator {
	String host;

	String hostPattern;

	Pattern hostPatternP;

	String protocolPattern;

	Pattern protocolPatternP;

//	boolean neverFollow;

	public WgetPatternURLValidator() {
	}

	public boolean isURLValid(URL url) {
/*		if (neverFollow) {
			return false;
		}*/
		if (host != null && !host.equals(url.getHost())) {
			return false;
		}
		if (hostPatternP != null && !hostPatternP.matcher(url.getHost()).matches()) {
			return false;
		}
		if (protocolPatternP != null && !protocolPatternP.matcher(url.getProtocol()).matches()) {
			return false;
		}
		return true;
	}

	public String getHostPattern() {
		return hostPattern;
	}

	public void setHostPattern(String hostPattern) {
		if (hostPattern != null) {
			hostPatternP = Pattern.compile(hostPattern, Pattern.CASE_INSENSITIVE);
		}
		this.hostPattern = hostPattern;
	}

	public String getProtocolPattern() {
		return protocolPattern;
	}

	public void setProtocolPattern(String protocolPattern) {
		if (protocolPattern != null) {
			protocolPatternP = Pattern.compile(protocolPattern, Pattern.CASE_INSENSITIVE);
		}
		this.protocolPattern = protocolPattern;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}
/*  public boolean isNeverFollow() {
    return neverFollow;
  }
  public void setNeverFollow(boolean neverFollow) {
    this.neverFollow = neverFollow;
  }*/

}