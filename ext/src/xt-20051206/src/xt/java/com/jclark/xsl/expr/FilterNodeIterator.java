// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 *
 */
class FilterNodeIterator extends DelegateExprContext implements NodeIterator 
{
    private int pos = 0;
    private int lastPos = 0;
    private NodeIterator iter;
    private final BooleanExpr predicate;

    FilterNodeIterator(NodeIterator iter,
                       ExprContext origContext,
                       BooleanExpr predicate) 
    {
        super(origContext);
        this.iter = iter;
        this.predicate = predicate;
    }
  
    public Node next() throws XSLException 
    {
        for (;;) {
            Node tem = iter.next();
            if (tem == null) {
                break;
	    }
            ++pos;
            if (predicate.eval(tem, this)) {
                return tem;
	    }
        }
        return null;
    }

    public int getPosition() 
    {
        return pos;
    }

    public int getLastPosition() throws XSLException
    {
        if (lastPos == 0) {
            CloneableNodeIterator cloneIter;
            if (iter instanceof CloneableNodeIterator) {
                cloneIter = (CloneableNodeIterator)iter;
	    } else {
                cloneIter = new CloneableNodeIteratorImpl(iter);
	    }
            iter = (NodeIterator)cloneIter.clone();
            int savePosition = pos;
            try {
                while (next() != null)
                    ;
                lastPos = pos;
            }
            finally {
                pos = savePosition;
                iter = cloneIter;
            }
        }
        return lastPos;
    }
}
