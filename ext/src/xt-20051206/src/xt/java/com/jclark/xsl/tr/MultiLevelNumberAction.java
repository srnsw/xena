// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.Pattern;
import com.jclark.xsl.conv.NumberListFormat;

/**
 *
 */
class MultiLevelNumberAction implements Action
{
    private Pattern count;
    private Pattern from;
    private NumberListFormatTemplate formatTemplate;

    /**
     *
     */
    static final class CacheEntry
    {
        Node parent;		// a node that matches count
        Node child;	        // a child of parent that matches count
        int n;			// child is the n-th such chil
    }

    /**
     *
     */
    static final class Cache
    {
        static final int SPARE = 4;
        CacheEntry[] entries;
        CacheEntry get(int level) {
            if (entries == null) {
                entries = new CacheEntry[level + 1 + SPARE];
            }
            else if (level >= entries.length) {
                CacheEntry[] old = entries;
                entries = new CacheEntry[level + 1 + SPARE];
                System.arraycopy(old, 0, entries, 0, old.length);
            }
            if (entries[level] == null) {
                entries[level] = new CacheEntry();
            }
            return entries[level];
        }
    }

    /**
     *
     */
    MultiLevelNumberAction(Pattern count, Pattern from, 
                           NumberListFormatTemplate formatTemplate)
    {
        this.count = count;
        this.from = from;
        this.formatTemplate = formatTemplate;
    }

    /**
     *
     */
    public void invoke(ProcessContext context, Node node,
                       Result result) 
        throws XSLException
    {
        NumberListFormat format = formatTemplate.instantiate(context, node);
        int n;
        if (count == null) {
            if (node.getType() == Node.ELEMENT) {
                n = numberUp(node.getName(), format, context, node, result);
            } else {
                n = 0;
            }
        } else {
            Cache cache = (Cache)context.get(this);
            if (cache == null) {
                cache = new Cache();
                context.put(this, cache);
            }
            n = numberUp(format, context, cache, node, result);
        }
        if (n == 0) {
            result.characters(format.getPrefix(0));
        }
        result.characters(format.getSuffix());
    }
  
    /**
     *
     */
    private int numberUp(NumberListFormat format,
                         ProcessContext context,
                         Cache cache,
                         Node node,
                         Result result) throws XSLException
    {
        do {
            Node parent = node.getParent();
            if (from != null && from.matches(node, context)) {
                break;
            }
            if (count.matches(node, context)) {
                int level = numberUp(format, context, cache, parent, result);
                CacheEntry entry = cache.get(level);
                int n;
                if (parent == null) {
                    n = 1;
                } else if (node.equals(entry.child)) {
                    n = entry.n;
                } else if (parent.equals(entry.parent) 
                           && entry.child.compareTo(node) < 0) {
                    n = entry.n;
                    for (NodeIterator iter = entry.child.getFollowingSiblings();;) {
                        Node tem = iter.next();
                        if (count.matches(tem, context)) {
                            ++n;
                            if (tem.equals(node))
                                break;
                        }
                    }
                    entry.n = n;
                    entry.child = node;
                }
                else {
                    n = 0;
                    for (NodeIterator iter = parent.getChildren();;) {
                        Node tem = iter.next();
                        if (count.matches(tem, context)) {
                            ++n;
                            if (tem.equals(node)) {
                                break;
                            }
                        }
                    }
                    entry.parent = parent;
                    entry.child = node;
                    entry.n = n;
                }
                result.characters(format.getPrefix(level));
                result.characters(format.formatNumber(level, n));
                return level + 1;
            }
            node = parent;
        } while (node != null);
        return 0;
    }

    /**
     *
     */
    private int numberUp(Name name,
                         NumberListFormat format,
                         ProcessContext context,
                         Node node,
                         Result result) throws XSLException
    {
        int i = 0;
        for (Node tem = node.getParent(); tem != null; tem = tem.getParent()) {
            if (name.equals(tem.getName())) {
                i = numberUp(name, format, context, tem, result);
                break;
            }
        }
        int n = 0;
        for (NodeIterator iter = node.getParent().getChildren();;) {
            Node tem = iter.next();
            if (name.equals(tem.getName()) && tem.getType() == Node.ELEMENT) {
                n++;
                if (tem.equals(node)) {
                    break;
                }
            }
        }
        result.characters(format.getPrefix(i));
        result.characters(format.formatNumber(i, n));
        return i + 1;
    }
}
