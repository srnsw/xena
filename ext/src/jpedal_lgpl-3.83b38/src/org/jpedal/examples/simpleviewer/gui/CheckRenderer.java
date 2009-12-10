package org.jpedal.examples.simpleviewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

public class CheckRenderer extends JPanel implements TreeCellRenderer {
	  protected JCheckBox check;
	  protected TreeLabel label;

	  public CheckRenderer() {
	    setLayout(null);
	    add(check = new JCheckBox());
	    add(label = new TreeLabel());
	    check.setBackground(UIManager.getColor("Tree.textBackground"));
	  }

	  public Component getTreeCellRendererComponent(JTree tree, Object value,
	               boolean isSelected, boolean expanded,
	               boolean leaf, int row, boolean hasFocus) {

	      String  stringValue = tree.convertValueToText(value, isSelected,
	              expanded, leaf, row, hasFocus);


	      setEnabled(tree.isEnabled());

	      if(value instanceof CheckNode){

	          check.setSelected(((CheckNode)value).isSelected());
	          setEnabled(((CheckNode)value).isEnabled());
	          check.setEnabled(((CheckNode)value).isEnabled());

	          label.setFont(tree.getFont());
	          label.setText(stringValue);
	          label.setSelected(isSelected);
	          label.setFocus(hasFocus);

	          return this;
	      }else
	          return new JLabel(stringValue);

	  }

	  public Dimension getPreferredSize() {
	    Dimension d_check = check.getPreferredSize();
	    Dimension d_label = label.getPreferredSize();
	    return new Dimension(d_check.width  + d_label.width,
	      (d_check.height < d_label.height ?
	       d_label.height : d_check.height));
	  }

	  public void doLayout() {
	    Dimension d_check = check.getPreferredSize();
	    Dimension d_label = label.getPreferredSize();
	    int y_check = 0;
	    int y_label = 0;
	    if (d_check.height < d_label.height) {
	      y_check = (d_label.height - d_check.height)/2;
	    } else {
	      y_label = (d_check.height - d_label.height)/2;
	    }
	    check.setLocation(0,y_check);
	    check.setBounds(0,y_check,d_check.width,d_check.height);
	    label.setLocation(d_check.width,y_label);
	    label.setBounds(d_check.width,y_label,d_label.width,d_label.height);
	  }


	  public void setBackground(Color color) {
	    if (color instanceof ColorUIResource)
	      color = null;
	    super.setBackground(color);
	  }


	  static class TreeLabel extends JLabel {
	    boolean isSelected;
	    boolean hasFocus;

	    TreeLabel() {
	    }

	    public void setBackground(Color color) {
		if(color instanceof ColorUIResource)
		    color = null;
		super.setBackground(color);
	    }

	    public void paint(Graphics g) {
	      String str;
	      if ((str = getText()) != null) {
	        if (0 < str.length()) {
	          if (isSelected) {
	            g.setColor(UIManager.getColor("Tree.selectionBackground"));
	          } else {
	            g.setColor(UIManager.getColor("Tree.textBackground"));
	          }
	          Dimension d = getPreferredSize();
	          int imageOffset = 0;
	          Icon currentI = getIcon();
	          if (currentI != null) {
	            imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
	          }
	          g.fillRect(imageOffset, 0, d.width -1 - imageOffset, d.height);
	          if (hasFocus) {
	            g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
	            g.drawRect(imageOffset, 0, d.width -1 - imageOffset, d.height -1);
	         }
	        }
	      }
	      super.paint(g);
	    }

	    public Dimension getPreferredSize() {
	      Dimension retDimension = super.getPreferredSize();
	      if (retDimension != null) {
	        retDimension = new Dimension(retDimension.width + 3,
					 retDimension.height);
	      }
	      return retDimension;
	    }

	    void setSelected(boolean isSelected) {
	    	this.isSelected = isSelected;
	    }

	    void setFocus(boolean hasFocus) {
	      this.hasFocus = hasFocus;
	    }
	  }
	}
