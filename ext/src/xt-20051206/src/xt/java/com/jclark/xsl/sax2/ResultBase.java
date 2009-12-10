//

package com.jclark.xsl.sax2;

import com.jclark.xsl.sax.CommentHandler;


import com.jclark.xsl.tr.*;
import com.jclark.xsl.om.*;

// import org.xml.sax.*;

import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.net.URL;

/**
 * abstract base class represents the results of executing a stylesheet Action --
 * constructs result components.
 *  events are fired to the Result, which may, in turn, fire
 * events to a SAX ContHandler
 */
public abstract class ResultBase implements Result, Attributes
{
    // downstream targets -- we propogate events to these guys
    private ContentHandler _contentHandler;
    private CommentHandler _commentHandler;
    ErrorHandler _errorHandler;
    private RawCharactersHandler _rawCharactersHandler;
    OutputMethodHandler _outputMethodHandler;

    // we'll collect TEXT here
    static private final int INITIAL_BUF_SIZE = 8192;
    private char[] buf = new char[INITIAL_BUF_SIZE];
    private int bufUsed = 0;

    // for Attributes implementation
    private Name[] _attributeNames = new Name[10];
    private String[] _attributeValues = new String[10];
    private int _nAttributes = 0;

    // what we'll be flushing when we encounter Element content
    private Name pendingElementType;
    private NamespacePrefixMap pendingNamespacePrefixMap;


    /**
     * Construct with an outputMethodHandler from which we'll
     * obtain the appropriate ContentHandler
     */
    ResultBase(OutputMethodHandler outputMethodHandler, 
               ErrorHandler errorHandler) 
    {
        _outputMethodHandler = outputMethodHandler;

        _contentHandler = null;
        _errorHandler = errorHandler;
    }

    /**
     * Construct with a ContentHandler already determined
     */
    ResultBase(ContentHandler contentHandler, ErrorHandler errorHandler) 
    {
        _outputMethodHandler = null;
        _errorHandler = errorHandler;
        setContentHandler(contentHandler);
    }

    //
    // set up all our downstream handlers: documentHandler,
    //                 commentHandler, rawCharactersHandler
    //  
    private void setContentHandler(ContentHandler handler) 
    {
        _contentHandler = handler;
        if (handler instanceof CommentHandler) {
            _commentHandler = (CommentHandler)handler;
        } else {
            _commentHandler = null;
        }

        if (handler instanceof RawCharactersHandler) {
            _rawCharactersHandler = (RawCharactersHandler)handler;
        } else {
            _rawCharactersHandler = null;
        }
    }

    /**
     *  call this when we're sure we're not getting any more
     *  attribute node constructing actions
     */
    public void flush() throws XSLException 
    {
        if (pendingElementType != null) {
            startElementContent(pendingElementType, pendingNamespacePrefixMap);
            pendingElementType = null;
        }
        else if (bufUsed > 0) {
            try {
                _contentHandler.characters(buf, 0, bufUsed);
                bufUsed = 0;
            }
            catch (SAXException e) {
                throwXSLException(e);
            }
        }
    }

    /**
     * rawCharacters are distinct from plain 'ol characters
     * in that we don't try to do any escaping
     */
    public void rawCharacters(String str) throws XSLException 
    {
        if (_rawCharactersHandler == null) {
            processingInstruction("javax.xml.transform.disable-output-escaping","");
            characters(str);
            processingInstruction("javax.xml.transform.enable-output-escaping","");
        } else {
            flush();
            try {
                _rawCharactersHandler.rawCharacters(str);
            }
            catch (SAXException e) {
                throwXSLException(e);
            }
        }
    }

    /**
     * construct some characters in the result
     */
    public void characters(String str) throws XSLException 
    {
        if (pendingElementType != null) {
            flush();
        }
        int strLength = str.length();
        if (bufUsed + strLength > buf.length) {
            // grow the char buffer
            // N.B. this could get big
            char[] oldBuf = buf;
            int newLen = oldBuf.length * 2;
            while (newLen < bufUsed + strLength) {
                newLen *= 2;
            }
            buf = new char[newLen];
            if (bufUsed > 0) {
                System.arraycopy(oldBuf, 0, buf, 
                                 0, bufUsed);
            }
        }

        // copy the string into the char array
        str.getChars(0, strLength, buf, bufUsed);
        bufUsed += strLength;
    }

    /**
     * construct a comment in our result
     */
    public void comment(String str) throws XSLException 
    {
        if (_commentHandler != null) {
            flush();
            try {
                _commentHandler.comment(fixComment(str));
            }
            catch (SAXException e) {
                throwXSLException(e);
            }
        }
    }


