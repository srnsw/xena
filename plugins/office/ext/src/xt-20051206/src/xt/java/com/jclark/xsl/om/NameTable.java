// $Id$

package com.jclark.xsl.om;

/**
 * constructs (or obtains) Names for qName/namespace pairs
 */
public interface NameTable 
{
    /**
     * obtain a Name with no namespace
     */
    Name createName(String localPart);

    /**
     * obtain a name in the given namespace
     */
    Name createName(String qName, String namespace);

    /**
     * obtain an empty NamespacePrefixMap
     */
    NamespacePrefixMap getEmptyNamespacePrefixMap();
}
