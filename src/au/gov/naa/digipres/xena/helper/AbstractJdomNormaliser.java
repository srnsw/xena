package au.gov.naa.digipres.xena.helper;
import java.io.IOException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * JDOM Normalisers may find it convenient to use this abstract class.
 * Allows normalisation using JDOM. This should be avoided because SAX is
 * more efficient and works with bigger files. However JDOM can be more
 * convenient to work with.
 * @author Chris Bitmead
 */
abstract public class AbstractJdomNormaliser extends AbstractNormaliser {
	public void parse(InputSource input, NormaliserResults results) 
	throws java.io.IOException, org.xml.sax.SAXException {
		// Call the JDOM normalisation procedure
		Element el = normalise(input);
		try {
			// Now write the tree as if it was SAX events.
			JdomUtil.writeElement(getContentHandler(), el);
		} catch (JDOMException x) {
			throw new SAXException(x);
		}
	}

	/**
	 * Subclasses should override this method and return the normalised result
	 * in the form of a JDOM Element tree.
	 * @param input InputSource corresponding to the raw data stream
	 * @return Element Normalised result tree
	 * @throws IOException read/write error
	 * @throws SAXException other error
	 */
	abstract public Element normalise(InputSource input) throws IOException, SAXException;
}
