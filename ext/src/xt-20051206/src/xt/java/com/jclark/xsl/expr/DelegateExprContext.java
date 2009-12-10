// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;


import java.net.URL;

/**
 * a base class for ExprContext classes that override some methods
 *  of an existing ExprContext, and delegate the rest of 'em to
 * that existing ExprContest
 */
class DelegateExprContext implements ExprContext
{
    ExprContext origContext;

    /**
     * wrap around an existing ExprContext 
     */
    DelegateExprContext(ExprContext context)
    {
        origContext = context;
    }

    public int getPosition() throws XSLException
    {
        return origContext.getPosition();
    }

    public int getLastPosition() throws XSLException
    {
        return origContext.getLastPosition();
    }

    public Variant getLocalVariableValue(Name name) throws XSLException 
    {
        return origContext.getLocalVariableValue(name);
    }

    public Variant getGlobalVariableValue(Name name) throws XSLException
    {
        return origContext.getGlobalVariableValue(name);
    }

    public ExtensionContext getExtensionContext(String namespace) throws XSLException
    {
        return origContext.getExtensionContext(namespace);
    }

    public Variant getSystemProperty(Name name)
    {
        return origContext.getSystemProperty(name);
    }

    public Node getCurrent(Node contextNode)
    {
        return origContext.getCurrent(contextNode);
    }

    /**
     * loads the document at the given URI
     */
    public NodeIterator getDocument(URL baseURL, String uriRef)
        throws XSLException
    {
        return origContext.getDocument(baseURL, uriRef);
    }

    /**
     *  @return the indexed nodes for the named key in the node's document
     */
    public KeyValuesTable getKeyValuesTable(Name keyName, Node contextNode)
    {
        return origContext.getKeyValuesTable(keyName, contextNode);
    }

    /**
     *
     */
    public Node getTree(Variant v) throws XSLException
    {
        return origContext.getTree(v);
    }
  
}
