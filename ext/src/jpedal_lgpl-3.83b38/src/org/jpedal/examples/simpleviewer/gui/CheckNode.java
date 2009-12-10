package org.jpedal.examples.simpleviewer.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public class CheckNode extends DefaultMutableTreeNode {

	  protected int selectionMode;
	  protected boolean isSelected, isEnabled;
	    private Object text;

	    public CheckNode() {
	    this(null);
	  }

	  public CheckNode(Object userObject) {
	    this(userObject, true, false);
	      
	      text=userObject;
	  }

	  public CheckNode(Object userObject, boolean allowsChildren
	                                    , boolean isSelected) {
	    super(userObject, allowsChildren);
	    this.isSelected = isSelected;
	      selectionMode = 0;
	      
	      text=userObject;
	  }

	    public Object getText() {
	        return text;
	    }

	  public void setSelectionMode(int mode) {
	    selectionMode = mode;
	    
	  }

	  public int getSelectionMode() {
	    return selectionMode;
	  }

	  public void setSelected(boolean isSelected) {  
	    this.isSelected = isSelected;
	    
	  }
	  
	  public boolean isSelected() {
	    return isSelected;
	  }

	    public void setEnabled(boolean isEnabled) {
	    this.isEnabled = isEnabled;

	  }

	  public boolean isEnabled() {
	    return isEnabled;
	  }
	}
