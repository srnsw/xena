// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.BooleanExpr;

/**
 * <xsl:if
 */
class IfAction implements Action
{
    BooleanExpr condition;
    Action ifTrueAction;
    Action ifFalseAction;

    IfAction(BooleanExpr condition, Action ifTrueAction, Action ifFalseAction)
    {
        this.condition = condition;
        this.ifTrueAction = ifTrueAction;
        this.ifFalseAction = ifFalseAction;
    }

    public void invoke(ProcessContext context, Node sourceNode,
                       Result result) 
        throws XSLException
    {
        if (condition.eval(sourceNode, context)) {
            ifTrueAction.invoke(context, sourceNode, result);
        } else {
            // odd, I wouldn't have thought there was an else
            ifFalseAction.invoke(context, sourceNode, result);
        }
    }
}
