// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.expr.TopLevelPattern;
import com.jclark.xsl.expr.Pattern;

import com.jclark.xsl.om.*;

import java.util.Hashtable;

/**
 * all the nodes which have been indexed for a given document
 *  in a given named key ... part of the implementation of
 * xsl:key 
 */
public class KeyValuesTable
{
    private String _keyName;
    private Hashtable _table = new Hashtable();
    
    /**
     * @param pattern the key's match pattern
     * @param valueExpr the expression we'll evaluate against each
     *   matched node to find its "value" (the key we use for lookup)
     * @param n a node in the document we're indexing
     * @param ExprContext an expression evaluation context for use in
     *    evaluating
     */
    public KeyValuesTable(Pattern pattern, StringExpr valueExpr, 
			  Node n, 
			  ExprContext context)
    { 
	try {
	    indexDoc(pattern, valueExpr, n, context);
	} catch (Exception ex) {
	    // FIXME: debugging code
	    ex.printStackTrace();
	}
    }

    /**
     * return a nodelist of the nodes with the "use" value
     * which matches the supplied argument
     */
    public NodeIterator get(String keyValue)
    {
	NodeArray na = (NodeArray) _table.get(keyValue);
	if (na == null) {
	    return new NullNodeIterator();
	} 
	return na.getIterator();
    }


    private void indexDoc(Pattern pattern, StringExpr valueExpr,
			  Node n, ExprContext context)
	throws XSLException
    {

	// I'm gonna guess that the Root node is never assigned a key
	NodeTestExpr nte = new NodeTestExpr(new DescendantAxisExpr(), pattern);

	// So this gives an Iterator over all nodes that match the pattern 
	NodeIterator matchNodesIter = nte.eval(n.getRoot(), context);

	Node matched = matchNodesIter.next();
	while (matched != null) {
	    String key = valueExpr.eval(matched, context);

	    // more than one node can match a key, so we need to index a list
	    NodeArray list = (NodeArray) _table.get(key);
	    if (list == null) {
		list = new NodeArray(matched);
		_table.put(key, list);
	    } else {
		list.add(matched);
	    }
	    matched = matchNodesIter.next();
	}

    }

    private class NodeArray
    {
	Node[] nodes = new Node[1];
	int off = 0;

	NodeArray(Node n)
	{
	    nodes[0] = n;
	}
	    
	void add(Node n) 
	{
	    if (off == 0) {
		Node oldNodes[] = nodes;
		nodes = new Node[oldNodes.length * 2];
		System.arraycopy(oldNodes, 0, nodes, oldNodes.length, 
				 oldNodes.length);
		off = oldNodes.length;
	    }
	    nodes[--off] = n;
	}

	NodeIterator getIterator()
	{
	    return new ArrayNodeIterator(nodes, off, nodes.length);
	}
    }
}






