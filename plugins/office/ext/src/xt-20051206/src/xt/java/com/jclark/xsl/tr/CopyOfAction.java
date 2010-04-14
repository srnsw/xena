// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;

/**
 * <xsl:copy-of
 */
class CopyOfAction implements Action
{
    private final VariantExpr expr;

    CopyOfAction(VariantExpr expr) 
    {
        this.expr = expr;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, Node sourceNode, 
                       Result result) throws XSLException
    {
        Variant value = expr.eval(sourceNode, context);
        if (value instanceof ResultFragmentVariant) {
            ((ResultFragmentVariant)value).append(result);
        }
        else if (value.isNodeSet()) {
            copyNodes(context, value.convertToNodeSet(), result);
        } else {
            result.characters(value.convertToString());
        }
    }

    /**
     *
     */
    private static void copyNodes(ProcessContext context, NodeIterator iter,
                                  Result result) throws XSLException
    {
        for (;;) {
            Node node = iter.next();
            if (node == null)
                break;
            switch (node.getType()) {

            case Node.ROOT:
                copyNodes(context, node.getChildren(), result);
                break;

            case Node.TEXT:
                result.characters(node.getData());
                break;

            case Node.ATTRIBUTE:
                result.attribute(node.getName(), node.getData());
                break;

            case Node.PROCESSING_INSTRUCTION:
                result.processingInstruction(node.getName().toString(),
                                             node.getData());
                break;

            case Node.COMMENT:
                result.comment(node.getData());
                break;

            case Node.ELEMENT:
                result.startElement(node.getName(),
                                    node.getNamespacePrefixMap());
                copyNodes(context, node.getAttributes(), result);
                copyNodes(context, node.getChildren(), result);
                result.endElement(node.getName());
                break;
            }
        }
    }
}
