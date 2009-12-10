// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.Pattern;
import com.jclark.xsl.conv.NumberListFormat;

/**
 *
 */
class SingleLevelNumberAction implements Action
{
    private Pattern count;
    private Pattern from;
    private NumberListFormatTemplate formatTemplate;

    SingleLevelNumberAction(Pattern count, Pattern from,
                            NumberListFormatTemplate formatTemplate)
    {
        this.count = count;
        this.from = from;
        this.formatTemplate = formatTemplate;
    }


    public void invoke(ProcessContext context, Node node, 
                       Result result) throws XSLException
    {
        NumberListFormat format = formatTemplate.instantiate(context, node);
        result.characters(format.getPrefix(0));
        if (count == null) {
            if (node.getType() == Node.ELEMENT) {
                Name name = node.getName();
                int n = 0;
                for (NodeIterator iter = node.getParent().getChildren();;) {
                    Node tem = iter.next();
                    if (name.equals(tem.getName()) && 
                        tem.getType() == Node.ELEMENT) {
                        n++;
                        if (tem.equals(node))
                            break;
                    }
                }
                result.characters(format.formatNumber(0, n));
            }
        } else {
            if (node.getType() == Node.ATTRIBUTE)
                node = node.getParent();
            do {
                Node parent = node.getParent();
                if (count.matches(node, context)) {
                    int n = 0;
                    for (NodeIterator iter = parent.getChildren() ; ; ) {
                        Node tem = iter.next();
                        if (count.matches(tem, context)) {
                            ++n;
                            if (tem.equals(node))
                                break;
                        }
                    }
                    result.characters(format.formatNumber(0, n));
                    break;
                }
                if (from != null && from.matches(node, context))
                    break;
                node = parent;
            } while (node != null);
        }
        result.characters(format.getSuffix());
    }
}
