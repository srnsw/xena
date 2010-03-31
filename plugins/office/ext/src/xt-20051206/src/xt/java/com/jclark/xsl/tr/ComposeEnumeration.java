// $Id$

package com.jclark.xsl.tr;

import java.util.Enumeration;

public
    class ComposeEnumeration implements Enumeration
    {
        private Enumeration top;
        private Enumeration cur;

        public ComposeEnumeration(Enumeration top)
        {
            this.top = top;
            this.cur = top.hasMoreElements() ? (Enumeration)top.nextElement() : top;
        }

        public boolean hasMoreElements()
        {
            for (; !cur.hasMoreElements(); cur = (Enumeration)top.nextElement()) {
                if (!top.hasMoreElements())
                    return false;
            }
            return true;
        }
        public Object nextElement()
        {
            for (; !cur.hasMoreElements(); cur = (Enumeration)top.nextElement())
                ;
            return cur.nextElement();
        }
    }
