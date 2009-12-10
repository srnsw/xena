// $Id$

package com.jclark.xsl.sax2;

import com.jclark.xsl.om.*;
import org.xml.sax.*;

import com.jclark.xsl.sax.CommentHandler;
import com.jclark.xsl.tr.OutputMethod;

import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

/**
 * automagically decides to use the html output method
 * if the first start element name is "html" in no namespace
 * 
 * <p>Recieves SAX parse events from a ResultBase, till it
 * gets a look at the root element's name.</p>
 *
 * <p>Tells the ResultBase that had been sending it SAX parse
 * events what this has learned. The result
 * base will switch to an appropriate ContentHandler. This
 * then sends any events it has queued up while waiting
 * to see the root element name, then is never used again
 * during the transform.</p>
 */
public class OutputMethodDefaulter 
    implements ContentHandler, CommentHandler, RawCharactersHandler
{
    // the _result sends us SAX events
    private final ResultBase _result;

    // stuff from the "xsl:output" element
    private final OutputMethod _outputMethod;

    // we queue up events till we see the root element tag
    private final Vector _savedEvents = new Vector();

    // line numbers
    private Locator _locator;


    /**
     * Construct with the guy who's gonna be sending us events and
     * whatever the stylesheet's told us about the output parameters
     */
    OutputMethodDefaulter(ResultBase result, OutputMethod outputMethod)
    {
        _result = result;
        _outputMethod = outputMethod;
    }

    ///////////////////////
    //
    // SAX Content Handler implementation

    /**
     * receive notice of doc's start
     */
    public void startDocument() { }

    /**
     * receive notice of doc's end
     */
    public void endDocument() throws SAXException 
    {
        getContentHandler(false).endDocument();
    }

    /**
     * the first start element, if it's "html" and in no namespace, then
     * we switch to an html contentHandler -- yucchh
     */
    public void startElement(String namespaceURI,
                             String localName,
                             String qName, Attributes atts) 
        throws SAXException 
    {
        // has the side effect of routing all subsequent SAX events elsewhere
        getContentHandler(qName.equalsIgnoreCase("html")
                          && atts.getValue("xmlns") == null)
            .startElement(namespaceURI, localName, qName, atts);
    }
  
    /**
     * receive notice of an element's end ... this should never
     * happen, because we should have routed all events to
     * another ContentHandler upon getting the first startElement()
     */
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
    {
        throw new Error("unbalanced call to endElement");
    }

    public void characters(char[] buf, int off, int len) throws SAXException 
    {
        for (int i = 0; i < len; i++) {
            switch (buf[off + i]) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            default:
                getContentHandler(false).characters(buf, off, len);
                return;
            }
        }
        _savedEvents.addElement(new CharactersEvent(buf, off, len));
    }

    public void ignorableWhitespace(char[] buf, int off, int len) 
        throws SAXException 
    {
        _savedEvents.addElement(new IgnorableWhitespaceEvent(buf, off, len));
    }


    public void processingInstruction(String target, String content)
    {
        _savedEvents.addElement(new ProcessingInstructionEvent(target, content));
    }

    public void setDocumentLocator(Locator loc) 
    {
        _locator = loc;
    }

    public void startPrefixMapping(String prefix, String namespace)
    {}

    public void endPrefixMapping(String prefix)
    {}

    public void skippedEntity(String name)
    {}

    ///////////////////////////////////

    public void comment(String content) 
    {
        _savedEvents.addElement(new CommentEvent(content));
    }


    public void rawCharacters(String chars) 
    {
        _savedEvents.addElement(new RawCharactersEvent(chars));
    }


    private ContentHandler getContentHandler(boolean isHtml)
        throws SAXException 
    {
        Name name = _outputMethod.getNameTable().createName(isHtml ? "html" : "xml");
        try {
            // tell the Result to start sending his events somewhere else
            ContentHandler handler = _result.setOutputMethod(name, _outputMethod);

            // let's do some additional setup on the new handler
            if (_locator != null) {
                handler.setDocumentLocator(_locator);
            }

            // and relieve ourself of the queued events
            handler.startDocument();
            for (Enumeration iter = _savedEvents.elements(); iter.hasMoreElements();) {
                ((Event)iter.nextElement()).emit(handler);
            }

            // now we'll write return so we can send the start event
            // that triggered this method, and this defaulter is out
            // of the picture
            return handler;
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    ///////////////
    //
    // define some classes to represent possibly delayed events
    //
    
    static abstract class Event 
    {
        abstract void emit(ContentHandler handler) throws SAXException;
    }

    static class ProcessingInstructionEvent extends Event 
    {
        private String target;
        private String content;
        ProcessingInstructionEvent(String target, String content) {
            this.target = target;
            this.content = content;
        }
        void emit(ContentHandler handler) throws SAXException {
            handler.processingInstruction(target, content);
        }
    }

    static class CommentEvent extends Event 
    {
        private String content;
        CommentEvent(String content) {
            this.content = content;
        }
        void emit(ContentHandler handler) throws SAXException {
            if (handler instanceof CommentHandler)
                ((CommentHandler)handler).comment(content);
        }
    }

    static class RawCharactersEvent extends Event
    {
        private String chars;
        RawCharactersEvent(String chars) {
            this.chars = chars;
        }
        void emit(ContentHandler handler) throws SAXException {
            if (handler instanceof RawCharactersHandler)
                ((RawCharactersHandler)handler).rawCharacters(chars);
            else {
                char[] buf = chars.toCharArray();
                handler.characters(buf, 0, buf.length);
            }
        }
    }

    static class CharactersEvent extends Event 
    {
        private char[] buf;
        CharactersEvent(char[] b, int off, int len) {
            buf = new char[len];
            System.arraycopy(b, off, buf, 0, len);
        }
        void emit(ContentHandler handler) throws SAXException {
            handler.characters(buf, 0, buf.length);
        }
    }

    static class IgnorableWhitespaceEvent extends Event 
    {
        private char[] buf;
        IgnorableWhitespaceEvent(char[] b, int off, int len) {
            buf = new char[len];
            System.arraycopy(b, off, buf, 0, len);
        }
        void emit(ContentHandler handler) throws SAXException {
            handler.ignorableWhitespace(buf, 0, buf.length);
        }
    }

}
