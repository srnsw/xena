package org.jpedal.decompression;

import java.util.BitSet;

public class CustomBitSet2D {
	
	private static final boolean debug = false;
	
	public int cbsPtr = 0;
	public int cbsLength = 0;
	private BitSet data = null;

	public CustomBitSet2D(BitSet bs, int l) {
		
		data = bs;
		cbsLength = l;
		
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
	
	public void printData(int from , int till){
		
		System.out.println("-> Displaying data from " + from + " till " + till);
		
		for(;from<till;from++){
			if(data.get(from)==true)
				System.out.print("1");
			else
				System.out.print("0");
		}
		
		throw new RuntimeException("\nEXIT");
		
	}
}
