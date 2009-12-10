// $Id$

package com.jclark.xsl.sax2;

/**
 * a SAX style event handler that receives raw characters events
 * for writing e.g. non XML output
 */
public interface RawCharactersHandler 
{
    void rawCharacters(String chars) throws org.xml.sax.SAXException;
}
