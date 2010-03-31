// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;

/**
 * a pattern that has a predicate to eliminate some nodes
 */
class FilterPattern extends PathPatternBase 
{
    private PathPatternBase pattern;
    private BooleanExpr predicate;

    FilterPattern(PathPatternBase pattern, BooleanExpr predicate) 
    {
        this.pattern = pattern;
        this.predicate = predicate;
    }

    public boolean matches(Node node, ExprContext context)
        throws XSLException 
    {
        if (!pattern.matches(node, context)) {
            return false;
        }
        return predicate.eval(node, new Context(node, context));
    }

    public int getDefaultPriority() 
    {
        return 1;
    }

    Name getMatchName() 
    {
        return pattern.getMatchName();
    }

    byte getMatchNodeType() 
    {
        return pattern.getMatchNodeType();
    }

    //////////////////////////////////////////
    //  
    // the context changes a bit from our caller's context to reflect a
    // different way of tracking position()
    //
    class Context extends DelegateExprContext 
    {
        Node node;
        int position = 0;
        int lastPosition = 0;

        Context(Node node, ExprContext context) 
        {
            super(context);
            this.node = node;
        }

        public int getPosition() throws XSLException 
        {
            if (position != 0) {
                return position;
            }
            NodeIterator iter;
            switch (node.getType()) {
            case Node.ROOT:
                position = 1;
                return 1;
            case Node.ATTRIBUTE:
                iter = node.getParent().getAttributes();
                break;
            default:
                iter = node.getParent().getChildren();
                break;
            }
            position = 1;
            for (;;) {
                Node tem = iter.next();
                if (tem.equals(node)) {
                    break;
                }
                if (pattern.matches(tem, origContext)) {
                    position++;
                }
            }
            return position;
        }

        public int getLastPosition() throws XSLException 
        {
            if (lastPosition != 0) {
                return lastPosition;
            }
            NodeIterator iter;
            switch (node.getType()) {
            case Node.ROOT:
                lastPosition = 1;
                return 1;
            case Node.ATTRIBUTE:
                iter = node.getParent().getAttributes();
                lastPosition = 0;
                break;
            default:
                iter = node.getFollowingSiblings();
                lastPosition = position;
                break;
            }
            for (;;) {
                Node tem = iter.next();
                if (tem == null) {
                    break;
                }
                if (pattern.matches(tem, origContext)) {
                    lastPosition++;
                }
            }
            return lastPosition;
        }
    }
}
