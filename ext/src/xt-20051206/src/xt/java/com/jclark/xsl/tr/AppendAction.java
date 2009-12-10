// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 *
 */
class AppendAction implements Action
{
    private Vector sequence = new Vector();

    public void invoke(ProcessContext context, 
                       Node sourceNode, Result result) throws XSLException
    {
        for (Enumeration enum = sequence.elements();
             enum.hasMoreElements();)
            ((Action)enum.nextElement()).invoke(context, sourceNode, result);
    }

    void add(Action action)
    {
        sequence.addElement(action);
    }
}
  
