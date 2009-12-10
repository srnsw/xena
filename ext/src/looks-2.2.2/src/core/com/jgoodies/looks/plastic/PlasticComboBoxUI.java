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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.plaf.metal.MetalTextFieldUI;

import com.jgoodies.looks.Options;


/**
 * The JGoodies Plastic Look and Feel implementation of <code>ComboBoxUI</code>.
 * Has the same height as text fields - unless you change the renderer.<p>
 *
 * Also, this class offers to use the combo's popup prototype display value
 * to compute the popup menu width. This is an optional feature of
 * the JGoodies Plastic L&amp;fs implemented via a client property key.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 *
 * @see Options#COMBO_POPUP_PROTOTYPE_DISPLAY_VALUE_KEY
 */
public class PlasticComboBoxUI extends MetalComboBoxUI {

    static final String CELL_EDITOR_KEY = "JComboBox.isTableCellEditor";

    /**
     * Used to determine the minimum height of a text field,
     * which in turn is used to answer the combobox's minimum height.
     */
    private static final JTextField PHANTOM = new JTextField("Phantom");

    /**
     * Different Plastic L&amp;fs may need different phantom UIs.
     * Therefore we store the LookAndFeel class and update the
     * phantom UI whenever the Look&amp;Feel changes.
     */
    private static Class phantomLafClass;


    private boolean tableCellEditor;
    private PropertyChangeListener propertyChangeListener;


    // ************************************************************************

    public static ComponentUI createUI(JComponent b) {
        ensurePhantomHasPlasticUI();
        return new PlasticComboBoxUI();
    }


    /**
     * Ensures that the phantom text field has a Plastic text field UI.
     */
    private static void ensurePhantomHasPlasticUI() {
        TextUI ui = PHANTOM.getUI();
        Class lafClass = UIManager.getLookAndFeel().getClass();
        if (   (phantomLafClass != lafClass)
            || !(ui instanceof MetalTextFieldUI)) {
            phantomLafClass = lafClass;
            PHANTOM.updateUI();
        }
    }


    // ************************************************************************

