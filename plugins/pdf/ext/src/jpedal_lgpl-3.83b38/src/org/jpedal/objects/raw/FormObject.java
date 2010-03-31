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
* FormObject.java
* ---------------
*/
package org.jpedal.objects.raw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.PdfDecoder;
import org.jpedal.color.DeviceCMYKColorSpace;
//<start-adobe><start-thin>
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
//<end-thin><end-adobe>
import org.jpedal.external.Options;
import org.jpedal.io.PdfReader;

import org.jpedal.objects.Javascript;

import org.jpedal.objects.acroforms.actions.ActionHandler;

import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.objects.raw.FormStream;

import javax.swing.*;

public class FormObject extends PdfObject{

	//unknown CMAP as String
	private String Filter=null, Location=null, M, Reason, SubFilter;
	private byte[] rawFilter, rawLocation, rawM,rawReason, rawSubFilter;

    /** transparant image for use when appearance stream is null */
    final private static BufferedImage OpaqueImage = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);

    /**
     * the C color for annotations
     */
    private Color cColor;
    /**
     * the contents for any text display on the annotation
     */
    private String contents;
    /**
     * whether the annotation is being displayed or not by default
     */
    private boolean show = false;

    private String stateTocheck = "";

    private Map OptValues=null; // values from Opt

    private boolean popupBuilt = false;

    private Object popupObj;

    /*
     * form flag values for the field flags
     */
    public static final int READONLY_ID = 1;
    public static final int REQUIRED_ID = 2;
    public static final int NOEXPORT_ID = 3;
    public static final int MULTILINE_ID = 12;
    public static final int PASSWORD_ID = 13;
    public static final int NOTOGGLETOOFF_ID = 14;
    public static final int RADIO_ID = 15;
    public static final int PUSHBUTTON_ID = 16;
    public static final int COMBO_ID = 17;
    public static final int EDIT_ID = 18;
    public static final int SORT_ID = 19;
    public static final int FILESELECT_ID = 20;
    public static final int MULTISELECT_ID = 21;
    public static final int DONOTSPELLCHECK_ID = 22;
    public static final int DONOTSCROLL_ID = 23;
    public static final int COMB_ID = 24;
    public static final int RICHTEXT_ID = 25;//same as RADIOINUNISON_ID (radio buttons)
    public static final int RADIOINUNISON_ID = 25;//same as RICHTEXT_ID (text fields)
    public static final int COMMITONSELCHANGE_ID = 26;

    private String[] OptString=null;

	protected boolean isXFAObject = false;
    private String parentRef;
    private String selectedItem;

    private boolean textColorChanged = false;
    private float[] textColor;
    private Font textFont;
    private int textSize = -1;
    private String textString=null;

    private boolean appearancesUsed = false;
    private boolean offsetDownIcon = false;
    private boolean noDownIcon = false;
    private boolean invertDownIcon = false;

    private String onState;
    private String currentState;
    private String normalOffState, normalOnState;
    private BufferedImage normalOffImage = null;
    private BufferedImage normalOnImage;
    private BufferedImage rolloverOffImage = null;
    private BufferedImage rolloverOnImage;
    private BufferedImage downOffImage = null;
    private BufferedImage downOnImage;
    //private boolean hasNormalOffImage = false;
    //private boolean hasRolloverOffImage = false;
    //private boolean hasDownOffImage = false;
    //private boolean hasDownImages = false;
    //private boolean hasRolloverOn = false;
    //private boolean hasNormalOn = false;

    //flag used to handle POPUP internally
    public static final int POPUP = 1;
	public static final boolean newfieldnameRead = true;

    private String layerName=null;
    
    boolean[] newFlags=null;

    private ActionHandler formHandler;

    private boolean[] Farray=null;
    
    protected Rectangle BBox=null;

