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
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.*;

import com.jgoodies.looks.Options;

/**
 * WindowsFieldCaret has different scrolling behavior than the DefaultCaret.
 * Also, this caret is visible in non-editable fields,
 * and the text is selected after a keyboard focus gained event.
 * For the latter see also issue #4337647 in Sun's bug database.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 *
 */
final class WindowsFieldCaret extends DefaultCaret implements UIResource {

    private static final LayeredHighlighter.LayerPainter WindowsPainter =
        new WindowsHighlightPainter(null);


    WindowsFieldCaret() {
        super();
    }


    // Begin of Added Code ----------------------------------------------

    private boolean isKeyboardFocusEvent = true;


    public void focusGained(FocusEvent e) {
        final JTextComponent c = getComponent();
        if (c.isEnabled()) {
            setVisible(true);
            setSelectionVisible(true);
        }
        if (   !c.isEnabled()
            || !isKeyboardFocusEvent
            || !Options.isSelectOnFocusGainActive(c)) {
            return;
        }
        if (c instanceof JFormattedTextField) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    selectAll();
                }
            });
        } else {
            selectAll();
        }
    }


    private void selectAll() {
        final JTextComponent c = getComponent();
        boolean backward = Boolean.TRUE.equals(c.getClientProperty(Options.INVERT_SELECTION_CLIENT_KEY));
        if (backward) {
            setDot(c.getDocument().getLength());
            moveDot(0);
        } else {
            setDot(0);
            moveDot(c.getDocument().getLength());
        }
    }


    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        if (!e.isTemporary()) {
            isKeyboardFocusEvent = true;
            if (  (getComponent() != null)
                && Boolean.TRUE.equals(getComponent().getClientProperty(Options.SET_CARET_TO_START_ON_FOCUS_LOST_CLIENT_KEY))) {
                setDot(0);
            }
        }
    }


    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
            isKeyboardFocusEvent = false;
        }
        super.mousePressed(e);

    }


    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        // super.mousePressed() does not transfer focus on popup clicks.
        // Windows does.
        if (e.isPopupTrigger()) {
            isKeyboardFocusEvent = false;
            if (  (getComponent() != null)
                && getComponent().isEnabled()
                && getComponent().isRequestFocusEnabled()) {
                getComponent().requestFocus();
            }
        }
    }

    // End of Added Code ------------------------------------------------

    /**
     * Adjusts the visibility of the caret according to
     * the windows feel which seems to be to move the
     * caret out into the field by about a quarter of
     * a field length if not visible.
     */
    protected void adjustVisibility(Rectangle r) {
        SwingUtilities.invokeLater(new SafeScroller(r));
    }


    /**
     * Gets the painter for the Highlighter.
     *
     * @return the painter
     */
    protected Highlighter.HighlightPainter getSelectionPainter() {
        return WindowsPainter;
    }


    private final class SafeScroller implements Runnable {

        SafeScroller(Rectangle r) {
            this.r = r;
        }


        public void run() {
            JTextField field = (JTextField) getComponent();
            if (field == null) {
                return;
            }
            TextUI ui = field.getUI();
            int dot = getDot();
            // PENDING: We need to expose the bias in DefaultCaret.
            Position.Bias bias = Position.Bias.Forward;
            Rectangle startRect = null;
            try {
                startRect = ui.modelToView(field, dot, bias);
            } catch (BadLocationException ble) {}

            Insets i = field.getInsets();
            BoundedRangeModel vis = field.getHorizontalVisibility();
            int x = r.x + vis.getValue() - i.left;
            int quarterSpan = vis.getExtent() / 4;
            if (r.x < i.left) {
                vis.setValue(x - quarterSpan);
            } else if (r.x + r.width > (i.left + vis.getExtent()+1)) {
                vis.setValue(x - (3 * quarterSpan));
            }
            // If we scroll, our visual location will have changed,
            // but we won't have updated our internal location as
            // the model hasn't changed. This checks for the change,
            // and if necessary, resets the internal location.
            if (startRect == null) {
                return;
            }
            try {
                Rectangle endRect;
                endRect = ui.modelToView(field, dot, bias);
                if (endRect != null && !endRect.equals(startRect)){
                    damage(endRect);
                }
            } catch (BadLocationException ble) {}
        }

        private final Rectangle r;
    }

    // Helper Classes *********************************************************

    private static final class WindowsHighlightPainter extends
            DefaultHighlighter.DefaultHighlightPainter {
        WindowsHighlightPainter(Color c) {
            super(c);
        }


        // --- HighlightPainter methods ---------------------------------------

        /**
         * Paints a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box for the highlight
         * @param c the editor
         */
        public void paint(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c) {
            Rectangle alloc = bounds.getBounds();
            try {
                // --- determine locations ---
                TextUI mapper = c.getUI();
                Rectangle p0 = mapper.modelToView(c, offs0);
                Rectangle p1 = mapper.modelToView(c, offs1);

                // --- render ---
                Color color = getColor();

                if (color == null) {
                    g.setColor(c.getSelectionColor());
                } else {
                    g.setColor(color);
                }
                boolean firstIsDot = false;
                boolean secondIsDot = false;
                if (c.isEditable()) {
                    int dot = c.getCaretPosition();
                    firstIsDot = (offs0 == dot);
                    secondIsDot = (offs1 == dot);
                }
                if (p0.y == p1.y) {
                    // same line, render a rectangle
                    Rectangle r = p0.union(p1);
                    if (r.width > 0) {
                        if (firstIsDot) {
                            r.x++;
                            r.width--;
                        } else if (secondIsDot) {
                            r.width--;
                        }
                    }
                    g.fillRect(r.x, r.y, r.width, r.height);
                } else {
                    // different lines
                    int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                    if (firstIsDot && p0ToMarginWidth > 0) {
                        p0.x++;
                        p0ToMarginWidth--;
                    }
                    g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                    if ((p0.y + p0.height) != p1.y) {
                        g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y
                                - (p0.y + p0.height));
                    }
                    if (secondIsDot && p1.x > alloc.x) {
                        p1.x--;
                    }
                    g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
                }
            } catch (BadLocationException e) {
                // can't render
            }
        }


        // --- LayerPainter methods ----------------------------
        /**
         * Paints a portion of a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset >= 0
         * @param offs1 the ending model offset >= offs1
         * @param bounds the bounding box of the view, which is not
         *        necessarily the region to paint.
         * @param c the editor
         * @param view View painting for
         * @return region drawing occured in
         */
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c, View view) {
            Color color = getColor();

            if (color == null) {
                g.setColor(c.getSelectionColor());
            } else {
                g.setColor(color);
            }
            boolean firstIsDot = false;
            boolean secondIsDot = false;
            if (c.isEditable()) {
                int dot = c.getCaretPosition();
                firstIsDot = (offs0 == dot);
                secondIsDot = (offs1 == dot);
            }
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                // Contained in view, can just use bounds.
                Rectangle alloc;
                if (bounds instanceof Rectangle) {
                    alloc = (Rectangle) bounds;
                } else {
                    alloc = bounds.getBounds();
                }
                if (firstIsDot && alloc.width > 0) {
                    g.fillRect(alloc.x + 1, alloc.y, alloc.width - 1,
                            alloc.height);
                } else if (secondIsDot && alloc.width > 0) {
                    g.fillRect(alloc.x, alloc.y, alloc.width - 1, alloc.height);
                } else {
                    g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
                }
                return alloc;
            } else {
                // Should only render part of View.
                try {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0,
                            Position.Bias.Forward, offs1,
                            Position.Bias.Backward, bounds);
                    Rectangle r = (shape instanceof Rectangle)
                            ? (Rectangle) shape
                            : shape.getBounds();
                    if (firstIsDot && r.width > 0) {
                        g.fillRect(r.x + 1, r.y, r.width - 1, r.height);
                    } else if (secondIsDot && r.width > 0) {
                        g.fillRect(r.x, r.y, r.width - 1, r.height);
                    } else {
                        g.fillRect(r.x, r.y, r.width, r.height);
                    }
                    return r;
                } catch (BadLocationException e) {
                    // can't render
                }
            }
            // Only if exception
            return null;
        }

    }

}
