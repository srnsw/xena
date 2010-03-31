// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.om.*;

class ElementNode extends ContainerNode
{
    private Name name;
    private AttributeNode[] attributeNodes = null;

    ElementNode(org.w3c.dom.Node domNode, ContainerNode parent, int childIndex)
    {
        super(domNode, parent, childIndex);
        org.w3c.dom.NamedNodeMap domAttributes = domNode.getAttributes();
        int len = domAttributes.getLength();
        if (len == 0)
            return;
        attributeNodes = new AttributeNode[len];
        int firstPrefixIndex = -1;
        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node domAttribute = domAttributes.item(i);
            String qName = domAttribute.getNodeName();
            switch (qName.indexOf(':')) {
            case -1:
                if (qName.equals("xmlns")) {
                    String uri = domAttribute.getNodeValue();
                    if (uri.length() == 0)
                        prefixMap = prefixMap.unbindDefault();
                    else
                        prefixMap = prefixMap.bindDefault(uri);
                }
                else
                    attributeNodes[i] = new AttributeNode(root.nameTable.createName(domAttribute.getNodeName()),
                                                          domAttribute,
                                                          this,
                                                          // attributes occur before children
                                                          i - len - 1);
                break;
            case 3:
                if (qName.equals("xml:space")) {
                    String value = domAttribute.getNodeValue();
                    if ("preserve".equals(value))
                        preserveSpace = true;
                    else if ("default".equals(value))
                        preserveSpace = false;
                }
                if (firstPrefixIndex < 0)
                    firstPrefixIndex = i;
                break;
            case 5:
                if (qName.startsWith("xmlns")) {
                    prefixMap = prefixMap.bind(qName.substring(6),
                                               domAttribute.getNodeValue());
                    break;
                }
                // fall through
            default:
                if (firstPrefixIndex < 0)
                    firstPrefixIndex = i;
                break;
            }
        }
        if (firstPrefixIndex >= 0) {
            // global attributes are hopefully relatively rare
            for (int i = firstPrefixIndex; i < len; i++) {
                if (attributeNodes[i] == null) {
                    org.w3c.dom.Node domAttribute = domAttributes.item(i);
                    String qName = domAttribute.getNodeName();
                    if (qName.startsWith("xmlns")
                        && (qName.length() == 5 || qName.charAt(5) == ':'))
                        ;
                    else {
                        try {
                            attributeNodes[i]
                                = new AttributeNode(prefixMap.expandAttributeName(domAttribute.getNodeName(),
                                                                                  null),
                                                    domAttribute,
                                                    this,
                                                    // attributes occur before children
                                                    i - len - 1);
                        }
                        catch (XSLException e) { }
                    }
                }
            }
        }
    }


    public byte getType() {
        return ELEMENT;
    }

    public Name getName() {
        if (name == null) {
            String qName = domNode.getNodeName();
            try {
                name = prefixMap.expandElementTypeName(qName, null);
            }
            catch (XSLException e) {
                name = root.nameTable.createName(qName);
            }
        }
        return name;
    }

    public String getAttributeValue(Name name) {
        if (attributeNodes == null)
            return null;
        for (int i = 0; i < attributeNodes.length; i++)
            if (attributeNodes[i] != null && name.equals(attributeNodes[i].name))
                return attributeNodes[i].getData();
        return null;
    }

    public Node getAttribute(Name name) {
        if (attributeNodes == null)
            return null;
        for (int i = 0; i < attributeNodes.length; i++)
            if (attributeNodes[i] != null && name.equals(attributeNodes[i].name))
                return attributeNodes[i];
        return null;
    }

    class AttributesIterator implements SafeNodeIterator {
        int i = 0;
        public Node next() {
            while (i < attributeNodes.length) {
                Node attributeNode = attributeNodes[i++];
                if (attributeNode != null)
                    return attributeNode;
            }
            return null;
        }
    }

    public SafeNodeIterator getAttributes() {
        if (attributeNodes == null)
            return NullNodeIterator.getInstance();
        return new AttributesIterator();
    }

    public boolean getPreserveSpace() {
        return preserveSpace || !root.loadContext.getStripSource(getName());
    }
}
