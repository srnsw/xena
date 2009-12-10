// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;

import java.io.CharConversionException;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A DocumentHandler that writes an XML representation to
 * an OutputStream.
 */

public class XMLOutputHandler implements OutputDocumentHandler,
                                         CommentHandler,
                                         RawCharactersHandler
{
    private OutputStream out = null;
    private boolean keepOpen;
    private boolean inStartTag = false;
    private boolean omitXmlDeclaration = false;
    private String standalone;
    private static final int DEFAULT_BUF_LENGTH = 8*1024;
    private byte[] buf = new byte[DEFAULT_BUF_LENGTH];
    private int bufUsed = 0;
    private String lineSeparator;
    private byte minimize = MINIMIZE_EMPTY_ELEMENTS;
    private String doctypeSystem;
    private String doctypePublic;
    private boolean outputDoctype = false;

    static final public byte MINIMIZE_NONE = 0;
    static final public byte MINIMIZE_EMPTY_ELEMENTS = 1;
    static final public byte MINIMIZE_EMPTY_ELEMENTS_HTML = 2;

    /**
     * Create a XMLOutputHandler that will write in UTF-8
     * to an OutputStream.
     */
    public XMLOutputHandler() 
    {
        lineSeparator = System.getProperty("line.separator");
    }

    public XMLOutputHandler(OutputStream out) 
    {
        this();
        this.out = out;
    }

    public DocumentHandler init(Destination dest, AttributeList atts)
        throws IOException 
    {
        this.out = dest.getOutputStream("application/xml", null);
        this.keepOpen = dest.keepOpen();
        if ("yes".equals(atts.getValue("omit-xml-declaration")))
            omitXmlDeclaration = true;
        this.standalone = atts.getValue("standalone");
        this.doctypeSystem = atts.getValue("doctype-system");
        this.doctypePublic = atts.getValue("doctype-public");
        if (this.doctypeSystem != null || this.doctypePublic != null)
            outputDoctype = true;
        if ("yes".equals(atts.getValue("indent")))
            return new Indenter(this, this);
        return this;
    }

    public void setMinimize(byte minimize) 
    {
        this.minimize = minimize;
    }

    public void startDocument() throws SAXException 
    {
        if (!omitXmlDeclaration) {
            writeRaw("<?xml version=\"1.0\" encoding=\"utf-8\"");
            if (standalone != null) {
                writeRaw(" standalone=\"");
                writeRaw(standalone);
                put((byte)'"');
            }
            writeRaw("?>");
            writeRaw(lineSeparator);
        }
    }

    public void characters(char cbuf[], int off, int len) throws SAXException 
    {
        if (len == 0)
            return;
        if (inStartTag)
            finishStartTag();
        do {
            char c = cbuf[off++];
            switch (c) {
            case '\n':
                writeRaw(lineSeparator);
                break;
            case '&':
                writeRaw("&amp;");
                break;
            case '<':
                writeRaw("&lt;");
                break;
            case  '>':
                writeRaw("&gt;");
                break;
            default:
                if (c < 0x80)
                    put((byte)c);
                else {
                    try {
                        writeMB(c);
                    }
                    catch (CharConversionException e) {
                        if (len-- == 0)
                            throw new SAXException(e);
                        writeSurrogatePair(cbuf[off - 1], cbuf[off]);
                        off++;
                    }
                }
            }
        } while (--len > 0);
    }
    
    public void rawCharacters(String chars) throws SAXException 
    {
        if (inStartTag)
            finishStartTag();
        writeRaw(chars);
    }

    public void ignorableWhitespace (char ch[], int start, int length)
        throws SAXException 
    {
        for (; length > 0; length--, start++)
            put((byte)ch[start]);
    }

    private void writeRaw(String str) throws SAXException 
    {
        final int n = str.length();
        for (int i = 0; i < n; i++) {
            char c = str.charAt(i);
            if (c < 0x80)
                put((byte)c);
            else {
                try {
                    writeMB(str.charAt(i));
                }
                catch (CharConversionException e) {
                    if (++i == n)
                        throw new SAXException(e.getMessage());
                    writeSurrogatePair(c, str.charAt(i));
                }
            }
        }
    }

    private final void writeMB(char c) 
        throws SAXException, 
               CharConversionException 
    {
        switch (c & 0xF800) {
        case 0:
            put((byte)(((c >> 6) & 0x1F) | 0xC0));
            put((byte)((c & 0x3F) | 0x80));
            break;
        default:
            put((byte)(((c >> 12) & 0xF) | 0xE0));
            put((byte)(((c >> 6) & 0x3F) | 0x80));
            put((byte)((c & 0x3F) | 0x80));
            break;
        case 0xD800:
            throw new CharConversionException("invalid surrogate pair");
        }
    }
  
    private final void writeSurrogatePair(char c1, char c2) 
        throws SAXException 
    {
        if ((c1 & 0xFC00) != 0xD800 || (c2 & 0xFC00) != 0xDC00)
            throw new SAXException("invalid surrogate pair");
        int c = ((c1 & 0x3FF) << 10) | (c2 & 0x3FF);
        c += 0x10000;
        put((byte)(((c >> 18) & 0x7) | 0xF0));
        put((byte)(((c >> 12) & 0x3F) | 0x80));
        put((byte)(((c >> 6) & 0x3F) | 0x80));
        put((byte)((c & 0x3F) | 0x80));
    }

    public void startElement(String name, 
                             AttributeList atts) throws SAXException 
    {
        if (inStartTag)
            finishStartTag();
        if (outputDoctype) {
            outputDoctype = false;
            writeRaw("<!DOCTYPE ");
            writeRaw(name);
            if (doctypePublic != null) {
                writeRaw(" PUBLIC ");
                byte lit = doctypePublic.indexOf('"') >= 0 ? 
                    (byte)'\'' : 
                    (byte)'"';
                put(lit);
                writeRaw(doctypePublic);
                put(lit);
            }
            else
                writeRaw(" SYSTEM");
            if (doctypeSystem != null) {
                byte lit = doctypeSystem.indexOf('"') >= 0 ? 
                    (byte)'\'' : 
                    (byte)'"';
                put((byte)' ');
                put(lit);
                writeRaw(doctypeSystem);
                put(lit);
            }
            put((byte)'>');
            writeRaw(lineSeparator);
        }
        put((byte)'<');
        writeRaw(name);
        int n = atts.getLength();
        for (int i = 0; i < n; i++) {
            put((byte)' ');
            writeRaw(atts.getName(i));
            put((byte)'=');
            put((byte)'"');
            attributeValue(atts.getValue(i));
            put((byte)'"');
        }
        inStartTag = true;
    }

    protected void attributeValue(String value) 
        throws SAXException 
    {
        int valueLength = value.length();
        for (int j = 0; j < valueLength; j++) {
            char c = value.charAt(j);
            switch (c) {
            case '\n':
                writeRaw("&#10;");
                break;
            case '&':
                writeRaw("&amp;");
                break;
            case '<':
                writeRaw("&lt;");
                break;
            case '"':
                writeRaw("&quot;");
                break;
            case '\r':
                writeRaw("&#13;");
                break;
            case '\t':
                writeRaw("&#9;");
                break;
            default:
                if (c < 0x80)
                    put((byte)c);
                else {
                    try {
                        writeMB(c);
                    }
                    catch (CharConversionException e) {
                        if (++j == valueLength)
                            throw new SAXException(e.getMessage());
                        writeSurrogatePair(value.charAt(j - 1), value.charAt(j));
                    }
                }
                break;
            }
        }
    }

    private final void finishStartTag() throws SAXException 
    {
        inStartTag = false;
        put((byte)'>');
    }

    public void endElement(String name) throws SAXException
    {
        if (inStartTag) {
            inStartTag = false;
            if (minimize != MINIMIZE_NONE) {
                if (minimize == MINIMIZE_EMPTY_ELEMENTS_HTML)
                    put((byte)' ');
                put((byte)'/');
                put((byte)'>');
                return;
            }
            put((byte)'>');
        }
        put((byte)'<');
        put((byte)'/');
        writeRaw(name);
        put((byte)'>');
    }

    public void processingInstruction(String target, String data)
        throws SAXException 
    {
        if (target == null) {
            comment(data);
            return;
        }
        if (inStartTag) {
            finishStartTag();
        }
        put((byte)'<');
        put((byte)'?');
        writeRaw(target);
        if (data.length() > 0) {
            put((byte)' ');
            writeMarkup(data);
        }
        put((byte)'?');
        put((byte)'>');
    }

    public void markup(String chars) throws SAXException 
    {
        if (inStartTag) {
            finishStartTag();
        }
        writeMarkup(chars);
    }

    public void comment(String body) throws SAXException 
    {
        if (inStartTag) {
            finishStartTag();
        }
        writeRaw("<!--");
        writeMarkup(body);
        writeRaw("-->");
    }

    private void writeMarkup(String str) throws SAXException 
    {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '\n') {
                writeRaw(lineSeparator);
            } else if (c < 0x80) {
                put((byte)c);
            } else {
                try {
                    writeMB(c);
                }
                catch (CharConversionException e) {
                    if (++i == len) {
                        throw new SAXException(e);
                    }
                    writeSurrogatePair(c, str.charAt(i));
                }
            }
        }
    }

    private final void put(byte b) throws SAXException 
    {
        if (bufUsed == buf.length) {
            flushBuf();
        }
        buf[bufUsed++] = b;
    }

    private final void flushBuf() throws SAXException 
    {
        try {
            out.write(buf, 0, bufUsed);
            bufUsed = 0;
        }
        catch (java.io.IOException e) {
            throw new SAXException(e);
        }
    }

    public void setDocumentLocator(Locator loc) 
    { }

    public void endDocument() throws SAXException 
    {
        if (bufUsed != 0)
            flushBuf();
        try {
            if (out != null) {
                if (keepOpen)
                    out.flush();
                else
                    out.close();
                out = null;
            }
        }
        catch (java.io.IOException e) {
            throw new SAXException(e);
        }
        out = null;
        buf = null;
    }
}
