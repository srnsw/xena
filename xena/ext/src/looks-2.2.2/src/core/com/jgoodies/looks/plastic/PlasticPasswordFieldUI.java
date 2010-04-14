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


import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.text.Caret;
import javax.swing.text.Element;
import javax.swing.text.View;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.common.ExtPasswordView;


/**
 * The JGoodies PlasticXP Look&amp;Feel implementation of a password field UI
 * delegate. It differs from its superclass in that it utilizes a password
 * view that renders the UIManager's echo char, not a star (&quot;*&quot;).
 * In addition it uses a custom caret if enabled by the L&amp;f.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public final class PlasticPasswordFieldUI extends BasicPasswordFieldUI {

    /**
	 * Creates a UI for a {@link JPasswordField}.
	 *
	 * @param c the password field component
	 * @return the UI
	 */
    public static ComponentUI createUI(JComponent c) {
        return new PlasticPasswordFieldUI();
    }

    /**
	 * Creates and returns a view (an <code>ExtPasswordView</code>) for an element.
	 *
	 * @param elem the element
	 * @return the view
	 */
    public View create(Element elem) {
        return (LookUtils.IS_JAVA_1_4_OR_5)
            ? new ExtPasswordView(elem)
            : super.create(elem);
    }


    /**
     * Creates the caret for a field.
     *
     * @return the caret
     */
    protected Caret createCaret() {
        return (PlasticLookAndFeel.isSelectTextOnKeyboardFocusGained())
            ? new PlasticFieldCaret()
            : super.createCaret();
    }


}