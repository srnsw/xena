package au.gov.naa.digipres.xena.kernel.metadatawrapper;
// SAX classes.
//JAXP 1.1
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.LegacyXenaCode;
import au.gov.naa.digipres.xena.kernel.MultiInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Wrap the XML with default Xena meta-data.
 *
 * @author Andrew Keeling
 */
public class DefaultInnerWrapNormaliser extends XMLFilterImpl {

    private final static String DEFAULTWRAPPER = "Default Package Wrapper";
    
    public void startDocument() throws SAXException {
        try {
            XMLReader normaliser = (XMLReader)getProperty("http://xena/normaliser");
            if (normaliser == null) {
                throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
            }
            
            XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
            super.startDocument();
            File outfile = ((File)getProperty("http://xena/file"));

            if (outfile == null)  {
                throw new XenaException("Output file was null!");
            }
            if (xis.getFile() == null) {
                throw new XenaException("XIS input file was null!");
            }
            

            ContentHandler th = getContentHandler();
            AttributesImpl att = new AttributesImpl();
            th.startElement(null, "xena","xena", att);

            
            th.startElement(null, "meta_data", "meta_data", att);
            
            // give the name of the meta data wrapper...
            th.startElement(null, "meta_data_wrapper_name","meta_data_wrapper_name", att);
            th.characters(DEFAULTWRAPPER.toCharArray(), 0, DEFAULTWRAPPER.length());
            th.endElement(null, "meta_data_wrapper_name", "meta_data_wrapper_name");
            
            // give the class name of the normaliser
            th.startElement(null, "normaliser_name", "normaliser_name", att);
            th.characters(normaliser.getClass().getName().toCharArray(), 0, normaliser.getClass().getName().length());
            th.endElement(null, "normaliser_name", "normaliser_name");

            // give the input source uri of the current xis
            th.startElement(null, "input_source_uri", "input_source_uri", att);
            th.characters(xis.getSystemId().toCharArray(), 0, xis.getSystemId().length());
            th.endElement(null, "input_source_uri", "input_source_uri");
            
            th.endElement(null, "meta_data", "meta_data");
            
            th.startElement(null, "content","content", att);
            
            
        } catch (XenaException x) {
            throw new SAXException(x);
        }
    }

    public void endDocument() throws org.xml.sax.SAXException {
        XenaInputSource xis = (XenaInputSource)getProperty("http://xena/input");
        File outfile = ((File)getProperty("http://xena/file"));
        //int level = ((Integer)getProperty("http://xena/level"));
        if (xis.getFile() != null || outfile != null) {
            ContentHandler th = getContentHandler();
            th.endElement(null, "content","content");
            th.endElement(null, "xena","xena");
        }
        super.endDocument();
    }

}
