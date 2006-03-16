package au.gov.naa.digipres.xena.plugin.pdf;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.gui.MainFrame;
import au.gov.naa.digipres.xena.helper.XmlContentHandlerSplitter;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * View for PDF files.
 *
 * @author Chris Bitmead
 */
public class PdfView extends XenaView {
    PdfViewer viewer;
    public PdfView() {
    }

    public boolean canShowTag(String tag) throws XenaException {
        return tag.equals(viewManager.getPluginManager().getTypeManager().lookupXenaFileType(XenaPdfFileType.class).getTag());
    }

    public String getViewName() {
        return "";
    }

    public ContentHandler getContentHandler() throws XenaException {
        // PdfViewer doesn't like getting created in the constructor
        viewer = new PdfViewer(MainFrame.singleton(), this.getInternalFrame());
        this.add(viewer);
        XmlContentHandlerSplitter splitter = new XmlContentHandlerSplitter();
        splitter.addContentHandler(getTmpFileContentHandler());
        splitter.addContentHandler(new XMLFilterImpl() {
            StringBuffer sb = new StringBuffer();

            public void endDocument() {
                sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
                byte[] bytes = null;
                try {
                    bytes = decoder.decodeBuffer(sb.toString());
                } catch (IOException x) {
                    MainFrame.singleton().showError(x);
                }
            viewer.openFile(bytes);
            }
            public void characters(char[] ch, int start, int length) throws
                    SAXException {
                sb.append(ch, start, length);
            }

        });
        return splitter;
    }
}


