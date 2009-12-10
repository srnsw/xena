/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2008, IDRsolutions and Contributors.
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

  * FormStream.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.raw.PdfDictionary;

import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.PdfShape;
import org.jpedal.objects.PdfPageData;
import org.jpedal.utils.Strip;
import org.jpedal.utils.LogWriter;
import org.jpedal.fonts.PdfFont;
import org.jpedal.parser.PdfStreamDecoder;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * can object and creat images, set values in Appearances
 */
public class FormStream {

    final public static boolean debugUnimplemented = false;//to show unimplemented parts*/
    final public static boolean debug = false;//print info to screen

    //only display once
    private static boolean showFontMessage=false;

    /**
     * exit when an unimplemented feature or error has occured in form/annot code
     */
    final public static boolean exitOnError=false;

    /* variables for forms to check with the (Ff)  flags field
    * (1<<bit position -1), to get required result
    */
    final public static int READONLY=(1);//1
    final public static int REQUIRED=(1<<1);//2
    final public static int NOEXPORT=(1<<2);//4
    final public static int MULTILINE=(1<<12);//4096;
    final public static int PASSWORD=(1<<13);//8192;
    final public static int NOTOGGLETOOFF=(1<<14);//16384;
    final public static int RADIO=(1<<15);//32768;
    final public static int PUSHBUTTON=(1<<16);//65536;
    final public static int COMBO=(1<<17);//131072;
    final public static int EDIT=(1<<18);//262144;
    final public static int SORT=(1<<19);//524288;
    final public static int FILESELECT=(1<<20);//1048576
    final public static int MULTISELECT=(1<<21);//2097152
    final public static int DONOTSPELLCHECK=(1<<22);//4194304
    final public static int DONOTSCROLL=(1<<23);//8388608
    final public static int COMB=(1<<24);//16777216
    final public static int RADIOINUNISON=(1<<25);//33554432 //same as RICHTEXT_ID
    final public static int RICHTEXT=(1<<25);//33554432 //same as RADIOINUNISON_ID
    final public static int COMMITONSELCHANGE=(1<<26);//67108864

