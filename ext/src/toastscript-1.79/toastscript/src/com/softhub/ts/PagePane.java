
package com.softhub.ts;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JPanel;

public class PagePane extends JPanel {

	private BorderLayout layout = new BorderLayout();
	private PageBorder border = new PageBorder();
	private PageCanvas canvas = new PageCanvas();
	private float width, height;

	public PagePane() {
		try {
		    jbInit();
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setLayout(layout);
		border.setBackgroundColor(Color.lightGray);
		setBorder(border);
		add(canvas, BorderLayout.CENTER);
	}

	public boolean updatePage(Rectangle viewBounds) {
		Rectangle bounds = getBounds();
		Rectangle r = viewBounds.intersection(bounds);
		int areaA = r.width * r.height;
		int areaB = viewBounds.width * viewBounds.height;
		canvas.activate(areaA > 0);
		return areaA * 2 > areaB;
	}

	public void updatePageSize(float width, float height, float scale) {
		this.width = width;
		this.height = height;
		Insets insets = getInsets();
		float dpi = getToolkit().getScreenResolution();
		float factor = scale * dpi / 72;
		int w = Math.round(width * factor) + insets.left + insets.right;
		int h = Math.round(height * factor) + insets.top + insets.bottom;
		setSize(w, h);
		canvas.setScale(scale);
	}

	public float getPageWidth() {
		return width;
	}

	public float getPageHeight() {
		return height;
	}

	public void updatePageScale(float scale) {
		updatePageSize(width, height, scale);
	}

	public PageCanvas getPageCanvas() {
		return canvas;
	}

}
