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

package com.jgoodies.looks.plastic;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;


/**
 * Paints a single drag symbol instead of many bumps.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see PlasticSplitPaneUI
 */
final class PlasticSplitPaneDivider extends BasicSplitPaneDivider {


	PlasticSplitPaneDivider(BasicSplitPaneUI ui) {
		super(ui);
	}

    protected JButton createLeftOneTouchButton() {
        JButton btn = super.createLeftOneTouchButton();
        btn.setOpaque(false);
        return btn;
    }

    protected JButton createRightOneTouchButton() {
        JButton btn = super.createRightOneTouchButton();
        btn.setOpaque(false);
        return btn;
    }

    public void paint(Graphics g) {
        if (splitPane.isOpaque()) {
    		Color bgColor = getBackground();
    		if (bgColor != null) {
    			g.setColor(bgColor);
    			g.fillRect(0, 0, getWidth(), getHeight());
    		}
        }

		/*
		Object value = splitPane.getClientProperty("add3D");
		if (value != null && value.equals(Boolean.TRUE)) {
			Rectangle r = new Rectangle(0, 0, size.width, size.height);
			FreebopUtils.addLight3DEffekt(g, r, true, false);
		}
		*/

		//paintDragRectangle(g);
		super.paint(g);
	}

	/*
	private void paintDragRectangle(Graphics g) {
		Dimension size = getSize();
		int xCenter = size.width / 2;
		int yCenter = size.height / 2;
		int x = xCenter - 2;
		int y = yCenter - 2;
		int w = 4;
		int h = 4;

		Color down = UIManager.getColor("controlDkShadow");
		Color up   = UIManager.getColor("controlHighlight");

		g.translate(x, y);

		g.setColor(down);
		g.drawLine(0, 1, 0, h - 1); // left
		g.drawLine(0, 0, w - 1, 0); // top

		g.setColor(up);
		g.drawLine(w - 1, 1, w - 1, h - 1);
		g.drawLine(1, h - 1, w - 1, h - 1);

		g.translate(-x, -y);

		super.paint(g);
	}


	private void paintDragLines(Graphics g) {
		Dimension size = getSize();
		Color bgColor = getBackground();

		if (bgColor != null) {
			g.setColor(bgColor);
			g.fillRect(0, 0, size.width, size.height);
		}

		int xCenter = size.width / 2;
		int yCenter = size.height / 2;
		int y0 = yCenter - 10;
		int y1 = yCenter + 10;

		Color dark = UIManager.getColor("controlDkShadow");
		int bars = 3;

		g.setColor(dark);
		for (int i = 0; i < bars; i++) {
			int x = 2 * i + xCenter - bars;
			g.drawLine(x, y0, x, y1);
		}

		super.paint(g);
	}
	*/
}