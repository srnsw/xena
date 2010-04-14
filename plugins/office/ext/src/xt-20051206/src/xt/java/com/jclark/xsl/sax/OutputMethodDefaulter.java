// $Id$

package com.jclark.xsl.sax;

import com.jclark.xsl.om.*;
import org.xml.sax.*;
import com.jclark.xsl.tr.OutputMethod;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

/**
 * able to automagically decide to use the html output method
 * if the first start element name is "html" in no namespace
 */
class OutputMethodDefaulter 
    implements DocumentHandler, CommentHandler, RawCharactersHandler
{
    private final ResultBase result;
    private final OutputMethod outputMethod;
    private final Vector savedEvents = new Vector();
    private Locator locator;
    

    public void startDocument() { }

    public void endDocument() throws SAXException 
    {
        getDocumentHandler(false).endDocument();
    }

    public void startElement(String name, AttributeList atts) throws SAXException 
    {
        getDocumentHandler(name.equalsIgnoreCase("html")
                           && atts.getValue("xmlns") == null)
            .startElement(name, atts);
    }
  
    public void endElement(String name) 
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
                getDocumentHandler(false).characters(buf, off, len);
                return;
            }
        }
        savedEvents.addElement(new CharactersEvent(buf, off, len));
    }

    public void ignorableWhitespace(char[] buf, int off, int len) 
        throws SAXException 
    {
        savedEvents.addElement(new IgnorableWhitespaceEvent(buf, off, len));
    }

    public void comment(String content) 
    {
        savedEvents.addElement(new CommentEvent(content));
    }

    public void processingInstruction(String target, String content)
    {
        savedEvents.addElement(new ProcessingInstructionEvent(target, content));
    }

    public void rawCharacters(String chars) 
    {
        savedEvents.addElement(new RawCharactersEvent(chars));
    }

    public void setDocumentLocator(Locator loc) 
    {
        this.locator = loc;
    }

    OutputMethodDefaulter(ResultBase result, OutputMethod outputMethod)
    {
        this.result = result;
        this.outputMethod = outputMethod;
    }

    private DocumentHandler getDocumentHandler(boolean isHtml)
        throws SAXException 
    {
        Name name = outputMethod.getNameTable().createName(isHtml ? "html" : "xml");
        try {
            DocumentHandler handler = result.setOutputMethod(name, outputMethod);
            if (locator != null) {
                handler.setDocumentLocator(locator);
            }
            handler.startDocument();
            for (Enumeration iter = savedEvents.elements(); 
                 iter.hasMoreElements();) {
                ((Event)iter.nextElement()).emit(handler);
            }
            return handler;
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    ///////////////////////////////////////////////////////////////
    //
    // represent Events we queue up till we decide whether to default to
    // html or xml output method. Once we decide, then we can release 'em
    //

    static abstract class Event 
    {
        abstract void emit(DocumentHandler handler) throws SAXException;
    }

    static class ProcessingInstructionEvent extends Event 
    {
        private String target;
        private String content;
        ProcessingInstructionEvent(String target, String content) {
            this.target = target;
            this.content = content;
        }
        void emit(DocumentHandler handler) throws SAXException {
            handler.processingInstruction(target, content);
        }
    }

    static class CommentEvent extends Event 
    {
        private String content;
        CommentEvent(String content) {
            this.content = content;
        }
        void emit(DocumentHandler handler) throws SAXException {
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
        void emit(DocumentHandler handler) throws SAXException {
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
        void emit(DocumentHandler handler) throws SAXException {
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
        void emit(DocumentHandler handler) throws SAXException {
            handler.ignorableWhitespace(buf, 0, buf.length);
        }
    }


}
