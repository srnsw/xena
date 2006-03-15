package au.gov.naa.digipres.xena.plugin.pdf;

import java.io.IOException;
import java.io.InputStream;

import org.jdom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Convert PDF to the Xena PDF format.
 *
 * @author Chris Bitmead
 */
public class PdfToXenaPdfNormaliser extends AbstractNormaliser {
    final static String PDF_PREFIX = "pdf";

    final static String PDF_URI = "http://preservation.naa.gov.au/pdf/1.0";

    /**
     * RFC suggests max of 76 characters per line
     */
    public static final int MAX_BASE64_RFC_LINE_LENGTH = 76;

    /**
     * Base64 turns 3 characters into 4...
     */
    public static final int CHUNK_SIZE = (MAX_BASE64_RFC_LINE_LENGTH * 3) / 4;

    public String getName() {
        return "PDF";
    }

    public void parse(InputSource input, NormaliserResults results) 
    throws IOException, SAXException {
        Element png = null;
        Type type = ((XenaInputSource) input).getType();
        String tag;
        ContentHandler ch = getContentHandler();
        AttributesImpl att = new AttributesImpl();
        ch.startElement(PDF_URI, PDF_PREFIX, PDF_PREFIX + ":" + PDF_PREFIX, att);

        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        InputStream is = input.getByteStream();

        // 80 characters makes nice looking output
        byte[] buf = new byte[CHUNK_SIZE];
        int c;
        while (0 <= (c = is.read(buf))) {
            byte[] tbuf = buf;
            if (c < buf.length) {
                tbuf = new byte[c];
                System.arraycopy(buf, 0, tbuf, 0, c);
            }
            char[] chs = encoder.encode(tbuf).toCharArray();
            ch.characters(chs, 0, chs.length);
        }
        ch.endElement(PDF_URI, PDF_PREFIX, PDF_PREFIX + ":" + PDF_PREFIX);
    }
}
