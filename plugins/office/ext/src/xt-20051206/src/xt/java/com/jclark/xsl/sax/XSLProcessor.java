// $Id$

package com.jclark.xsl.sax;

import com.jclark.xsl.tr.ActionDebugTarget;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.io.IOException;

/**
 * 
  <p>typical usage:
   <pre>
    // find an implementation, and construct it
    XSLProcessor xsl = new XSLProcessorImpl();
    xsl.setParser(sourceParser, styleParser);
    xsl.loadStylesheet(someInputSource);

    // maybe clone for re-use ...

    // maybe set some parameters ...

    // attach an output handler
    xsl.setDocumentHandler(someHandler);
    xsl.parse(someOtherInputSource);
   </pre>
  </p>
*/
public interface XSLProcessor extends Parser
{
    /** 
     * setParser must be called before any other methods 
     */
    void setParser(Parser parser);

    /**
     * set one parser for the stylesheet, and another for the input
     */
    void setParser(Parser sourceParser, Parser stylesheetParser);

    /**
     * set the output target for the transform
     */
    void setOutputMethodHandler(OutputMethodHandler handler);

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
