package org.jpedal.decompression;

import java.util.BitSet;

public class RefLine {
	
	private BitSet line = new BitSet();
	private int lineL = 0;
	private int linePtr = 0;
	private boolean currentColor = false; 
	
	public RefLine(BitSet b, int l){
		lineL = l;
		//System.out.println("");
		for(int i=0;i<l;i++){
			line.set(i, b.get(i));
			if(CCITT2D.debugRun && b.get(i)){
				
				//System.out.print("x ");
				
			}
		}
		//System.out.println("");
	}
	
	public BitSet getBitSet(){
		return line;
	}
	
	public int getL(){
		return lineL;
	}
	
	public ChangingPicElement getNextChangingPicElement(){
		int i = 0;
		
		for(i = linePtr;i<lineL;i++){
			if(line.get(i)==currentColor){
				
			}else{
				break;
			}
			
		}
		int color = 0;
		
		if(currentColor)
			color = 1;
		
		ChangingPicElement ret = new ChangingPicElement(linePtr,i-linePtr,color);
		
		currentColor = !currentColor;
		linePtr = i;
		
		return ret;
		
	}
	
	public ChangingPicElement getNextPicElement(int currentPosInCur){
		int i = 0;
		linePtr = currentPosInCur;
		
		for(i = linePtr;i<lineL;i++){
			if(line.get(i)==currentColor){
				
			}else{
				break;
			}
			
		}
		int color = 0;
		
		if(currentColor)
			color = 1;
		
		ChangingPicElement ret = new ChangingPicElement(linePtr,i-linePtr,color);
		
		currentColor = !currentColor;
		linePtr = i;
		
		return ret;
		
	}
	
	public ChangingPicElement getNextChangingPicElement(int currentPosInCur){
		int i = 0;
		
		linePtr = currentPosInCur;
		
		for(i = linePtr;i<lineL;i++){
			if(line.get(i)==currentColor){
				
			}else{
				break;
			}
			
		}
		int color = 0;
		
		if(currentColor)
			color = 1;
		
		ChangingPicElement ret = new ChangingPicElement(linePtr,i-linePtr,color);
		
		currentColor = !currentColor;
		linePtr = i;
		
		return ret;
		
	}

}
