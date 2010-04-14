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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.sun.java.swing.plaf.windows.WindowsTextFieldUI;

/**
 * The JGoodies Windows Look&amp;Feel implementation of
 * {@link javax.swing.plaf.ComboBoxUI}.
 * Corrects the editor insets for editable combo boxes
 * as well as the render insets for non-editable combos. And it has
 * the same height as text fields - unless you change the renderer.<p>
 *
 * Also, this class offers to use the combo's popup prototype display value
 * to compute the popup menu width. This is an optional feature of
 * the JGoodies Windows L&amp;f implemented via a client property key.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 *
 * @see Options#COMBO_POPUP_PROTOTYPE_DISPLAY_VALUE_KEY
 */
public class WindowsComboBoxUI extends com.sun.java.swing.plaf.windows.WindowsComboBoxUI {

    private static final String CELL_EDITOR_KEY = "JComboBox.isTableCellEditor";

    /**
     * Used to determine the minimum height of a text field,
     * which in turn is used to answer the combobox's minimum height.
     */
    private static final JTextField PHANTOM = new JTextField("Phantom");

    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    private static final Border EMPTY_BORDER = new EmptyBorder(EMPTY_INSETS);


    private boolean tableCellEditor;
    private PropertyChangeListener propertyChangeListener;


    // ************************************************************************

    public static ComponentUI createUI(JComponent b) {
        ensurePhantomHasWindowsUI();
        return new WindowsComboBoxUI();
    }


    /**
     * Ensures that the phantom text field has a Windows text field UI.
     */
    private static void ensurePhantomHasWindowsUI() {
        if (!(PHANTOM.getUI() instanceof WindowsTextFieldUI)) {
            PHANTOM.updateUI();
        }
    }


    // ************************************************************************

    public void installUI(JComponent c) {
        super.installUI(c);
        tableCellEditor = isTableCellEditor();
    }

    protected void installListeners() {
        super.installListeners();
        propertyChangeListener = new TableCellEditorPropertyChangeHandler();
        comboBox.addPropertyChangeListener(CELL_EDITOR_KEY, propertyChangeListener);
    }

    protected void uninstallListeners() {
        super.uninstallListeners();
        comboBox.removePropertyChangeListener(CELL_EDITOR_KEY, propertyChangeListener);
        propertyChangeListener = null;
    }


    /**
     * Creates the arrow button that is to be used in the combo box.<p>
     *
     * Overridden to paint black triangles.
     */
    protected JButton createArrowButton() {
        return LookUtils.IS_LAF_WINDOWS_XP_ENABLED
                    ? super.createArrowButton()
                    : new WindowsArrowButton(SwingConstants.SOUTH);
    }


    /**
     * Creates the editor that is to be used in editable combo boxes.
     * This method only gets called if a custom editor has not already
     * been installed in the JComboBox.
     */
    protected ComboBoxEditor createEditor() {
        return new com.jgoodies.looks.windows.WindowsComboBoxEditor.UIResource(tableCellEditor);
    }


    /**
     * Creates a layout manager for managing the components which
     * make up the combo box.<p>
     *
     * Overriden to use a layout that has a fixed width arrow button.
     *
     * @return an instance of a layout manager
     */
    protected LayoutManager createLayoutManager() {
        return new WindowsComboBoxLayoutManager();
    }


    protected void configureEditor() {
        super.configureEditor();
        if (!comboBox.isEnabled()) {
            editor.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
        }
    }

    /**
     * Creates a ComboPopup that honors the optional combo popup display value
     * that is used to compute the popup menu width.
     */
    protected ComboPopup createPopup() {
        return new WindowsComboPopup(comboBox);
    }


    /**
     * Creates the default renderer that will be used in a non-editiable combo
     * box. A default renderer will used only if a renderer has not been
     * explicitly set with <code>setRenderer</code>.<p>
     *
     * This method differs from the superclass implementation in that
     * it uses an empty border with the default left and right text insets,
     * the same as used by a combo box editor.
     *
     * @return a <code>ListCellRender</code> used for the combo box
     * @see javax.swing.JComboBox#setRenderer
     */
    protected ListCellRenderer createRenderer() {
        if (tableCellEditor) {
            return super.createRenderer();
        }
        BasicComboBoxRenderer renderer = new BasicComboBoxRenderer.UIResource();
        renderer.setBorder(UIManager.getBorder("ComboBox.rendererBorder"));
        return renderer;
    }


