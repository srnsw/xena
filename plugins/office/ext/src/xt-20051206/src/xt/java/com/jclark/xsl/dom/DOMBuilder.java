// $Id$

package com.jclark.xsl.dom;

import org.xml.sax.*;
import com.jclark.xsl.sax.CommentHandler;
import org.w3c.dom.*;

/**
 * provides callback methods for xslt engine and
 *  constructs W3C DOM Nodes therefrom
 */
public class DOMBuilder implements DocumentHandler, CommentHandler
{
    private Node parent;
    private Document document;

    public DOMBuilder(Node parent)
    {
        this.parent = parent;
        if (parent.getNodeType() == Node.DOCUMENT_NODE) {
            this.document = (Document)parent;
        } else {
            this.document = parent.getOwnerDocument();
        }
    }

    public void startDocument() { }

    public void characters(char ch[], int start, int len)
    {
        parent.appendChild(document.createTextNode(new String(ch, start, 
                                                              len)));
    }

    public void ignorableWhitespace (char ch[], int start, int len)
    {
        characters(ch, start, len);
    }
  
    public void startElement(String name, AttributeList atts)
    {
        Element element = document.createElement(name);
        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            element.setAttribute(atts.getName(i), atts.getValue(i));
        }
        parent.appendChild(element);
        parent = element;
    }

    public void endElement(String name)
    {
        parent = parent.getParentNode();
    }

    public void processingInstruction(String target, String data)
    {
        parent.appendChild(document.createProcessingInstruction(target, data));
    }

    public void comment(String body)
    {
        parent.appendChild(document.createComment(body));
    }

    public void endDocument() { }

    public void setDocumentLocator(Locator loc) { }
}
