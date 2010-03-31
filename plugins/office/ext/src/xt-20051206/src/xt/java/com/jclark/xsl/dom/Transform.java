// $Id$

package com.jclark.xsl.dom;

import org.w3c.dom.Node;

/**
 * An object which transforms a source DOM into a
 * result DOM
 */
public interface Transform 
{
    /**
     * run a transformation of the DOM at sourceRoot, constructing
     * the results onto the DOM at resultRoot
     */
    void transform(Node sourceRoot, 
                   Node resultRoot) throws TransformException;
}