    /**
     * The minumum size is the size of the display area plus insets plus the button.
     */
    public Dimension getMinimumSize(JComponent c) {
        if (!isMinimumSizeDirty) {
            return new Dimension(cachedMinimumSize);
        }
        Dimension size = getDisplaySize();
        Insets insets = getInsets();
        size.height += insets.top + insets.bottom;
        int buttonWidth = getEditableButtonWidth();
        size.width +=  insets.left + insets.right + buttonWidth;
        // The combo editor benefits from extra space for the caret.
        // To make editable and non-editable equally wide,
        // we always add 1 pixel.
        size.width += 1;

        // Honor corrections made in #paintCurrentValue
        ListCellRenderer renderer = comboBox.getRenderer();
        if (renderer instanceof JComponent) {
            JComponent component = (JComponent) renderer;
            Insets rendererInsets = component.getInsets();
            Insets editorInsets = UIManager.getInsets("ComboBox.editorInsets");
            int offsetLeft   = Math.max(0, editorInsets.left - rendererInsets.left);
            int offsetRight  = Math.max(0, editorInsets.right - rendererInsets.right);
            // int offsetTop    = Math.max(0, editorInsets.top - rendererInsets.top);
            // int offsetBottom = Math.max(0, editorInsets.bottom - rendererInsets.bottom);
            size.width += offsetLeft + offsetRight;
            //size.height += offsetTop + offsetBottom;
        }

        // The height is oriented on the JTextField height
        Dimension textFieldSize = PHANTOM.getMinimumSize();
        size.height = (LookUtils.IS_OS_WINDOWS_VISTA && !LookUtils.IS_LAF_WINDOWS_XP_ENABLED)
           ? textFieldSize.height
           : Math.max(textFieldSize.height, size.height);

        cachedMinimumSize.setSize(size.width, size.height);
        isMinimumSizeDirty = false;

        return new Dimension(size);
    }

    /**
     * Delegates to #getMinimumSize(Component).
     * Overridden to return the same result in JDK 1.5 as in JDK 1.4.
     */
    public Dimension getPreferredSize(JComponent c) {
        return getMinimumSize(c);
    }


    /**
     * Paints the currently selected item.
     */
    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
        ListCellRenderer renderer = comboBox.getRenderer();
        Component c;
        boolean isVistaReadOnlyCombo = isVistaXPStyleReadOnlyCombo();

        if (hasFocus && !isPopupVisible(comboBox)) {
            c = renderer.getListCellRendererComponent(listBox,
                                                      comboBox.getSelectedItem(),
                                                      -1,
                                                      true,
                                                      false);
        } else {
            c = renderer.getListCellRendererComponent(listBox,
                                                      comboBox.getSelectedItem(),
                                                      -1,
                                                      false,
                                                      false );
            c.setBackground(UIManager.getColor("ComboBox.background"));
        }
        Border oldBorder = null;
        Rectangle originalBounds = new Rectangle(bounds);
        if ((c instanceof JComponent) && !tableCellEditor) {
            JComponent component = (JComponent) c;
            if (isRendererBorderRemovable(component)) {
                oldBorder = component.getBorder();
                component.setBorder(EMPTY_BORDER); //new WindowsBorders.DashedBorder(c.getForeground(), 1));
            }
            Insets rendererInsets = component.getInsets();
            Insets editorInsets = UIManager.getInsets("ComboBox.editorInsets");
            int offsetLeft   = Math.max(0, editorInsets.left - rendererInsets.left);
            int offsetRight  = Math.max(0, editorInsets.right - rendererInsets.right);
            int offsetTop    = Math.max(0, editorInsets.top - rendererInsets.top);
            int offsetBottom = Math.max(0, editorInsets.bottom - rendererInsets.bottom);
            bounds.x += offsetLeft;
            bounds.y += offsetTop;
            bounds.width  -= offsetLeft + offsetRight - 1;
            bounds.height -= offsetTop + offsetBottom;
        }

        c.setFont(comboBox.getFont());
        if (hasFocus && !isPopupVisible(comboBox) && !isVistaReadOnlyCombo) {
            c.setForeground(listBox.getSelectionForeground());
            c.setBackground(listBox.getSelectionBackground());
        } else {
            if (comboBox.isEnabled()) {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            } else {
                c.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
                c.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
            }
        }

