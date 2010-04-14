// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * a result tree fragment to be stored in a variable?
 */
class ActionResultFragmentVariant extends ResultFragmentVariantBase
{
    final private Action action;
    final private Node stylesheetNode;
    final private ExtensionHandler extensionHandler;
    final private Node node;

    final private ProcessContext.Memento memento;

    /**
     *
     */
    ActionResultFragmentVariant(Action action,
                                Node stylesheetNode,
                                ExtensionHandler extensionHandler,
                                Node node,
                                ProcessContext.Memento memento)
    {
        this.action = action;
        this.stylesheetNode = stylesheetNode;
        this.extensionHandler = extensionHandler;
        this.node = node;
        this.memento = memento;
    }

    public void append(Result result) throws XSLException
    {
        memento.invoke(action, node, result);
    }

    public Object convertToObject() throws XSLException
    {
        return extensionHandler.wrapResultFragmentVariant(this);
    }

    public Node getBaseNode()
    {
        return stylesheetNode;
    }
  
}