protected float[] C;

	protected float[] RD,Rect;

	protected boolean[] flags = new boolean[32];

	boolean Open=true, H_Boolean=true; //note default it true but false in Popup!!

	protected int F=-1,Ff=-1,MaxLen=-1, W=-1;

	protected int Q=-1; //default value

	int SigFlags=-1, StructParent=-1;

	protected int TI=-1;

    protected PdfObject A;

    //internal flag used to store status on additional actions when we decode
    private int popupFlag=0;


	protected PdfObject AA,AP=null, Cdict;

	private PdfObject BI;

	protected PdfObject BS;

	protected PdfObject D;

	/**
     * Filters the MK command and its properties
     * <p/>
     * appearance characteristics dictionary  (all optional)
     * R rotation on wiget relative to page
     * BC array of numbers, range between 0-1 specifiying the border color
     * number of array elements defines type of colorspace
     * 0=transparant
     * 1=gray
     * 3=rgb
     * 4=cmyk
     * BG same as BC but specifies wigets background color
     * <p/>
     * buttons only -
     * CA its normal caption text
     * <p/>
     * pushbuttons only -
     * RC rollover caption text
     * AC down caption text
     * I formXObject defining its normal icon
     * RI formXObject defining its rollover icon
     * IX formXObject defining its down icon
     * IF icon fit dictionary, how to fit its icon into its rectangle
     * (if specified must contain all following)
     * SW when it should be scaled to fit ( default A)
     * A always
     * B when icon is bigger
     * S when icon is smaller
     * N never
     * S type of scaling - (default P)
     * P keep aspect ratio
     * A ignore aspect ratio (fit exactly to width and hight)
     * A array of 2 numbers specifying its location when scaled keeping the aspect ratio
     * range between 0.0-1.0, [x y] would be positioned x acress, y up
     * TP positioning of text relative to icon - (integer)
     * 0=caption only
     * 1=icon only
     * 2=caption below icon
     * 3=caption above icon
     * 4=caption on right of icon
     * 5=caption on left of icon
     * 6=caption overlaid ontop of icon
     */
	private PdfObject MK;

	private PdfObject DC, DP, DS, E, Fdict, Fo, FS,
			JS, K, Nobj, Next, O, PC, PI, PO, Popup,
			PV, R, Sig, Sound, U, V, Win, WP, WS, X;

	protected int[] ByteRange, I;

	protected byte[] rawAS, rawCert, rawContactInfo, rawContents, rawDstring, rawDA, rawDV, rawFstring, rawJS, rawH, rawN, rawNM, rawPstring, rawRC, rawS, rawSubj, rawT, rawTM, rawTU,
	rawURI, rawV;

    protected int FT=-1;

    protected String AS, Cert, ContactInfo, Contents, Dstring, DA, DV, Fstring, JSString, H, N, NM, Pstring, RC, S, Subj, T, TM, TU, URI, Vstring;

    private byte[][] Border, DmixedArray, Fields, State, rawXFAasArray;
    protected PdfObject Bl, OC, Off, On, P;

	private PdfObject XFAasStream;

    protected Object[] Opt,Reference;

	protected byte[][] Kids;

    public FormObject(String ref) {
        super(ref);
        objType=PdfDictionary.Form;
    }

    public FormObject(String ref, boolean flag) {
        super(ref);
        objType=PdfDictionary.Form;
        this.includeParent=flag;
    }


    public FormObject(int ref, int gen) {
       super(ref,gen);

       objType=PdfDictionary.Form;
    }


    public FormObject(int type) {
    	super(type);
    	objType=PdfDictionary.Form;
	}

	public FormObject() {
		super();
		objType=PdfDictionary.Form;
	}

    public FormObject(String ref, ActionHandler inFormHandler) {

	    super(ref);
		formHandler = inFormHandler;
        objType=PdfDictionary.Form;
	}

    public FormObject(String ref, int parentType) {
        super(ref);
        objType=PdfDictionary.Form;

        this.parentType=parentType;

    }
    
    public ActionHandler getHandler(){
    	return formHandler;
    }

    public void setHandler(ActionHandler inFormHandler) {
		formHandler = inFormHandler;
	}
    
    private void copyMK(PdfObject source){

		//System.out.println(source.getMKInt(PdfDictionary.TP)+" "+TP);

		int sourceTP=source.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.TP);
		if(sourceTP!=-1)
			getDictionary(PdfDictionary.MK).setIntNumber(PdfDictionary.TP,sourceTP);

		int sourceR=source.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R);
		getDictionary(PdfDictionary.MK).setIntNumber(PdfDictionary.R,sourceR);

		//make sure also added to getTextStreamValueAsByte
		int[] textStreams=new int[]{PdfDictionary.AC,PdfDictionary.CA, PdfDictionary.RC};

		for(int jj=0;jj<textStreams.length;jj++){
			byte[] bytes=source.getDictionary(PdfDictionary.MK).getTextStreamValueAsByte(textStreams[jj]);
			if(bytes!=null)
			getDictionary(PdfDictionary.MK).setTextStreamValue(textStreams[jj],bytes);
		}

		//make sure also added to getTextStreamValueAsByte
		int[] floatStreams=new int[]{PdfDictionary.BC,PdfDictionary.BG};

		for(int jj=0;jj<floatStreams.length;jj++){
			float[] floats=source.getDictionary(PdfDictionary.MK).getFloatArray(floatStreams[jj]);
			if(floats!=null)
			getDictionary(PdfDictionary.MK).setFloatArray(floatStreams[jj],floats);
		}

    }

	public boolean getBoolean(int id){

        switch(id){

        case PdfDictionary.Open:
        	return Open;

        case PdfDictionary.H:
        	return H_Boolean;

            default:
            	return super.getBoolean(id);
        }

    }

    public void setBoolean(int id,boolean value){

        switch(id){

        case PdfDictionary.Open:
        	Open=value;
        	break;

        case PdfDictionary.H:
        	H_Boolean=value;
        	break;

            default:
                super.setBoolean(id, value);
        }
    }

    /**
     * used internally to set status while parsing - should not be called
     * @param popup
     */
    public void setActionFlag(int popup) {
        popupFlag=popup;
	}

    /**
     * get status found during decode
     */
     public int getActionFlag() {
        return popupFlag;
    }

    /**
    public void setFloatNumber(int id,float value){

        switch(id){

	        case PdfDictionary.R:
	    		R=value;
	    		break;

            default:

                super.setFloatNumber(id,value);
        }
    }

    public float getFloatNumber(int id){

        switch(id){

            case PdfDictionary.R:
        		return R;

            default:

                return super.getFloatNumber(id);
        }
    }  /**/

    public PdfObject getDictionary(int id){

        switch(id){

            case PdfDictionary.A:
               return A;

            case PdfDictionary.AA:
                return AA;

            case PdfDictionary.AP:

            	if(AP==null)
            		AP=new FormObject();
	        	return AP;

            case PdfDictionary.BI:
	        	return BI;

	        case PdfDictionary.Bl:
	        	return Bl;

	        case PdfDictionary.BS:
	        	return BS;

	        case PdfDictionary.C:
	        	return Cdict;


	        //case PdfDictionary.C2:
	        	//return C2;

	        case PdfDictionary.D:
	        	return D;

	        case PdfDictionary.DC:
	        	return DC;

	        case PdfDictionary.DP:
	        	return DP;

	        case PdfDictionary.DS:
	        	return DS;

	        case PdfDictionary.E:
	        	return E;

	        case PdfDictionary.F:
	        	return Fdict;

	        case PdfDictionary.Fo:
	        	return Fo;
	        	
	        case PdfDictionary.FS:
	        	return FS;	

	        case PdfDictionary.JS:
                return JS;

	        //case PdfDictionary.I:
	        	//return I;

	        case PdfDictionary.K:
	        	return K;

            case PdfDictionary.MK: //can't return null

            	if(MK==null)
            		MK=new MKObject();
            	return MK;

            case PdfDictionary.N:
	        	return Nobj;

            case PdfDictionary.Next:
            	return Next;

            case PdfDictionary.O:
	        	return O;

            case PdfDictionary.OC:
                return OC;

            case PdfDictionary.Off:
            	return Off;

            case PdfDictionary.On:
            	return On;

            case PdfDictionary.P:
	        	return P;

            case PdfDictionary.PC:
	        	return PC;

            case PdfDictionary.PI:
	        	return PI;

            case PdfDictionary.PO:
	        	return PO;

            case PdfDictionary.Popup:
	        	return Popup;

            case PdfDictionary.PV:
	        	return PV;

            case PdfDictionary.R:
	        	return R;

            case PdfDictionary.Sig:
	        	return Sig;

            case PdfDictionary.Sound:
	        	return Sound;

            case PdfDictionary.U:
	        	return U;

            case PdfDictionary.V:
	        	return V;

            case PdfDictionary.Win:
	        	return Win;

            case PdfDictionary.WP:
	        	return WP;

            case PdfDictionary.WS:
	        	return WS;

            case PdfDictionary.X:
                return X;

            case PdfDictionary.XFA:
                return XFAasStream;

            default:
                return super.getDictionary(id);
        }
    }

    public void setIntNumber(int id,int value){

        switch(id){

            case PdfDictionary.F:
	        	F=value;
	        break;

	        case PdfDictionary.Ff:
	        	Ff=value;
	        	commandFf(Ff);
	        break;

	        case PdfDictionary.Q: //correct alignment converted to Java value

	        	switch(value){

		        case 0:
		        	Q = JTextField.LEFT;
	        	break;

		        case 1:
		        	Q = JTextField.CENTER;
		        	break;

		        case 2:
		        	Q = JTextField.RIGHT;
		        	break;

		        default:
		        	Q = JTextField.LEFT;
		        	break;
	        	}

	        break;

	        case PdfDictionary.MaxLen:
	        	MaxLen=value;
	        	break;

	        case PdfDictionary.Rotate://store in MK so works for Annot
	        	if(MK==null)
            		MK=new MKObject();

	            MK.setIntNumber(PdfDictionary.R,value);

	        break;

            case PdfDictionary.SigFlags:
                SigFlags=value;
            break;

            case PdfDictionary.StructParent:
	        	StructParent=value;
	        break;

	        case PdfDictionary.TI:
	            TI=value;
	        break;

	        case PdfDictionary.W:
	            W=value;
	        break;

            default:
            	super.setIntNumber(id, value);
        }
    }

    public int getInt(int id){

        switch(id){

            case PdfDictionary.F:
        		return F;

        	case PdfDictionary.Ff:
                return Ff;

        	case PdfDictionary.MaxLen:
        		return MaxLen;

            case PdfDictionary.Q:
                return Q;

            case PdfDictionary.SigFlags:
                return SigFlags;

            case PdfDictionary.StructParent:
        		return StructParent;

        	case PdfDictionary.TI:
	            return TI;

        	case PdfDictionary.W:
	            return W;

            default:
            	return super.getInt(id);
        }
    }

    public void setDictionary(int id,PdfObject value){

    	value.setID(id);

        //if in AP array as other value store here
        if(currentKey!=null){

            //System.out.println("Other values---- "+id+" "+value+" "+objType);
            setOtherValues(id, value);
            return;
        }

        switch(id){

            case PdfDictionary.A:
        		A=value;
        	break;

            case PdfDictionary.AA:
        		AA=value;
        	break;

            case PdfDictionary.AP:
	        	AP=value;

	        	//copy across
	        	if(MK==null && AP!=null && AP.getDictionary(PdfDictionary.N)!=null)
            		MK=AP.getDictionary(PdfDictionary.N).getDictionary(PdfDictionary.MK);

			break;

            case PdfDictionary.BI:
	        	BI=value;
			break;

            case PdfDictionary.Bl:
	        	Bl=value;
			break;

	        case PdfDictionary.BS:
	        	BS=value;
			break;

	        case PdfDictionary.C:
	        	Cdict=value;
			break;

	        //case PdfDictionary.C2:
	        	//C2=value;
			//break;

	        case PdfDictionary.D:
	        	D=value;
			break;

	        case PdfDictionary.DC:
	        	DC=value;
			break;

	        case PdfDictionary.DP:
	        	DP=value;
			break;

	        case PdfDictionary.DS:
	        	DS=value;
			break;

	        case PdfDictionary.E:
	        	E=value;
			break;

	        case PdfDictionary.F:
	        	Fdict=value;
			break;

	        case PdfDictionary.Fo:
	        	Fo=value;
			break;
			
	        case PdfDictionary.FS:
	        	FS=value;
			break;

	        case PdfDictionary.JS:
                JS=value;
            break;

	        case PdfDictionary.K:
	        	K=value;
			break;

			//case PdfDictionary.I:
	        	//I=value;
			//break;

            case PdfDictionary.MK:
            	MK=value;
            break;

            case PdfDictionary.N:
	        	Nobj=value;
			break;

            case PdfDictionary.Next:
	        	Next=value;
			break;

            case PdfDictionary.O:
	        	O=value;
			break;

            case PdfDictionary.OC:
                OC=value;
            break;

            case PdfDictionary.Off:
            	Off=value;
            break;

            case PdfDictionary.On:
            	On=value;
            break;

            case PdfDictionary.P:
	        	P=value;
			break;

            case PdfDictionary.PC:
	        	PC=value;
			break;

            case PdfDictionary.PI:
	        	PI=value;
			break;

            case PdfDictionary.PO:
	        	PO=value;
			break;

            case PdfDictionary.Popup:
                setActionFlag(FormObject.POPUP);
	        	Popup=value;
			break;

            case PdfDictionary.PV:
	        	PV=value;
			break;

            case PdfDictionary.R:
	        	R=value;
			break;

            case PdfDictionary.Sig:
	        	Sig=value;
			break;

            case PdfDictionary.Sound:
	        	Sound=value;
			break;

            case PdfDictionary.U:
	        	U=value;
			break;

            case PdfDictionary.V:
	        	V=value;
			break;

            case PdfDictionary.Win:
	        	Win=value;
			break;

            case PdfDictionary.WP:
	        	WP=value;
			break;

            case PdfDictionary.WS:
	        	WS=value;
			break;

            case PdfDictionary.X:
	        	X=value;
			break;

            case PdfDictionary.XFA:
            	XFAasStream=value;
    		break;

            default:
            	super.setDictionary(id, value);
        }
    }


    public int setConstant(int pdfKeyType, int keyStart, int keyLength, byte[] raw) {

        int PDFvalue =PdfDictionary.Unknown;

        int id=0,x=0,next;

        try{

            //convert token to unique key which we can lookup

            for(int i2=keyLength-1;i2>-1;i2--){

            	next=raw[keyStart+i2];

            	//System.out.println((char)next);
            	next=next-48;

                id=id+((next)<<x);

                x=x+8;
            }

            /**
             * not standard
             */
            switch(id){

//                case StandardFonts.CIDTYPE0:
//                    PDFvalue =StandardFonts.CIDTYPE0;
//                break;


                default:

//                	if(pdfKeyType==PdfDictionary.Encoding){
//                		PDFvalue=PdfCIDEncodings.getConstant(id);
//
//                		if(PDFvalue==PdfDictionary.Unknown){
//
//                			byte[] bytes=new byte[keyLength];
//
//                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
//
//                			unknownValue=new String(bytes);
//                		}
//
//                		if(debug && PDFvalue==PdfDictionary.Unknown){
//                			System.out.println("Value not in PdfCIDEncodings");
//
//                           	 byte[] bytes=new byte[keyLength];
//
//                               System.arraycopy(raw,keyStart,bytes,0,keyLength);
//                               System.out.println("Add to CIDEncodings and as String");
//                               System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in PdfFont Object");
//
//                               System.out.println("final public static int CMAP_"+new String(bytes)+"="+id+";");
//
//                		}
//                	}else
                	PDFvalue=super.setConstant(pdfKeyType,id);

                    if(PDFvalue==-1){


                         if(debug){

                        	 byte[] bytes=new byte[keyLength];

                            System.arraycopy(raw,keyStart,bytes,0,keyLength);
                            System.out.println("key="+new String(bytes)+" "+id+" not implemented in setConstant in "+this);

                            System.out.println("final public static int "+new String(bytes)+"="+id+";");

                        }

                    }

                    break;

            }

        }catch(Exception ee){
            ee.printStackTrace();
        }

        //System.out.println(pdfKeyType+"="+PDFvalue);
        switch(pdfKeyType){


    		default:
    			super.setConstant(pdfKeyType,id);

        }

        return PDFvalue;
    }

  //return as constnt we can check
    public int getNameAsConstant(int id) {

        byte[] raw=null;

        switch(id){

            case PdfDictionary.FT:
                return FT;

            case PdfDictionary.H:
                raw=rawH;
                break;

            case PdfDictionary.N:
                raw=rawN;
                break;

            case PdfDictionary.S:
                raw=rawS;
                break;

            default:
                return super.getNameAsConstant(id);

        }

        if(raw==null)
            return super.getNameAsConstant(id);
        else
        	return PdfDictionary.generateChecksum(0,raw.length,raw);

    }

    public int getParameterConstant(int key) {

    	//System.out.println("Get constant for "+key +" "+this);
        switch(key){


            case PdfDictionary.Subtype:
                if(FT!=PdfDictionary.Unknown)
                    return FT;
                else
                    return super.getParameterConstant(key);
//
//            	//special cases first
//            	if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isZapfDingbats)
//            		return StandardFonts.ZAPF;
//            	else if(key==PdfDictionary.BaseEncoding && Encoding!=null && Encoding.isSymbol)
//            		return StandardFonts.SYMBOL;
//            	else
//            		return BaseEncoding;
        default:
        	return super.getParameterConstant(key);

        }
    }

