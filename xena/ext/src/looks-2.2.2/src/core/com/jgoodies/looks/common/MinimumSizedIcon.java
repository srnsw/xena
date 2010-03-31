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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;

import com.jgoodies.looks.Options;

/**
 * An <code>Icon</code> with a minimum size that is read from the
 * <code>UIManager</code> <code>defaultIconSize</code> key.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */

public class MinimumSizedIcon implements Icon {

	private final Icon icon;
	private final int  width;
	private final int  height;
	private final int  xOffset;
	private final int  yOffset;


	public MinimumSizedIcon() {
		this(null);
	}

	public MinimumSizedIcon(Icon icon) {
		Dimension minimumSize = Options.getDefaultIconSize();
		this.icon      = icon;
		int iconWidth  = icon == null ? 0 : icon.getIconWidth();
		int iconHeight = icon == null ? 0 : icon.getIconHeight();
		width   = Math.max(iconWidth,  Math.max(20, minimumSize.width));
		height  = Math.max(iconHeight, Math.max(20, minimumSize.height));
		xOffset = Math.max(0, (width  - iconWidth)  / 2);
		yOffset = Math.max(0, (height - iconHeight) / 2);
	}


	public int getIconHeight() {  return height;	}
	public int getIconWidth()	{  return width;	}


	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (icon != null)
			icon.paintIcon(c, g, x + xOffset, y + yOffset);
	}


}