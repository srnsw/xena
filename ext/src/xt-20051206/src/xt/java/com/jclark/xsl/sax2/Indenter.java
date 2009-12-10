// $Id$

package com.jclark.xsl.sax2;

import com.jclark.xsl.sax.CommentHandler;
import com.jclark.xsl.sax.Destination;

import org.xml.sax.*;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * performs "pretty-printing" by wrapping  another 
 * ContentHandler, intercepting SAX events,
 * and inserting whitespace events as appropriate.
 *
 * ... (Actually, I don't think it adds any
 * indenting, just newlines)
 */
public class Indenter 
    implements OutputContentHandler, CommentHandler,
               RawCharactersHandler
{
    private ContentHandler _contentHandler = null;
    private CommentHandler _commentHandler = null;
    private RawCharactersHandler _rawCharactersHandler;
    private char[] _newline = new char[]{'\n'};

    static private final byte IN_PCDATA_CHUNK = 0;
    static private final byte JUST_HAD_START_TAG = 1;
    static private final byte OTHER = 2;

    private byte _state = IN_PCDATA_CHUNK;

    public Indenter(ContentHandler handler,
                    RawCharactersHandler rawCharactersHandler) 
    {
        _contentHandler = handler;
        if (handler instanceof CommentHandler) {
            _commentHandler = (CommentHandler)handler;
        } else {
            _commentHandler = null;
        }
        _rawCharactersHandler = rawCharactersHandler;
    }

    public ContentHandler init(Destination dest, Properties atts)
        throws IOException, SAXException
    {
        if (_contentHandler instanceof OutputContentHandler) {
            _contentHandler = 
                ( (OutputContentHandler) _contentHandler).init(dest, atts);
        }
        return this;
    }

    public void setDocumentLocator(Locator locator)
    {
        _contentHandler.setDocumentLocator(locator);
    }

    public void startDocument()
        throws SAXException
    {
        _contentHandler.startDocument();
    }

    public void endDocument()
        throws SAXException
    {
        maybeNewline();
        _contentHandler.endDocument();
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName, 
                             Attributes atts)
        throws SAXException
    {
        maybeNewline();
        _state = JUST_HAD_START_TAG;
        _contentHandler.startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException
    {
        if (_state == JUST_HAD_START_TAG) {
            _state = OTHER;
        } else {
            maybeNewline();
        }
        _contentHandler.endElement(namespaceURI, localName, qName);
    }

    public void characters(char ch[], int start, int length)
        throws SAXException 
    {
        if (length > 0) {
            _contentHandler.characters(ch, start, length);
            _state = IN_PCDATA_CHUNK;
        }
    }

    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException 
    {
        if (length > 0) {
            _contentHandler.ignorableWhitespace(ch, start, length);
            _state = IN_PCDATA_CHUNK;
        }
    
    }

    public void rawCharacters(String chars) throws SAXException
    {
        if (chars.length() > 0) {
            _rawCharactersHandler.rawCharacters(chars);
            _state = IN_PCDATA_CHUNK;
        }
    }

    public void startPrefixMapping(String prefix, String namespaceURI)
        throws SAXException
    {
        _contentHandler.startPrefixMapping(prefix, namespaceURI);
    }

    public void endPrefixMapping(String prefix)
        throws SAXException
    {
        _contentHandler.endPrefixMapping(prefix);
    }

    public void skippedEntity(String name)
        throws SAXException
    {
        _contentHandler.skippedEntity(name);
    }

    //
    //
    private final void maybeNewline() throws SAXException
    {
        if (_state != IN_PCDATA_CHUNK) {
            _contentHandler.characters(_newline, 0, 1);
            _newline[0] = '\n';
        }
        _state = OTHER;
    }

    public void processingInstruction(String target, String data)
        throws SAXException 
    {
        maybeNewline();
        _contentHandler.processingInstruction(target, data);
    }

    public void comment(String contents)
        throws SAXException 
    {
        if (_commentHandler != null) {
            maybeNewline();
            _commentHandler.comment(contents);
        }
    }
}
