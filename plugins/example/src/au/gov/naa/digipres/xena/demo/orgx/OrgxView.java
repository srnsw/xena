/*
 * Created on 4/05/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.SpringUtilities;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.kernel.view.XmlDivertor;

public class OrgxView extends XenaView {

    
    private JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private JPanel metaDataPanel = new JPanel();

    private JPanel contentPanel = new JPanel();

    public OrgxView() {
        this.setLayout(new BorderLayout());
        this.add(splitPane, BorderLayout.CENTER);

        // Add our content pane to the top half of the split panel
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        splitPane.add(scrollPane);
        
        // add our meta data panel to the bottom half of the split panel
        metaDataPanel.setLayout(new BorderLayout());
        splitPane.add(metaDataPanel);

        
        splitPane.setResizeWeight(1.0);

        //SpringLayout springLayout = new SpringLayout();
        metaDataPanel.setLayout(new SpringLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(165, 163, 151));
        Border titleBorder = new TitledBorder(etchedBorder, "Org X Package");
        this.setBorder(titleBorder);
    }
    
    /*
     * Override the render methods...
     */
    

    public ContentHandler getContentHandler() throws XenaException {
        XMLFilterImpl pkgHandler = new OrgxDivertor(this, contentPanel);
        return pkgHandler;
    }
    
    
    private class OrgxDivertor extends XmlDivertor {
        
        private boolean inMeta = false;
        private StringBuffer tagContentBuffer;
        private int entries = 0;
        
        public OrgxDivertor(XenaView view, JComponent component) throws XenaException {
            super(view, component);
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            /* If we are not diverted, and we have a meta data name, simply add the element name as a JLabel.
             * Also create a new string buffer to hold our tag content
             */
            if (!isDiverted()) {
                if (qName.equals(OrgXMetaDataWrapper.ORGX_META_TAG)) {
                    inMeta = true;
                } else if (qName.equals(OrgXMetaDataWrapper.ORGX_CONTENT_TAG)) {
                    this.setDivertNextTag();
                } else if (inMeta) {
                    StringBuffer tagName = new StringBuffer();
                    tagName.append(qName);
                    JLabel tagLabel = new JLabel(tagName.toString());
                    metaDataPanel.add(tagLabel);
                    entries++;
                    tagContentBuffer = new StringBuffer();
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            /*
             * Add all the characters within the tag to our tag content buffer.
             */
            //TODO - test this!
            if (!isDiverted()) {
                if (tagContentBuffer != null) {
                    tagContentBuffer.append(ch, start, length);
                }
            }
            super.characters(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            /*
             * If we are not diverted and we are in a meta data tag, 
             * create and add a new JLabel using our tag content buffer.
             */
            super.endElement(uri, localName, qName);
            if (!isDiverted()) {
                if (qName.equals(OrgXMetaDataWrapper.ORGX_META_TAG)) {
                    inMeta = false;
                } else if (tagContentBuffer != null) {
                    JLabel newMetaDataLabel = new JLabel(tagContentBuffer.toString());
                    metaDataPanel.add(newMetaDataLabel);
                }
                tagContentBuffer = null;
            }
            repaint();
        }
        
        @Override
        public void endDocument() {
            // Make the layout as a grid, on the meta data panel, 2 columns, the number of rows as the number of entries,
            // and all spacing / padding as 5.
            SpringUtilities.makeCompactGrid(metaDataPanel, 2, entries, 5, 5, 5,5);;
        }
    }

    
    @Override
    public String getViewName() {
        return "OrgX Viewer";
    }

    @Override
    public boolean canShowTag(String tag) throws XenaException {
        return tag.equals(OrgXMetaDataWrapper.ORGX_OPENING_TAG);
    }

}
