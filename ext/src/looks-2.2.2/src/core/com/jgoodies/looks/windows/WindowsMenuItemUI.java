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

package com.jgoodies.looks.windows;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;

import com.jgoodies.looks.common.ExtBasicMenuItemUI;
import com.jgoodies.looks.common.MenuItemRenderer;

/**
 * The JGoodies Windows look&amp;feel implementation of <code>MenuItemUI</code>.<p>
 *
 * It differs from the superclass in that it uses a Windows specific
 * menu item renderer that checks if mnemonics shall be shown or hidden
 * and may paint disabled text with a shadow.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public final class WindowsMenuItemUI extends ExtBasicMenuItemUI {


    public static ComponentUI createUI(JComponent b) {
        return new WindowsMenuItemUI();
    }


    protected MenuItemRenderer createRenderer(
            JMenuItem menuItem,
            boolean iconBorderEnabled,
            Font    acceleratorFont,
            Color   selectionForeground,
            Color   disabledForeground,
            Color   acceleratorForeground,
            Color   acceleratorSelectionForeground) {
        return new WindowsMenuItemRenderer(
                menuItem,
                iconBorderEnabled(),
                acceleratorFont,
                selectionForeground,
                disabledForeground,
                acceleratorForeground,
                acceleratorSelectionForeground);
    }


}