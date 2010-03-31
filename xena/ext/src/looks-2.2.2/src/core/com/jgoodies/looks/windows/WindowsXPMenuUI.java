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
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import com.jgoodies.looks.common.MenuItemRenderer;

/**
 * The JGoodies Windows XP look&amp;feel implementation of <code>MenuUI</code>.<p>
 *
 * It differs from the superclass in that it uses an overhauled menu
 * rendering an aligmnent system. Furthermore, you can set a client property
 * <tt>Options.NO_ICONS_KEY</tt> to indicate that this menu has no icons.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see	com.jgoodies.looks.Options
 */

public final class WindowsXPMenuUI extends com.sun.java.swing.plaf.windows.WindowsMenuUI {

    private static final String MENU_PROPERTY_PREFIX    = "Menu";
    private static final String SUBMENU_PROPERTY_PREFIX = "MenuItem";

    // May be changed to SUBMENU_PROPERTY_PREFIX later
    private String propertyPrefix = MENU_PROPERTY_PREFIX;

    private MenuItemRenderer renderer;

    public static ComponentUI createUI(JComponent b) {
        return new WindowsXPMenuUI();
    }

    // Install and Uninstall **************************************************

    protected void installDefaults() {
        super.installDefaults();
        if (arrowIcon == null || arrowIcon instanceof UIResource) {
            arrowIcon = UIManager.getIcon("Menu.arrowIcon");
        }
        renderer =
            new MenuItemRenderer(
                menuItem,
                false,
                acceleratorFont,
                selectionForeground,
                disabledForeground,
                acceleratorForeground,
                acceleratorSelectionForeground);
        Integer gap =
            (Integer) UIManager.get(getPropertyPrefix() + ".textIconGap");
        defaultTextIconGap = gap != null ? gap.intValue() : 2;
    }

    protected void uninstallDefaults() {
        super.uninstallDefaults();
        renderer = null;
    }

    protected String getPropertyPrefix() {
        return propertyPrefix;
    }

    protected Dimension getPreferredMenuItemSize(
        JComponent c,
        Icon aCheckIcon,
        Icon anArrowIcon,
        int textIconGap) {

        if (isSubMenu(menuItem)) {
            ensureSubMenuInstalled();
            return renderer.getPreferredMenuItemSize(
                c,
                aCheckIcon,
                anArrowIcon,
                textIconGap);
        } else {
            Dimension size =
                super.getPreferredMenuItemSize(
                    c,
                    aCheckIcon,
                    anArrowIcon,
                    textIconGap);
            int width = size.width;
            int height = size.height;
            if (height % 2 == 1)
                height--;
            return new Dimension(width, height);
        }
    }

    protected void paintMenuItem(
        Graphics g,
        JComponent c,
        Icon aCheckIcon,
        Icon anArrowIcon,
        Color background,
        Color foreground,
        int textIconGap) {
        if (isSubMenu(menuItem)) {
            renderer.paintMenuItem(
                g,
                c,
                aCheckIcon,
                anArrowIcon,
                background,
                foreground,
                textIconGap);
        } else {
            super.paintMenuItem(
                g,
                c,
                aCheckIcon,
                anArrowIcon,
                background,
                foreground,
                textIconGap);
        }
    }

    /**
     * Checks if we have already detected the correct menu type,
     * menu in menu bar vs. sub menu; reinstalls if necessary.
     */
    private void ensureSubMenuInstalled() {
        if (propertyPrefix.equals(SUBMENU_PROPERTY_PREFIX))
            return;

        ButtonModel model = menuItem.getModel();

        //save values of armed and selected properties.
        //they will be resetted in #ununinstallDefaults().
        boolean oldArmed = model.isArmed();
        boolean oldSelected = model.isSelected();

        uninstallDefaults();
        propertyPrefix = SUBMENU_PROPERTY_PREFIX;
        installDefaults();

        //restore values of armed and selected properties
        model.setArmed(oldArmed);
        model.setSelected(oldSelected);
    }


    // Helper Code **********************************************************

    private boolean isSubMenu(JMenuItem aMenuItem) {
        return !((JMenu) aMenuItem).isTopLevelMenu();
    }


}