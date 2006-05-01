
package au.gov.naa.digipres.xena.demo.foo;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class FooNormaliser extends AbstractNormaliser {

	public static final String FOO_URI = "http://preservation.naa.gov.au/foo/0.1";
	public static final String FOO_OPENING_ELEMENT_LOCAL_NAME = "data";
	public static final String FOO_OPENING_ELEMENT_QUALIFIED_NAME = "foo:data";
	
	
	public void parse(InputSource source) throws IOException, SAXException {
		ContentHandler contentHandler = getContentHandler();
		AttributesImpl openingAttribute = new AttributesImpl();
		
		contentHandler.startElement(FOO_URI, FOO_OPENING_ELEMENT_LOCAL_NAME, FOO_OPENING_ELEMENT_QUALIFIED_NAME, openingAttribute);
		char[] message = "The foo file contents will go in here!".toCharArray();
		contentHandler.characters(message, 0, message.length);
		contentHandler.endElement(FOO_URI, FOO_OPENING_ELEMENT_LOCAL_NAME, FOO_OPENING_ELEMENT_QUALIFIED_NAME);
	}


	public String getName() {
		return "Foo";
	}

}