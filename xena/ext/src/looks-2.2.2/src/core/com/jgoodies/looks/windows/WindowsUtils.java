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

import java.awt.Graphics;

import javax.swing.UIManager;

/**
 * Drawing utils.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */

final class WindowsUtils {

    private WindowsUtils() {
        // Override default constructor; prevents instantiation.
    }

	/*
	static class CheckBoxIcon implements Icon, Serializable, UIResource {
		final static int csize = 13;

		private final Color background;
		private final Color highlight;
		private final Color shadow;
		private final Color darkShadow;

		CheckBoxIcon(Color background, Color highlight, Color shadow, Color darkShadow) {
			this.background = background;
			this.highlight	= highlight;
			this.shadow		= shadow;
			this.darkShadow = darkShadow;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			AbstractButton b = (AbstractButton) c;
			ButtonModel model = b.getModel();

			// outer bevel
			g.setColor(background);
			g.fill3DRect(x, y, csize, csize, false);

			// inner bevel
			g.setColor(shadow);
			g.fill3DRect(x + 1, y + 1, csize - 2, csize - 2, false);

			// inside box
			if ((model.isPressed() && model.isArmed()) || !model.isEnabled()) {
				g.setColor(background);
			} else {
				g.setColor(highlight);
			}
			g.fillRect(x + 2, y + 2, csize - 4, csize - 4);

			if (model.isEnabled()) {
				g.setColor(darkShadow);
			} else {
				g.setColor(shadow);
			}

			// paint check
			if (model.isSelected()) {
				g.drawLine(x + 9, y + 3, x + 9, y + 3);
				g.drawLine(x + 8, y + 4, x + 9, y + 4);
				g.drawLine(x + 7, y + 5, x + 9, y + 5);
				g.drawLine(x + 6, y + 6, x + 8, y + 6);
				g.drawLine(x + 3, y + 7, x + 7, y + 7);
				g.drawLine(x + 4, y + 8, x + 6, y + 8);
				g.drawLine(x + 5, y + 9, x + 5, y + 9);
				g.drawLine(x + 3, y + 5, x + 3, y + 5);
				g.drawLine(x + 3, y + 6, x + 4, y + 6);
			}
		}

		public int getIconWidth() {
			return csize;
		}

		public int getIconHeight() {
			return csize;
		}
	}

	static class RadioButtonIcon implements Icon, Serializable, UIResource {
		private final Color background;
		private final Color highlight;
		private final Color shadow;
		private final Color darkShadow;

		RadioButtonIcon(Color background, Color highlight, Color shadow, Color darkShadow) {
			this.background = background;
			this.highlight	= highlight;
			this.shadow		= shadow;
			this.darkShadow = darkShadow;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			AbstractButton b = (AbstractButton) c;
			ButtonModel model = b.getModel();

			// fill interior
			if ((model.isPressed() && model.isArmed()) || !model.isEnabled()) {
				g.setColor(background);
			} else {
				g.setColor(highlight);
			}
			g.fillRect(x + 2, y + 2, 8, 8);

			// outter left arc
			g.setColor(shadow);
			g.drawLine(x + 4, y + 0, x + 7, y + 0);
			g.drawLine(x + 2, y + 1, x + 3, y + 1);
			g.drawLine(x + 8, y + 1, x + 9, y + 1);
			g.drawLine(x + 1, y + 2, x + 1, y + 3);
			g.drawLine(x + 0, y + 4, x + 0, y + 7);
			g.drawLine(x + 1, y + 8, x + 1, y + 9);

			// outter right arc
			g.setColor(highlight);
			g.drawLine(x + 2, y + 10, x + 3, y + 10);
			g.drawLine(x + 4, y + 11, x + 7, y + 11);
			g.drawLine(x + 8, y + 10, x + 9, y + 10);
			g.drawLine(x + 10, y + 9, x + 10, y + 8);
			g.drawLine(x + 11, y + 7, x + 11, y + 4);
			g.drawLine(x + 10, y + 3, x + 10, y + 2);

			// inner left arc
			g.setColor(darkShadow);
			g.drawLine(x + 4, y + 1, x + 7, y + 1);
			g.drawLine(x + 2, y + 2, x + 3, y + 2);
			g.drawLine(x + 8, y + 2, x + 9, y + 2);
			g.drawLine(x + 2, y + 3, x + 2, y + 3);
			g.drawLine(x + 1, y + 4, x + 1, y + 7);
			g.drawLine(x + 2, y + 8, x + 2, y + 8);

			// inner right arc
			g.setColor(background);
			g.drawLine(x + 2, y + 9, x + 3, y + 9);
			g.drawLine(x + 4, y + 10, x + 7, y + 10);
			g.drawLine(x + 8, y + 9, x + 9, y + 9);
			g.drawLine(x + 9, y + 8, x + 9, y + 8);
			g.drawLine(x + 10, y + 7, x + 10, y + 4);
			g.drawLine(x + 9, y + 3, x + 9, y + 3);

			// indicate whether selected or not
			if (model.isSelected()) {
				g.setColor(darkShadow);
				g.fillRect(x + 4, y + 5, 4, 2);
				g.fillRect(x + 5, y + 4, 2, 4);
			}
		}

		public int getIconWidth() {
			return 13;
		}

		public int getIconHeight() {
			return 13;
		}
	}
	*/

    public static void drawRoundedDashedRect(Graphics g, int x, int y, int width, int height) {
        for (int vx = x+1; vx < (x + width); vx += 2) {
            g.fillRect(vx, y, 1, 1);
            g.fillRect(vx, y + height-1, 1, 1);
        }
        int offset = (width + 1) % 2;
        for (int vy = y+1; vy < (y + height - offset); vy += 2) {
            g.fillRect(x, vy, 1, 1);
            g.fillRect(x + width-1, vy+offset, 1, 1);
        }
    }


	static void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(UIManager.getColor("controlLtHighlight"));
		g.drawLine(0, 0, w - 2, 0);
		g.drawLine(0, 0, 0, h - 2);
		g.setColor(UIManager.getColor("controlShadow"));
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(0, h - 1, w - 1, h - 1);
		g.translate(-x, -y);
	}


	static void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
		g.translate(x, y);
		g.setColor(UIManager.getColor("controlShadow"));
		g.drawLine(0, 0, w - 2, 0);
		g.drawLine(0, 0, 0, h - 2);
		g.setColor(UIManager.getColor("controlLtHighlight"));
		g.drawLine(w - 1, 0, w - 1, h - 1);
		g.drawLine(0, h - 1, w - 1, h - 1);
		g.translate(-x, -y);
	}
}
