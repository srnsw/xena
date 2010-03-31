// $Id$

package com.jclark.xsl.dom;

 import com.jclark.xsl.om.*;

class ProcessingInstructionNode extends NodeBase
{
    ProcessingInstructionNode(org.w3c.dom.Node domNode,
                              ContainerNode parent,
                              int childIndex)
    {
        super(domNode, parent, childIndex);
    }

    public byte getType()
    {
        return PROCESSING_INSTRUCTION;
    }

    public Name getName()
    {
        return root.nameTable.createName(domNode.getNodeName());
    }

    public final String getData()
    {
        return domNode.getNodeValue();
    }
}
