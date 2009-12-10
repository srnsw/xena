/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
*
* 	This file is part of JPedal
*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


*
* ---------------
* T1Glyphs.java
* ---------------
*/
package org.jpedal.fonts.glyph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.Type1;
import org.jpedal.fonts.objects.FontData;
import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.objects.GraphicsState;
import org.jpedal.PdfDecoder;
import org.jpedal.utils.LogWriter;

public class T1Glyphs extends PdfJavaGlyphs {

	private static String nybChars = "0123456789.ee -";

	//flag to show if actually 1c
	public boolean is1C=false;


	/**holds mappings for drawing the glpyhs*/
	protected Map charStrings=new Hashtable();

	/**holds the numbers*/
    int max=100;
    
    double[] operandsRead = new double[max];

	/**pointer on stack*/
	int operandReached=0;

		float[] pt;

		//co-ords for closing glyphs
		private double xs=-1,ys=-1,x=0,y=0;

		/**tracks points read in t1 flex*/
		private int ptCount=0;

		/**op to be used next*/
		int currentOp=0;

		/**used to count up hints*/
		private int hintCount=0;

		/** I byte ops in CFF DIct table */
		private static String[] raw1ByteValues =
			{
				"version",
				"Notice",
				"FullName",
				"FamilyName",
				"Weight",
				"FontBBox",
				"BlueValues",
				"OtherBlues",
				"FamilyBlues",
				"FamilyOtherBlues",
				"StdHW",
				"StdVW",
				"escape",
				"UniqueID",
				"XUID",
				"charset",
				"Encoding",
				"CharStrings",
				"Private",
				"Subrs",
				"defaultWidthX",
				"nominalWidthX",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"intint",
				"longint",
				"BCD",
				"-Reserved-" };

		/** 2 byte ops in CFF DIct table */
		private static String[] raw2ByteValues =
			{
				"Copyright",
				"isFixedPitch",
				"ItalicAngle",
				"UnderlinePosition",
				"UnderlineThickness",
				"PaintType",
				"CharstringType",
				"FontMatrix",
				"StrokeWidth",
				"BlueScale",
				"BlueShift",
				"BlueFuzz",
				"StemSnapH",
				"StemSnapV",
				"ForceBold",
				"-Reserved-",
				"-Reserved-",
				"LanguageGroup",
				"ExpansionFactor",
				"initialRandomSeed",
				"SyntheticBase",
				"PostScript",
				"BaseFontName",
				"BaseFontBlend",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"-Reserved-",
				"ROS",
				"CIDFontVersion",
				"CIDFontRevision",
				"CIDFontType",
				"CIDCount",
				"UIDBase",
				"FDArray",
				"FDSelect",
				"FontName" };





		/**used by t1 font renderer to ensure hsbw or sbw executed first*/
		private boolean allowAll=false;

		private double h;
	private boolean isCID;

	public T1Glyphs(boolean isCID) {

		this.isCID=isCID;
	}

    public T1Glyphs() {
       
    }

        /**
     * return name of font
     * NAME will be LOWERCASE to avoid issues of capitalisation
     * when used for lookup - if no name, will default to  null
     *
     * @mode is PdfDecoder.SUBSTITUTE_* CONSTANT. RuntimeException will be thrown on invalid value
     */
    public static String[] readFontNames(FontData fontData,int mode) {

        String[] fontNames=new String[1];
        fontNames[0]=null;

        BufferedReader br =new BufferedReader(new StringReader(new String(fontData.getBytes(0,fontData.length()))));

        String line=null;

        while (true) {

            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (line == null)
                break;

            if (line.startsWith("/FontName")){
                int nameStart=line.indexOf("/",9);
                if(nameStart!=-1){
                    int nameEnd=line.indexOf(" ", nameStart);
                    if(nameEnd!=-1){
                        String name=line.substring(nameStart+1,nameEnd);
                        fontNames[0]=name.toLowerCase();
                        break;
                    }
                }
            }
        }

        if(br!=null){
            try{
                br.close();
            }catch (Exception e) {
                LogWriter.writeLog(
                        "Exception " + e + " closing stream");
            }
        }
        
        if(fontData!=null)
        	fontData.close();


        return fontNames;
    }

    /**
 * @param factory
 * @param debug
 * @param lastKey
 * @param isFlex
 * @param routine
 */
private boolean processFlex(GlyphFactory factory, boolean debug, int lastKey, boolean isFlex, int routine) {

	//if in flex feature see if we have all values - exit if not
	if((isFlex)&&(ptCount==14)&&(routine==0)){
		isFlex=false;
		for(int i=0;i<12;i=i+6){
			factory.curveTo(pt[i],pt[i+1],pt[i+2],pt[i+3],pt[i+4],pt[i+5]);
			if(debug)
				System.out.println("t1 flex "+pt[i]+ ' ' +pt[i+1]+ ' ' +pt[i+2]+ ' ' +pt[i+3]+ ' ' +pt[i+4]+ ' ' +pt[i+5]);
		}
	}else if((!isFlex)&&(routine>=0)&&(routine<=2)){ //determine if flex feature and enable
		isFlex=true;
		ptCount=0;
		pt=new float[16];
		if(debug)
			System.out.println("flex on "+lastKey+ ' ' +routine);
	}
	return isFlex;
}

		/**
		 * @param factory
		 * @param rawInt
		 * @param debug
		 * @param dicEnd
		 */
		private int endchar(GlyphFactory factory, int rawInt, boolean debug, int dicEnd) {
			int p;
			if(debug)
				System.out.println("Endchar");
			if(operandReached==5){ //allow for width and 4 chars
				operandReached--;
				currentOp++;
			}
			if(operandReached==4){
				endchar(factory, rawInt);
			}else
				factory.closePath();

			p =dicEnd;
			return p;
		}

