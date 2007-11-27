package au.gov.naa.digipres.xena.demo.foo;

import java.io.BufferedReader;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class FooNormaliser extends AbstractNormaliser {

	public static final String FOO_URI = "http://preservation.naa.gov.au/foo/0.1";
	public static final String FOO_OPENING_ELEMENT_LOCAL_NAME = "data";
	public static final String FOO_OPENING_ELEMENT_QUALIFIED_NAME = "foo:data";

	public static final String FOO_PART_ELEMENT_LOCAL_NAME = "part";
	public static final String FOO_PART_ELEMENT_QUALIFIED_NAME = "foo:part";

	@Override
	public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException {

		ContentHandler contentHandler = getContentHandler();
		AttributesImpl openingAttribute = new AttributesImpl();
		contentHandler.startElement(FOO_URI, FOO_OPENING_ELEMENT_LOCAL_NAME, FOO_OPENING_ELEMENT_QUALIFIED_NAME, openingAttribute);
		BufferedReader reader = new BufferedReader(input.getCharacterStream());

		long magicNumberLength = (new Integer(FooGuesser.FOO_MAGIC.length)).longValue();
		reader.skip(magicNumberLength);

		int nextCharVal;
		boolean startedElement = false;
		while ((nextCharVal = reader.read()) != -1) {
			char currentChar = (char) nextCharVal;

			if (currentChar == '~') {
				// Don't close the element if we haven't already started one!
				if (startedElement) {
					contentHandler.endElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME);
					startedElement = false;
				}
				contentHandler.startElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME, new AttributesImpl());
				startedElement = true;
			} else if (currentChar == '\\') {
				int escapedCharVal = reader.read();
				if (escapedCharVal == -1) {
					break;
				}
				char escapedChar = (char) escapedCharVal;
				char[] escapedCharArray = {escapedChar};
				contentHandler.characters(escapedCharArray, 0, 1);
			} else {
				char[] newCharArray = {currentChar};
				contentHandler.characters(newCharArray, 0, 1);
			}
		}

		// Don't close the element if we haven't already started one!
		if (startedElement) {
			contentHandler.endElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME);
		}

		contentHandler.endElement(FOO_URI, FOO_OPENING_ELEMENT_LOCAL_NAME, FOO_OPENING_ELEMENT_QUALIFIED_NAME);
	}

	@Override
	public String getName() {
		return "Foo";
	}

}
