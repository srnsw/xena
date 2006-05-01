
package au.gov.naa.digipres.xena.demo.foo;


import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;


public class FooNormaliser extends AbstractNormaliser {
    
    public static final String FOO_URI = "http://preservation.naa.gov.au/foo/0.1";
    public static final String FOO_OPENING_ELEMENT_LOCAL_NAME = "data";
    public static final String FOO_OPENING_ELEMENT_QUALIFIED_NAME = "foo:data";
    
    public static final String FOO_PART_ELEMENT_LOCAL_NAME = "part";
    public static final String FOO_PART_ELEMENT_QUALIFIED_NAME = "foo:part";
    
    public void parse(InputSource input) throws IOException, SAXException {
        
        ContentHandler contentHandler = getContentHandler();
        AttributesImpl openingAttribute = new AttributesImpl();     
        contentHandler.startElement(FOO_URI, FOO_OPENING_ELEMENT_LOCAL_NAME, FOO_OPENING_ELEMENT_QUALIFIED_NAME, openingAttribute);
        
        AttributesImpl partAttribute = new AttributesImpl();        
        contentHandler.startElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME, partAttribute);
        
        BufferedReader reader = new BufferedReader(input.getCharacterStream());
        
        long magicNumberLength = (new Integer(FooGuesser.FOO_MAGIC.length)).longValue();
        reader.skip(magicNumberLength);
        
        int nextCharVal;
        while ( (nextCharVal = reader.read() ) != -1) {
            char currentChar = (char)nextCharVal;
            
            if (currentChar == '~') {
                contentHandler.endElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME);
                contentHandler.startElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME, partAttribute);
            } else if (currentChar == '\\') {
                int escapedCharVal = reader.read();
                if (escapedCharVal == -1) {
                    break;
                }
                char escapedChar = (char)escapedCharVal;
                char[] escapedCharArray = {escapedChar};
                contentHandler.characters(escapedCharArray, 0, 1);
                
            } else {
                char[] newCharArray =  {currentChar};
                contentHandler.characters(newCharArray, 0, 1);
            }
        }
        contentHandler.endElement(FOO_URI, FOO_PART_ELEMENT_LOCAL_NAME, FOO_PART_ELEMENT_QUALIFIED_NAME);
        contentHandler.endElement(FOO_URI, FOO_OPENING_ELEMENT_LOCAL_NAME, FOO_OPENING_ELEMENT_QUALIFIED_NAME);
    }


    
    public String getName() {
        return "Foo";
    }
    
}