		/**
		 * @param debug
		 * @param p
		 * @param lastKey
		 */
		private int mask(boolean debug, int p, int lastKey) {
			if(debug)
				System.out.println("hintmask/cntrmask "+lastKey);

			//if((lastKey==18)||(lastKey==1)||(lastKey==8))
				hintCount+=operandReached/2;

			if(debug)
				System.out.println("hintCount="+hintCount);

			int count=hintCount;
			while(count>0){
				p++;
				count=count-8;
			}
			return p;
		}

		/**
		 * @param debug
		 */
		private double sbw(boolean debug) {

			double yy;

			double val=operandsRead[operandReached-2];
			y=val;

			val=operandsRead[operandReached-1];
			x=val;

			xs=x;
			ys=y;
			allowAll=true;
			yy=y;

			h=operandsRead[operandReached-3];

			if(debug)
				System.out.println("sbw xs,ys set to "+x+ ' ' +y);
			return yy;
		}

		/**
		 * @param factory
		 * @param debug
		 * @param isFirst
		 */
		private void hmoveto(GlyphFactory factory, boolean debug, boolean isFirst) {
			if((isFirst)&&(operandReached==2))
				currentOp++;

			double val=operandsRead[currentOp];
			x=x+val;
			factory.moveTo((float)x,(float)y);

			//if((xs==-1)|(!pointDrawn)){
				xs=x;
				ys=y;

				if(debug)
					System.out.println("reset xs,ys to "+x+ ' ' +y);

			//}

			if(debug)
				System.out.println("hmoveto "+x+ ' ' +y);
		}

		/**
		 * @param factory
		 * @param debug
		 * @param isFirst
		 */
		private void rmoveto(GlyphFactory factory, boolean debug, boolean isFirst) {
			if((isFirst)&&(operandReached==3))
				currentOp++;

			if(debug)
				System.out.println(currentOp+" "+operandReached+ ' ' +isFirst+" x,y=("+x+ ' ' +y+") xs,ys=("+xs+ ' ' +ys+") rmoveto "+operandsRead[currentOp]+ ' ' +operandsRead[currentOp+1]);

			double val=operandsRead[currentOp+1];
			y=y+val;
			val=operandsRead[currentOp];
			x=x+val;

			factory.moveTo((float)x,(float)y);
			//if(xs==-1){
				xs=x;
				ys=y;

				if(debug)
					System.out.println("xs,ys=("+xs+ ' ' +ys+") x="+x+" y="+y);
			//}
		}

		/**
		 * @param factory
		 * @param debug
		 * @param key
		 *
		 */
		private void vhhvcurveto(GlyphFactory factory, boolean debug, int key) {
			boolean  isHor=(key==31);
			while ( operandReached >= 4 ){
				operandReached -= 4;
				if ( isHor )
					x += operandsRead[currentOp];
				else
					y += operandsRead[currentOp];
				pt[0]=(float) x;
				pt[1]=(float) y;
				x += operandsRead[currentOp+1];
				y += operandsRead[currentOp+2];
				pt[2]=(float) x;
				pt[3]=(float) y;
				if ( isHor ){
					y += operandsRead[currentOp+3];
					if ( operandReached ==1 )
						x += operandsRead[currentOp+4];
				}else{
					x += operandsRead[currentOp+3];
					if ( operandReached == 1 )
						y += operandsRead[currentOp+4];
				}
				pt[4]=(float) x;
				pt[5]=(float) y;
				factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

				if(debug)
					System.out.println(currentOp+"vh/hvCurveto "+operandsRead[currentOp]+ ' ' +operandsRead[currentOp+1]+ ' ' +operandsRead[currentOp+2]+ ' ' +operandsRead[currentOp+3]+ ' ' +operandsRead[currentOp+4]+ ' ' +operandsRead[currentOp+5]);

				currentOp  += 4;

				isHor = !isHor;
			}
		}

