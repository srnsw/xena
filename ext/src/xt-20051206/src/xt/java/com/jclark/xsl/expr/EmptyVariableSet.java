// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

public class EmptyVariableSet implements VariableSet 
{
    public boolean contains(Name name) 
    {
        return false;
    }
}