//    public void setStream(){
//
//        hasStream=true;
//    }


    public PdfArrayIterator getMixedArray(int id) {

    	switch(id){

            case PdfDictionary.Border:
                return new PdfArrayIterator(Border);

            case PdfDictionary.D:
                           return new PdfArrayIterator(DmixedArray);

            case PdfDictionary.Dest:
            	return new PdfArrayIterator(DmixedArray);

            case PdfDictionary.Fields:
                return new PdfArrayIterator(Fields);

            case PdfDictionary.State:
                return new PdfArrayIterator(State);

            case PdfDictionary.XFA:
                return new PdfArrayIterator(rawXFAasArray);

            default:
			return super.getMixedArray(id);
        }
	}

    public byte[] getTextStreamValueAsByte(int id) {

        switch(id){

            case PdfDictionary.Cert:
	        	return rawCert;

            case PdfDictionary.ContactInfo:
	        	return rawContactInfo;
	        	
            case PdfDictionary.Contents:
	        	return rawContents;

/**
	        case PdfDictionary.AC:
	            return rawAC;

	        case PdfDictionary.CA:
	            return rawCA;

	        case PdfDictionary.RC:
	            return rawRC;
*/
            default:
                return super.getTextStreamValueAsByte(id);

        }
    }

    public double[] getDoubleArray(int id) {

        switch(id){
            default:
            	return super.getDoubleArray(id);
        }
    }

    public void setDoubleArray(int id,double[] value) {

        switch(id){

//            case PdfDictionary.FontMatrix:
//                FontMatrix=value;
//            break;

            default:
            	super.setDoubleArray(id, value);
        }
    }

    public int[] getIntArray(int id) {

        switch(id){

        	case PdfDictionary.I:
            return deepCopy(I);

            case PdfDictionary.ByteRange:
            return deepCopy(ByteRange);

            default:
            	return super.getIntArray(id);
        }
    }

    public void setIntArray(int id,int[] value) {

        switch(id){

        case PdfDictionary.I:
        	I=value;
        break;

        case PdfDictionary.ByteRange:
        	ByteRange=value;
        break;

            default:
            	super.setIntArray(id, value);
        }
    }

    public void setMixedArray(int id,byte[][] value) {

        switch(id){

            case PdfDictionary.Border:
                Border=value;
            break;

            case PdfDictionary.Dest:
            	DmixedArray=value;
            break;

            case PdfDictionary.Fields:
                Fields=value;
            break;

            case PdfDictionary.State:
                State=value;
            break;

            case PdfDictionary.XFA:
                rawXFAasArray=value;
            break;


            default:
            	super.setMixedArray(id, value);
        }
    }

    public float[] getFloatArray(int id) {

        switch(id){

        case PdfDictionary.C:
       	return C;

        case PdfDictionary.Rect:
        	return Rect;

        case PdfDictionary.RD:
        	return RD;

            default:
            	return super.getFloatArray(id);

        }
    }

    public void setFloatArray(int id,float[] value) {

        switch(id){

        	case PdfDictionary.C:
	            C=value;
    	        break;

            case PdfDictionary.RD:
	            RD=value;
	        break;

	        case PdfDictionary.Rect:
	            Rect=value;
	        break;

            default:
            	super.setFloatArray(id, value);
        }
    }

    public void setName(int id,byte[] value) {

        switch(id){

	        case PdfDictionary.AS:
	            rawAS=value;
	    	break;
	    	
	        case PdfDictionary.DV:
	            rawDV=value;
        	break;

	        case PdfDictionary.Filter:
                rawFilter=value;
            break;

            case PdfDictionary.SubFilter:
                rawSubFilter=value;
            break;
            
            case PdfDictionary.FT:
	            //setup first time
                FT=PdfDictionary.generateChecksum(0,value.length,value);
	    	break;

	        case PdfDictionary.H:
	            rawH=value;

	            //set H flags
	    	break;

        	case PdfDictionary.N:
                rawN=value;
        	break;

            case PdfDictionary.S:
                rawS=value;
            break;

            default:
                super.setName(id,value);

        }

    }

    public void setObjectArray(int id, Object[] objectValues) {

        switch(id){

            case PdfDictionary.Opt:
            	Opt=objectValues;
            	break;

            case PdfDictionary.Reference:
            	Reference=objectValues;
            	break;

            default:
                super.setObjectArray(id, objectValues);
                break;
        }
    }

    public Object[] getObjectArray(int id) {

        switch(id){

            case PdfDictionary.Opt:
            	return Opt;

            case PdfDictionary.Reference:
            	return Reference;

            default:
                return super.getObjectArray(id);
        }
    }

    public byte[][] getStringArray(int id) {

        switch(id){

            //case PdfDictionary.XFA:
              //              return deepCopy(rawXFAasArray);

            default:
            	return super.getStringArray(id);
        }
    }

    public void setStringArray(int id,byte[][] value) {

        switch(id){

            //case PdfDictionary.XFA:
              //  rawXFAasArray=value;

            default:
            	super.setStringArray(id, value);
        }

    }

    public void setTextStreamValue(int id,byte[] value) {

        switch(id){

            case PdfDictionary.Cert:
                rawCert=value;
            break;

            case PdfDictionary.ContactInfo:
                rawContactInfo=value;
            break;

            case PdfDictionary.Contents:
                rawContents=value;
            break;

            case PdfDictionary.D:
	            rawDstring=value;
        	break;

        	case PdfDictionary.DA:
	            rawDA=value;
        	break;

        	case PdfDictionary.DV:
	            rawDV=value;
        	break;

            case PdfDictionary.F:
	            rawFstring=value;
        	break;

            case PdfDictionary.JS:
                rawJS=value;
        	break;
        	
        	
            case PdfDictionary.Location:
	            rawLocation=value;
	        break;

	        case PdfDictionary.M:
	            rawM=value;
	        break;

            case PdfDictionary.P:
	            rawPstring=value;
        	break;

	        case PdfDictionary.RC:
	            rawRC=value;
	            break;

            case PdfDictionary.Reason:
	            rawReason=value;
	        break;


        	case PdfDictionary.NM:
	            rawNM=value;
        	break;

        	case PdfDictionary.Subj:
	            rawSubj=value;
	            break;
	            
	        case PdfDictionary.T:
	            rawT=value;
	        break;

	        case PdfDictionary.TM:
	            rawTM=value;
	        break;

	        case PdfDictionary.TU:
	            rawTU=value;
	        break;

	        case PdfDictionary.URI:
	        	rawURI=value;
	        break;

	        case PdfDictionary.V:
	        	rawV=value;
	        	Vstring=null; //can be reset
	        break;

            default:
                super.setTextStreamValue(id,value);

        }

    }

    public void setTextStreamValue(int id,String value) {

        switch(id){

	        case PdfDictionary.V:
	        	Vstring=value; //can be reset
	        break;

            default:
                super.setTextStreamValue(id,value);

        }

    }

    public String getName(int id) {

        switch(id){

        case PdfDictionary.AS:

            //setup first time
             if(AS==null && rawAS!=null)
                 AS=new String(rawAS);

             return AS;

        case PdfDictionary.FT:

            //setup first time
            return null;

        case PdfDictionary.H:

            //setup first time
             if(H==null && rawH!=null)
                 H=new String(rawH);

             return H;

        case PdfDictionary.Filter:

            //setup first time
            if(Filter==null && rawFilter!=null)
                Filter=new String(rawFilter);

            return Filter;

            case PdfDictionary.SubFilter:

            //setup first time
            if(SubFilter==null && rawSubFilter!=null)
                SubFilter=new String(rawSubFilter);

            return SubFilter;
        case PdfDictionary.N:

            //setup first time
             if(N==null && rawN!=null)
                 N=new String(rawN);

             return N;

            case PdfDictionary.S:

           //setup first time
            if(S==null && rawS!=null)
                S=new String(rawS);

            return S;

            default:
                return super.getName(id);

        }
    }

    public String getTextStreamValue(int id) {

        switch(id){

            case PdfDictionary.Cert:

            //setup first time
            if(Cert==null && rawCert!=null)
            	Cert=PdfReader.getTextString(rawCert, false);

            return Cert;

            case PdfDictionary.ContactInfo:

                //setup first time
                if(ContactInfo==null && rawContactInfo!=null)
                	ContactInfo=PdfReader.getTextString(rawContactInfo, false);

                return ContactInfo;

            case PdfDictionary.Contents:

                //setup first time
                if(Contents==null && rawContents!=null)
                	Contents=PdfReader.getTextString(rawContents, false);

                return Contents;

            case PdfDictionary.D:

                //setup first time
                if(Dstring==null && rawDstring!=null)
                	Dstring=PdfReader.getTextString(rawDstring, false);

                return Dstring;

        	case PdfDictionary.DA:

            //setup first time
            if(DA==null && rawDA!=null)
            	DA=PdfReader.getTextString(rawDA, false);

            return DA;

        	case PdfDictionary.DV:

                //setup first time
                if(DV==null && rawDV!=null)
                	DV=PdfReader.getTextString(rawDV, false);

                return DV;


            case PdfDictionary.F:

                //setup first time
                if(Fstring==null && rawFstring!=null)
                	Fstring=PdfReader.getTextString(rawFstring, false);

                return Fstring;

        	case PdfDictionary.JS:

	            //setup first time
	            if(JSString==null && rawJS!=null)
	            	JSString=PdfReader.getTextString(rawJS, false);

	            return JSString;

            case PdfDictionary.NM:

	            //setup first time
	            if(NM==null && rawNM!=null)
	            	NM=PdfReader.getTextString(rawNM, false);

	            return NM;

            case PdfDictionary.Location:

	            //setup first time
	            if(Location==null && rawLocation!=null)
	            	Location=new String(rawLocation);
	            return Location;

	        case PdfDictionary.M:

	            //setup first time
	            if(M==null && rawM!=null)
	            	M=new String(rawM);
	            return M;

            case PdfDictionary.P:

                //setup first time
                if(Pstring==null && rawPstring!=null)
                	Pstring=PdfReader.getTextString(rawPstring, false);

                return Pstring;

            case PdfDictionary.RC:

	            //setup first time
	            if(RC==null && rawRC!=null)
	            	RC=new String(rawRC);
	            return RC;
	            
            case PdfDictionary.Reason:

	            //setup first time
	            if(Reason==null && rawReason!=null)
	            	Reason=new String(rawReason);
	            return Reason;
	            
            case PdfDictionary.Subj:
	            //setup first time
	            if(Subj==null && rawSubj!=null)
	            	Subj=PdfReader.getTextString(rawSubj, false);

	            return Subj;
	            
        	case PdfDictionary.T:
	            //setup first time
	            if(T==null && rawT!=null)
	            	T=PdfReader.getTextString(rawT, false);

	            return T;

	        case PdfDictionary.TM:

	            //setup first time
	            if(TM==null && rawTM!=null)
	            	TM=PdfReader.getTextString(rawTM, false);

	            return TM;

	        case PdfDictionary.TU:

	            //setup first time
	            if(TU==null && rawTU!=null)
	            	TU=PdfReader.getTextString(rawTU, false);

	            return TU;


	        case PdfDictionary.URI:

	            //setup first time
	            if(URI==null && rawURI!=null)
	            	URI=PdfReader.getTextString(rawURI, false);

	            return URI;

	        case PdfDictionary.V:

	            //setup first time
	            if(Vstring==null && rawV!=null)
	            	Vstring=PdfReader.getTextString(rawV, false);
	            
	            return Vstring;

            default:
                return super.getTextStreamValue(id);

        }
    }

    /**
     * unless you need special fucntions,
     * use getStringValue(int id) which is faster
     */
    public String getStringValue(int id,int mode) {

        byte[] data=null;

        //get data
        switch(id){

//            case PdfDictionary.BaseFont:
//                data=rawBaseFont;
//                break;

        }


        //convert
        switch(mode){
            case PdfDictionary.STANDARD:

                //setup first time
                if(data!=null)
                    return new String(data);
                else
                    return null;


            case PdfDictionary.LOWERCASE:

                //setup first time
                if(data!=null)
                    return new String(data);
                else
                    return null;

            case PdfDictionary.REMOVEPOSTSCRIPTPREFIX:

                //setup first time
                if(data!=null){
                	int len=data.length;
                	if(len>6 && data[6]=='+'){ //lose ABCDEF+ if present
                		int length=len-7;
                		byte[] newData=new byte[length];
                		System.arraycopy(data, 7, newData, 0, length);
                		return new String(newData);
                	}else
                		return new String(data);
                }else
                    return null;

            default:
                throw new RuntimeException("Value not defined in getName(int,mode) in "+this);
        }
    }

    public byte[][] getKeyArray(int id) {

        switch(id){

        case PdfDictionary.Kids:
            return deepCopy(Kids);



            default:
            	return super.getKeyArray(id);
        }
    }

    public void setKeyArray(int id,byte[][] value) {

        switch(id){

        case PdfDictionary.Kids:
            Kids=value;
        break;

            default:
            	super.setKeyArray(id, value);
        }

    }

    public boolean decompressStreamWhenRead() {
		return true;
	}

    /**
     * resolve what type of field <b>type</b> specifies
     * and return as String
     */
    public static String resolveType(int type) {

        if (type == PdfDictionary.Btn)
            return "Button";
        else if (type == PdfDictionary.Ch)
            return "Choice";
        else if (type == PdfDictionary.Tx)
            return "Text";
        else if (type == PdfDictionary.Popup)
            return "Popup";
        else if (type == PdfDictionary.Square)
            return "Square";
        else if (type == PdfDictionary.Text)
            return "Text Annot";


        return null;
    }

    /**
     * read and setup the form flags for the Ff entry
	 * <b>field</b> is the data to be used to setup the Ff flags
     */
    private void commandFf(int flagValue) {
        /**use formObject.flags
         * to get flags representing field preferences the following are accessed by array address (bit position -1)
         *
         * <b>bit positions</b>
         * all
         * 1=readonly - if set there is no interaction
         * 2=required - if set the field must have a value when submit-form-action occures
         * 3=noexport - if set the field must not be exported by a submit-form-action
         *
         * Choice fields
         * 18=combo - set its a combobox, else a list box
         * 19=edit - defines a comboBox to be editable
         * 20=sort - defines list to be sorted alphabetically
         * 22=multiselect - if set more than one items can be selected, else only one
         * 23=donotspellcheck - (only used on editable combobox) don't spell check
         * 27=commitOnselchange - if set commit the action when selection changed, else commit when user exits field
         *
         * text fields
         * 13=multiline - uses multipul lines else uses a single line
         * 14=password - a password is intended
         * 21=fileselect -text in field represents a file pathname to be submitted
         * 23=donotspellcheck - don't spell check
         * 24=donotscroll - once the field is full don't enter anymore text.
         * 25=comb - (only if maxlen is present, (multiline, password and fileselect are CLEAR)), the text is justified across the field to MaxLen
         * 26=richtext - use richtext format specified by RV entry in field dictionary
         *
         * button fields
         * 15=notoggletooff - (use in radiobuttons only) if set one button must always be selected
         * 16=radio - if set is a set of radio buttons
         * 17=pushbutton - if set its a push button
         * 	if neither 16 nor 17 its a check box
         * 26=radiosinunison - if set all radio buttons with the same on state are turned on and off in unison (same behaviour as html browsers)
         */

            //System.out.println("flag value="+flag);

            boolean[] flags = new boolean[32];
             /**/
            flags[1] = (flagValue & FormStream.READONLY) == FormStream.READONLY;
            flags[2] = (flagValue & FormStream.REQUIRED) == FormStream.REQUIRED;
            flags[3] = (flagValue & FormStream.NOEXPORT) == FormStream.NOEXPORT;
            flags[12] = (flagValue & FormStream.MULTILINE) == FormStream.MULTILINE;
            flags[13] = (flagValue & FormStream.PASSWORD) == FormStream.PASSWORD;
            flags[14] = (flagValue & FormObject.NOTOGGLETOOFF_ID) == FormStream.NOTOGGLETOOFF;
            flags[15] = (flagValue & FormStream.RADIO) == FormStream.RADIO;
            flags[16] = (flagValue & FormStream.PUSHBUTTON) == FormStream.PUSHBUTTON;
            flags[17] = (flagValue & FormStream.COMBO) == FormStream.COMBO;
            flags[18] = (flagValue & FormStream.EDIT) == FormStream.EDIT;
            flags[19] = (flagValue & FormStream.SORT) == FormStream.SORT;
            flags[20] = (flagValue & FormStream.FILESELECT) == FormStream.FILESELECT;
            flags[21] = (flagValue & FormStream.MULTISELECT) == FormStream.MULTISELECT;
            flags[22] = (flagValue & FormStream.DONOTSPELLCHECK) == FormStream.DONOTSPELLCHECK;
            flags[23] = (flagValue & FormStream.DONOTSCROLL) == FormStream.DONOTSCROLL;
            flags[24] = (flagValue & FormStream.COMB) == FormStream.COMB;
            flags[25] = (flagValue & FormStream.RICHTEXT) == FormStream.RICHTEXT;//same as RADIOINUNISON_ID
            flags[25] = (flagValue & FormStream.RADIOINUNISON) == FormStream.RADIOINUNISON;//same as RICHTEXT_ID
            flags[26] = (flagValue & FormStream.COMMITONSELCHANGE) == FormStream.COMMITONSELCHANGE;

            setFlags(flags);

            /**if (flags[3] || flags[22] || flags[24] || flags[26]) {
                LogWriter.writeFormLog("{stream} new flags (3 22 24 26) UNIMPLEMENTED flags - 3=" +
                        flags[3] + " 22=" + flags[22] + " 24=" + flags[24] + " 26= " + flags[26], debugUnimplemented);
            }

            if (debug)
                System.out.println("Ff values flags=" +
                        ConvertToString.convertArrayToString(getFieldFlags()) + '\n');
           /**/
    }

    /**
     * sets the flags array to be <b>interactiveFlags</b>
     */
    protected void setFlags(boolean[] interactiveFlags) {

    	flags = interactiveFlags;
        /**
    	if(getPDFRef()!=null && getPDFRef().indexOf("185 0 R")!=-1){
    		System.out.println("Set flags "+this.getPDFRef()+" "+interactiveFlags[16]);
    		throw new RuntimeException("xx");
    	}/**/

    }


     /**
	 * takes a String <b>colorString</b>, and turns it into the color it represents
	 * e.g. (0.5)  represents gray (127,127,127)
	 */
	protected static Color generateColorFromString(float[] C) {
//		0=transparant
//		1=gray
//		3=rgb
//		4=cmyk


		int i=0;

        if(C!=null)
        i=C.length;


		Color newColor = null;
		if(i==0){
		    //LogWriter.writeFormLog("{stream} CHECK transparent color",debugUnimplemented);
		    newColor = new Color(0,0,0,0);//if num of tokens is 0 transparant, fourth variable my need to be 1

		}else if(i==1){
		    if(debug)
		    	System.out.println("{stream} CHECK gray color="+C[0]);

		    float tok0 =C[0];

		    if(tok0<=1){
		    	newColor = new Color(tok0,tok0,tok0);
		    }else {
		    	newColor = new Color((int)tok0,(int)tok0,(int)tok0);
		    }

		}else if(i==3){
		    if(debug)
		        System.out.println("rgb color="+C[0]+ ' ' +C[1]+ ' ' +C[2]);

		    if(C[0]<=1 && C[1]<=1 && C[2]<=1){
		    	newColor = new Color(C[0],C[1],C[2]);
		    }else {
		    	newColor = new Color((int)C[0],(int)C[1],(int)C[2]);
		    }

		}else if(i==4){
	        //LogWriter.writeFormLog("{stream} CHECK cmyk color="+toks[0]+ ' ' +toks[1]+ ' ' +toks[2]+ ' ' +toks[3],debugUnimplemented);
		   /** float[] cmyk = {
		            Float.parseFloat(toks[0]),
		            Float.parseFloat(toks[1]),
		            Float.parseFloat(toks[2]),
		            Float.parseFloat(toks[3])
		    };*/

		    DeviceCMYKColorSpace cs=new DeviceCMYKColorSpace();
		    cs.setColor(C,3);
		    newColor =(Color) cs.getColor();

		    //newColor = new Color(ColorSpace.getInstance(ColorSpace.TYPE_CMYK),cmyk,1);

		}else{
		    //LogWriter.writeFormLog("{stream} ERROR i="+i+" toks="+ConvertToString.convertArrayToString(toks),debugUnimplemented);
		}

		return newColor;
	}

	/**
	 * takes a value, and turns it into the color it represents
	 * e.g. (0.5)  represents gray (127,127,127)
	 * grey = array length 1, with one value
	 * rgb = array length 3, in the order of red,green,blue
	 * cmyk = array length 4, in the reverse order, (ie. k, y, m, c)
	 */
	public static  Color generateColor(float[] toks) {
//		0=transparant
//		1=gray
//		3=rgb
//		4=cmyk
		

		int i=-1;
		if(toks!=null)
			i=toks.length;

		Color newColor = null;
		if(i==0){
		    //LogWriter.writeFormLog("{stream} CHECK transparent color",debugUnimplemented);
		    newColor = new Color(0,0,0,0);//if num of tokens is 0 transparant, fourth variable my need to be 1
		
		}else if(i==1){
		    
		    float tok0 = toks[0];
		    
		    if(tok0<=1){
		    	newColor = new Color(tok0,tok0,tok0);
		    }else {
		    	newColor = new Color((int)tok0,(int)tok0,(int)tok0);
		    }
		    
		}else if(i==3){
		    if(debug)
		        System.out.println("rgb color="+toks[0]+ ' ' +toks[1]+ ' ' +toks[2]);
		    
		    float tok0 = toks[0];
		    float tok1 = toks[1];
		    float tok2 = toks[2];
		    
		    if(tok0<=1 && tok1<=1 && tok2<=1){
		    	newColor = new Color(tok0,tok1,tok2);
		    }else {
		    	newColor = new Color((int)tok0,(int)tok1,(int)tok2);
		    }

		}else if(i==4){
	       
		    DeviceCMYKColorSpace cs=new DeviceCMYKColorSpace();
            cs.setColor(new float[]{toks[3],toks[2],toks[1],toks[0]},4);
		    newColor =(Color) cs.getColor();

		}
		
		return newColor;
	}

    /**
     * returns true if this formObject represents an XFAObject
     */
    public boolean isXFAObject(){
    	return isXFAObject;
    }

    protected FormObject duplicate() {
        FormObject newObject = new FormObject();

        newObject.parentRef = parentRef;
        newObject.flags = flags;

        newObject.I = I;
        newObject.selectedItem = selectedItem;

        newObject.ref = ref;

        newObject.AA=AA;
        newObject.AP=AP;
        newObject.BS=BS;
        newObject.C=C;
        newObject.D=D;

        newObject.F=F;
        newObject.Ff=Ff;
        newObject.OC=OC;
        newObject.Opt=Opt;
        newObject.Q=Q;
        newObject.MaxLen=MaxLen;
        newObject.rawAS=rawAS;
        newObject.rawDA=rawDA;
        newObject.rawDV=rawDV;
        newObject.rawJS=rawJS;
        newObject.FT=FT;
        newObject.rawNM=rawNM;
//        newObject.rawT=rawT;
        newObject.Rect=Rect;
        newObject.TI=TI;
        newObject.copyMK(this);
        newObject.rawT=rawT;
        newObject.rawTM=rawTM;
        newObject.rawTU=rawTU;
        newObject.rawV=rawV;
        
        newObject.textColor = textColor;
        newObject.textFont = textFont;
        newObject.textSize = textSize;
        newObject.textString = textString;
        
        newObject.appearancesUsed = appearancesUsed;
        newObject.offsetDownIcon = offsetDownIcon;
        newObject.noDownIcon = noDownIcon;
        newObject.invertDownIcon = invertDownIcon;
        
        newObject.onState = onState;
        newObject.currentState = currentState;
        newObject.normalOffImage = normalOffImage;
        newObject.normalOnImage = normalOnImage;
        newObject.rolloverOffImage = rolloverOffImage;
        newObject.rolloverOnImage = rolloverOnImage;
        newObject.downOffImage = downOffImage;
        newObject.downOnImage = downOnImage;
        
        newObject.pageNumber = pageNumber;

        //annotations
        newObject.cColor = cColor;
        newObject.contents = contents;
        newObject.show = show;

        newObject.stateTocheck = stateTocheck;

        newObject.layerName=layerName;
        
        return newObject;
    }

    /** overwrites all the values on this form with any values from the parent*/
    public void copyInheritedValuesFromParent(FormObject parentObj) {
		
		if(pageNumber==-1 && parentObj.pageNumber!=-1)
		pageNumber=parentObj.pageNumber;
		
		formHandler=parentObj.formHandler;
		
		if(AA==null)
		AA=parentObj.AA;
		
		if(C==null)
        C=parentObj.C;

        if(D==null)
		D=parentObj.D;

		if(F==-1)
        F=parentObj.F;

        if(Ff==-1)
		Ff=parentObj.Ff;

        if(OC==null)
        OC=parentObj.OC;

        if(Opt==null)
		Opt=parentObj.Opt;

        if(Q==-1)
		Q=parentObj.Q;

        if(MaxLen==-1)
		MaxLen=parentObj.MaxLen;

        if(rawAS==null)
		rawAS=parentObj.rawAS;

        if(rawDA==null)
        rawDA=parentObj.rawDA;

        if(rawDV==null)
		rawDV=parentObj.rawDV;

        if(rawJS==null)
		rawJS=parentObj.rawJS;

        if(FT==-1)
        FT=parentObj.FT;

        if(rawNM==null)
		rawNM=parentObj.rawNM;

        if(rawT==null)
		rawT=parentObj.rawT;

        if(Rect==null)
		Rect=parentObj.Rect;

        if(TI==-1)
		TI=parentObj.TI;

        if(rawTM==null)
		rawTM=parentObj.rawTM;

        if(rawTU==null)
		rawTU=parentObj.rawTU;

        if(rawV==null)
		rawV=parentObj.rawV;
	}

    /**
     * get actual object reg
     *
     * @deprecated use formObject.getObjectRefAsString();
     */
    public String getPDFRef() {
        return getObjectRefAsString();
    }

    /**
     * returns the alignment (Q)
     */
    public int getAlignment(){

        if(Q==-1)
        Q= JTextField.LEFT;
        
    	return Q;
    }
    
    public boolean hasColorChanged(){
    	return textColorChanged;
    }
    
    /** rests the color changed flag to false, to say that it has be refreshed on screen */
    public void resetColorChanged(){
    	textColorChanged = false;
    }

    /**
     * sets the text color for this form
     * 
     */
    public void setTextColor(float[] color) {
    	//JS made public so that javascript can access it
    	
    	//check if is javascript and convert to our float
    	if(color.length>0 && Float.isNaN(color[0])){//not-a-number
			float[] tmp = new float[color.length-1];
			System.arraycopy(color, 1, tmp, 0, color.length-1);
			color = tmp;
		}
    	
        textColor = color;
        
        //set flag to say that the text color has chnaged so we can update the forms.
        textColorChanged = true;
    }
    

    /**
     * set the text font for this form
     */
    protected void setTextFont(Font font) {
        textFont = font;
    }

    /**
     * sets the text size for this form
     */
    protected void setTextSize(int size) {
        textSize = size;
    }

    /**
     * sets the child on state,
     * only applicable to radio buttons
     */
    public void setChildOnState(String curValue) {
        onState = curValue;
    }

    /**
     * sets the current state,
     * only applicable to check boxes
     */
    public void setCurrentState(String curValue) {
    	currentState = curValue;
    }

    /**
     * sets the text value
     */
    public void setTextValue(String text) {
    	//use empty string so that the raw pdf value does not get recalled.
    	if(text==null)
			text = "";
    	
    	textString = text;
    }

    /**
     * sets the selected item
     * only applicable to the choices fields
     */
    public void setSelectedItem(String curValue) {

        selectedItem = curValue;
       
    }

    /**
     * sets the field name for this field (used by XFA)
     */
    public void setFieldName(String field) {

    	T=field;
    	
    }
    
    /**
     * sets the parent for this field
     */
    public void setParent(String parent) {
        this.parentRef = parent;
    }
    
    /**
     * gets the parent for this field
     */
    public String getParentRef() {

        //option to take from file as well
        if(parentRef==null && includeParent)
            return getStringKey(PdfDictionary.Parent);
        else
            return parentRef;
    }

    /**
	 * return the characteristic type
	 */
	private boolean[] calcFarray(int flagValue) {
		
		if(flagValue==0)
			return new boolean[9];
		
		boolean[] Farray=new boolean[9];
		
		final int[] pow={0,1,2,4,8,16,32,64,128,256};
		for(int jj=1;jj<9;jj++){
			if(((flagValue & pow[jj])==pow[jj]))//bit 3		print
				Farray[jj - 1] = true;
		}
		
		return Farray;
	}

    /**
     * sets the top index
     * for the choice fields
     */
    public void setTopIndex(int[] index) {
        
    	if(index==null)
    		TI=-1;
    	else if(index.length>0) //@chris -fyi added by MArk to fix exception in xfa files on tests
    		TI=index[0];
    	
    	I = index;
        
    }

    /**
     * return the bounding rectangle for this object
     */
    public Rectangle getBoundingRectangle(){

        float[] coords=getFloatArray(PdfDictionary.Rect);

        if(coords!=null){
            float x1=coords[0],y1=coords[1],x2=coords[2],y2=coords[3];


            if(x1>x2){
                float tmp = x1;
                x1 = x2;
                x2 = tmp;
            }
            if(y1>y2){
                float tmp = y1;
                y1 = y2;
                y2 = tmp;
            }

            BBox = new Rectangle((int)(x1+0.5f),(int)(y1+0.5f),(int) (x2-x1),(int)(y2-y1));
        }else
            BBox = new Rectangle(0,0,0,0);

        return BBox;
    }

   /**
     * sets the type this form specifies
     */
    public void setType(int type, boolean isXFA) {

        if(isXFA)
            FT=type;
    }

    /**
     * sets the flag <b>pos</b> to value of <b>flag</b>
     */
    public void setFlag(int pos, boolean flag) {

        flags[pos - 1] = flag;
        
        if(newFlags==null)
        	newFlags=getFF(Ff);
        
        newFlags[pos - 1] = flag;
        
    }

    /**
     * returns the flags array (Ff in PDF)
     *  * all
     * 1=readonly - if set there is no interaction
     * 2=required - if set the field must have a value when submit-form-action occures
     * 3=noexport - if set the field must not be exported by a submit-form-action
     *
     * Choice fields
     * 18=combo - set its a combobox, else a list box
     * 19=edit - defines a comboBox to be editable
     * 20=sort - defines list to be sorted alphabetically
     * 22=multiselect - if set more than one items can be selected, else only one
     * 23=donotspellcheck - (only used on editable combobox) don't spell check
     * 27=commitOnselchange - if set commit the action when selection changed, else commit when user exits field
     *
     * text fields
     * 13=multiline - uses multipul lines else uses a single line
     * 14=password - a password is intended
     * 21=fileselect -text in field represents a file pathname to be submitted
     * 23=donotspellcheck - don't spell check
     * 24=donotscroll - once the field is full don't enter anymore text.
     * 25=comb - (only if maxlen is present, (multiline, password and fileselect are CLEAR)), the text is justified across the field to MaxLen
     * 26=richtext - use richtext format specified by RV entry in field dictionary
     *
     * button fields
     * 15=notoggletooff - (use in radiobuttons only) if set one button must always be selected
     * 16=radio - if set is a set of radio buttons
     * 17=pushbutton - if set its a push button
     * 	if neither 16 nor 17 its a check box
     * 26=radiosinunison - if set all radio buttons with the same on state are turned on and off in unison (same behaviour as html browsers)

     */
    public boolean[] getFieldFlags() {

        return flags;
    }

    private boolean[] getFF(int flagValue) {
    	
    	boolean[] flags = new boolean[32];
        /**/
       flags[1] = (flagValue & FormStream.READONLY) == FormStream.READONLY;
       flags[2] = (flagValue & FormStream.REQUIRED) == FormStream.REQUIRED;
       flags[3] = (flagValue & FormStream.NOEXPORT) == FormStream.NOEXPORT;
       flags[12] = (flagValue & MULTILINE_ID) == FormStream.MULTILINE;
       flags[13] = (flagValue & FormStream.PASSWORD) == FormStream.PASSWORD;
       flags[14] = (flagValue & FormStream.NOTOGGLETOOFF) == FormStream.NOTOGGLETOOFF;
       flags[15] = (flagValue & FormStream.RADIO) == FormStream.RADIO;
       flags[16] = (flagValue & FormStream.PUSHBUTTON) == FormStream.PUSHBUTTON;
       flags[17] = (flagValue & FormStream.COMBO) == FormStream.COMBO;
       flags[18] = (flagValue & FormStream.EDIT) == FormStream.EDIT;
       flags[19] = (flagValue & FormStream.SORT) == FormStream.SORT;
       flags[20] = (flagValue & FormStream.FILESELECT) == FormStream.FILESELECT;
       flags[21] = (flagValue & FormStream.MULTISELECT) == FormStream.MULTISELECT;
       flags[22] = (flagValue & FormStream.DONOTSPELLCHECK) == FormStream.DONOTSPELLCHECK;
       flags[23] = (flagValue & FormStream.DONOTSCROLL) == FormStream.DONOTSCROLL;
       flags[24] = (flagValue & FormStream.COMB) == FormStream.COMB;
       flags[25] = (flagValue & FormStream.RICHTEXT) == FormStream.RICHTEXT;//same as RADIOINUNISON_ID
       flags[25] = (flagValue & FormStream.RADIOINUNISON) == FormStream.RADIOINUNISON;//same as RICHTEXT_ID
       flags[26] = (flagValue & FormStream.COMMITONSELCHANGE) == FormStream.COMMITONSELCHANGE;

       return flags;
	}

    /**
     * the normal off image
     * if only one state call this to set normalOffImage to be the default image
     */
    protected void setNormalAppOff(BufferedImage image, String state) {
        normalOffState = state;
        normalOffImage = image;
        appearancesUsed = true;
    }

    /**
     * the on normal image
     */
    protected void setNormalAppOn(BufferedImage image, String state) {
        normalOnState = state;
        normalOnImage = image;
        appearancesUsed = true;
    }

    public boolean hasAPimages(){
        return appearancesUsed;
    }
    /**
     * sets the image
     */
    protected void setAppearanceImage(BufferedImage image, int imageType, int status) {

        appearancesUsed=true;

        switch(imageType){
            case PdfDictionary.D:
                if(status==PdfDictionary.On){
                    downOnImage = image;
                }else if(status==PdfDictionary.Off){
                    downOffImage = image;
                }else
                    throw new RuntimeException("Unknown status use PdfDictionary.On or PdfDictionary.Off");
            break;

            case PdfDictionary.N:
                if(status==PdfDictionary.On){
                 normalOnImage = image;
                }else if(status==PdfDictionary.Off){
                    normalOffImage=OpaqueImage;
                }else
                    throw new RuntimeException("Unknown status use PdfDictionary.On or PdfDictionary.Off");
            break;

            case PdfDictionary.R:
                if(status==PdfDictionary.On){
                	rolloverOnImage = image;
                }else if(status==PdfDictionary.Off){
                    rolloverOffImage=OpaqueImage;
                }else
                    throw new RuntimeException("Unknown status use PdfDictionary.On or PdfDictionary.Off");
            break;

            default:
                throw new RuntimeException("Unknown type use PdfDictionary.D, PdfDictionary.N or PdfDictionary.R");
        }
    }

    /**
     * sets the rollover off image
     * if only one state call this to set rolloverOffImage to be the default image
     */
    protected void setRolloverAppOff(BufferedImage image) {
        rolloverOffImage = image;
        appearancesUsed = true;
    }

    /**
     * sets the rollover on image
     */
    protected void setRolloverAppOn(BufferedImage image) {
        rolloverOnImage = image;
        appearancesUsed = true;
    }

    /**
     * sets the border color
     */
    public void setBorderColor(String nextField) {
    	
    	if(nextField!=null)
        	getDictionary(PdfDictionary.MK).setFloatArray(PdfDictionary.BC, generateFloatFromString(nextField));
    	
    }

    /**
     * sets the background color for this form
     */
    public void setBackgroundColor(String nextField) {
    	
    	if(nextField!=null)
        	getDictionary(PdfDictionary.MK).setFloatArray(PdfDictionary.BG, generateFloatFromString(nextField));
    		
    }
    
    /**
	 * takes a String <b>colorString</b>, and turns it into the color it represents
	 * e.g. (0.5)  represents gray (127,127,127)
	 * cmyk = 4 tokens in the order c, m, y, k
	 */
	private static  float[] generateFloatFromString(String colorString) {
//		0=transparant
//		1=gray
//		3=rgb
//		4=cmyk
		if(debug)
			System.out.println("CHECK generateColorFromString="+colorString);
		
		StringTokenizer tokens = new StringTokenizer(colorString,"[()] ,");
		
		float[] toks = new float[tokens.countTokens()];
		int i=0;
		while(tokens.hasMoreTokens()){
			
			String tok = tokens.nextToken();
			if(debug)
				System.out.println("token"+(i+1)+ '=' +tok+" "+colorString);
			
			toks[i] = Float.parseFloat(tok);
			i++;
		}
		
		if(i==0)
			return null;
		else
			return toks;
	}

    /**
     * sets the normal caption for this form
     */
    public void setNormalCaption(String caption) {
        
        if(caption!=null){
        	getDictionary(PdfDictionary.MK).setTextStreamValue(PdfDictionary.CA, caption.getBytes());
        }
    }

    /**
     * sets whether there should be a down looking icon
     */
    protected void setOffsetDownApp() {
        offsetDownIcon = true;
    }

    /**
     * sets whether a down icon should be used
     */
    protected void setNoDownIcon() {
        noDownIcon = true;
    }

    /**
     * sets whether to invert the normal icon for the down icon
     */
    protected void setInvertForDownIcon() {
        invertDownIcon = true;
    }
    
    /**
     * returns to rotation of this field object, 
     * currently in stamp annotations only
     *
     * @deprecated use formObject.getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R);
     */
    public int getRotation(){
    	
    	return getDictionary(PdfDictionary.MK).getInt(PdfDictionary.R);
    }

    /**
     * returns true if has normal of image
     */
    public boolean hasNormalOff() {
        return normalOffImage!=null;
    }

    /**
     * returns true if has rollover off image
     */
    public boolean hasRolloverOff() {
        return rolloverOffImage!=null;
    }

    /**
     * returns true if has down off image
     */
    public boolean hasDownOff() {
        return downOffImage!=null;
    }

    /**
     * returns true if has one or more down images set
     */
    public boolean hasDownImage() {
        return (downOnImage!=null || hasDownOff());
    }

    /**
     * returns true if has a rollover on image
     */
    public boolean hasRolloverOn() {
        return rolloverOnImage!=null;
    }

    /**
     * returns true if has a normal on image
     */
    public boolean hasNormalOn() {
        return normalOnImage!=null;
    }
    
    protected void setStateToCheck(String stateTocheck) {
        this.stateTocheck = stateTocheck;

    }

    public void overwriteWith(FormObject form) {
        if (form == null)
            return;

        if (form.parentRef != null)
            parentRef = form.parentRef;
        if (form.flags != null)
            flags = form.flags;

        if (form.I != null)
            I = form.I;
        if (form.selectedItem != null)
            selectedItem = form.selectedItem;
        
        if (form.ref != null)
            ref = form.ref;
        if (form.textColor != null)
            textColor = form.textColor;
        if (form.textFont != null)
            textFont = form.textFont;
        if (form.textSize != -1)
            textSize = form.textSize;
        if (form.textString!=null)
            textString = form.textString;
        
        if (form.appearancesUsed)
            appearancesUsed = form.appearancesUsed;
        if (form.offsetDownIcon)
            offsetDownIcon = form.offsetDownIcon;
        if (form.noDownIcon)
            noDownIcon = form.noDownIcon;
        if (form.invertDownIcon)
            invertDownIcon = form.invertDownIcon;
        
        if (form.onState != null)
            onState = form.onState;
        if (form.currentState != null)
            currentState = form.currentState;
        if (form.normalOffImage != null)
            normalOffImage = form.normalOffImage;
        if (form.normalOnImage != null)
            normalOnImage = form.normalOnImage;
        if (form.rolloverOffImage != null)
            rolloverOffImage = form.rolloverOffImage;
        if (form.rolloverOnImage != null)
            rolloverOnImage = form.rolloverOnImage;
        if (form.downOffImage != null)
            downOffImage = form.downOffImage;
        if (form.downOnImage != null)
            downOnImage = form.downOnImage;
        if (form.pageNumber != -1)
            pageNumber = form.pageNumber;

        //annotations
        if (form.cColor != null)
            cColor = form.cColor;
        if (form.contents != null)
            contents = form.contents;
        if (form.show)
            show = form.show;

        if (form.stateTocheck != null)
            stateTocheck = form.stateTocheck;
        
        //align
        AA=form.AA;
        AP=form.AP;
        BS=form.BS;
        C=form.C;
        D=form.D;

        F=form.F;
        Ff=form.Ff;
        Opt=form.Opt;
        OC=form.OC;
        rawAS=form.rawAS;
    	Q = form.Q;
    	MaxLen=form.MaxLen;
    	rawDA=form.rawDA;
        rawDV=form.rawDV;
    	FT=form.FT;
    	rawJS=form.rawJS;
    	rawNM=form.rawNM;
    	rawT=form.rawT;
    	Rect=form.Rect;
    	
    	rawTM=form.rawTM;
    	rawTU=form.rawTU;
    	rawV=form.rawV;
    	
    	TI=form.TI;
    	
    	copyMK(form);
    	
    }

    public Object getPopupObj(){
		return popupObj;
    }
    
	/**
     * See also  {@link FormObject#getUserName()}
     * @return the full field name for this form
     *
     * @deprecated use formObject.getTextStreamValue(PdfDictionary.T);
	 * NO LONGER USED INTERNALLY
     */
	public String getFieldName() {
		
		//ensure resolved
		if(T==null)
		this.getTextStreamValue(PdfDictionary.T);
		
		return T;
	}

	/**
	 * @return the currently selected State of this field at time of opening.
	 */
	public String getCurrentState() {
		return currentState;
	}

	/**
	 * @return the on state for this field
	 */
	public String getOnState() {
		return onState;
	}

	/**
	 * @return the characteristics for this field.
	 * <br>
	 * bit 1 is index 0 in []
	 * [0] 1 = invisible
	 * [1] 2 = hidden - dont display or print
	 * [2] 3 = print - print if set, dont if not
	 * [3] 4 = nozoom
	 * [4] 5= norotate
	 * [5] 6= noview
	 * [6] 7 = read only (ignored by wiget)
	 * [7] 8 = locked
	 * [8] 9 = togglenoview
	 */
	public boolean[] getCharacteristics() {

		//lazy initialisation
		if(Farray==null){
			
			if(F==-1)
				Farray=new boolean[9];
			else
				Farray=calcFarray(F);
			
		//	System.out.println("F="+F+" "+this.getPDFRef()+" "+characteristic+" display="+display+" "+characteristic[2]);
		}
		

        return Farray;
	}

	/**
	 * @return userName for this field (TU value)
     *
     * @deprecated use formObject.getTextStreamValue(PdfDictionary.TU);
	 */
	public String getUserName() {
		
		//ensure resolved
		if(TU==null)
		getTextStreamValue(PdfDictionary.TU);
			
		return TU;
	}

	/**
	 * @return the state to check for this field,
	 * this is used towards identifying which of a set of radio buttons is on,
	 */
	public String getStateTocheck() {
		return stateTocheck;
	}

	/**
	 * @return the default text size for this field
	 */
	public int getTextSize() {
		return textSize;
	}

	/**
	 * @return the values map for this field,
	 * map that references the display value from the export values
	 */
	public Map getValuesMap() {
		
		
		if(Opt!=null && OptValues==null){
							
			Object[] rawOpt=getObjectArray(PdfDictionary.Opt);	
			
			if(rawOpt!=null){
				
				int count=rawOpt.length;
				
				String key,value;
				Object[] obj;
				
				for(int ii=0;ii<count;ii++){
					
					if(rawOpt[ii] instanceof Object[]){ //2 items (see p678 in v1.6 PDF ref)
						obj=(Object[])rawOpt[ii];
						
						key=PdfReader.getTextString((byte[]) obj[0], false);
						value=PdfReader.getTextString((byte[]) obj[1], false);
						
						if(OptValues==null)
							OptValues=new HashMap();
							
						OptValues.put(key, value);
													
					}
				}									
			}		
		}
		
		return OptValues;
	}

	/**
	 * @return the default value for this field
     *
     * @deprecated use formObject.getTextStreamValue(PdfDictionary.DV);
	 */
	public String getDefaultValue() {
		
		return getTextStreamValue(PdfDictionary.DV);
	}

	/**
	 * @return the items array list (Opt)
	 */
	public String[] getItemsList() {
		
		if(OptString==null){
			Object[] rawOpt=getObjectArray(PdfDictionary.Opt);	
			
			if(rawOpt!=null){
				int count=rawOpt.length;
				OptString=new String[count];
				
				
				for(int ii=0;ii<count;ii++){
					
					if(rawOpt[ii] instanceof Object[]){ //2 items (see p678 in v1.6 PDF ref)
						Object[] obj=(Object[])rawOpt[ii];
						
						OptString[ii]=PdfReader.getTextString((byte[]) obj[1], false);
						//System.out.println("A Opt="+OptString[ii]+" "+PdfReader.getTextString((byte[]) obj[0]));
						
					}else if(rawOpt[ii] instanceof byte[]){
						OptString[ii]=PdfReader.getTextString((byte[]) rawOpt[ii], false);
						//System.out.println("B Opt="+OptString[ii]);
						
					}else{ 
					}	
				}
			}						
		}
		
		
		return OptString;
	}

	/**
	 * @return the selected Item for this field
	 */
	public String getSelectedItem() {
		
		if(selectedItem==null)
    		selectedItem=getTextStreamValue(PdfDictionary.V);
    	
		//if no value set but selection, use that
		if(selectedItem==null && I!=null ){
			String[] items= this.getItemsList();
	          int itemSelected=I[0];
	          if(items!=null && itemSelected>-1 && itemSelected<items.length)
	        	  return items[itemSelected];
	          else
	        	  return null;
		}else
			return selectedItem;
	}

	/**
	 * @return the top index, or item that is visible in the combobox or list first.
	 */
	public int[] getTopIndex() {

		if(I==null && TI!=-1){
			I=new int[1];
			I[0]=TI;
		}
		
		return I;
	}

	/**
	 * @return the text string for this field - if no value set but a default (DV value)
     * set, return that.
	 */
	public String getTextString() {
		if(textString==null)
			textString =getTextStreamValue(PdfDictionary.V);
		
		if(textString==null && getDefaultValue()!=null)
        	return getDefaultValue();
		else
        	return textString;
	}

	/**
	 * @return the maximum length of the text in the field
     *
     * @deprecated use formObject.getInt(PdfDictionary.MaxLen)
	 */
	public int getMaxTextLength() {
		
		return MaxLen;
	}

	/**
	 * @return the normal caption for this button,
	 * the caption displayed when nothing is interacting with the icon, and at all other times unless 
	 * a down and/or rollover caption is present
     *
     * @deprecated use formObject.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
	 */
	public String getNormalCaption() {
		
		return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
	}

	/**
	 * @return the down caption,
	 * caption displayed when the button is down/pressed
     *
     * @deprecated use formObject.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.AC);  
	 */
	public String getDownCaption() {
		
		return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.AC);
	}

	/**
	 * @return the rollover caption,
	 * the caption displayed when the user rolls the mouse cursor over the button
     *
     * @deprecated use formObject.getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.RC);
	 */
	public String getRolloverCaption() {
		
		return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.RC);
	}

	/**
	 * @return whether or not appearances are used in this field
	 */
	public boolean isAppearancesUsed() {
		return appearancesUsed;
	}

	/**
	 * @return the position of the view of the text in this field
	 * 
	 * positioning of text relative to icon - (integer)
     * 	0=caption only
     * 	1=icon only
     * 	2=caption below icon
     * 	3=caption above icon
     * 	4=caption on right of icon
     * 	5=caption on left of icon
     * 	6=caption overlaid ontop of icon
     */
	public int getTextPosition() {
		
		return getDictionary(PdfDictionary.MK).getInt(PdfDictionary.TP);
	}

	/**
	 * @return the default state of this field,
	 * the state to return to when the field is reset
     *
     * @deprecated use formObject.getName(PdfDictionary.AS);
	 */
	public String getDefaultState() {
		
		if(AS==null)
			this.getName(PdfDictionary.AS);
		
		return AS;
		
	}

	/**
	 * @return the normal on state for this field
	 */
	public String getNormalOnState() {
		return normalOnState;
	}
	
	/**
	 * @return the normal off state for this field
	 */
	public String getNormalOffState() {
		return normalOffState;
	}

	/**
	 * @return the normal off image for this field
	 */
	public BufferedImage getNormalOffImage() {
		
		return normalOffImage;
	}

	/**
	 * @return the normal On image for this field
	 */
	public BufferedImage getNormalOnImage() {
		
		return normalOnImage;
	}

	/**
	 * @return if this field has not got a down icon
	 */
	public boolean hasNoDownIcon() {
		return noDownIcon;
	}

	/**
	 * @return whether this field has a down icon as an offset of the normal icon
	 */
	public boolean hasOffsetDownIcon() {
		return offsetDownIcon;
	}

	/**
	 * @return whether this field has a down icon as an inverted image of the normal icon
	 */
	public boolean hasInvertDownIcon() {
		return invertDownIcon;
	}

	/**
	 * @return the down off image for this field
	 */
	public BufferedImage getDownOffImage() {
		return downOffImage;
	}

	/**
	 * @return the down on image for this field
	 */
	public BufferedImage getDownOnImage() {
		return downOnImage;
	}

	/**
	 * @return the rollover image for this field,
	 * the image displayed when the user moves the mouse over the field
	 */
	public BufferedImage getRolloverOffImage() {
		return rolloverOffImage;
	}

	/**
	 * @return the rollover on image,
	 * the image displayed when the user moves the mouse over the field, when in the on state
	 */
	public BufferedImage getRolloverOnImage() {
		return rolloverOnImage;
	}

	/**
	 * @return the text font for this field
	 */
	public Font getTextFont() {
		return textFont;
	}

	/**
	 * @return the text color for this field
	 */
	public Color getTextColor() {
		return generateColor(textColor);
	}

	/**
	 * @return the border color for this field
     *
     * @deprecated use FormObject.generateColor(formObject.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BC));
	 */
	public Color getBorderColor() {
		
		return generateColor(getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BC));
	}

	/**
	 * @return the border style for this field
     *
     * @deprecated use formObject.getDictionary(pdfDictionary.BS);
	 */
	public PdfObject getBorder() {
		return BS;
	}

	/**
	 * @return the background color for this field
     * @deprecated - use FormObject.generateColor(formObj.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BG));
	 */
	public Color getBackgroundColor() {
    	
		return generateColor(getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BG));
	}

	/**
	 * return true if the popup component has been built
	 */
	public boolean isPopupBuilt() {
		return popupBuilt;
	}

	/**
	 * store the built popup component for use next time
	 * and set popupBuilt to true.
	 */
	public void setPopupBuilt(Object popup) {
		if(popup==null)
			return;
		
		popupObj = popup;
		popupBuilt = true;
	}

    /**
     * @return
     */
    public String getLayerName() {
    	
    	//lazy initialisation
    	if(this.layerName==null){
			PdfObject OC=this.getDictionary(PdfDictionary.OC);
			
			if(OC!=null)
				this.layerName=OC.getName(PdfDictionary.Name);
		}
    	
        return layerName;
    }
    
    /**JS stores if any of the form values have changed acessed via hasFormChanged()*/
	private boolean formChanged = false;
	
	/**JS has the form fields changed */
	public boolean hasValueChanged(){
		return formChanged;
	}
	
	/** flags up this forms value as being changed so that it will be updated to the view */
	public void setFormChanged() {
		formChanged = true;
	}
	
	/**JS resets the form changed flag to indicate the values have been updated */
	public void resetFormChanged() {
		formChanged = false;
	}
	
	/** @oldJS
	 * returns the current value for this field,
	 * if text field the text string,
	 * if choice field the selected item
	 * if button field the normal caption
	 * @return
	 */
	public String getValue(){


        int subtype=getParameterConstant(PdfDictionary.Subtype);

        switch(subtype){
		case PdfDictionary.Tx:
			if(textString==null)
				textString = getTextStreamValue(PdfDictionary.V);
			
			//check if we have a null value and if so return an emtpy string instead,
            //as JS assumes text values are always string values.
			if(textString==null)
				return "";
			else 
				return textString;
			
        case PdfDictionary.Ch:

        	if(selectedItem==null)
        		selectedItem=getTextStreamValue(PdfDictionary.V);
        	
            
            return selectedItem;

        case PdfDictionary.Btn:


            return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
            
        case PdfDictionary.Sig:

            
            return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);

        default:// to catch all the annots
        	
        	return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
		}
		//NOTE - Do not return empty string if value is null, as affects 0.00 values.
	}
	
	/**@oldJS
	 * sets the value of this field dependent on which type of field it is 
	 */
	public void setValue(String inVal){//need to kept as java strings
		boolean preFormChanged = formChanged;
		String CA=null;
		

        int subtype=getParameterConstant(PdfDictionary.Subtype);
        
        switch(subtype){
		case PdfDictionary.Tx:
			
			String curVal = getTextStreamValue(PdfDictionary.V);
			//if the current value is the same as the new value, our job is done.
			if(curVal!=null && curVal.equals(inVal))
				break;
			
			if(textString != null && textString.equals(inVal)){
				break;
			}
			
			//use empty string so that the raw pdf value does not get recalled.
			if(inVal==null)
				inVal = "";
			textString = inVal;
			
			formChanged = true;
			break;

        case PdfDictionary.Ch:

        	if(selectedItem==null)
        		selectedItem=getTextStreamValue(PdfDictionary.V);
        	

            if(selectedItem != null && selectedItem.equals(inVal)){
				break;
			}
			selectedItem = inVal;
			formChanged = true;
			break;

        case PdfDictionary.Btn:

        	CA=getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
    		

            if(CA != null && CA.equals(inVal)){
				break;
			}
            getDictionary(PdfDictionary.MK).setTextStreamValue(PdfDictionary.CA,inVal.getBytes());
           
			formChanged = true;
			break;

        default:

        	CA=getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
		

            if(CA != null && CA.equals(inVal)){
				break;
			}
            
            getDictionary(PdfDictionary.MK).setTextStreamValue(PdfDictionary.CA,inVal.getBytes());
            
			formChanged = true;
			
		}
		
		if(formChanged && !preFormChanged){
			formHandler.C(this);
		}
	}
	
	/**@oldJs
	 * defines the thickness of the border when stroking.
	 */
	public void setLineWidth(int lineWidth){

		if(BS==null)
			BS=new FormObject();
		
		BS.setIntNumber(PdfDictionary.W, lineWidth);
		
	}
	
	/**JS Controls whether the field is hidden or visible on screen and in print. Values are:
	 * visible,
	 * hidden,
	 * noPrint,
	 * noView.
	 * 
	 * GetCharacteristics will add this value into its array when called.
	 */
	public int display = -1;
	
	/**@oldJs
	 * added for backward compatibility or old adobe files. 
	 */
	public void setBorderWidth(int width){ setLineWidth(width); } 
	
	/*
	change; p373 js_api
	Example;
	changeEx
	commitKey;
	fieldFull;
	keyDown;
	modifier;
	name;
	rc;
	richChange;
	richChangeEx;
	richValue;
	selEnd;
	selStart;
	shift;
	source;
	target;
	targetName;
	type;
	value;
	 */
	/**@oldJS
	 * Verifies the current keystroke event before the data is committed. It can be used to check target 
	 * form field values to verify, for example, whether character data was entered instead of numeric 
	 * data. JavaScript sets this property to true after the last keystroke event and before the field 
	 * is validated.
	 */
	public boolean willCommit(){
		//CHRIS javascript unimplemented willcommit
		return true;
	}
	
	/**JS shows if the display value has changed, if it has we need to check what to and change*/
	public boolean hasDisplayChanged() {
		boolean checkChange = (display!=-1);
		if(checkChange)
			return true;
		else 
			return false;
	}
	
	/**@oldJS
	 * added to return this for event.target from javascript 
	 */
	public Object getTarget(){ return this; }
	
	/**@oldJS
	 * JS returns the normal caption associated to this button
	 */
	public String buttonGetCaption(){ return buttonGetCaption(0); }
	
	/**@oldJS
	 * returns the caption associated with this button,
	 * @param nFace - 
	 * 0  normal caption (default)
	 * 1  down caption
	 * 2  rollover caption
	 */
	public String buttonGetCaption(int nFace){
		switch(nFace){
		case 1: return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.AC);
		case 2: return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.RC);
		default: return getDictionary(PdfDictionary.MK).getTextStreamValue(PdfDictionary.CA);
		}
	}
	
	/**@oldJS
	 * sets this buttons normal caption to <b>cCaption</b> 
	 */
	public void buttonSetCaption(String cCaption){ buttonSetCaption(cCaption,0);}
	
	/**@oldJS
	 * sets this buttons caption to <b>cCaption</b>, it sets the caption defined by <b>nFace</b>.
	 * @param nFace -
	 * 0  (default) normal caption
	 * 1  down caption
	 * 2  rollover caption
	 */
	public void buttonSetCaption(String cCaption,int nFace){
		switch(nFace){
		case 1: getDictionary(PdfDictionary.MK).setTextStreamValue(PdfDictionary.AC, cCaption.getBytes());;
		case 2: getDictionary(PdfDictionary.MK).setTextStreamValue(PdfDictionary.RC, cCaption.getBytes());;
		default: getDictionary(PdfDictionary.MK).setTextStreamValue(PdfDictionary.CA, cCaption.getBytes());;
		}
	}
	
	/**@oldJS
	 * returns the background color for the annotation objects 
	 */
	public Object getfillColor(){
		return generateColor(getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BG));
	}
	
	
}

