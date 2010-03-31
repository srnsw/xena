// $Id$

package com.jclark.xsl.sax;

import com.jclark.xsl.tr.*;
import com.jclark.xsl.om.*;
import org.xml.sax.*;
import java.io.IOException;
import java.net.URL;

/**
 * abstract base class represents the results of executing a stylesheet Action --
 * constructs result components.
 *  events are fired to the Result, which in turn fires
 * events to a SAX (1) DocumentHandler
 */
public abstract class ResultBase implements Result, AttributeList
{

    // FIXME: we should maybe move this to SAX2? (contentHandler)
    private DocumentHandler documentHandler;
    private CommentHandler commentHandler;
    ErrorHandler errorHandler;
    private RawCharactersHandler rawCharactersHandler;
    static private final int INITIAL_BUF_SIZE = 8192;
    private char[] buf = new char[INITIAL_BUF_SIZE];
    private int bufUsed = 0;
    private Name[] attributeNames = new Name[10];
    private String[] attributeValues = new String[10];
    private int nAttributes;
    private Name pendingElementType;
    private NamespacePrefixMap pendingNamespacePrefixMap;
    OutputMethodHandler outputMethodHandler;

    /**
     * Construct with an outputMethodHandler from which we'll
     * obtain the appropriate DocumentHandler
     */
    ResultBase(OutputMethodHandler outputMethodHandler, 
               ErrorHandler errorHandler) 
    {
        this.outputMethodHandler = outputMethodHandler;
        this.documentHandler = null;
        this.errorHandler = errorHandler;
    }

    /**
     * Construct with a DocummentHandler already determined
     */
    ResultBase(DocumentHandler documentHandler, ErrorHandler errorHandler) 
    {
        this.outputMethodHandler = null;
        this.errorHandler = errorHandler;
        setDocumentHandler(documentHandler);
    }

    //
    // set up all our downstream handlers: documentHandler,
    //                 commentHandler, rawCharactersHandler
    //  
    private void setDocumentHandler(DocumentHandler handler) 
    {
        documentHandler = handler;
        if (handler instanceof CommentHandler) {
            commentHandler = (CommentHandler)handler;
        } else {
            commentHandler = null;
        }

        if (handler instanceof RawCharactersHandler) {
            rawCharactersHandler = (RawCharactersHandler)handler;
        } else {
            rawCharactersHandler = null;
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
                documentHandler.characters(buf, 0, bufUsed);
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
        if (rawCharactersHandler == null) {
            characters(str);
        } else {
            flush();
            try {
                rawCharactersHandler.rawCharacters(str);
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
        if (commentHandler != null) {
            flush();
            try {
                commentHandler.comment(fixComment(str));
            }
            catch (SAXException e) {
                throwXSLException(e);
            }
        }
    }

    /**
     * clean up occurences of "--" within comments 
     * (they're illegal in XML 1.0)
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
     * construct a processingInstruction in the result
     */
    public void processingInstruction(String target, String data)
        throws XSLException 
    {
        try {
            flush();
            documentHandler.processingInstruction(target,
                                                  fixProcessingInstruction(data));
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
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

    /**
     * start construction an Element
     */
    public void startElement(Name elementType, NamespacePrefixMap nsMap) 
        throws XSLException 
    {
        flush();
        pendingElementType = elementType;
        pendingNamespacePrefixMap = nsMap;
        nAttributes = 0;
    }

    public void endElement(Name elementType) throws XSLException
    {
        flush();
        endElementContent(elementType);
    }

    protected final DocumentHandler getDocumentHandler() 
    {
        return documentHandler;
    }

    /**
     * @return the number of attribute nodes we have at this moment
     */
    public int getLength() 
    {
        return nAttributes;
    }

    /**
     * @return the name of the i'th Attribute
     */
    protected final Name getAttributeName(int i) 
    {
        return attributeNames[i];
    }

    /**
     * @return the value of the i'th Attribute
     */
    public String getValue(int i) 
    {
        return attributeValues[i];
    }

    /**
     * @return the type of the i'th Attribute (always "CDATA")
     */
    public String getType(int i) 
    {
        return "CDATA";
    }

    /**
     * @return the type of the named Attribute (always "CDATA")
     */
    public String getType(String name) 
    {
        return "CDATA";
    }

    /**
     * @return the value of the named Attribute (always "CDATA")
     *  .. not sure how these names work with namespaces
     *  returns null if the named Attribute is not found
     */
    public String getValue(String name) 
    {
        int len = getLength();
        for (int i = 0; i < len; i++) {
            if (name.equals(getName(i))) {
                return getValue(i);
            }
        }
        return null;
    }

    /**
     * we're finished with adding attributes?
     */
    protected abstract void startElementContent(Name elementType,
                                                NamespacePrefixMap nsMap)
        throws XSLException;

    /**
     *
     */
    protected abstract void endElementContent(Name elementType)
        throws XSLException;

    /**
     * construct an Attribute with the given name ... if we're not
     * at an appropriate point, eg, we've already started putting
     * text into an element, do nothing
     */
    public void attribute(Name name, String value) throws XSLException 
    {
        if (pendingElementType == null) {
            return;
        }
        for (int i = 0; i < nAttributes; i++) {
            if (attributeNames[i].equals(name)) {
                attributeValues[i] = value;
                return;
            }
        }
        if (nAttributes == attributeNames.length) {
            attributeNames = grow(attributeNames);
            attributeValues = grow(attributeValues);
        }
        attributeNames[nAttributes] = name;
        attributeValues[nAttributes] = value;
        nAttributes++;
    }

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

    /**
     * initialize, (and possibly construct) the DocumentHandler
     */
    public void start(OutputMethod outputMethod) throws XSLException 
    {
        try {
            if (documentHandler == null) {
                Name name = outputMethod.getName();
                if (name == null) {
                    setDocumentHandler(new OutputMethodDefaulter(this, 
                                                                 outputMethod));
                } else {
                    setOutputMethod(name, outputMethod);
                }
            }
            documentHandler.startDocument();
        }
        catch (IOException e) {
            throw new XSLException(e);
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }

    /**
     * get the appropriate DocumentHandler from the 
     * outputMethodHandler (we've already obtained for our destiantion)
     * for the named output method
     */
    DocumentHandler setOutputMethod(Name name, 
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
        setDocumentHandler(outputMethodHandler
                           .createDocumentHandler(nameString,
                                                  new OutputMethodAttributeList(method)));
        return documentHandler;
    }

    /**
     * flush any pending construction work, nothing else will be built
     */
    public void end() throws XSLException 
    {
        try {
            flush();
            documentHandler.endDocument();
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
    public abstract void resultTreeFragment(ResultTreeFragment frag) 
        throws XSLException;

    /**
     *
     */
    public void message(Node node, String str) throws XSLException 
    {
        if (errorHandler != null) {
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
                errorHandler.warning(new SAXParseException(str, null,
                                                           systemId, 
                                                           lineNumber, -1, 
                                                           null));
            }
            catch (SAXException e) {
                throwXSLException(e);
            }
        }
    }
}
