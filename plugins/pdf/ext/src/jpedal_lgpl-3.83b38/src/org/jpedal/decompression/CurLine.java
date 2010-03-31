package org.jpedal.decompression;

import java.util.BitSet;

public class CurLine {
	
	private BitSet set = new BitSet();
	private int setL = 0;
	public int setPtr = 0;
	
	public CurLine(int w){
		setL = w;
	}
	
	public BitSet getBitSet(){
		return set;
	}
	
	public int getL(){
		return setL;
	}
	
	public void parseRun(int run, int col){
		int i = 0;
		int x = setPtr;
		for(i=x;i<(x+run);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		//setL = i;
	}
	
	public void parsePicElementV0(ChangingPicElement pic){
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
	}
	
	
	public void parsePicElementVR1(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		start++;
		lenght--;
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
	}

	public void parsePicElementVL1(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		start--;
		lenght++;
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
		
	}

	public void parsePicElementPass(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		
		boolean color = set.get(start-1);
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, color);
			setPtr++;
		}
		
		setL = i;
		
		
	}

	public void parsePicElementVR2(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		start = start + 2;
		lenght = lenght - 2;
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
	}

	public void parsePicElementVL2(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		start = start - 2;
		lenght = lenght + 2;
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
	}


	public void parsePicElementVR3(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		start = start + 3;
		lenght = lenght - 3;
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
	}

	public void parsePicElementVL3(ChangingPicElement pic) {
		int start = pic.getStart();
		int lenght = pic.getLenght();
		int col = pic.getColor();
		
		start = start - 3;
		lenght = lenght + 3;
		
		int i = 0;
		for(i=start;i<(start+lenght);i++){
			set.set(i, col==1);
			setPtr++;
		}
		
		setL = i;
		
	}

	public boolean getEOEL() {
		if(setPtr>=setL){
			return true;
		}
		return false;
	}

}
