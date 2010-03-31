// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;

import java.io.Writer;
import java.io.IOException;
import java.util.Hashtable;

/**
 * A OutputDocumentHandler that writes an HTML representation.
 */
public class HTMLOutputHandler 
    implements OutputDocumentHandler, CommentHandler, RawCharactersHandler 
{

    private Writer writer;
    private static final int DEFAULT_BUF_LENGTH = 4*1024;
    private char[] buf = new char[DEFAULT_BUF_LENGTH];
    private int bufUsed = 0;
    private boolean inCdata = false;
    private boolean inPcdataChunk = true;
    private boolean inBlock = false;
    private final String lineSeparator = System.getProperty("line.separator");
    private boolean indent = true;
    private char maxRepresentableChar = '\uFFFF';
    private boolean keepOpen;
    private String encoding;
    private String doctypeSystem;
    private String doctypePublic;

    static private final int NORMAL_CONTENT = 0;
    static private final int EMPTY_CONTENT = 1;
    static private final int CDATA_CONTENT = 2;
    static private final int CONTENT_TYPE = 3;
    static private final int BLOCK_ELEMENT = 4;
    static private final int HEAD_ELEMENT = 8;
    static private final int PCDATA_ELEMENT = 16;

    public HTMLOutputHandler() { }

    public HTMLOutputHandler(Writer writer) 
    {
        this.writer = writer;
    }
  
    public DocumentHandler init(Destination dest, AttributeList atts)
        throws IOException 
    {
        String mediaType = atts.getValue("media-type");
        if (mediaType == null) {
            mediaType = "text/html";
        }
        encoding = atts.getValue("encoding");
        if (encoding == null) {
            // not all Java implementations support ASCII
            writer = dest.getWriter(mediaType, "iso-8859-1");
            // use character references for non-ASCII characters
            maxRepresentableChar = '\u007F';
        }
        else {
            writer = dest.getWriter(mediaType, encoding);
            encoding = dest.getEncoding();
            if (encoding.equalsIgnoreCase("iso-8859-1")) {
                maxRepresentableChar = '\u00FF';
            }
            else if (encoding.equalsIgnoreCase("us-ascii")) {
                maxRepresentableChar = '\u007F';
            }
        }
        doctypeSystem = atts.getValue("doctype-system");
        doctypePublic = atts.getValue("doctype-public");
        keepOpen = dest.keepOpen();
        if ("no".equals(atts.getValue("indent"))) {
            indent = false;
        }
        return this;
    }

    public void setWriter(Writer writer)
    {
        this.writer = writer;
    }

    public void startDocument() throws SAXException 
    {
    }

    public void characters(char[] ch, int off, int len) throws SAXException 
    {
        if (len == 0) {
            return;
        }
        inPcdataChunk = true;
        if (inCdata) {
            writeUnquoted(new String(ch, off, len));
        } else {
            for (; len > 0; len--, off++) {
                char c = ch[off];
                switch (c) {
                case '\n':
                    write(lineSeparator);
                    break;
                case '&':
                    write("&amp;");
                    break;
                case '<':
                    write("&lt;");
                    break;
                case '>':
                    write("&gt;");
                    break;
                case '\u00A0':
                    write("&nbsp;");
                    break;
                default:
                    if (c <= maxRepresentableChar) {
                        write(c);
                    } else {
                        write(getCharString(c));
                    }
                    break;
                }
            }
        }
    }

    public void ignorableWhitespace(char[] ch, int off,
                                    int len) 
        throws SAXException 
    {
        characters(ch, off, len);
    }

    public void rawCharacters(String chars) throws SAXException 
    {
        if (chars.length() != 0) {
            writeUnquoted(chars);
            inPcdataChunk = true;
        }
    }

    private void writeUnquoted(String str) throws SAXException 
    {
        int start = 0;
        for (;;) {
            int i = str.indexOf('\n', start);
            if (i < 0) {
                break;
            }
            if (i > start) {
                write(str.substring(start, i));
            }
            write(lineSeparator);
            start = i + 1;
        }
        write(start == 0 ? str : str.substring(start));
    }

    public void startElement(String name, AttributeList atts)
        throws SAXException 
    {
        if (inCdata) { // FIXME need to keep a count
            return;
        }
        if (doctypeSystem != null || doctypePublic != null) {
            write("<!DOCTYPE ");
            write(name.equals("HTML") ? "HTML" : "html");
            if (doctypePublic != null) {
                write(" PUBLIC ");
                char lit = doctypePublic.indexOf('"') >= 0 ? '\'' : '"';
                write(lit);
                write(doctypePublic);
                write(lit);
            } else {
                write(" SYSTEM");
            }
            if (doctypeSystem != null) {
                char lit = doctypeSystem.indexOf('"') >= 0 ? '\'' : '"';
                write(' ');
                write(lit);
                write(doctypeSystem);
                write(lit);
            }
            write('>');
            doctypeSystem = null;
            doctypePublic = null;
            write(lineSeparator);
        }
        int flags = getElementTypeFlags(name);
        int contentType = (flags & CONTENT_TYPE);
        boolean isBlockElement = (flags & BLOCK_ELEMENT) != 0;
        if (inPcdataChunk) {
            inPcdataChunk = false;
        } else if (indent && (!inBlock || isBlockElement)) {
            write(lineSeparator);
        }
        inBlock = !isBlockElement;
        write('<');
        write(name);
        int nAtts = atts.getLength();
        for (int i = 0; i < nAtts; i++)
            attribute(atts.getName(i), atts.getValue(i));
        if (contentType == CDATA_CONTENT)
            inCdata = true;
        write('>');
        if (encoding != null && (flags & HEAD_ELEMENT) != 0) {
            write(lineSeparator
                  + "<META http-equiv=\"Content-Type\" content=\"text/html; charset="
                  + encoding + "\">");
        }
    }

    private void attribute(String name, String value) throws SAXException 
    {
        write(' ');
        write(name);
        if (!isBooleanAttribute(name, value)) {
            write('=');
            write('"');
            int len = value.length();
            for (int i = 0; i < len; i++) {
                char c = value.charAt(i);
                switch (c) {
                case '\n':
                    write(lineSeparator);
                    break;
                case '&':
                    // Handle JavaScript macros (see HTML 4.0 B.7.1)
                    if (i + 1 < len && value.charAt(i + 1) == '{')
                        write(c);
                    else
                        write("&amp;");
                    break;
                case '"':
                    write("&quot;");
                    break;
                case '\u00A0':
                    write("&nbsp;");
                    break;
                default:
                    if (c <= maxRepresentableChar) {
                        write(c);
                    } else {
                        write(getCharString(c));
                    }
                    break;
                }
            }
            write('"');
        }
    }

    public void endElement(String name) throws SAXException 
    {
        int flags = getElementTypeFlags(name);
        int contentType = (flags & CONTENT_TYPE);
        boolean isBlockElement = (flags & BLOCK_ELEMENT) != 0;
        if (contentType != EMPTY_CONTENT) {
            if (inPcdataChunk) {
                inPcdataChunk = false;
            } else if (indent && (!inBlock || isBlockElement)) {
                write(lineSeparator);
            }
            inBlock = !isBlockElement;
            write('<');
            write('/');
            write(name);
            write('>');
        }
        if ((flags & PCDATA_ELEMENT) != 0) {
            inPcdataChunk = true;
        }
        inCdata = false;
    }

    public void comment(String str) throws SAXException 
    {
        write("<!--");
        // FIXME deal with -- in str
        writeUnquoted(str);
        write("-->");
    }

    public void processingInstruction(String target, String data)
        throws SAXException 
    {
        if (target == null) {
            comment(data);
            return;
        }
        write("<?");
        // FIXME deal with > in str
        write(target);
        if (data.length() != 0) {
            write(' ');
            writeUnquoted(data);
        }
        write('>');
    }

    static private final String emptyElements[] = {
        "area",
        "base",
        "basefont",
        "br",
        "col",
        "frame",
        "hr",
        "img",
        "input",
        "isindex",
        "link",
        "meta",
        "param"
    };

    static private final String cdataElements[] = {
        "script",
        "style"
    };

    static private final String blockElements[] = {
        "address",
        "area",
        "base",
        "blockquote",
        "body",
        "br",
        "caption",
        "center",
        "col",
        "colgroup",
        "dd",
        "dir",
        "div",
        "dl",
        "dt",
        "fieldset",
        "form",
        "frame",
        "frameset",
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "head",
        "hr",
        "html",
        "isindex",
        "li",
        "link",
        "map",
        "menu",
        "meta",
        "noframes",
        "noscript",
        "ol",
        "p",
        "pre",
        "style",
        "table",
        "tbody",
        "tfoot",
        "thead",
        "title",
        "tr",
        "ul"
    };

    static private final String pcdataElements[] = {
        "applet",
        "img",
        "object"
    };

    static private final String booleanAttributes[] = {
        "checked",
        "compact",
        "declare",
        "defer",
        "disabled",
        "ismap",
        "multiple",
        "nohref",
        "noresize",
        "noshade",
        "nowrap",
        "readonly",
        "selected"
    };

    static private final String charEntities[] = {
        "\u00A0nbsp",
        "\u00A1iexcl",
        "\u00A2cent",
        "\u00A3pound",
        "\u00A4curren",
        "\u00A5yen",
        "\u00A6brvbar",
        "\u00A7sect",
        "\u00A8uml",
        "\u00A9copy",
        "\u00AAordf",
        "\u00ABlaquo",
        "\u00ACnot",
        "\u00ADshy",
        "\u00AEreg",
        "\u00AFmacr",
        "\u00B0deg",
        "\u00B1plusmn",
        "\u00B2sup2",
        "\u00B3sup3",
        "\u00B4acute",
        "\u00B5micro",
        "\u00B6para",
        "\u00B7middot",
        "\u00B8cedil",
        "\u00B9sup1",
        "\u00BAordm",
        "\u00BBraquo",
        "\u00BCfrac14",
        "\u00BDfrac12",
        "\u00BEfrac34",
        "\u00BFiquest",
        "\u00C0Agrave",
        "\u00C1Aacute",
        "\u00C2Acirc",
        "\u00C3Atilde",
        "\u00C4Auml",
        "\u00C5Aring",
        "\u00C6AElig",
        "\u00C7Ccedil",
        "\u00C8Egrave",
        "\u00C9Eacute",
        "\u00CAEcirc",
        "\u00CBEuml",
        "\u00CCIgrave",
        "\u00CDIacute",
        "\u00CEIcirc",
        "\u00CFIuml",
        "\u00D0ETH",
        "\u00D1Ntilde",
        "\u00D2Ograve",
        "\u00D3Oacute",
        "\u00D4Ocirc",
        "\u00D5Otilde",
        "\u00D6Ouml",
        "\u00D7times",
        "\u00D8Oslash",
        "\u00D9Ugrave",
        "\u00DAUacute",
        "\u00DBUcirc",
        "\u00DCUuml",
        "\u00DDYacute",
        "\u00DETHORN",
        "\u00DFszlig",
        "\u00E0agrave",
        "\u00E1aacute",
        "\u00E2acirc",
        "\u00E3atilde",
        "\u00E4auml",
        "\u00E5aring",
        "\u00E6aelig",
        "\u00E7ccedil",
        "\u00E8egrave",
        "\u00E9eacute",
        "\u00EAecirc",
        "\u00EBeuml",
        "\u00ECigrave",
        "\u00EDiacute",
        "\u00EEicirc",
        "\u00EFiuml",
        "\u00F0eth",
        "\u00F1ntilde",
        "\u00F2ograve",
        "\u00F3oacute",
        "\u00F4ocirc",
        "\u00F5otilde",
        "\u00F6ouml",
        "\u00F7divide",
        "\u00F8oslash",
        "\u00F9ugrave",
        "\u00FAuacute",
        "\u00FBucirc",
        "\u00FCuuml",
        "\u00FDyacute",
        "\u00FEthorn",
        "\u00FFyuml",
        "\u0152OElig",
        "\u0153oelig",
        "\u0160Scaron",
        "\u0161scaron",
        "\u0178Yuml",
        "\u0192fnof",
        "\u02C6circ",
        "\u02DCtilde",
        "\u0391Alpha",
        "\u0392Beta",
        "\u0393Gamma",
        "\u0394Delta",
        "\u0395Epsilon",
        "\u0396Zeta",
        "\u0397Eta",
        "\u0398Theta",
        "\u0399Iota",
        "\u039AKappa",
        "\u039BLambda",
        "\u039CMu",
        "\u039DNu",
        "\u039EXi",
        "\u039FOmicron",
        "\u03A0Pi",
        "\u03A1Rho",
        "\u03A3Sigma",
        "\u03A4Tau",
        "\u03A5Upsilon",
        "\u03A6Phi",
        "\u03A7Chi",
        "\u03A8Psi",
        "\u03A9Omega",
        "\u03B1alpha",
        "\u03B2beta",
        "\u03B3gamma",
        "\u03B4delta",
        "\u03B5epsilon",
        "\u03B6zeta",
        "\u03B7eta",
        "\u03B8theta",
        "\u03B9iota",
        "\u03BAkappa",
        "\u03BBlambda",
        "\u03BCmu",
        "\u03BDnu",
        "\u03BExi",
        "\u03BFomicron",
        "\u03C0pi",
        "\u03C1rho",
        "\u03C2sigmaf",
        "\u03C3sigma",
        "\u03C4tau",
        "\u03C5upsilon",
        "\u03C6phi",
        "\u03C7chi",
        "\u03C8psi",
        "\u03C9omega",
        "\u03D1thetasym",
        "\u03D2upsih",
        "\u03D6piv",
        "\u2002ensp",
        "\u2003emsp",
        "\u2009thinsp",
        "\u200Czwnj",
        "\u200Dzwj",
        "\u200Elrm",
        "\u200Frlm",
        "\u2013ndash",
        "\u2014mdash",
        "\u2018lsquo",
        "\u2019rsquo",
        "\u201Asbquo",
        "\u201Cldquo",
        "\u201Drdquo",
        "\u201Ebdquo",
        "\u2020dagger",
        "\u2021Dagger",
        "\u2022bull",
        "\u2026hellip",
        "\u2030permil",
        "\u2032prime",
        "\u2033Prime",
        "\u2039lsaquo",
        "\u203Arsaquo",
        "\u203Eoline",
        "\u2044frasl",
        "\u20ACeuro",
        "\u2111image",
        "\u2118weierp",
        "\u211Creal",
        "\u2122trade",
        "\u2135alefsym",
        "\u2190larr",
        "\u2191uarr",
        "\u2192rarr",
        "\u2193darr",
        "\u2194harr",
        "\u21B5crarr",
        "\u21D0lArr",
        "\u21D1uArr",
        "\u21D2rArr",
        "\u21D3dArr",
        "\u21D4hArr",
        "\u2200forall",
        "\u2202part",
        "\u2203exist",
        "\u2205empty",
        "\u2207nabla",
        "\u2208isin",
        "\u2209notin",
        "\u220Bni",
        "\u220Fprod",
        "\u2211sum",
        "\u2212minus",
        "\u2217lowast",
        "\u221Aradic",
        "\u221Dprop",
        "\u221Einfin",
        "\u2220ang",
        "\u2227and",
        "\u2228or",
        "\u2229cap",
        "\u222Acup",
        "\u222Bint",
        "\u2234there4",
        "\u223Csim",
        "\u2245cong",
        "\u2248asymp",
        "\u2260ne",
        "\u2261equiv",
        "\u2264le",
        "\u2265ge",
        "\u2282sub",
        "\u2283sup",
        "\u2284nsub",
        "\u2286sube",
        "\u2287supe",
        "\u2295oplus",
        "\u2297otimes",
        "\u22A5perp",
        "\u22C5sdot",
        "\u2308lceil",
        "\u2309rceil",
        "\u230Alfloor",
        "\u230Brfloor",
        "\u2329lang",
        "\u232Arang",
        "\u25CAloz",
        "\u2660spades",
        "\u2663clubs",
        "\u2665hearts",
        "\u2666diams"
    };

    static private String[][] charMap = new String[256][];
    static private Hashtable elementTypeTable = new Hashtable();
    static private Hashtable booleanAttributesTable = new Hashtable();

    static {
        for (int i = 0; i < charEntities.length; i++) {
            int c = charEntities[i].charAt(0);
            int lo = c & 0xff;
            int hi = c >> 8;
            if (charMap[hi] == null)
                charMap[hi] = new String[256];
            charMap[hi][lo] = "&" + charEntities[i].substring(1) + ";";
        }
        char[] charBuf = new char[1];
        for (int i = 0; i < 128; i++) {
            if (charMap[0][i] == null) {
                charBuf[0] = (char)i;
                charMap[0][i] = new String(charBuf);
            }
        }
        Integer type = new Integer(BLOCK_ELEMENT);
        for (int i = 0; i < blockElements.length; i++)
            elementTypeTable.put(blockElements[i], type);
        Integer blockType = new Integer(EMPTY_CONTENT|BLOCK_ELEMENT);
        Integer inlineType = new Integer(EMPTY_CONTENT);
        for (int i = 0; i < emptyElements.length; i++) {
            if (elementTypeTable.get(emptyElements[i]) == null)
                type = inlineType;
            else
                type = blockType;
            elementTypeTable.put(emptyElements[i], type);
        }
        blockType = new Integer(CDATA_CONTENT|BLOCK_ELEMENT);
        inlineType = new Integer(CDATA_CONTENT);
        for (int i = 0; i < cdataElements.length; i++) {
            if (elementTypeTable.get(cdataElements[i]) == null)
                type = inlineType;
            else
                type = blockType;
            elementTypeTable.put(cdataElements[i], type);
        }
        for (int i = 0; i < pcdataElements.length; i++) {
            type = (Integer)elementTypeTable.get(pcdataElements[i]);
            if (type == null)
                type = new Integer(PCDATA_ELEMENT);
            else
                type = new Integer(PCDATA_ELEMENT|type.intValue());
            elementTypeTable.put(pcdataElements[i], type);
        }
        for (int i = 0; i < booleanAttributes.length; i++)
            booleanAttributesTable.put(booleanAttributes[i], booleanAttributes[i]);
        elementTypeTable.put("head", new Integer(BLOCK_ELEMENT|HEAD_ELEMENT));
    }


    private static String getCharString(char c) 
    {
        String[] v = charMap[c >> 8];
        if (v == null) {
            v = new String[256];
            charMap[c >> 8] = v;
        }
        String name = v[c & 0xFF];
        if (name == null) {
            name = "&#" + Integer.toString(c) + ";";
            v[c & 0xFF] = name;
        }
        return name;
    }

    private static int getElementTypeFlags(String name)
    {
        Integer type
            = (Integer)elementTypeTable.get(name.toLowerCase());
        if (type == null) {
            return 0;
        }
        return type.intValue();
    }

    private static boolean isBooleanAttribute(String name, String value) 
    {
        if (!name.equalsIgnoreCase(value))
            return false;
        return booleanAttributesTable.get(name.toLowerCase()) != null;
    }

    private final void write(String s) throws SAXException 
    {
        int start = 0;
        int len = s.length();
        int avail = buf.length - bufUsed;
        while (avail < len) {
            s.getChars(start, start + avail, buf, bufUsed);
            bufUsed = buf.length;
            flushBuf();
            start += avail;
            len -= avail;
            avail = buf.length;
        }
        s.getChars(start, start + len, buf, bufUsed);
        bufUsed += len;
    }

    private final void write(char b) throws SAXException
    {
        if (bufUsed == buf.length) {
            flushBuf();
        }
        buf[bufUsed++] = b;
    }

    private final void flushBuf() throws SAXException
    {
        try {
            writer.write(buf, 0, bufUsed);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
        bufUsed = 0;
    }

    public void endDocument() throws SAXException
    {
        write(lineSeparator);
        if (bufUsed != 0) {
            flushBuf();
        }
        try {
            if (keepOpen) {
                writer.flush();
            } else {
                writer.close();
            }
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
        writer = null;
        buf = null;
    }

    public void setDocumentLocator(org.xml.sax.Locator loc)
    { }

}
