// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.util.Comparator;
import com.jclark.xsl.om.Node;

/**
 *
 */
class DocumentOrderComparator implements Comparator 
{
    public int compare(Object obj1, Object obj2) 
    {
        return ((Node)obj1).compareTo((Node)obj2);
    }
}