		/**
		 * @param factory
		 * @param debug
		 * @param key
		 */
		private void vvhhcurveto(GlyphFactory factory, boolean debug, int key) {
			
			boolean isVV=(key==26);
			if ( (operandReached & 1) ==1 ){
				if(isVV)
					x += operandsRead[0];
				else
					y += operandsRead[0];
				currentOp++;
			}

			//note odd co-ord order
			while (currentOp<operandReached ){
				if(isVV)
					y += operandsRead[currentOp];
				else
					x += operandsRead[currentOp];
				pt[0]=(float) x;
				pt[1]=(float) y;
				x += operandsRead[currentOp+1];
				y += operandsRead[currentOp+2];
				pt[2]=(float) x;
				pt[3]=(float) y;
				if(isVV)
					y += operandsRead[currentOp+3];
				else
					x += operandsRead[currentOp+3];
				pt[4]=(float) x;
				pt[5]=(float) y;
				currentOp += 4;
				factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

				if(debug)
					System.out.println("vv/hhCurveto "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);


			}
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void rlinecurve(GlyphFactory factory, boolean debug) {
			//lines
			int lineCount=( operandReached - 6 ) / 2;
			while ( lineCount > 0 ){
				x += operandsRead[currentOp];
				y += operandsRead[currentOp+1];
				factory.lineTo((float)x,(float)y);

				if(debug)
					System.out.println("rlineCurve "+operandsRead[0]+ ' ' +operandsRead[1]);

				currentOp += 2;
				lineCount--;
			}
			//curves
			float[] coords=new float[6];
			x += operandsRead[currentOp];
			y += operandsRead[currentOp+1];
			coords[0]=(float) x;
			coords[1]=(float) y;

			x += operandsRead[currentOp+2];
			y += operandsRead[currentOp+3];
			coords[2]=(float) x;
			coords[3]=(float) y;

			x += operandsRead[currentOp+4];
			y += operandsRead[currentOp+5];
			coords[4]=(float) x;
			coords[5]=(float) y;

			factory.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);

			if(debug)
				System.out.println("rlineCurve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);
			currentOp += 6;
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void closepath(GlyphFactory factory, boolean debug) {
			if(xs!=-1)
				factory.lineTo((float)xs,(float)ys);

			if(debug)
				System.out.println("close to xs="+xs+" ys="+ys+ ' ' +x+ ',' +y);

			xs=-1; //flag as unset

		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void hsbw(GlyphFactory factory, boolean debug) {
			x=x+operandsRead[0];
			factory.moveTo((float)x,0);
			if(debug)
				System.out.println("hsbw "+x+" xs,ys="+xs+ ' ' +ys);
			allowAll=true;
		}

		/**
		 * @param debug
		 */
		private void pop(boolean debug) {

			//for(int ii=1;ii<count-1;ii++){
			//operandsRead[ii-1]=operandsRead[ii];
			//}
			if(operandReached>0)
			operandReached--;
			if(debug)
				System.out.println("POP");

			if(debug){
				for(int i=0;i<6;i++)
					System.out.println(i+" == "+operandsRead[i]+ ' ' +operandReached);
			}
		}

		/**
		 * @param debug
		 */
		private void setcurrentpoint(boolean debug) {
			//x=operandsRead[0];
			 //y=operandsRead[1];
			 //factory.moveTo((float)operandsRead[0],(float)operandsRead[1]);
			 if(debug)
			 System.out.println("setCurrentPoint "+operandsRead[0]+ ' ' +operandsRead[1]);
		}

		/**
		 * @param debug
		 */
		private void div(boolean debug) {
			if(debug){
				for(int i=0;i<6;i++)
				System.out.println(i+" "+currentOp+ ' ' +operandsRead[i]+ ' ' +operandReached);
			}
			double value=operandsRead[operandReached-2]/operandsRead[operandReached-1];

			//operandReached--;
			if(operandReached>0)
			operandReached--;
			operandsRead[operandReached-1]=value;

			if(debug){
				for(int i=0;i<6;i++)
					System.out.println("after===="+i+" == "+operandsRead[i]+ ' ' +operandReached);
			}
			if(debug)
				System.out.println("DIV");
		}

		/**
		 * @param factory
		 * @param debug
		 * @param isFirst
		 */
		private void vmoveto(GlyphFactory factory, boolean debug, boolean isFirst) {
			if((isFirst)&&(operandReached==2))
				currentOp++;
			y=y+operandsRead[currentOp];
			factory.moveTo((float)x,(float)y);

			//if((xs==-1)){
				xs=x;
				ys=y;

				if(debug)
					System.out.println("Set xs,ys= "+xs+ ' ' +ys);
			//}

			if(debug)
				System.out.println("vmoveto "+operandsRead[0]+ ' ' +operandsRead[1]+" currentOp"+currentOp+" y="+y+ ' ' +isFirst);
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void rlineto(GlyphFactory factory, boolean debug) {
			int lineCount=operandReached/2;
			while ( lineCount > 0 ){
				x += operandsRead[currentOp];
				y += operandsRead[currentOp+1];
				factory.lineTo((float)x,(float)y);
				currentOp += 2;
				lineCount--;

				if(debug)
					System.out.println("x,y= ("+x+ ' ' +y+") rlineto "+operandsRead[0]+ ' ' +operandsRead[1]);

			}
		}

		/**
		 * @param factory
		 * @param debug
		 * @param key
		 */
		private void hvlineto(GlyphFactory factory, boolean debug, int key) {
			boolean isHor = ( key==6 );
			int start=0;
			while (start<operandReached ){
				if ( isHor )
					x += operandsRead[start];
				else
					y += operandsRead[start];
				factory.lineTo((float)x,(float)y);

				if(debug)
					System.out.println("h/vlineto "+operandsRead[0]+ ' ' +operandsRead[1]);

				start++;
				isHor =!isHor;
			}
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void rrcurveto(GlyphFactory factory, boolean debug) {
			
			
			int  curveCount = ( operandReached  ) / 6;
			
			if(debug && curveCount>1)
				System.out.println("**********currentOp="+currentOp+" curves="+curveCount);
			
			while ( curveCount > 0 ){
				float[] coords=new float[6];
				x += operandsRead[currentOp];
				y += operandsRead[currentOp+1];
				coords[0]=(float) x;
				coords[1]=(float) y;

				x += operandsRead[currentOp+2];
				y += operandsRead[currentOp+3];
				coords[2]=(float) x;
				coords[3]=(float) y;

				x += operandsRead[currentOp+4];
				y += operandsRead[currentOp+5];
				coords[4]=(float) x;
				coords[5]=(float) y;

				factory.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);

				//if(debug)
				//	System.out.println("now="+x+" "+y);
				if(debug)
					System.out.println("rrcurveto "+operandsRead[currentOp]+ ' ' +
							operandsRead[currentOp+1]+ ' ' +operandsRead[currentOp+2]+
                            ' ' +operandsRead[currentOp+3]+ ' ' +operandsRead[currentOp+4]+
                            ' ' +operandsRead[currentOp+5]);
				currentOp += 6;
				curveCount--;
			}
		}

		/**
		 * @param factory
		 * @param rawInt
		 */
		private void endchar(GlyphFactory factory, int rawInt) {
			StandardFonts.checkLoaded(StandardFonts.STD);
			float adx=(float)(x+operandsRead[currentOp]);
			float ady=(float)(y+operandsRead[currentOp+1]);
			String bchar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+2]);
			String achar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+3]);

			x=0;
			y=0;
			decodeGlyph(null,factory,bchar,rawInt, "", 0,true);
			factory.closePath();
			factory.moveTo(adx,ady);
			x=adx;
			y=ady;
			decodeGlyph(null,factory,achar,rawInt, "", 0,true);

			if(xs==-1){
				xs=x;
				ys=y;

					System.out.println("ENDCHAR Set xs,ys= "+xs+ ' ' +ys);
			}
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void rcurveline(GlyphFactory factory, boolean debug) {
			//curves
			int  curveCount=( operandReached - 2 ) / 6;
			while ( curveCount > 0 ){
				float[] coords=new float[6];
				x += operandsRead[currentOp];
				y += operandsRead[currentOp+1];
				coords[0]=(float) x;
				coords[1]=(float) y;

				x += operandsRead[currentOp+2];
				y += operandsRead[currentOp+3];
				coords[2]=(float) x;
				coords[3]=(float) y;

				x += operandsRead[currentOp+4];
				y += operandsRead[currentOp+5];
				coords[4]=(float) x;
				coords[5]=(float) y;

				factory.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);

				if(debug)
					System.out.println("rCurveline "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);
				currentOp += 6;
				curveCount--;
			}

			// line
			x += operandsRead[currentOp];
			y += operandsRead[currentOp+1];
			factory.lineTo((float)x,(float)y);
			currentOp += 2;

			if(debug)
				System.out.println("rCurveline "+operandsRead[0]+ ' ' +operandsRead[1]);
		}

