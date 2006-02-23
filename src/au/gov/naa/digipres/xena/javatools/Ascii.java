package au.gov.naa.digipres.xena.javatools;
import java.util.*;

public class Ascii {
	Map names = new HashMap();
	Map slashNames = new HashMap();

	public Ascii() {
		names.put(new Integer(0), "NUL");
		names.put(new Integer(1), "SOH");
		names.put(new Integer(2), "STX");
		names.put(new Integer(3), "ETX");
		names.put(new Integer(4), "EOT");
		names.put(new Integer(5), "ENQ");
		names.put(new Integer(6), "ACK");
		names.put(new Integer(7), "BEL");
		names.put(new Integer(8), "BS");
		names.put(new Integer(9), "HT");
		names.put(new Integer(10), "LF");
		names.put(new Integer(11), "VT");
		names.put(new Integer(12), "FF");
		names.put(new Integer(13), "CR");
		names.put(new Integer(14), "SO");
		names.put(new Integer(15), "SI");
		names.put(new Integer(16), "DLE");
		names.put(new Integer(17), "DC1");
		names.put(new Integer(18), "DC2");
		names.put(new Integer(19), "DC3");
		names.put(new Integer(20), "DC4");
		names.put(new Integer(21), "NAK");
		names.put(new Integer(22), "SYN");
		names.put(new Integer(23), "ETB");
		names.put(new Integer(24), "CAN");
		names.put(new Integer(25), "EM");
		names.put(new Integer(26), "SUB");
		names.put(new Integer(27), "ESC");
		names.put(new Integer(28), "FS");
		names.put(new Integer(29), "GS");
		names.put(new Integer(30), "RS");
		names.put(new Integer(31), "US");
		names.put(new Integer(32), "SPACE");
		names.put(new Integer(127), "DEL");

		slashNames.put(new Integer(0), "0");
		slashNames.put(new Integer(7), "a");
		slashNames.put(new Integer(8), "b");
		slashNames.put(new Integer(9), "t");
		slashNames.put(new Integer(10), "n");
		slashNames.put(new Integer(11), "v");
		slashNames.put(new Integer(12), "f");
		slashNames.put(new Integer(13), "r");
//				" BS   \\b  010   8     08",
	}

	public String getOctal(int c) {
		return "0" + Integer.toString(c, 8);
	}

	public String getHexidecimal(int c) {
		return "0x" + Integer.toString(c, 16);
	}

	public String getDecimal(int c) {
		return Integer.toString(c, 10);
	}

	public String getName(int c) {
		String name = (String) names.get(new Integer(c));
		if (name == null) {
			if (c < 128) {
				char[] str = {
					(char)c};
				name = new String(str);
			} else {
				name = "";
			}
		}
		return name;
	}

	public String getSlashName(int c) {
		String name = (String) slashNames.get(new Integer(c));
		if (name == null) {
			name = "";
		} else {
			name = "\\" + name;
		}
		return name;
	}
}
