// $Id$

package com.jclark.xsl.sax2;

import org.xml.sax.*;
import com.jclark.xsl.om.*;
import com.jclark.xsl.tr.*;

/**
 *
 */
public class MultiNamespaceResult extends ResultBase
{

    // FIXME: we can probably remove all this pseudoattribute stuff, and
    //  just send start/end  mapping events

    private NamespacePrefixMap[] elementMaps = new NamespacePrefixMap[20];
    private NamespacePrefixMap currentMap = null;
    private int elementDepth = 0;

    private String attributeNameStrings[] = new String[20];
    private String pseudoAttributeValues[] = new String[10];
    private int nPseudoAttributes = 0;

    public MultiNamespaceResult(OutputMethodHandler outputMethodHandler,
                                ErrorHandler errorHandler)
    {
        super(outputMethodHandler, errorHandler);
    }

    public MultiNamespaceResult(ContentHandler contentHandler, 
                                ErrorHandler errorHandler)
    {
        super(contentHandler, errorHandler);
    }

    /**
     * Create a Result that can write to the given uri. Used to
     * implement the "document" extension element,
     * we can only do this if we already have an OutputMethodHandler
     * instead of a ContentHandler for the transform results
     */
    public Result createResult(String uri) throws XSLException
    {
        if (_outputMethodHandler != null) {
            OutputMethodHandler om = 
                _outputMethodHandler.createOutputMethodHandler(uri);
            if (om != null) {
                return new MultiNamespaceResult(om, _errorHandler);
            }
        }
        return null;
    }