		/**
		 * @param factory
		 * @param rawInt
		 * @param currentOp
		 */
		private void seac(GlyphFactory factory, int rawInt, int currentOp) {
			StandardFonts.checkLoaded(StandardFonts.STD);
			float adx=(float)(operandsRead[currentOp+1]);
			float ady=(float)(operandsRead[currentOp+2]);
			String bchar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+3]);
			String achar=StandardFonts.getUnicodeChar(StandardFonts.STD ,(int)operandsRead[currentOp+4]);
			x=0;
			y=0;
			decodeGlyph(null,factory,bchar,rawInt, "", 0,true);

			factory.closePath();
			factory.moveTo(0,0);
			x=adx;
			y=ady;
			decodeGlyph(null,factory,achar,rawInt, "", 0,true);
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void flex1(GlyphFactory factory, boolean debug) {
			double   dx = 0, dy = 0,x1=x, y1=y;

			/*workout dx/dy/horizontal and reset flag*/
			for ( int count =0; count <10; count=count+2 ){
				dx += operandsRead[count];
				dy += operandsRead[count+1];
			}
			boolean isHorizontal=(Math.abs(dx)>Math.abs(dy));

			for(int points=0;points<6;points=points+2){//first curve
				x += operandsRead[points];
				y += operandsRead[points+1];
				pt[points]=(float) x;
				pt[points+1]=(float) y;
			}
			factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
											if(debug)
				System.out.println("flex1 first curve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);

			for(int points=0;points<4;points=points+2){//second curve
				x += operandsRead[points+6];
				y += operandsRead[points+7];
				pt[points]=(float) x;
				pt[points+1]=(float) y;
			}

			if ( isHorizontal ){ // last point
				x += operandsRead[10];
				y  = y1;
			}else{
				x  = x1;
				y += operandsRead[10];
			}
			pt[4]=(float) x;
			pt[5]=(float) y;
			factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);
			if(debug)
				System.out.println("flex1 second curve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void flex(GlyphFactory factory, boolean debug) {
			for(int curves=0;curves<12;curves=curves+6){
				for(int points=0;points<6;points=points+2){
					x += operandsRead[curves+points];
					y += operandsRead[curves+points+1];
					pt[points]=(float) x;
					pt[points+1]=(float) y;
				}
				factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

				if(debug)
					System.out.println("flex "+pt[0]+ ' ' +pt[1]+ ' ' +pt[2]+ ' ' +pt[3]+ ' ' +pt[4]+ ' ' +pt[5]);

			}
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void hflex(GlyphFactory factory, boolean debug) {
			//first curve
			x += operandsRead[0];
			pt[0]=(float) x;
			pt[1]=(float) y;
			x += operandsRead[1];
			y += operandsRead[2];
			pt[2]=(float) x;
			pt[3]=(float) y;
			x += operandsRead[3];
			pt[4]=(float) x;
			pt[5]=(float) y;
			factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

			if(debug)
				System.out.println("hflex first curve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);

			//second curve
			x += operandsRead[4];
			pt[0]=(float) x;
			pt[1]=(float) y;
			x += operandsRead[5];
			pt[2]=(float) x;
			pt[3]=(float) y;
			x += operandsRead[6];
			pt[4]=(float) x;
			pt[5]=(float) y;
			factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

			if(debug)
				System.out.println("hflex second curve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);
		}

		/**
		 * @param factory
		 * @param debug
		 */
		private void hflex1(GlyphFactory factory, boolean debug) {
			//first curve
			x+=operandsRead[0];
			y+=operandsRead[1];
			pt[0]=(float) x;
			pt[1]=(float) y;
			x+=operandsRead[2];
			y+=operandsRead[3];
			pt[2]=(float) x;
			pt[3]=(float) y;
			x+=operandsRead[4];
			pt[4]=(float) x;
			pt[5]=(float) y;
			factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

			if(debug)
				System.out.println("36 first curve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);

			//second curve
			x+=operandsRead[5];
			pt[0]=(float) x;
			pt[1]=(float) y;
			x+=operandsRead[6];
			y+=operandsRead[7];
			pt[2]=(float) x;
			pt[3]=(float) y;
			x += operandsRead[8];
			pt[4]=(float) x;
			pt[5]=(float) y;
			factory.curveTo(pt[0],pt[1],pt[2],pt[3],pt[4],pt[5]);

			if(debug)
				System.out.println("36 second curve "+operandsRead[0]+ ' ' +operandsRead[1]+ ' ' +operandsRead[2]+ ' ' +operandsRead[3]+ ' ' +operandsRead[4]+ ' ' +operandsRead[5]);
		}


	/**
		 * used by  non type3 font
		 */
		public PdfGlyph getEmbeddedGlyph(GlyphFactory factory, String glyph, float[][]Trm, int rawInt, 
				String displayValue, float currentWidth, String key) {


			/**flush cache if needed*/
			if((lastTrm[0][0]!=Trm[0][0])|(lastTrm[1][0]!=Trm[1][0])|
					(lastTrm[0][1]!=Trm[0][1])|(lastTrm[1][1]!=Trm[1][1])){
				lastTrm=Trm;
				flush();
			}

			//either calculate the glyph to draw or reuse if alreasy drawn
			PdfGlyph transformedGlyph2 = getEmbeddedCachedShape(rawInt);

			if (transformedGlyph2 == null) {

				/**create new stack for glyph*/
				operandsRead = new double[max];
				operandReached=0;

				x=-factory.getLSB();
              
                y=0;
				decodeGlyph(key,factory,glyph,rawInt, displayValue,currentWidth,false);

                //generate Glyph
                transformedGlyph2=factory.getGlyph(false);

				//save so we can reuse if it occurs again in this TJ command
				setEmbeddedCachedShape(rawInt, transformedGlyph2);
			}

            return transformedGlyph2;
		}

		/** Utility method used during processing of type1C files */
		final public int getNumber(byte[] fontDataAsArray, int pos,double[] values,int valuePointer,boolean debug) {

			int b0, i;
			double x = 0;

			b0 = fontDataAsArray[pos] & 0xFF;

			if ((b0 < 28) | (b0 == 31)) { //error!
				System.err.println("!!!!Incorrect type1C operand");
			} else if (b0 == 28) { //2 byte number in range -32768
												  // +32767
				x = (fontDataAsArray[pos + 1] << 8) + (fontDataAsArray[pos + 2] & 0xff);
				pos += 3;
			}else if(b0==255){

				if(is1C){
					int top=((fontDataAsArray[pos + 1] & 0xFF)<<8)+(fontDataAsArray[pos + 2] & 0xFF);
					if(top>32768)
						top=65536-top;
					double numb =top;
					double dec =((fontDataAsArray[pos + 3] & 0xFF) << 8)+ (fontDataAsArray[pos + 4] & 0xFF);
					x=numb+(dec/65536);
					if(fontDataAsArray[pos + 1]<0){
						if(debug)
							System.out.println("Negative "+x);
						x=-x;

					}

					if(debug){
						System.out.println("x="+x);

						for(int j=0;j<5;j++){
							System.out.println(j+" "+ fontDataAsArray[pos+j]+ ' ' +
                                    (fontDataAsArray[pos+j] & 0xff)+ ' ' +
                                    (fontDataAsArray[pos+j] & 0x7f));
						}
					}
				}else{
				//x=((content[pos + 1]& 127) << 24) + (content[pos + 2]<<16)+(content[pos + 3] << 8) + content[pos + 4];
				x =
					((fontDataAsArray[pos + 1] & 0xFF) << 24)
						+ ((fontDataAsArray[pos + 2] & 0xFF) << 16)
						+ ((fontDataAsArray[pos + 3] & 0xFF) << 8)
						+ (fontDataAsArray[pos + 4] & 0xFF);

				}

				pos+=5;
			} else if (b0 == 29) { //4 byte signed number
				x =
					((fontDataAsArray[pos + 1] & 0xFF) << 24)
						+ ((fontDataAsArray[pos + 2] & 0xFF) << 16)
						+ ((fontDataAsArray[pos + 3] & 0xFF) << 8)
						+ (fontDataAsArray[pos + 4] & 0xFF);
				pos += 5;
			} else if (b0 == 30) { //BCD values

				char buf[] = new char[65];
				pos += 1;
				i = 0;
				while (i < 64) {
					int b = fontDataAsArray[pos++] & 0xFF;

					int nyb0 = (b >> 4) & 0x0f;
					int nyb1 = b & 0x0f;

					if (nyb0 == 0xf)
						break;
					buf[i++] = nybChars.charAt(nyb0);
					if (i == 64)
						break;
					if (nyb0 == 0xc)
						buf[i++] = '-';
					if (i == 64)
						break;
					if (nyb1 == 0xf)
						break;
					buf[i++] = nybChars.charAt(nyb1);
					if (i == 64)
						break;
					if (nyb1 == 0xc)
						buf[i++] = '-';
				}
				x = (Double.valueOf(new String(buf, 0, i))).doubleValue();

			} else if (b0 < 247) { //-107 +107
				x = b0 - 139;
				pos++;
			} else if (b0 < 251) { //2 bytes +108 +1131
				x = ((b0 - 247) << 8) + (fontDataAsArray[pos + 1] & 0xff) + 108;
				pos += 2;
			} else { //-1131 -108
				x = - ((b0 - 251) << 8) - (fontDataAsArray[pos + 1] & 0xff) - 108;
				pos += 2;
			}

			//assign number
			values[valuePointer]=x;
			
			//if(debug)
			//	System.out.println("Number ="+x);
			return pos;
		}
		
		/** Utility method used during processing of type1C files */
		final public int getNumber(FontData fontDataAsObject, int pos,double[] values,int valuePointer,boolean debug) {

			int b0, i;
			double x = 0;

			b0 = fontDataAsObject.getByte(pos) & 0xFF;

			if ((b0 < 28) | (b0 == 31)) { //error!
				System.err.println("!!!!Incorrect type1C operand");
			} else if (b0 == 28) { //2 byte number in range -32768
												  // +32767
				x = (fontDataAsObject.getByte(pos + 1) << 8) + (fontDataAsObject.getByte(pos + 2) & 0xff);
				pos += 3;
			}else if(b0==255){

				if(is1C){
					int top=((fontDataAsObject.getByte(pos + 1) & 0xFF)<<8)+(fontDataAsObject.getByte(pos + 2) & 0xFF);
					if(top>32768)
						top=65536-top;
					double numb =top;
					double dec =((fontDataAsObject.getByte(pos + 3) & 0xFF) << 8)+ (fontDataAsObject.getByte(pos + 4) & 0xFF);
					x=numb+(dec/65536);
					if(fontDataAsObject.getByte(pos + 1)<0){
						if(debug)
							System.out.println("Negative "+x);
						x=-x;

					}

					if(debug){
						System.out.println("x="+x);

						for(int j=0;j<5;j++){
							System.out.println(j+" "+ fontDataAsObject.getByte(pos+j)+ ' ' +
                                    (fontDataAsObject.getByte(pos+j) & 0xff)+ ' ' +
                                    (fontDataAsObject.getByte(pos+j) & 0x7f));
						}
					}
				}else{
				//x=((content[pos + 1]& 127) << 24) + (content[pos + 2]<<16)+(content[pos + 3] << 8) + content[pos + 4];
				x =
					((fontDataAsObject.getByte(pos + 1) & 0xFF) << 24)
						+ ((fontDataAsObject.getByte(pos + 2) & 0xFF) << 16)
						+ ((fontDataAsObject.getByte(pos + 3) & 0xFF) << 8)
						+ (fontDataAsObject.getByte(pos + 4) & 0xFF);

				}

				pos+=5;
			} else if (b0 == 29) { //4 byte signed number
				x =
					((fontDataAsObject.getByte(pos + 1) & 0xFF) << 24)
						+ ((fontDataAsObject.getByte(pos + 2) & 0xFF) << 16)
						+ ((fontDataAsObject.getByte(pos + 3) & 0xFF) << 8)
						+ (fontDataAsObject.getByte(pos + 4) & 0xFF);
				pos += 5;
			} else if (b0 == 30) { //BCD values

				char buf[] = new char[65];
				pos += 1;
				i = 0;
				while (i < 64) {
					int b = fontDataAsObject.getByte(pos++) & 0xFF;

					int nyb0 = (b >> 4) & 0x0f;
					int nyb1 = b & 0x0f;

					if (nyb0 == 0xf)
						break;
					buf[i++] = nybChars.charAt(nyb0);
					if (i == 64)
						break;
					if (nyb0 == 0xc)
						buf[i++] = '-';
					if (i == 64)
						break;
					if (nyb1 == 0xf)
						break;
					buf[i++] = nybChars.charAt(nyb1);
					if (i == 64)
						break;
					if (nyb1 == 0xc)
						buf[i++] = '-';
				}
				x = (Double.valueOf(new String(buf, 0, i))).doubleValue();

			} else if (b0 < 247) { //-107 +107
				x = b0 - 139;
				pos++;
			} else if (b0 < 251) { //2 bytes +108 +1131
				x = ((b0 - 247) << 8) + (fontDataAsObject.getByte(pos + 1) & 0xff) + 108;
				pos += 2;
			} else { //-1131 -108
				x = - ((b0 - 251) << 8) - (fontDataAsObject.getByte(pos + 1) & 0xff) - 108;
				pos += 2;
			}

			//assign number
			values[valuePointer]=x;
			
			//if(debug)
			//	System.out.println("Number ="+x);
			return pos;
		}


/*
		 * creates glyph from type1c font commands
		 */
	protected void decodeGlyph(String embKey,GlyphFactory factory,String glyph,int rawInt, String displayValue, float currentWidth,boolean isRecursive) {

		byte[]  glyphStream=null;

		boolean debug=false;//rawInt==1445;//rawInt==40 || rawInt==105 || rawInt==109;

		//System.out.println(glyph+" "+baseFontName+" "+rawInt+" "+currentWidth);
		
		allowAll=false; //used by T1 to make sure sbw of hsbw

		/**
		if((this.baseFontName.indexOf("YYSITY+Aybabtu-Regular")!=-1)){
			debug=rawInt==0;
			if(displayValue.equals("aaaaaa"))
				debug=true;
			//else
			//	debug=false;
		//	System.out.println(isCID+" "+glyph+" "+baseFontName+" "+currentWidth+" "+displayValue+" "+rawInt);
		}/***/

		/**
		 * get the stream of commands for the glyph
		 */
		if(isCID){
			glyphStream=(byte[]) charStrings.get(String.valueOf(rawInt));
		}else{
			if(glyph==null)
				glyph=displayValue;//getMappedChar(rawInt,false);


			if(glyph==null){
				glyph=embKey;

				if(glyph==null)
					glyph=".notdef";
			}
			
			/**
			 * get the bytestream of commands and reset global values
			 */
			glyphStream=(byte[]) charStrings.get(glyph);

			if(glyphStream==null){
				
				if(embKey!=null)
					glyphStream=(byte[]) charStrings.get(embKey);
				if(glyphStream==null)
					glyphStream=(byte[]) charStrings.get(".notdef");
			}
		}

		/**
		 * if valid stream then decode
		 */
		if(glyphStream!=null){

			boolean isFirst=true; //flag to pick up extra possible first value
			boolean isNumber=false;
			ptCount=0;
			int commandCount=-1; //command number
			int p = 0,lastNumberStart=0, nextVal,key=0,lastKey,dicEnd=glyphStream.length,lastVal=0;
			currentOp=0;
			hintCount=0;
			double ymin=999999,ymax=0,yy=1000;
			boolean isFlex=false; //flag to show its a flex command in t1
			pt=new float[6];

			h=100000;
			/**set length for 1C*/
			if(is1C){
				operandsRead=new double[max];
				operandReached=0;
				allowAll=true;

			}

			if((debug)){
				System.out.println("******************"+ ' ' +displayValue+ ' ' +glyph);
				for(int j=0;j<dicEnd;j++)
					System.out.println(j+" "+(glyphStream[j] & 0xff));

				System.out.println("=====");
			}

			/**
			 * work through the commands decoding and extracting numbers (operands are FIRST)
			 */

			while (p < dicEnd) {

				//get next byte value from stream
				nextVal = glyphStream[p] & 0xFF;

				//if(debug)
				//System.out.println("p="+p+">>>"+nextVal+" operandReached="+operandReached+" currentOp="+currentOp+" x="+x+" y="+y);
				
				if (nextVal >31 || nextVal==28) {  //if its a number get it and update pointer p

					//track location
					lastNumberStart=p;
					
					isNumber=true;
					p= getNumber(glyphStream, p,operandsRead,operandReached,debug);
					lastVal=(int) operandsRead[operandReached];//nextVal;
					operandReached++;
					
					if(nextVal==28 && debug)
						System.out.println("Shortint "+lastVal);

				}else{  // operator
					
					commandCount++;
					isNumber=false;
					lastKey=key;
					key = nextVal;
					p++;
					currentOp=0;

					if (key ==12) { //handle escaped keys (ie 2 byte ops)
						key= glyphStream[p] & 0xFF;
						p++;

						if(key==7){ //sbw
							yy = sbw(debug);
							operandReached=0; //move to first operator
						}else if(allowAll){ //other 2 byte operands
							if(key==16){ //other subroutine
								isFlex = processFlex(factory, debug, lastKey, isFlex, lastVal);
								operandReached=0; //move to first operator
							}else if(key==33){ //setcurrentpoint
								setcurrentpoint(debug);
								operandReached=0; //move to first operator
							}else  if(key==34){ //hflex
								hflex(factory, debug);
								operandReached=0; //move to first operator
							}else if(key==35){ //fle
								flex(factory, debug);
								operandReached=0; //move to first operator
							}else if(key==36){ //hflex1
								hflex1(factory, debug);
								operandReached=0; //move to first operator
							}else if(key==37){ //flex1
								flex1(factory, debug);
								operandReached=0; //move to first operator
							}else if(key==6){ //seac
								seac(factory, rawInt, currentOp);
								operandReached=0; //move to first operator
							}else if(key==12){ //div functionn
								div(debug);
							}else if(key==17){ //POP function
								pop(debug);
							}else if(key==0){ //dotsection
								operandReached=0; //move to first operator
								if(debug)
									System.out.println("Dot section");
							}else  if(debug){
								operandReached=0; //move to first operator
								System.out.println("1 Not implemented "+p+" id="+key+" op="+Type1.T1C[key]);
							}else
								operandReached=0; //move to first operator
						}
					}else if(key==13){ //hsbw (T1 only)
						hsbw(factory, debug);
						operandReached=0; //move to first operator
					} else if(allowAll){ //other one byte ops
						if(key==0){ //reserved
						}else if((key==1)|(key==3)|(key==18)|(key==23)){ //hstem vstem hstemhm vstemhm
							hintCount+=operandReached/2;
							operandReached=0; //move to first operator
							if(debug)
								System.out.println("One of hstem vstem hstemhm vstemhm "+key+ ' ' +xs+ ' ' +ys);
						}else if(key==4){ //vmoveto
							if(isFlex){
								double val=operandsRead[currentOp];
								y=y+val;
								pt[ptCount]=(float) x;
								ptCount++;
								pt[ptCount]=(float) y;
								ptCount++;
								if(debug)
									System.out.println("flex value "+x+ ' ' +y);
							}else
								vmoveto(factory, debug, isFirst);
							operandReached=0; //move to first operator
						}else if((key==5)){//rlineto
							rlineto(factory, debug);
							operandReached=0; //move to first operator
						}else if((key==6)|(key==7)){//hlineto or vlineto
							hvlineto(factory, debug, key);
							operandReached=0; //move to first operator
						}else if(key==8){//rrcurveto
							rrcurveto(factory, debug);
							operandReached=0; //move to first operator
						}else if(key==9){ //closepath (T1 only)
							closepath(factory, debug);
							operandReached=0; //move to first operator
						}else if(key==10 || (key==29)){ //callsubr and callgsubr

							if(debug)
								System.out.println(key+" -------------- last Value="+lastVal+ ' ' +allowAll+" commandCount="+commandCount+" operandReached="+operandReached);
							
							if(key==10 && (lastVal>=0)&&(lastVal<=2) && lastKey!=11 && operandReached>5){//last key stops spurious match in multiple sub-routines
								isFlex = processFlex(factory, debug, lastKey, isFlex, lastVal);
								operandReached=0; //move to first operator

                            }else{
								
//								factor in bias
								if(key==10)
									lastVal=lastVal+localBias;
								else
									lastVal=lastVal+globalBias;
						
								byte[] newStream=null;
								if(key==10){ //local subroutine
									
									newStream=(byte[]) charStrings.get("subrs"+ (lastVal));
									
									if(debug)
										System.out.println("=================callsubr "+lastVal);
								}else{ //global subroutine
									if(debug)
										System.out.println("=================callgsubr "+lastVal);
									
									newStream=(byte[]) charStrings.get("global"+ (lastVal));
								}
								
								if(newStream!=null){
									
									if (debug)
										System.out.println("Subroutine============="+lastVal+" op="+currentOp+ ' ' +operandReached);
									
									/**LILYPONDTOOL
									//operandsRead=new double[dicEnd];
									
									/**/
									//lastVal=5; //reset lastVal
									
									int newLength=newStream.length;
									int oldLength=glyphStream.length;
									int totalLength=newLength+oldLength-2;
									
									dicEnd=dicEnd+newLength-2;
									//workout length of new stream
									byte[] combinedStream=new byte[totalLength];
									/**
									if(debug){
										System.out.println("Before-------------");
										for(int p2=p-2;p2<glyphStream.length;p2++){
											System.out.println(p2+" "+(glyphStream[p2] & 0xff));
										}
										
										System.out.println("New-------------");
										for(int p2=0;p2<newLength;p2++)
											System.out.println(p2+" "+(newStream[p2] & 0xff));
									}
									/**/
									System.arraycopy(glyphStream, 0, combinedStream, 0, lastNumberStart);
									System.arraycopy(newStream, 0, combinedStream, lastNumberStart, newLength);
									System.arraycopy(glyphStream, p, combinedStream, lastNumberStart+newLength, oldLength-p);
									
									glyphStream=combinedStream;
									
									p=lastNumberStart;
									
									if(operandReached>0)
									operandReached--;
									
									/**
									if(debug){
										//System.out.println("P now="+p);
										
										System.out.println("Merged-------------");
										for(int p2=0;p2<glyphStream.length;p2++){
											if(p2==lastNumberStart || p==(lastNumberStart+newLength))
												System.out.println("----");
											System.out.println(p2+" "+(glyphStream[p2] & 0xff));
										}
									}
									/**/
									
								}else if(debug)
									System.out.println("No data found for sub-routine "+charStrings);

								
							}
							//operandReached=0; //move to first operator
						}else if(key==11){ //return
							if(debug)
								System.out.println("return============="+p);
							//operandReached=0; //move to first operator
						}else if((key==14)){ //endchar
							p = endchar(factory, rawInt, debug, dicEnd);
							operandReached=0; //move to first operator
							p=dicEnd+1;
						}else if(key==16){ //blend
							if(debug)
								System.out.println("Blend");
							operandReached=0; //move to first operator
						}else if((key==19)|(key==20)){ //hintmask //cntrmask
							p = mask(debug, p, lastKey);
							operandReached=0; //move to first operator
						}else if(key==21){//rmoveto
							if(isFlex){
								if(debug)
								System.out.println(currentOp+" "+ptCount+ ' ' +pt.length);
								double val=operandsRead[currentOp+1];
								y=y+val;
								val=operandsRead[currentOp];
								x=x+val;
								pt[ptCount]=(float) x;
								ptCount++;

								pt[ptCount]=(float) y;
								ptCount++;
								if(debug)
									System.out.println("flex value "+pt[ptCount-2]+ ' ' +pt[ptCount-1]+" count="+ptCount);

							}else
								rmoveto(factory, debug, isFirst);
							operandReached=0; //move to first operator
						}else if(key==22){ //hmoveto
							if(isFlex){
								double val=operandsRead[currentOp];
								x=x+val;
								pt[ptCount]=(float) x;
								ptCount++;
								pt[ptCount]=(float) y;
								ptCount++;
								if(debug)
									System.out.println("flex value "+x+ ' ' +y);
							}else
								hmoveto(factory, debug, isFirst);
							operandReached=0; //move to first operator
						}else if(key==24){ //rcurveline
							rcurveline(factory, debug);
							operandReached=0; //move to first operator
						}else if(key==25){ //rlinecurve
							rlinecurve(factory, debug);
							operandReached=0; //move to first operator
						}else if((key==26)|(key==27)){ //vvcurve hhcurveto
							vvhhcurveto(factory, debug, key);
							operandReached=0; //move to first operator
						}else if((key==30)|(key==31)){	//vhcurveto/hvcurveto
							vhhvcurveto(factory, debug, key);
							operandReached=0; //move to first operator
						}else if(debug){ //unknown command
							operandReached=0; //move to first operator

								System.out.println("Unsupported command "+p+">>>>>"+hintCount+">>>>>>key="+key+ ' ' +Type1.T1CcharCodes1Byte[key]+" <1<<"+operandsRead);
								for(int j=0;j<dicEnd;j++)
									System.out.println(j+" "+(glyphStream[j] & 0xff));

						}

						/**/
						if(debug && !isNumber){
							BufferedImage img=new BufferedImage(600,600,BufferedImage.TYPE_INT_ARGB);
							Graphics2D g2=img.createGraphics();
							g2.setColor(Color.red);
							AffineTransform af=new AffineTransform();
							af.scale(0.5,0.5);
							af.translate(0,340);
							g2.transform(af);
							
							for(int ii=0;ii<7;ii++)
							g2.drawLine(ii*100,0,ii*100,1000);
							PdfGlyph transformedGlyph2=factory.getGlyph(true);
							transformedGlyph2.render(GraphicsState.STROKE,g2, 1f);

							//if(p>128)
								ShowGUIMessage.showGUIMessage(p+" x "+" x,y="+x+ ' ' +y,img,p+" x ");
						}/***/
					}

					if(ymin>y)
						ymin=y;

					if(ymax<y)
						ymax=y;

					if(key!=19)
					isFirst=false;
				}
			}

			if(yy>h)
				ymin=yy-h;

			//System.out.println("raw ---ymin="+ymin+" yy="+yy+" ymax="+ymax+" "+glyph+" "+rawInt);

			if((ymax)<yy){
				ymin=0;

			}else if((yy==ymax)){//added for M2003W.pdf font display
				//if(ymin<0)
				//	ymin=270;
			}else{

				float dy=(float) (ymax-(yy-ymin));

				//System.out.println(dy+" "+ymin+" "+yy+" "+ymax+" "+(yy-ymax));
				if((dy<0)){

					if(yy-ymax<=dy)
						ymin=dy;
					else
						ymin=ymin-dy;
					//}
				}else
					ymin=0;

                if(ymin<0)
                ymin=0;
//                 if(glyph.indexOf("325")!=-1){
//                     ymin=0;
//            System.out.println(glyph+" "+ymin+" "+ymax+" "+dy);
//            }
                //if((ymin<0)&&((ymax-(yy-ymin)<0))){
					//if(ymin>yy)
					//	ymin=yy-ymin;
					//else
					//if(ymax>yy)
					//ymin=yy-ymax;
				//debug=true;
				//}
			}

			/**set values to adjust glyph vertically*/

            factory.setYMin((float)(ymin),(float) ymax);

			/**/
			if((debug)&(!isRecursive)){
				BufferedImage img=new BufferedImage(600,600,BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2=img.createGraphics();
				g2.setColor(Color.red);
				AffineTransform af=new AffineTransform();
				af.scale(0.25,0.25);
				//af.translate(0,30);
				g2.transform(af);

				PdfGlyph transformedGlyph2=factory.getGlyph(true);

				g2.setColor(Color.green);
				for(int j=0;j<7;j++)
				g2.drawLine(0,j*50,1000,j*50);
				transformedGlyph2.render(GraphicsState.STROKE,g2, 1f);
				//if(p>160)
				//ShowGUIMessage.showGUIMessage("Completed "+p+" x "+ii+" x,y="+x+" "+y,img,p+" x "+ii);
				//System.out.println(ii+"/"+p+" start="+xs+" "+ys);
			}/***/

		}
	}

	/**add a charString value*/
	 public void setCharString(String glyph,byte[] stream){
		 charStrings.put(glyph,stream);
	 }

	public boolean is1C() {
		return is1C;
	}



	public void setis1C(boolean is1C) {

		this.is1C=is1C;
	}

}
