// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.conv.NumberListFormat;
import com.jclark.xsl.expr.Pattern;
import com.jclark.xsl.expr.DescendantsOrSelfNodeIterator;
import java.util.Hashtable;

class AnyLevelNumberAction implements Action
{
    private Pattern count;
    private Pattern from;
    private NumberListFormatTemplate formatTemplate;

    AnyLevelNumberAction(Pattern count, Pattern from,
                         NumberListFormatTemplate formatTemplate)
    {
        this.count = count;
        this.from = from;
        this.formatTemplate = formatTemplate;
    }

    static final class Cache
    {
        Cache() {
            nodes = new Node[10];
            used = 0;
        }
        Node[] nodes;
        int[] numbers;
        int used;
        private void append(Node node, int n) {
            if (used == nodes.length) {
                Node[] oldNodes = nodes;
                nodes = new Node[oldNodes.length * 2];
                System.arraycopy(oldNodes, 0, nodes, 0, oldNodes.length);
                if (numbers != null) {
                    int[] oldNumbers = numbers;
                    numbers = new int[oldNumbers.length * 2];
                    System.arraycopy(oldNumbers, 0, numbers, 0, oldNumbers.length);
                }
            }
            nodes[used] = node;
            if (numbers != null) {
                numbers[used] = n;
            } else if (n != used) {
                numbers = new int[nodes.length];
                for (int i = 0; i < used; i++) {
                    numbers[i] = i;
                }
                numbers[used] = n;
            }
            used++;
        }

        private int numberOf(int i) {
            if (numbers == null) {
                return i + 1;
            }
            return numbers[i] + 1;
        }

        int getNumber(Node node) {
            int start = 0;
            int end = used;
            // find last entry before or equal to node
            while (start != end) {
                int mid = (start + end) >> 1;
                int cmp = node.compareTo(nodes[mid]);
                if (cmp == 0) {
                    return numberOf(mid);
                }
                if (cmp < 0) {
                    end = mid;
                } else {
                    start = mid;
                }
            }
            if (start == 0) {
                return 0;
            }
            return numberOf(start - 1);
        }
    }

    public void invoke(ProcessContext context, Node node, 
                       Result result) throws XSLException
    {
        NumberListFormat format = formatTemplate.instantiate(context, node);
        Node root = node.getRoot();
        Hashtable documentTable = (Hashtable)context.get(this);
        if (documentTable == null) {
            documentTable = new Hashtable();
            context.put(this, documentTable);
        }
        Cache cache;
        if (count != null) {
            cache = (Cache)documentTable.get(root);
            if (cache == null) {
                cache = new Cache();
                documentTable.put(root, cache);
                int n = 0;
                for (NodeIterator iter = 
                         new DescendantsOrSelfNodeIterator(root);;) {
                    Node tem = iter.next();
                    if (tem == null) {
                        break;
                    }
                    if (from != null && from.matches(tem, context)) {
                        n = 0;
                    } else if (count.matches(tem, context)) {
                        cache.append(tem, n++);
                    }
                }
            }
        }
        else if (node.getType() == Node.ELEMENT) {
            Hashtable elementTable = (Hashtable)documentTable.get(root);
            if (elementTable == null) {
                elementTable = new Hashtable();
                documentTable.put(root, elementTable);
            }
            Name name = node.getName();
            cache = (Cache)elementTable.get(name);
            if (cache == null) {
                cache = new Cache();
                elementTable.put(name, cache);
                int n = 0;
                for (NodeIterator iter = new DescendantsOrSelfNodeIterator(root);;) {
                    Node tem = iter.next();
                    if (tem == null)
                        break;
                    if (from != null && from.matches(tem, context))
                        n = 0;
                    else if (name.equals(tem.getName()) && tem.getType() == Node.ELEMENT)
                        cache.append(tem, n++);
                }
            }
        }
        else
            return;
        result.characters(format.getPrefix(0));
        result.characters(format.formatNumber(0, cache.getNumber(node)));
        result.characters(format.getSuffix());
    }
}
