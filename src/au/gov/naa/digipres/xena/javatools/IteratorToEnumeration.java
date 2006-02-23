package au.gov.naa.digipres.xena.javatools;
import java.util.Enumeration;
import java.util.Iterator;

public class IteratorToEnumeration implements Enumeration {
	Iterator it;

	public IteratorToEnumeration(Iterator it) {
		this.it = it;
	}
	public boolean hasMoreElements() {
		return it.hasNext();
	}
	public Object nextElement() {
		return it.next();
	}
}