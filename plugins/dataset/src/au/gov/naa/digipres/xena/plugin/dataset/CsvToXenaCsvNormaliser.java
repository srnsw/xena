/*
 * Created on 16/02/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class CsvToXenaCsvNormaliser extends AbstractNormaliser{

    final static String URI = "http://preservation.naa.gov.au/dataset/1.0";

    final static String PREFIX = "csv";
    
    public CsvToXenaCsvNormaliser() {
        
    }
    
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Old CSV";
    }

    public void parse(InputSource input, NormaliserResults results) 
    throws IOException, SAXException {
        // TODO Auto-generated method stub
        InputStream is = input.getByteStream();
        is.mark(Integer.MAX_VALUE);
        if (input.getEncoding() == null) {
            try {
                input.setEncoding(CharsetDetector.mustGuessCharSet(is, 2 ^ 16));
            } catch (IOException iox) {
                input.setEncoding("US-ASCII");
            }
        }
        is.reset();
        //Namespace nameSpace = Namespace.getNamespace(PREFIX, URI);
        ContentHandler contentHandler = getContentHandler();
        AttributesImpl topAttribute = new AttributesImpl();
        AttributesImpl attribute = new AttributesImpl();
        contentHandler.startElement(URI, "csv", "csv:csv", topAttribute);
        BufferedReader br = new BufferedReader(input.getCharacterStream());
        String linetext = null;
        attribute.clear();
        attribute.addAttribute("http://www.w3.org/XML/1998/namespace", "space", "xml:space", null, "preserve");
        while ((linetext = br.readLine()) != null) {
            contentHandler.startElement(URI, "line", "csv:row", attribute);
            char[] arr = linetext.toCharArray();
            for (int i = 0; i < arr.length; i++) {
                char c = arr[i];
                if (!isValidCharacter(c)) {
                    throw new SAXException("CSV normalisation - Cannot use character in XML: 0x" + 
                                           Integer.toHexString(c) +
                                           ". This is probably not a CSV file.");
                }
            }
            contentHandler.characters(arr, 0, arr.length);
            contentHandler.endElement(URI, "line", "csv:row");
        }
        contentHandler.endElement(URI, "csv", "csv:csv");
    }

    /**
     * Return true if the given character is valid as defined by Character.isDefined,
     * and is also a character which is allowable in an XML string.
     * @param c
     */
    public boolean isValidCharacter(char c)
    {
        boolean valid = true;
        if (!Character.isDefined(c))
        {
            valid = false;
        }
        int intVal = (int)c;
        if (!(intVal == 0x0009 ||
              intVal == 0x000A ||
              intVal == 0x000D ||
              (intVal >= 0x0020 && intVal <= 0xD7FF) ||         
              (intVal >= 0xE000 && intVal <= 0xFFFD) || 
              (intVal >= 0x10000 && intVal > 0x10FFFF)))
        {
            valid = false;
        }
        return valid;
    }
    
}
