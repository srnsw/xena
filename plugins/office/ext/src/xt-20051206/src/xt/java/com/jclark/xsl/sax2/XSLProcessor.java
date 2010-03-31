// $Id$

package com.jclark.xsl.sax2;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import com.jclark.xsl.tr.ActionDebugTarget;
import com.jclark.xsl.sax.SaxFilterMaker;

import java.io.IOException;

/**
 * 
  <p>typical usage:
   <pre>
    // find an implementation, and construct it
    XSLProcessor xsl = new XSLProcessorImpl();
    xsl.setReaders(sourceXMLReader, styleXMLReader);
    xsl.loadStylesheet(someInputSource);

    // maybe clone for re-use ...
    XSLProcessor nextTime = xsl.clone();

    // maybe set some parameters ...

    // attach an output handler
    xsl.setContentHandler(someHandler);
    xsl.transform(someOtherInputSource);
   </pre>
  </p>
*/
public interface XSLProcessor extends XMLReader
{

    /**
     * set one parser for the stylesheet, and another for the input
     */
    void setReaders(XMLReader sourceReader, XMLReader stylesheetReader);

    /**
     * prepare for parsing the input XML document
     */
    public void setSourceReader(XMLReader sourceReader);

    /**
     * set the output target for the transform.
     *  Choose one of
     * <code>setOutputMethodHandler()</code> or 
     * <code>setContentHandler()</code>
     */
    void setOutputMethodHandler(OutputMethodHandler handler);

    /**
     * set the output target for the transform.
     *  Choose one of
     * <code>setOutputMethodHandler()</code> or 
     * <code>setContentHandler()</code>
     */
    void setContentHandler(ContentHandler handler);

    /** 
     * loadStylesheet must be called before parse but after setParser 
     */
    void loadStylesheet(InputSource stylesheet) 
        throws IOException, SAXException;

    /**
     * clone after loadStylesheet() 
     * enables us to re-use a transformer, without recompiling the stylesheet
     */
    Object clone();

    /**
     * set the run-time parameters for the stylesheet
     */
    void setParameter(String name, Object obj);

    /**
     * sets a special kind of extension element processor
     * N.B. this signature will probably change in future releases
     */
    void setSaxExtensionFilter(String name, SaxFilterMaker xrap);

    /**
     * sets a special kind of extension element processor
     * N.B. this signature will probably change in future releases
     */
    void setDebugger(String name, ActionDebugTarget xrap);

}