    protected void startElementContent(Name elementType, 
                                       NamespacePrefixMap map)
        throws XSLException
    {

        // we maintain a stack of NameSpacePrefixMaps
        if (elementDepth >= elementMaps.length) {
            //  grow the stack
            NamespacePrefixMap[] oldElementMaps = elementMaps;
            elementMaps = new NamespacePrefixMap[oldElementMaps.length * 2];
            System.arraycopy(oldElementMaps, 0, elementMaps, 0,
                             oldElementMaps.length);
        }
        elementMaps[elementDepth++] = currentMap;

        nPseudoAttributes = 0;

        // we have some new namespace prefix mappings coming into scope
        if (map != currentMap) {
            int size = map.getSize();
            boolean changed = false;
            for (int i = 0; i < size; i++) {
                String prefix = map.getPrefix(i);
                String namespace = map.getNamespace(i);

                if (currentMap == null || 
                    !namespace.equals(currentMap.getNamespace(prefix))) {
                    changed = true;
                    addPseudoAttribute("xmlns:" + prefix, namespace);
                    try {
                        getContentHandler().startPrefixMapping(prefix, namespace);
                    } catch (SAXException ex) {
                        throw new XSLException (ex);
                    }

                }
            }

            String defaultNamespace = map.getDefaultNamespace();
            if (defaultNamespace == null) {
                if (currentMap == null) {
                    changed = true;
                } else if (currentMap.getDefaultNamespace() != null) {
     
                    // resets the default namespace to empty
                    addPseudoAttribute("xmlns", "");

                    // CHECKME: not too sure 'bout this
                    try { 
                        getContentHandler().startPrefixMapping("", "");
                    } catch (SAXException ex) {
                        throw new XSLException (ex);
                    }
                    changed = true;
                }
            }
            else if (currentMap == null || 
                     !defaultNamespace.equals(currentMap.getDefaultNamespace())) {
                changed = true;
                addPseudoAttribute("xmlns", defaultNamespace);
                try {
                    getContentHandler().startPrefixMapping("", defaultNamespace);
                } catch (SAXException ex) {
                    throw new XSLException (ex);
                }
            }
            if (changed) {
                currentMap = map;
            }
        }

        int nAtts = super.getLength();

        if (attributeNameStrings.length <= nAtts) {
            // Note that in this case we can't have any pseudo attributes
            attributeNameStrings = new String[nAtts];
        }
        for (int i = 0; i < nAtts; i++) {
            attributeNameStrings[i] = getAttributeNameString(getAttributeName(i));
        }
        try {
            getContentHandler().startElement(elementType.getNamespace(),
                                             elementType.getLocalPart(),
                                             elementType.toString(), this);
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }

    protected void endElementContent(Name elementType) throws XSLException 
    {
        // FIXME: generate some end prefix mapping events?
        // we could iterate over all the prefixes in current
        // map and see if they're in the next on the stack

        try {
            getContentHandler().endElement(elementType.getNamespace(),
                                           elementType.getLocalPart(),
                                           elementType.toString());
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
        currentMap = elementMaps[--elementDepth];
    }

    /////////////////////////////////////////
    //
    // Some extra Attributes implementation
    //

    public String getURI(int i)
    {
        int n = super.getLength();
        if (i < n) {
            return super.getURI(i);
        }
        return "";  // FIXME
    }

    public String getLocalName(int i)
    {
        int n = super.getLength();
        if (i < n) {
            return super.getLocalName(i);
        }
        return attributeNameStrings[i]; // FIXME
    }

    public String getQName(int i)
    {
        return attributeNameStrings[i];
    }

    public String getType(int i)
    {
        int n = super.getLength();
        if (i < n) {
            return super.getType(i);
        }
        return "CDATA"; // FIXME
    }


    // we're also allowed to look at the pseudo attributes, here
    public String getValue(int i) 
    {
        int n = super.getLength();
        if (i < n) {
            return super.getValue(i);
        }
        return pseudoAttributeValues[i - n];
    }

    public int getLength() 
    {
        return super.getLength() + nPseudoAttributes;
    }

    // pseudoAttributes are for namespace declarations
    private void addPseudoAttribute(String name, String value)
    {
        if (nPseudoAttributes >= pseudoAttributeValues.length) {
            pseudoAttributeValues = grow(pseudoAttributeValues);
        }
        pseudoAttributeValues[nPseudoAttributes] = value;
        int n = nPseudoAttributes++ + super.getLength();
        while (n >= attributeNameStrings.length) {
            attributeNameStrings = grow(attributeNameStrings);
        }
        attributeNameStrings[n] = name;
    }

    // tries to figure out a good attribute qName for the Name
    private String getAttributeNameString(Name name) throws XSLException 
    {
        String namespace = name.getNamespace();
        if (namespace == null) {
            // no namespace, it's easy
            return name.toString();
        }

        if (namespace.equals(Name.XML_NAMESPACE)) {
            // xml's namespace always uses "xml"
            return "xml:" + name.getLocalPart();
        }

        String prefix = name.getPrefix();
        if (prefix != null && 
            namespace.equals(currentMap.getNamespace(prefix))) {
            return name.toString();
        }
        String newPrefix = currentMap.getPrefix(namespace);
        if (newPrefix != null) {
            return newPrefix + ":" + name.getLocalPart();
        }
        if (prefix != null && currentMap.getNamespace(prefix) == null) {
            addPseudoAttribute("xmlns:" + prefix, namespace);
            currentMap = currentMap.bind(prefix, namespace);
            return name.toString();
        }
        // Must generate our own prefix...
        for (int j = 0;  ; j++) {
            newPrefix = "ns" + Integer.toString(j);
            if (currentMap.getPrefix(newPrefix) == null)
                break;
        }
        addPseudoAttribute("xmlns:" + newPrefix, namespace);
        currentMap = currentMap.bind(newPrefix, namespace);
        return newPrefix + ":" + name.getLocalPart();
    }

    //
    //
    //////////////////////////////////////////////////////
    //

    public void resultTreeFragment(ResultTreeFragment frag) 
        throws XSLException 
    {
        try {
            flush();
            frag.emit(getContentHandler());
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }
}
