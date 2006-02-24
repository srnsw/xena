package au.gov.naa.digipres.xena.plugin.email.msg;
import javax.mail.Address;

public class MsgAddress extends Address {
	String s;

	public MsgAddress(String s) {
		this.s = s;
	}

	public String getType() {
		return "msg";
	}

	public String toString() {
		return s;
	}

	public boolean equals(Object object) {
		return ((MsgAddress)object).s.equals(s);
	}
}