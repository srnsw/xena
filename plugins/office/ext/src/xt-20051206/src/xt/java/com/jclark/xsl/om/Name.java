// $Id$

package com.jclark.xsl.om;

/**
 * A Name is a two part object, consisting of a namespace (<code>String</code>)
 * and a local part (<code>String</code> with no colon)
 *
 * Names are the same if they have the same namespace, local part and
 * creator.
 *
 * This implementation keeps track of the prefix a namespace was
 * bound to when the name was created, so we may have more than
 * one name with identical namespace, local part and creater,
 * but with different prefixes, yet "equals()" holds true for all.
 */
public interface Name 
{
    /**
     * The constant <code> http://www.w3.org/XML/1998/namespace</code>
     */
    static String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    /**
     * get the URI reference that is the namespace
     */
    String getNamespace();

    /**
     * get the part of the name that has no prefix
     */
    String getLocalPart();

    /**
     * get the prefix
     */
    String getPrefix();

    /**
     * In this implementation, its a NameTable
     */
    Object getCreator();
}
