package org.jpedal.examples.simpleviewer.javabean;

import java.beans.PropertyEditorSupport;

public class RotationEditor extends PropertyEditorSupport {

//	public RotationEditor() {
//		setSource(this);
//	}
//
//	public RotationEditor(Object source) {
//		if (source == null) {
//			throw new NullPointerException();
//		}
//		setSource(source);
//	}
	
	public String[] getTags() {
		return new String[] { "0", "90", "180", "270" };
	}

	public void setAsText(String s) {
		if (s.equals("0"))
			setValue(new Integer(0));
		else if (s.equals("90"))
			setValue(new Integer(90));
		else if (s.equals("180"))
			setValue(new Integer(180));
		else if (s.equals("270"))
			setValue(new Integer(270));
		else
			throw new IllegalArgumentException(s);
	}
	
	public String getJavaInitializationString() {
		switch (((Number) getValue()).intValue()) {
		default:
		case 0:
			return "0";
		case 90:
			return "90";
		case 180:
			return "180";
		case 270:
			return "270";
		}
	}
}
