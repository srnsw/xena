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

package com.jgoodies.looks.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.uif_lite.component.UIFSplitPane;

/**
 * Contains nested split panels and demonstrates how ClearLook
 * removes obsolete decorations.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 *
 * @see UIFSplitPane
 */
final class SplitTab {


    /**
     * Builds and returns the panel.
     */
    JComponent build() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(Borders.DIALOG_BORDER);
        panel.add(buildHorizontalSplit());
        return panel;
    }


    /**
     * Builds and returns the horizontal split using stripped split panes.<p>
     *
     * Nesting split panes often leads to duplicate borders.
     * However, a look&feel should not remove borders completely
     * - unless he has good knowledge about the context: the surrounding
     * components in the component tree and the border states.
     */
    private JComponent buildHorizontalSplit() {
        JComponent left = new JScrollPane(buildTree());
        left.setPreferredSize(new Dimension(200, 100));

        JComponent upperRight = new JScrollPane(buildTextArea());
        upperRight.setPreferredSize(new Dimension(100, 100));

        JComponent lowerRight = new JScrollPane(buildTable());
        lowerRight.setPreferredSize(new Dimension(100, 100));

        JSplitPane verticalSplit = UIFSplitPane.createStrippedSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    upperRight,
                    lowerRight);
        verticalSplit.setOpaque(false);
        JSplitPane horizontalSplit = UIFSplitPane.createStrippedSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            left,
            verticalSplit);
        horizontalSplit.setOpaque(false);
        return horizontalSplit;
    }


    /**
     * Builds and returns a sample tree.
     */
    private JTree buildTree() {
        JTree tree = new JTree(createSampleTreeModel());
        tree.expandRow(3);
        tree.expandRow(2);
        tree.expandRow(1);
        return tree;
    }


    /**
     * Builds and returns a sample text area.
     */
    private JTextArea buildTextArea() {
        JTextArea area = new JTextArea();
        area.setText(
            "May\nI\nKindly\nRemind you that a\nMargin\nImproves a text's readability.");
        return area;
    }


    /**
     * Builds and returns a sample table.
     */
    private JTable buildTable() {
        TableModel model = new SampleTableModel(
                createSampleTableData(),
                new String[] { "Artist", "Title      ", "Free" });
        JTable table = new JTable(model);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.setRowSelectionInterval(2, 2);
        int tableFontSize    = table.getFont().getSize();
        int minimumRowHeight = tableFontSize + 6;
        int defaultRowHeight = LookUtils.IS_LOW_RESOLUTION ? 17 : 18;
        table.setRowHeight(Math.max(minimumRowHeight, defaultRowHeight));
        return table;
    }


    /**
     * Creates and returns a sample tree model.
     */
    private TreeModel createSampleTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Musicians");
        DefaultMutableTreeNode parent;

        //
        parent = new DefaultMutableTreeNode("Drums");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("Elvin Jones"));
        parent.add(new DefaultMutableTreeNode("Jack DeJohnette"));
        parent.add(new DefaultMutableTreeNode("Rashied Ali"));

        //
        parent = new DefaultMutableTreeNode("Piano");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("Alexander von Schlippenbach"));
        parent.add(new DefaultMutableTreeNode("McCoy Tyner"));
        parent.add(new DefaultMutableTreeNode("Sun Ra"));

        parent = new DefaultMutableTreeNode("Saxophon");
        root.add(parent);
        parent.add(new DefaultMutableTreeNode("Albert Ayler"));
        parent.add(new DefaultMutableTreeNode("Archie Shepp"));
        parent.add(new DefaultMutableTreeNode("Charlie Parker"));
        parent.add(new DefaultMutableTreeNode("John Coltrane"));
        parent.add(new DefaultMutableTreeNode("Ornette Coleman"));
        parent.add(new DefaultMutableTreeNode("Pharoa Sanders"));
        parent.add(new DefaultMutableTreeNode("Sonny Rollins"));

        return new DefaultTreeModel(root);
    }


    /**
     * Creates and returns sample table data.
     */
    private Object[][] createSampleTableData() {
        return new Object[][] {
            { "Albert Ayler",   "Greenwich Village",       Boolean.TRUE  },
            { "Carla Bley",     "Escalator Over the Hill", Boolean.TRUE  },
            { "Frank Zappa",    "Yo' Mama",                Boolean.TRUE  },
            { "John Coltrane",  "Ascension",               Boolean.TRUE  },
            { "Miles Davis",    "In a Silent Way",         Boolean.TRUE  },
            { "Pharoa Sanders", "Karma",                   Boolean.TRUE  },
            { "Wayne Shorter",  "Juju",                    Boolean.TRUE  },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
            { "",               "",                        Boolean.FALSE },
        };
    }

    private static final class SampleTableModel extends AbstractTableModel {

        private final String[] columnNames;
        private final Object[][] rowData;

        SampleTableModel(Object[][] rowData, String[] columnNames) {
            this.columnNames = columnNames;
            this.rowData = rowData;
        }
        public String getColumnName(int column) { return columnNames[column].toString(); }
        public int getRowCount() { return rowData.length; }
        public int getColumnCount() { return columnNames.length; }
        public Class getColumnClass(int column) {
            return column == 2 ? Boolean.class : super.getColumnClass(column);
        }
        public Object getValueAt(int row, int col) { return rowData[row][col]; }
        public boolean isCellEditable(int row, int column) { return true; }
        public void setValueAt(Object value, int row, int col) {
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }

}