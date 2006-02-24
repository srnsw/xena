package au.gov.naa.digipres.xena.plugin.html.javatools.util;
import java.util.*;

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