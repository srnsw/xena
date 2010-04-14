// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.sax.SaxFilterMaker;
import java.util.Properties;

/**
 *  Represents a (compiled) XSLT stylesheet. Transforms an XML tree
 */
public interface Sheet
{
    /**
     * transform the document rooted at 
     *   <code>node</code> against this stylesheet
     * @param node the root node of source document
     * @param parser a loader we can (re-)use
     * @param params the XSLT run-time parameters
     * @param result the destination for the transformation results
     */
    Result process(Node node, XMLProcessor parser,
                   ParameterSet params, Result result) throws XSLException;

    /**
     * sets the experimental xrap processor for use during processing by
     * the ProcessContext
     */
    public void setSaxExtensionFilter(SaxFilterMaker xrap);

    /**
     * gets the xrap (extension) processor for use during processing
     */
    public SaxFilterMaker getSaxExtensionFilter();

    /**
     * sets the experimental xrap processor for use during processing by
     * the ProcessContext
     */
    public void setDebugger(ActionDebugTarget debugger);

    /**
     * gets the xrap (extension) processor for use during processing
     */
    public ActionDebugTarget getDebugger();

    /**
     * get the parameters controlling how a source tree object model
     * is build
     */
    LoadContext getSourceLoadContext();

    /**
     * returns the current values set either by client code,
     * the stylesheet, or defaulted for the output method properties
     */
    public OutputMethod getOutputMethod();

    /**
     * returns the current values set either by client code,
     * the stylesheet, or defaulted for the output method properties
     * see java.xml.transform.Transform.getOutputMethodProperties()
     */
    public Properties getOutputMethodProperties();

    /**
     * override the stylesheet's or default value for an
     * output method property
     */
    public void setOutputMethodProperty(String encodedPropertyNamespace,
                                        String encodedPropertyName, 
                                        String value)
        throws XSLException;
    /**
     * resets the output method properties to just those which
     * were set in the stylesheet
     */
    public void clearOutputMethodProperties();

    /**
     * returns the current value of the named property
     */ 
    public String getOutputMethodProperty(String encodedPropertyNamespace,
                                          String encodedPropertyName);
}
