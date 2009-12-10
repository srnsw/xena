// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;


/**
 * the XPath function key("name", "value")
 */
class KeyFunction implements Function
{
    public ConvertibleExpr makeCallExpr(ConvertibleExpr e[], Node exprNode) 
	throws ParseException 
    {

        if (e.length != 2) {
            throw new ParseException("Key function:: expected two argument's");
	}

        final StringExpr se = e[0].makeStringExpr();

        final NamespacePrefixMap prefixMap = exprNode.getNamespacePrefixMap();

        if (e[1] instanceof NodeSetExpr) {

            final NodeSetExpr nse = (NodeSetExpr)e[1];

            return new ConvertibleNodeSetExpr() {
                    public NodeIterator eval(Node node, 
					     ExprContext context) 
			throws XSLException 
		    {
			Name keyName = 
			    prefixMap.expandAttributeName(se.eval(node, context),
							  node);

                        return getKeyedNodes(keyName, node, 
					     nse.eval(node, context),
					     context);
                    }
                };

        } else if (e[1] instanceof VariantExpr) {

            final VariantExpr ve = (VariantExpr)e[1];

            return new ConvertibleNodeSetExpr() 
		{

                    public NodeIterator eval(Node node, 
					     ExprContext context) 
			throws XSLException 
		    {
			Name keyName = 
			    prefixMap.expandAttributeName(se.eval(node, context),
							  node);

                        Variant v = ve.eval(node, context);
                        if (v.isNodeSet()) {
                            return getKeyedNodes(keyName,
						 node, v.convertToNodeSet(),
						 context);
                        } else {
                            return getKeyedNodes(keyName,
						 node, v.convertToString(),
						 context);
			}
                    }
                };

        } else {

            final StringExpr kvStrExpr = e[1].makeStringExpr();
            return new ConvertibleNodeSetExpr() 
		{
                    public NodeIterator eval(Node node, ExprContext context) 
                        throws XSLException
		    {
			Name keyName = 
			    prefixMap.expandAttributeName(se.eval(node, context),
							  node);
			
                        return getKeyedNodes(keyName, node, 
					     kvStrExpr.eval(node, context),
					     context);
                    }
                };
	}
    }
  

    /**
     * returns an iterator of all the nodes of a document with 
     * the key of the given key name, and indexed by the given keyValue
     */
    static private final NodeIterator getKeyedNodes(Name keyName,
						    Node refNode,
						    String keyValue,
						    ExprContext context)
	throws XSLException
    {
	KeyValuesTable kvt = context.getKeyValuesTable(keyName, refNode);
	if (kvt == null) {
	    return new NullNodeIterator();
	} else {
	    return kvt.get(keyValue);
	}
    }



    /**
     * returns an iterator of all the nodes of a document with 
     * the key of the given key name, and indexed by any of the 
     * string values of the nodes in the given keyValues iterator
     */
    static private final NodeIterator getKeyedNodes(Name keyName,
						    Node refNode,
						    NodeIterator keyValues,
						    ExprContext context)
	throws XSLException
    {

        NodeIterator[] iters = new NodeIterator[10];
        int length = 0;
        for (;;) {
            // for each node in the first expression
            // we build a NodeIterator 
            Node tem = keyValues.next();
            if (tem == null) {
                // we've exhausted our supply of nodes in the 
                //  first expression
                break;
            }
            if (length == iters.length) {
                // we need a bigger array
                NodeIterator[] oldIters = iters;
                iters = new NodeIterator[oldIters.length * 2];
                System.arraycopy(oldIters, 0, iters, 0, oldIters.length);
            }
            iters[length++] = getKeyedNodes(keyName, refNode,
					    Converter.toString(tem),
					    context);
        }

        // so, how many iterators did we build?
        switch (length) {
        case 0:
            return new NullNodeIterator();
        case 1:
            return iters[0];
        case 2:
            return new UnionNodeIterator(iters[0], iters[1]);
        }
        return new MergeNodeIterator(iters, length);

    }

}
