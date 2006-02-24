package au.gov.naa.digipres.xena.plugin.html.javatools.wget;
import java.io.*;
import org.jdom.input.*;
import org.jdom.*;

public class XmlTest {
	public static void main(String[] args) throws IOException, JDOMException {
		SAXBuilder sax = new SAXBuilder();
		FileInputStream is = new FileInputStream("c:/tmp/amp.xml");
		Element el = sax.build(is).detachRootElement();
	}

}