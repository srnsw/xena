// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;

class SiblingNodeIterator implements SafeNodeIterator
{
    ContainerNode parent;
    int childIndex;
    NodeBase node;
    org.w3c.dom.Node domNode;
    int preserveSpace = -1;
  
    SiblingNodeIterator(ContainerNode parent,
                        int childIndex,
                        org.w3c.dom.Node domNode)
    {
        this.parent = parent;
        this.childIndex = childIndex;
        this.domNode = domNode;
    }

    static final private boolean hasNonWhitespaceChar(String str)
    {
        int len = str.length();
        for (int i = 0; i < len; i++)
            switch (str.charAt(i)) {
            case ' ':
            case '\n':
            case '\t':
            case '\r':
                break;
            default:
                return true;
            }
        return false;
    }

    final void advance()
    {
        for (;;) {
            org.w3c.dom.Node tem = domNode.getNextSibling();
            if (tem != null) {
                domNode = tem;
                break;
            }
            tem = domNode.getParentNode();
            if (tem.equals(parent.domNode)) {
                domNode = null;
                break;
            }
            domNode = tem;
        }
    }

    static int computeChildIndex(ContainerNode parent, org.w3c.dom.Node domNode)
    {
        int preserveSpace = -1;
        boolean ignoreText = false;
        int childIndex = 0;
    loop:
        for (;;) {
            switch (domNode.getNodeType()) {
            case org.w3c.dom.Node.ELEMENT_NODE:
                childIndex++;
                ignoreText = false;
                break;
            case org.w3c.dom.Node.COMMENT_NODE:
                if (parent.root.includeComments) {
                    ignoreText = false;
                    childIndex++;
                }
                break;
            case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                if (parent.root.includeProcessingInstructions) {
                    ignoreText = false;
                    childIndex++;
                }
                break;
            case org.w3c.dom.Node.TEXT_NODE:
            case org.w3c.dom.Node.CDATA_SECTION_NODE:
                if (!ignoreText) {
                    if (preserveSpace == -1)
                        preserveSpace = parent.getPreserveSpace() ? 1 : 0;
                    if (preserveSpace == 1 || hasNonWhitespaceChar(domNode.getNodeValue())) {
                        ignoreText = true;
                        childIndex++;
                    }
                }
                break;
            case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
                {
                    org.w3c.dom.Node tem = domNode.getLastChild();
                    if (tem != null) {
                        domNode = tem;
                        continue loop;
                    }
                }
                break;
            default:
                ignoreText = false;
                break;
            }
            for (;;) {
                org.w3c.dom.Node tem = domNode.getPreviousSibling();
                if (tem != null) {
                    domNode = tem;
                    break;
                }
                domNode = domNode.getParentNode();
                if (domNode.equals(parent.domNode))
                    break loop;
            }
        }
        return childIndex;
    }

    public Node next() {
        ++childIndex;
        while (domNode != null) {
            switch (domNode.getNodeType()) {
            case org.w3c.dom.Node.ELEMENT_NODE:
                {
                    Node tem = new ElementNode(domNode, parent, childIndex);
                    advance();
                    return tem;
                }
            case org.w3c.dom.Node.COMMENT_NODE:
                if (parent.root.includeComments) {
                    Node tem = new CommentNode(domNode, parent, childIndex);
                    advance();
                    return tem;
                }
                advance();
                break;
            case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                if (parent.root.includeProcessingInstructions) {
                    Node tem = new ProcessingInstructionNode(domNode, parent, childIndex);
                    advance();
                    return tem;
                }
                advance();
                break;
            case org.w3c.dom.Node.TEXT_NODE:
            case org.w3c.dom.Node.CDATA_SECTION_NODE:
                {
                    if (preserveSpace == -1)
                        preserveSpace = parent.getPreserveSpace() ? 1 : 0;
                    boolean keepThisNode = preserveSpace == 1;
                    TextNode textNode = new TextNode(domNode, parent, childIndex);
                    if (!keepThisNode && hasNonWhitespaceChar(domNode.getNodeValue()))
                        keepThisNode = true;
                    advance();
                loop:
                    while (domNode != null) {
                        switch (domNode.getNodeType()) {
                        case org.w3c.dom.Node.TEXT_NODE:
                        case org.w3c.dom.Node.CDATA_SECTION_NODE:
                            {
                                String value = domNode.getNodeValue();
                                if (!keepThisNode && hasNonWhitespaceChar(value))
                                    keepThisNode = true;
                                textNode.merge(value);
                                advance();
                                break;
                            }
                        case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
                            {
                                org.w3c.dom.Node tem = domNode.getFirstChild();
                                if (tem == null)
                                    advance();
                                else
                                    domNode = tem;
                            }
                            break;
                        case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
                            if (parent.root.includeProcessingInstructions)
                                break loop;
                            advance();
                            break;
                        case org.w3c.dom.Node.COMMENT_NODE:
                            if (parent.root.includeComments)
                                break loop;
                            advance();
                            break;
                        default:
                            break loop;
                        }
                    }
                    if (keepThisNode)
                        return textNode;
                    break;
                }
            case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
                {
                    org.w3c.dom.Node tem = domNode.getFirstChild();
                    if (tem == null)
                        advance();
                    else
                        domNode = tem;
                    break;
                }
            default:
                advance();
                break;
            }
        }
        return null;
    }
}
