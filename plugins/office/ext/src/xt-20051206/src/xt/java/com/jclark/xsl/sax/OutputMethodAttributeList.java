// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import com.jclark.xsl.tr.*;
import com.jclark.xsl.om.*;

/**
 * represents the set of attributes that modify how
 * output is serialized
 */
class OutputMethodAttributeList implements AttributeList
{
    final private OutputMethod method;
    final private Name[] names;
    private Name[] cdataSectionElements;

    OutputMethodAttributeList(OutputMethod method) 
    {
        this.method = method;
        this.names = method.getAttributeNames();
        this.cdataSectionElements = method.getCdataSectionElements();
        if (cdataSectionElements.length == 0)
            cdataSectionElements = null;
    }

    public int getLength() 
    {
        return names.length;
    }

    public String getName(int i) 
    {
        if (cdataSectionElements != null && i-- == 0) {
            return "cdata-section-elements";
        }
        return nameToString(names[i]);
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

    public String getValue(int i) 
    {
        if (cdataSectionElements != null && i-- == 0) {
            StringBuffer buf = new StringBuffer();
            for (i = 0; i < cdataSectionElements.length; i++) {
                if (i != 0) {
                    buf.append(' ');
                }
                buf.append(nameToString(cdataSectionElements[i]));
            }
            return buf.toString();
        }
        return method.getSpecifiedValue(names[i]);
    }

    public String getType(int i) 
    {
        return "CDATA";
    }

    public String getType(String name) 
    {
        return "CDATA";
    }

    public String getValue(String nameString) 
    {
        if (nameString.equals("cdata-section-elements")) {
            return cdataSectionElements == null ? null : getValue(0);
        }
        int ns = nameString.lastIndexOf(OutputMethodHandler.namespaceSeparator);
        Name name;
        if (ns < 0) {
            name = method.getNameTable().createName(nameString);
        } else {
            name = method.getNameTable().createName(nameString.substring(0, ns),
                                                    nameString.substring(ns + 1));
        }
        return method.getSpecifiedValue(name);
    }
}