    /**
     * construct a processingInstruction in the result
     */
    public void processingInstruction(String target, String data)
        throws XSLException 
    {
        try {
            flush();
            _contentHandler.processingInstruction(target,
                                                  fixProcessingInstruction(data));
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }


    /**
     * start construction an Element ... we may yet get 
     * some attributes, so we'll wait a bit before propagating
     * the event downstream
     */
    public void startElement(Name elementType, NamespacePrefixMap nsMap) 
        throws XSLException 
    {
        flush();
        pendingElementType = elementType;
        pendingNamespacePrefixMap = nsMap;
        _nAttributes = 0;
    }

    public void endElement(Name elementType) throws XSLException
    {
        flush();
        endElementContent(elementType);
    }

    protected final ContentHandler getContentHandler() 
    {
        return _contentHandler;
    }

    /////////////////////////////////
    //
    // SAX Attributes implementation

    public int getIndex(String qName)
    {
        int len = _nAttributes; // getLength();
        for (int i = 0; i < len; i++) {
            if (qName.equals(getQName(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Attributes implementation: get the index of the Attribute
     * with the given Name components, or <code>-1</node> if
     * it does not exist 
     */
    public int getIndex(String namespaceURI, String localName)
    {
        if (namespaceURI == null || localName == null) {
            return -1;
        }
        int len = _nAttributes; // getLength();
        for (int i = 0; i < len; i++) {
            if (localName.equals(_attributeNames[i].getLocalPart())) {
                if (namespaceURI.equals(_attributeNames[i].getNamespace())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @return the number of attribute nodes we have at this moment
     */
    public int getLength() 
    {
        return _nAttributes;
    }

    /**
     * Attributes implementation: get the value of the Attribute 
     * with the given name in the given namespace
     * the String value of the Attribute, or <code>null</code> if 
     * it does not exist
     */
    public String getValue(String namespaceURI, String localName)
    {
        return getValue(getIndex(namespaceURI, localName));
    }

    /**
     * Attributes implementation: get the value of the i'th Attribute
     */
    public String getValue(int index) 
    {
        if (index < 0 || index > _nAttributes) {
            return null;
        }
        return _attributeValues[index];
    }

    /**
     * Attributes implementation: get the value of the named 
     *  Attribute
     */
    public String getValue(String qName) 
    {
        return getValue(getIndex(qName));
    }

    /**
     * Attributes implementation: get the namespace for the 
     * i'th Attribute's name
     */
    public String getURI(int index) 
    {
        if (index < 0 || index >= _nAttributes) {
            return null;
        }
        String uri = _attributeNames[index].getNamespace();
        return uri == null ?  "" : uri; 
    }

    /**
     * Attributes implementation: get the Attribute's local name
     */
    public String getLocalName(int index)
    {
        if (index < 0 || index >= _nAttributes) {
            return null;
        }
        String name = _attributeNames[index].getLocalPart();
        return name == null ?  "" : name; 
    }
 
    public String getQName(int index)
    {
        if (index < 0 || index >= _nAttributes) {
            return null;
        }
        String qName = _attributeNames[index].toString();
        return qName == null ? "" : qName;
    }

    /**
     * @return the type of the i'th Attribute (always "CDATA")
     */
    public String getType(int index) 
    {
        if (index < 0 || index >= _nAttributes) {
            return null;
        }
        return "CDATA";
    }

    /**
     * @return the type of the named Attribute (always "CDATA")
     */
    public String getType(String qName) 
    {
        return getType(getIndex(qName));
    }

    /**
     * @return the type of the named Attribute (always "CDATA")
     */
    public String getType(String namespaceURI, String localName) 
    {
        return getType(getIndex(namespaceURI, localName));
    }

    /**
     * @return the name of the i'th Attribute
     */
    protected final Name getAttributeName(int index) 
    {
        return _attributeNames[index];
    }

    ////////////////////////////////


    /**
     * notify that we're finished with adding attributes
     */
    protected abstract void startElementContent(Name elementType,
                                                NamespacePrefixMap nsMap)
        throws XSLException;

    /**
     * Notify the Element is ending
     */
    protected abstract void endElementContent(Name elementType)
        throws XSLException;

    /**
     * construct an Attribute with the given Name ... if we're not
     * at an appropriate point, eg, we've already started putting
     * text into an element, do nothing
     */
    public void attribute(Name name, String value) throws XSLException 
    {
        if (pendingElementType == null) {
            return;
        }

        // we may be doing a replace
        for (int i = 0; i < _nAttributes; i++) {
            if (_attributeNames[i].equals(name)) {
                _attributeValues[i] = value;
                return;
            }
        }

        // not doing a replace, and we need more room for this guy ...
        if (_nAttributes == _attributeNames.length) {
            _attributeNames = grow(_attributeNames);
            _attributeValues = grow(_attributeValues);
        }
        
        _attributeNames[_nAttributes] = name;
        _attributeValues[_nAttributes] = value;
        _nAttributes++;

    }

    /**
     * initialize, (and possibly construct) the ContentHandler
     * called by the transformation engine
     *
     * @param outputMethod the xsl:output parameters gleaned
     *   from the stylesheet 
     *  
     */
    public void start(OutputMethod outputMethod) throws XSLException 
    {
        try {
            if (_contentHandler == null) {
                Name name = outputMethod.getName();
                if (name == null) {
                    setContentHandler(new OutputMethodDefaulter(this, 
                                                                outputMethod));
                } else {
                    // has the side effect of setting _contentHandler
                    setOutputMethod(name, outputMethod);
                }
            }
            _contentHandler.startDocument();
        }
        catch (IOException e) {
            throw new XSLException(e);
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }

    /**
     * Copy a Result Tree Fragment to the Destination via the Handler
     */
    public abstract void resultTreeFragment(ResultTreeFragment frag) 
        throws XSLException;

    /**
     * get the appropriate ContentHandler from the 
     * outputMethodHandler (we've already obtained for our destination)
     * for the named output method
     */
    ContentHandler setOutputMethod(Name name, 
                                   OutputMethod method) 
        throws IOException, SAXException 
    {
        String nameString;
        if (name.getNamespace() != null) {
            nameString = (name.getNamespace()
                          + OutputMethodHandler.namespaceSeparator
                          + name.getLocalPart());
        } else {
            nameString = name.getLocalPart();
        }


        setContentHandler(_outputMethodHandler
                          .createContentHandler(nameString,
                                                new OutputMethodProperties(method)));

        return _contentHandler;
    }

    /**
     * flush any pending construction work, nothing else will be built
     */
    public void end() throws XSLException 
    {
        try {
            flush();
            _contentHandler.endDocument();
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }

    //
    //
    protected void throwXSLException(SAXException e) throws XSLException 
    {
        Exception wrapped = e.getException();
        if (wrapped != null) {
            throw new XSLException(wrapped);
        } else {
            throw new XSLException(e.getMessage());
        }
    }


    /**
     *
     */
    public void message(Node node, String str) throws XSLException 
    {
        if (_errorHandler != null) {
            String systemId = null;
            int lineNumber = -1;
            if (node != null) {
                URL url = node.getURL();
                if (url != null) {
                    systemId = url.toString();
                }
                lineNumber = node.getLineNumber();
            }
            try {
                _errorHandler.warning(new SAXParseException(str, null,
                                                            systemId, 
                                                            lineNumber, -1, 
                                                            null));
            }
            catch (SAXException e) {
                throwXSLException(e);
            }
        }
    }

    //////////////////////
    ///
    //

    // double the size of an array of strings
    static String[] grow(String[] v) 
    {
        String[] old = v;
        v = new String[old.length * 2];
        System.arraycopy(old, 0, v, 0, old.length);
        return v;
    }

    // double the size of an array of Names
    static Name[] grow(Name[] v) 
    {
        Name[] old = v;
        v = new Name[old.length * 2];
        System.arraycopy(old, 0, v, 0, old.length);
        return v;
    }

    //////////
    //

    /**
     * clean up occurences of "--" within comments 
     *  as they're illegal in XML 1.0
     */
    private static final String fixComment(String str)
    {
        int i = str.indexOf('-');
        while (i++ >= 0) {
            int len = str.length();
            if (i == len) {
                return str + " ";
            }
            if (str.charAt(i) == '-') {
                str = str.substring(0, i) + " " + str.substring(i);
            }
            i = str.indexOf('-', i);
        }
        return str;
    }

    /**
     * clean up processing instruction by breaking up any string: "?>"
     * which may appear therein
     */
    private static final String fixProcessingInstruction(String str) 
    {
        int i = str.indexOf('?');
        while (i++ >= 0) {
            int len = str.length();
            if (i == len) {
                break;
            }
            if (str.charAt(i) == '>') {
                str = str.substring(0, i) + " " + str.substring(i);
            }
            i = str.indexOf('?', i);
        }
        return str;
    }

}
