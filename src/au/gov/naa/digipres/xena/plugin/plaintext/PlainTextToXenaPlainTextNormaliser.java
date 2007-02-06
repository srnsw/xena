package au.gov.naa.digipres.xena.plugin.plaintext;
//JAXP 1.1
import java.io.BufferedReader;
import java.io.InputStream;

import org.jdom.Namespace;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Normalise plaintext documents to Xena plaintext instances.
 *
 * @author Chris Bitmead
 */
public class PlainTextToXenaPlainTextNormaliser extends AbstractNormaliser {
	final static String PREFIX = "plaintext";

	final static String URI = "http://preservation.naa.gov.au/plaintext/1.0";

	private String tabSizeString = null;

	protected boolean found = false;

	public PlainTextToXenaPlainTextNormaliser() {
	}

	public String getName() {
		return "Plaintext";
	}

	public String encoding;

	public void setTabSize(Integer tabSizeInteger) {
		this.tabSizeString = tabSizeInteger.toString();
	}

	public Integer getTabSizeString() {
		return Integer.valueOf(tabSizeString);
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws java.io.IOException, org.xml.sax.SAXException {
		InputStream inputStream = input.getByteStream();
		inputStream.mark(Integer.MAX_VALUE);
		if (input.getEncoding() == null) {
			input.setEncoding(CharsetDetector.mustGuessCharSet(inputStream, 2 ^ 16));
		}
		inputStream.reset();
		Namespace nameSpace = Namespace.getNamespace(PREFIX, URI);
		ContentHandler contentHandler = getContentHandler();
		AttributesImpl topAttribute = new AttributesImpl();
		AttributesImpl attribute = new AttributesImpl();
        AttributesImpl emptyAttribute = new AttributesImpl();
        tabSizeString  = normaliserManager.getPluginManager().
                                    getPropertiesManager().
                                    getPropertyValue(PlainTextProperties.PLUGIN_NAME, PlainTextProperties.TAB_SIZE);
        
        
		if (tabSizeString != null) {
			topAttribute.addAttribute(URI, "tabsize", "tabsize", null, tabSizeString.toString());
		}
		contentHandler.startElement(URI, "plaintext", "plaintext:plaintext", topAttribute);
		BufferedReader bufferedReader = new BufferedReader(input.getCharacterStream());
		String linetext = null;
		attribute.clear();
		attribute.addAttribute("http://www.w3.org/XML/1998/namespace", "space", "xml:space", null, "preserve");
		
        // here we spec whether we are going by line or char.
        // TODO: aak - my feeling is, if it is guessed at plain text, to hell with it, we just do it this way.
        // the only question is do we add an enclosing tag?
        // XXX - aak - according to field marshal carden, we will go char by char, and put an enclosing tag around bad chars.
        
        
        
		boolean goingByLine = false;
        boolean enclosingTagRoundBadChars = true;
        
        
        while ((linetext = bufferedReader.readLine()) != null) {
			contentHandler.startElement(URI, "line", "plaintext:line", attribute);
			char[] arr = linetext.toCharArray();
			for (int i = 0; i < arr.length; i++) {
				char c = arr[i];
                if (goingByLine) {
                    // going by line, we just check each char to make sure it is valid.
                    if (!XMLCharacterValidator.isValidCharacter(c)) {
                        contentHandler.startElement(URI, "line", "plaintext:line", attribute);
                        throw new SAXException("PlainText normalisation - Cannot use character in XML: 0x" + 
    					                       Integer.toHexString(c) +
    					                       ". This is probably not a PlainText file");
                    }
                } else {
                    // not going by line, we check each char, if valid give it to the content handler, otherwise give
                    // the content handler an escaped string with the hex value of our bad char.
                    char[] singleCharArray = {c};
                    if (XMLCharacterValidator.isValidCharacter(c))
					{
                        contentHandler.characters(singleCharArray, 0, singleCharArray.length);
                    }
					else
					{
                        if (enclosingTagRoundBadChars)
						{
                            // write out the bad character from within a tag.
                            contentHandler.startElement(URI, "bad_char", "plaintext:bad_char", attribute);
                            String badCharString = Integer.toHexString(c);
                            contentHandler.characters(badCharString.toCharArray(), 0, badCharString.toCharArray().length);
                            contentHandler.endElement(URI, "bad_char", "plaintext:bad_char");
                        }
						else
						{
                            // write out the bad character escaped...
                            String badCharString = "\\" + Integer.toHexString(c);
                            contentHandler.characters(badCharString.toCharArray(), 0, badCharString.toCharArray().length);
                        }
                    }
                }
			}
            // if going by line dont forget to write our line!!!
			if (goingByLine) {
			    contentHandler.characters(arr, 0, arr.length);
            }
            contentHandler.endElement(URI, "line", "plaintext:line");
		}
		contentHandler.endElement(URI, "plaintext", "plaintext:plaintext");
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
