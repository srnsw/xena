// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariableSet;

class AddVariableSet implements VariableSet
{
    private final Name var;
    private final VariableSet set;

    AddVariableSet(Name var, VariableSet set)
    {
        this.var = var;
        this.set = set;
    }

    public boolean contains(Name name)
    {
        return name.equals(var) || set.contains(name);
    }
}
