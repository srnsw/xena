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


/**
 * A theme with low saturated blue primary colors and a light brown
 * window background.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public class DesertBlue extends DesertBluer {

	public String getName() { return "Desert Blue"; }


	private static final ColorUIResource SECONDARY2	= new ColorUIResource(148, 144, 140);
	private static final ColorUIResource SECONDARY3	= new ColorUIResource(211, 210, 204);



	protected ColorUIResource getPrimary1()		    { return Colors.GRAY_DARK;         }
	protected ColorUIResource getPrimary2()		    { return Colors.BLUE_LOW_MEDIUM;   }
	protected ColorUIResource getPrimary3()		    { return Colors.BLUE_LOW_LIGHTEST; }

	protected ColorUIResource getSecondary1()	    { return Colors.GRAY_MEDIUM; }
	protected ColorUIResource getSecondary2()	    { return SECONDARY2;         }
	protected ColorUIResource getSecondary3()	    { return SECONDARY3;         }

	public    ColorUIResource getTitleTextColor()	{ return Colors.BLUE_MEDIUM_DARKEST;}

    public ColorUIResource getFocusColor() {
        return PlasticLookAndFeel.getHighContrastFocusColorsEnabled()
            ? Colors.YELLOW_FOCUS
            : Colors.BLUE_MEDIUM_DARK;
    }
	// Background
	public    ColorUIResource getPrimaryControlShadow()		{ return getPrimary3(); }
	public    ColorUIResource getMenuItemSelectedBackground()	{ return getPrimary1(); }

	public void addCustomEntriesToTable(UIDefaults table) {
		super.addCustomEntriesToTable(table);
		Object[] uiDefaults = {
			"ScrollBar.is3DEnabled", 				Boolean.FALSE,
            "ScrollBar.thumbHighlight", 			getPrimaryControlHighlight(),
			PlasticScrollBarUI.MAX_BUMPS_WIDTH_KEY, null,
		};
		table.putDefaults(uiDefaults);
	}


}