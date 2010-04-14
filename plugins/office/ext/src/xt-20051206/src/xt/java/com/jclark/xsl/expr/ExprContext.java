// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

import java.net.URL;
import java.util.Hashtable;

/**
 * packages up the context available to the XSLT engine
 * when evaluating XPath expressions
 */
public interface ExprContext 
{

    /**
     *
     */
    int getPosition() throws XSLException;
    
    /**
     *
     */
    int getLastPosition() throws XSLException;
    
    /**
     * access to the stylesheet's global variables
     */
    Variant getGlobalVariableValue(Name name) throws XSLException;
    
    /**
     * access to the stylesheet's in-scope local variables
     */
    Variant getLocalVariableValue(Name name) throws XSLException;
    
    /**
     *
     */
    ExtensionContext getExtensionContext(String namespace)
	throws XSLException;
    
    /**
     * provides access to the system properties for the 
     * system-property() function in XSLT 1.0 section 12.4
     */
    Variant getSystemProperty(Name name);
    
    /**
     *
     */
    Node getCurrent(Node contextNode);
    
    /**
     * returns a parsed representation of the document at the given
     * URL. ... enables  the "document()" function of XSLT 1.0 section 12.1
     */
    NodeIterator getDocument(URL baseURL, String uriRef) throws XSLException;
    
    /**
     *  @return the indexed nodes for the named key in the node's document
     */
    KeyValuesTable getKeyValuesTable(Name keyName, Node n);
    
    /**
     *
     */
    Node getTree(Variant v) throws XSLException;
    
}
