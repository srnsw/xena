// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantBase;
import java.net.URL;

/**
 * a result tree fragment, I guess used as a variable or param?
 */
public abstract class ResultFragmentVariantBase 
    extends VariantBase implements ResultFragmentVariant 
{
    private String cachedStringValue = null;
    private Node cachedNode = null;

    /**
     *
     */
    public String convertToString() throws XSLException
    {
        if (cachedStringValue == null) {
            StringResult result = new StringResult();
            append(result);
            cachedStringValue = result.toString();
        }
        return cachedStringValue;
    }

    /**
     *
     */
    public boolean convertToBoolean() throws XSLException
    {
        return true;
    }

    /**
     * for converting to a NodeSet
     */
    public Node getTree(ProcessContext context) throws XSLException
    {

        if (cachedNode == null) {
            Node[] rootNodeRef = new Node[1];
            Result result = 
                context.createNodeResult(getBaseNode(), rootNodeRef);

            result.start(null);

            append(result);

            result.end();

            cachedNode = rootNodeRef[0];
        }
        return cachedNode;
    }
}
