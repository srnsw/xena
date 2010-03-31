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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicPopupMenuUI;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.common.PopupMenuLayout;

/**
 * The JGoodies Plastic look&amp;feel implementation of <code>PopMenuUI</code>.
 * It differs from the superclass in that it provides an option to get a
 * narrow border. You can set a client property
 * {@link Options#NO_MARGIN_KEY} to indicate that this popup menu
 * has a border without margin. That is useful in the special case
 * where the popup contains only a single component, for example
 * a JScrollPane.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see	com.jgoodies.looks.Options#NO_MARGIN_KEY
 */
public final class PlasticPopupMenuUI extends BasicPopupMenuUI {

    private PropertyChangeListener borderListener;

	public static ComponentUI createUI(JComponent b) {
		return new PlasticPopupMenuUI();
	}


    public void installDefaults() {
        super.installDefaults();
        installBorder();
        if (   (popupMenu.getLayout() == null)
            || (popupMenu.getLayout() instanceof UIResource)) {
            popupMenu.setLayout(new PopupMenuLayout(popupMenu, BoxLayout.Y_AXIS));
        }
    }

    protected void installListeners() {
        super.installListeners();
        borderListener = new BorderStyleChangeHandler();
        popupMenu.addPropertyChangeListener(Options.NO_MARGIN_KEY, borderListener);
    }

    protected void uninstallListeners() {
        popupMenu.removePropertyChangeListener(Options.NO_MARGIN_KEY, borderListener);
        super.uninstallListeners();
    }

    // Narrow Border **********************************************************

    private final class BorderStyleChangeHandler implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent e) {
            installBorder();
        }

    }

    /**
     * Installs a border without margin, iff the client property
     * <code>Options.NO_MARGIN_KEY</code> is set to <code>Boolean.TRUE</code>.
     */
    private void installBorder() {
        boolean useNarrowBorder = Boolean.TRUE.equals(
                popupMenu.getClientProperty(Options.NO_MARGIN_KEY));
        String suffix = useNarrowBorder ? "noMarginBorder" : "border";
        LookAndFeel.installBorder(popupMenu, "PopupMenu." + suffix);
    }


}