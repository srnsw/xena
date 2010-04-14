// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import java.io.OutputStream;
import java.io.IOException;

/**
 * performs "pretty-printing" by wrapping  another 
 * OutputDocumentHandler, intercepting SAX events,
 * and inserting whitespace events as appropriate.
 * ... (Actually, I don't think it adds any
 * indenting, just newlines)
 */
public class Indenter 
    implements OutputDocumentHandler, CommentHandler,
               RawCharactersHandler
{
    private DocumentHandler documentHandler = null;
    private CommentHandler commentHandler = null;
    private RawCharactersHandler rawCharactersHandler;
    private char[] newline = new char[]{'\n'};

    static private final byte IN_PCDATA_CHUNK = 0;
    static private final byte JUST_HAD_START_TAG = 1;
    static private final byte OTHER = 2;

    private byte state = IN_PCDATA_CHUNK;

    public Indenter(DocumentHandler handler,
                    RawCharactersHandler rawCharactersHandler) 
    {
        this.documentHandler = handler;
        if (handler instanceof CommentHandler) {
            this.commentHandler = (CommentHandler)handler;
        } else {
            this.commentHandler = null;
        }
        this.rawCharactersHandler = rawCharactersHandler;
    }

    public DocumentHandler init(Destination dest, AttributeList atts)
        throws IOException, SAXException
    {
        if (documentHandler instanceof OutputDocumentHandler) {
            documentHandler = 
                ((OutputDocumentHandler)documentHandler).init(dest, atts);
        }
        return this;
    }

    public void setDocumentLocator(Locator locator)
    {
        documentHandler.setDocumentLocator(locator);
    }

    public void startDocument()
        throws SAXException
    {
        documentHandler.startDocument();
    }

    public void endDocument()
        throws SAXException
    {
        maybeNewline();
        documentHandler.endDocument();
    }

    public void startElement(String name, AttributeList atts)
        throws SAXException
    {
        maybeNewline();
        state = JUST_HAD_START_TAG;
        documentHandler.startElement(name, atts);
    }

    public void endElement(String name)
        throws SAXException
    {
        if (state == JUST_HAD_START_TAG) {
            state = OTHER;
        } else {
            maybeNewline();
        }
        documentHandler.endElement(name);
    }

    public void characters(char ch[], int start, int length)
        throws SAXException 
    {
        if (length > 0) {
            documentHandler.characters(ch, start, length);
            state = IN_PCDATA_CHUNK;
        }
    }

    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException 
    {
        if (length > 0) {
            documentHandler.ignorableWhitespace(ch, start, length);
            state = IN_PCDATA_CHUNK;
        }
    
    }

    public void rawCharacters(String chars) throws SAXException
    {
        if (chars.length() > 0) {
            rawCharactersHandler.rawCharacters(chars);
            state = IN_PCDATA_CHUNK;
        }
    }

    private final void maybeNewline() throws SAXException
    {
        if (state != IN_PCDATA_CHUNK) {
            documentHandler.characters(newline, 0, 1);
            newline[0] = '\n';
        }
        state = OTHER;
    }

    public void processingInstruction(String target, String data)
        throws SAXException 
    {
        maybeNewline();
        documentHandler.processingInstruction(target, data);
    }

    public void comment(String contents)
        throws SAXException 
    {
        if (commentHandler != null) {
            maybeNewline();
            commentHandler.comment(contents);
        }
    }
}
