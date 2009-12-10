/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
*
* 	This file is part of JPedal
*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


*
* ---------------
* SignaturesTreeCellRenderer.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

public class SignaturesTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon icon;

    public Icon getLeafIcon() {
        return icon;
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {

        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) value);
		value = node.getUserObject();
		int level = node.getLevel();
		
        String s = value.toString();
        icon = null;
        Font treeFont = tree.getFont();

        if(level== 2){
        	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        	String text=parent.getUserObject().toString();
        	if(text.equals("The following signature fields are not signed")){
        		URL resource = getClass().getResource("/org/jpedal/examples/simpleviewer/res/unlock.png");
        		icon = new ImageIcon(resource);
        	} else {
        		URL resource = getClass().getResource("/org/jpedal/examples/simpleviewer/res/lock.gif");
        		icon = new ImageIcon(resource);
        		treeFont = new Font(treeFont.getFamily(), Font.BOLD, treeFont.getSize());
        	}
        }
        
        setFont(treeFont);
        setText(s);
        setIcon(icon);
        if (isSelected) {
            setBackground(new Color(236, 233, 216));
            setForeground(Color.BLACK);
        } else {
            setBackground(tree.getBackground());
            setForeground(tree.getForeground());
        }
        setEnabled(tree.isEnabled());
        
        setOpaque(true);

        return this;
    }
}
