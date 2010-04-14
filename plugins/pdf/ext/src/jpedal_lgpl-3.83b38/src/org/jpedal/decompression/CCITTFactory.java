package org.jpedal.decompression;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.BitSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class CCITTFactory {
	
	private int EOL = -1;
	
	private static final boolean debug = false;
	
	private boolean byteAlign = false;
	
	private int k = 0;
	
	public static boolean test = true;
	
	private static int row = 0;
	
	private CusotomBitSet cbs = null;

	// ---------- BLACK --------------
	
	private int[][] b2 = new int[][]{
			{3, 2, 0},
			{2, 3, 0},
	};
	
	private int[][] b3 = new int[][]{
			{2, 1, 0},
			{3, 4, 0}
	};
	
	private int[][] b4 = new int[][]{
			{3, 5, 0},
			{2, 6, 0}
	};
	
	private int[][] b5 = new int[][]{
			{3, 7, 0}
	};
	
	private int[][] b6 = new int[][]{
			{5, 8, 0},
			{4, 9, 0}
	};
	
	private int[][] b7 = new int[][]{
			{4, 10, 0},
			{5, 11, 0},
			{7, 12, 0}
	};
	
	private int[][] b8 = new int[][]{
			{4, 13, 0},
			{7, 14, 0}
	};
	
	private int[][] b9 = new int[][]{
			{24, 15, 0}
	};
	
	private int[][] b10 = new int[][]{
			{55, 0, 0},
			{23, 16, 0},
			{24, 17, 0},
			{8, 18, 0},
			{15, 64, 1}
	};
	
	private int[][] b11 = new int[][]{
			{103, 19, 0},
			{104, 20, 0},
			{108, 21, 0},
			{55, 22, 0},
			{40, 23, 0},
			{23, 24, 0},
			{24, 25, 0},
			{8, 1792, 1},
			{12, 1856, 1},
			{13, 1920, 1}
	};
	
	private int[][] b12 = new int[][]{
			{202, 26, 0},
			{203, 27, 0},
			{204, 28, 0},
			{205, 29, 0},
			{104, 30, 0},
			{105, 31, 0},
			{106, 32, 0},
			{107, 33, 0},
			{210, 34, 0},
			{211, 35, 0},
			{212, 36, 0},
			{213, 37, 0},
			{214, 38, 0},
			{215, 39, 0},
			{108, 40, 0},
			{109, 41, 0},
			{218, 42, 0},
			{219, 43, 0},
			{84, 44, 0},
			{85, 45, 0},
			{86, 46, 0},
			{87, 47, 0},
			{100, 48, 0},
			{101, 49, 0},
			{82, 50, 0},
			{83, 51, 0},
			{36, 52, 0},
			{55, 53, 0},
			{56, 54, 0},
			{39, 55, 0},
			{40, 56, 0},
			{88, 57, 0},
			{89, 58, 0},
			{43, 59, 0},
			{44, 60, 0},
			{90, 61, 0},
			{102, 62, 0},
			{103, 63, 0},
			{200, 128, 1},
			{201, 192, 1},
			{91, 256, 1},
			{51, 320, 1},
			{52, 384, 1},
			{53, 448, 1},
			{1, EOL, 1},
			{18, 1984, 1},
			{19, 2048, 1},
			{20, 2112, 1},
			{21, 2176, 1},
			{22, 2240, 1},
			{23, 2304, 1},
			{28, 2368, 1},
			{29, 2432, 1},
			{30, 2496, 1},
			{31, 2560, 1}
	};
	
	private int[][] b13 = new int[][]{
			{108, 512, 1},
			{109, 576, 1},
			{74, 640, 1},
			{75, 704, 1},
			{76, 768, 1},
			{77, 832, 1},
			{114, 896, 1},
			{115, 960, 1},
			{116, 1024, 1},
			{117, 1088, 1},
			{118, 1152, 1},
			{119, 1216, 1},
			{82, 1280, 1},
			{83, 1344, 1},
			{84, 1408, 1},
			{85, 1472, 1},
			{90, 1536, 1},
			{91, 1600, 1},
			{100, 1664, 1},
			{101, 1728, 1}
	};
	
	// ---------- WHITE --------------
	
	private int[][] w4 = new int[][]{
			{7, 2, 0},
			{8, 3, 0},
			{11, 4, 0},
			{12, 5, 0},
			{14, 6, 0},
			{15, 7, 0}
	};
	
	private int[][] w5 = new int[][]{
			{19, 8, 0},
			{20, 9, 0},
			{7, 10, 0},
			{8, 11, 0},
			{27, 64, 1},
			{18, 128, 1}
	};
	
	private int[][] w6 = new int[][]{
			{7, 1, 0},
			{8, 12, 0},
			{3, 13, 0},
			{52, 14, 0},
			{53, 15, 0},
			{42, 16, 0},
			{43, 17, 0},
			{23, 192, 1},
			{24, 1664, 1}
	};

	private int[][] w7 = new int[][]{
			{39, 18, 0},
			{12, 19, 0},
			{8, 20, 0},
			{23, 21, 0},
			{3, 22, 0},
			{4, 23, 0},
			{40, 24, 0},
			{43, 25, 0},
			{19, 26, 0},
			{36, 27, 0},
			{24, 28, 0},
			{55, 256, 1}
	};

	private int[][] w8 = new int[][]{
			{53, 0, 0},
			{2, 29, 0},
			{3, 30, 0},
			{26, 31, 0},
			{27, 32, 0},
			{18, 33, 0},
			{19, 34, 0},
			{20, 35, 0},
			{21, 36, 0},
			{22, 37, 0},
			{23, 38, 0},
			{40, 39, 0},
			{41, 40, 0},
			{42, 41, 0},
			{43, 42, 0},
			{44, 43, 0},
			{45, 44, 0},
			{4, 45, 0},
			{5, 46, 0},
			{10, 47, 0},
			{11, 48, 0},
			{82, 49, 0},
			{83, 50, 0},
			{84, 51, 0},
			{85, 52, 0},
			{36, 53, 0},
			{37, 54, 0},
			{88, 55, 0},
			{89, 56, 0},
			{90, 57, 0},
			{91, 58, 0},
			{74, 59, 0},
			{75, 60, 0},
			{50, 61, 0},
			{51, 62, 0},
			{52, 63, 0},
			{54, 320, 1},
			{55, 384, 1},
			{100, 448, 1},
			{101, 512, 1},
			{104, 576, 1},
			{103, 640, 1}
	};

	private int[][] w9 = new int[][]{
			{204, 704, 1},
			{205, 768, 1},
			{210, 832, 1},
			{211, 896, 1},
			{212, 960, 1},
			{213, 1024, 1},
			{214, 1088, 1},
			{215, 1152, 1},
			{216, 1216, 1},
			{217, 1280, 1},
			{218, 1344, 1},
			{219, 1408, 1},
			{152, 1472, 1},
			{153, 1536, 1},
			{154, 1600, 1},
			{155, 1728, 1}
	};
	
	private int[][] w11 = new int[][]{
			{8, 1792, 1},
			{12, 1856, 1},
			{13, 1920, 1}
	};
	
	private int[][] w12 = new int[][]{
			{1, EOL, 1},
			{18, 1984, 1},
			{19, 2048, 1},
			{20, 2112, 1},
			{21, 2176, 1},
			{22, 2240, 1},
			{23, 2304, 1},
			{28, 2368, 1},
			{29, 2432, 1},
			{30, 2496, 1},
			{31, 2560, 1}
	};
	
	private BitSet bs = new BitSet();
	private int bsLength = 0;
	private byte tmp = 0;
	private int mask = 0;
	
	private boolean isWhite = true;
	private boolean isTerminating = false;
	
	private boolean itemFound = false;
	private boolean isEndOfLine = false;
	public boolean EOS = false;
	
	private int cRTC = 0;
	private int width = 0;
	private int height = 0;
	private int line = 0; 
	private int rowC = 0;
	
	private BitSet out = new BitSet();
	private int outPtr = 0;
	
	private byte[] output = null;

	public CCITTFactory(byte[] data, int w, int h,PdfObject p){
		
		//System.out.println("RAW stream ...  w = " + w + " h =" +h);
		//for(int i=0;i<100;i++){
		//	System.out.print((data[i]&255)+" ");
		//}
		//System.out.println(".....EOS.");
		
		//data = this.clean(data);
		
		bs = this.fromByteArray(data);
		cbs = new CusotomBitSet(bs, bsLength);
		bs = new BitSet();
		width = w;
		height = h;
		byteAlign = p.getBoolean(PdfDictionary.EncodedByteAlign);
		k = p.getInt(PdfDictionary.K);
		
		//if(k!=0){
		//	System.err.println("At the moment the decoder can only handle 1-D encoded files!");
		//}

		cbs.moveToEOLMarker();
		
		
		bs = cbs.getSection();

		//cbs.printData(0, 200);
		if(debug)
			System.out.println("->Width =" + w + " Height =" + h + "\n");

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


	public byte[] decode(){

		//output = new byte[((width+7)/8)*(height)];
		
		int temp = width % 8;
		int temp2 = width / 8;		
		int temp3 = 0;
		
		if(temp==0){
			temp3 = temp2;
		}else{
			temp3 = temp2+1;
		}
		
		output = new byte[(temp3)*(height)];
		
		for(;!EOS;){
			getRun_1D();
		}
		
		int bytePtr = 0;
		int bitPtr = 7;
		int mask = 0;
		byte entry = 0;

		for(int j=0;j<outPtr;j++){

			if(out.get(j)==true){
				mask = 1 << bitPtr;

				entry |= mask;
				bitPtr--;
			}else{
				bitPtr--;
			}
			
			if(((j+1)%(width))==0 && (j!=0)){
				bitPtr = -1;
			}

			if(bitPtr < 0){		
				output[bytePtr]=entry;
				bytePtr++;
				bitPtr = 7;
				entry = 0;
			}
		}
		
		if(debug)
			System.out.println("\nDecoding complete!");

		
        //make a a DEEP copy so we cant alter
      /*  int len=output.length;
        byte[] copy=new byte[len];
        System.arraycopy(output, 0, copy, 0, len);

		*/
		/** create an image from the raw data *//*
		DataBuffer db = new DataBufferByte(copy, copy.length);
		
		WritableRaster raster = Raster.createPackedRaster(db, width, height, 1, null);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		image.setData(raster);

		Image scaledImage = image.getScaledInstance(200, -1, Image.SCALE_AREA_AVERAGING);

		BufferedImage result = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_BGR);
		Graphics2D g = result.createGraphics();
		g.drawImage(scaledImage, 0, 0, null);
		g.dispose();

		JLabel label = new JLabel(new ImageIcon(result));
		
		JOptionPane.showConfirmDialog(null, label, "JBIG2 Display", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);*/

		
		return output;
	}
	
	
	
	private void getRun_1D(){
		int maskAdj = 0;

		if(isTerminating){

			isTerminating = false;
			isWhite = !isWhite;
			
			if(isEndOfLine){
				isEndOfLine = false;
				isWhite = true;
			}
		}
		
		for(int t=2;t<=13;t++){
			tmp = 0;
			for(int y=0;y<t;y++){			
				if(bs.get(y)==true){
					mask = 1 << (t-y-1-maskAdj);
					tmp |= mask;
				}
			}
			
			this.checkTables((tmp&255), t);

			if(itemFound){

				if(t==12 && tmp==1){
					cRTC++;
					if(cRTC==6){
						EOS = true;
						return;
					}
				}else{
					cRTC = 0;
				}
				
				bs = cbs.moveToNextRun(bs,t-maskAdj);
				
				if(isEndOfLine && byteAlign){
					bs = cbs.byteAlignRow(bs,rowC);
				}
	
				itemFound = false;
				return;
			}
			
			if(t==8){
				bs = cbs.shiftOnce(bs);
				maskAdj++;
			}
	
		}

		if(cbs.getCbsPtr()>cbs.cbsLength){
			EOS = true;
		}

		return;
	}

	
	private void checkTables(int tmp, int i) {
		
		switch(i){
			case(2):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b2.length;z++){
						if(tmp==b2[z][0]){
							itemFound = true;
							
							inputNumbers(b2[z][1],1,(b2[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b2[z][1] + " cbsPtr=" + cbs.getCbsPtr());
							
							
						}
					}
				}
				break;
			
			case(3):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b3.length;z++){
						if(tmp==b3[z][0]){
							itemFound = true;
							
							inputNumbers(b3[z][1],1,(b3[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b3[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							

						}
					}
				}
				break;
			
			case(4):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b4.length;z++){
						if(tmp==b4[z][0]){
							itemFound = true;
							
							inputNumbers(b4[z][1],1,(b4[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b4[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							

						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w4.length;z++){
						if(tmp==w4[z][0]){
							itemFound = true;
							
							inputNumbers(w4[z][1],0,(w4[z][2]==0));
							
							if(debug)
								System.out.println("Code White " + w4[z][1]+ " cbsPtr=" + cbs.getCbsPtr());

						}
					}
				}
				
				break;
			
			case(5):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b5.length;z++){
						if(tmp==b5[z][0]){
							itemFound = true;
							
							inputNumbers(b5[z][1],1,(b5[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b5[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							

						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w5.length;z++){
						if(tmp==w5[z][0]){
							itemFound = true;
							
							inputNumbers(w5[z][1],0,(w5[z][2]==0));
							
							if(debug)
								System.out.println("Code White " + w5[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				
				break;
			
			case(6):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b6.length;z++){
						if(tmp==b6[z][0]){
							itemFound = true;
							
							inputNumbers(b6[z][1],1,(b6[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b6[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							

						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w6.length;z++){
						if(tmp==w6[z][0]){
							itemFound = true;
							
							inputNumbers(w6[z][1],0,(w6[z][2]==0));
							
							if(debug)
								System.out.println("Code White " + w6[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				
				break;
			
			case(7):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b7.length;z++){
						if(tmp==b7[z][0]){
							itemFound = true;
							
							inputNumbers(b7[z][1],1,(b7[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b7[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							

						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w7.length;z++){
						if(tmp==w7[z][0]){
							itemFound = true;
							
							inputNumbers(w7[z][1],0,(w7[z][2]==0));
							
							if(debug)
								System.out.println("Code White " + w7[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				
				break;
			
			case(8):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b8.length;z++){
						if(tmp==b8[z][0]){
							itemFound = true;
							
							inputNumbers(b8[z][1],1,(b8[z][2]==0));
							
							if(debug)
								System.out.println("Code Black " + b8[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w8.length;z++){
						if(tmp==w8[z][0]){
							itemFound = true;
							
							if(debug)
								System.out.println("Code White " + w8[z][1]+ " cbsPtr=" + cbs.getCbsPtr());

							
							inputNumbers(w8[z][1],0,(w8[z][2]==0));
							
							
						}
					}
				}
				
				break;
			
			case(9):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b9.length;z++){
						if(tmp==b9[z][0]){
							itemFound = true;
							
							inputNumbers(b9[z][1],1,(b9[z][2]==0));
							
							if(debug)
								System.out.println("Code Black "  + b9[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							

						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w9.length;z++){
						if(tmp==w9[z][0]){
							itemFound = true;
							
							inputNumbers(w9[z][1],0,(w9[z][2]==0));
							
							if(debug)
								System.out.println("Code White "  + w9[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				
				break;
			
			case(10):
				
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b10.length;z++){
						if(tmp==b10[z][0]){
							itemFound = true;
							
							inputNumbers(b10[z][1],1,(b10[z][2]==0));
							
							if(debug)
								System.out.println("Code Black "  + b10[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				break;
			
			case(11):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b11.length;z++){
						if(tmp==b11[z][0]){
							itemFound = true;
							
							inputNumbers(b11[z][1],1,(b11[z][2]==0));
							
							if(debug)
								System.out.println("Code Black "  + b11[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w11.length;z++){
						if(tmp==w11[z][0]){
							itemFound = true;
							
							inputNumbers(w11[z][1],0,(w11[z][2]==0));
							
							if(debug)
								System.out.println("Code White "  + w11[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				break;
			
			case(12):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b12.length;z++){
						if(tmp==b12[z][0]){
							itemFound = true;

							if(debug)
								System.out.println("Code Black "  + b12[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
							if(b12[z][1]==-1){
								row++;
								isWhite = true;
							}else{
								inputNumbers(b12[z][1],1,(b12[z][2]==0));
							}

						}
					}
				}else{
					for(int z=0;!itemFound && z<this.w12.length;z++){
						if(tmp==w12[z][0]){
							itemFound = true;
							
							if(debug)
								System.out.println("Code White "  + w12[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
							if(w12[z][1]==-1){
								row++;
							}
							
							inputNumbers(w12[z][1], 0,(w12[z][2]==0));
							
						}
					}
				}
				
				break;
			
			case(13):
				if(!isWhite){
					for(int z=0;!itemFound && z<this.b13.length;z++){
						if(tmp==b13[z][0]){
							itemFound = true;
							
							inputNumbers(b13[z][1],1,(b13[z][2]==0));
							
							if(debug)
								System.out.println("Code Black "  + b13[z][1]+ " cbsPtr=" + cbs.getCbsPtr());
							
						}
					}
				}
				
				break;
			
			default:
				if(1==1)
				throw new RuntimeException("Should not get here! Will exit!");
				break;
		}
		
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
	
	private void inputNumbers(int code,int i,boolean isT) {
			
		if(isT)
			isTerminating = true;
		
		/*if(code != -1){
			if(i==0){
				System.out.println("White = " + code);
			}else{
				System.out.println("Black = " + code);
			}
		}*/

		if(code==-1){
			if(line != 0){
				System.err.println("EOF marker encountered but not EOL yet!");
			}else{
				
			}

			line = 0;
			isWhite = true;
			isTerminating = false;
		}else{
			line = line + code;
			
			if(line==width){
				if(isT){
					line = 0;
					isEndOfLine = true;
					rowC++;
					//System.out.println(" --- "+rowC+ " -------------------------------------");
				}

			}else if(line>width){
				//System.err.println("Wrong line lenght! " + (line - width) + " cbsL=" + cbs.cbsLength);
				line = 0;
				isEndOfLine = true;

			}
			
			if(code!=0){
				out.set(outPtr, (outPtr + code), (i==0));
				outPtr = outPtr + code; 
			}
		}
		
	}

}
