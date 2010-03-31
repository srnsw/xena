package org.jpedal.decompression;

import java.util.BitSet;

public class CusotomBitSet {
	
	private boolean debug = false;

	public int cbsPtr = 0;
	public int cbsLength = 0;
	private BitSet data = null;
	
	public CusotomBitSet(BitSet bs, int l) {
		data = bs;
		cbsLength = l;
	}
	
	public void moveToEOLMarker(){
		boolean isEOL = false;
		int i = 0;
		
		while(!isEOL){
			isEOL = true;
			for(i=0;i<12;i++){
				if(i==11){
					if(data.get(i+cbsPtr)==false){
						isEOL = false;
					}
				}else{
					if(data.get(i+cbsPtr)==true){
						isEOL = false;
					}
				}
			}
			
			cbsPtr++;
			
			// if EOL not found in the first 20 bits assume that
			// there is no EOL at the start od the line and start
			// at 0
			if(cbsPtr > 36){
				
				cbsPtr = 0;
				return;
			}
		}
		
		cbsPtr = cbsPtr + i - 1;
		
		if(debug)
			System.out.println("-> CusotomBitSet pointer = " + cbsPtr);
		
		return;
	}
	
	public BitSet getSection(){
		BitSet sec = new BitSet();
		
		int i = 0;
		
		for(;i<13;i++){
			sec.set(i, data.get(i+cbsPtr));
		}
		
		cbsPtr = cbsPtr + i;
		
		if(debug){
			System.out.println("-> CusotomBitSet pointer = " + cbsPtr);
			
			System.out.println("-> Sction listing:");
			for(int n=0;n<13;n++){
				if(sec.get(n)==true)
					System.out.print("1");
				else
					System.out.print("0");
			}
				
			System.out.println("");

		}

		return sec;
	}
	
	public int getSingleDigit(){
		
		boolean x = data.get(cbsPtr++);
		
		if(cbsPtr==cbsLength){
			return 0;
		}
		
		if(x){
			return 2;
		}else{
			return 1;
		}
	}
	
	public BitSet shiftOnce(BitSet x){
		
		BitSet sec = new BitSet();
		
		for(int i=0;i<12;i++){
			sec.set(i, x.get(i+1));
		}
		sec.set(12, data.get(cbsPtr++));
		
		if(debug){

			System.err.println("-> Parameter:");
			for(int n=0;n<13;n++){
				if(x.get(n)==true)
					System.err.print("1");
				else
					System.err.print("0");
			}
			
			System.err.println("");
			System.err.println("- > shifted to:");
			
			for(int n=0;n<13;n++){
				if(sec.get(n)==true)
					System.err.print("1");
				else
					System.err.print("0");
			}
				
			System.err.println("");
			//System.out.println("-> CusotomBitSet pointer = " + cbsPtr);
		}
		
		return sec;
	}

	public BitSet moveToNextRun(BitSet x, int t) {
		BitSet next = new BitSet();
		int tParam = t;
		int cbsPtrParam = cbsPtr;
		int over = 13 - (13 - t);
		int nextPtr = 0;
		
		for(;t<13;t++){
			next.set(nextPtr, x.get(t));
			nextPtr++;
		}
		
		for(int c=0;c<over;c++){
			next.set(nextPtr, data.get(cbsPtr));
			cbsPtr++;
			nextPtr++;
		}
		
		if(debug ){// || cbsPtrParam==1181){

			System.out.println("-> Parameter (t val=" + tParam + "):");
			for(int n=0;n<13;n++){
				if(x.get(n)==true)
					System.out.print("1");
				else
					System.out.print("0");
			}
			
			System.out.println("");
			System.out.println("-> CusotomBitSet pointer = " + cbsPtrParam + "\n");
			System.out.println("- > next:");
			
			for(int n=0;n<13;n++){
				if(next.get(n)==true)
					System.out.print("1");
				else
					System.out.print("0");
			}
				
			System.out.println("");
			System.out.println("-> CusotomBitSet pointer = " + cbsPtr);
			
		}
		//System.out.println("-> After moving to next run:  pointer = " + cbsPtr);

		return next;
	}

	public int getCbsPtr() {
		return this.cbsPtr;
	}
	
	public void printData(int from , int till){
		
		System.out.println("-> Displaying data from " + from + " till " + till);
		
		for(;from<till;from++){
			if(data.get(from)==true)
				System.out.print("1");
			else
				System.out.print("0");
		}
		
		System.out.println("");
	}

	public BitSet byteAlignRow(BitSet bs, int row) {
		BitSet ret = null;

		if(row==387 && false){
			System.out.println("Step inn!");
		}
		
		int iPart = (cbsPtr-13)%8;
		int iDrop = 8-(iPart);

		//System.out.println("items to drop " + iDrop);
		
		if(iPart>0){
			cbsPtr = (cbsPtr-13)+iDrop;
			ret = getSection();
			//System.out.println("items to drop " + iDrop);
			
			if(row==387 && false){
				System.out.println("-> Data to be aligned!, cbsPtr="+(cbsPtr-13));
				
				for(int i=0;i<13;i++){
					if(bs.get(i)==true)
						System.out.print("1");
					else
						System.out.print("0");
				}
				
				System.out.println("");
				
				System.out.println("-> Aligned data....");
				
				for(int i=0;i<13;i++){
					if(ret.get(i)==true)
						System.out.print("1");
					else
						System.out.print("0");
				}
				
				System.out.println("");
			}
		
			return ret;
		}else{
			return bs;
		}
	}

}
