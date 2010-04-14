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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

/**
 * An implementation of the <code>Icon</code> interface that has a minimum size
 * and active border. The minimum size is read from the <code>UIManager</code>
 * <code>defaultIconSize</code> key.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see	MinimumSizedIcon
 */

public final class MinimumSizedCheckIcon extends MinimumSizedIcon {

	private final JMenuItem menuItem;

	public MinimumSizedCheckIcon(Icon icon, JMenuItem menuItem) {
		super(icon);
		this.menuItem = menuItem;
	}


	public void paintIcon(Component c, Graphics g, int x, int y) {
		paintState(g, x, y);
		super.paintIcon(c, g, x, y);
	}

	private void paintState(Graphics g, int x, int y) {
		ButtonModel model = menuItem.getModel();
		//if (!model.isEnabled()) return;

		int w = getIconWidth();
		int h = getIconHeight();

		g.translate(x, y);
		if (model.isSelected() || model.isArmed() /* && model.isPressed()*/) {
			Color background = model.isArmed()
								? UIManager.getColor("MenuItem.background")
								: UIManager.getColor("ScrollBar.track");
			Color upColor	 = UIManager.getColor("controlLtHighlight");
			Color downColor	 = UIManager.getColor("controlDkShadow");

			// Background
			g.setColor(background);
			g.fillRect(0, 0, w, h);
			// Top and left border
			g.setColor(model.isSelected() ? downColor : upColor);
			g.drawLine(0, 0, w-2, 0);
			g.drawLine(0, 0, 0, h-2);
			// Bottom and right border
			g.setColor(model.isSelected() ? upColor: downColor);
			g.drawLine(0, h-1, w-1, h-1);
			g.drawLine(w-1, 0, w-1, h-1);
		}
		g.translate(-x, -y);
		g.setColor(UIManager.getColor("textText"));
	}


}