// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * collects the attributes from the xsl:output element
 * which determine the type of output we are to produce
 */
public interface OutputMethod
{
    /**
     * i.e. "text", "xml", "html", etc
     */
    Name getName();
    
    /**
     * the "cdata-section-elements" attribute
     */
    Name[] getCdataSectionElements();

    /**
     * used for obtaining Names from namespace / localName pairs
     */
    NameTable getNameTable();
        
    /**
     * return the value for the named output method attribute,
     * only if that attribute was specified in the stylesheet
     *
     * @return null if the value was not declared in the stylesheet
     */
    public String getSpecifiedValue(Name name);
    
    /**
     * all the known output method attribute names, excluding
     * "method" and "cdata-section-elements"
     */
    public Name[] getAttributeNames();
    
    /**
     * gets the value specified in the stylesheet, if available,
     * else gets the defaulted value
     */
    public String getPropertyValue(Name name);

}
