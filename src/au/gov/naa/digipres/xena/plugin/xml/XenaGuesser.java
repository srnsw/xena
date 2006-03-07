package au.gov.naa.digipres.xena.plugin.xml;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 * Guesser for Xena files.
 *
 * @author Chris Bitmead
 */
public class XenaGuesser extends Guesser {
	
	private Type type;
	
	
	/**
	 * @throws XenaException 
	 * 
	 */
	public XenaGuesser() throws XenaException
	{
		super();
		type = TypeManager.singleton().lookup(XenaXmlFileType.class);
	}

	public Guess guess(XenaInputSource source) throws IOException, XenaException {
		Guess guess = new Guess(type);
		
			String topXmlTag = getTag(source);
			if (topXmlTag != null) {
				try {
					guess.setType((FileType)TypeManager.singleton().lookup("Xena-" + topXmlTag));
					// Need that little bit more certainty of being like a magic number
					guess.setMagicNumber(true);
                    guess.setDataMatch(true);
				} catch (XenaException x) {
				    guess.setPossible(true);
				}
			}
		
		return guess;
	}

	private String getTag(InputSource is) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//			parser.setProperty("http://xml.org/sax/features/validation", false);
//			System.out.println("val: " + parser.isValidating() + " " + parser.getClass().getName());
			parser.parse(is, new DefaultHandler() {
				public void startElement(String uri,
										 String localName,
										 String qName,
										 Attributes attributes) throws SAXException {
					// Bail out early as soon as we've found what we want
					// for super efficiency.
					String tag = qName;
					if (tag == null || tag.equals("")) {
						tag = localName;
					}
					throw new SAXException(tag, new FoundException(tag));
				}
			});
		} catch (SAXException e) {
			if (e.getException() instanceof FoundException) {
				return e.getMessage();
			}
			// Nothing
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	class FoundException extends RuntimeException {
		FoundException(String tag) {
			this.tag = tag;
		}

		String tag;
	}
    
    public String getName() {
        return "XenaGuesser";
    }
    
	@Override
	protected Guess createBestPossibleGuess()
	{
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setDataMatch(true);
		guess.setPossible(true);
		return guess;
	}

	@Override
	public Type getType()
	{
		return type;
	}
    
}
