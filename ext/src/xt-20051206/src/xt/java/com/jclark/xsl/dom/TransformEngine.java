// $Id$

package com.jclark.xsl.dom;

import org.w3c.dom.Node;

/**
 * 
 */
public interface TransformEngine
{
    Transform createTransform(Node stylesheetRoot) 
        throws TransformException;
}
