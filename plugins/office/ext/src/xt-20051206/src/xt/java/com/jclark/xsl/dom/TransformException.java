// $Id$

package com.jclark.xsl.dom;

import org.w3c.dom.Node;

public class TransformException extends Exception
{
    private final Node node;

    public TransformException(String detail, Node node) {
        super(detail);
        this.node = node;
    }
  
    public Node getNode()
    {
        return node;
    }
}
