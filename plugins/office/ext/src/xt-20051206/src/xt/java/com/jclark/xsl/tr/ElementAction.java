// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.StringExpr;

class ElementAction implements Action
{
    private StringExpr nameExpr;
    private StringExpr namespaceExpr;
    private NamespacePrefixMap nsMap;
    private Action content;

    /**
     *
     */
    ElementAction(StringExpr nameExpr, StringExpr namespaceExpr,
                  NamespacePrefixMap nsMap, Action content)
    {
        this.nameExpr = nameExpr;
        this.namespaceExpr = namespaceExpr;
        this.nsMap = nsMap;
        this.content = content;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, Node sourceNode, Result result)
        throws XSLException
    {
        String qname = nameExpr.eval(sourceNode, context);
        NamespacePrefixMap map = 
            nsMap.getNameTable().getEmptyNamespacePrefixMap();
        Name name;
        if (namespaceExpr != null) {
            int i = qname.indexOf(':');
            String ns = namespaceExpr.eval(sourceNode, context);
            if (ns.length() == 0) {
                name = nsMap.getNameTable().createName(qname.substring(i + 1));
            } else {
                if (i > 0) {
                    map = map.bind(qname.substring(0, i), ns);
                } else {
                    map = map.bindDefault(ns);
                }
                name = map.expandElementTypeName(qname, null);
            }
        } else {
            name = nsMap.expandElementTypeName(qname, sourceNode);
            String ns = name.getNamespace();
            if (ns != null) {
                String prefix = name.getPrefix();
                if (prefix != null) {
                    map = map.bind(prefix, ns);
                } else {
                    map = map.bindDefault(ns);
                }
            }
        }
        result.startElement(name, map);
        if (content != null) {
            content.invoke(context, sourceNode, result);
        }
        result.endElement(name);
    }
}
