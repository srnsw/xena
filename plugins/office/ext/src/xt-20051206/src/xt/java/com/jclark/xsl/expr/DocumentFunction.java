// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import java.net.URL;

/**
 * represents the XSLT Function: node-set document(object, node-set?) 
 *
 *   The document function allows access to XML documents
 *  other than the main source document.
 */
class DocumentFunction implements Function 
{
    public ConvertibleExpr makeCallExpr(ConvertibleExpr args[], 
                                        final Node exprNode)
        throws ParseException 
    {
        if (args.length != 1 && args.length != 2) {
            throw new ParseException("expected one or two arguments");
        }
        ConvertibleExpr e = args[0];
        final NodeSetExpr base = (args.length == 1 ? 
                                  null :
                                  args[1].makeNodeSetExpr());

        if (e instanceof NodeSetExpr) {
            final NodeSetExpr nse = (NodeSetExpr)e;
            return new ConvertibleNodeSetExpr() 
                {
                    public NodeIterator eval(Node node, 
                                             ExprContext context)
                        throws XSLException
                    {
                        return document(context,
                                        nse.eval(node, context),
                                        base == null ? 
                                        null : 
                                        base.eval(node, context).next());
                    }
                };

        } else if (e instanceof VariantExpr) {

            final VariantExpr ve = (VariantExpr)e;
            return new ConvertibleNodeSetExpr() 
                {
                    public NodeIterator eval(Node node, 
                                             ExprContext context)
                        throws XSLException 
                    {
                        Variant v = ve.eval(node, context);
                        if (v.isNodeSet()) {
                            return document(context,
                                            v.convertToNodeSet(),
                                            base == null ? 
                                            null : 
                                            base.eval(node, context).next());
                        } else {
                            Node baseNode = v.getBaseNode();
                            if (baseNode == null) {
                                baseNode = exprNode;
                            }
                            return document(context,
                                            v.convertToString(),
                                            base == null ?
                                            baseNode : 
                                            base.eval(node, context).next());
                        }
                    }
                };

        } else {

            final StringExpr se = e.makeStringExpr();
            return new ConvertibleNodeSetExpr() 
                {
                    public NodeIterator eval(Node node, ExprContext context) 
                        throws XSLException 
                    {
                        return document(context,
                                        se.eval(node, context),
                                        base == null ? 
                                        exprNode : 
                                        base.eval(node, context).next());
                    }
                };
        }
    }
    
  
    private static final NodeIterator document(ExprContext context,
                                               NodeIterator iter,
                                               Node baseNode) 
        throws XSLException 
    {
        NodeIterator[] iters = new NodeIterator[1];
        int len = 0;
        for (;;) {
            Node node = iter.next();
            if (node == null) {
                break;
            }

            if (len == iters.length) {
                // time to grow our list of iterators
                NodeIterator[] oldIters = iters;
                iters = new NodeIterator[iters.length * 2];
                System.arraycopy(oldIters, 0, iters, 0, oldIters.length);
            }

            iters[len++] = document(context,
                                    Converter.toString(node),
                                    baseNode == null ? node : baseNode);
        }
        if (len > 1) {
            return new MergeNodeIterator(iters, len);
        }
        if (len == 1) {
            return iters[0];
        }
        return new SingleNodeIterator(null); // why not a NullNodeIterator?
    }

    private static final NodeIterator document(ExprContext context, 
                                               String urlRef, 
                                               Node baseNode) 
        throws XSLException 
    {
        return context.getDocument(baseNode == null ? null :
                                   baseNode.getURL(),
                                   urlRef);
    }



}
