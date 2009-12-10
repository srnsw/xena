// $Id$

package com.jclark.xsl.tr;

import java.net.URL;

import com.jclark.xsl.om.XSLException;
import com.jclark.xsl.expr.ExtensionContext;

/**
 * for extension functions -- i think
 */
public interface ExtensionHandler
{

    /**
     *
     */
    ExtensionContext createContext(String namespace) throws XSLException;

    /**
     *
     */
    Object wrapResultFragmentVariant(ResultFragmentVariant frag) 
        throws XSLException;
}
