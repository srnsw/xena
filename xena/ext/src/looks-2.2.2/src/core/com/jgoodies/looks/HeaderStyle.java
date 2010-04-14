/*
 * Copyright (c) 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks;

import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * Describes the header styles for JMenuBar and JToolBar.
 * Header styles are look-independent and can be shadowed by a look-dependent
 * <code>BorderStyle</code>.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see	BorderStyle
 */
public final class HeaderStyle {

    public static final HeaderStyle SINGLE = new HeaderStyle("Single");
    public static final HeaderStyle BOTH   = new HeaderStyle("Both");

    private final String name;


    private HeaderStyle(String name) {
        this.name = name;
    }


    /**
     * Looks up the client property for the <code>HeaderStyle</code>
     * from the JToolBar.
     *
     * @param menuBar   the menu bar to inspect
     * @return the menu bar's header style
     */
    public static HeaderStyle from(JMenuBar menuBar) {
        return from0(menuBar);
    }


    /**
     * Looks up the client property for the <code>HeaderStyle</code>
     * from the JToolBar.
     *
     * @param toolBar   the tool bar to inspect
     * @return the tool bar's header style
     */
    public static HeaderStyle from(JToolBar toolBar) {
        return from0(toolBar);
    }


    /**
     * Looks up the client property for the <code>HeaderStyle</code>
     * from the specified JComponent.
     *
     * @param c    the component to inspect
     * @return the header style for the given component
     */
    private static HeaderStyle from0(JComponent c) {
        Object value = c.getClientProperty(Options.HEADER_STYLE_KEY);
        if (value instanceof HeaderStyle)
            return (HeaderStyle) value;

        if (value instanceof String) {
            return HeaderStyle.valueOf((String) value);
        }

        return null;
    }


    /**
     * Looks up and answers the <code>HeaderStyle</code> with the specified name.
     *
     * @param name    the name of the HeaderStyle object to lookup
     * @return the associated HeaderStyle
     */
    private static HeaderStyle valueOf(String name) {
        if (name.equalsIgnoreCase(SINGLE.name))
            return SINGLE;
        else if (name.equalsIgnoreCase(BOTH.name))
            return BOTH;
        else
            throw new IllegalArgumentException("Invalid HeaderStyle name "
                    + name);
    }


    public String toString() {
        return name;
    }

}
