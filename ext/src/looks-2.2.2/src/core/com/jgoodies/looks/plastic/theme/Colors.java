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

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

/**
 * Consists of base colors useful for building new color themes.
 * <p>
 * The field names use the following naming convention:
 * &lt;color name&gt;_&lt;saturation&gt;_&lt;brightness&gt;,
 * for example: <code>BLUE_HIGH_LIGHT</code> and <code>GREEN_MEDIUM_LIGHTER</code>.<p>
 *
 * The saturation values are: <code>LOW</code>, <code>MEDIUM</code>,
 * and <code>HIGH</code>. Brightness values are:
 * <code>DARKEST</code>, <code>DARKER</code>, <code>DARK</code>,
 * <code>MEDIUM</code>, <code>LIGHT</code>, <code>LIGHTER</code>,
 * and <code>LIGHTEST</code>.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class Colors {

    // Gray Colors **********************************************************************

    static final ColorUIResource GRAY_DARKEST           = new ColorUIResource( 64,  64,  64);
    static final ColorUIResource GRAY_DARKER            = new ColorUIResource( 82,  82,  82);
    static final ColorUIResource GRAY_DARK              = new ColorUIResource( 90,  90,  90);
    static final ColorUIResource GRAY_MEDIUMDARK        = new ColorUIResource(110, 110, 110);
    static final ColorUIResource GRAY_MEDIUM            = new ColorUIResource(128, 128, 128);
    static final ColorUIResource GRAY_MEDIUMLIGHT       = new ColorUIResource(150,
            150, 150);
    static final ColorUIResource GRAY_LIGHT             = new ColorUIResource(170, 170, 170);
    static final ColorUIResource GRAY_LIGHTER           = new ColorUIResource(220, 220, 220);
    static final ColorUIResource GRAY_LIGHTER2          = new ColorUIResource(230, 230, 230);
    static final ColorUIResource GRAY_LIGHTEST          = new ColorUIResource(240, 240, 240);

    // Brown Colors *********************************************************************

    static final ColorUIResource BROWN_LIGHTEST         = new ColorUIResource(242, 241, 238);

    // Blue Colors *********************************************************************

    static final ColorUIResource BLUE_LOW_MEDIUM        = new ColorUIResource(166, 202, 240);
    static final ColorUIResource BLUE_LOW_LIGHTEST      = new ColorUIResource(195, 212, 232);

    static final ColorUIResource BLUE_MEDIUM_DARKEST    = new ColorUIResource( 44,  73, 135);
    static final ColorUIResource BLUE_MEDIUM_DARK       = new ColorUIResource( 49, 106, 196);
    static final ColorUIResource BLUE_MEDIUM_MEDIUM     = new ColorUIResource( 85, 115, 170); // 58, 110, 165);
    static final ColorUIResource BLUE_MEDIUM_LIGHTEST   = new ColorUIResource(172, 210, 248); //189, 220, 251); //153, 179, 205);

    // Green Colors *********************************************************************

    static final ColorUIResource GREEN_LOW_DARK         = new ColorUIResource( 75, 148,  75);
    static final ColorUIResource GREEN_LOW_MEDIUM       = new ColorUIResource(112, 190, 112);
    static final ColorUIResource GREEN_LOW_LIGHTEST     = new ColorUIResource(200, 222, 200);

    static final ColorUIResource GREEN_CHECK            = new ColorUIResource( 33, 161,  33);

    // Pink Colors **********************************************************************

    static final ColorUIResource PINK_HIGH_DARK         = new ColorUIResource(128,   0, 128); //192,   0, 192);
    static final ColorUIResource PINK_LOW_DARK          = new ColorUIResource(128,  70, 128); //192,   0, 192);
    static final ColorUIResource PINK_LOW_MEDIUM        = new ColorUIResource(190, 150, 190);
    static final ColorUIResource PINK_LOW_LIGHTER       = new ColorUIResource(233, 207, 233);

    // Red Colors **********************************************************************

    static final ColorUIResource RED_LOW_DARK           = new ColorUIResource(128,  70,  70);
    static final ColorUIResource RED_LOW_MEDIUM         = new ColorUIResource(190, 112, 112);
    static final ColorUIResource RED_LOW_LIGHTER        = new ColorUIResource(222, 200, 200);

    // Yellow Colors *******************************************************************

    static final ColorUIResource YELLOW_LOW_MEDIUMDARK  = new ColorUIResource(182, 149,  18);
    static final ColorUIResource YELLOW_LOW_MEDIUM      = new ColorUIResource(244, 214,  96);
    static final ColorUIResource YELLOW_LOW_LIGHTEST    = new ColorUIResource(249, 225, 131);

    // Focus Colors *********************************************************************

    static final ColorUIResource BLUE_FOCUS             = BLUE_MEDIUM_LIGHTEST;
    static final ColorUIResource ORANGE_FOCUS           = new ColorUIResource(245, 165,  16);
    static final ColorUIResource YELLOW_FOCUS           = new ColorUIResource(255, 223,  63);
    static final ColorUIResource GRAY_FOCUS             = new ColorUIResource(Color.LIGHT_GRAY);

}