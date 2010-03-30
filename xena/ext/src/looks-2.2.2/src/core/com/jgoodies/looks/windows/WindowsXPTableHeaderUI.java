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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.sun.java.swing.plaf.windows.WindowsTableHeaderUI;


/**
 * The JGoodies Windows L&amp;F implementation of <code>TableHeaderUI</code>.
 * A Windows table header that honors the XP header style even if the user
 * uses custom non-opaque renderers. The renderers should be a subclass of
 * <code>JComponent</code> because we need to replace the border by the one
 * specified in the XP style.
 *
 * @author Andrej Golovnin
 * @version $Revision$
 */
public final class WindowsXPTableHeaderUI extends WindowsTableHeaderUI {

    private TableCellRenderer xpRenderer;

    public static ComponentUI createUI(JComponent h) {
        return new WindowsXPTableHeaderUI();
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        xpRenderer = header.getDefaultRenderer();
    }

    public void uninstallUI(JComponent c) {
        xpRenderer = null;
        super.uninstallUI(c);
    }

    public void paint(Graphics g, JComponent c) {
        TableColumnModel cm = header.getColumnModel();
        if (cm.getColumnCount() <= 0) {
            return;
        }
        boolean ltr = header.getComponentOrientation().isLeftToRight();

        Rectangle clip = g.getClipBounds();
        Point left = clip.getLocation();
        Point right = new Point(clip.x + clip.width - 1, clip.y);
        int cMin = header.columnAtPoint(ltr ? left : right);
        int cMax = header.columnAtPoint(ltr ? right : left);
        // This should never happen.
        if (cMin == -1) {
            cMin = 0;
        }
        // If the table does not have enough columns to fill the view we'll get
        // -1.
        // Replace this with the index of the last column.
        if (cMax == -1) {
            cMax = cm.getColumnCount() - 1;
        }

        TableColumn draggedColumn = header.getDraggedColumn();
        int columnWidth;
        Rectangle cellRect = header.getHeaderRect(cMin);
        TableColumn aColumn;
        if (ltr) {
            for (int column = cMin; column <= cMax; column++) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
            }
        } else {
            for (int column = cMax; column >= cMin; column--) {
                aColumn = cm.getColumn(column);
                columnWidth = aColumn.getWidth();
                cellRect.width = columnWidth;
                if (aColumn != draggedColumn) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += columnWidth;
            }
        }

        // Paint the dragged column if we are dragging.
        if (draggedColumn != null) {
            int draggedColumnIndex = viewIndexForColumn(draggedColumn);
            Rectangle draggedCellRect = header
                    .getHeaderRect(draggedColumnIndex);

            // Draw a gray well in place of the moving column.
            g.setColor(header.getParent().getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                    draggedCellRect.width, draggedCellRect.height);

            draggedCellRect.x += header.getDraggedDistance();

            // Fill the background.
            g.setColor(header.getBackground());
            g.fillRect(draggedCellRect.x, draggedCellRect.y,
                    draggedCellRect.width, draggedCellRect.height);

            paintCell(g, draggedCellRect, draggedColumnIndex);
        }

        // Remove all components in the rendererPane.
        rendererPane.removeAll();
    }

    private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
        TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
        TableCellRenderer renderer = aColumn.getHeaderRenderer();
        if (renderer == null) {
            renderer = header.getDefaultRenderer();
        }

        JTable table = header.getTable();
        Component background = xpRenderer.getTableCellRendererComponent(table,
                null, false, false, -1, columnIndex);
        Component c = renderer.getTableCellRendererComponent(table,
                aColumn.getHeaderValue(), false, false, -1, columnIndex);

        if (c != background) {
            // The DefaultTableCellRenderer is used in the most cases as
            // the base class for all header renderers. And due to
            // the optimizations in its #isOpaque method, we have to add
            // the component to the renderer pane to determine its
            // non-opaqueness.
            rendererPane.add(c);
            if (!c.isOpaque()) {
                rendererPane.paintComponent(g, background, header, cellRect.x,
                        cellRect.y, cellRect.width, cellRect.height, true);

                // All custom header renderers will use TableHeader.cellBorder
                // returned by UIManager#getBorder. But this one does not
                // comply with the Windows XP style. It is the one used by
                // Windows Classis L&F. So replace the border of the custom
                // renderers component by the one which comply with XP style.
                if ((c instanceof JComponent)
                        && (background instanceof JComponent)) {
                    ((JComponent) c).setBorder(
                            ((JComponent) background).getBorder());
                }
            }
        }

        rendererPane.paintComponent(g, c, header, cellRect.x, cellRect.y,
                cellRect.width, cellRect.height, true);
    }

    private int viewIndexForColumn(TableColumn aColumn) {
        TableColumnModel cm = header.getColumnModel();
        for (int column = cm.getColumnCount() - 1; column >= 0; column--) {
            if (cm.getColumn(column) == aColumn) {
                return column;
            }
        }
        return -1;
    }

}
