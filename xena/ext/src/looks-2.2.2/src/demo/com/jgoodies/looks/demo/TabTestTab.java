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
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.looks.Options;
import com.jgoodies.uif_lite.component.Factory;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * Demonstrates optionals settings for the JGoodies
 * tabbed panes using two <code>SimpleInternalFrame</code>.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
final class TabTestTab {

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
        JSplitPane pane = Factory.createStrippedSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildMainLeftPanel(),
            buildMainRightPanel(),
            0.2f);
        pane.setOpaque(false);
        return pane;
    }


    /**
     * Builds and returns a panel that uses a tabbed pane with embedded tabs
     * enabled.
     */
    private JComponent buildMainLeftPanel() {
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
        tabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
        tabbedPane.addTab("Tree", Factory.createStrippedScrollPane(buildTree()));
        tabbedPane.addTab("Help", Factory.createStrippedScrollPane(buildHelp()));

        SimpleInternalFrame sif = new SimpleInternalFrame("Embedded Tabs");
        sif.setPreferredSize(new Dimension(150, 100));
        sif.add(tabbedPane);
        return sif;
    }


    /**
     * Builds and returns a sample tree.
     */
    private JTree buildTree() {
        JTree tree = new JTree(createSampleTreeModel());
        tree.putClientProperty(Options.TREE_LINE_STYLE_KEY,
                               Options.TREE_LINE_STYLE_NONE_VALUE);
        tree.expandRow(3);
        tree.expandRow(2);
        tree.expandRow(1);
        return tree;
    }


    private JComponent buildHelp() {
        JTextArea area = new JTextArea("\n This tabbed pane uses\n embedded tabs.");
        return area;
    }


    /**
     * Builds and returns a tabbed pane with the no-content-border enabled.
     */
    private JComponent buildMainRightPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
        tabbedPane.addTab("Top",    buildSplittedTabs(JTabbedPane.TOP));
        tabbedPane.addTab("Bottom", buildSplittedTabs(JTabbedPane.BOTTOM));
        tabbedPane.addTab("Left",   buildSplittedTabs(JTabbedPane.LEFT));
        tabbedPane.addTab("Right",  buildSplittedTabs(JTabbedPane.RIGHT));

        SimpleInternalFrame sif = new SimpleInternalFrame("Tabbed Pane without Content Border");
        sif.setPreferredSize(new Dimension(300, 100));
        sif.add(tabbedPane);
        return sif;
    }


    /**
     * Builds and returns a split pane with tabs using different tab layouts
     * on the left and right-hand side. The tab on the left-hand side uses
     * the <code>WRAP_TAB_LAYOUT</code>, the tab on the right side uses
     * the <code>SCROLL_TAB_LAYOUT</code>.
     * The tabs are positioned using the specified orientation.
     *
     * @param tabPlacement the placement for the tabs relative to the content
     * @throws IllegalArgumentException if tab placement is not
     *            one of the supported values
     */
    private JComponent buildSplittedTabs(int tabPlacement) {
        int orientation = (tabPlacement == JTabbedPane.TOP
                        || tabPlacement == JTabbedPane.BOTTOM)
                        ? JSplitPane.HORIZONTAL_SPLIT
                        : JSplitPane.VERTICAL_SPLIT;
        JComponent split = Factory.createStrippedSplitPane(
                orientation,
                buildTabPanel(tabPlacement, JTabbedPane.WRAP_TAB_LAYOUT),
                buildTabPanel(tabPlacement, JTabbedPane.SCROLL_TAB_LAYOUT),
                0.5f);
        split.setOpaque(false);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(split, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }


    /**
     * Builds and returns a sample tabbed pane with the specified orientation
     * and tab layout style.
     *
     * @param tabPlacement the placement for the tabs relative to the content
     * @param tabLayoutPolicy the policy for laying out tabs when all tabs will not fit on one run
     * @throws IllegalArgumentException if tab placement or tab layout policy is not
     *            one of the supported values
     */
    private JComponent buildTabPanel(int tabPlacement, int tabLayoutPolicy) {
        JTabbedPane tabbedPane = new JTabbedPane(tabPlacement, tabLayoutPolicy);
        String[] colors = {
                "Black", "White", "Red", "Green", "Blue", "Yellow" };
        for (int i = 0; i < colors.length; i++) {
            String color = colors[i];
            JPanel filler = new JPanel(null);
            filler.setOpaque(false);
            tabbedPane.addTab(color, filler);
        }
        return tabbedPane;
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


}