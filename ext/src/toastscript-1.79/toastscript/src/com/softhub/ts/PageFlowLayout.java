
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

import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

public class PageFlowLayout implements LayoutManager {

    private int vgap = 12;

	public PageFlowLayout() {
	}

	public void setGapV(int vgap) {
		this.vgap = vgap;
	}

    public int getGapV() {
        return vgap;
    }

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	public Dimension preferredLayoutSize(Container target) {
		return new Dimension(80, 80);
	}

	public Dimension minimumLayoutSize(Container target) {
		return new Dimension(120, 120);
	}

	public void layoutContainer(Container target) {
		synchronized (target.getTreeLock()) {
			Component comp[] = target.getComponents();
            int i, w = 0, h = 0, n = comp.length;
            for (i = 0; i < n; i++) {
                Component c = comp[i];
                Dimension d = c.getSize();
                c.setLocation(0, h);
                w = Math.max(w, d.width);
                h += d.height + vgap;
            }
            target.setSize(w, h);
		}
	}

}