    public void installUI( JComponent c ) {
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


    // Overridden Superclass Configuration ************************************

    /**
     * Creates and answers the arrow button that is to be used in the combo box.<p>
     *
     * Overridden to use a button that can have a pseudo 3D effect.
     */
    protected JButton createArrowButton() {
        return new PlasticComboBoxButton(
            comboBox,
            PlasticIconFactory.getComboBoxButtonIcon(),
            comboBox.isEditable(),
            currentValuePane,
            listBox);
    }

    /**
     * Creates the editor that is to be used in editable combo boxes.
     * This method only gets called if a custom editor has not already
     * been installed in the JComboBox.
     */
    protected ComboBoxEditor createEditor() {
        return new PlasticComboBoxEditor.UIResource(tableCellEditor);
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
        return new PlasticComboBoxLayoutManager();
    }


    protected ComboPopup createPopup() {
        return new PlasticComboPopup(comboBox);
    }


    /**
     * Creates the default renderer that will be used in a non-editiable combo
     * box. A default renderer will used only if a renderer has not been
     * explicitly set with <code>setRenderer</code>.<p>
     *
     * This method differs from the superclass implementation
     * in that it uses an empty border with wider left and right margins
     * of 2 pixels instead of 1.
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
        if (comboBox.isEditable()) {
            Insets editorBorderInsets = UIManager.getInsets("ComboBox.editorBorderInsets");
            size.width += editorBorderInsets.left + editorBorderInsets.right;
            //size.height += editorBorderInsets.top + editorBorderInsets.bottom;
            // The combo editor benefits from extra space for the caret.
            // To make editable and non-editable equally wide,
            // we always add 1 pixel.
            size.width += 1;
        } else if (arrowButton != null) {
            Insets arrowButtonInsets = arrowButton.getInsets();
            size.width += arrowButtonInsets.left;
        }
        int buttonWidth = getEditableButtonWidth();
        size.width +=  insets.left + insets.right + buttonWidth;

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
        size.height = Math.max(textFieldSize.height, size.height);

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


    // Painting ***************************************************************

    public void update(Graphics g, JComponent c) {
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            if (isToolBarComboBox(c)) {
                c.setOpaque(false);
            }        }
        paint(g, c);
    }


    /**
     * Checks and answers if this combo is in a tool bar.
     *
     * @param c   the component to check
     * @return true if in tool bar, false otherwise
     */
    protected boolean isToolBarComboBox(JComponent c) {
        Container parent = c.getParent();
        return parent != null
            && (parent instanceof JToolBar
                || parent.getParent() instanceof JToolBar);
    }


    // Helper Code ************************************************************

    /**
     * Computes and returns the width of the arrow button in editable state.
     * The perceived width shall be equal to the width of a scroll bar.
     * Therefore we subtract a pixel that is perceived as part of the
     * arrow button but that is painted by the editor's border.
     *
     * @return the width of the arrow button in editable state
     */
    static int getEditableButtonWidth() {
        return UIManager.getInt("ScrollBar.width") - 1;
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


    // Helper Classes *********************************************************

    /**
     * This layout manager handles the 'standard' layout of combo boxes.
     * It puts the arrow button to the right and the editor to the left.
     * If there is no editor it still keeps the arrow button to the right.
     *
     * Overriden to use a fixed arrow button width.
     */
    private final class PlasticComboBoxLayoutManager
        extends MetalComboBoxUI.MetalComboBoxLayoutManager {

        public void layoutContainer(Container parent) {
            JComboBox cb = (JComboBox) parent;

            // Use superclass behavior if the combobox is not editable.
            if (!cb.isEditable()) {
                super.layoutContainer(parent);
                return;
            }

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

    // Required if we have a combobox button that does not extend MetalComboBoxButton
    public PropertyChangeListener createPropertyChangeListener() {
        return new PlasticPropertyChangeListener();
    }

    /**
     * Overriden to use PlasticComboBoxButton instead of a MetalComboBoxButton.
     * Required if we have a combobox button that does not extend MetalComboBoxButton
     */
    private final class PlasticPropertyChangeListener
        extends BasicComboBoxUI.PropertyChangeHandler {

        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            String propertyName = e.getPropertyName();

            if (propertyName.equals("editable")) {
                PlasticComboBoxButton button =
                    (PlasticComboBoxButton) arrowButton;
                button.setIconOnly(comboBox.isEditable());
                comboBox.repaint();
            } else if (propertyName.equals("background")) {
                Color color = (Color) e.getNewValue();
                arrowButton.setBackground(color);
                listBox.setBackground(color);

            } else if (propertyName.equals("foreground")) {
                Color color = (Color) e.getNewValue();
                arrowButton.setForeground(color);
                listBox.setForeground(color);
            }
        }
    }

    /**
     * Differs from the BasicComboPopup in that it uses the standard
     * popmenu border and honors an optional popup prototype display value.
     */
    private static final class PlasticComboPopup extends BasicComboPopup {

        private PlasticComboPopup(JComboBox combo) {
            super(combo);
        }

        /**
         * Configures the list created by #createList().
         */
        protected void configureList() {
            super.configureList();
            list.setForeground(UIManager.getColor("MenuItem.foreground"));
            list.setBackground(UIManager.getColor("MenuItem.background"));
        }

        /**
         * Configures the JScrollPane created by #createScroller().
         */
        protected void configureScroller() {
            super.configureScroller();
            scroller.getVerticalScrollBar().putClientProperty(
                MetalScrollBarUI.FREE_STANDING_PROP,
                Boolean.FALSE);
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
         * the popup menu width. This is an optional feature of the
         * JGoodies Plastic L&amp;fs implemented via a client property key.<p>
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