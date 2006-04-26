/*
 * Created on 30/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.foo;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;

public class XenaFooDeNormaliser extends AbstractDeNormaliser {

    private BufferedWriter bufferedWriter;

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Foo Denormaliser";

    }

    private boolean inFooPart = false;

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
        if (qName.equals(FooNormaliser.FOO_PART_ELEMENT_QUALIFIED_NAME)) {
            inFooPart = true;
            try {
                bufferedWriter.write("~");
            } catch (IOException iox) {
                throw new SAXException(iox);
            }
        }   
    }
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
        if (qName.equals(FooNormaliser.FOO_PART_ELEMENT_QUALIFIED_NAME)) {
            inFooPart = false;
        }
    }
    
    @Override
    public void characters(char[] ch, int offset, int len)
            throws org.xml.sax.SAXException {
        try {
            String content = new String(ch);
            for (int i = offset; i < offset + len; i++) {
                if (ch[i] == '~' || ch[i] == '\\') {
                    bufferedWriter.write('\\');
                }
                bufferedWriter.write(ch[i]);
            }
        } catch (IOException iox) {
            throw new SAXException(iox);
        }
    }

    @Override
    public void startDocument() throws org.xml.sax.SAXException {
        if (streamResult == null) {
            throw new SAXException("StreamResult not initialised by the normaliser manager");
        }
        bufferedWriter = new BufferedWriter(streamResult.getWriter());
        try {
            char[] fooMagic = new char[FooGuesser.FOO_MAGIC.length];
            for (int i = 0; i < FooGuesser.FOO_MAGIC.length; i++) {
                fooMagic[i] = (char)FooGuesser.FOO_MAGIC[i];
            }
            bufferedWriter.write(fooMagic);
        } catch (IOException x) {
            throw new SAXException(x);
        }
    }

    @Override
    public void endDocument() throws org.xml.sax.SAXException {
        try {
            bufferedWriter.close();
        } catch (IOException x) {
            throw new SAXException(x);
        }
        bufferedWriter = null;
    }

}
