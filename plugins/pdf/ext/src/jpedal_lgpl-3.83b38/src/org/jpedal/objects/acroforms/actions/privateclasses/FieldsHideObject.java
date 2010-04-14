package org.jpedal.objects.acroforms.actions.privateclasses;

public class FieldsHideObject {
	private String[] fieldsToHide = new String[0];
    private boolean[] whetherToHide = new boolean[0];
    
    public void setFieldArray(String[] newFieldarray){
    	fieldsToHide = newFieldarray;
    }
    
    public void setHideArray(boolean[] newHidearray){
    	whetherToHide = newHidearray;
    }
    
    public String[] getFieldArray(){
    	return fieldsToHide;
    }
    
    public boolean[] getHideArray(){
    	return whetherToHide;
    }
}