    /** transparant image for use when appearance stream is null */
    final private static BufferedImage OpaqueImage = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);

    /** handle of file reader for form streams*/
    protected PdfObjectReader currentPdfFile;

    public static boolean marksNewJavascriptCode=false;

    //use to create bigger images
    //which we can then scale down to
    //improve image quality
    private static int multiplyer=1;

    /**
     * stop anyone creating empty  instance
     */
    public FormStream() {}

    /**
     * used if we want to recreate (ie for better print quality)
     */
    public FormStream(PdfObjectReader inCurrentPdfFile, int multiplyer) {

        currentPdfFile = inCurrentPdfFile;
        this.multiplyer=multiplyer;

    }

    public static final int[] id = {PdfDictionary.A,PdfDictionary.C2,PdfDictionary.Bl,
            PdfDictionary.E, PdfDictionary.X, PdfDictionary.D, PdfDictionary.U, PdfDictionary.Fo,
            PdfDictionary.PO, PdfDictionary.PC, PdfDictionary.PV,
            PdfDictionary.PI, PdfDictionary.O, PdfDictionary.C1, PdfDictionary.K,
            PdfDictionary.F, PdfDictionary.V, PdfDictionary.C2, PdfDictionary.DC,
            PdfDictionary.WS, PdfDictionary.DS, PdfDictionary.WP, PdfDictionary.DP};

    /**
     * takes in a FormObject already populated with values for the child to overwrite
     */
    public FormObject createAppearanceString(FormObject formObj, PdfObjectReader inCurrentPdfFile, int multiplyer) {

        currentPdfFile = inCurrentPdfFile;
        this.multiplyer=multiplyer;

        init(formObj);

        return formObj;
    }

    private void init(FormObject formObject) {

        final boolean debug=false;//formObject.getPDFRef().equals("68 0 R");

        if(debug)
            System.out.println("------------------------------setValues-------------------------------"+formObject+" "+formObject.getObjectRefAsString());

        //setup images
        PdfObject I=formObject.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I);
        if(I!=null)
            formObject.setNormalAppOff(rotate(decode(currentPdfFile,I, formObject.getParameterConstant(PdfDictionary.Subtype)), 
                    formObject.getRotation() * Math.PI / 180), null);

        //set Ff flags
        int Ff=formObject.getInt(PdfDictionary.Ff);
        if(Ff!=PdfDictionary.Unknown)
            commandFf(formObject,Ff);

        //set Javascript
        if(!marksNewJavascriptCode) //lock out to test
            resolveAdditionalAction(formObject);

        if(debug)
            System.out.println("AP="+formObject.getDictionary(PdfDictionary.AP));

        setupAPimages(formObject, debug);


        //set H
        int key = formObject.getNameAsConstant(PdfDictionary.H);
        if(key!=PdfDictionary.Unknown){
            /**
             * highlighting mode
             * done when the mouse is pressed or held down inside the fields active area
             * N nothing
             * I invert the contents
             * O invert the border
             * P display down appearance stream, or if non available offset the normal to look down
             * T same as P
             *
             * this overides the down appearance
             * Default value = I
             */
            if (key==PdfDictionary.T || key==PdfDictionary.P) {
                if (!formObject.hasDownImage())
                    formObject.setOffsetDownApp();

            } else if (key==PdfDictionary.N) {
                //do nothing on press
                formObject.setNoDownIcon();

            } else if (key==PdfDictionary.I) {
                //invert the contents colors
                formObject.setInvertForDownIcon();

            }
        }

        //set Fonts

        String textStream=formObject.getTextStreamValue(PdfDictionary.DA);

        if(textStream!=null){
            decodeFontCommandObj(textStream, formObject);

            //System.out.println("textStream="+textStream);

        }

        //code from FormStream not called but may be needed
        //this is font stream
        /*stream
        //turn into byte array and add
        // (<text>)tj
        String textString = formObject.getContents();
        if (textString != null) {
            byte[] textbytes = textString.getBytes();


            System.out.println("textString="+textString);
            int streamLength = stream.length;
            byte[] newbytes = new byte[streamLength + textbytes.length];
            for (int i = 0; i < newbytes.length; i++) {
                if (i < streamLength)
                    newbytes[i] = stream[i];
                else
                    newbytes[i] = textbytes[i - streamLength];
            }

            //then send into stream decoder
            PdfStreamDecoder textDecoder = new PdfStreamDecoder();
            textDecoder.decodeStreamIntoObjects(newbytes);

            StringBuffer textData = textDecoder.getlastTextStreamDecoded();
            if (textData != null)
                formObject.setTextValue(textData.toString());
        }
        /**/

    }

    public void setupAPimages(FormObject formObject, boolean debug) {
        //setup images - trickle throuh maps
        int[] values=new int[]{PdfDictionary.N, PdfDictionary.R, PdfDictionary.D}; //N must be first
        final String[] names=new String[]{"PdfDictionary.N", "PdfDictionary.R", "PdfDictionary.D"}; //N must be first

        int APcount=values.length;
        PdfObject APobj;
        BufferedImage img=null;
        for(int ii=0;ii<APcount;ii++){

            //debug=formObject.getPDFRef().equals("68 0 R") && values[ii]==PdfDictionary.N;

            APobj=formObject.getDictionary(PdfDictionary.AP).getDictionary(values[ii]);
            if(APobj!=null){

                if(debug && values[ii]==PdfDictionary.N)
                    System.out.println(" AP ("+names[ii]+")="+APobj+" "+APobj.getObjectRefAsString()+
                            " AP="+formObject.getDictionary(PdfDictionary.AP)+
                            " form="+formObject+" "+formObject.getObjectRefAsString()+" stream="+APobj.getDecodedStream()+" Off="+APobj.getDictionary(PdfDictionary.Off));


                //main
                if(APobj.getDecodedStream()!=null){
                    img=rotate(decode(currentPdfFile, APobj, formObject.getParameterConstant(PdfDictionary.Subtype)),
                            formObject.getRotation() * Math.PI / 180);

                    if(debug)
                            System.out.println(img);
//            		try {
//            			System.out.println(img);
//						ImageIO.write(img, "png", new java.io.File("/Users/markee/Desktop/xx"+values[ii]+".png"));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}

                    if(img==null)
                        continue;

                    if(values[ii]==PdfDictionary.D){

                        formObject.setAppearanceImage(img, PdfDictionary.D, PdfDictionary.Off);

                        if(debug){
                            System.out.println("D "+img);
                        }
                        
                    }else if(values[ii]==PdfDictionary.N && !formObject.hasNormalOff()){

                        formObject.setNormalAppOff(img, null);
                        if(debug)
                            System.out.println("N "+img);

                    }else if(values[ii]==PdfDictionary.R){
                        //formObject.setAppearanceImage(img, PdfDictionary.R, PdfDictionary.Off);
                        formObject.setRolloverAppOff(img);
                        if(debug)
                            System.out.println("R "+img);
                    }

                }else{  //Off, /On Other

                    //On
                    PdfObject OnObj=APobj.getDictionary(PdfDictionary.On);
                    if(OnObj!=null){

                        img=rotate(decode(currentPdfFile, OnObj, formObject.getParameterConstant(PdfDictionary.Subtype)),
                                formObject.getRotation() * Math.PI / 180);

                        if(debug)// && values[ii]==PdfDictionary.N)
                            System.out.println("On="+OnObj+" "+img);

                        if(img==null)
                            continue;

                        if(values[ii]==PdfDictionary.D)	{

                            formObject.setAppearanceImage(img, PdfDictionary.D, PdfDictionary.On);

                            if (!formObject.hasDownOff())
                                formObject.setAppearanceImage(OpaqueImage, PdfDictionary.D, PdfDictionary.Off);

                        }else if(values[ii]==PdfDictionary.N){
                            formObject.setNormalAppOn(img, "On");

                        }else if(values[ii]==PdfDictionary.R){

                            formObject.setRolloverAppOn(img);
                            if (!formObject.hasRolloverOff())
                                formObject.setRolloverAppOff(OpaqueImage);
                        }
                    }

                    //Off
                    PdfObject OffObj=APobj.getDictionary(PdfDictionary.Off);
                    if(OffObj!=null){

                        img=rotate(decode(currentPdfFile, OffObj, formObject.getParameterConstant(PdfDictionary.Subtype)),
                                formObject.getRotation() * Math.PI / 180);

                        if(debug)//  && values[ii]==PdfDictionary.N)
                            System.out.println("Off="+OffObj+" "+OffObj.getDecodedStream()+" "+img);

                        if(img==null)
                            continue;

                        if(values[ii]==PdfDictionary.D)	{
                            formObject.setAppearanceImage(img, PdfDictionary.D, PdfDictionary.Off);

                        }else if(values[ii]==PdfDictionary.N){
                            formObject.setNormalAppOff(img, "Off");

                        }else if(values[ii]==PdfDictionary.R){
                            formObject.setRolloverAppOff(img);
                        }
                    }


                    //Other
                    Map otherValues=APobj.getOtherDictionaries();
                    if(otherValues!=null && !otherValues.isEmpty()){

                        if(debug)
                            System.out.println(formObject.getObjectRefAsString()+" AP Other="+otherValues);

                        Iterator keys=otherValues.keySet().iterator();
                        Object val;
                        String key;
                        while(keys.hasNext()){
                            key=(String)keys.next();
                            val=otherValues.get(key);

                            PdfObject otherObj=((PdfObject)val);

                            img=rotate(decode(currentPdfFile, otherObj, formObject.getParameterConstant(PdfDictionary.Subtype)),
                                    formObject.getRotation() * Math.PI / 180);

                            if(debug)
                                System.out.println("(other) "+key+" "+otherObj+" "+img);

                            if(img==null)
                                continue;


                            if(values[ii]==PdfDictionary.D)	{

                                if(debug)
                                    System.out.println("D(other) set="+img+" "+formObject);

                                formObject.setAppearanceImage(img, PdfDictionary.D, PdfDictionary.On);

                                if (!formObject.hasDownOff())
                                    formObject.setAppearanceImage(OpaqueImage, PdfDictionary.D, PdfDictionary.Off);

                            }else if(values[ii]==PdfDictionary.N){

                                //store so kids can retrieve later
                                formObject.setStateToCheck(key);

                                formObject.setNormalAppOn(img, key);

                                if(debug)
                                    System.out.println("Set N (other) "+formObject);

                                if (!formObject.hasNormalOff())
                                    formObject.setNormalAppOff(OpaqueImage, null);


                            }else if(values[ii]==PdfDictionary.R){

                                formObject.setRolloverAppOn(img);

                                if (!formObject.hasRolloverOff()) {
                                    formObject.setRolloverAppOff(OpaqueImage);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * read and setup the form flags for the Ff entry
     * <b>field</b> is the data to be used to setup the Ff flags
     */
    private static void commandFf(FormObject formObject, int flagValue) {
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
        {
            //System.out.println("flag value="+flag);

            boolean[] flags = new boolean[32];
             /**/
            flags[1] = (flagValue & READONLY) == READONLY;
            flags[2] = (flagValue & REQUIRED) == REQUIRED;
            flags[3] = (flagValue & NOEXPORT) == NOEXPORT;
            flags[12] = (flagValue & MULTILINE) == MULTILINE;
            flags[13] = (flagValue & PASSWORD) == PASSWORD;
            flags[14] = (flagValue & NOTOGGLETOOFF) == NOTOGGLETOOFF;
            flags[15] = (flagValue & RADIO) == RADIO;
            flags[16] = (flagValue & PUSHBUTTON) == PUSHBUTTON;
            flags[17] = (flagValue & COMBO) == COMBO;
            flags[18] = (flagValue & EDIT) == EDIT;
            flags[19] = (flagValue & SORT) == SORT;
            flags[20] = (flagValue & FILESELECT) == FILESELECT;
            flags[21] = (flagValue & MULTISELECT) == MULTISELECT;
            flags[22] = (flagValue & DONOTSPELLCHECK) == DONOTSPELLCHECK;
            flags[23] = (flagValue & DONOTSCROLL) == DONOTSCROLL;
            flags[24] = (flagValue & COMB) == COMB;
            flags[25] = (flagValue & RICHTEXT) == RICHTEXT;//same as RADIOINUNISON
            flags[25] = (flagValue & RADIOINUNISON) == RADIOINUNISON;//same as RICHTEXT
            flags[26] = (flagValue & COMMITONSELCHANGE) == COMMITONSELCHANGE;

            formObject.setFlags(flags);


        }
    }

    /**
     * defines actions to be executed on events 'Trigger Events'
     *
     * @Action This is where the raw data is parsed and put into the FormObject
     */
    private void resolveAdditionalAction(FormObject formObject) {

        /**
         * entries NP, PP, FP, LP never used
         * A action when pressed in active area ?some others should now be ignored?
         * E action when cursor enters active area
         * X action when cursor exits active area
         * D action when cursor button pressed inside active area
         * U action when cursor button released inside active area
         * Fo action on input focus
         * BI action when input focus lost
         * PO action when page containing is opened,
         * 	actions O of pages AA dic, and OpenAction in document catalog should be done first
         * PC action when page is closed, action C from pages AA dic follows this
         * PV action on viewing containing page
         * PI action when no longer visible in viewer
         * K action on - [javascript]
         * 	keystroke in textfield or combobox
         * 	modifys the list box selection
         * 	(can access the keystroke for validity and reject or modify)
         * F the display formatting of the field (e.g 2 decimal places) [javascript]
         * V action when fields value is changed [javascript]
         * C action when another field changes (recalculate this field) [javascript]
         */
        int possValuesCount=id.length;
        int idValue;

        for(int jj=0;jj<possValuesCount;jj++){

            //store most actions in lookup table to make code shorter/faster
            idValue = id[jj];


        }
    }


/**
     * decode appearance stream and convert into VectorRenderObject we can redraw
     * */
    private static BufferedImage decode(PdfObjectReader currentPdfFile, PdfObject XObject, int subtype){

        int width = (int) (20*multiplyer),height = (int) (20*multiplyer);

        boolean useHires=true;

    	currentPdfFile.checkResolved(XObject);

    	try{

    		/**
    		 * generate local object to decode the stream
    		 */
    		PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(currentPdfFile,useHires,true); //switch to hires as well

    		ObjectStore localStore = new ObjectStore();
    		glyphDecoder.setStore(localStore);

            glyphDecoder.setMultiplier(multiplyer);

    		/**
    		 * create renderer object
    		 */
    		DynamicVectorRenderer glyphDisplay=new DynamicVectorRenderer(0,false,20,localStore);

    		//fix for hires
    		if(!useHires)
    			glyphDisplay.setOptimisedRotation(false);
    		else
            //if(useHires)
                glyphDisplay.setHiResImageForDisplayMode(useHires);

    		glyphDecoder.init(false,true,15,0,new PdfPageData(),0,glyphDisplay,currentPdfFile);

    		/**read any resources*/
    		try{

    			PdfObject Resources =XObject.getDictionary(PdfDictionary.Resources);
    			currentPdfFile.checkResolved(Resources);
    			if (Resources != null)
    				glyphDecoder.readResources(Resources,false);

    		}catch(Exception e){
    			e.printStackTrace();
    			System.out.println("Exception "+e+" reading resources in XForm");
    }

            /**decode the stream*/
    		byte[] commands=XObject.getDecodedStream();


            if(commands!=null)
    			glyphDecoder.decodeStreamIntoObjects(commands);


    		boolean ignoreColors=glyphDecoder.ignoreColors;

    		glyphDecoder=null;

    		localStore.flush();

    		org.jpedal.fonts.glyph.T3Glyph form= new org.jpedal.fonts.glyph.T3Glyph(glyphDisplay, 0,0,ignoreColors,"");

    		float[] BBox=XObject.getFloatArray(PdfDictionary.BBox);

    		float rectX1=0,rectY1=0;
    		if(BBox!=null){

    			rectX1 = (BBox[0]*multiplyer);
    			rectY1 = (BBox[1]*multiplyer);
    			width=(int) ((BBox[2]-BBox[0])*multiplyer);
    			height=(int) ((BBox[3]-BBox[1])*multiplyer);

    			if(width==0 && height>0)
    				width=1;
    			if(width>0 && height==0)
    				height=1;

    			/**
    			if(formFieldValues!=null){
    				if(whenToScale==null || whenToScale.equals("A")){
    					//scale icon to fit BBox
    					Rectangle formRect = (Rectangle)formFieldValues.get("rect");
    					if(formRect.width!=width || formRect.height!=height){
    						// default is A Always scale to fit Form BBox, look in org.jpedal.fonts.T3Glyph D1 D0
    						LogWriter.writeFormLog("{stream} XObject MK IF A command, the icon should be scaled to fit the BBox",false);
    					}
    				}else if(whenToScale.equals("N")){
    					//do nothing as already does this
    				}else {
    					LogWriter.writeFormLog("{XObject} XObject MK IF Unimplemented command="+whenToScale,false);
    				}
    			}/**/
    		}

    		if(width<0)
    			width=-width;
    		if(height<0)
    			height=-height;

    		if(width==0 || height==0)
    			return null;

    		BufferedImage aa;
    		Graphics2D g2;

            float a,b,c,d,e,f;
            int offset=height;

            float[] matrix=XObject.getFloatArray(PdfDictionary.Matrix);

            if(matrix!=null){

    			a = matrix[0]*multiplyer;
    			b = matrix[1]*multiplyer;
    			c = matrix[2]*multiplyer;
    			d = matrix[3]*multiplyer;
    			e = matrix[4];
    			f = matrix[5];

    			if(c<0){
    				aa=new BufferedImage(height,width,BufferedImage.TYPE_INT_ARGB);
    				offset=width;
    			}else{
    				aa=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

    				if(e!=0f)
    					e = -rectX1;
    				if(f!=0f)
    					f = -rectY1;
    			}
    			g2=(Graphics2D) aa.getGraphics();

    		    AffineTransform flip=new AffineTransform();
    			flip.translate(0, offset);
    			flip.scale(1, -1);
    			g2.setTransform(flip);

    			if(debug)
    				System.out.println(" rectX1 = "+rectX1+" rectY1 = "+rectY1+" width = "+width+" height = "+height);

    			AffineTransform affineTransform = new AffineTransform(a,b,c,d,e,f);
    			g2.transform(affineTransform);
    		} else {
    			aa=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

    			g2=(Graphics2D) aa.getGraphics();

    			AffineTransform flip=new AffineTransform();
    			flip.translate(0, offset);
    			flip.scale(1, -1);
    			g2.setTransform(flip);
    		}

            //add transparency for highlights
            if(subtype==PdfDictionary.Highlight)
            g2.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f ) );

    		form.render(0,g2, 0f);

    		g2.dispose();

            return aa;

    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}catch(Error e){
    		e.printStackTrace();
    		return null;
    	}
    }



    /**
     * method to rotate an image through a given angle
     * @param src the source image
     * @param angle the angle to rotate the image through
     * @return the rotated image
     */
    private static BufferedImage rotate(BufferedImage src, double angle) {
        BufferedImage dst=null;

        if(src == null)
            return null;

        try{
            int w = src.getWidth();
            int h = src.getHeight();
            int newW = (int)(Math.round(h * Math.abs(Math.sin(angle))+w * Math.abs(Math.cos(angle))));
            int newH = (int)(Math.round(h * Math.abs(Math.cos(angle))+w * Math.abs(Math.sin(angle))));
            AffineTransform at = AffineTransform.getTranslateInstance((newW-w)/2,(newH-h)/2);
            at.rotate(angle, w/2, h/2);

            dst = new BufferedImage(newW,newH,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = dst.createGraphics();
            g.drawRenderedImage(src, at);
            g.dispose();
        }catch(Error e){
            dst=null;
        }
        return dst;
    }

     /**/
    /**
     * join the dots and save the image for the inklist annot
     *
     * NO exmaple found i switchover so moved into here for if we need
     */
    private static void commandInkList(Object field, FormObject formObject) {
        //the ink list join the dots array
        if(debug)
            System.out.println("inklist array="+field);

        /**current shape object being drawn note we pass in handle on pageLines*/
        PdfShape currentDrawShape=new PdfShape();
        Rectangle rect = formObject.getBoundingRectangle();

        String paths = (String)field;
        StringTokenizer tok = new StringTokenizer(paths,"[] ",true);
        int countArrays=0;
        boolean isFirstPoint = false;
        String next,first=null,second=null;
        while(tok.hasMoreTokens()){
            next = tok.nextToken();
            if(next.equals("[")){
                countArrays++;
                isFirstPoint = true;
                continue;
            }else if(next.equals("]")){
                countArrays--;
                continue;
            }else if(next.equals(" ")){
                continue;
            }else {
                if(first!=null){
                    second = next;
                }else {
                    first = next;
                    continue;
                }
            }

            if(isFirstPoint){
                currentDrawShape.moveTo(Float.parseFloat(first)-rect.x,Float.parseFloat(second)-rect.y);
                isFirstPoint = false;
            }else{
                currentDrawShape.lineTo(Float.parseFloat(first)-rect.x,Float.parseFloat(second)-rect.y);
            }

            first = null;
        }
//          close for s command
//            currentDrawShape.closeShape();

        org.jpedal.objects.GraphicsState currentGraphicsState= new org.jpedal.objects.GraphicsState();

        Shape currentShape =
                currentDrawShape.generateShapeFromPath(null,
                        currentGraphicsState.CTM,
                        false,null,false,null,currentGraphicsState.getLineWidth(),0);

        Stroke inkStroke = currentGraphicsState.getStroke();

        BufferedImage image = new BufferedImage(rect.width,rect.height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.setStroke(inkStroke);
        g2.setColor(Color.red);
        g2.scale(1,-1);
        g2.translate(0,-image.getHeight());
        g2.draw(currentShape);

        g2.dispose();

        formObject.setNormalAppOff(image,null);
    }

    //////code from DecodeCommand in AnnotStream
    /**{
     //the ink list join the dots array

     //current shape object being drawn note we pass in handle on pageLines
     PdfShape currentDrawShape=new PdfShape();
     Rectangle rect = formObject.getBoundingRectangle();

     String paths = Strip.removeArrayDeleminators((String)field);
     StringTokenizer tok = new StringTokenizer(paths,"[] ",true);

     boolean isFirstPoint = false;
     String next,first=null,second=null;
     while(tok.hasMoreTokens()){
     next = tok.nextToken();
     if(next.equals("[")){
     isFirstPoint = true;
     continue;
     }else if(next.equals("]")){
     continue;
     }else if(next.equals(" ")){
     continue;
     }else {
     if(first!=null){
     second = next;
     }else {
     first = next;
     continue;
     }
     }

     if(isFirstPoint){
     currentDrawShape.moveTo(Float.parseFloat(first)-rect.x,Float.parseFloat(second)-rect.y);
     isFirstPoint = false;
     }else{
     currentDrawShape.lineTo(Float.parseFloat(first)-rect.x,Float.parseFloat(second)-rect.y);
     }

     first = null;
     }
     //          close for s command
     //            currentDrawShape.closeShape();

     org.jpedal.objects.GraphicsState currentGraphicsState=formObject.getGraphicsState();

     Shape currentShape =
     currentDrawShape.generateShapeFromPath(null,
     currentGraphicsState.CTM,
     false,null,false,null,currentGraphicsState.getLineWidth(),0);

     Stroke inkStroke = currentGraphicsState.getStroke();

     BufferedImage image = new BufferedImage(rect.width,rect.height,BufferedImage.TYPE_INT_ARGB);
     Graphics2D g2 = (Graphics2D)image.getGraphics();
     g2.setStroke(inkStroke);
     g2.setColor(Color.red);
     g2.scale(1,-1);
     g2.translate(0,-image.getHeight());
     g2.draw(currentShape);

     g2.dispose();

     //ShowGUIMessage.showGUIMessage("path draw",image,"path drawn");

     formObject.setNormalAppOff(image,null);

     //        }else if(command.equals("RD")){
     //            //rectangle differences left top right bottom order as recieved
     //            //the bounds of the internal object, in from the Rect
     //
     //            StringTokenizer tok = new StringTokenizer(Strip.removeArrayDeleminators((String)field));
     //            float left = Float.parseFloat(tok.nextToken());
     //            float top = Float.parseFloat(tok.nextToken());
     //            float right = Float.parseFloat(tok.nextToken());
     //            float bottom = Float.parseFloat(tok.nextToken());
     //
     //            formObject.setInternalBounds(left,top,right,bottom);
     //        }else {
     //        	notFound = true;
     }
     /**/
    public boolean hasXFADataSet() {
        return false;
    }

    /**
     * takes the PDF commands and creates a font
     */
    private static void decodeFontCommandObj(String fontStream,FormObject formObject){

        //now parse the stream into a sequence of tokens
        StringTokenizer tokens=new StringTokenizer(fontStream,"() []");
        int tokenCount=tokens.countTokens();
        String[] tokenValues=new String[tokenCount];
        int i=0;
        while(tokens.hasMoreTokens()){
            tokenValues[i]=tokens.nextToken();
            i++;
        }

        //now work out what it does and build up info
        for(i=tokenCount-1;i>-1;i--){
//			System.out.println(tokenValues[i]+" "+i);

            //look for commands
            if(tokenValues[i].equals("g")){ //set color (takes 1 values
                i--;
                float col=0;
                try{
                    col=Float.parseFloat(tokenValues[i]);
                }catch(Exception e){
                    LogWriter.writeLog("Error in generating g value "+tokenValues[i]);
                }

                formObject.setTextColor(new float[]{col});

            }else if(tokenValues[i].equals("Tf")){ //set font (takes 2 values - size and font
                i--;
                int textSize=8;
                try{
                    textSize=(int) Float.parseFloat(tokenValues[i]);
//					if(textSize==0)
//						textSize = 0;//TODO check for 0 sizes CHANGE size to best fit on 0
                }catch(Exception e){
                    LogWriter.writeLog("Error in generating Tf size "+tokenValues[i]);
                }

                i--;//decriment for font name
                String font=null;
                try{
                    font=tokenValues[i];
                    if(font.startsWith("/"))
                        font = font.substring(1);
                }catch(Exception e){
                    LogWriter.writeLog("Error in generating Tf font "+tokenValues[i]);
                }

                PdfFont currentFont=new PdfFont();

                formObject.setTextFont(currentFont.setFont(font, textSize));

                formObject.setTextSize(textSize);

            }else if(tokenValues[i].equals("rg")){
                i--;
                float b=Float.parseFloat(tokenValues[i]);
                i--;
                float g=Float.parseFloat(tokenValues[i]);
                i--;
                float r=Float.parseFloat(tokenValues[i]);

                formObject.setTextColor(new float[]{r,g,b});

//				if(debug)
//					System.out.println("rgb="+r+","+g+","+b+" rg     CHECK ="+currentAt.textColor);

            }else if(tokenValues[i].equals("Sig")){
                LogWriter.writeFormLog("Sig-  UNIMPLEMENTED="+fontStream+"< "+i,debugUnimplemented);
            }else if(tokenValues[i].equals("\\n")){
                //ignore \n
                if(debug)
                    System.out.println("ignore \\n");
            }else {
                if(!showFontMessage){
                    showFontMessage=true;
                    LogWriter.writeFormLog("{stream} Unknown FONT command "+tokenValues[i]+ ' ' +i+" string="+fontStream,debugUnimplemented);

                }
            }
        }
    }
}
