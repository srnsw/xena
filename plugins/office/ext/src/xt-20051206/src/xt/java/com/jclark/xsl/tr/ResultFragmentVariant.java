// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.Variant;

/**
 *
 */
public interface ResultFragmentVariant extends Variant
{

    void append(Result result) throws XSLException;

    Node getTree(ProcessContext context) throws XSLException;
}
