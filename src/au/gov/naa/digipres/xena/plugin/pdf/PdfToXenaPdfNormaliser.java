package au.gov.naa.digipres.xena.plugin.pdf;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * Convert PDF to the Xena PDF format.
 *
 * @author Chris Bitmead
 */
public class PdfToXenaPdfNormaliser extends AbstractNormaliser 
{
    public final static String PDF_PREFIX = "pdf";

    final static String PDF_URI = "http://preservation.naa.gov.au/pdf/1.0";

    public String getName() 
    {
        return "PDF";
    }

    public void parse(InputSource input, NormaliserResults results) throws IOException, SAXException 
    {
        ContentHandler ch = getContentHandler();
        AttributesImpl att = new AttributesImpl();
        InputStream is = input.getByteStream();
        ch.startElement(PDF_URI, PDF_PREFIX, PDF_PREFIX + ":" + PDF_PREFIX, att);
        InputStreamEncoder.base64Encode(is, ch);
        ch.endElement(PDF_URI, PDF_PREFIX, PDF_PREFIX + ":" + PDF_PREFIX);
    }
}
