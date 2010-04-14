// $Id$

package com.jclark.xsl.tr;

final class ImportantBoolean
{
    private final boolean b;
    private Importance imp;

    ImportantBoolean(boolean b, Importance imp)
    {
        this.b = b;
        this.imp = imp;
    }

    public Importance getImportance()
    {
        return imp;
    }

    public boolean getBoolean()
    {
        return b;
    }
}
