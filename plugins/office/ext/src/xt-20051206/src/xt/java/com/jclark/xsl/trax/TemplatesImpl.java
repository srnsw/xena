// $Id$

package com.jclark.xsl.trax;

import com.jclark.xsl.tr.Sheet;
import com.jclark.xsl.tr.Engine;

import java.util.Properties;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

/**
 * An implementation of the TrAX Templates interface.
 */
class TemplatesImpl implements Templates
{

    protected  Sheet _sheet;
    private Engine _engine;
    private TransformerFactoryImpl _factory;

    /**
     * 
     */
    public TemplatesImpl(Sheet sheet, 
                         Engine engine, TransformerFactoryImpl factory)
    {
        _sheet = sheet;
        _engine = engine;
        _factory = factory;
    }
    
    /**
     * Create a new transformation context for this Templates object.
     * @return A valid non-null instance of a Transformer.
     */
    public Transformer newTransformer() throws TransformerConfigurationException
    {
        try {
            return new TransformerImpl(_sheet, _engine, _factory);
        } catch(Exception e) {
            throw new TransformerConfigurationException(e);
        }
    }
    
    /**
     * Return a empty Properties object.
     */
    public Properties getOutputProperties()
    {
        // FIXME: see if we can't get these from Sheet
        return null;
    }
    
}

