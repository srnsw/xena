// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import com.jclark.xsl.om.*;
import com.jclark.xsl.tr.*;

/**
 *
 */
public class MultiNamespaceResult extends ResultBase
{
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

    public MultiNamespaceResult(DocumentHandler documentHandler, 
                                ErrorHandler errorHandler)
    {
        super(documentHandler, errorHandler);
    }

    public Result createResult(String uri) throws XSLException
    {
        if (outputMethodHandler != null) {
            OutputMethodHandler om = 
                outputMethodHandler.createOutputMethodHandler(uri);
            if (om != null) {
                return new MultiNamespaceResult(om, errorHandler);
            }
        }
        return null;
    }

    protected void startElementContent(Name elementType, 
                                       NamespacePrefixMap map)
        throws XSLException
    {
        // WDL debug
//          if (elementType == null) {
//              try {
//                  throw new Exception("no element type name!");
//              } catch (Exception e) {
//                  e.printStackTrace();
//                  throw new XSLException(e);
//              }
//          }

        if (elementDepth >= elementMaps.length) {
            NamespacePrefixMap[] oldElementMaps = elementMaps;
            elementMaps = new NamespacePrefixMap[oldElementMaps.length * 2];
            System.arraycopy(oldElementMaps, 0, elementMaps, 0,
                             oldElementMaps.length);
        }
        elementMaps[elementDepth++] = currentMap;
        nPseudoAttributes = 0;
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
                }
            }
            String defaultNamespace = map.getDefaultNamespace();
            if (defaultNamespace == null) {
                if (currentMap == null) {
                    changed = true;
                } else if (currentMap.getDefaultNamespace() != null) {
                    addPseudoAttribute("xmlns", "");
                    changed = true;
                }
            }
            else if (currentMap == null || 
                     !defaultNamespace.equals(currentMap.getDefaultNamespace())) {
                changed = true;
                addPseudoAttribute("xmlns", defaultNamespace);
            }
            if (changed) {
                currentMap = map;
            }
        }
        int nAtts = super.getLength();
        if (attributeNameStrings.length <= nAtts)
            // Note that in this case we can't have any pseudo attributes
            attributeNameStrings = new String[nAtts];
        for (int i = 0; i < nAtts; i++)
            attributeNameStrings[i] = getAttributeNameString(getAttributeName(i));

        try {
            getDocumentHandler().startElement(elementType.toString(), this);
        }
        catch (SAXException e) {

            // WDL debug
            // e.printStackTrace();

            throwXSLException(e);
        }
    }

    protected void endElementContent(Name elementType) throws XSLException 
    {
        try {
            getDocumentHandler().endElement(elementType.toString());
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
        currentMap = elementMaps[--elementDepth];
    }

    public String getName(int i) 
    {
        return attributeNameStrings[i];
    }

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
  
    private String getAttributeNameString(Name name) throws XSLException 
    {
        String namespace = name.getNamespace();
        if (namespace == null) {
            return name.toString();
        }
        if (namespace.equals(Name.XML_NAMESPACE)) {
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

    public void resultTreeFragment(ResultTreeFragment frag) 
        throws XSLException 
    {
        try {
            flush();
            frag.emit(getDocumentHandler());
        }
        catch (SAXException e) {
            throwXSLException(e);
        }
    }
}
