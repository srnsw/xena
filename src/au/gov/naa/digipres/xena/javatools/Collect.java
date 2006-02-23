package au.gov.naa.digipres.xena.javatools;
import java.util.*;

public class Collect {
	public static void addAll(Collection c, Object[] arr) {
		for (int i = 0; i < arr.length; i++) {
			c.add(arr[i]);
		}
	}
}