// $Id$

package com.jclark.xsl.om;

/**
 * Associates namespaces with prefixes.
 *
 * Every Node in a document is associated with 
 * a <code>NameSpacePrefixMap</code> which represents
 * all the in-scope namespace bindings for that Node
 */
public interface NamespacePrefixMap 
{
    /**
     * A NamespacePrefixMap is associated with a single NameTable
     */
    NameTable getNameTable();

    /**
     * returns the two-part Name for the given Attribute's  qName
     * 
     * non-colonized names are returned as belonging to no namespace
     *
     * @param node -- provided for particularizing any Exception with Location
     * @throws XSLException -- if there's no binding for the prefix
     */
    Name expandAttributeName(String qName, Node node) throws XSLException;

    /**
     * returns the two-part Name for the given qName
     * non-colonized names are identified in the default namespace, if there
     * is one, else no namespace
     *
     * @param node -- provided for particularizing any Exception with Location
     * @throws XSLException -- if there's no binding for the prefix
     */
    Name expandElementTypeName(String qName, Node node) throws XSLException;

    /**
     * record the association of a prefix to a namespace
     */
    NamespacePrefixMap bind(String prefix, String namespace);

    /**
     * identify the given namespace as the default namespace
     */
    NamespacePrefixMap bindDefault(String namespace);

    /**
     * remove the default namespace
     */
    NamespacePrefixMap unbindDefault();

    /**
     * removes the association of a prefix with a namespace
     */
    NamespacePrefixMap unbind(String prefix);

    /**
     * @return the default namespace (which needs no prefix)
     */
    String getDefaultNamespace();

    /**
     * The number of bound prefixes
     * @return the number of bindings in this map
     */
    int getSize();

    /**
     * @return the i'th prefix
     */
    String getPrefix(int i);

    /**
     * @return the i'th namespace
     */
    String getNamespace(int i);

    /**
     * @return the (first??) prefix bound to the given namespace (or null)
     */
    String getPrefix(String namespace);

    /**
     *  @return the namespace bound to the given prefix (or null)
     */
    String getNamespace(String prefix);
}

