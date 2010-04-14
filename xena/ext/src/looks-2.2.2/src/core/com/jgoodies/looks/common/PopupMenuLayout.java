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

package com.jgoodies.looks.common;

import java.awt.Container;

import javax.swing.BoxLayout;
import javax.swing.JPopupMenu;
import javax.swing.plaf.UIResource;


/**
 * The JGoodies implementation of a layout manager for Popup menus.
 * In comparison to the JDK's implementation it flushes the values of the client
 * properties <code>maxTextWidth</code> and <code>maxAccWidth</code> in
 * the method {@link #invalidateLayout(Container)} and not in the method
 * {@link #preferredLayoutSize(Container)}.
 *
 * @author Andrej Golovnin
 * @version $Revision$
 */
public final class PopupMenuLayout extends BoxLayout implements UIResource {


    /**
     * Creates a layout manager that will lay out components along
     * the given axis.
     *
     * @param target  the container that needs to be laid out
     * @param axis    the axis to lay out components along
     */
    public PopupMenuLayout(Container target, int axis) {
        super(target, axis);
    }


    /**
     * Indicates that a child has changed its layout related information,
     * and thus any cached calculations should be flushed.
     * <p>
     * In case the target is an instance of JPopupMenu it flushes the values of
     * the client properties <code>maxTextWidth</code> and <code>maxAccWidth</code>.
     *
     * @param target  the affected container
     */
    public synchronized void invalidateLayout(Container target) {
        if (target instanceof JPopupMenu) {
            JPopupMenu menu = (JPopupMenu) target;
            menu.putClientProperty(MenuItemRenderer.MAX_TEXT_WIDTH, null);
            menu.putClientProperty(MenuItemRenderer.MAX_ACC_WIDTH,  null);
        }
        super.invalidateLayout(target);
    }


}