        // Fix for 4238829: should lay out the JPanel.
        boolean shouldValidate = c instanceof JPanel;

        Boolean oldOpaque = null;
        if (isVistaReadOnlyCombo && (c instanceof JComponent) && !(c instanceof DefaultListCellRenderer)) {
            oldOpaque = Boolean.valueOf(c.isOpaque());
            ((JComponent) c).setOpaque(false);
        }
        currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y,
                                        bounds.width, bounds.height, shouldValidate);
        if (hasFocus) {
            Color oldColor = g.getColor();
            g.setColor(comboBox.getForeground());
            if (isVistaReadOnlyCombo) {
                int width = originalBounds.width - 2;
                if ((width % 2) == 0) {
                    width += 1;
                }
                WindowsUtils.drawRoundedDashedRect(g,
                        originalBounds.x+1, originalBounds.y+1,
                        width, originalBounds.height-2);
            } /*else {
                BasicGraphicsUtils.drawDashedRect(g,
                        bounds.x, bounds.y, bounds.width, bounds.height);
            }*/
            g.setColor(oldColor);
        }
        if (oldOpaque != null) {
            ((JComponent) c).setOpaque(oldOpaque.booleanValue());
        }
        if (oldBorder != null) {
            ((JComponent) c).setBorder(oldBorder);
        }
    }

    /**
     * Checks and answer whether the border of the given renderer component
     * can be removed temporarily, so the combo's selection background will
     * be consistent with the default renderer and native appearance.
     * This test is invoked from <code>#paintCurrentValue</code>.<p>
     *
     * It is safe to remove an EmptyBorder if the component doesn't override
     * <code>#update</code>, <code>#paint</code> and <code>#paintBorder</code>.
     * Since we know the default renderer, we can remove its border.<p>
     *
     * Custom renderers may set a hint to make their border removable.
     * To do so, set the client property "isBorderRemovable"
     * to <code>Boolean.TRUE</code>. If this client property is set,
     * its value will be returned. If it is not set, <code>true</code> is returned
     * if and only if the component's border is an EmptyBorder.
     *
     * @param rendererComponent  the renderer component to check
     * @return true if the component's border can be removed, false if not
     * @see #paintCurrentValue(Graphics, Rectangle, boolean)
     */
    protected boolean isRendererBorderRemovable(JComponent rendererComponent) {
        if (rendererComponent instanceof BasicComboBoxRenderer.UIResource)
            return true;
        Object hint = rendererComponent.getClientProperty(Options.COMBO_RENDERER_IS_BORDER_REMOVABLE);
        if (hint != null)
            return Boolean.TRUE.equals(hint);
        Border border = rendererComponent.getBorder();
        return border instanceof EmptyBorder;
    }


    private boolean isVistaXPStyleReadOnlyCombo() {
        return     LookUtils.IS_OS_WINDOWS_VISTA
                && LookUtils.IS_LAF_WINDOWS_XP_ENABLED
                && !comboBox.isEditable();
    }


    /**
     * Returns the area that is reserved for drawing the currently selected item.
     */
    protected Rectangle rectangleForCurrentValue() {
        int width  = comboBox.getWidth();
        int height = comboBox.getHeight();
        Insets insets = getInsets();
        int buttonWidth = getEditableButtonWidth();
        if (arrowButton != null) {
            buttonWidth = arrowButton.getWidth();
        }
        if (comboBox.getComponentOrientation().isLeftToRight()) {
            return new Rectangle(
                    insets.left,
                    insets.top,
                    width  - (insets.left + insets.right + buttonWidth),
                    height - (insets.top  + insets.bottom));
        } else {
            return new Rectangle(
                    insets.left + buttonWidth,
                    insets.top ,
                    width  - (insets.left + insets.right + buttonWidth),
                    height - (insets.top  + insets.bottom));
        }
    }


    // Helper Code ************************************************************

    /**
     * Computes and returns the width of the arrow button in editable state.
     *
     * @return the width of the arrow button in editable state
     */
    private int getEditableButtonWidth() {
        return UIManager.getInt("ScrollBar.width");
    }

    /**
     * Checks and answers if this UI's combo has a client property
     * that indicates that the combo is used as a table cell editor.
     *
     * @return <code>true</code> if the table cell editor client property
     *    is set to <code>Boolean.TRUE</code>, <code>false</code> otherwise
     */
    private boolean isTableCellEditor() {
        return Boolean.TRUE.equals(comboBox.getClientProperty(CELL_EDITOR_KEY));
    }


    // Collaborator Classes ***************************************************

    /**
     * This layout manager handles the 'standard' layout of combo boxes.
     * It puts the arrow button to the right and the editor to the left.
     * If there is no editor it still keeps the arrow button to the right.
     *
     * Overriden to use a fixed arrow button width.
     */
    private final class WindowsComboBoxLayoutManager
        extends BasicComboBoxUI.ComboBoxLayoutManager {

        public void layoutContainer(Container parent) {
            JComboBox cb = (JComboBox) parent;

            int width  = cb.getWidth();
            int height = cb.getHeight();

            Insets insets = getInsets();
            int buttonWidth  = getEditableButtonWidth();
            int buttonHeight = height - (insets.top + insets.bottom);

            if (arrowButton != null) {
                if (cb.getComponentOrientation().isLeftToRight()) {
                    arrowButton.setBounds(
                        width - (insets.right + buttonWidth),
                        insets.top,
                        buttonWidth,
                        buttonHeight);
                } else {
                    arrowButton.setBounds(
                        insets.left,
                        insets.top,
                        buttonWidth,
                        buttonHeight);
                }
            }
            if (editor != null) {
                editor.setBounds(rectangleForCurrentValue());
            }
        }

   }


    /**
     * Differs from the BasicComboPopup in that it uses the standard
     * popmenu border and honors an optional popup prototype display value.
     */
    private static final class WindowsComboPopup extends BasicComboPopup {

        private WindowsComboPopup(JComboBox combo) {
            super(combo);
        }

        /**
         * Calculates the placement and size of the popup portion
         * of the combo box based on the combo box location and
         * the enclosing screen bounds. If no transformations are required,
         * then the returned rectangle will have the same values
         * as the parameters.<p>
         *
         * In addition to the superclass behavior, this class offers
         * to use the combo's popup prototype display value to compute
         * the popup menu width. This is an optional feature of
         * the JGoodies Windows L&amp;f implemented via a client property key.<p>
         *
         * If a prototype is set, the popup width is the maximum of the
         * combobox width and the prototype based popup width.
         * For the latter the renderer is used to render the prototype.
         * The prototype based popup width is the prototype's width
         * plus the scrollbar width - if any. The scrollbar test checks
         * if there are more items than the combo's maximum row count.
         *
         * @param px starting x location
         * @param py starting y location
         * @param pw starting width
         * @param ph starting height
         * @return a rectangle which represents the placement and size of the popup
         *
         * @see Options#COMBO_POPUP_PROTOTYPE_DISPLAY_VALUE_KEY
         * @see JComboBox#getMaximumRowCount()
         */
        protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
            Rectangle defaultBounds = super.computePopupBounds(px, py, pw, ph);
            Object popupPrototypeDisplayValue = comboBox.getClientProperty(
                    Options.COMBO_POPUP_PROTOTYPE_DISPLAY_VALUE_KEY);
            if (popupPrototypeDisplayValue == null) {
                return defaultBounds;
            }

            ListCellRenderer renderer = list.getCellRenderer();
            Component c = renderer.getListCellRendererComponent(
                    list, popupPrototypeDisplayValue, -1, true, true);
            pw = c.getPreferredSize().width;
            boolean hasVerticalScrollBar =
                comboBox.getItemCount() > comboBox.getMaximumRowCount();
            if (hasVerticalScrollBar) {
                // Add the scrollbar width.
                JScrollBar verticalBar = scroller.getVerticalScrollBar();
                pw += verticalBar.getPreferredSize().width;
            }
            Rectangle prototypeBasedBounds = super.computePopupBounds(px, py, pw, ph);
            return prototypeBasedBounds.width > defaultBounds.width
                ? prototypeBasedBounds
                : defaultBounds;
        }

    }


    // Handling Combo Changes *************************************************

    /**
     * Listens to changes in the table cell editor client property
     * and updates the default editor - if any - to use the correct
     * insets for this case.
     */
    private final class TableCellEditorPropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            tableCellEditor = isTableCellEditor();
            if (comboBox.getRenderer() == null || comboBox.getRenderer() instanceof UIResource) {
                comboBox.setRenderer(createRenderer());
            }
            if (comboBox.getEditor() == null || comboBox.getEditor() instanceof UIResource) {
                comboBox.setEditor(createEditor());
            }
        }
    }

}
