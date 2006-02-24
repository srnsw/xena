package au.gov.naa.digipres.xena.plugin.email.trim;
import javax.mail.Address;

public class TrimAddress extends Address {
	String s;

	public TrimAddress(String s) {
		this.s = s;
	}

	public String getType() {
		return "trim";
	}

	public String toString() {
		return s;
	}

	public boolean equals(Object object) {
		return ((TrimAddress)object).s.equals(s);
	}
}