// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * a place to obtain run-time parameters from the
 *  execution environment
 */
public interface ParameterSet
{
    Object getParameter(Name name);
}
