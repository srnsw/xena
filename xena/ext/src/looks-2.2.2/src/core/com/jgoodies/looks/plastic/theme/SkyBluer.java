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

package com.jgoodies.looks.plastic.theme;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticScrollBarUI;
import com.jgoodies.looks.plastic.PlasticTheme;

/**
 * A theme with medium blue primary colors and a light gray window background.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public class SkyBluer extends PlasticTheme {

    public String getName() {
        return "Sky Bluer";
    }

    protected ColorUIResource getPrimary1() {
        return Colors.BLUE_MEDIUM_DARKEST;
    }

    protected ColorUIResource getPrimary2() {
        return Colors.BLUE_MEDIUM_MEDIUM;
    }

    protected ColorUIResource getPrimary3() {
        return Colors.BLUE_MEDIUM_LIGHTEST;
    }

    protected ColorUIResource getSecondary1() {
        return Colors.GRAY_MEDIUMDARK;
    }

    protected ColorUIResource getSecondary2() {
        return Colors.GRAY_LIGHT;
    }

    protected ColorUIResource getSecondary3() {
        return Colors.GRAY_LIGHTER;
    }

    public ColorUIResource getMenuItemSelectedBackground() {
        return getPrimary2();
    }

    public ColorUIResource getMenuItemSelectedForeground() {
        return getWhite();
    }

    public ColorUIResource getMenuSelectedBackground() {
        return getSecondary2();
    }

    public ColorUIResource getFocusColor() {
        return PlasticLookAndFeel.getHighContrastFocusColorsEnabled()
            ? Colors.YELLOW_FOCUS
            : super.getFocusColor();
    }

    /*
     * TODO: The following two lines are likely an improvement.
     *       However, they require a rewrite of the PlasticInternalFrameTitlePanel.
    public    ColorUIResource getWindowTitleBackground() 		{ return getPrimary1(); }
    public    ColorUIResource getWindowTitleForeground() 		{ return WHITE; 		}
    */

    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        Object[] uiDefaults =
            { PlasticScrollBarUI.MAX_BUMPS_WIDTH_KEY, new Integer(30), };
        table.putDefaults(uiDefaults);
    }

}
