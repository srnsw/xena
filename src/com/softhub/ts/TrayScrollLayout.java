
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

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Dimension;

public class TrayScrollLayout extends ScrollPaneLayout
    implements TrayConstants
{
    protected Component controlBar;

    public void addLayoutComponent(String s, Component comp) {
        if (s.equals(CONTROL_BAR)) {
            controlBar = addSingletonComponent(controlBar, comp);
        } else {
            super.addLayoutComponent(s, comp);
        }
    }

    public void removeLayoutComponent(Component comp) {
        if (comp == controlBar) {
            controlBar = null;
        } else {
            super.removeLayoutComponent(comp);
        }
    }

    public Component getControlBar() {
        return controlBar;
    }

    public void layoutContainer(Container parent) {
        super.layoutContainer(parent);
        if (controlBar != null) {
            if (hsb != null && hsb.isVisible()) {
                Rectangle r = hsb.getBounds();
                // height of control bar controlled by its preferred size
                Dimension controlSize = controlBar.getPreferredSize();
                int w2 = r.width / 2;
                int h = controlSize.height;
                int yc = r.y - (h - r.height);
                Rectangle leftR = new Rectangle(r.x, yc, w2, h);
                Rectangle rightR = new Rectangle(r.x + w2, r.y, w2, r.height);
                controlBar.setBounds(leftR);
                hsb.setBounds(rightR);
            } else if (viewport != null) {
                Rectangle r = viewport.getBounds();
                Dimension d = controlBar.getPreferredSize();
                // doesn't seem to have effect anymore... was it necessary in old jre?
                viewport.setBounds(r.x, r.y, r.width, r.height - d.height);
                controlBar.setBounds(r.x, r.y + r.height - d.height, r.width, d.height);
            }
        }
    }

}
