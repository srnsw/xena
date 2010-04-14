// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;

public interface ResultTreeFragment
{
    void emit(DocumentHandler handler) throws SAXException;
}
