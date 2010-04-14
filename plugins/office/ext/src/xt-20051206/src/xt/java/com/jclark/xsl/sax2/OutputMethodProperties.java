// $Id$

package com.jclark.xsl.sax2;

import org.xml.sax.*;

import com.jclark.xsl.tr.OutputMethod;

import com.jclark.xsl.om.Name;
import com.jclark.xsl.om.NameTable;

import java.util.Properties;

/**
 * Presents a <code>java.util.Properties</code> representation
 * the information, declared in the
 * xsl:output element, that modify how
 * transformation output is serialized 
 */
class OutputMethodProperties extends Properties
{

    // we delegate most of the information magement to this guy
    final private OutputMethod _method;

    final private Name[] _names;

    private Name[] _cdataSectionElements;

    /**
     * @param method -- from the stylesheet, the output parameters
     */
    OutputMethodProperties(OutputMethod method) 
    {
        _method = method;

        // the names of the xsl:output parameters thismethod recognizes
        _names = method.getAttributeNames();

        _cdataSectionElements = method.getCdataSectionElements();

        if (_cdataSectionElements.length == 0) {
            _cdataSectionElements = null;
        }
    }

    /**
     * Properties implementation: get the value of the named property
     *
     * @param nameString represents a namespace/localname by
     *   separating the namespaceURI from the localName with
     * the special character <code>OutputMethodHandler.namespaceSeparator</code>
     */
    public String getProperty(String nameString) 
    {
        // this property we don't keep in the list
        if (nameString.equals("cdata-section-elements")) {
            return _cdataSectionElements == null ? null : getValue(0);
        }

        int ns = nameString.lastIndexOf(OutputMethodHandler.namespaceSeparator);

        Name name;
        if (ns < 0) {
            name = toName("", nameString);
        } else {
            name = toName(nameString.substring(0, ns),
                          nameString.substring(ns + 1));
        }
        return _method.getSpecifiedValue(name);
    }

    static private final String nameToString(Name name) 
    {
        if (name.getNamespace() == null) {
            return name.getLocalPart();
        }
        return (name.getNamespace()
                + OutputMethodHandler.namespaceSeparator
                + name.getLocalPart());
    }

    /**
     * @return the value of the i'th Attribute
     */
    private String getValue(int i) 
    {

        if (_cdataSectionElements != null && i-- == 0) {
            StringBuffer buf = new StringBuffer();
            for (i = 0; i < _cdataSectionElements.length; i++) {
                if (i != 0) {
                    buf.append(' ');
                }
                buf.append(nameToString(_cdataSectionElements[i]));
            }
            return buf.toString();
        }
        return _method.getSpecifiedValue(_names[i]);
    }

    //
    private Name toName(String namespaceURI, String localPart)
    {
        Name name;
        if (namespaceURI == null || namespaceURI.length() == 0) {
            name = _method.getNameTable().createName(localPart);
        } else {
            name = _method.getNameTable().createName(namespaceURI,
                                                     localPart);
        }
        return name;
    }

}
