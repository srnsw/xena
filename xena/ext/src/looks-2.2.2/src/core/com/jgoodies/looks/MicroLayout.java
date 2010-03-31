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

package com.jgoodies.looks;

import java.awt.Insets;

import javax.swing.plaf.InsetsUIResource;

/**
 * Describes the insets and margins used by a Look&amp;Feel or theme.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @since 2.1
 */
public final class MicroLayout {

    private final InsetsUIResource textInsets;
    private final InsetsUIResource wrappedTextInsets;
    private final InsetsUIResource comboBoxEditorInsets;
    private final Insets           buttonBorderInsets;
    private final InsetsUIResource buttonMargin;
    private final InsetsUIResource commitButtonMargin;
    private final int comboBorderSize;
    private final int comboPopupBorderSize;
    private final InsetsUIResource checkBoxMargin;
    private final InsetsUIResource menuItemMargin;
    private final InsetsUIResource menuMargin;
    private final InsetsUIResource popupMenuSeparatorMargin;


    // Instance Creation ******************************************************

    public MicroLayout(
            InsetsUIResource textInsets,
            InsetsUIResource wrappedTextInsets,
            InsetsUIResource comboBoxEditorInsets,
            int comboBorderSize,
            int comboPopupBorderSize,
            Insets           buttonBorderInsets,
            InsetsUIResource buttonMargin,
            InsetsUIResource commitButtonMargin,
            InsetsUIResource checkBoxMargin,
            InsetsUIResource menuItemMargin,
            InsetsUIResource menuMargin,
            InsetsUIResource popupMenuSeparatorMargin) {
        this.textInsets = textInsets;
        this.wrappedTextInsets = wrappedTextInsets;
        this.comboBoxEditorInsets = comboBoxEditorInsets;
        this.buttonBorderInsets = buttonBorderInsets;
        this.buttonMargin = buttonMargin;
        this.commitButtonMargin = commitButtonMargin;
        this.comboBorderSize = comboBorderSize;
        this.comboPopupBorderSize = comboPopupBorderSize;
        this.checkBoxMargin = checkBoxMargin;
        this.menuItemMargin = menuItemMargin;
        this.menuMargin = menuMargin;
        this.popupMenuSeparatorMargin = popupMenuSeparatorMargin;
    }


    // Getters ****************************************************************

    /**
     * Returns the insets used for button borders.
     *
     * @return the insets used for button borders.
     */
    public Insets getButtonBorderInsets() {
        return buttonBorderInsets;
    }


    /**
     * Returns the margin used for standard buttons. These insets describe
     * buttons that are arranged with other components in a row of a form.
     * The standard button <em>height</em> will often be the same for
     * text fields, combo boxes, and other components that are arranged in
     * a row.<p>
     *
     * Toolbar buttons may have a different height, as well as
     * commit buttons that are placed in a special command bar area,
     * for example OK, Cancel, Apply.
     *
     * @return the margin for standard buttons.
     *
     * @see #getCommitButtonMargin()
     */
    public InsetsUIResource getButtonMargin() {
        return buttonMargin;
    }

    /**
     * Returns the margin used for commit buttons in command areas.
     * Such command areas are often at the bottom or side of a dialog or pane;
     * frequently used labels are OK, Cancel, Apply, Yes, No, Retry.
     * The <em>height</em> of a commit button may differ from the height
     * used for buttons that are arranged in a row with other components
     * in a form.
     *
     * @return the margin for commit buttons in command areas.
     *
     * @see #getButtonMargin()
     */
    public InsetsUIResource getCommitButtonMargin() {
        return commitButtonMargin;
    }

    public int getComboBorderSize() {
        return comboBorderSize;
    }

    public int getComboPopupBorderSize() {
        return comboPopupBorderSize;
    }

    public InsetsUIResource getComboBoxEditorInsets() {
        return comboBoxEditorInsets;
    }

    public InsetsUIResource getCheckBoxMargin() {
        return checkBoxMargin;
    }


    public InsetsUIResource getMenuItemMargin() {
        return menuItemMargin;
    }

    public InsetsUIResource getMenuMargin() {
        return menuMargin;
    }

    public InsetsUIResource getPopupMenuSeparatorMargin() {
        return popupMenuSeparatorMargin;
    }


    public InsetsUIResource getTextInsets() {
        return textInsets;
    }


    public InsetsUIResource getWrappedTextInsets() {
        return wrappedTextInsets;
    }

}
