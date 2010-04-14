package org.jpedal.decompression;

import java.util.BitSet;

import org.jpedal.objects.raw.PdfObject;

public class CCITTMix {
	
	private int width=0;
	private int height=0;
	private int topPointer = 0;
	private int oMode  = 0;
	private int bsLength = 0;
	private int outSetL =0 ;
	
	private RefLine ref;
	private CurLine cur;
	private CusotomBitSet outSet = null;
	private BitSet bs;
	private BitSet outSet2;
	
	private byte[] data = null;
	
	public CCITTMix(byte[] data, int w, int h,PdfObject p){
			
		//data = clean(data);
		this.data = data;
		width = w;
		height = h;
		bs = this.fromByteArray(data);
		outSet = new CusotomBitSet(bs, bsLength);
		
		//System.out.println("stream data: width="+width+" height="+height);	
	}
	
	public void dummy(){
		
		outSet.printData(0, 300);
		
	}
	
	public byte[] decode(){
		
		ref = new RefLine(new BitSet(),width);
		cur = new CurLine(width);
		
		byte[] output = new byte[((width+7)/8)*(height)];
		
		int bytePtr = 0;
		int bitPtr = 7;
		int mask = 0;
		byte entry = 0;

		for(int j=0;j<outSetL;j++){
			
			
			if(bs.get(j)==true){
				
				switch(bitPtr){
					case(0):
						mask =1;
						break;
					case(1):
						mask =2;
						break;
					case(2):
						mask =4;
						break;
					case(3):
						mask =8;
						break;
					case(4):
						mask =16;
						break;
					case(5):
						mask =32;
						break;
					case(6):
						mask =64;
						break;
					case(7):
						mask =128;
						break;
				
				}
	
				entry |= mask;
				bitPtr--;
			}else{
				bitPtr--;
			}
			
			
			
			if(bitPtr < 0){
				output[bytePtr]=entry;
				bytePtr++;
				bitPtr = 7;
				entry = 0;
			}
		}
		
		return output;
	}
	
	private static byte[] clean(byte[] data) {
		byte[] ret = null;
		int ptr = 0;

		
		if(data[0]==32||data[0]==10||data[0]==0){
			ptr = 1;
		}
		
		if(data[1]==32||data[1]==10||data[1]==1){
			ptr = 2;
		}
		
		ret = new byte[data.length-ptr];
		
		for(int i = ptr,q=0;i<data.length;i++,q++){
			ret[q] = data[i];
		}
		
		return ret;
	}
	
	private BitSet fromByteArray(byte[] bytes) {
		
		int bitSetPtr = 0;
		byte tmp;
		int value = 0;
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length; i++) {
        	tmp=bytes[i];
        	for(int z=7;z>=0;z--){
        		
        		value = (tmp & (1 << z));
        		
        		if(value>=1)
        			bits.set(bitSetPtr,true);

        		bitSetPtr++;
        	}
        }
        
        // will give a true length of bs as bs.lenght only counts till the last 1 in stream!
        bsLength = bitSetPtr;
        
        return bits;
        
	}

}
