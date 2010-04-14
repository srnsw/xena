// $Id$

package com.jclark.xsl.om;

import javax.xml.transform.TransformerException;


public class XSLException extends TransformerException 
{
    private Node node;
    private Exception exception;

//      public XSLException() 
//      {
//          this.node = null;
//      }

    public XSLException(String detail, Node node) 
    {
        super(detail, node);
        this.node = node;
    }

    public XSLException(String detail) 
    {
        super(detail);
        this.node = null;
    }

    public XSLException(Exception exception) 
    {
        super(exception);
    }

    public XSLException(Exception exception, Node node) 
    {
        super(exception.getMessage(), node, exception);
        this.node = node;
    }

    void setNode(Node node) 
    {
        this.node = node;
    }

    public Node getNode() 
    {
        return node;
    }

}
