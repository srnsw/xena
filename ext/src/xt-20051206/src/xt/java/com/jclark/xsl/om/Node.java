// $Id$

package com.jclark.xsl.om;

import java.net.URL;
import javax.xml.transform.SourceLocator;

/**
 * represents a Node in an XML document
 */
public interface Node extends SourceLocator
{

    static byte ELEMENT = 0;
    static byte TEXT = 1;
    static byte ATTRIBUTE = 2;
    static byte ROOT = 3;
    static byte PROCESSING_INSTRUCTION = 4;
    static byte COMMENT = 5;
    static byte NAMESPACE = 6;
    static int N_TYPES = 7;
    static byte ALLTYPES = 7;  // useful for the "node()" node type test

    


    // should we be interested in namespace nodes, too?

    /**
     * returns one of: <code>ELEMENT, TEXT, ATTRIBUTE, 
     * ROOT, PROCESSING_INSTRUCTION or COMMENT </code>
     */
    byte getType();

    /**
     * Returns element type name for element; attribute name for an attribute;
     * target for a PI., Namespace prefix for a Namespace
     */
    Name getName();

    /**
     * Returns text for TEXT node; value for attribute node;
     * content for comment node;
     * content after PI for PI node;  
     */
    String getData();

    /**
     * as the name implies ...
     */
    Node getParent();

    /**
     * as the name implies ...
     */
    SafeNodeIterator getChildren();

    /**
     * as the name implies ...
     */
    SafeNodeIterator getFollowingSiblings();

    /**
     * base URL ??
     */
    URL getURL();

    /**
     * if decorated with locator events, this returns the line number
     * in the XML source where this node was found
     */
    int getLineNumber();

    
    /**
     * in-scope namespaces ??
     */
    NamespacePrefixMap getNamespacePrefixMap();

    /**
     * also compares document order 
     * @returns ?? -1 if precedes, +1 if follows?
     */
    int compareTo(Node node);

    /**
     * finds an Element Node, in the this node's document, 
     * with the given ID
     */
    Node getElementWithId(String id);

    /**
     * if this is an attribute?? and it is of type ID ?
     */
    boolean isId(String id);

    /**
     * does this only work on Elements?
     */
    String getAttributeValue(Name name);

    /**
     * does this only work on Elements?
     */
    Node getAttribute(Name name);

    /**
     * does this only work on Elements?
     */
    SafeNodeIterator getAttributes();

    /**
     * does this only work on Elements?
     */
    SafeNodeIterator getNamespaces();

    /**
     * guaranteed to be unique (and repeatable)
     */
    String getGeneratedId();

    /**
     */
    String getUnparsedEntityURI(String name);

    /**
     * gets the owning Document's root
     */
    Node getRoot();
}
