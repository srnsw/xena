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
 * PdfStreamDecoder.java
 * ---------------
 */
package org.jpedal.parser;

import com.idrsolutions.pdf.color.shading.ShadingFactory;

import org.jpedal.PdfDecoder;
import org.jpedal.PdfHighlights;
import org.jpedal.color.*;
import org.jpedal.constants.PDFImageProcessing;
import org.jpedal.constants.PageInfo;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfFontException;
import org.jpedal.external.*;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.*;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.images.ImageOps;
import org.jpedal.images.ImageTransformer;
import org.jpedal.images.ImageTransformerDouble;
import org.jpedal.images.SamplingFactory;
import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.*;
//<start-adobe>
import org.jpedal.objects.structuredtext.StructuredContentHandler;
//<end-adobe>
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Fonts;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;
import org.jpedal.utils.StringUtils;
import org.jpedal.utils.repositories.Vector_Object;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.*;
import java.util.*;

 /**
 * Contains the code which 'parses' the commands in
 * the stream and extracts the data (images and text).
 * Users should not need to call it.
 */
public class PdfStreamDecoder implements StreamDecoder{

    private float samplingUsed=-1;
    
    private boolean getSamplingOnly=false;

    private boolean showTextAsRotated=false;

	private PdfLayerList layers=null;

	private boolean doNotRotate=false;

	private boolean TRFlagged=false;

	ImageHandler customImageHandler=null;


    //int values for all colorspaces
    private Map colorspacesUsed=new HashMap();
    
    //used in CS to avoid miscaching
    private String csInUse,CSInUse;

	boolean rejectSuperimposedImages=false,sharpenDownsampledImages=false;

	boolean isLayerVisible=true;

	int layerLevel=0;
	
	PdfObject groupObj=null;

    //flag used to debug feature - please do not use
    final public static boolean newForms=true;

    //count recusrsive levels for forms
    private int streams=0;

	Map layerVisibility=new HashMap();

	//only load 1 instance of any 1 font
	private  HashMap fontsLoaded=new HashMap();

	//start of Dictionary on Inline image
	private int startInlineStream=0;

                         
	protected int glyphCount=0;

	//used internally to show optimisations
	private int optionsApplied= PDFImageProcessing.NOTHING;

	//save font info and generate glyph on first render
	private boolean generateGlyphOnRender=false;

	final static int[] prefixes={60,40}; //important that [ comes before (  '<'=60 '('=40
	final static int[] suffixes={62,41}; //'>'=62 ')'=41

	/**flag to show if image transparent*/
	boolean isMask=true;

	/**flag used to show if printing worked*/
	private boolean pageSuccessful=true;

    /**flag to show if YCCK images*/
    private boolean hasYCCKimages=false;

	/**flag to show if non-embedded CID fonts*/
	private boolean hasNonEmbeddedCIDFonts=false;

	/**and the list of CID fonts*/
	private StringBuffer nonEmbeddedCIDFonts=new StringBuffer();

	/**Any printer errors*/
	private String pageErrorMessages="";

	/** last text stream decoded*/
	private StringBuffer textData;

	/**interactive display*/
	private StatusBar statusBar=null;

	/**get percentage of gap required to create a space 1=100% */
	public static float currentThreshold = 0.6f; //referenced in Decoder if changed
	public static Map currentThresholdValues; //referenced in Decoder if changed

	/**flag to engage text printing*/
	private int textPrint=0;

    //used to clip and adjust scaling for images
    int minX=0, minY=0,maxX=-1,maxY=-1;

	//last Trm incase of multple Tj commands
	protected boolean multipleTJs =false;

	/**provide access to pdf file objects*/
	private PdfObjectReader currentPdfFile;

	/**flag to show if image is for clip*/
	private boolean isClip = false;

	/**thousand as a value*/
	final private static float THOUSAND=1000;

	/**flag to show content is being rendered*/
	private boolean renderText=false;

	/**flag to show content is being rendered*/
	private boolean renderImages=false;

	/**copy of flag to tell program whether to create
	 * (and possibly update) screen display
	 */
	private boolean renderPage = false;

	/**flag to show raw images extracted*/
	private boolean rawImagesExtracted=true;

	/**flag to show raw images extracted*/
	private boolean finalImagesExtracted=true;

	/**flag to allow Xform metadata to be extracted*/
	private boolean xFormMetadata=true;

	/**flag to show raw images extracted*/
	private boolean clippedImagesExtracted=true;
                                                      
	private boolean   markedContentExtracted=false;

	//<start-adobe>
	private StructuredContentHandler contentHandler;
	//<end-adobe>

	/**flag to show text is being extracted*/
	private boolean textExtracted=true;

	/**flags to show we need colour data as well*/
	private boolean textColorExtracted=false,colorExtracted=false;

	/**start of ascii escape char*/
	private static final String[] hex={"&#0;","&#1;","&#2;","&#3;",
		"&#4;","&#5;","&#6;","&#7;","&#8;","&#9;","&#10;","&#11;",
		"&#12;","&#13;","&#14;","&#15;","&#16;","&#17;","&#18;","&#19;",
		"&#20;","&#21;","&#22;","&#23;","&#24;","&#25;","&#26;",
		"&#27;","&#28;","&#29;","&#30;","&#31;"};

	/**store text data and can be passed out to other classes*/
	private PdfData pdfData = new PdfData();

	/**current XML tag*/
	private String font_as_string="";

	/**stroke colorspace*/
	protected GenericColorSpace strokeColorSpace=new DeviceRGBColorSpace();

	/**nonstroke colorspace*/
	protected GenericColorSpace nonstrokeColorSpace=new DeviceRGBColorSpace();

	/**flag to show if we physical generate a scaled version of the
	 * images extracted*/
	private  boolean createScaledVersion = true;

	/**store image data extracted from pdf*/
	private PdfImageData pdfImages=new PdfImageData();

	/**token counter so we can work out order objects drawn*/
	private int tokenNumber = 0;

	/**flag to show if page content or a substream*/
	private boolean isPageContent = true;

	/**flag to show if stack setup*/
	private boolean isStackInitialised=false;

	/**stack for graphics states*/
	private Vector_Object graphicsStateStack;

	/**stack for graphics states*/
	private Vector_Object strokeColorStateStack;

	/**stack for graphics states*/
	private Vector_Object nonstrokeColorStateStack;

	/**stack for graphics state clips*/
	private Vector_Object clipStack;
	
	/**custom upscale val for JPedal settings*/
    private float multiplyer = 1;

	/**stack for graphics states*/
	private Vector_Object textStateStack;

    /**used to debug*/
    private String indent="";

	/**horizontal and vertical lines on page*/
	private PageLines pageLines=new PageLines();

	/**current shape object being drawn note we pass in handle on pageLines*/
	private PdfShape currentDrawShape=new PdfShape();

	/**maximum ops*/
	private static final int MAXOPS=50;

	/**current op*/
	private int currentOp=0;

	/**actual numbe rof operands read*/
	private int operandCount=0;

	/**images on page*/
	private int imageCount=0;

	/**lookup table for operands on commands*/
	private String[] operand= new String[MAXOPS];
	private int[] opStart= new int[MAXOPS];
	private int[] opEnd= new int[MAXOPS];

	/**flag to show type of move command being executed*/
	protected int moveCommand = 0; //0=t*, 1=Tj, 2=TD

	/**current font for text*/
	private String currentFont = "";

	/**name of current image in pdf*/
	private String currentImage = "";

	private Map globalXObjects = new Hashtable(),localXObjects=new Hashtable();

	/**gap between characters*/
	protected float charSpacing = 0;

	/**current graphics state - updated and copied as file decode*/
	protected GraphicsState gs=new GraphicsState();

	/**current text state - updated and copied as file decode*/
	protected TextState currentTextState = new TextState();

	/**used to store font information from pdf and font functionality*/
	protected PdfFont currentFontData;

	/**GS*/
	private Map GraphicsStates=new HashMap();

	/**fonts*/
	private Map unresolvedFonts=new Hashtable();
	private Map resolvedFonts=new Hashtable();

	/**colors*/
	private Map colorspaces=new HashMap();
	private Map colorspacesObjects=new HashMap();
	private Map XObjectColorspaces=new HashMap();
    private  Map colorspacesSeen=new HashMap();

	private Map patterns=new HashMap(),shadings=new HashMap();

	/**length of current text fragment*/
	private int textLength = 0;

	/**flag to show we use hi-res images to draw onscreen*/
	private boolean useHiResImageForDisplay=false;

	/**co-ords (x1,y1 is top left corner)*/
	private float x1, y1, x2, y2;

	/** holds page information used in grouping*/
	private PdfPageData pageData = new PdfPageData();

	protected DynamicVectorRenderer current;

	/** constant for conversion*/
	static final double radiansToDegrees=180f/Math.PI;

	public static final int UNSET = 0;

	public static final int PATTERN = 1;

	/**used in grouping rotated text by Storypad*/
	double rotationAsRadians=0;

	protected GlyphFactory factory=null;
	/**

    /**page number*/
	private int pageNum;

	/**list of fonts used for display*/
	private String fontsInFile;

	/**xml color tag to show color*/
	private String currentColor=GenericColorSpace.cb+"C='1' M='1' Y='1' K='1'>";

	/**max width of type3 font*/
	private int T3maxWidth,T3maxHeight;

	/**allows uasge of old approximation for height (Deprecated so please advise IDRsolutions if being used)*/
	private boolean legacyTextMode=false;

	private boolean extractRawCMYK=false;

	/**flag to show we are drawing directly to g2 and not storing to render later*/
	protected boolean renderDirectly;

	/**hook onto g2 if we render directly*/
	protected Graphics2D g2;

	/**clip if we render directly*/
	private Shape defaultClip;

	/**flag to show embedded fonts present*/
	private boolean hasEmbeddedFonts=false;

	/**flag to show if images are included in datastream*/
	private boolean includeImagesInData=false;

	/**track font size*/
	protected int lastFontSize=-1;

	/**shows if t3 glyph uses internal colour or current colour*/
	public boolean ignoreColors=false;

	/**flag to show being used for printing onto G2*/
	private boolean isPrinting;

	private String fileName="";

	private ObjectStore objectStoreStreamRef;

	/**tracks name of xform in case we need to save*/
	private String lastFormID=null;

	private int pageH;

	private int formLevel=0;

	private float topLevelStrokeAlpha=1f, topLevelNonStrokeAlpha=1f;
	private float nonStrokeAlpha=1f, strokeAlpha=1f;

	private boolean imagesProcessedFully;

	/**flag to show we keep raw stream from objects*/
	private boolean keepRaw=false;

	public static boolean runningStoryPad=false;

	final private static float[] matches={1f,0f,0f,1f,0f,0f}; 	

	private Map scalings=new HashMap();

	private boolean isType3Font;

	/** hold values used in TR transfer function*/
	private Map TRPDFfunctionsCache=new HashMap();
	private Map imposedImages;

	public static boolean showInvisibleText=false;

	public static boolean useTextPrintingForNonEmbeddedFonts=false;
	private int currentRotation=0;

	//use in rotation handling to handle minor issues
	float unRotatedY =-1,lastHeight=-1;

	//used to handle rounding errors on rotated text
	private float rotatedY;

	//show if we include rotation
	public static boolean includeRotation=false;


	static{

		SamplingFactory.setDownsampleMode(null);

	}

	private Map lines=new HashMap();

	private int streamType=PdfStreamDecoder.UNSET;

    /**
     * only used by Pattern Colorspace to pass in cached values
     * 
     */
    public PdfStreamDecoder(Map colorspacesObjects) {
        this.colorspacesObjects=colorspacesObjects;
    }


	/**
	 * NOT PART OF API
	 *
	 * to replace ObjectStore.getCurrentFileName()
	 *
	 * <b>for internal use</b>
	 */
	public void setName(String name){

		if(name!=null){
			this.fileName=name.toLowerCase();

			/**check no separators*/
			int sep=fileName.lastIndexOf(47); // '/'=47
			if(sep!=-1)
				fileName=fileName.substring(sep+1);
			sep=fileName.lastIndexOf(92); // '\\'=92
			if(sep!=-1)
				fileName=fileName.substring(sep+1);
			sep=fileName.lastIndexOf(46); // "."=46
			if(sep!=-1)
				fileName=fileName.substring(0,sep);
		}
	}

	/** should be called after constructor or other methods may not work
	 * <p>Also initialises DynamicVectorRenderer*/
	public void setStore(ObjectStore newObjectRef){
		objectStoreStreamRef = newObjectRef;

		current=new DynamicVectorRenderer(this.pageNum,objectStoreStreamRef,isPrinting);
		current.setHiResImageForDisplayMode(useHiResImageForDisplay);

		//<start-jfr>
		if(customImageHandler!=null && current!=null)
			current.setCustomImageHandler(customImageHandler);
		//<end-jfr>

	}


	/**/

	public PdfStreamDecoder(PdfObjectReader currentPdfFile,boolean useHires,boolean isType3Font){


		StandardFonts.checkLoaded(StandardFonts.STD);

		//lazy init on t1
		if(factory==null)
			factory=new org.jpedal.fonts.glyph.T1GlyphFactory();

		this.currentPdfFile=currentPdfFile;
		this.useHiResImageForDisplay=useHires;

		this.isType3Font=isType3Font;

		String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
		this.rejectSuperimposedImages=(operlapValue!=null && operlapValue.toLowerCase().indexOf("true")!=-1);

        String nodownsamplesharpen=System.getProperty("org.jpedal.sharpendownsampledimages");
        if(nodownsamplesharpen!=null)
            this.sharpenDownsampledImages=(nodownsamplesharpen.toLowerCase().indexOf("true")!=-1);
	}

	public PdfStreamDecoder(PdfObjectReader currentPdfFile){

		StandardFonts.checkLoaded(StandardFonts.STD);

		//lazy init on t1
		if(factory==null)
			factory=new org.jpedal.fonts.glyph.T1GlyphFactory();

		this.currentPdfFile=currentPdfFile;

		String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
		this.rejectSuperimposedImages=(operlapValue!=null && operlapValue.toLowerCase().indexOf("true")!=-1);

        String nodownsamplesharpen=System.getProperty("org.jpedal.sharpendownsampledimages");
        if(nodownsamplesharpen!=null)
            this.sharpenDownsampledImages=(nodownsamplesharpen.toLowerCase().indexOf("true")!=-1);

	}

	public PdfStreamDecoder(){

		StandardFonts.checkLoaded(StandardFonts.STD);

		//lazy init on t1
		if(factory==null)
			factory=new org.jpedal.fonts.glyph.T1GlyphFactory();

		String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
		rejectSuperimposedImages=(operlapValue!=null && operlapValue.toLowerCase().indexOf("true")!=-1);

        String nodownsamplesharpen=System.getProperty("org.jpedal.sharpendownsampledimages");
        if(nodownsamplesharpen!=null)
        this.sharpenDownsampledImages=(nodownsamplesharpen.toLowerCase().indexOf("true")!=-1);
	}



	public void print(Graphics2D g2,AffineTransform scaling,boolean showImageable, int currentPrintPage){

		if(showImageable)
			current.setBackgroundColor(null);
		current.setPrintPage(currentPrintPage);
		current.paint(g2,null,scaling,null,false,false);
	}

	/**
	 * create new StreamDecoder to create screen display with hires images
	 */
	public PdfStreamDecoder(boolean useHiResImageForDisplay, PdfLayerList layers) {

		this.layers=layers;

		StandardFonts.checkLoaded(StandardFonts.STD);

		//lazy init on t1
		if(factory==null)
			factory=new org.jpedal.fonts.glyph.T1GlyphFactory();


		String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
		rejectSuperimposedImages=(operlapValue!=null && operlapValue.toLowerCase().indexOf("true")!=-1);

        String nodownsamplesharpen=System.getProperty("org.jpedal.sharpendownsampledimages");
        if(nodownsamplesharpen!=null)
            this.sharpenDownsampledImages=(nodownsamplesharpen.toLowerCase().indexOf("true")!=-1);
	}

	/**used internally to allow for colored streams*/
	public void setDefaultColors(PdfPaint strokeCol, PdfPaint nonstrokeCol) {

		this.strokeColorSpace.setColor(strokeCol);
		this.nonstrokeColorSpace.setColor(nonstrokeCol);
		gs.setStrokeColor(strokeCol);
		gs.setNonstrokeColor(nonstrokeCol);
	}

	/**method ensures images rendered as ARGB rather than RGB. Used internally
	 * to ensure prints correctly on some files. Not recommended for
	 * external usage.
	 */
	public void optimiseDisplayForPrinting(){
		isPrinting=true;
	}

	/**return the data*/
	public PdfData getText(){
		return  pdfData;
	}

	/**return the data*/
	public PdfImageData getImages(){
		return  pdfImages;
	}

	final private BufferedImage processImageXObject(
			PdfObject XObject,
			String image_name,
			boolean createScaledVersion,
			byte[] objectData, boolean saveRawData) throws PdfException {
		
		LogWriter.writeMethod("{processImageXObject}",0);

		boolean imageMask = false;

		BufferedImage image=null;

		//add filename to make it unique
		image_name = fileName+ '-' + image_name;

		int depth=1;

		int width = XObject.getInt(PdfDictionary.Width);
		int height = XObject.getInt(PdfDictionary.Height);
		int newDepth = XObject.getInt(PdfDictionary.BitsPerComponent);
		if(newDepth!=PdfDictionary.Unknown)
			depth=newDepth;

		isMask= XObject.getBoolean(PdfDictionary.ImageMask);
		imageMask=isMask;

		PdfObject ColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);

        if(1==2){
        	if(XObject!=null)
        	System.out.println(image_name+" XObject="+XObject.getObjectRefAsString());
            
        	if(ColorSpace!=null )
        	System.out.println("Colorspace="+ColorSpace.getObjectRefAsString());
        }
        
		//handle colour information
		GenericColorSpace decodeColorData=new DeviceRGBColorSpace();

		if(ColorSpace!=null)
			decodeColorData=ColorspaceFactory.getColorSpaceInstance(isPrinting,currentPdfFile, 
					ColorSpace, XObjectColorspaces,colorspacesSeen, true);

        //pass through decode params
        PdfObject parms=XObject.getDictionary(PdfDictionary.DecodeParms);
        if(parms!=null)
        decodeColorData.setDecodeParms(parms);

		//set any intent
		String intent=XObject.getName(PdfDictionary.Intent);
		decodeColorData.setIntent(intent);

		//tell user and log
		LogWriter.writeLog("Processing XObject: "+ image_name+ " width="+ width+ " Height="+ height+ 
				" Depth="+ depth+ " colorspace="+ decodeColorData);

		/**
		 * allow user to process image
		 */
		if(customImageHandler!=null)
			image=customImageHandler.processImageData(gs,XObject, ColorSpace);

		//extract and process the image
		if(customImageHandler==null ||(image==null && !customImageHandler.alwaysIgnoreGenericHandler()))
			image=processImage(decodeColorData,
					objectData,
					image_name,
					width,
					height,
					depth,
					imageMask,
					createScaledVersion,XObject, saveRawData);


		return image;



	}

	/**
	 * read in the image and process and save raw image
	 */
	final private BufferedImage processImage(GenericColorSpace decodeColorData,
			byte[] data,String name,
			int w,int h,int d,boolean imageMask,
			boolean createScaledVersion, PdfObject XObject, boolean saveRawData ) throws PdfException {

		if (LogWriter.debug)
			LogWriter.writeMethod("{process_image} imageMask="+imageMask);

        //track its use
        colorspacesUsed.put(new Integer(decodeColorData.getID()),"x");

        int rawd=d;

        int sampling=1,newW=0,newH=0;
			        
		float[] decodeArray=XObject.getFloatArray(PdfDictionary.Decode);

		if (LogWriter.debug && decodeArray!=null){
			String val="";
			for(int jj=0;jj<decodeArray.length;jj++)
				val=val+" "+decodeArray[jj];
			LogWriter.writeMethod("decodeArray="+val);
		}

		PdfArrayIterator Filters = XObject.getMixedArray(PdfDictionary.Filter);

		boolean isDCT=false,isJPX=false;
		//check not handled elsewhere
		int firstValue=PdfDictionary.Unknown;
		if(Filters!=null && Filters.hasMoreTokens()){
			while(Filters.hasMoreTokens()){
				firstValue=Filters.getNextValueAsConstant(true);
				isDCT=firstValue==PdfFilteredReader.DCTDecode;
				isJPX=firstValue==PdfFilteredReader.JPXDecode;
			}

		}else
			Filters=null;

		boolean removed=false, isDownsampled=false;

		BufferedImage image = null;
		String type = "jpg";

		int colorspaceID=decodeColorData.getID();

		int compCount = decodeColorData.getColorSpace().getNumComponents();

		int pX=0,pY=0;
		
		/**setup any imageMask*/
		byte[] maskCol =new byte[4];
		if (imageMask)
			getMaskColor(maskCol);

		/**setup sub-sampling*/
		if(renderPage && streamType!=PATTERN){
			
			if(SamplingFactory.downsampleLevel== SamplingFactory.high || getSamplingOnly){// && w>500 && h>500){ // ignore small items

				//ensure all positive for comparison
				float[][] CTM=new float[3][3];
				for(int ii=0;ii<3;ii++){
					for(int jj=0;jj<3;jj++){
						if(gs.CTM[ii][jj]<0)
							CTM[ii][jj]=-gs.CTM[ii][jj];
						else
							CTM[ii][jj]=gs.CTM[ii][jj];
					}
				}

				if(CTM[0][0]==0 || CTM[0][0]<CTM[0][1])
					pX=(int) (CTM[0][1]);
				else
					pX=(int) (CTM[0][0]);

				if(CTM[1][1]==0 || CTM[1][1]<CTM[1][0])
					pY=(int) (CTM[1][0]);
				else
					pY=(int) (CTM[1][1]);

				//don't bother on small items
				if(!getSamplingOnly &&(w<500 || h<500)){ //change??

                    pX=0;//pageData.getCropBoxWidth(this.pageNum);
					pY=0;//pageData.getCropBoxHeight(this.pageNum);
				}

			}else if(SamplingFactory.downsampleLevel==SamplingFactory.medium){
				pX=pageData.getCropBoxWidth(this.pageNum);
				pY=pageData.getCropBoxHeight(this.pageNum);
			}
		}

		if(PdfDecoder.debugHiRes)
		System.out.println("pX="+pX+" pY="+pY+" w="+w+" h="+h+" *PdfDecoder.multiplyer="+multiplyer);

		PdfObject DecodeParms=XObject.getDictionary(PdfDictionary.DecodeParms), newMask=null, newSMask=null;

		newMask=XObject.getDictionary(PdfDictionary.Mask);
		newSMask=XObject.getDictionary(PdfDictionary.SMask);

        //flag masks
        if(newMask!=null || newSMask!=null)
        LogWriter.writeLog("newMask= "+ newMask + " newSMask="+ newSMask);

		//work out if inverted (assume true and disprove)
		//work this into saved data @mariusz so 125% works
		boolean arrayInverted=false;
		if(decodeArray!=null){

			arrayInverted=true;
			int count=decodeArray.length;
			for(int aa=0;aa<count;aa=aa+2){
				if(decodeArray[aa]==1f && decodeArray[aa+1]==0f){
					//okay
				}else{
					arrayInverted=false;
					aa=count;
				}
			}
		}

		/**
		 * down-sample size if displaying (some cases excluded at present)
		 */
		if(renderPage && 
				newMask==null &&
				decodeColorData.getID()!=ColorSpaces.ICC &&
				(arrayInverted || decodeArray==null || decodeArray.length==0)&&
				(d==1 || d==8) 
				&& pX>0 && pY>0 && (SamplingFactory.isPrintDownsampleEnabled || !isPrinting)){
//			@speed - debug

			//see what we could reduce to and still be big enough for page
			newW=w;
            newH=h;


			//if smask smaller scale down to fit
			if(newSMask!=null){
				int smaskW=newSMask.getInt(PdfDictionary.Width);
				int smaskH=newSMask.getInt(PdfDictionary.Height);

				if(smaskW<w || smaskH<h){
					w=smaskW;
					h=smaskH;
				}
			}

			int smallestH=pY<<2; //double so comparison works
			int smallestW=pX<<2;

			//cannot be smaller than page
			while(newW>smallestW && newH>smallestH){
				sampling=sampling<<1;
				newW=newW>>1;
				newH=newH>>1;
			}

			int scaleX=w/pX;
			if(scaleX<1)
				scaleX=1;

			int scaleY=h/pY;
			if(scaleY<1)
				scaleY=1;

			//choose smaller value so at least size of page
			sampling=scaleX;
			if(sampling>scaleY)
				sampling=scaleY;

        }

        
            //get sampling and exit from this code as we don't need to go further
            if(getSamplingOnly){// && pX>0 && pY>0){

                float scaleX=(((float)w)/pX);
                float scaleY=(((float)h)/pY);

                if(PdfDecoder.debugHiRes)
                System.out.println("sampling="+sampling+" "+scaleX+" "+scaleY);

                if(scaleX<scaleY)
                    samplingUsed=scaleX;
                else
                    samplingUsed=scaleY;

                //we may need to check mask as well

                boolean checkMask=false;
                if(newSMask!=null){

				    /**read the stream*/
				    byte[] objectData =currentPdfFile.readStream(newSMask,true,true,keepRaw, false,false, null);

				    if(objectData!=null){

                        if(DecodeParms==null)
						    DecodeParms=newSMask.getDictionary(PdfDictionary.DecodeParms);

                        int maskW=newSMask.getInt(PdfDictionary.Width);
						int maskH=newSMask.getInt(PdfDictionary.Height);

                        //if all white image with mask, use mask
                        boolean isDownscaled=maskW/2>w && maskH/2>h;

					    boolean ignoreMask=isDownscaled && 
					    DecodeParms!=null &&  
					    DecodeParms.getInt(PdfDictionary.Colors)!=-1 &&  
					    DecodeParms.getInt(PdfDictionary.Predictor)!=15;

					    
                        //ignoreMask is hack to fix odd Visuality files
                        if(!ignoreMask)
                               checkMask=true;
                    }
				   
                }

                 if(!checkMask){

                    //getSamplingOnly=false;

                    return null;
                 }
            }
            
            if(PdfDecoder.debugHiRes)
                            System.out.println("sampling = "+sampling);
        {
            
        	
            if(sampling>1 && multiplyer>1){
				
				//samplingUsed= sampling;

                sampling = (int) (sampling/ multiplyer);
				
				if(PdfDecoder.debugHiRes)
				System.out.println("reset sampling to "+sampling);
			}
            

            //switch to 8 bit and reduce bw image size by averaging
			if(sampling>1){

				isDownsampled=true;

				byte[] index=decodeColorData.getIndexedMap();

				newW=w/sampling;
				newH=h/sampling;

				if(PdfDecoder.debugHiRes)
				System.out.println("width="+w+" "+newW+"sampling="+sampling+" d="+d);

                if(d==1){

                    //save raw 1 bit data
					//code in DynamicVectorRenderer may need alignment if it changes
					if(!imageMask && !saveRawData){

						//copy and turn upside down first
						int count=data.length;

						byte[] turnedData=new byte[count];
						System.arraycopy(data,0,turnedData,0,count);

//						turnedData=ImageOps.invertImage(turnedData, w, h, d, 1, null);

						boolean isInverted=!doNotRotate && (renderDirectly || useHiResImageForDisplay) && DynamicVectorRenderer.isInverted(gs.CTM);
			    		boolean isRotated=!doNotRotate && (renderDirectly || useHiResImageForDisplay) && DynamicVectorRenderer.isRotated(gs.CTM);

						if(renderDirectly){
							isInverted=false;
							isRotated=false;
						}
						
						//I optimised the code slightly - you were setting booleans are they had been
						//used - I removed so it keeps code shorter

						if(isInverted)//invert at byte level with copy
							turnedData=ImageOps.invertImage(turnedData, w, h, d, 1, index);
						
						if(isRotated){ //rotate at byte level with copy New Code still some issues
							turnedData=ImageOps.rotateImage(turnedData, w, h, d, 1, index);

							//reset
							int temp = h;
							h=w;
							w=temp;

							temp = pX;
							pX=pY;
							pY=temp;
							
						}
						
						//invert all the bits if needed before we store
						if(arrayInverted){
							for(int aa=0;aa<count;aa++)
								turnedData[aa]= (byte) (turnedData[aa]^255);
						}
						
						
						
						Integer pn = new Integer(this.pageNum);
						Integer iC = new Integer(imageCount);
						String key = pn.toString() + iC.toString();
						current.getObjectStore().saveRawImageData(key,turnedData,w,h,pX,pY);

						if(isRotated){
							//reset
							int temp = h;
							h=w;
							w=temp;

							temp = pX;
							pX=pY;
							pY=temp;
						}
					}

					//make 1 bit indexed flat
					if(index!=null)
						index=decodeColorData.convertIndexToRGB(index);

					int size=newW*newH;

					if(imageMask){
						size=size*4;
						maskCol[3]=(byte)255;
					}else if(index!=null)
						size=size*3;

					byte[] newData=new byte[size];

					final int[] flag={1,2,4,8,16,32,64,128};

					int origLineLength= (w+7)>>3;

					int bit;
					byte currentByte;

					//scan all pixels and down-sample
					for(int y=0;y<newH;y++){
						for(int x=0;x<newW;x++){

							int bytes=0,count=0;

							//allow for edges in number of pixels left
							int wCount=sampling,hCount=sampling;
							int wGapLeft=w-x;
							int hGapLeft=h-y;
							if(wCount>wGapLeft)
								wCount=wGapLeft;
							if(hCount>hGapLeft)
								hCount=hGapLeft;

							//count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
							for(int yy=0;yy<hCount;yy++){
								for(int xx=0;xx<wCount;xx++){

									currentByte=data[((yy+(y*sampling))*origLineLength)+(((x*sampling)+xx)>>3)];	


									if(imageMask && !arrayInverted)
										currentByte=(byte) (currentByte ^ 255);

									bit=currentByte & flag[7-(((x*sampling)+xx)& 7)];

									if(bit!=0)
										bytes++;
									count++;
								}
							}

                            //set value as white or average of pixels
							int offset=x+(newW*y);
							if(count>0){								
								if(imageMask){
                                    //System.out.println("xx");
									for(int ii=0;ii<4;ii++){
										if(arrayInverted)
											newData[(offset*4)+ii]=(byte)(255-(((maskCol[ii] & 255)*bytes)/count));
										else
											newData[(offset*4)+ii]=(byte)((((maskCol[ii] & 255)*bytes)/count));
										//System.out.println(newData[(offset*4)+ii]+" "+(byte)(((maskCol[ii] & 255)*bytes)/count);

									}
                                }else if(index!=null && d==1){
                                    int av;

                                    for(int ii=0;ii<3;ii++){
                                        av=(index[ii] & 255) +(index[ii+3] & 255);
                                        //can be in either order so look at index
                                        if(decodeColorData.getID()==ColorSpaces.DeviceRGB && index[0]==-1 && index[1]==-1 && index[2]==-1)
                                            newData[(offset*3)+ii]=(byte)(255-((av *bytes)/count));
                                        else
                                            newData[(offset*3)+ii]=(byte)((av *bytes)/count);
                                            
                                    }
								}else if(index!=null){
                                    for(int ii=0;ii<3;ii++)
										newData[(offset*3)+ii]=(byte)(((index[ii] & 255)*bytes)/count);
                                }else
									newData[offset]=(byte)((255*bytes)/count);
							}else{

								if(imageMask){
									for(int ii=0;ii<3;ii++)
										newData[(offset*4)+ii]=(byte)0;

								}else if(index!=null){
									for(int ii=0;ii<3;ii++)
										newData[((offset)*3)+ii]=0;
								}else
									newData[offset]=(byte) 255;							
							}
						}
					}

					data=newData;

					if(index!=null)
						compCount=3;

					h=newH;
					w=newW;
					decodeColorData.setIndex(null, 0);
					d=8;

					//imageMask=false;

				}else if(d==8 && (Filters==null || (!isDCT && !isJPX))){

					boolean hasIndex=decodeColorData.getIndexedMap()!=null;

					int oldSize=data.length;


					int x=0,y=0,xx=0,yy=0,jj=0,comp=0,origLineLength=0;
					try{

						if(hasIndex)
							comp=1;
						else
							comp=decodeColorData.getColorComponentCount();

						//black and white
						if(w*h==oldSize || decodeColorData.getID()==ColorSpaces.DeviceGray)
							comp=1;

						byte[] newData=new byte[newW*newH*comp];

						//System.err.println(w+" "+h+" "+data.length+" comp="+comp+" scaling="+sampling+" "+decodeColorData);

						origLineLength= w*comp;

						//System.err.println("size="+w*h*comp+" filter"+filter+" scaling="+sampling+" comp="+comp);
						//System.err.println("w="+w+" h="+h+" data="+data.length+" origLineLength="+origLineLength+" sampling="+sampling);
						//scan all pixels and down-sample
						for(y=0;y<newH;y++){
							for(x=0;x<newW;x++){

								//allow for edges in number of pixels left
								int wCount=sampling,hCount=sampling;
								int wGapLeft=w-x;
								int hGapLeft=h-y;
								if(wCount>wGapLeft)
									wCount=wGapLeft;
								if(hCount>hGapLeft)
									hCount=hGapLeft;

								for(jj=0;jj<comp;jj++){
									int byteTotal=0,count=0,ptr;
									//count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
									for(yy=0;yy<hCount;yy++){
										for(xx=0;xx<wCount;xx++){

											ptr=((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj));
											if(ptr<oldSize){
												byteTotal=byteTotal+(data[ptr] & 255);

												count++;
											}
										}
									}

									//set value as white or average of pixels
									if(count>0)
										//if(index==null)
										newData[jj+(x*comp)+(newW*y*comp)]=(byte)((byteTotal)/count);
									//else
									//	newData[x+(newW*y)]=(byte)(((index[1] & 255)*byteTotal)/count);
									else{
										//if(index==null)
										//newData[jj+x+(newW*y*comp)]=(byte) 255;
										//else
										//	newData[x+(newW*y)]=index[0];
									}
								}
							}
						}

						data=newData;
						h=newH;
						w=newW;

					}catch(Exception e){

					}
				}else if(!isDCT && !isJPX && index==null){
				}
			}
		}

		/**handle any decode array*/
		if(decodeArray==null || decodeArray.length == 0){
		}else if(Filters!=null &&(isJPX||isDCT)){ //don't apply on jpegs
		}else
			applyDecodeArray(data, d, decodeArray,colorspaceID);

		if (imageMask) {/** create an image from the raw data*/

			//allow for 1 x 1 pixel
			/**
			 * allow for 1 x 1 pixels scaled up
			 */
			if(w==1 && h==1 && data[0]!=0){

				float ih=gs.CTM[1][1];
				if(ih==0)
					ih=gs.CTM[1][0];
				if(ih<0)
					ih=-ih;

				float iw=gs.CTM[0][0];
				if(iw==0)
					iw=gs.CTM[0][1];
				if(iw<0)
					iw=-iw;

				//allow for odd values less than 1
				if(iw<1)
					iw=1;
				if(ih<1)
					ih=1;

				image=new BufferedImage((int)iw,(int)ih,BufferedImage.TYPE_INT_ARGB);

				Graphics2D img2g=image.createGraphics();

				img2g.setPaint(nonstrokeColorSpace.getColor());
				img2g.fillRect(0,0,(int)iw,(int)ih);

			}else{

				//see if black and back object

				if(isDownsampled){
					/** create an image from the raw data*/
					DataBuffer db = new DataBufferByte(data, data.length);

					int[] bands = {0,1,2,3};
					image =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
					Raster raster =Raster.createInterleavedRaster(db,w,h,w * 4,4,bands,null);
					image.setData(raster);

					// System.out.println("w="+w+" h="+h);
					// ShowGUIMessage.showGUIMessage("x",image,"x");
				}else{

					//try to keep as binary if possible
					boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);

					if(maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0 && !hasObjectBehind && !this.isType3Font){

						if(d==1){
							WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, 1, null);
							image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
							image.setData(raster);

						}else{ //down-sampled above //never called
							final int[] bands = {0};

							//WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(newData, newData.length), newW, newH, 1, null);
							Raster raster =Raster.createInterleavedRaster(new DataBufferByte(data, data.length),w,h,w,1,bands,null);

							image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
							image.setData(raster);

						}
						
					}else{

						//if(hasObjectBehind){
						//image=ColorSpaceConvertor.convertToARGB(image);
						if(d==8 && isDownsampled){ //never called

							byte[] index={(byte)(maskCol[0]),(byte)(maskCol[1]), (byte) (maskCol[2]),(byte)255,(byte)255,(byte)255};
							image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, index.length,true,true);
                        }else if((w<2000 && h<2000)|| hasObjectBehind){   //needed for hires
							byte[] index={maskCol[0],maskCol[1],maskCol[2],(byte)255,(byte)255,(byte)255};
							image = ColorSpaceConvertor.convertIndexedToFlat(1,w, h, data, index, index.length,true,false);
                            //}

                       /**}else{
						
                        WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, d, null);
                        image = new BufferedImage(new IndexColorModel(d, 1, maskCol, 0, false), raster, false, null);
                        /**/
						}
					}
                }
			}
		}else if (Filters == null) { //handle no filters

			//save out image
			LogWriter.writeLog("Image "+ name+ ' ' + w+ "W * "+ h+ "H with No Compression at BPC "+ d);

			image =makeImage(decodeColorData,w,h,d,data,compCount);

		} else if (isDCT) { //handle JPEGS

			LogWriter.writeLog("JPeg Image "+ name+ ' ' + w+ "W * "+ h+ 'H'+" arrayInverted="+arrayInverted);

			/**
			 * get image data,convert to BufferedImage from JPEG & save out
			 */
			if(colorspaceID== ColorSpaces.DeviceCMYK){
				if(extractRawCMYK){
					LogWriter.writeLog("Raw CMYK image " + name + " saved.");
					if(!objectStoreStreamRef.saveRawCMYKImage(data, name))
						addPageFailureMessage("Problem saving Raw CMYK image "+name);
				}
			}

            /**
            try {
                java.io.FileOutputStream a =new java.io.FileOutputStream("/Users/markee/Desktop/"+ name + ".jpg");

                a.write(data);
                a.flush();
                a.close();

            } catch (Exception e) {
                LogWriter.writeLog("Unable to save jpeg " + name);

            }  /**/

			//separation, renderer
			try{
				image=decodeColorData.JPEGToRGBImage(data, w, h, decodeArray,pX , pY , arrayInverted);

                //flag if YCCK
                if(decodeColorData.isImageYCCK())
                hasYCCKimages=true;

				removed=ColorSpaceConvertor.wasRemoved;

                //image=simulateOP(image);
            }catch(Exception e){
				addPageFailureMessage("Problem converting "+name+" to JPEG");
				e.printStackTrace();
                image.flush();
				image=null;
			}/**catch(Error err){
				addPageFailureMessage("Problem converting "+name+" to JPEG");
				//e.printStackTrace();
				image=null;
			}/**/

			type = "jpg";
		}else if(isJPX){ //needs imageio library

			LogWriter.writeLog("JPeg 2000 Image "+ name+ ' ' + w+ "W * "+ h+ 'H');

			if(JAIHelper.isJAIused()){

				image = decodeColorData.JPEG2000ToRGBImage(data,w,h,decodeArray,pX,pY);

				type = "jpg";
			}else{
				throw new RuntimeException("JPeg 2000 Images need both JAI (imageio.jar) on classpath, " +
				"and the VM parameter -Dorg.jpedal.jai=true switch turned on");
			}

		} else { //handle other types
			LogWriter.writeLog(name+ ' ' + w+ "W * "+ h+ "H BPC="+d+" "+decodeColorData);

			image =makeImage(decodeColorData,w,h,d,data,compCount);

			//choose type on basis of size and avoid ICC as they seem to crash the Java class
			if (d == 8 || nonstrokeColorSpace.getID()== ColorSpaces.DeviceRGB || nonstrokeColorSpace.getID()== ColorSpaces.ICC)
				type = "jpg";
		}

		if (image != null) {

            /**handle any soft mask*/
			if(newSMask!=null){

				BufferedImage smaskImage=null;

				/**read the stream*/
				byte[] objectData =currentPdfFile.readStream(newSMask,true,true,keepRaw, false,false, null);

				if(objectData!=null){

					if(DecodeParms==null)
						DecodeParms=newSMask.getDictionary(PdfDictionary.DecodeParms);

					boolean ignoreMask=DecodeParms!=null &&  DecodeParms.getInt(PdfDictionary.Colors)!=-1
                            &&  DecodeParms.getInt(PdfDictionary.Predictor)!=15 && decodeColorData.getID()!=ColorSpaces.ICC;

					//ignoreMask is hack to fix odd Visuality files
					if(!ignoreMask){

						int rawOptions=optionsApplied;

						if(optionsApplied==PDFImageProcessing.NOTHING)
							doNotRotate=true;

						int maskW=newSMask.getInt(PdfDictionary.Width);
						int maskH=newSMask.getInt(PdfDictionary.Height);

						boolean isWhiteAndDownscaled=false;
						
                  
                        if(isWhiteAndDownscaled){
                        	
                        	PdfObject XObjectColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);
                    		PdfObject maskColorSpace=newSMask.getDictionary(PdfDictionary.ColorSpace);
                    		
                    		isWhiteAndDownscaled=XObjectColorSpace!=null &&
                    		XObjectColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceRGB &&
                    		maskColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceGray;

                        }
                        
                        if(isWhiteAndDownscaled  && (isDCT || isJPX)){
                        	
                            //invert and get image
                        	int c=objectData.length;
                    		for(int ii=0;ii<c;ii++)
                    			objectData[ii]= (byte) (((byte)255)-objectData[ii]);
                        	
                            image =processImageXObject(newSMask,name,false,objectData,true);

                        }else{

							//process the image and save raw version
							smaskImage =processImageXObject(newSMask,name,false,objectData,true);
	
							//restore
							doNotRotate=false;
							optionsApplied=rawOptions;
	
							//apply mask
							if(smaskImage!=null){
							image=applySmask(image,smaskImage,newSMask);
                               smaskImage.flush();
                               smaskImage=null;
                            }
                        }
					}
				}

				/**handle any mask*/
			}else if(newMask!=null){

				int[] maskArray=newMask.getIntArray(PdfDictionary.Mask);

				//see if object or colors
				if(maskArray!=null){

					int colorComponents=decodeColorData.getColorComponentCount();
					byte[] index=decodeColorData.getIndexedMap();

					if(index!=null){

						int itemCount=maskArray.length,indexValue;
						int[] newIndex=new int[colorComponents*itemCount];
						for(int jj=0;jj<itemCount;jj++){							
							indexValue=maskArray[jj];
							for(int i=0;i<colorComponents;i++)
								newIndex[i+(jj*colorComponents)]=index[(indexValue*colorComponents)+i] & 255;							
						}

						maskArray=newIndex;
					}

					int count=maskArray.length;

					//work out number of values involved
					int numberColors=count/colorComponents;

					//put values in  the table
					int i=0,value;
					int[][] matches=new int[numberColors][colorComponents];
					for(int currentCol=0;currentCol<numberColors;currentCol++){
						for(int comp=0;comp<colorComponents;comp++){

							value=maskArray[i];
							i++;

							if(colorComponents==1){
								matches[currentCol][0]=value;
								matches[currentCol][1]=value;
								matches[currentCol][2]=value;
							}else if(colorComponents==3){
								matches[currentCol][comp]=value;
							}else{
							}
						}
					}

					image = convertPixelsToTransparent(image, colorComponents, numberColors, matches);

				}else{

					byte[] objectData=currentPdfFile.readStream(newMask,true,true,keepRaw, false,false, null);

					int maskW=newMask.getInt(PdfDictionary.Width);
					int maskH=newMask.getInt(PdfDictionary.Height);

					/**fast op on data to speed up image manipulation*/
					int both=PDFImageProcessing.IMAGE_INVERTED+ PDFImageProcessing.IMAGE_ROTATED;

					if((optionsApplied & both)==both){
						byte[] processedData=ImageOps.rotateImage(objectData, maskW, maskH, 1, 1, null);
						if(processedData!=null){
							int temp = maskH;
							maskH=maskW;
							maskW=temp;
							processedData=ImageOps.rotateImage(processedData, maskW, maskH, d, 1, null);
							if(processedData!=null){
								temp = maskH;
								maskH=maskW;
								maskW=temp;
							}
						}

						objectData=processedData;

					}else if((optionsApplied & PDFImageProcessing.IMAGE_INVERTED)==PDFImageProcessing.IMAGE_INVERTED){//invert at byte level with copy
						byte[] processedData=ImageOps.invertImage(objectData, maskW, maskH, 1, 1, null);
						objectData=processedData;

					}

					if((optionsApplied & PDFImageProcessing.IMAGE_ROTATED)==PDFImageProcessing.IMAGE_ROTATED){ //rotate at byte level with copy New Code still some issues
						byte[] processedData=ImageOps.rotateImage(objectData, maskW, maskH, d, 1, null);
						objectData=processedData;
					}

					if(objectData!=null){

						boolean needsConversion=decodeColorData!=null && decodeColorData.getID()== ColorSpaces.DeviceGray;
                        boolean isRGB=decodeColorData!=null && decodeColorData.getID()== ColorSpaces.DeviceRGB;

                        if(!needsConversion && !isRGB && isDCT){

                            PdfArrayIterator maskFilters = newMask.getMixedArray(PdfDictionary.Filter);

                            //get type as need different handling                    
                            boolean maskNeedsInversion =false;

                            int firstMaskValue=PdfDictionary.Unknown;
                            if(maskFilters!=null && maskFilters.hasMoreTokens()){
                                while(maskFilters.hasMoreTokens()){
                                    firstMaskValue=maskFilters.getNextValueAsConstant(true);
                                    maskNeedsInversion =(firstMaskValue==PdfFilteredReader.CCITTFaxDecode || firstMaskValue==PdfFilteredReader.JBIG2Decode);
                                }
                            }
                            if(!maskNeedsInversion)
                            needsConversion=true;
                        }

                        image=overlayImage(image,objectData,newMask,needsConversion);
					}
				}
			}
			
			//simulate overPrint //currentGraphicsState.getNonStrokeOP() &&
			if(colorspaceID==ColorSpaces.DeviceCMYK && gs.getOPM()==1.0f){
            //if((colorspaceID==ColorSpaces.DeviceCMYK || colorspaceID==ColorSpaces.ICC) && gs.getOPM()==1.0f){

				//try to keep as binary if possible
				boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
				boolean isBlank=false;

				//see if allblack
				if(hasObjectBehind){

					isBlank=true; //assume true and disprove
					for(int ii=0;ii<data.length;ii++){
						if(data[ii]!=0){
							ii=data.length;
							isBlank=false;
						}
					}
				}

                //if so reject
				if(isBlank){
                    image.flush();
					image=null;
                }

				else if((isDCT || isJPX) && gs.getNonStrokeOP()){
					image=simulateOP(image,true);
                }else if(gs.getNonStrokeOP()){
					image=simulateOP(image,false);
				}

				if(image==null)
					return null;   
			}

			data = null;

			if (image!=null && image.getSampleModel().getNumBands() == 1)
				type = "tif";

			if( isPageContent &&(clippedImagesExtracted || finalImagesExtracted || rawImagesExtracted)){

				//save the raw image or blank if demo or encryption enabled
				if (currentPdfFile.isExtractionAllowed()){

					if(PdfDecoder.inDemo){

						int imageType=image.getType();
						if(imageType==0)
							imageType=BufferedImage.TYPE_INT_RGB;
						BufferedImage newImage=new BufferedImage(image.getWidth(),image.getHeight(),imageType);
						Graphics2D g2= newImage.createGraphics();
						g2.drawImage(image,null,null);

						int x=image.getWidth();
						int y=image.getHeight();

						//add demo cross
						g2.setColor(Color.red);
						g2.drawLine(0, 0, x,y);
						g2.drawLine(0, y, x, 0);

						objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(newImage),false,createScaledVersion,type);
					}else{
						if(!PdfStreamDecoder.runningStoryPad)
							//    objectStoreStreamRef.saveStoredImage(name,image,false,createScaledVersion,type);
							//else
							objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(image),false,createScaledVersion,type);
					}
				}else{

					/**create copy and scale if required*/
					if(PdfDecoder.dpi!=72){

						int imageType=image.getType();
						if(imageType==0)
							imageType=BufferedImage.TYPE_INT_RGB;
						BufferedImage newImage=new BufferedImage(image.getWidth(),image.getHeight(),imageType);
						newImage.createGraphics().drawImage(image,null,null);
						float s=((float)PdfDecoder.dpi)/72;
						AffineTransform scale = new AffineTransform();
						scale.scale(s, s);
						AffineTransformOp scalingOp =new AffineTransformOp(scale, ColorSpaces.hints);
						newImage =scalingOp.filter(newImage, null);
						objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(newImage),false,createScaledVersion,type);

					}else{
						objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(image),false,createScaledVersion,type);
					}
				}
			}
		}

		if(image == null && !removed){
			imagesProcessedFully = false;
		}

		//apply any tranfer function
		PdfObject TR=gs.getTR();
		if(TR!=null) //array of values
			image=applyTR(image, TR);

		//try to simulate some of blend by removing white if not bottom image
		if(DecodeParms!=null  && DecodeParms.getInt(PdfDictionary.Blend)!=PdfDictionary.Unknown &&
				current.hasObjectsBehind(gs.CTM))
			image= makeBlackandWhiteTransparent(image);

        //sharpen 1 bit
        if(pX>0 && pY>0 && rawd==1 && sharpenDownsampledImages && (decodeColorData.getID()==ColorSpaces.DeviceGray || decodeColorData.getID()==ColorSpaces.DeviceRGB)){

            Kernel kernel = new Kernel(3, 3,
                new float[] {
                    -1, -1, -1,
                    -1, 9, -1,
                    -1, -1, -1});
            BufferedImageOp op = new ConvolveOp(kernel);
            image = op.filter(image, null);

        }

		return image;
	}

	/**
	 * add MASK to image
	 */
	private static BufferedImage overlayImage(BufferedImage image, byte[] maskData, PdfObject newMask, boolean needsInversion) {

		image=ColorSpaceConvertor.convertToRGB(image);

		Raster ras=image.getRaster();

		int maskW=newMask.getInt(PdfDictionary.Width);
		int maskH=newMask.getInt(PdfDictionary.Height);

		int width=image.getWidth();
		int height=image.getHeight();

		boolean isScaled=(width!=maskW || height!=maskH);
		float scaling=0;

		if(isScaled){
			float scalingW=(float)width/(float)maskW;
			float scalingH=(float)height/(float)maskH;

			if(scalingW>scalingH)
				scaling=scalingW;
			else
				scaling=scalingH;
		}

		BufferedImage newImage=new BufferedImage(maskW, maskH, BufferedImage.TYPE_INT_ARGB);

		WritableRaster output=newImage.getRaster();

		//workout y offset (remember needs to be factor of 8)
		int lineBytes=maskW;
		if((lineBytes & 7)!=0)
			lineBytes=lineBytes+8;

		lineBytes=lineBytes>>3;

		int bytes=0,x,y;

		final int[] bit={128,64,32,16,8,4,2,1};

		for(int rawy=0;rawy<maskH;rawy++){

			if(isScaled){
				y=(int)(scaling*rawy);

				if(y>height)
					y=height;
			}else
				y=rawy;

			boolean isTransparent;
			int xOffset;
			byte b;
			for(int rawx=0;rawx<maskW;rawx++){

				if(isScaled){
					x=(int)(scaling*rawx);

					if(x>width)
						x=height;
				}else
					x=rawx;

				xOffset=(rawx>>3);

				b=maskData[bytes+xOffset];

				//invert if needed
				if(needsInversion)
					isTransparent=(b & bit[rawx & 7])==0;
				else
					isTransparent=(b & bit[rawx & 7])!=0;

				//System.out.println("co-ords="+rawx+" "+rawy+" xOffset="+xOffset+" byte="+b+" bit="+bit[rawx & 7]+" isTransparent="+isTransparent);


				//if it matched replace and move on
				if(!isTransparent && x<width && y<height){
					int[] values=new int[3];
					values=ras.getPixel(x,y,values); //get pixel from data
					output.setPixel(rawx,rawy,new int[]{values[0],values[1],values[2],255});
					//output.setPixel(rawx,rawy,new int[]{255,0,0,255});

				}
			}

			bytes=bytes+lineBytes;

		}

		return newImage;
	}

	/**
	 * add MASK to image
	 */
	private static BufferedImage convertPixelsToTransparent(BufferedImage image, int colorComponents, int numberColors, int[][] matches) {
		Raster ras=image.getRaster();
		image=ColorSpaceConvertor.convertToARGB(image);

		int[] transparentPixel={255,0,0,0};
		for(int y=0;y<image.getHeight();y++){
			for(int x=0;x<image.getWidth();x++){

				int[] values=new int[4];
				//get raw color data
				ras.getPixel(x,y,values);

				//see if we have a match
				boolean noMatch=true;
				for(int currentCol=0;currentCol<numberColors;currentCol++){

					//assume it matches
					noMatch=false;

					//test assumption
					for(int comp=0;comp<colorComponents;comp++){
						if(matches[currentCol][comp]!=values[comp]){
							comp=colorComponents;
							noMatch=true;
						}
					}

					//if it matched replace and move on
					if(!noMatch){
						image.getRaster().setPixel(x,y,transparentPixel);
						currentCol=numberColors;
					}
				}
			}
		}
		return image;
	}

	/**
	 * CMYK overprint mode
	 */
	private BufferedImage simulateOP(BufferedImage image, boolean isJPEG) {

        Raster ras=image.getRaster();
		image=ColorSpaceConvertor.convertToARGB(image);
        int w=image.getWidth();
        int h=image.getHeight();

		boolean hasNoTransparent=false, pixelsSet=false;

        //reset
        minX=w;
        minY=h;
        maxX=-1;
        maxY=-1;

        int[] transparentPixel={255,0,0,0};
        
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){

				int[] values=new int[4];
				//get raw color data
				ras.getPixel(x,y,values);

				//see if black
				boolean transparent=values[1]<3 && values[2]<3 && values[3]<3;

				//if it matched replace and move on
				if(transparent){
					image.getRaster().setPixel(x,y,transparentPixel);
                }else{
					hasNoTransparent=true;

                    //see if we can reduce in size by working out size needed
//                    if(minX>x)
//                    minX=x;
//                    if(maxX<x)
//                    maxX=x;
//                    if(minY>y)
//                    minY=y;
//                    if(maxY<y)
//                    maxY=y;
//                    pixelsSet=true;
                }
			}
		}

		if(hasNoTransparent){
            //trim to size
//            if(pixelsSet && (minX>0 || minY>0)){
//                try{
//                    //System.out.println("before="+image);
//                image=image.getSubimage(minX,minY,maxX-minX,maxY-minY);
//                    //System.out.println("after="+image);
//
//                }catch(Exception ee){
//                    ee.printStackTrace();
//                }
//            }
            return image;
        }else
            return null;

	}

	/**
	 * see if all one color
	 */
	private BufferedImage rejectSolidImage(BufferedImage image) {
		
		if(image==null)
			return image;
		
        Raster ras=image.getRaster();
		int w=image.getWidth();
        int h=image.getHeight();

        int components=image.getColorModel().getNumComponents();

		int[] col=null;

		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){

				int[] values=new int[components];
				//get raw color data
				ras.getPixel(x,y,values);

                if(x==0 && y==0)
                    col=values;

                for(int jj=0;jj<components;jj++)
                    if(col[jj]!=values[jj]) //exit at once if no match
                        return image;

			}
		}

        //all same so eliminate
		return null;

	}

	/**
	 * make transparent
	 */
	private static BufferedImage makeBlackandWhiteTransparent(BufferedImage image) {

		Raster ras=image.getRaster();

        int w=ras.getWidth();
        int h=ras.getHeight();

		//image=ColorSpaceConvertor.convertToARGB(image);
		BufferedImage newImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

		boolean validPixelsFound=false;

		int[] transparentPixel={255,0,0,0};
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){

				int[] values=new int[3];
				//get raw color data
				ras.getPixel(x,y,values);

				//see if white
				boolean transparent=(values[0]>245 && values[1]>245 && values[2]>245);
				boolean isBlack=(values[0]<10 && values[1]<10 && values[2]<10);


				//if it matched replace and move on
				if(transparent || isBlack) {
					newImage.getRaster().setPixel(x,y,transparentPixel);
				}else{
					validPixelsFound=true;

					int[] newPixel=new int[4];

					newPixel[3]=255;
					newPixel[0]=values[0];
					newPixel[1]=values[1];
					newPixel[2]=values[2];

					newImage.getRaster().setPixel(x,y,newPixel);
				}
			}
		}

		if(validPixelsFound)
			return newImage;
		else
			return null;

	}
	
	/**
	 * make transparent
	 */
	private static boolean isAllWhite(BufferedImage image) {

		Raster ras=image.getRaster();

        int whiteCount=0;
        int w=image.getWidth();
        int h=image.getHeight();

        int[] values=null;
        //int[] transparentPixel={255,0,0,0};
		for(int y=0;y<h;y++){
			for(int x=0;x<w;x++){

				values=new int[4];
				//get raw color data
				ras.getPixel(x,y,values);

               // System.out.println(x+","+y+"="+values[0]+" "+values[1]+" "+values[2]+" "+values[3]);
                //see if white
				boolean isWhite=(values[1]>245 && values[2]>245 && values[3]>245);
				//boolean isWhite=(values[1]<15 && values[2]<15 && values[3]<15);

                if(!isWhite){
                    whiteCount++;
                    //System.out.println(x+","+y+" ="+values[0]+" "+values[1]+" "+values[2]+" "+values[3]);
                    

                }
	}
		}

        //System.out.println(whiteCount+" "+(w*h));

        //System.out.println(image);
        //org.jpedal.gui.ShowGUIMessage.showGUIMessage("img",image,"img");

        if(whiteCount>=w*h)
			return true;
		else
			return false;

	}

	/**
	 * apply TR
	 */
	private BufferedImage applyTR(BufferedImage image,PdfObject TR) {

		/**
		 * get TR function first
		 **/
		PDFFunction[] functions;//see if cached or read in when first needed

		Object TRPDFFunctions=null;//TRPDFfunctionsCache.get(oldTR);

		if(TRPDFFunctions!=null){ //use cached if stored
			functions =(PDFFunction[]) TRPDFFunctions;
		}else{

			functions =new PDFFunction[4];

			int total=0;

			byte[][] kidList = TR.getKeyArray(PdfDictionary.TR);

			if(kidList!=null)
				total=kidList.length;

			//get functions
			for(int count=0;count<total;count++){

				String ref=new String(kidList[count]);
				PdfObject Function=new FunctionObject(ref);

                //handle /Identity as null or read 
                byte[] possIdent=kidList[count];
                if(possIdent!=null && possIdent.length>4 && possIdent[0]==47 &&  possIdent[1]==73 && possIdent[2]==100 &&  possIdent[3]==101)//(/Identity
                    Function=null;
                else
				    currentPdfFile.readObject(Function);

				/** setup the translation function */
				if(Function!=null)
					functions[count] = FunctionFactory.getFunction(Function, currentPdfFile);

			}

			//cache incase used again
			TRPDFfunctionsCache.put(TR, functions);

		}

		/**
		 * apply colour transform
		 */
		Raster ras=image.getRaster();
		//image=ColorSpaceConvertor.convertToARGB(image);

		for(int y=0;y<image.getHeight();y++){
			for(int x=0;x<image.getWidth();x++){

				int[] values=new int[4];

				//get raw color data
				ras.getPixel(x,y,values);

				for(int a=0;a<3;a++){
					float[] raw={values[a]/255f};
					
                    if(functions[a]!=null){
					    float[] processed=functions[a].compute(raw);

					    values[a]= (int) (255*processed[0]);
                    }
				}

				image.getRaster().setPixel(x,y,values);
			}
		}

		return image;

	}


	/**
	 * apply soft mask
	 **/
	private static BufferedImage applySmask(BufferedImage image, BufferedImage smask,
			PdfObject newSMask) {

		int[] gray={0};
		int[] val={0,0,0,0};
		int[] transparentPixel={0,0,0,0};

		//get type as need different handling
		PdfArrayIterator maskFilters = newSMask.getMixedArray(PdfDictionary.Filter);

		boolean maskIsDCT=false;//,maskIsJPX=false;

		int firstValue=PdfDictionary.Unknown;
		if(maskFilters!=null && maskFilters.hasMoreTokens()){
			while(maskFilters.hasMoreTokens()){
				firstValue=maskFilters.getNextValueAsConstant(true);
				maskIsDCT=firstValue==PdfFilteredReader.DCTDecode;
				//isJPX=firstValue==PdfFilteredReader.JPXDecode;
			}
		}

		PdfObject ColorSpace=newSMask.getDictionary(PdfDictionary.ColorSpace);
		boolean needsConversion=maskIsDCT && ColorSpace!=null &&
		ColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceGray;

		//fix for Smask encoded with DCTDecode but not JPX
		if(needsConversion){
			smask=ColorSpaceConvertor.convertColorspace(smask,BufferedImage.TYPE_BYTE_GRAY);
			val=gray;
		}

		Raster mask=smask.getRaster();
        WritableRaster imgRas=null;

        boolean isConverted=false;

		/**
		 * allow for scaled mask
		 */
		int imageW=image.getWidth();
		int imageH=image.getHeight();

		int smaskW=smask.getWidth();
		int smaskH=smask.getHeight();
		float ratio=0;

		if(imageW!=smaskW || imageH!=smaskH){
			float ratioW=(float)imageW/(float)smaskW;
			float ratioH=(float)imageH/(float)smaskH;

			if(ratioW>ratioH)
				ratio=ratioW;
			else
				ratio=ratioH;

		}

		int colorComponents=smask.getColorModel().getNumComponents();

		for(int y=0;y<imageH;y++){
			for(int x=0;x<imageW;x++){

				int[] values=new int[colorComponents];

				//get raw color data
				try{
					if(ratio==0)
						mask.getPixel(x,y,values);
					else
						mask.getPixel((int)(x/ratio),(int)(y/ratio),values);

				}catch(Exception e){
					e.printStackTrace();
				}

				//see if we have a match
				boolean noMatch=true;

				//assume it matches
				noMatch=false;

				//test assumption
				if(colorComponents==1){  //hack to filter out DCTDecode stream
					if(values[0]>127)
						noMatch=true;
				}else{

					for(int comp=0;comp<colorComponents;comp++){
						if(values[comp]!=val[comp]){
							comp=colorComponents;
							noMatch=true;
						}
					}
				}

				//if it matched replace and move on
				if(!noMatch){

                    if(!isConverted){ // do it first time needed
                        image=ColorSpaceConvertor.convertToARGB(image);
                        imgRas=image.getRaster();
                        isConverted=true;
                    }

                    //handle 8bit gray, not DCT
                    if(colorComponents==1){

                        int[] pix=new int[4];

						imgRas.getPixel(x,y,pix);

                        //remove what appears invisible in Acrobat
                        if(values[0]==pix[0]){//pix[0]>32 && pix[1]>32 && pix[2]>32 && values[0]<32)
                            //System.out.println(x+" "+y+" a="+values[0]+" r="+pix[0]+" g="+pix[1]+" b="+pix[2]);
					imgRas.setPixel(x,y,transparentPixel);
                           // imgRas.setPixel(x,y,new int[]{(int)(255),(int)(255),(int)(0),128});//transparentPixel);
                        }else
                            imgRas.setPixel(x,y,new int[]{(int)(pix[0]),(int)(pix[1]),(int)(pix[2]),values[0]});

                    }else
                        imgRas.setPixel(x,y,transparentPixel);
                        
                }
			}
		}

		return image;
	}

	/**
	 * @param maskCol
	 */
	private void getMaskColor(byte[] maskCol) {
		int foreground =nonstrokeColorSpace.getColor().getRGB();
		maskCol[0]=(byte) ((foreground>>16) & 0xFF);
		maskCol[1]=(byte) ((foreground>>8) & 0xFF);
		maskCol[2]=(byte) ((foreground) & 0xFF);
	}

	/**
	 * apply DecodeArray
	 */
	private static void applyDecodeArray(byte[] data, int d, float[] decodeArray,
			int type) {

		int count = decodeArray.length;

		int maxValue=0;
		for(int i=0;i<count;i++) {
			if(maxValue<decodeArray[i])
				maxValue=(int) decodeArray[i];
		}

		/**
		 * see if will not change output
		 * and ignore if unnecessary
		 */
		boolean isIdentify=true; //assume true and disprove
		int compCount=decodeArray.length;

		for(int comp=0;comp<compCount;comp=comp+2){
			if((decodeArray[comp]!=0.0f)||((decodeArray[comp+1]!=1.0f)&&(decodeArray[comp+1]!=255.0f))){
				isIdentify=false;
				comp=compCount;
			}
		}

		if(isIdentify)
			return ;

		if(d==1){ //gray or bw straight switch

			int byteCount=data.length;
			for(int ii=0;ii<byteCount;ii++){
				data[ii]=(byte) ~data[ii];

			}
			/**
			 * handle rgb
			 */
		}else if((d==8 && maxValue>1)&&(type==ColorSpaces.DeviceRGB || type==ColorSpaces.CalRGB || type==ColorSpaces.DeviceCMYK)){

			int j=0;

			for(int ii=0;ii<data.length;ii++){
				int currentByte=(data[ii] & 0xff);
				if(currentByte<decodeArray[j])
					currentByte=(int) decodeArray[j];
				else if(currentByte>decodeArray[j+1])
					currentByte=(int)decodeArray[j+1];

				j=j+2;
				if(j==decodeArray.length)
					j=0;
				data[ii]=(byte)currentByte;
			}
		}else{
			/**
			 * apply array
			 *
			 * Assumes black and white or gray colorspace
			 * */
			maxValue = (d<< 1);
			int divisor = maxValue - 1;

			for(int ii=0;ii<data.length;ii++){
				byte currentByte=data[ii];

				int dd=0;
				int newByte=0;
				int min=0,max=1;
				for(int bits=7;bits>-1;bits--){
					int current=(currentByte >> bits) & 1;

					current =(int)(decodeArray[min]+ (current* ((decodeArray[max] - decodeArray[min])/ (divisor))));

					/**check in range and set*/
					if (current > maxValue)
						current = maxValue;
					if (current < 0)
						current = 0;

					current=((current & 1)<<bits);

					newByte=newByte+current;

					//rotate around array
					dd=dd+2;

					if(dd==count){
						dd=0;
						min=0;
						max=1;
					}else{
						min=min+2;
						max=max+2;
					}
				}

				data[ii]=(byte)newByte;

			}
		}

	}



	public void init(boolean isPageContent,boolean renderPage,
			int renderMode, int extractionMode,PdfPageData currentPageData,
			int pageNumber,DynamicVectorRenderer current,
			PdfObjectReader currentPdfFile) throws PdfException{

		if(current!=null)
			this.current=current;

		this.pageNum=pageNumber;
		this.pageData=currentPageData;
		this.isPageContent=isPageContent;
		this.currentPdfFile=currentPdfFile;

		//<start-jfr>
		if(customImageHandler!=null && current!=null)
			current.setCustomImageHandler(customImageHandler);
		//<end-jfr>

		//setup height
		this.pageH = pageData.getMediaBoxHeight(pageNumber);

		//set width
		pageLines.setMaxWidth(pageData.getCropBoxWidth(pageNumber)-pageData.getCropBoxX(pageNumber),
				pageData.getCropBoxHeight(pageNumber)-pageData.getCropBoxY(pageNumber));

		textExtracted=(extractionMode & PdfDecoder.TEXT)==PdfDecoder.TEXT;

		this.renderPage=renderPage;

		renderText=renderPage &&(renderMode & PdfDecoder.RENDERTEXT) == PdfDecoder.RENDERTEXT;
		renderImages=renderPage &&(renderMode & PdfDecoder.RENDERIMAGES )== PdfDecoder.RENDERIMAGES;

		extractRawCMYK=clippedImagesExtracted=(extractionMode &PdfDecoder.CMYKIMAGES)==PdfDecoder.CMYKIMAGES;
		rawImagesExtracted=(extractionMode & PdfDecoder.RAWIMAGES) == PdfDecoder.RAWIMAGES;
		clippedImagesExtracted=(extractionMode &PdfDecoder.CLIPPEDIMAGES)==PdfDecoder.CLIPPEDIMAGES;
		finalImagesExtracted=(extractionMode & PdfDecoder.FINALIMAGES) == PdfDecoder.FINALIMAGES;
		xFormMetadata=(extractionMode & PdfDecoder.XFORMMETADATA) == PdfDecoder.XFORMMETADATA;

		textColorExtracted=(extractionMode & PdfDecoder.TEXTCOLOR) == PdfDecoder.TEXTCOLOR;

		colorExtracted=(extractionMode & PdfDecoder.COLOR) == PdfDecoder.COLOR;

		/**init text extraction*/
		if((legacyTextMode)&&(textExtracted)){
			if(PdfDecoder.currentHeightLookupData==null)
				PdfDecoder.currentHeightLookupData = new org.jpedal.fonts.PdfHeightTable();
		}
		//flag if colour info being extracted
		if(textColorExtracted)
			pdfData.enableTextColorDataExtraction();

		if ((finalImagesExtracted) | (renderImages))
			createScaledVersion = true;
		else
			createScaledVersion = false;

		currentFontData=new PdfFont(currentPdfFile);

		//delete
		strokeColorSpace = new DeviceRGBColorSpace();
		nonstrokeColorSpace = new DeviceRGBColorSpace();

	}

	//////////////////////////////////////////////////////
	/**
	 * turn raw data into a BufferedImage
	 */
	final private BufferedImage makeImage(GenericColorSpace decodeColorData,int w,int h,int d,
			byte[] data,int comp) {

		LogWriter.writeMethod("{makeImage}",0);

		//ensure correct size
		if(decodeColorData.getID()== ColorSpaces.DeviceGray && d==8){
			int requiredSize=w*h;
			int oldSize=data.length;
			if(oldSize<requiredSize){
				byte[] oldData=data;
				data=new byte[requiredSize];
				System.arraycopy(oldData,0,data,0,oldSize);
			}
		}

		/**
		 * put data into separate array. If we keep in PdfData then on pages where same image reused
		 * such as adobe/Capabilities and precisons, its flipped each time as its an object :-(
		 */
		//int byteCount=rawData.length;
		//byte[] data=new byte[byteCount];
		//System.arraycopy(rawData, 0, data, 0, byteCount);


		ColorSpace cs=decodeColorData.getColorSpace();
		int ID=decodeColorData.getID();

		BufferedImage image = null;
		byte[] index =decodeColorData.getIndexedMap();

		optionsApplied=PDFImageProcessing.NOTHING;


		/**fast op on data to speed up image manipulation*/
		//optimse rotations here as MUCH faster and flag we have done this
		//something odd happens if CTM[2][1] is negative so factor ignore this case
		boolean isInverted=!doNotRotate && useHiResImageForDisplay && DynamicVectorRenderer.isInverted(gs.CTM);
		boolean isRotated=!doNotRotate && useHiResImageForDisplay &&
                DynamicVectorRenderer.isRotated(gs.CTM) && !DynamicVectorRenderer.isRotationreversed(gs.CTM);

        if(renderDirectly && ! this.isType3Font){
			isInverted=false;
			isRotated=false;
		}

		//I optimised the code slightly - you were setting booleans are they had been
		//used - I removed so it keeps code shorter

		if(isInverted){//invert at byte level with copy
			byte[] processedData=ImageOps.invertImage(data, w, h, d, comp, index);

			if(processedData!=null){

				data=processedData;
				optionsApplied=optionsApplied+PDFImageProcessing.IMAGE_INVERTED;

			}
		}



		if(isRotated){ //rotate at byte level with copy New Code still some issues
			byte[] processedData=ImageOps.rotateImage(data, w, h, d, comp, index);

			if(processedData!=null){
				data=processedData;

				optionsApplied=optionsApplied+PDFImageProcessing.IMAGE_ROTATED;

				//reset
				int temp = h;
				h=w;
				w=temp;
			}
		}

        //data=ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, 255);

        //System.out.println("index="+index);

		if (index != null) { //indexed images

            LogWriter.writeLog("Indexed ");


			/**convert index to rgb if CMYK or ICC*/
			if (comp == 4)
				comp=3;


            if(!decodeColorData.isIndexConverted()){
                index=decodeColorData.convertIndexToRGB(index);
            }

			//workout size and check in range
			int size =decodeColorData.getIndexSize()+1;

            //pick out draft setting of totally empty iamge and ignore
            if(d==8 && decodeColorData.getIndexSize()==0 && decodeColorData.getID()==ColorSpaces.DeviceRGB){
            	
            	boolean hasPixels=false;
            	
            	int indexCount=index.length;            	
            	for(int ii=0;ii<indexCount;ii++){
            		if(index[ii]!=0){
            			hasPixels=true;
            			ii=indexCount;
            		}
            	}
            	
            	if(!hasPixels){
            		int pixelCount=data.length;
                	
	            	for(int ii=0;ii<pixelCount;ii++){
	            		if(data[ii]!=0){
	            			hasPixels=true;
	            			ii=pixelCount;
	            		}
	            	}
            	}
            	if(!hasPixels){
            		return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
            	}
            }
			//allow for half bytes (ie bootitng.pdf)
			if(d==4 && size>16)
				size=16;

//			WritableRaster raster =Raster.createPackedRaster(db, w, h, d, null);

//			ColorModel cm=new IndexColorModel(d, size, index, 0, false);
//			image = new BufferedImage(cm, raster, false, null);

			//if(debugColor)
			//System.out.println("xx d="+d+" w="+w+" data="+data.length+" index="+index.length+" size="+size);
			
			try{
				image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, size,false,false);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			//if(debugColor)
			//throw new RuntimeException("xx");
		
		} else if (d == 1) { //bitmaps next

			/** create an image from the raw data*/
			DataBuffer db = new DataBufferByte(data, data.length);

			WritableRaster raster =Raster.createPackedRaster(db, w, h, d, null);
			image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
			image.setData(raster);

		}else if(ID==ColorSpaces.Separation || ID==ColorSpaces.DeviceN){
			LogWriter.writeLog("Converting Separation/DeviceN colorspace to sRGB ");
			image=decodeColorData.dataToRGB(data,w,h);

		}else if(ID==6){
			LogWriter.writeLog("Converting lab colorspace to sRGB ");
			image=decodeColorData.dataToRGB(data,w,h);

			//direct images
		} else if (comp == 4) { //handle CMYK or ICC

            LogWriter.writeLog("Converting ICC/CMYK colorspace to sRGB ");

			//ICC (note CMYK uses ICC so check which type and check enough data)
			if((ID==3)) //&((w*h*4)==data.length)) /**CMYK*/
				image =ColorSpaceConvertor.algorithmicConvertCMYKImageToRGB(data,w,h);
			else
				image =ColorSpaceConvertor.convertFromICCCMYK(w,h,data, cs);

			//ShowGUIMessage.showGUIMessage("y",image,"y");
		} else if (comp == 3) {

            LogWriter.writeLog("Converting 3 comp colorspace to sRGB ");

			//work out from size what sort of image data we have
			if (w * h == data.length) {
				if (d == 8 && index!=null){
					image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, index.length,false,false);

					//image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_INDEXED);

					//WritableRaster raster =Raster.createPackedRaster(db,w,h,d,null);
					//image.setData(raster);
				}else{

					/** create an image from the raw data*/
					DataBuffer db = new DataBufferByte(data, data.length);

					int[] bands = {0};

					image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
					Raster raster =Raster.createInterleavedRaster(db,w,h,w,1,bands,null);
					image.setData(raster);

				}
			} else{

                LogWriter.writeLog("Converting data to sRGB ");

				/** create an image from the raw data*/
				DataBuffer db = new DataBufferByte(data, data.length);

				int[] bands = {0,1,2};
				image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
				Raster raster =Raster.createInterleavedRaster(db,w,h,w * 3,3,bands,null);
				image.setData(raster);

			}
		} else if (comp == 1 &&(d == 8|| d==4)) {

            LogWriter.writeLog("comp=1 and d= "+d);

			//expand out 4 bit raster as does not appear to be easy way
			if(d==4){
				int origSize=data.length;
				int newSize=w*h;

				byte[] newData=new byte[newSize];
				byte rawByte;
				int ptr=0,currentLine=0;
				boolean oddValues=((w & 1)==1);
				for(int ii=0;ii<origSize;ii++){
					rawByte=data[ii];

                    currentLine=currentLine+2;
					newData[ptr]=(byte) (rawByte & 240);
                    if(newData[ptr]==-16)   //fix for white
                    newData[ptr]=(byte)255;
                    ptr++;

					if(oddValues && currentLine>w){ //ignore second value if odd as just packing
						currentLine=0;
					}else{
						newData[ptr]=(byte) ((rawByte & 15) <<4);
                        if(newData[ptr]==-16)  //fix for white
                        newData[ptr]=(byte)255;
                        ptr++;
					}

                    if(ptr==newSize)
						ii=origSize;
				}
				data=newData;

			}

			/** create an image from the raw data*/
			DataBuffer db = new DataBufferByte(data, data.length);

			image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
			int[] bands ={0};
			Raster raster =Raster.createInterleavedRaster(db,w,h,w,1,bands,null);
			image.setData(raster);

		} else
			LogWriter.writeLog("Image "+ cs.getType()+ " not currently supported with components "+ comp);

		//convert type 0 to rgb (as do work with other ops)
		//if (image.getType() == 0)
		//image = ColorSpaceConvertor.convertToRGB(image);

        return image;
	}

    final private void readArrayPairs(PdfObject Resources, boolean resetFontList, int type) throws PdfException {

		LogWriter.writeMethod("{readArrayPairs}", 0);

		final boolean debugPairs=false;

		if(debugPairs){
			System.out.println("-------------readArrayPairs-----------"+type);
			System.out.println("new="+Resources+" "+Resources.getObjectRefAsString());
		}
		String id = "",value;

		if(resetFontList && type==PdfDictionary.Font)
			fontsInFile="";

		/**
		 * new code
		 */
		if(Resources!=null){


			PdfObject resObj=Resources.getDictionary(type);

			if(debugPairs)
				System.out.println("new res object="+resObj);

			if(resObj!=null){

				/**
				 * read all the key pairs for Glyphs
				 */
				PdfKeyPairsIterator keyPairs=resObj.getKeyPairsIterator();

				PdfObject obj;

				if(debugPairs){
					System.out.println("New values");
					System.out.println("----------");
				}

				while(keyPairs.hasMorePairs()){

					id=keyPairs.getNextKeyAsString();                
					value=keyPairs.getNextValueAsString();
					obj=keyPairs.getNextValueAsDictionary();

					if(debugPairs)
						System.out.println(id+" "+" "+obj);

					switch(type){

					case PdfDictionary.ColorSpace:
						colorspaces.put(id,obj);
						break;

					case PdfDictionary.ExtGState:
						GraphicsStates.put(id,obj);
						break;

					case PdfDictionary.Font:

						unresolvedFonts.put(id,value);

						break;

					case PdfDictionary.Pattern:
						patterns.put(id,obj);

						break;

					case PdfDictionary.Shading:
						shadings.put(id,obj);

						break;

					case PdfDictionary.XObject:
						if(resetFontList)
							globalXObjects.put(id, obj);
						else
							localXObjects.put(id, obj);

						break;

					}

					keyPairs.nextPair();
				}
			}
		}
	}

	private PdfFont createFont(PdfObject pdfObject, String font_id) throws PdfException{

		LogWriter.writeMethod("{createFont}", 0);

        /**
		 * allow for no actual object - ie /PDFdata/baseline_screens/debug/res.pdf
		 **/
        //found examples with no type set so cannot rely on it
		//int rawType=pdfObject.getParameterConstant(PdfDictionary.Type);
		//if(rawType!=PdfDictionary.Font)
		//	return null;

		String baseFont="";

		String subFont=null;
		int fontType=PdfDictionary.Unknown,origfontType=PdfDictionary.Unknown;

		PdfObject descendantFont=pdfObject.getDictionary(PdfDictionary.DescendantFonts);

		boolean isEmbedded= isFontEmbedded(pdfObject);

		/**
		 * handle any font remapping but not on CID fonts or Type3 and gets too messy
		 **/
		if(FontMappings.fontSubstitutionTable!=null && !isEmbedded &&
				pdfObject.getParameterConstant(PdfDictionary.Subtype)!=StandardFonts.TYPE3){

			String rawFont;

			if(descendantFont==null)
				rawFont=pdfObject.getName(PdfDictionary.BaseFont);
			else
				rawFont=descendantFont.getName(PdfDictionary.BaseFont);

			if(rawFont==null)
				rawFont=pdfObject.getName(PdfDictionary.Name);

			if(rawFont==null)
				rawFont=font_id;

            if(rawFont.indexOf('#')!=-1)
                rawFont= StringUtils.convertHexChars(rawFont);


			baseFont=(rawFont).toLowerCase();

			if(baseFont.startsWith("/"))
				baseFont=baseFont.substring(1);

			//strip any postscript
			int pointer=baseFont.indexOf('+');
			if(pointer==6)
				baseFont=baseFont.substring(7);

			String testFont=baseFont.toLowerCase(), nextSubType;

			subFont=(String) FontMappings.fontSubstitutionLocation.get(testFont);

			String newSubtype=(String)FontMappings.fontSubstitutionTable.get(testFont);

			//do not replace on MAC as default does not have certain values we need
			if(PdfDecoder.isRunningOnMac && testFont.equals("zapfdingbats"))
				testFont="No match found";

			//check aliases
			if(newSubtype==null){
				//check for mapping
				HashMap fontsMapped=new HashMap();
				String nextFont;
				while(true){
					nextFont=(String) FontMappings.fontSubstitutionAliasTable.get(testFont);

					if(nextFont==null)
						break;

					testFont=nextFont;

					nextSubType=(String)FontMappings.fontSubstitutionTable.get(testFont);

					if(nextSubType!=null){
						newSubtype=nextSubType;
						subFont=(String) FontMappings.fontSubstitutionLocation.get(testFont);
					}

					if(fontsMapped.containsKey(testFont)){
						StringBuffer errorMessage=new StringBuffer("[PDF] Circular font mapping for fonts");
						Iterator i=fontsMapped.keySet().iterator();
						while(i.hasNext()){
							errorMessage.append(' ');
							errorMessage.append(i.next());
						}
						throw new PdfException(errorMessage.toString());
					}
					fontsMapped.put(nextFont,"x");
				}
			}

			if(newSubtype!=null && descendantFont==null){

				//convert String to correct int value
				if (newSubtype.equals("/Type1")|| newSubtype.equals("/Type1C")|| newSubtype.equals("/MMType1"))
					fontType=StandardFonts.TYPE1;
				else if (newSubtype.equals("/TrueType"))
					fontType=StandardFonts.TRUETYPE;
				else if (newSubtype.equals("/Type3"))
					fontType=StandardFonts.TYPE3;
				else
					throw new RuntimeException("Unknown font type "+newSubtype+" used for font substitution");

				origfontType=pdfObject.getParameterConstant(PdfDictionary.Subtype);

			}else if(PdfDecoder.enforceFontSubstitution){
				LogWriter.writeLog("baseFont="+baseFont+" fonts added= "+FontMappings.fontSubstitutionTable);
				throw new PdfFontException("No substitute Font found for font="+baseFont+"<");
			}
		}

		//get subtype if not set above
		if(fontType==PdfDictionary.Unknown){
			fontType=pdfObject.getParameterConstant(PdfDictionary.Subtype);

			/**handle CID fonts where /Subtype stored inside sub object*/
			if (fontType==StandardFonts.TYPE0) {

				//get CID type and use in preference to Type0 on CID fonts
				PdfObject desc=pdfObject.getDictionary(PdfDictionary.DescendantFonts);
				fontType=desc.getParameterConstant(PdfDictionary.Subtype);

				origfontType=fontType;

				//track non-embedded, non-substituted CID fonts
				if(!isEmbedded && subFont==null) {
					hasNonEmbeddedCIDFonts=true;

					//track list
					if(nonEmbeddedCIDFonts.length()>0)
						nonEmbeddedCIDFonts.append(',');
					nonEmbeddedCIDFonts.append(baseFont);
				}
			}
		}

		if(fontType==PdfDictionary.Unknown){
			LogWriter.writeLog("Font type not supported");

			currentFontData=new PdfFont(currentPdfFile);
		}


		/**
		 * check for OpenType fonts and reassign type
		 */
		if(fontType==StandardFonts.TYPE1){

			PdfObject FontDescriptor=pdfObject.getDictionary(PdfDictionary.FontDescriptor);
			if(FontDescriptor!=null){

				PdfObject FontFile3=FontDescriptor.getDictionary(PdfDictionary.FontFile3);
				if(FontFile3!=null){ //must be present for OTTF font

					//get data
					byte[] stream=currentPdfFile.readStream(FontFile3,true,true,false, false,false, null);

					//check first 4 bytes
					if(stream!=null && stream.length>3 && stream[0]==79 && stream[1]==84 && stream[2]==84 && stream[3]==79)
						fontType=StandardFonts.TRUETYPE; //put it through our TT handler which also does OT

				}
			}
		}

		try{
			currentFontData=FontFactory.createFont(fontType,currentPdfFile,subFont);

			/**set an alternative to Lucida*/
			if(PdfDecoder.defaultFont!=null)
				currentFontData.setDefaultDisplayFont(PdfDecoder.defaultFont);

			currentFontData.createFont(pdfObject, font_id,renderPage, objectStoreStreamRef, fontsLoaded);

		}catch(Exception e){
			LogWriter.writeLog("[PDF] Problem "+e+" reading Font  type "+ StandardFonts.getFontypeAsString(fontType)+" in "+fileName);
			
			addPageFailureMessage("Problem "+e+" reading Font type "+StandardFonts.getFontypeAsString(fontType)+" in "+fileName);
		}
		
		/**
		 * add line giving font info so we can display or user access
		 */
		String name=currentFontData.getFontName();
		
		//deal with odd chars
		if(name.indexOf('#')!=-1)
			name= StringUtils.convertHexChars(name);
	       
		String details;
		if(currentFontData.isFontSubstituted()){
			details=font_id+"  "+name+"  "+StandardFonts.getFontypeAsString(origfontType)+"  Substituted ("+subFont+ ' ' +StandardFonts.getFontypeAsString(fontType)+")";
		}else if(currentFontData.isFontEmbedded){
			hasEmbeddedFonts=true;
			if(currentFontData.is1C() && descendantFont==null)
				details=font_id+"  "+name+" Type1C  Embedded";
			else
				details=font_id+"  "+name+"  "+StandardFonts.getFontypeAsString(fontType)+"  Embedded";
		}else
			details=font_id+"  "+name+"  "+StandardFonts.getFontypeAsString(fontType);

		if(fontsInFile==null)
			fontsInFile=details;
		else
			fontsInFile=details+'\n'+fontsInFile;

		return currentFontData;
	}

	/**
	 * check for embedded font file to see if font embedded
	 */
	private static boolean isFontEmbedded(PdfObject pdfObject) {

		//ensure we are looking in DescendantFonts object if CID
		int fontType=pdfObject.getParameterConstant(PdfDictionary.Subtype);
		if (fontType==StandardFonts.TYPE0)
			pdfObject=pdfObject.getDictionary(PdfDictionary.DescendantFonts);


		PdfObject descFontObj=pdfObject.getDictionary(PdfDictionary.FontDescriptor);


		if(descFontObj==null)
			return false;
		else
			return descFontObj.hasStream();
	}

	public static final boolean debugRes=false;



	/**
	 * read page header and extract page metadata
	 * @throws PdfException
	 */
	public final void readResources(PdfObject Resources,boolean resetList) throws PdfException {

		LogWriter.writeMethod("{readResources}", 0);

		//decode	
		String[] names={"ColorSpace","ExtGState","Font", "Pattern","Shading","XObject"};
		int[] keys={PdfDictionary.ColorSpace, PdfDictionary.ExtGState, PdfDictionary.Font,
				PdfDictionary.Pattern, PdfDictionary.Shading,PdfDictionary.XObject};

		for(int ii=0;ii<names.length;ii++){

			if(keys[ii]==PdfDictionary.Font || keys[ii]==PdfDictionary.XObject)
				readArrayPairs(Resources, resetList,keys[ii]);
			else
				readArrayPairs(Resources, false,keys[ii]);		
		}
	}

	/**
	 *
	 *  objects off the page, stitch into a stream and
	 * decode and put into our data object. Could be altered
	 * if you just want to read the stream
	 * @param pdfObject
	 * @param pageStream
	 * @throws PdfException
	 */
	public final T3Size decodePageContent(PdfObject pdfObject, int minX, int minY, GraphicsState newGS, byte[] pageStream) throws PdfException{/* take out min's%%*/

		LogWriter.writeMethod("{decodePageContent}", 0);

		try{
//		if(DynamicVectorRenderer.textBasedHighlight)
//			PdfHighlights.setLineAreas(null);
		
		//check switched off
		imagesProcessedFully = true;

		//reset count
		imageCount=0;

		if((!this.renderDirectly)&&(statusBar!=null))
			statusBar.percentageDone=0;

		if(newGS!=null)
			gs = newGS;
		else
			gs = new GraphicsState(minX,minY);/* take out min's%%*/


        //save for later
		if (renderPage){

			/**
			 * check setup and throw exception if null
			 */
			if(current==null)
				throw new PdfException("DynamicVectorRenderer not setup PdfStreamDecoder setStore(...) should be called");

			if(renderDirectly)
				current.renderClip(gs.getClippingShape(),null,defaultClip,g2);
			else
				current.drawClip(gs) ;
		}


		//get the binary data from the file
		byte[] b_data = null;

		//reset text state
		currentTextState = new TextState();

		byte[][] pageContents= null;
		if(pdfObject!=null)
			pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);


        //@speed - once converted, lose readPageIntoStream(contents); method
		if(pdfObject!=null && pageContents==null)
			b_data=currentPdfFile.readStream(pdfObject,true,true,false, false,false, null);
		else if(pageStream!=null)
			b_data=pageStream;
		else
			b_data=readPageIntoStream(pdfObject);

		//if page data found, turn it into a set of commands
		//and decode the stream of commands
		if (b_data!=null && b_data.length > 0) {

			//reset graphics state for each page and flush queue
			//currentGraphicsState.resetCTM();
			decodeStreamIntoObjects(b_data);

		}

		//flush fonts
		resolvedFonts.clear();
		unresolvedFonts.clear();

		/**fontHandle
        //lose font handles asap
        currentFontData.unsetUnscaledFont();
        currentFontData=null;
        this.releaseResources();
        fonts=null;
		 */
		T3Size t3=new T3Size();
		t3.x = T3maxWidth;
		t3.y = T3maxHeight;
		return t3;

		
		}catch(Error err){
			addPageFailureMessage("Problem decoding page "+err);
		
			
		}
		
		return null;
	}

    /**
	 *
	 *  just scan for DO and CM to get image sizes so we can work out sampling used
	 */
	public final float decodePageContentForImageSampling(PdfObject pdfObject, int minX, int minY, GraphicsState newGS, byte[] pageStream) throws PdfException{/* take out min's%%*/

		LogWriter.writeMethod("{decodePageContent}", 0);

		try{

		//check switched off
		imagesProcessedFully = true;

		//reset count
		imageCount=0;

		if(newGS!=null)
			gs = newGS;
		else
			gs = new GraphicsState(minX,minY);/* take out min's%%*/

		//get the binary data from the file
		byte[] b_data = null;

		//reset text state
		currentTextState = new TextState();

		byte[][] pageContents= null;
		if(pdfObject!=null)
			pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);

        //@speed - once converted, lose readPageIntoStream(contents); method
		if(pdfObject!=null && pageContents==null)
			b_data=currentPdfFile.readStream(pdfObject,true,true,false, false,false, null);
		else if(pageStream!=null)
			b_data=pageStream;
		else
			b_data=readPageIntoStream(pdfObject);

		//if page data found, turn it into a set of commands
		//and decode the stream of commands
		if (b_data!=null && b_data.length > 0) {

            getSamplingOnly=true;
            //reset graphics state for each page and flush queue
			//currentGraphicsState.resetCTM();
			decodeStreamIntoObjects(b_data);


        }

		//flush fonts
		resolvedFonts.clear();
		unresolvedFonts.clear();

        getSamplingOnly=false;

		return samplingUsed;


		}catch(Error err){

            getSamplingOnly=false;

            addPageFailureMessage("Problem decoding page "+err);
		}

		return -1;
	}

    /**
	 * decode the actual 'Postscript' stream into text and images by extracting
	 * commands and decoding each.
	 */
	public final void decodeStreamIntoObjects(byte[] characterStream) {

		LogWriter.writeMethod("{decodeStreamIntoObjects}", 0);

		final boolean debug=false;

		int count=prefixes.length;
		int start=0,end=0;
		int sLen=characterStream.length;

		if(!renderDirectly && statusBar!=null){
			statusBar.percentageDone=0;
			statusBar.resetStatus("stream");
		}

		int streamSize=characterStream.length,charCount = streamSize;
		int dataPointer = 0,startCommand=0; //reset

		if(streamSize==0)
			return ;

		int current=0,nextChar=(int)characterStream[0],commandID=-1;

		/**
		 * loop to read stream and decode
		 */
		while (true) {

			if(!renderDirectly && statusBar!=null)
				statusBar.percentageDone=(100*dataPointer)/streamSize;

			current=nextChar;

			if(current==13 || current==10 || current==32 || current==9){

				dataPointer++;

				while(true){ //read next valid char

					if(dataPointer==charCount) //allow for end of stream
						break;

					current =(int)characterStream[dataPointer];

					if(current!=13 && current!=10 && current!=32)
						break;

					dataPointer++;

				}
			}

			if(dataPointer==charCount) //allow for end of stream
				break;

			/**
			 * read in value (note several options)
			 */
			boolean matchFound=false;
			int type=0;

			if(current==60 && characterStream[dataPointer+1]==60) //look for <<
				type=1;
			else if(current==91) //[
				type=2;
			else if(current>=97 && current<=122) //lower case alphabetical a-z
				type=3;
			else if(current>=65 && current<=90) //upper case alphabetical A-Z
				type=3;
			else if(current==39 || current==34) //not forgetting the non-alphabetical commands '\'-'\"'/*
				type=3;
			else if(current==32)
				type=4;
			else
				type=0;

			if(debug)
				System.out.println("Char="+current+" type="+type);

			if(type==3){ //option - its an aphabetical so may be command or operand values

				start=dataPointer;

				while(true){ //read next valid char

					dataPointer++;
					if((dataPointer)==sLen) //trap for end of stream
						break;

					current = characterStream[dataPointer];
					//return,space,( / or [
					if (current == 13 || current == 10 || current == 32 || current == 40 || current == 47 || current == 91 || current=='<')
						break;

				}

				end=dataPointer-1;


				//move back if ends with / or [
				int endC=characterStream[end];
				if(endC==47 || endC==91 || endC=='<')
					end--;

				//see if command
				commandID=-1;
				if(end-start<3){ //no command over 3 chars long
					//@turn key into ID.
					//convert token to int
					int key=0,x=0;
					for(int i2=end;i2>start-1;i2--){
						key=key+(characterStream[i2]<<x);
						x=x+8;
					}
					commandID=Cmd.getCommandID(key);
				}

				/**
				 * if command execute otherwise add to stack
				 */
				if (commandID==-1) {

					opStart[currentOp]=start;
					opEnd[currentOp]=end;



					currentOp++;
					if (currentOp == this.MAXOPS)
						currentOp = 0;
					operandCount++;
				}else{

                    //if(debugColor && tokenNumber>58)
					//break;
                    



					try {
						dataPointer = processToken(commandID,characterStream,startCommand, dataPointer);
						startCommand=dataPointer;
					} catch (Exception e) {

						LogWriter.writeLog("[PDF] "+ e);
						LogWriter.writeLog("Processing token >" + Cmd.getCommandAsString(commandID)
								+ "<>" + fileName+" <"+pageNum);

					} catch (OutOfMemoryError ee) {
						addPageFailureMessage("Memory error decoding token stream");
						LogWriter.writeLog("[MEMORY] Memory error - trying to recover");
					}

					currentOp=0;
					operandCount=0;
				}
			}else if(type!=4){

				start=dataPointer;

				//option  << values >>
				//option  [value] and [value (may have spaces and brackets)]
				if(type==1 || type==2){

					boolean inStream=false;
					matchFound=true;

					int last=32;  // ' '=32

					while(true){ //read rest of chars

						if(last==92 && current==92) //allow for \\  \\=92
							last=120;  //'x'=120

						else
							last = current;

						dataPointer++; //roll on counter

						if(dataPointer==sLen) //allow for end of stream
							break;

						//read next valid char, converting CR to space
						current = characterStream[dataPointer];
						if(current==13 || current==10)
							current=32;

						//exit at end
						boolean isBreak=false;


						if(current==62 && last==62 &&(type==1))  //'>'=62
							isBreak=true;

						if(type==2){
							//stream flags
							if((current==40)&&(last!=92)) 	//'('=40 '\\'=92
								inStream=true;
							else if((current==41)&&(last!=92))
								inStream=false;

							//exit at end
							if (inStream == false && current==93 && last != 92)	//']'=93
								isBreak=true;
						}

						if(isBreak)
							break;
					}

					end=dataPointer;
				}

				if(!matchFound){ //option 3 other braces

					int last=32;
					for(int startChars=0;startChars<count;startChars++){

						if(current==prefixes[startChars]){
							matchFound=true;

							start=dataPointer;

							int numOfPrefixs=0;//counts the brackets when inside a text stream
							while(true){ //read rest of chars

								if((last==92) &&(current==92)) //allow for \\ '\\'=92
									last=120; //'x'=120
								else
									last = current;
								dataPointer++; //roll on counter

								if(dataPointer==sLen)
									break;
								current = characterStream[dataPointer]; //read next valid char, converting CR to space
								if((current==13)|(current==10))
									current=32;

								if(current ==prefixes[startChars] && last!=92) // '\\'=92
									numOfPrefixs++;

								if ((current == suffixes[startChars])&& (last != 92)){ //exit at end  '\\'=92
									if(numOfPrefixs==0)
										break;
									else{
										numOfPrefixs--;

									}
								}
							}
							startChars=count; //exit loop after match
						}
					}
					end=dataPointer;
				}

				//option 2 -its a value followed by a deliminator (CR,space,/)
				if(!matchFound){

					if(debug)
						System.out.println("Not type 2");

					start=dataPointer;
					int firstChar=characterStream[start];

					while(true){ //read next valid char
						dataPointer++;
						if((dataPointer)==sLen) //trap for end of stream
							break;

						current = characterStream[dataPointer];
						if (current == 13 || current == 10 || current == 32 || current == 40 || current == 47 || current == 91 || (firstChar=='/' && current=='<'))
//							// '('=40	'/'=47  '['=91
							break;

					}

					end=dataPointer;

					if(debug)
						System.out.println("end="+end);
				}

				if(debug)
					System.out.println("stored start="+start+" end="+end);

				if(end<characterStream.length){
					int next=(int)characterStream[end];
					if(next==47 || next==91)
						end--;
				}

				opStart[currentOp]=start;
				opEnd[currentOp]=end;

				

				currentOp++;
				if (currentOp == this.MAXOPS)
					currentOp = 0;
				operandCount++;

			}

			//increment pointer
			if(dataPointer < charCount){

				nextChar=(int)characterStream[dataPointer];
				if(nextChar != 47 && nextChar != 40 && nextChar!= 91  && nextChar!= '<'){
					dataPointer++;
					if(dataPointer<charCount)
						nextChar=(int)characterStream[dataPointer];
				}
			}

			//break at end
			if ((charCount <= dataPointer))
				break;
		}
	}

	////////////////////////////////////////////////////////////////////////
	final private void d1(float urX,float llX,float wX,float urY,float llY,float wY) {

		//flag to show we use text colour or colour in stream
		ignoreColors=true;

		/**/
		//not fully implemented
		//float urY = Float.parseFloat(generateOpAsString(0,characterStream));
		//float urX = Float.parseFloat(generateOpAsString(1,characterStream));
		//float llY = Float.parseFloat(generateOpAsString(2,characterStream));
		//float llX = Float.parseFloat(generateOpAsString(3,characterStream));
		//float wY = Float.parseFloat(generateOpAsString(4,characterStream));
		//float wX = Float.parseFloat(generateOpAsString(5,characterStream));
		/***/

		//this.minX=(int)llX;
		//this.minY=(int)llY;

		//currentGraphicsState = new GraphicsState(0,0);/*remove values on contrutor%%*/

		//setup image to draw on
		//current.init((int)(wX),(int)(urY-llY+1));

		//wH=urY;
		//wW=llX;

		T3maxWidth=(int)wX;
		if(wX==0)
			T3maxWidth=(int)(llX-urX);
		else
			T3maxWidth=(int)wX; //Float.parseFloat(generateOpAsString(5,characterStream));

		T3maxHeight=(int)wY;
		if(wY==0)
			T3maxHeight=(int)(urY-llY);
		else
			T3maxHeight=(int)wY; //Float.parseFloat(generateOpAsString(5,characterStream));

		/***/
	}
	////////////////////////////////////////////////////////////////////////
	final private void d0(int w,int y) {

		//flag to show we use text colour or colour in stream
		ignoreColors=false;

		//float glyphX = Float.parseFloat((String) operand.elementAt(0));
		T3maxWidth=w;
		T3maxHeight=y;

		//setup image to draw on
		//current.init((int)glyphX,(int)glyphY);

	}
	////////////////////////////////////////////////////////////////////////
	final private void TD(boolean isLowerCase,float x,float y) {

		relativeMove(x, y);

		if (!isLowerCase) { //set leading as well
			float TL = -y;
			currentTextState.setLeading(TL);
		}
		multipleTJs=false;


	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get postscript data (which may be split across several objects)
	 */
	final private byte[] readPageIntoStream(PdfObject pdfObject){

		LogWriter.writeMethod("{readPageIntoStream}", 0);

		byte[][] pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);

		//reset buffer object
		byte[] binary_data = new byte[0];

		//exit on empty
		if(pageContents==null || (pageContents!=null && pageContents.length>0 && pageContents[0]==null))
			return binary_data;

		/**read an array*/
		if(pageContents!=null){

			int count=pageContents.length;

			byte[] decoded_stream_data=null;

			//read all objects for page into stream
			for(int ii=0;ii<count;ii++) {

				//if(pageContents[ii].length==0)
				//	break;

				//get the data for an object
				//currentPdfFile.resetCache();
		//decoded_stream_data =currentPdfFile.readStream(new String(pageContents[ii]),true);

                PdfObject streamData=new StreamObject(new String(pageContents[ii]));
                currentPdfFile.readObject(streamData);
                decoded_stream_data=streamData.getDecodedStream();
                
                //System.out.println(decoded_stream_data+" "+OLDdecoded_stream_data);
                if(ii==0 && decoded_stream_data!=null)
					binary_data=decoded_stream_data;
				else
					binary_data = appendData(binary_data, decoded_stream_data);
			}
		}

		return binary_data;
	}

	/**
	 * append into data_buffer by copying processed_data then
	 * binary_data into temp and then temp back into binary_data
	 */
	private static byte[] appendData(byte[] binary_data, byte[] decoded_stream_data) {

		if (decoded_stream_data != null){
			int current_length = binary_data.length + 1;

			//find end of our data which we decompressed.
			int processed_length = decoded_stream_data.length;
			if (processed_length > 0) { //trap error
				while (decoded_stream_data[processed_length - 1] == 0)
					processed_length--;

				//put current into temp so I can resize array
				byte[] temp = new byte[current_length];
				System.arraycopy(
						binary_data,
						0,
						temp,
						0,
						current_length - 1);

				//add a space between streams
				temp[current_length - 1] =  ' ';

				//resize
				binary_data = new byte[current_length + processed_length];

				//put original data back
				System.arraycopy(temp, 0, binary_data, 0, current_length);

				//and add in new data
				System.arraycopy(decoded_stream_data,0,binary_data,current_length,processed_length);
			}
		}
		return binary_data;
	}

	/**
	 * convert to to String
	 */
	private String generateOpAsString(int p,byte[] dataStream, boolean loseSlashPrefix) {

		String s="";

		int start=this.opStart[p];

		//remove / on keys
		if(loseSlashPrefix && dataStream[start]==47)
			start++;

		int end=this.opEnd[p];

		//lose spaces or returns at end
		while((dataStream[end]==32)||(dataStream[end]==13)||(dataStream[end]==10))
			end--;

		int count=end-start+1;

		//discount duplicate spaces
		int spaces=0;
		for(int ii=0;ii<count;ii++){
			if((ii>0)&&((dataStream[start+ii]==32)||(dataStream[start+ii]==13)||(dataStream[start+ii]==10))&&
					((dataStream[start+ii-1]==32)||(dataStream[start+ii-1]==13)||(dataStream[start+ii-1]==10)))
				spaces++;
		}

		char[] charString=new char[count-spaces];
		int pos=0;

		for(int ii=0;ii<count;ii++){
			if((ii>0)&&((dataStream[start+ii]==32)||(dataStream[start+ii]==13)||(dataStream[start+ii]==10))&&
					((dataStream[start+ii-1]==32)||(dataStream[start+ii-1]==13)||(dataStream[start+ii-1]==10)))
			{
			}else{
				if((dataStream[start+ii]==10)||(dataStream[start+ii]==13))
					charString[pos]=' ';
				else
					charString[pos]=(char)dataStream[start+ii];
				pos++;
			}
		}

		s=String.copyValueOf(charString);

		return s;

	}

	////////////////////////////////////////////////////
	final private void BT() {

		//set values used in plot
		currentTextState.resetTm();

		//keep position in case we need
		currentTextState.setTMAtLineStart();

		//currentGraphicsState.setClippingShape(null);

		//font info
		currentFont = currentTextState.getFontName();
		currentTextState.setCurrentFontSize(0);
		lastFontSize=-1;

		//currentGraphicsState.setLineWidth(0);

		//save for later and set TR
		if (renderPage == true){

			if(renderDirectly){
				current.renderClip(gs.getClippingShape(),null,defaultClip,g2);
			}else{
				current.drawClip(gs) ;
				current.drawTR(GraphicsState.FILL);
				//current.setLineWidth(0);
				//  current.drawColor((Color)currentGraphicsState.getNonstrokeColor(),GraphicsState.FILL);
				//   current.drawColor((Color)currentGraphicsState.getStrokeColor(),GraphicsState.STROKE);

			}
		}
	}

	//////////////////////////////////////////////////////////
	/**
	 * restore GraphicsState status from graphics stack
	 */
	final private void restoreGraphicsState() {

		if(!isStackInitialised){

			LogWriter.writeLog("No GraphicsState saved to retrieve");

		}else{

			gs = (GraphicsState) graphicsStateStack.pull();
			currentTextState = (TextState) textStateStack.pull();

			strokeColorSpace=(GenericColorSpace) strokeColorStateStack.pull();
			nonstrokeColorSpace=(GenericColorSpace) nonstrokeColorStateStack.pull();

			Object currentClip=clipStack.pull();

			if(currentClip==null)
				gs.setClippingShape(null);
			else
				gs.setClippingShape((Area)currentClip);

			//save for later
			if (renderPage == true){

                nonStrokeAlpha=gs.getNonStrokeAlpha();
                strokeAlpha=gs.getStrokeAlpha();

				if(renderDirectly)
					current.renderClip(gs.getClippingShape(),null,defaultClip,g2);
				else{
					current.drawClip(gs) ;/**/

					current.resetOnColorspaceChange();
					current.drawFillColor(gs.getNonstrokeColor());
					current.drawStrokeColor(gs.getStrokeColor());

					/**
					 * align display
					 */
                    current.setGraphicsState(GraphicsState.FILL,nonStrokeAlpha);
					current.setGraphicsState(GraphicsState.STROKE,strokeAlpha);

					//current.drawTR(currentGraphicsState.getTextRenderType()); //reset TR value
				}
			}


		}
	}
	///////////////////////////////////////////////////////////////////////
	final private void L(float x,float y) {
		currentDrawShape.lineTo(x,y);
	}
	///////////////////////////////////////////////////////////////////////
	final private void F(boolean isStar) {

		if(isLayerVisible){

			//set Winding rule
			if (isStar){
				currentDrawShape.setEVENODDWindingRule();
			}else
				currentDrawShape.setNONZEROWindingRule();

			currentDrawShape.closeShape();

			//generate shape and stroke
			Shape currentShape =
				currentDrawShape.generateShapeFromPath(gs.getClippingShape(),
						gs.CTM,
						isClip, pageLines,true,nonstrokeColorSpace.getColor(),
						gs.getLineWidth(),pageData.getCropBoxWidth(1));


			//simulate overPrint - may need changing to draw at back of stack
			if(nonstrokeColorSpace.getID()==ColorSpaces.DeviceCMYK && gs.getOPM()==1.0f){

				PdfArrayIterator BMvalue = gs.getBM();

				//check not handled elsewhere
				int firstValue=PdfDictionary.Unknown;
				if(BMvalue !=null && BMvalue.hasMoreTokens()) {
					firstValue= BMvalue.getNextValueAsConstant(false);
				}

				if(firstValue==PdfDictionary.Multiply){

					float[] rawData=nonstrokeColorSpace.getRawValues();

					if(rawData!=null && rawData[3]==1){

						//try to keep as binary if possible
                        //boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
                        //if(hasObjectBehind){
							currentShape=null;
                        //}
					}
				}
			}

			
			if(nonstrokeColorSpace.getID()==ColorSpaces.ICC && gs.getOPM()==1.0f){

				PdfArrayIterator BMvalue = gs.getBM();

				//check not handled elsewhere
				int firstValue=PdfDictionary.Unknown;
				if(BMvalue !=null && BMvalue.hasMoreTokens()) {
					firstValue= BMvalue.getNextValueAsConstant(false);
				}

				if(firstValue==PdfDictionary.Multiply){

					float[] rawData=nonstrokeColorSpace.getRawValues();

					/**if(rawData!=null && rawData[2]==1){

                        //try to keep as binary if possible
                        boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
                        if(hasObjectBehind)
                            currentShape=null;
                    }else*/{ //if zero just remove
                    	int elements=rawData.length;
                    	boolean isZero=true;
                    	for(int ii=0;ii<elements;ii++)
                    		if(rawData[ii]!=0)
                    			isZero=false;

                    	if(isZero)
                    		currentShape=null;
                    }
				}
			}


            //do not paint white CMYK in overpaint mode
            if(gs.getNonStrokeAlpha()<1 && nonstrokeColorSpace.getID()==ColorSpaces.DeviceN && gs.getOPM()==1.0f && nonstrokeColorSpace.getColor().getRGB()==-16777216 ){
            	
            	//System.out.println(gs.getNonStrokeAlpha());
            	//System.out.println(nonstrokeColorSpace.getAlternateColorSpace()+" "+nonstrokeColorSpace.getColorComponentCount()+" "+nonstrokeColorSpace.pantoneName);
                boolean ignoreTransparent =true; //assume true and disprove
                float[] raw=nonstrokeColorSpace.getRawValues();

                if(raw!=   null){
                    int count=raw.length;
                    for(int ii=0;ii<count;ii++){

                        //System.out.println(ii+"="+raw[ii]+" "+count);

                        if(raw[ii]>0){
                            ignoreTransparent =false;
                            ii=count;
                        }
                    }
                }

                if(ignoreTransparent){
                    currentShape=null;
                }
            }

			//save for later
            if (renderPage && currentShape!=null){

				gs.setStrokeColor( strokeColorSpace.getColor());
				gs.setNonstrokeColor(nonstrokeColorSpace.getColor());
				gs.setFillType(GraphicsState.FILL);

				if(renderDirectly)
                    current.renderShape(null,GraphicsState.FILL,
                                                    strokeColorSpace.getColor(), nonstrokeColorSpace.getColor(),
                                                    gs.getStroke(), currentShape, g2,
                                                    strokeAlpha, nonStrokeAlpha,renderDirectly);
                                                    //gs.getStrokeAlpha(), topLevelNonStrokeAlpha) ;

//					current.renderShape(null,gs.getFillType(),gs.getStrokeColor(),
//							gs.getNonstrokeColor(),gs.getStroke(), currentShape,g2,
//                            gs.getStrokeAlpha(),gs.getNonStrokeAlpha()) ;
				else
					current.drawShape(currentShape,gs) ;

			}
		}
		//always reset flag
		isClip = false;
		currentDrawShape.resetPath(); // flush all path ops stored

	}
	////////////////////////////////////////////////////////////////////////
	final private void TC(float tc) {
		currentTextState.setCharacterSpacing(tc);
	}
	////////////////////////////////////////////////////////
	final private void CM(float[][] Trm) {

		//multiply to get new CTM
		gs.CTM =Matrix.multiply(Trm, gs.CTM);

		multipleTJs=false;

	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * used by TD and T* to move current co-ord
	 */
	protected final void relativeMove(float new_x, float new_y) {

		//create matrix to update Tm
		float[][] temp = new float[3][3];

		currentTextState.Tm = currentTextState.getTMAtLineStart();

		//set Tm matrix
		temp[0][0] = 1;
		temp[0][1] = 0;
		temp[0][2] = 0;
		temp[1][0] = 0;
		temp[1][1] = 1;
		temp[1][2] = 0;
		temp[2][0] = new_x;
		temp[2][1] = new_y;
		temp[2][2] = 1;

		//multiply to get new Tm
		currentTextState.Tm = Matrix.multiply(temp, currentTextState.Tm);

		currentTextState.setTMAtLineStart();

		if(currentRotation!=0){
			//create matrix to update Tm
			float[][] temp2 = new float[3][3];

			currentTextState.TmNoRotation = currentTextState.getTMAtLineStartNoRotation();

			//set Tm matrix
			temp2[0][0] = 1;
			temp2[0][1] = 0;
			temp2[0][2] = 0;
			temp2[1][0] = 0;
			temp2[1][1] = 1;
			temp2[1][2] = 0;
			temp2[2][0] = new_x;
			temp2[2][1] = new_y;
			temp2[2][2] = 1;

			//multiply to get new Tm
			currentTextState.TmNoRotation = Matrix.multiply(temp2, currentTextState.TmNoRotation);

			float plusX=new_x,plusY=new_y;
			if(plusX<0)
				plusX=-new_x;
			if(plusY<0)
				plusY=-new_y;

			//if new object, recalculate
			if(plusX>currentTextState.Tm[0][0] && plusY>currentTextState.Tm[1][1])
				convertToUnrotated(currentTextState.Tm);

			currentTextState.setTMAtLineStartNoRotation();

		}

		//move command
		moveCommand = 2; //0=t*, 1=Tj, 2=TD
	}

	/**
	 * remove rotation on matrix and set unrotated
	 */
	private void convertToUnrotated(float[][] trm) {

		final boolean showCommands=false;

		if(showCommands){
			System.out.println("------------original value--------------");
			Matrix.show(trm);
		}

		//now we have it, apply to trm to turn back

		//note we convert radians to degrees - ignore if slight
		if(trm[0][1]==0 || trm[1][0]==0)
			return;

		rotationAsRadians=-Math.asin(trm[1][0]/trm[0][0]);


		//build transformation matrix by hand to avoid errors in rounding
		float[][] rotation = new float[3][3];
		rotation[0][0] = (float) Math.cos(-rotationAsRadians);
		rotation[0][1] = (float) Math.sin(-rotationAsRadians);
		rotation[0][2] = 0;
		rotation[1][0] = (float) -Math.sin(-rotationAsRadians);
		rotation[1][1] = (float) Math.cos(-rotationAsRadians);
		rotation[1][2] = 0;
		rotation[2][0] = 0;
		rotation[2][1] = 0;
		rotation[2][2] = 1;

		//round numbers if close to 1
		for (int yy = 0; yy < 3; yy++) {
			for (int xx = 0; xx < 3; xx++) {
				if ((rotation[xx][yy] > .99) & (rotation[xx][yy] < 1))
					rotation[xx][yy] = 1;
			}
		}

		//matrix for corner
		float[][] pt=new float[3][3];
		pt[0][0]=trm[2][0];//+trm[0][1];
		pt[1][1]=trm[2][1];//+trm[1][0];
		pt[2][2]=1;

		if(showCommands){
			System.out.println("---------------------pt before-----------rotation="+currentRotation+" radians="+rotationAsRadians);
			Matrix.show(pt);
		}

		pt=Matrix.multiply(rotation,pt);

		if(showCommands){
			System.out.println("---------------------pt--------------------------"+(pt[1][0]+pt[1][1]));
			Matrix.show(pt);
		}
		//apply to trm

		if(showCommands){
			System.out.println("====================before====================rotation="+currentRotation+" radians="+rotationAsRadians);
//			Matrix.show(trm);
		}
		float[][] unrotatedTrm=Matrix.multiply(rotation,trm);

		//put onto start of line
		float diffY=pt[1][0];
		float newY=pt[1][1]-diffY;
		float convertedY=currentTextState.Tm[2][1];
		Integer key=new Integer((int)(newY+.5));
		Float mappedY=(Float)lines.get(key);

		//allow for fp error
		if(mappedY==null)
			mappedY=(Float)lines.get(new Integer((int)(newY+1)));


		if(mappedY==null){
			lines.put(key,new Float(currentTextState.Tm[2][1]));

			//if(currentTextState.Tm[2][0]>1200)
			//        System.out.println(mappedY+" "+newY+" "+key+" "+lines.keySet());

		}else{
			convertedY=mappedY.floatValue();

		}

		unrotatedTrm[2][1]=convertedY;

		currentTextState.TmNoRotation=unrotatedTrm;

		//adjust matrix so all on same line if on same line
		if(unRotatedY ==-1){
			//track last line
			unRotatedY =currentTextState.TmNoRotation[2][1];
			rotatedY =currentTextState.Tm[2][1];

			//currentTextState.TmNoRotation[2][1]= rotatedY;
		}else if(1==2){
			float diff= unRotatedY -(currentTextState.TmNoRotation[2][1]);
			if(diff<0)
				diff=-diff;

			if(showCommands)
				System.out.println("diff="+diff+" currentTextState.TmNoRotation[1][1]="+currentTextState.TmNoRotation[1][1]);

			if(diff<currentTextState.TmNoRotation[1][1]){

				float diffH=lastHeight-currentTextState.TmNoRotation[1][1];
				if(diffH<0)
					diffH=-diffH;

				if(diffH<2){
					unRotatedY =currentTextState.TmNoRotation[2][1];

					currentTextState.TmNoRotation[2][1]= rotatedY;
					if(showCommands)
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>set to "+ rotatedY);
				}
			} else{
				unRotatedY =currentTextState.TmNoRotation[2][1];
				rotatedY =currentTextState.Tm[2][1];

				if(showCommands)
					System.out.println(">>>>>>>reset to "+ rotatedY+ ' '+unRotatedY);
			}
		}

		lastHeight=currentTextState.TmNoRotation[1][1];
		currentTextState.TmNoRotation[0][1]=0;
		currentTextState.TmNoRotation[1][0]=0;

		if(showCommands)
			Matrix.show(currentTextState.TmNoRotation);

		/**
         if(tokenNumber>3514)
         showCommands=false;
         else
         showCommands=true;
         /**/
	}

	////////////////////////////////////////////////////////////////////////
	final private void S(boolean isLowerCase) {

		if(isLayerVisible){

			//close for s command
			if (isLowerCase)
				currentDrawShape.closeShape();

			Shape currentShape =
				currentDrawShape.generateShapeFromPath( null,
						gs.CTM,
						isClip,pageLines,false,null,
						gs.getLineWidth(),pageData.getCropBoxWidth(1));


            //simulate overPrint - may need changing to draw at back of stack
            if(1==2 && !isLowerCase && strokeColorSpace.getID()==ColorSpaces.DeviceCMYK && gs.getOPM()==1.0f){

                PdfArrayIterator BMvalue = gs.getBM();

                //check not handled elsewhere
                int firstValue=PdfDictionary.Unknown;
                if(BMvalue !=null && BMvalue.hasMoreTokens()) {
                    firstValue= BMvalue.getNextValueAsConstant(false);
                }


                if(firstValue==PdfDictionary.Normal){// || firstValue==PdfDictionary.Multiply){

                    float[] rawData=strokeColorSpace.getRawValues();

                    if(rawData!=null  && rawData[0]==0  && rawData[1]==0  && rawData[2]==0 && rawData[3]==0){

                        //try to keep as binary if possible
                        //boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
                        //if(hasObjectBehind){
                            currentShape=null;
                        //}
                    }
                }
            }

            if(currentShape!=null){ //allow for the odd combination of crop with zero size
                Area crop=gs.getClippingShape();

                if(crop!=null && (crop.getBounds().getWidth()==0 || crop.getBounds().getHeight()==0 ))
                currentShape=null;
            }

            if(currentShape!=null){ //allow for the odd combination of f then S

				if(currentShape.getBounds().getWidth()<=1){// && currentGraphicsState.getLineWidth()<=1.0f){
					currentShape=currentShape.getBounds2D();
					//    System.out.println("XX");
				}

//				if(currentShape!=null && currentShape.getBounds().getX()>628 && currentShape.getBounds().getX()<630){
//				System.out.println(currentShape+" "+currentShape.getBounds2D());
//				Matrix.show(currentGraphicsState.CTM);

//				}

				//save for later
				if (renderPage == true){

					gs.setStrokeColor( strokeColorSpace.getColor());
					gs.setNonstrokeColor( nonstrokeColorSpace.getColor());
					gs.setFillType(GraphicsState.STROKE);

					if(renderDirectly)
						current.renderShape(null,gs.getFillType(),
								gs.getStrokeColor(),gs.getNonstrokeColor(),
								gs.getStroke(), currentShape,g2,strokeAlpha,
								nonStrokeAlpha, renderDirectly) ;
					else
						current.drawShape( currentShape,gs);

				}
			}
		}

		//always reset flag
		isClip = false;
		currentDrawShape.resetPath(); // flush all path ops stored

	}
	///////////////////////////////////////////////////////////////////////////
	final private void I() {
		//if (currentToken.equals("i")) {
		//int value =
		//	(int) Float.parseFloat((String) operand.elementAt(0));

		//set value
		//currentGraphicsState.setFlatness(value);
		//}
	}

	////////////////////////////////////////////////////////////
	final private void D(byte[] characterStream) {


		String values = ""; //used to combine values

		//and the dash array
		int items = operandCount;

		if(items==1)
			values=generateOpAsString(0,characterStream, false);
		else{
			//concat values
			StringBuffer list=new StringBuffer();
			for (int i = items - 1; i > -1; i--){
				list.append(generateOpAsString(i,characterStream, false));
				list.append(' ');
			}
			values=list.toString();
		}

		//allow for default
		if ((values.equals("[ ] 0 "))|| (values.equals("[]0"))|| (values.equals("[] 0 "))) {
			gs.setDashPhase(0);
			gs.setDashArray(new float[0]);
		} else {

			//get dash pattern
			int pointer=values.indexOf(']');

			String dash=values.substring(0,pointer);
			int phase=(int)Float.parseFloat(values.substring(pointer+1,values.length()).trim());

			//put into dash array
			float[] dash_array = PdfArray.convertToFloatArray(dash);

			//put array into global value
			gs.setDashArray(dash_array);

			//last value is phase
			gs.setDashPhase(phase);

		}
	}
	////////////////////////////////////////////////////////////////////////
	final private void SCN(boolean isLowerCase,byte[] stream)  {

		float[] values=null;

		if(isLowerCase){

			if(nonstrokeColorSpace.getID()==ColorSpaces.Pattern){
				nonstrokeColorSpace.setColor(getValuesAsString(operandCount,stream),operandCount);
			}else{
				values=getValuesAsFloat(operandCount,stream);

				float[] tempValues=new float[operandCount];
				for(int ii=0;ii<operandCount;ii++)
					tempValues[operandCount-ii-1]=values[ii];
				values=tempValues;

				//System.out.println(nonstrokeColorSpace);
				nonstrokeColorSpace.setColor(values,operandCount);
			}

            //track colrspace use
            colorspacesUsed.put(new Integer(nonstrokeColorSpace.getID()),"x");

		}else{
			if(strokeColorSpace.getID()==ColorSpaces.Pattern)
				strokeColorSpace.setColor(getValuesAsString(operandCount,stream),operandCount);
			else{
				values=getValuesAsFloat(operandCount,stream);

				float[] tempValues=new float[operandCount];
				for(int ii=0;ii<operandCount;ii++)
					tempValues[operandCount-ii-1]=values[ii];
				values=tempValues;

				strokeColorSpace.setColor(values,operandCount);
			}

            //track colrspace use
            colorspacesUsed.put(new Integer(strokeColorSpace.getID()),"x");


		}
	}

	/***
	 * COMMANDS - refer to Adobes pdf manual to
	 * see what these commands do
	 */
	/////////////////////////////////////////////////
	final private void B(boolean isStar,boolean isLowerCase) {

		if(isLayerVisible){
			//set Winding rule
			if (isStar)
				currentDrawShape.setEVENODDWindingRule();
			else
				currentDrawShape.setNONZEROWindingRule();

			//close for s command
			if (isLowerCase)
				currentDrawShape.closeShape();

			Shape currentShape =
				currentDrawShape.generateShapeFromPath( null,
						gs.CTM,
						isClip,pageLines,false,null,
						gs.getLineWidth(),pageData.getCropBoxWidth(1));


			//save for later
			if ((renderPage == true && currentShape!=null)){

				gs.setStrokeColor( strokeColorSpace.getColor());
				gs.setNonstrokeColor( nonstrokeColorSpace.getColor());
				gs.setFillType(GraphicsState.FILLSTROKE);

				if(renderDirectly)
					current.renderShape(null,gs.getFillType(),
							gs.getStrokeColor(),
							gs.getNonstrokeColor(),
							gs.getStroke(),currentShape,g2,
							gs.getStrokeAlpha(),gs.getNonStrokeAlpha(), renderDirectly);
				else
					current.drawShape( currentShape,gs) ;

			}
		}
		//always reset flag
		isClip = false;

		currentDrawShape.resetPath(); // flush all path ops stored
	}
	///////////////////////////////////////////////////////////////////////
	/**handle the M commands*/
	final private void mm(int mitre_limit) {

		//handle M command
		gs.setMitreLimit(mitre_limit);

	}

	/**handle the m commands*/
	final private void M(float x,float y) {

		//handle m command
		currentDrawShape.moveTo(x, y);


	}
	/////////////////////////////////////////////////////
	final private void J(boolean isLowerCase,int value) {

		int style = 0;
		if (!isLowerCase) {

			//map join style
			if (value == 0)
				style = BasicStroke.JOIN_MITER;
			if (value == 1)
				style = BasicStroke.JOIN_ROUND;
			if (value == 2)
				style = BasicStroke.JOIN_BEVEL;

			//set value
			gs.setJoinStyle(style);
		} else {
			//map cap style
			if (value == 0)
				style = BasicStroke.CAP_BUTT;
			if (value == 1)
				style = BasicStroke.CAP_ROUND;
			if (value == 2)
				style = BasicStroke.CAP_SQUARE;

			//set value
			gs.setCapStyle(style);
		}
	}
	////////////////////////////////////////////////////////////////////////
	final private void RG(boolean isLowerCase,byte[] stream)  {

		//ensure color values reset
		current.resetOnColorspaceChange();

		//set flag to show which color (stroke/nonstroke)
		boolean isStroke=!isLowerCase;

		float[] operand=getValuesAsFloat(operandCount,stream);

		float[] tempValues=new float[operandCount];
		for(int ii=0;ii<operandCount;ii++)
			tempValues[operandCount-ii-1]=operand[ii];
		operand=tempValues;

		//set colour
		if(isStroke){
			if (strokeColorSpace.getID() != ColorSpaces.DeviceRGB)
				strokeColorSpace=new DeviceRGBColorSpace();

			strokeColorSpace.setColor(operand,operandCount);

            //track colorspace use
            colorspacesUsed.put(new Integer(strokeColorSpace.getID()),"x");

		}else{
			if (nonstrokeColorSpace.getID() != ColorSpaces.DeviceRGB)
				nonstrokeColorSpace=new DeviceRGBColorSpace();

			nonstrokeColorSpace.setColor(operand,operandCount);

            //track colrspace use
            colorspacesUsed.put(new Integer(nonstrokeColorSpace.getID()),"x");

		}
	}
	////////////////////////////////////////////////////////////////////////
	final private void Y(float x3,float y3,float x,float y) {
		currentDrawShape.addBezierCurveY(x, y, x3, y3);
	}
	////////////////////////////////////////////////////////////////////////
	final private void TZ(float tz) {

		//Text height
		currentTextState.setHorizontalScaling(tz / 100);
	}
	////////////////////////////////////////////////////////////////////////
	final private void RE(float x,float y,float w,float h) {

		//get values
		currentDrawShape.appendRectangle(x, y, w, h);
	}
	//////////////////////////////////////////////////////////////////////
	final private void ET() {

		//currentGraphicsState.setLineWidth(0);
		//current.setLineWidth(0);

		current.resetOnColorspaceChange();

	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * put item in graphics stack
	 */
	final private void pushGraphicsState() {

		if(!isStackInitialised){
			isStackInitialised=true;

			graphicsStateStack = new Vector_Object(10);
			textStateStack = new Vector_Object(10);
			strokeColorStateStack= new Vector_Object(20);
			nonstrokeColorStateStack= new Vector_Object(20);
			clipStack=new Vector_Object(20);
		}

		//store
		graphicsStateStack.push(gs.clone());

		//store clip
		Area currentClip=gs.getClippingShape();
		if(currentClip==null)
			clipStack.push(null);
		else{
			clipStack.push(currentClip.clone());
		}
		//store text state (technically part of gs)
		textStateStack.push(currentTextState.clone());

		//save colorspaces
		nonstrokeColorStateStack.push(nonstrokeColorSpace.clone());
		strokeColorStateStack.push(strokeColorSpace.clone());

		current.resetOnColorspaceChange();

	}

	final private void MP() {

		//<start-adobe>
		if(markedContentExtracted)
			contentHandler.MP();
		//<end-adobe>

	}

	final private void DP(int startCommand, int dataPointer,byte[] raw,String op) {

		//<start-adobe>
		if(markedContentExtracted){
			
			MCObject obj=new MCObject(op);
			currentPdfFile.readObject(obj);
			
			contentHandler.DP(obj);
		}
		//<end-adobe>

	}

	////////////////////////////////////////////////////////////////////////
	final private void EMC() {

		//<start-adobe>
		if(markedContentExtracted)
			contentHandler.EMC();
		//<end-adobe>

		layerLevel--;
		//reset flag
		if(layers==null || layerLevel==0 || layerVisibility.containsKey(new Integer(layerLevel)))
			isLayerVisible=true;
		else
			isLayerVisible=false;

	}

	final private void TJ(byte[] characterStream,int startCommand,int dataPointer) {

		if(!isLayerVisible)
			return;

		//extract the text
		StringBuffer current_value=processTextArray(characterStream, startCommand,dataPointer);

		//<start-adobe>
		//will be null if no content
		if ((current_value != null) && (isPageContent)) {

			/**add raw element if not in marked content*/
			if (!markedContentExtracted) {

				//get colour if needed
				if(textColorExtracted){
					if ((gs.getTextRenderType() & GraphicsState.FILL) == GraphicsState.FILL){
						currentColor=this.nonstrokeColorSpace.getXMLColorToken();
					}else{
						currentColor=this.strokeColorSpace.getXMLColorToken();
					}
				}

				/**save item and add in graphical elements*/
				if(textExtracted){

					pdfData.addRawTextElement(
							(charSpacing * THOUSAND),
							currentTextState.writingMode,
							font_as_string,
							currentFontData.getCurrentFontSpaceWidth(),
							currentTextState,
							x1,
							y1,
							x2,
							y2,
							moveCommand,
							current_value,
							tokenNumber,
							textLength,currentColor,currentRotation);
					
//					if(DynamicVectorRenderer.newHighlight){
//						Rectangle fontbb = currentFontData.getBoundingBox();
//
//						float y = fontbb.y;
//						if(y==0) //If no y set it may be embedded so we should guess a value
//							y = 100;
//						if(y<0)
//							y = -y;
//
//						float h = 1000+(y);
//						//Percentage of fontspace used compared to default
//						h = 1000/h;
//						
//						//On screen font height
//						float fontHeight = ((y1-y2)/h);
//						
//						//Highlight length for entire font
//						y2 = y1-fontHeight;
//						
//						current.addToLineAreas(new Rectangle((int)x1, (int)(y2), (int)(x2-x1), (int)(y1-y2)), currentTextState.writingMode);
//					}
				}
			}else
				contentHandler.setText(current_value,x1,y1,x2,y2);

		}
		//<end-adobe>

		moveCommand = -1; //flags no move!
	}

	///////////////////////////////////////////////////////////////////////
	final private void G(boolean isLowerCase,byte[] stream) {

		//ensure color values reset
		current.resetOnColorspaceChange();

		boolean isStroke=!isLowerCase;
		float[] operand=getValuesAsFloat(1,stream);

		//set colour and colorspace
		if(isStroke){
			if (strokeColorSpace.getID() != ColorSpaces.DeviceGray)
				strokeColorSpace=new DeviceGrayColorSpace();

			strokeColorSpace.setColor(operand,operandCount);

            //track colrspace use
            colorspacesUsed.put(new Integer(strokeColorSpace.getID()),"x");

		}else{
			if (nonstrokeColorSpace.getID() != ColorSpaces.DeviceGray)
				nonstrokeColorSpace=new DeviceGrayColorSpace();

			nonstrokeColorSpace.setColor(operand,operandCount);

            //track colorspace use
            colorspacesUsed.put(new Integer(nonstrokeColorSpace.getID()),"x");

		}
	}

	private void TL(float tl) {
		currentTextState.setLeading(tl);
	}

	private void BDC(int startCommand, int dataPointer,byte[] raw,String op) {

		layerLevel++;

        int rawStart=startCommand;

        PdfObject BDCobj=new MCObject(op);

		if(startCommand<1)
			startCommand=1;

        boolean hasDictionary=true;
		while(startCommand<raw.length && raw[startCommand]!='<' && raw[startCommand-1]!='<'){
			startCommand++;

            if(raw[startCommand]=='B' && raw[startCommand+1]=='D' && raw[startCommand+2]=='C'){
                hasDictionary=false;
                break;
            }
        }

		/**
		 * read Dictionary object
		 */
		if(hasDictionary &&(markedContentExtracted || (layers!=null && isLayerVisible)))
            currentPdfFile.readDictionaryAsObject(BDCobj, "layer" ,startCommand+1,raw, dataPointer, "", false);
		
		
		//add in layer if visible
		if(layers!=null && isLayerVisible){

            String name="";

            if(hasDictionary){
                //see if name and if shown
                name = BDCobj.getName(PdfDictionary.OC);


                //see if Layer defined and get title if no Name as alternative
                if(name==null){

                    PdfObject layerObj=BDCobj.getDictionary(PdfDictionary.Layer);
                    if(layerObj!=null)
                        name=layerObj.getTextStreamValue(PdfDictionary.Title);
                }
            }else{ //direct just /OC and /MCxx

                //find /OC
                for(int ii=rawStart;ii<dataPointer;ii++){
                    if(raw[ii]=='/' && raw[ii+1]=='O' && raw[ii+2]=='C'){ //find oc

                        ii=ii+2;
                        //roll onto value
                        while(raw[ii]!='/')
                            ii++;

                        ii++; //roll pass /

                        int strStart=ii,charCount=0;

                        while(ii<dataPointer){
                            ii++;
                            charCount++;

                            if(raw[ii]==13 || raw[ii]==10 || raw[ii]==32 || raw[ii]=='/')
                                break;
                        }

                        name=new String(raw,strStart,charCount);

                    }
                }
            }

            if(name!=null) //name referring to Layer or Title
                isLayerVisible=layers.decodeLayer(name,true);

            //flag so we can next values
			if(isLayerVisible)
				layerVisibility.put(new Integer(layerLevel),"x");

			//@delete-start---------------------------------------------------
			//debug code
			int count=1;
			if(!isLayerVisible && 1==2){

				System.out.println(BDCobj.getName(PdfDictionary.OC)+"----------->");
				while(dataPointer<raw.length){

					System.out.print((char)raw[dataPointer]);

					if(raw[dataPointer-2]=='B' && raw[dataPointer-1]=='D' && raw[dataPointer]=='C'){
						count++;
						dataPointer=dataPointer+3;
					}else if(raw[dataPointer-2]=='B' && raw[dataPointer-1]=='M' && raw[dataPointer]=='C'){
						count++;
						dataPointer=dataPointer+3;
					}else if(raw[dataPointer-2]=='E' && raw[dataPointer-1]=='M' && raw[dataPointer]=='C'){
						count--;
						dataPointer=dataPointer+3;
					}else
						dataPointer++;

					if(count==0)
						break;
				}

				System.out.println("<----------->");



				dataPointer--;

			}
			//@delete-end
		}
		
		//get Structured Content
		if(markedContentExtracted){

			//<start-adobe>
			contentHandler.BDC(BDCobj);
			//<end-adobe>
		}
		

	}

	private void BMC(String op) {

		layerLevel++;

		//<start-adobe>
		if(markedContentExtracted)
			contentHandler.BMC(op);

		//<end-adobe>
	}

	private static final int[][] intValues={
		{0,100000,200000,300000,400000,500000,600000,700000,800000,900000},
		{0,10000,20000,30000,40000,50000,60000,70000,80000,90000},
		{0,1000,2000,3000,4000,5000,6000,7000,8000,9000},
		{0,100,200,300,400,500,600,700,800,900},
		{0,10,20,30,40,50,60,70,80,90},
		{0,1,2,3,4,5,6,7,8,9}};

	private static final float[][] floatValues={
		{0,100000f,200000f,300000f,400000f,500000f,600000f,700000f,800000f,900000f},
		{0,10000f,20000f,30000f,40000f,50000f,60000f,70000f,80000f,90000f},
		{0,1000f,2000f,3000f,4000f,5000f,6000f,7000f,8000f,9000f},
		{0,100f,200f,300f,400f,500f,600f,700f,800f,900f},
		{0,10f,20f,30f,40f,50f,60f,70f,80f,90f},
		{0,1f,2f,3f,4f,5f,6f,7f,8f,9f}};

	final float parseFloat(int id,byte[] stream){

		float f=0f,dec=0f,num=0f;

		int start=opStart[id];
		int charCount=opEnd[id]-start;

		int floatptr=charCount;
		int intStart=0;
		boolean isMinus=false;
		//hand optimised float code
		//find decimal point
		for(int j=charCount-1;j>-1;j--){
			if(stream[start+j]==46){ //'.'=46
				floatptr=j;
				break;
			}
		}

		int intChars=floatptr;
		//allow for minus
		if(stream[start]==43){ //'+'=43
			intChars--;
			intStart++;
		}else if(stream[start]==45){ //'-'=45
			//intChars--;
			intStart++;
			isMinus=true;
		}

		//optimisations
		int intNumbers=intChars-intStart;
		int decNumbers=charCount-floatptr;

		if((intNumbers>3)){ //non-optimised to cover others
			isMinus=false;
			f=Float.parseFloat(this.generateOpAsString(id,stream, false));

		}else{

			float units=0f,tens=0f,hundreds=0f,tenths=0f,hundredths=0f, thousands=0f, tenthousands=0f,hunthousands=0f;
			int c;

			//hundreds
			if(intNumbers>2){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					hundreds=100.0f;
					break;
				case 2:
					hundreds=200.0f;
					break;
				case 3:
					hundreds=300.0f;
					break;
				case 4:
					hundreds=400.0f;
					break;
				case 5:
					hundreds=500.0f;
					break;
				case 6:
					hundreds=600.0f;
					break;
				case 7:
					hundreds=700.0f;
					break;
				case 8:
					hundreds=800.0f;
					break;
				case 9:
					hundreds=900.0f;
					break;
				}
				intStart++;
			}

			//tens
			if(intNumbers>1){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					tens=10.0f;
					break;
				case 2:
					tens=20.0f;
					break;
				case 3:
					tens=30.0f;
					break;
				case 4:
					tens=40.0f;
					break;
				case 5:
					tens=50.0f;
					break;
				case 6:
					tens=60.0f;
					break;
				case 7:
					tens=70.0f;
					break;
				case 8:
					tens=80.0f;
					break;
				case 9:
					tens=90.0f;
					break;
				}
				intStart++;
			}

			//units
			if(intNumbers>0){
				c=stream[start+intStart]-48;
				switch(c){
				case 1:
					units=1.0f;
					break;
				case 2:
					units=2.0f;
					break;
				case 3:
					units=3.0f;
					break;
				case 4:
					units=4.0f;
					break;
				case 5:
					units=5.0f;
					break;
				case 6:
					units=6.0f;
					break;
				case 7:
					units=7.0f;
					break;
				case 8:
					units=8.0f;
					break;
				case 9:
					units=9.0f;
					break;
				}
			}

			//tenths
			if(decNumbers>1){
				floatptr++; //move beyond.
				c=stream[start+floatptr]-48;
				switch(c){
				case 1:
					tenths=0.1f;
					break;
				case 2:
					tenths=0.2f;
					break;
				case 3:
					tenths=0.3f;
					break;
				case 4:
					tenths=0.4f;
					break;
				case 5:
					tenths=0.5f;
					break;
				case 6:
					tenths=0.6f;
					break;
				case 7:
					tenths=0.7f;
					break;
				case 8:
					tenths=0.8f;
					break;
				case 9:
					tenths=0.9f;
					break;
				}
			}

			//hundredths
			if(decNumbers>2){
				floatptr++; //move beyond.
				//c=value.charAt(floatptr)-48;
				c=stream[start+floatptr]-48;
				switch(c){
				case 1:
					hundredths=0.01f;
					break;
				case 2:
					hundredths=0.02f;
					break;
				case 3:
					hundredths=0.03f;
					break;
				case 4:
					hundredths=0.04f;
					break;
				case 5:
					hundredths=0.05f;
					break;
				case 6:
					hundredths=0.06f;
					break;
				case 7:
					hundredths=0.07f;
					break;
				case 8:
					hundredths=0.08f;
					break;
				case 9:
					hundredths=0.09f;
					break;
				}
			}

			//thousands
			if(decNumbers>3){
				floatptr++; //move beyond.
				c=stream[start+floatptr]-48;
				switch(c){
				case 1:
					thousands=0.001f;
					break;
				case 2:
					thousands=0.002f;
					break;
				case 3:
					thousands=0.003f;
					break;
				case 4:
					thousands=0.004f;
					break;
				case 5:
					thousands=0.005f;
					break;
				case 6:
					thousands=0.006f;
					break;
				case 7:
					thousands=0.007f;
					break;
				case 8:
					thousands=0.008f;
					break;
				case 9:
					thousands=0.009f;
					break;
				}
			}

			//tenthousands
			if(decNumbers>4){
				floatptr++; //move beyond.
				c=stream[start+floatptr]-48;
				switch(c){
				case 1:
					tenthousands=0.0001f;
					break;
				case 2:
					tenthousands=0.0002f;
					break;
				case 3:
					tenthousands=0.0003f;
					break;
				case 4:
					tenthousands=0.0004f;
					break;
				case 5:
					tenthousands=0.0005f;
					break;
				case 6:
					tenthousands=0.0006f;
					break;
				case 7:
					tenthousands=0.0007f;
					break;
				case 8:
					tenthousands=0.0008f;
					break;
				case 9:
					tenthousands=0.0009f;
					break;
				}
			}

//			tenthousands
			if(decNumbers>5){
				floatptr++; //move beyond.
				c=stream[start+floatptr]-48;

				switch(c){
				case 1:
					hunthousands=0.00001f;
					break;
				case 2:
					hunthousands=0.00002f;
					break;
				case 3:
					hunthousands=0.00003f;
					break;
				case 4:
					hunthousands=0.00004f;
					break;
				case 5:
					hunthousands=0.00005f;
					break;
				case 6:
					hunthousands=0.00006f;
					break;
				case 7:
					hunthousands=0.00007f;
					break;
				case 8:
					hunthousands=0.00008f;
					break;
				case 9:
					hunthousands=0.00009f;
					break;
				}
			}

			dec=tenths+hundredths+thousands+tenthousands+hunthousands;
			num=hundreds+tens+units;
			f=num+dec;

		}

		if(isMinus)
			return -f;
		else
			return f;
	}


	final int parseInt(int id,byte[] stream){

		int number=0;

		int start=opStart[id];
		int charCount=opEnd[id]-start;

		int intStart=0;
		boolean isMinus=false;


		int intChars=charCount;
		//allow for minus
		if(stream[start]==43){ //'+'=43
			intChars--;
			intStart++;
		}else if(stream[start]==45){ //'-'=45
			//intChars--;
			intStart++;
			isMinus=true;
		}

		//optimisations
		int intNumbers=intChars-intStart;

		if((intNumbers>6)){ //non-optimised to cover others
			isMinus=false;
			number=Integer.parseInt(this.generateOpAsString(id,stream, false));

		}else{ //optimise dlookup version

			int c;

			for(int jj=5;jj>-1;jj--){
				if(intNumbers>jj){
					c=stream[start+intStart]-48;
					number=number+intValues[5-jj][c];
					intStart++;
				}
			}
		}

		if(isMinus)
			return -number;
		else
			return number;
	}

	////////////////////////////////////////////////////////////////////////
	final private void TM() {

		if(includeRotation){

			float[][] trm=currentTextState.Tm;


			if(trm[1][0]==0 && trm[0][1]==0){
				currentRotation=0;
				unRotatedY =-1;
			}else{

				//note we convert radians to degrees - ignore if slight
				if(trm[0][1]==0 || trm[1][0]==0){
					currentRotation=0;
					unRotatedY =-1;

				}else{
					rotationAsRadians=-Math.asin(trm[1][0]/trm[0][0]);

					int newRotation=(int)(rotationAsRadians*radiansToDegrees);

					if(newRotation==0){
						currentRotation=0;
						unRotatedY =-1;
					}else{
						//set new rotation
						currentRotation = newRotation;
						convertToUnrotated(trm);
					}
				}
			}
		}

		//keep position in case we need
		currentTextState.setTMAtLineStart();
		currentTextState.setTMAtLineStartNoRotation();

		multipleTJs=false;

		//move command
		moveCommand = 1; //0=t*, 1=Tj, 2=TD

	}

	//////////////////////////////////////////////////////////////////////////
	final private void H() {
		currentDrawShape.closeShape();
	}
	////////////////////////////////////////////////////////////////////////
	final private void TR(int value) {

		//Text render mode

		if (value == 0)
			value = GraphicsState.FILL;
		else if (value == 1)
			value = GraphicsState.STROKE;
		else if (value == 2)
			value = GraphicsState.FILLSTROKE;
		else if(value==3){
			value = GraphicsState.INVISIBLE;

			//allow user to over-ride
			if(showInvisibleText)
				value=GraphicsState.FILL;

		}else if(value==7)
			value = GraphicsState.CLIPTEXT;

		gs.setTextRenderType(value);

		if (renderPage == true){

			if(!renderDirectly)
				current.drawTR(value);
		}

	}
	////////////////////////////////////////////////////////////////////////
	final private void Q(boolean isLowerCase) {

		//save or retrieve
		if (isLowerCase)
			pushGraphicsState();
		else{
			restoreGraphicsState();

			//switch to correct font
			String fontID=currentTextState.getFontID();

			PdfFont restoredFont = resolveFont(fontID);
			if(restoredFont!=null){
				currentFontData=restoredFont;

				current.drawFontBounds(currentFontData.getBoundingBox());
			}
		}
	}

	/**
	 * decode or get font
	 * @param fontID
	 */
	private PdfFont resolveFont(String fontID) {

		PdfFont restoredFont=(PdfFont) resolvedFonts.get(fontID);

		//check it was decoded
		if(restoredFont==null){

			String ref=(String)unresolvedFonts.get(fontID);

			if(ref!=null){

				//remove from list
				unresolvedFonts.remove(fontID);

				PdfObject newFont=new FontObject(ref);

//                if(ref!=null && ref.length()>2 && ref.charAt(0)=='<') {
//                    System.out.println("XXX");
//                    newFont.setStatus(PdfObject.UNDECODED_DIRECT);
//                }else
                    newFont.setStatus(PdfObject.UNDECODED_REF);

                //must be done AFTER setStatus()

//                if(ref.charAt(0)=='<'){
//                //ref=ref.substring(2,ref.length());
//                System.out.println(ref);
//                    newFont.setStatus(PdfObject.UNDECODED_DIRECT);
//                }
                newFont.setUnresolvedData(ref.getBytes(), PdfDictionary.Font);
                currentPdfFile.checkResolved(newFont);

				//currentPdfFile.readObject(newFont);

				try {
					restoredFont = createFont(newFont,fontID);
				} catch (PdfException e) {
					e.printStackTrace();
				}

				//store
				if(restoredFont!=null)
					resolvedFonts.put(fontID,restoredFont);
			}
		}

		return restoredFont;
	}

	final private int ID(byte[] stream, int startInlineStream, int dataPointer) throws Exception{
		
		/**
		 * read Dictionary
		 */
		PdfObject XObject=new XObject(1);
		currentPdfFile.readDictionaryAsObject(XObject,"",startInlineStream,stream, dataPointer-2,"", true);

		//reset global flag
		isMask=false;

		BufferedImage image =   null;

		boolean inline_imageMask = false;

		//store pointer to current place in file
		int inline_start_pointer = dataPointer + 1;
		int i_w = 0, i_h = 0, i_bpc = 0;

		//find end of stream
		int i = inline_start_pointer;
		int streamLength=stream.length;

		//find end
		while (true) {

			//System.out.println(i+"="+stream[i]+" "+stream[i+1]+" "+stream[i+2]+" "+stream[i+3]+" "+stream[i+4]+" "
              //      +(char)stream[i]+""+(char)stream[i+1]+""+(char)stream[i+2]+""+(char)stream[i+3]+""+(char)stream[i+4]+""+(char)stream[i+5]);
			//look for end EI
			
			//handle Pdflib variety
			if (streamLength-i>3 &&  stream[i + 1] == 69 && stream[i + 2] == 73 && stream[i+3] == 10)
				break;
 
			//general case
			if ((streamLength-i>3)&&(stream[i] == 32 || stream[i] == 10 || stream[i] == 13 ||  (stream[i+3] == 32 && stream[i+4] == 'Q'))
					&& (stream[i + 1] == 69)
					&& (stream[i + 2] == 73)
					&& ( stream[i+3] == 32 || stream[i+3] == 10 || stream[i+3] == 13))
				break;



			i++;

			if(i==streamLength)
				break;
		}

		if(isLayerVisible && (renderImages || finalImagesExtracted || clippedImagesExtracted || rawImagesExtracted)){

			//load the data
			//		generate the name including file name to make it unique
			String image_name =this.fileName+ "-IN-" + tokenNumber;

			int endPtr=i;
			//hack for odd files
			if(i<stream.length && stream[endPtr] != 32 && stream[endPtr] != 10 && stream[endPtr] != 13)
				endPtr++;

			/**
			 * put image data in array
			 */
			byte[] i_data = new byte[endPtr - inline_start_pointer];
			System.arraycopy(
					stream,
					inline_start_pointer,
					i_data,
					0,
					endPtr - inline_start_pointer);

            //System.out.print(">>");
            //for(int ss=inline_start_pointer-5;ss<endPtr+15;ss++)
            //System.out.print((char)stream[ss]);
            //System.out.println("<<"+i_data.length+" end="+endPtr);
			//pass in image data
			XObject.setStream(i_data);
            
			/**
			 * work out colorspace
			 */
			PdfObject ColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);

			//check for Named value
			if(ColorSpace!=null){
				String colKey=ColorSpace.getGeneralStringValue();

				if(colKey!=null){
					Object col=colorspaces.get(colKey);

					if(col!=null)
						ColorSpace=(PdfObject) col;
					else{
						//throw new RuntimeException("error with "+colKey+" on ID "+colorspaces);
					}
				}
			}

			if(ColorSpace!=null && ColorSpace.getParameterConstant(PdfDictionary.ColorSpace)==PdfDictionary.Unknown)
				ColorSpace=null; //no values set

			/**
			 * allow user to process image
			 */
			if(customImageHandler!=null)
				image=customImageHandler.processImageData(gs,XObject, ColorSpace);

			PdfArrayIterator filters = XObject.getMixedArray(PdfDictionary.Filter);

			//check not handled elsewhere
			int firstValue=PdfDictionary.Unknown;
			boolean needsDecoding=false;
			if(filters!=null && filters.hasMoreTokens()){
				firstValue=filters.getNextValueAsConstant(false);

				needsDecoding=(firstValue!=PdfFilteredReader.JPXDecode && firstValue!=PdfFilteredReader.DCTDecode);
			}

			i_w=XObject.getInt(PdfDictionary.Width);
			i_h=XObject.getInt(PdfDictionary.Height);
			i_bpc=XObject.getInt(PdfDictionary.BitsPerComponent);
			inline_imageMask=XObject.getBoolean(PdfDictionary.ImageMask);

			//handle filters (JPXDecode/DCT decode is handle by process image)
			if(needsDecoding){

				PdfObject DecodeParms=XObject.getDictionary(PdfDictionary.DecodeParms);

				byte[] globalData=null;//used by JBIG but needs to be read now so we can decode
				if(DecodeParms!=null){
					PdfObject Globals=DecodeParms.getDictionary(PdfDictionary.JBIG2Globals);
					if(Globals!=null)
						globalData=currentPdfFile.readStream(Globals,true,true,false, false,false, null);
				}

				i_data=currentPdfFile.decodeFilters(DecodeParms, i_data, filters, i_w, i_h, true, globalData, null);
			}

			//handle colour information
			GenericColorSpace decodeColorData=new DeviceRGBColorSpace();
			if(ColorSpace!=null)
				decodeColorData=ColorspaceFactory.getColorSpaceInstance(isPrinting,currentPdfFile,
                        ColorSpace, colorspacesObjects, null, false);

			if(i_data!=null){
				//flag to show if plotted and generates image (stored in global image object)
                //disabled as breaks adopted_leg_dist1.pdf when layers opened
                boolean alreadyCached=false;//(!isType3Font && useHiResImageForDisplay && current.isImageCached(this.pageNum));

				optionsApplied= PDFImageProcessing.NOTHING;

                if(!alreadyCached &&(customImageHandler==null ||(image==null && !customImageHandler.alwaysIgnoreGenericHandler())))
					image=processImage(decodeColorData,
							i_data,
							image_name,
							i_w,
							i_h,
							i_bpc,
							inline_imageMask,
							createScaledVersion,
							XObject,false);

				
				//generate name including filename to make it unique
				currentImage = image_name;
				if (image != null || alreadyCached){
					if(renderDirectly || this.useHiResImageForDisplay){

						gs.x=gs.CTM[2][0];
						gs.y=gs.CTM[2][1];
						if(renderDirectly){
							current.renderImage(null,image,gs.getNonStrokeAlpha(),
									gs,g2,gs.x,gs.y, optionsApplied);
						}else
							current.drawImage(pageNum, image, gs, alreadyCached, image_name, optionsApplied);
					}else{
						if(this.clippedImagesExtracted)
							generateTransformedImage(image,image_name);
						else
							generateTransformedImageSingle(image,image_name);
					}

					if(image!=null)
						image.flush();
				}
			}
		}

		dataPointer = i + 3;

		return dataPointer;

	}
	////////////////////////////////////////////////////////////////////////
	final private void TS(float ts) {

		//Text rise
		currentTextState.setTextRise(ts);
	}
	////////////////////////////////////////////////////////////////////////
	final private void double_quote(byte[] characterStream,int startCommand,int dataPointer,float tc,float tw) {

		//Tc part
		currentTextState.setCharacterSpacing(tc);

		//Tw
		currentTextState.setWordSpacing(tw);
		TSTAR();
		TJ(characterStream, startCommand,dataPointer);
	}
	////////////////////////////////////////////////////////////////////////
	private  void TSTAR() {
		relativeMove(0, -currentTextState.getLeading());

		//move command
		moveCommand = 0; //0=t*, 1=Tj, 2=TD

		multipleTJs=false;
	}
	//////////////////
	final private void K(boolean isLowerCase,byte[] stream) {

		//ensure color values reset
		current.resetOnColorspaceChange();

		//set flag to show which color (stroke/nonstroke)
		boolean isStroke=!isLowerCase;

		/**allow for less than 4 values
		 * (ie second mapping for device colourspace
		 */
		if (operandCount > 3) {

			float[] operand=getValuesAsFloat(operandCount,stream);

			float[] tempValues=new float[operandCount];
			for(int ii=0;ii<operandCount;ii++)
				tempValues[operandCount-ii-1]=operand[ii];
			operand=tempValues;

			//set colour and make sure in correct colorspace
			if(isStroke){
				if (strokeColorSpace.getID() != ColorSpaces.DeviceCMYK)
					strokeColorSpace=new DeviceCMYKColorSpace();

				strokeColorSpace.setColor(operand,operandCount);

                //track colorspace use
                colorspacesUsed.put(new Integer(strokeColorSpace.getID()),"x");

			}else{
				if (nonstrokeColorSpace.getID() != ColorSpaces.DeviceCMYK)
					nonstrokeColorSpace=new DeviceCMYKColorSpace();
			
				//make white on forms transparent				
				if(!newForms && formLevel>0 && groupObj!=null && !groupObj.getBoolean(PdfDictionary.K) && (nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK) &&
						operand[0]==0 && operand[1]==0 &&
						operand[2]==0 && operand[3]==0){
		
					nonstrokeColorSpace.setColorIsTransparent();
				
				}else
					nonstrokeColorSpace.setColor(operand,operandCount);

                //track colorspace use
                colorspacesUsed.put(new Integer(nonstrokeColorSpace.getID()),"x");

			}
		}
	}

	private String[] getValuesAsString(int count,byte[] dataStream) {

		String[] op=new String[count];
		for(int i=0;i<count;i++)
			op[i]=this.generateOpAsString(i,dataStream, true);
		return op;
	}

	private float[] getValuesAsFloat(int count,byte[] dataStream) {

		float[] op=new float[count];
		for(int i=0;i<count;i++)
			op[i]=this.parseFloat(i,dataStream);
		return op;
	}

	final private void W(boolean isStar) {

		//set Winding rule
		if (isStar)
			currentDrawShape.setEVENODDWindingRule();
		else
			currentDrawShape.setNONZEROWindingRule();

		//set clipping flag
		isClip = true;

	}

	/**set width from lower case w*/
	final private void width(float w) {

		//ensure minimum width
		//if(w<1)
		//w=1;

		gs.setLineWidth(w);

	}

	final private void one_quote(
			byte[] characterStream,
			int startCommand,int dataPointer) {

		if(!isLayerVisible)
			return;

		TSTAR();
		TJ(characterStream, startCommand,dataPointer);

	}

	private void N() {

		if (isClip == true) {

			//create clipped shape
			currentDrawShape.closeShape();

            Shape s=currentDrawShape.generateShapeFromPath(  null,
					gs.CTM,
					false,null,false,null,0,0);

            //ignore huge shapes which will crash Java
            if(currentDrawShape.getSegmentCount()<5000){
                Area newClip=new Area(s);

                gs.updateClip(newClip);
            }

            gs.checkWholePageClip(pageData.getMediaBoxHeight(pageNum)+pageData.getMediaBoxY(pageNum));

			//always reset flag
			isClip = false;

			//save for later
			if (renderPage){
				if(renderDirectly){

					//set the stroke to current value
					//Stroke newStroke=currentGraphicsState.getStroke();
					//g2.setStroke(newStroke);

					current.renderClip(gs.getClippingShape(),null,defaultClip,g2) ;
				}else
					current.drawClip(gs) ;
			}
		}

		currentDrawShape.resetPath(); // flush all path ops stored

	}


	////////////////////////////////////////////////////////////////////////
	final private void sh(String shadingObject) {

		if (renderPage){

			PdfObject Shading= (PdfObject) shadings.get(shadingObject);

            //workout shape
			Shape shadeShape=gs.getClippingShape();
			if(shadeShape==null)
				shadeShape=new Rectangle(pageData.getMediaBoxX(pageNum),pageData.getMediaBoxY(pageNum),pageData.getMediaBoxWidth(pageNum),pageData.getMediaBoxHeight(pageNum));

			/**
			 * generate the appropriate shading and then colour in the current clip with it
			 */
			try{

				/**
				 * workout colorspace
				 **/				
				PdfObject ColorSpace=Shading.getDictionary(PdfDictionary.ColorSpace);

				GenericColorSpace newColorSpace=ColorspaceFactory.getColorSpaceInstance(isPrinting,
                        currentPdfFile, ColorSpace, colorspacesObjects, null, false);

				/**setup shading object*/

				PdfPaint shading=ShadingFactory.createShading(Shading, isPrinting,this.pageH,newColorSpace,currentPdfFile,gs.CTM,pageH,false);

                if(shading!=null){
					/**
					 * shade the current clip
					 */
					gs.setFillType(GraphicsState.FILL);

					gs.setNonstrokeColor(shading);

                    //track colrspace use
                    colorspacesUsed.put(new Integer(newColorSpace.getID()),"x");

                    if(renderDirectly)
						current.renderShape(null,gs.getFillType(),
								gs.getStrokeColor(),
								gs.getNonstrokeColor(),
								gs.getStroke(), shadeShape,g2,
								gs.getStrokeAlpha(),
								gs.getNonStrokeAlpha(), renderDirectly) ;
					else
						current.drawShape(shadeShape,gs) ;
				}
			}catch(Exception e){
			}
		}

	}
	////////////////////////////////////////////////////////////////////////
	final private void TW(float tw) {
		currentTextState.setWordSpacing(tw);
	}
	////////////////////////////////////////////////////////
	final private void CS(boolean isLowerCase,String colorspaceObject) {

        //ensure color values reset
		current.resetOnColorspaceChange();

		//set flag for stroke
		boolean isStroke = !isLowerCase;
		
		//ensure if used for both Cs and cs simultaneously we only cache one version and do not overwrite
		boolean alreadyUsed=(!isLowerCase && colorspaceObject.equals(csInUse))||(isLowerCase && colorspaceObject.equals(CSInUse));
		
		if(isLowerCase)
			csInUse=colorspaceObject;
		else
			CSInUse=colorspaceObject;


		/**
		 * work out colorspace
		 */
		PdfObject ColorSpace=(PdfObject)colorspaces.get(colorspaceObject);

        if(ColorSpace==null)
			ColorSpace=new ColorSpaceObject(((String)colorspaceObject).getBytes());

        String ref=ColorSpace.getObjectRefAsString(), ref2=ref+"-"+isLowerCase;

		GenericColorSpace newColorSpace= null;

		//(ms) 20090430 new code does not work so commented out

        //int ID=ColorSpace.getParameterConstant(PdfDictionary.ColorSpace);

//        if(isLowerCase)
//            System.out.println(" cs="+colorspaceObject+" "+alreadyUsed+" ref="+ref);
//        else
//            System.out.println(" CS="+colorspaceObject+" "+alreadyUsed+" ref="+ref);

        if(!alreadyUsed &&colorspacesObjects.containsKey(ref)){

            newColorSpace=(GenericColorSpace) colorspacesObjects.get(ref);

			//reinitialise
			newColorSpace.reset();
        }else if(alreadyUsed &&colorspacesObjects.containsKey(ref2)){

            newColorSpace=(GenericColorSpace) colorspacesObjects.get(ref2);

			//reinitialise
			newColorSpace.reset();
		}else{

		    newColorSpace=ColorspaceFactory.getColorSpaceInstance(isPrinting,currentPdfFile, ColorSpace, colorspacesObjects, null, false);
            
		    //broken on calRGB so ignore at present
		    //if(newColorSpace.getID()!=ColorSpaces.CalRGB)

           if((newColorSpace.getID()==ColorSpaces.ICC || newColorSpace.getID()==ColorSpaces.Separation)){
		   //if(newColorSpace.getID()==ColorSpaces.Separation)

               if(!alreadyUsed){
		            colorspacesObjects.put(ref, newColorSpace);
               }else
                    colorspacesObjects.put(ref2, newColorSpace);

              // System.out.println("cache "+ref +" "+isLowerCase+" "+colorspaceObject);
           }
            
        }

		//pass in pattern arrays containing all values
		if(newColorSpace.getID()==ColorSpaces.Pattern){
                     
			//at this point we only know it is Pattern so need to pass in WHOLE array
			newColorSpace.setPattern(patterns,pageH,gs.CTM);
			newColorSpace.setGS(gs);
		}

        //track colrspace use
        colorspacesUsed.put(new Integer(newColorSpace.getID()),"x");

		if(isStroke)
			strokeColorSpace=newColorSpace;
		else
			nonstrokeColorSpace=newColorSpace;


	}
	////////////////////////////////////////////////////////////////////////
	final private void V(float x3,float y3,float x2,float y2) {
		currentDrawShape.addBezierCurveV(x2, y2, x3, y3);
	}
	////////////////////////////////////////////////////////////////////////
	final private void TF(float Tfs,String fontID) {


		//set global variables to new values
		currentTextState.setFontTfs(Tfs);

		PdfFont newFont=resolveFont(fontID);

		if(newFont!=null){
			//@fontHandle currentFontData.unsetUnscaledFont();
			currentFontData=newFont;

			current.drawFontBounds(currentFontData.getBoundingBox());
		}

		//convert ID to font name and store
		currentFont = currentFontData.getFontName();
		currentTextState.setFont(currentFont,fontID);

		//compensate for odd font scaling in Type 3
		font_as_string =Fonts.createFontToken(currentFont,currentTextState.getCurrentFontSize());

	}

	/**
	 * process each token and add to text or decode
	 * if not known command, place in array (may be operand which is
	 * later used by command)
	 */
	final private int processToken(int commandID,
			byte[] characterStream,
			int startCommand,int dataPointer) throws  Exception
    {

        //reorder values so work
        if(operandCount>0){

            int[] orderedOpStart=new int[MAXOPS];
            int[] orderedOpEnd=new int[MAXOPS];
            int opid=0;
            for(int jj=this.currentOp-1;jj>-1;jj--){

                orderedOpStart[opid]=opStart[jj];
                orderedOpEnd[opid]=opEnd[jj];
                if(opid==operandCount)
                    jj=-1;
                opid++;
            }
            if(opid==operandCount){
                currentOp--; //decrease to make loop comparison faster
                for(int jj=this.MAXOPS-1;jj>currentOp;jj--){

                    orderedOpStart[opid]=opStart[jj];
                    orderedOpEnd[opid]=opEnd[jj];
                    if(opid==operandCount)
                        jj=currentOp;
                    opid++;
                }
                currentOp++;
            }

            opStart=orderedOpStart;
            opEnd=orderedOpEnd;
        }


        /**
         * call method to handle commands
         */

        /**text commands first and all other
         * commands if not found in first
         **/
        boolean notFound=true;
        if(!getSamplingOnly &&(renderText || textExtracted)) {

            notFound=false;

            switch(commandID){
                case Cmd.Tc :
                    TC(parseFloat(0,characterStream));
                    break;
                case Cmd.Tw :
                    TW(parseFloat(0,characterStream));
                    break;
                case Cmd.Tz :
                    TZ(parseFloat(0,characterStream));
                    break;
                case Cmd.TL :
                    TL(parseFloat(0,characterStream));
                    break;
                case Cmd.Tf :
                    TF(parseFloat(0,characterStream),(generateOpAsString(1,characterStream, true)));
                    break;
                case Cmd.Tr :
                    TR(parseInt(0,characterStream));
                    break;
                case Cmd.Ts :
                    TS(parseFloat(0,characterStream));
                    break;
                case Cmd.TD :
                    TD(false,parseFloat(1,characterStream),parseFloat(0,characterStream));
                    break;
                case Cmd.Td :
                    TD(true,parseFloat(1,characterStream),parseFloat(0,characterStream));
                    break;
                case Cmd.Tm :
                    //set Tm matrix
                    currentTextState.Tm[0][0] =parseFloat(5,characterStream);
                    currentTextState.Tm[0][1] =parseFloat(4,characterStream);
                    currentTextState.Tm[0][2] = 0;
                    currentTextState.Tm[1][0] =parseFloat(3,characterStream);
                    currentTextState.Tm[1][1] =parseFloat(2,characterStream);
                    currentTextState.Tm[1][2] = 0;
                    currentTextState.Tm[2][0] =parseFloat(1,characterStream);
                    currentTextState.Tm[2][1] =parseFloat(0,characterStream);
                    currentTextState.Tm[2][2] = 1;

                    //set Tm matrix
                    currentTextState.TmNoRotation[0][0] =currentTextState.Tm[0][0];
                    currentTextState.TmNoRotation[0][1] =currentTextState.Tm[0][1];
                    currentTextState.TmNoRotation[0][2] = 0;
                    currentTextState.TmNoRotation[1][0] =currentTextState.Tm[1][0];
                    currentTextState.TmNoRotation[1][1] =currentTextState.Tm[1][1];
                    currentTextState.TmNoRotation[1][2] = 0;
                    currentTextState.TmNoRotation[2][0] =currentTextState.Tm[2][0];
                    currentTextState.TmNoRotation[2][1] =currentTextState.Tm[2][1];
                    currentTextState.TmNoRotation[2][2] = 1;


                    TM();
                    break;
                case Cmd.Tstar :
                    TSTAR();
                    break;
                case Cmd.Tj :
                    TJ(characterStream, startCommand,dataPointer);
                    break;
                case Cmd.TJ :
                    TJ(characterStream, startCommand,dataPointer);
                    break;
                case Cmd.quote :
                    one_quote(characterStream,startCommand,dataPointer);
                    break;
                case Cmd.doubleQuote :
                    double_quote(characterStream,startCommand,dataPointer,parseFloat(1,characterStream),parseFloat(2,characterStream));
                    break;
                default:
                    notFound=true;
                    break;

            }
        }

        if(!getSamplingOnly && (renderPage || textColorExtracted || colorExtracted)) {

            notFound=false;

            switch(commandID){
                case Cmd.rg :
                    RG(true,characterStream);
                    break;
                case Cmd.RG :
                    RG(false,characterStream);
                    break;
                case Cmd.SCN :
                    SCN(false,characterStream);
                    break;
                case Cmd.scn :
                    SCN(true,characterStream);
                    break;
                case Cmd.SC :
                    SCN(false,characterStream);
                    break;
                case Cmd.sc :
                    SCN(true,characterStream);
                    break;
                case Cmd.cs :
                    CS(true,generateOpAsString(0,characterStream, true));
                    break;
                case Cmd.CS :
                    CS(false,generateOpAsString(0,characterStream, true));
                    break;
                case Cmd.g :
                    G(true,characterStream);
                    break;
                case Cmd.G :
                    G(false,characterStream);
                    break;
                case Cmd.k :
                    K(true,characterStream);
                    break;
                case Cmd.K :
                    K(false,characterStream);
                    break;
                case Cmd.sh:
                    sh(generateOpAsString(0,characterStream, true));
                    break;
                default:
                    notFound=true;
                    break;

            }
        }

        if(notFound){

            switch(commandID){

                case Cmd.cm :
                    //create temp Trm matrix to update Tm
                    float[][] Trm = new float[3][3];

                    //set Tm matrix
                    Trm[0][0] = parseFloat(5,characterStream);
                    Trm[0][1] = parseFloat(4,characterStream);
                    Trm[0][2] = 0;
                    Trm[1][0] = parseFloat(3,characterStream);
                    Trm[1][1] = parseFloat(2,characterStream);
                    Trm[1][2] = 0;
                    Trm[2][0] = parseFloat(1,characterStream);
                    Trm[2][1] = parseFloat(0,characterStream);
                    Trm[2][2] = 1;

                    CM(Trm);
                    break;

                case Cmd.Do :
                    DO(generateOpAsString(0,characterStream, true));
                    break;

                case Cmd.q :
                    Q(true);
                    break;
                case Cmd.Q :
                    Q(false);
                    break;

                default:
                    notFound=true;
                    break;
            }
        }

        if(notFound && !getSamplingOnly){

            /**
             * other commands here
             */
            switch (commandID) {
                case Cmd.BI:
                    startInlineStream=dataPointer;
                    break;
                case Cmd.ID :
                    dataPointer=ID(characterStream,startInlineStream, dataPointer);
                    break;
                case Cmd.B :
                    B(false,false);
                    break;
                case Cmd.b :
                    B(false,true);
                    break;
                case Cmd.bstar :
                    B(true,true);
                    break;
                case Cmd.Bstar :
                    B(true,false);
                    break;
                case Cmd.c :
                    float x3 =parseFloat(1,characterStream);
                    float y3 = parseFloat(0,characterStream);
                    float x2 =parseFloat(3,characterStream);
                    float y2 = parseFloat(2,characterStream);
                    float x = parseFloat(5,characterStream);
                    float y = parseFloat(4,characterStream);
                    currentDrawShape.addBezierCurveC(x, y, x2, y2, x3, y3);
                    break;
                case Cmd.d :
                    D(characterStream);
                    break;
                case Cmd.F :
                    F(false);
                    break;
                case Cmd.f :
                    F(false);
                    break;
                case Cmd.Fstar :
                    F(true);
                    break;
                case Cmd.fstar :
                    F(true);
                    break;
                case Cmd.h :
                    H();
                    break;
                case Cmd.l :
                    L(parseFloat(1,characterStream),parseFloat(0,characterStream));
                    break;
                case Cmd.m :
                    M(parseFloat(1,characterStream),parseFloat(0,characterStream));
                    break;
                case Cmd.n :
                    N();
                    break;
                case Cmd.S :
                    S(false);
                    break;
                case Cmd.s :
                    S(true);
                    break;
                case Cmd.v :
                    V(parseFloat(1,characterStream),parseFloat(0,characterStream),parseFloat(3,characterStream),parseFloat(2,characterStream));
                    break;
                case Cmd.Wstar :
                    W(true);
                    break;
                case Cmd.W :
                    W(false);
                    break;
                case Cmd.y :
                    Y(parseFloat(1,characterStream),parseFloat(0,characterStream),parseFloat(3,characterStream),parseFloat(2,characterStream));
                    break;
                case Cmd.re :
                    RE(parseFloat(3,characterStream),parseFloat(2,characterStream),parseFloat(1,characterStream),parseFloat(0,characterStream));
                    break;

                case Cmd.gs :
                    gs(generateOpAsString(0,characterStream, true));
                    break;
                case Cmd.i:
                    I();
                    break;
                case Cmd.J :
                    J(false,parseInt(0,characterStream));
                    break;
                case Cmd.j :
                    J(true,parseInt(0,characterStream));
                    break;

                case Cmd.MP :
                    MP();
                    break;
                case Cmd.DP :
                    DP(startCommand,  dataPointer, characterStream,generateOpAsString(0,characterStream, false));
                    break;
                case Cmd.BDC :
                    BDC(startCommand,  dataPointer, characterStream,generateOpAsString(0,characterStream, false));
                    break;
                case Cmd.BMC :
                    BMC(generateOpAsString(0,characterStream, false));
                    break;
                case Cmd.d0 :
                    d0((int) parseFloat(0,characterStream),(int) parseFloat(1,characterStream));
                    break;
                case Cmd.d1 :
                    d1(parseFloat(1,characterStream),
                            parseFloat(3,characterStream),
                            parseFloat(5,characterStream),
                            parseFloat(0,characterStream),
                            parseFloat(2,characterStream),
                            parseFloat(4,characterStream));
                    break;
                case Cmd.EMC :
                    EMC();
                    break;
                case Cmd.BT :
                    BT();
                    break;
                case Cmd.ET :
                    ET();
                    break;

                case Cmd.M:
                    mm((int) (parseFloat(0,characterStream)));
                    break;
                case Cmd.w:
                    width(parseFloat(0,characterStream));
                    break;

            }
        }

        //reset array of trailing values
        currentOp=0;
        operandCount=0;

        //increase pointer
        tokenNumber++;

        return dataPointer;

    }

	private void gs(Object key) {

		PdfObject GS= (PdfObject) GraphicsStates.get(key);

		//@newspeed
		/**new code when ready
        if(oldGS==null){
            currentGraphicsState=new GraphicsState();
        }else
            currentGraphicsState=oldGS.clone();

         /**/
		/**
		 * set gs
		 */
		gs.setMode(GS);

		/**
		 * align display
		 */
		nonStrokeAlpha=gs.getNonStrokeAlpha();
		strokeAlpha=gs.getStrokeAlpha();
		///System.out.println("-------------"+formLevel);
		//System.out.println(topLevelNonStrokeAlpha+" "+nonStrokeAlpha);
		//System.out.println(topLevelStrokeAlpha+" "+strokeAlpha);

        //modify for PlayTime
		if(!newForms){
            if(formLevel==0){

                topLevelNonStrokeAlpha=nonStrokeAlpha;
                topLevelStrokeAlpha=strokeAlpha;
            }else{

                if(topLevelNonStrokeAlpha<nonStrokeAlpha)
                    nonStrokeAlpha=topLevelNonStrokeAlpha;

                if(topLevelStrokeAlpha<strokeAlpha)
                    strokeAlpha=topLevelStrokeAlpha;
            }
        }

		current.setGraphicsState(GraphicsState.FILL,nonStrokeAlpha);
		current.setGraphicsState(GraphicsState.STROKE,strokeAlpha);

	}

	//////////////////////////////////////////////////////////////////////
	/**
	 * process form or image - we must always process XForms becasue they may contain text
	 */
	final private void DO(String name) throws  PdfException {

        //System.out.println("DO "+name);

        if(!isLayerVisible)
			return;

		/**
		 * ignore multiple overlapping images
		 */
		if(rejectSuperimposedImages){

			if(imposedImages==null)
				imposedImages=new HashMap();

			String key=((int)gs.CTM[2][0])+"-"+((int)gs.CTM[2][1])+ '-' +
			((int)gs.CTM[0][0])+ '-' +((int)gs.CTM[1][1])+ '-' +
			((int)gs.CTM[0][1])+ '-' +((int)gs.CTM[1][0]);

			if(imposedImages.get(key)==null)
				imposedImages.put(key,"x");
			else
				return ;
		}

		//generate name including filename to make it unique less /
		currentImage = this.fileName + '-' + name;

		//new version
		PdfObject XObject =(PdfObject) localXObjects.get(name);
		if(XObject==null)
			XObject=(PdfObject) globalXObjects.get(name);

        if(XObject!=null)
        currentPdfFile.checkResolved(XObject);

        try{

			if (XObject!=null) {

				int subtype=XObject.getParameterConstant(PdfDictionary.Subtype);

                //see if visible
				boolean isVisible=true;
				String layerName =XObject.getName(PdfDictionary.OC);


                if(layerName !=null && layers!=null && layers.isLayerName(layerName)){
					//System.out.println("name="+ layerName);
					isVisible=layers.isVisible(layerName);
				}

				if(!isVisible){
				}else if(subtype==PdfDictionary.Form) {

					//@newspeed - disabled - will need recoding or switching off
					/**
					if(this.xFormMetadata){

						lastFormID=name;

						//creat Map with just the values required
						Map xFormData=new HashMap();
						String[] requiredKeys={"OPI","BBox","Matrix"};

						int count=requiredKeys.length;
						for(int j=0;j<count;j++){
							Object value=currentValues.get(requiredKeys[j]);

							if(value!=null)
								xFormData.put(requiredKeys[j],value);
						}

						Map newValues=new HashMap();
						Map textFields=new HashMap();
						textFields.put("F","x");

						currentPdfFile.flattenValuesInObject(false,false,xFormData,newValues,textFields,null,objectRef);

						this.pdfImages.setXformData(lastFormID, newValues);
					}/**/

					if(!this.renderDirectly && statusBar!=null)
						statusBar.inSubroutine(true);

					//reset operand
					currentOp=0;
					operandCount=0;

					//read stream for image
					byte[] objectData=currentPdfFile.readStream(XObject,true,true,keepRaw, false,false, null);
                    if(objectData!=null)
						processXForm(XObject, objectData);

					if(!this.renderDirectly && statusBar!=null)
						statusBar.inSubroutine(false);

					lastFormID=null;

				} else if(subtype==PdfDictionary.Image) {

					/**don't process unless needed*/
					//<start-adobe>
					if (!markedContentExtracted && contentHandler!=null)
						contentHandler.setImageName(name);
					//<end-adobe>

					if(renderImages || clippedImagesExtracted || finalImagesExtracted || rawImagesExtracted){

						//read stream for image
						byte[] objectData=currentPdfFile.readStream(XObject,true,true,false, false,false, null);

						if(objectData!=null){

							boolean alreadyCached=false;//(useHiResImageForDisplay && current.isImageCached(this.pageNum));

							BufferedImage image=null;

							//reset before image processing
							optionsApplied=PDFImageProcessing.NOTHING;

                            //process the image and save raw version
							if(!alreadyCached){
								//last flag change from true to false to fix issue
								image =processImageXObject(XObject, name,createScaledVersion,objectData,true);
							}

//                            if(name.equals("X321")){
//
//                                Graphics2D gg2=image.createGraphics();
//                                gg2.setPaint(Color.RED);
//                                gg2.drawRect(0,0,image.getWidth()-1, image.getHeight()-1);
                        //        org.jpedal.gui.ShowGUIMessage.showGUIMessage("x",image,"x");
//                            }
							
							//fix for oddity in Annotation
							if(image!=null && image.getWidth()==1 && image.getHeight()==1 && isType3Font){
                                image.flush();
                                image=null;
                            }

							if(PdfDecoder.debugHiRes)
							System.out.println("final="+image);
                            //save transformed image
							if (image != null || alreadyCached){

                                //manipulate CTM to allow for image truncated
                                float[][] savedCMT=null;

//                                if(minX!=-1){
//                                    savedCMT=new float[][]{{gs.CTM[0][0],gs.CTM[0][1],gs.CTM[0][2]},
//                                    {gs.CTM[1][0],gs.CTM[1][1],gs.CTM[1][2]},
//                                    {gs.CTM[2][0],gs.CTM[2][1],gs.CTM[2][2]}};
//
//                                    if(name.equals("X321")){
//                                        System.out.println("gs.CTM[2][0]="+gs.CTM[2][0]);
//                                    }
//                                    gs.CTM[2][0]=gs.CTM[2][0]+minX;
//                                    gs.CTM[2][1]=gs.CTM[2][1]+minY;
//
//                                    if(name.equals("X321")){
//                                        System.out.println("gs.CTM[2][0]="+gs.CTM[2][0]);
//                                    }
//
//                                }
								if(renderDirectly || useHiResImageForDisplay){

									if(PdfDecoder.clipOnMac && PdfDecoder.isRunningOnMac&& !alreadyCached)
										image=clipForMac(image);

									gs.x=gs.CTM[2][0];
									gs.y=gs.CTM[2][1];	
									if(renderDirectly)
										current.renderImage(null,image,gs.getNonStrokeAlpha(),
												gs,g2, gs.x,gs.y, optionsApplied);

                                    else  if (image!=null || alreadyCached)
										current.drawImage(pageNum,image,gs,alreadyCached,name,optionsApplied);

								}else{

									if(this.clippedImagesExtracted)
										generateTransformedImage(image,name);
									else{
										try{
											generateTransformedImageSingle(image,name);
										}catch(Exception e){
											LogWriter.writeLog("Exception "+e+" on transforming image in file");
										}
									}
								}

								if(image!=null)
									image.flush();

                                //restore
                                if(savedCMT!=null){
                                    gs.CTM=savedCMT;
                                    maxX=-1; //flag as used
                                }
							}
						}
					}
				} else {
					LogWriter.writeLog("[PDF] " + " not supported");
				}
			}
		}catch(Error e){
			e.printStackTrace();
			imagesProcessedFully = false;
			addPageFailureMessage("Error "+e+" in DO with image isPrinting="+isPrinting+" useHiResImageForDisplay="+useHiResImageForDisplay);
		}catch(Exception e){
			e.printStackTrace();
			imagesProcessedFully = false;
			addPageFailureMessage("Error "+e+" in DO with image isPrinting="+isPrinting+" useHiResImageForDisplay="+useHiResImageForDisplay);
		}

	}

	/**
	 * routine to decode an XForm stream
	 */
	private void processXForm(PdfObject XObject, byte[] formData) throws  PdfException {

        String oldIndent=indent;
        indent=indent+"   ";

		float[] transformMatrix=new float[6];

		/**
		 * work through values and see if all match
		 * exit on first failure
		 */
		boolean isIdentity=true;// assume right and try to disprove

		//set value and see if Transform matrix
		float[] matrix=XObject.getFloatArray(PdfDictionary.Matrix);
		if(matrix!=null){
			transformMatrix=matrix;

			//see if it matches if not set flag and exit
			for(int ii=0;ii<6;ii++){
				if(matrix[ii]!=matches[ii]){
                    isIdentity=false;
					break;
				}
			}	
		}

		float[][] CTM=null;

		if(matrix!=null && !isIdentity) {

			//save current
			float[][] currentCTM=new float[3][3];
			for(int i=0;i<3;i++)
				System.arraycopy(gs.CTM[i], 0, currentCTM[i], 0, 3);
			scalings.put(new Integer(formLevel),currentCTM);

			CTM=gs.CTM;

			float[][] scaleFactor={{transformMatrix[0],transformMatrix[1],0},
					{transformMatrix[2],transformMatrix[3],0},
					{transformMatrix[4],transformMatrix[5],1}};

			scaleFactor=Matrix.multiply(scaleFactor,CTM);
			gs.CTM=scaleFactor;
		}

		//track depth
		formLevel++;

		//preserve colorspaces
		GenericColorSpace mainStrokeColorData=(GenericColorSpace)strokeColorSpace.clone();
		GenericColorSpace mainnonStrokeColorData=(GenericColorSpace)nonstrokeColorSpace.clone();

		//preserve GS state as could be over-written
		Map old_gs_state=GraphicsStates;
		GraphicsStates=new HashMap();
		Iterator keys=old_gs_state.keySet().iterator();
		while(keys.hasNext()){
			Object key=keys.next();
			GraphicsStates.put(key,old_gs_state.get(key));
		}

        //preserve Colorspace state as could be over-written
        Map old_ColorSpace_Objects=colorspaces;
        colorspaces=new HashMap();
        keys=old_ColorSpace_Objects.keySet().iterator();
        while(keys.hasNext()){
            Object key=keys.next();
            colorspaces.put(key,old_ColorSpace_Objects.get(key));
        }

        //preserve XObject state as could be over-written
//		Map old_globalXObjects=globalXObjects;
//		globalXObjects=new HashMap();
//		keys=old_globalXObjects.keySet().iterator();
//		while(keys.hasNext()){
//			Object key=keys.next();
//			globalXObjects.put(key,old_globalXObjects.get(key));
//		}

        Map old_localXObjects=localXObjects;
		localXObjects=new HashMap();
		keys=old_localXObjects.keySet().iterator();
		while(keys.hasNext()){
			Object key=keys.next();
			localXObjects.put(key,old_localXObjects.get(key));
		}

		//preserve fonts
		Map rawFonts=unresolvedFonts;
		Map rawDecodedFonts=resolvedFonts;
		resolvedFonts=new HashMap();
		unresolvedFonts=new HashMap();

		PdfObject mainGroup=groupObj;

        //renderer
        DynamicVectorRenderer oldCurrent=null;

		/**read any resources*/
		PdfObject Resources=XObject.getDictionary(PdfDictionary.Resources);

		/**read any resources*/
		groupObj=XObject.getDictionary(PdfDictionary.Group);
		currentPdfFile.checkResolved(groupObj);
		
		//reset fags
		if(!newForms && formLevel==0){
			topLevelStrokeAlpha=1f;
			topLevelNonStrokeAlpha=1f;

            strokeAlpha=1f;
			nonStrokeAlpha=1f;
		}

		//System.out.println("formLevel="+formLevel);

		currentPdfFile.checkResolved(Resources);
		readResources(Resources,false);

        //allow for no fonts in FormObject when we use any global
        if(unresolvedFonts.isEmpty())
            unresolvedFonts=rawFonts;

		/**
		 * see if bounding box and set
		 */
		float[] BBox=XObject.getFloatArray(PdfDictionary.BBox);
		Area clip=null;
		boolean clipChanged=false;

		//only apply if no scaling
		if(BBox!=null && matrix==null && BBox[0]==0 && BBox[1]==0){

			clip=gs.getClippingShape();

			Area newClip;

			float scaling=gs.CTM[0][0];
			if(scaling==0)
				scaling=gs.CTM[0][1];

			int x,y,w,h;

			if(gs.CTM[0][1]>0 && gs.CTM[1][0]<0){
				y=(int)(BBox[0]+gs.CTM[2][1]);
				x=(int)(BBox[1]+gs.CTM[2][0]-BBox[3]);
				h=(int)((BBox[2]-BBox[0])*scaling);
				w=(int)((BBox[3]-BBox[1])*scaling);
			}else{
				x=(int)(BBox[0]+gs.CTM[2][0]);
				y=(int)(BBox[1]+gs.CTM[2][1]);
				w=(int)(1+(BBox[2]-BBox[0])*scaling);
				h=(int)(1+(BBox[3]-BBox[1])*scaling);                
			}

//			System.out.println("co-ords="+x+" "+y+" "+w+" "+h+" scaling="+scaling+" "+gs.CTM[0][0]+" "+gs.CTM[1][1]);
//			System.out.println("BBox="+BBox[0]+" "+BBox[1]+" "+BBox[2]+" "+BBox[3]);
//			System.out.println(clip);
//			
//			if(clip!=null)
//				System.out.println(clip.getBounds());
				
			
			newClip=new Area(new Rectangle(x,y,w,h));

			// newClip=new Area(new Rectangle(105,75,(int)(BBox[3])-135,(int)(BBox[2]-125)));
			gs.updateClip(newClip);

			if(renderDirectly)
				current.renderClip(gs.getClippingShape(),null,defaultClip,g2);
			else
				current.drawClip(gs) ;

			clipChanged=true;
			//Shape currentShape=new Rectangle(105,75,(int)(BBox[3])-135,(int)(BBox[2]-125));

		}

		/**decode the stream*/
		if(formData.length>0){
            if(this.nonStrokeAlpha==1 || !newForms)
                decodeStreamIntoObjects(formData);
            else{

                streams++;
                oldCurrent=current;
                boolean oldRenderDirectly=renderDirectly;
                float mainstrokeAlpha=strokeAlpha;
                float mainnonstrokeAlpha=nonStrokeAlpha;
                strokeAlpha=1f;
                nonStrokeAlpha=1f;
                renderDirectly=false;

                current=new DynamicVectorRenderer(pageNum,objectStoreStreamRef,false);
                current.setHiResImageForDisplayMode(this.useHiResImageForDisplay);

                decodeStreamIntoObjects(formData);

                if(oldRenderDirectly)
                    oldCurrent.renderXForm(g2, current, mainnonstrokeAlpha);
                else
                    oldCurrent.drawXForm(current);

                //restore
                streams--;
                current=oldCurrent;
                nonStrokeAlpha=mainnonstrokeAlpha;
                strokeAlpha=mainstrokeAlpha;
                renderDirectly=oldRenderDirectly;
            }

        }
		//restore clip if changed
		if(clipChanged){
			gs.setClippingShape(clip);
			if(renderDirectly)
				current.renderClip(gs.getClippingShape(),null,defaultClip,g2);
			else
				current.drawClip(gs) ;
		}

		formLevel--;

		//restore old matrix
		CTM=(float[][]) scalings.get(new Integer(formLevel));
		if(CTM!=null)
			gs.CTM=CTM;

		//flush local refs if duplicates
		//if(formLevel==0)
		//	localXObjects.clear();

		/**restore old colorspace and fonts*/
		strokeColorSpace=mainStrokeColorData;
		nonstrokeColorSpace=mainnonStrokeColorData;
		unresolvedFonts=rawFonts;
		resolvedFonts=rawDecodedFonts;
		GraphicsStates=old_gs_state;
        colorspaces=old_ColorSpace_Objects;

        //globalXObjects=old_globalXObjects;
        localXObjects=old_localXObjects;
		groupObj=mainGroup;

        indent=oldIndent;
	}
	////////////////////////////////////////////////////////
	/**
	 * save the current image, clipping and resizing. Id reparse, we don't
	 * need to repeat some actions we know already done.
	 */
	final private void generateTransformedImageSingle(BufferedImage image,String image_name) {

		LogWriter.writeMethod("{generateTransformedImageSingle}", 0);

		float x = 0, y = 0, w = 0, h = 0;

		//if valid image then process
		if (image != null) {

			// get clipped image and co-ords
			Area clipping_shape = gs.getClippingShape();

			/**
			 * scale the raw image to correct page size (at 72dpi)
			 */
			//object to scale and clip. Creating instance does the scaling
			ImageTransformer image_transformation;

			//object to scale and clip. Creating instance does the scaling
			image_transformation =new ImageTransformer(PdfDecoder.dpi,gs,image,true,PdfDecoder.isDraft);

			//get initial values
			x = image_transformation.getImageX();
			y = image_transformation.getImageY();
			w = image_transformation.getImageW();
			h = image_transformation.getImageH();

			//get back image, which will become null if TOO small
			image = image_transformation.getImage();

			//apply clip as well if exists and not inline image
			if (image != null && customImageHandler!=null && clipping_shape != null && clipping_shape.getBounds().getWidth()>1 &&
                    clipping_shape.getBounds().getHeight()>1 && !customImageHandler.imageHasBeenScaled()) {

				//see if clip is wider than image and ignore if so
				boolean ignore_image = clipping_shape.contains(x, y, w, h);

				if (ignore_image == false) {
					//do the clipping
					image_transformation.clipImage(clipping_shape);

					//get ALTERED values
					x = image_transformation.getImageX();
					y = image_transformation.getImageY();
					w = image_transformation.getImageW();
					h = image_transformation.getImageH();
				}
			}

			//alter image to allow for way we draw 'upside down'
			image = image_transformation.getImage();

			image_transformation = null; //flush

			//allow for null image returned (ie if too small)
			if (image != null) {

				/**turn correct way round if needed*/
				//if((currentGraphicsState.CTM[0][1]!=0 )&&(currentGraphicsState.CTM[1][0]!=0 )&&(currentGraphicsState.CTM[0][0]>=0 )){

				/*if((currentGraphicsState.CTM[0][1]>0 )&&(currentGraphicsState.CTM[1][0]>0 )&&(currentGraphicsState.CTM[0][0]>=0 )){
                        double dx=1,dy=1,scaleX=0,scaleY=0;

                        if(currentGraphicsState.CTM[0][1]>0){
                            dx=-1;
                            scaleX=image.getWidth();
                        }
                        if(currentGraphicsState.CTM[1][0]>0){
                            dy=-1;
                            scaleY=image.getHeight();
                        }

                        AffineTransform image_at =new AffineTransform();
                        image_at.scale(dx,dy);
                        image_at.translate(-scaleX,-scaleY);
                        AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);
                        image = invert.filter(image,null);


                    }
				 */
				//store  final image on disk & in memory
				if(finalImagesExtracted || rawImagesExtracted){
					pdfImages.setImageInfo(currentImage, pageNum, x, y, w, h,lastFormID);

					if(includeImagesInData){

						float xx=x;
						float yy=y;

						if(clipping_shape!=null){

							int minX=(int)clipping_shape.getBounds().getMinX();
							int maxX=(int)clipping_shape.getBounds().getMaxX();

							int minY=(int)clipping_shape.getBounds().getMinY();
							int maxY=(int)clipping_shape.getBounds().getMaxY();

							if((xx>0 && xx<minX)||(xx<0))
								xx=minX;

							float currentW=xx+w;
							if(xx<0)
								currentW=w;
							if(maxX<(currentW))
								w=maxX-xx;

							if(yy>0 && yy<minY)
								yy=minY;

							if(maxY<(yy+h))
								h=maxY-yy;

						}

						pdfData.addImageElement(xx,yy,w,h,currentImage);
					}
				}
				//add to screen being drawn
				if (renderImages || !isPageContent) {

					//check it is not null
					if (image != null) {
						gs.x=x;
						gs.y=y;				
						if(renderDirectly)
							current.renderImage(null,image,gs.getNonStrokeAlpha(),
									gs,g2,gs.x,gs.y, optionsApplied);
						else
							current.drawImage(pageNum,image,gs,false,image_name, optionsApplied);

					}
				}

				/**save if required*/
				if(isPageContent && finalImagesExtracted) {


					if (PdfDecoder.inDemo) {
						int cw = image.getWidth();
						int ch = image.getHeight();

						Graphics2D g2 = image.createGraphics();
						g2.setColor(Color.red);
						g2.drawLine(0, 0, cw, ch);
						g2.drawLine(0, ch, cw, 0);
					}

					//save the scaled/clipped version of image if allowed
					if(currentPdfFile.isExtractionAllowed() && !PdfStreamDecoder.runningStoryPad){

						String image_type = objectStoreStreamRef.getImageType(currentImage);
						objectStoreStreamRef.saveStoredImage(
								currentImage,
								addBackgroundToMask(image),
								false,
								false,
								image_type);
					}
				}
			}
		} else
			//flag no image and reset clip
			LogWriter.writeLog("NO image written");


	}

	private BufferedImage addBackgroundToMask(BufferedImage image) {
		if(isMask){

			int cw = image.getWidth();
			int ch = image.getHeight();

			BufferedImage background=new BufferedImage(cw,ch,BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = background.createGraphics();
			g2.setColor(Color.white);
			g2.fillRect(0, 0, cw, ch);
			g2.drawImage(image,0,0,null);
			image=background;

		}
		return image;
	}


	/**
	 * pass in status bar object
	 *
	 */
	public void setStatusBar(StatusBar statusBar){
		this.statusBar=statusBar;
	}

	/**
	 * clip image as MAC has nasty bug :-(
	 */
	final private BufferedImage clipForMac(BufferedImage image) {

		LogWriter.writeMethod("{clipForMac}", 0);

		//if valid image then process
		if ((image != null)) {

			/**
			 * scale the raw image to correct page size (at 72dpi)
			 */

			//object to scale and clip. Creating instance does the scaling
			ImageTransformerDouble image_transformation =new ImageTransformerDouble(PdfDecoder.dpi,gs,image,createScaledVersion,false);

			//extract images either scaled/clipped or scaled then clipped

			image_transformation.doubleScaleTransformShear(true);

			//get intermediat eimage and save
			image = image_transformation.getImage();

		}
		return image;
	}


	/**
	 * save the current image, clipping and
	 *  resizing. This gives us a
	 * clipped hires copy. In reparse, we don't
	 * need to repeat some actions we know already done.
	 */
	final private void generateTransformedImage(BufferedImage image,String image_name) {

		LogWriter.writeMethod("{generateTransformedImage}", 0);

		float x = 0, y = 0, w = 0, h = 0;

		//if valid image then process
		if ((image != null)) {

			/**
			 * scale the raw image to correct page size (at 72dpi)
			 */

			//object to scale and clip. Creating instance does the scaling
			ImageTransformerDouble image_transformation =new ImageTransformerDouble(PdfDecoder.dpi,gs,image,createScaledVersion,true);

			//extract images either scaled/clipped or scaled then clipped

			image_transformation.doubleScaleTransformShear(false);

			//get intermediate image and save
			image = image_transformation.getImage();

			//save the scaled/clipped version of image if allowed
			{//if(currentPdfFile.isExtractionAllowed()){

				/**make sure the right way*/
				/*
				int dx=1,dy=1,iw=0,ih=0;
				if(currentGraphicsState.CTM[0][0]<0){
					dx=-dx;
					iw=image.getWidth();
				}

				if(currentGraphicsState.CTM[1][1]<0){
					dy=-dy;
					ih=image.getHeight();
				}
				if((dy<0)|(dx<0)){

					AffineTransform image_at =new AffineTransform();
					image_at.scale(dx,dy);
					image_at.translate(-iw,-ih);
					AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);
					image = invert.filter(image,null);

				}

				 */

				String image_type = objectStoreStreamRef.getImageType(currentImage);
				if(image_type==null)
					image_type="tif";

				if (PdfDecoder.inDemo) {
					Graphics2D g2 = image.createGraphics();
					g2.setColor(Color.red);
					int cw = image.getWidth();
					int ch = image.getHeight();
					g2.drawLine(0, 0, cw, ch);
					g2.drawLine(0, ch, cw, 0);
				}

				if(objectStoreStreamRef.saveStoredImage(
						"CLIP_"+currentImage,
						addBackgroundToMask(image),
						false,
						false,
						image_type))
					addPageFailureMessage("Problem saving "+image);

			}

			if((finalImagesExtracted)|(renderImages))
				image_transformation.doubleScaleTransformScale();

			//complete the image and workout co-ordinates
			image_transformation.completeImage();

			//get initial values
			x = image_transformation.getImageX();
			y = image_transformation.getImageY();
			w = image_transformation.getImageW();
			h = image_transformation.getImageH();

			//get final image to allow for way we draw 'upside down'
			image = image_transformation.getImage();

			image_transformation = null; //flush

			//allow for null image returned (ie if too small)
			if (image != null) {

				//store  final image on disk & in memory
				if((finalImagesExtracted)| (clippedImagesExtracted)|(rawImagesExtracted)){
					pdfImages.setImageInfo(currentImage, pageNum, x, y, w, h,lastFormID);

					if(includeImagesInData)
						pdfData.addImageElement(x,y,w,h,currentImage);

				}

				//add to screen being drawn
				if ((renderImages) || (!isPageContent)) {

					//check it is not null
					if (image != null) {
						gs.x=x;
						gs.y=y;
						if(renderDirectly)
							current.renderImage(null,image,gs.getNonStrokeAlpha(),
									gs,g2,gs.x,gs.y, optionsApplied);
						else
							current.drawImage(pageNum,image,gs,false,image_name,optionsApplied);

					}
				}

				/**used to debug code by popping up window after glyph*
                 Object[] options = { "OK" };
                 int n =JOptionPane.showOptionDialog(null,null,"Storypad",JOptionPane.OK_OPTION,JOptionPane.INFORMATION_MESSAGE,null,options,options[0]);
                 /***/

				/**save if required*/
				if( (!renderDirectly)&&(isPageContent)&& (finalImagesExtracted)) {

					if (PdfDecoder.inDemo) {
						Graphics2D g2 = image.createGraphics();
						g2.setColor(Color.red);
						int cw = image.getWidth();
						int ch = image.getHeight();
						g2.drawLine(0, 0, cw, ch);
						g2.drawLine(0, ch, cw, 0);
					}

					//save the scaled/clipped version of image if allowed
					if(currentPdfFile.isExtractionAllowed()){
						String image_type = objectStoreStreamRef.getImageType(currentImage);
						objectStoreStreamRef.saveStoredImage(
								currentImage,
								addBackgroundToMask(image),
								false,
								false,
								image_type);
					}
				}

			}
		} else
			//flag no image and reset clip
			LogWriter.writeLog("NO image written");

	}

	/**
	 * turn TJ into string and plot. THis routine is long but requently called so we want all code 'inlined'
	 */
	final private StringBuffer processTextArray(byte[] stream,int startCommand,int dataPointer) {

		//flag text found as opposed to just spacing
		boolean hasContent=false;

		boolean isMultiple=false;

		boolean firstTime=true;

		//roll on at start if necessary
		while((stream[startCommand]==91)||(stream[startCommand]==10)||(stream[startCommand]==13)||(stream[startCommand]==32)){

			if(stream[startCommand]==91)
				isMultiple=true;

			startCommand++;
		}

		//set threshold - value indicates several possible values
		float currentThreshold=PdfStreamDecoder.currentThreshold;
		if(currentThreshold<0){

			Float specificSetting=(Float)PdfStreamDecoder.currentThresholdValues.get(currentFontData.getFontName());

			if(specificSetting==null) //use default
				currentThreshold=-currentThreshold;
			else //use specific
				currentThreshold=specificSetting.floatValue();

		}

		/**reset global variables and initialise local ones*/
		textLength = 0;
		int Tmode=gs.getTextRenderType();
		//int foreground =((Color)nonstrokeColorSpace.getColor()).getRGB();
		int orientation=0; //show if horizontal or vertical text and running which way using constants in PdfData
		boolean isHorizontal=true,inText = false;
		float[][] TrmWithRotationRemoved = new float[3][3]; //needed by Storypad to turn

		float[][] Trm = new float[3][3];
		float[][] temp = new float[3][3];
		float[][] TrmBeforeSpace = new float[3][3];
		float[][] TrmBeforeSpaceWithRotationRemoved = new float[3][3];
		char rawChar = ' ', nextChar, lastChar = ' ', openChar = ' ', lastTextChar = 'x';
		int fontSize = 0, rawInt = 0;
		float width = 0,fontScale = 1,lastWidth = 0,currentWidth = 0,leading = 0;
		String displayValue = "";
		float TFS = currentTextState.getTfs();
		float rawTFS=TFS;

		if(TFS<0)
			TFS=-TFS;

		int type=currentFontData.getFontType();

		float spaceWidth = currentFontData.getCurrentFontSpaceWidth();
		String unicodeValue="";
		textData = new StringBuffer(50); //used to return a value

		float currentGap=0;

		boolean isCID = currentFontData.isCIDFont();

		//flag to show text highlight needs to be shifted up to allow for displacement in Trm
		boolean isTextShifted=false;

		/**set colors*/
		if(renderText && Tmode!=GraphicsState.INVISIBLE){
			gs.setStrokeColor(strokeColorSpace.getColor());
			gs.setNonstrokeColor(nonstrokeColorSpace.getColor());

			if(showTextAsRotated && currentRotation!=0){
				gs.setStrokeColor(new PdfColor(0,0,255));
				gs.setNonstrokeColor(new PdfColor(0,0,255));
			}
		}

		/**set character size */
		int charSize=2;
		if(isCID)
			charSize=4;

		/** create temp matrix for current text location and factor in scaling*/
		Trm = Matrix.multiply(currentTextState.Tm, gs.CTM);
		
		if(currentRotation!=0)
			TrmWithRotationRemoved = Matrix.multiply(currentTextState.TmNoRotation, gs.CTM);

		//adjust for negative TFS
		if(rawTFS<0){
			Trm[2][0]=Trm[2][0]-(Trm[0][0]/2);
			Trm[2][1]=Trm[2][1]-(Trm[1][1]/2);

			if(currentRotation!=0){
				TrmWithRotationRemoved[2][0]=TrmWithRotationRemoved[2][0]-(TrmWithRotationRemoved[0][0]/2);
				TrmWithRotationRemoved[2][1]= Trm[2][1]-(TrmWithRotationRemoved[1][1]/2);
			}
		}

		charSpacing = currentTextState.getCharacterSpacing() / TFS;
		float wordSpacing = currentTextState.getWordSpacing() / TFS;
		
		if(multipleTJs){ //allow for consecutive TJ commands
			Trm[2][0]=currentTextState.Tm[2][0];
			Trm[2][1]=currentTextState.Tm[2][1];


			if(currentRotation!=0){
				TrmWithRotationRemoved[2][0]=currentTextState.TmNoRotation[2][0];
				TrmWithRotationRemoved[2][1]=currentTextState.TmNoRotation[2][1];
			}
		}

		/**define matrix used for converting to correctly scaled matrix and multiply to set Trm*/
		temp[0][0] = rawTFS * currentTextState.getHorizontalScaling();
		temp[1][1] = rawTFS;
		temp[2][1] = currentTextState.getTextRise();
		temp[2][2] =1;
		Trm = Matrix.multiply(temp, Trm);

		if(currentRotation!=0)
			TrmWithRotationRemoved = Matrix.multiply(temp, TrmWithRotationRemoved);

		//check for leading before text
		if(isMultiple && stream[startCommand]!=60 && stream[startCommand]!=40 && stream[startCommand]!=93){

			float offset=0;
			while((stream[startCommand]!=40)){
				StringBuffer kerning=new StringBuffer();
				while((stream[startCommand]!=40)&&(stream[startCommand]!=32)){
					kerning.append((char)stream[startCommand]);
					startCommand++;
				}
				offset=offset+Float.parseFloat(kerning.toString());

				while(stream[startCommand]==32)
					startCommand++;
			}
			offset=Trm[0][0]*offset/THOUSAND;

			Trm[2][0]=Trm[2][0]-offset;

			if(currentRotation!=0)
				TrmWithRotationRemoved[2][0]=TrmWithRotationRemoved[2][0]-offset;

		}


		multipleTJs=true; //flag will be reset by Td/Tj/T* if move takes place.

		/**workout if horizontal or vertical plot and set values*/
		if (Trm[1][1] != 0) {
			isHorizontal=true;
			orientation = PdfData.HORIZONTAL_LEFT_TO_RIGHT;

			fontSize = Math.round(Trm[1][1] );
			if(fontSize==0)
				fontSize = Math.round(Trm[0][1] );

			fontScale = Trm[0][0];

		} else {

			isHorizontal=false;
			fontSize = Math.round(Trm[1][0] );

			if(fontSize==0)
				fontSize = Math.round(Trm[0][0] );

			if(fontSize<0){
				fontSize=-fontSize;
				orientation = PdfData.VERTICAL_BOTTOM_TO_TOP;
			}else
				orientation = PdfData.VERTICAL_TOP_TO_BOTTOM;
			fontScale = Trm[0][1];
		}
		
		//Set text orientation state
		currentTextState.writingMode = orientation;
		
		if(fontSize==0)
			fontSize=1;

		/**
		 * text printing mode to get around problems with PCL printers
		 */
		Font javaFont=null;

        if(isPrinting && textPrint==PdfDecoder.STANDARDTEXTSTRINGPRINT && StandardFonts.isStandardFont(currentFontData.getFontName(),true)){

            javaFont=currentFontData.getJavaFontX(fontSize);

        }else if(currentFontData.isFontSubstituted() || currentFontData.isFontEmbedded){
			javaFont=null;
		}else if((useTextPrintingForNonEmbeddedFonts || textPrint!=PdfDecoder.NOTEXTPRINT)&& isPrinting)
			javaFont=currentFontData.getJavaFontX(fontSize);

		float x,y;
		/**extract starting x and y values (we update Trm as we work through text)*/
		if(currentRotation==0){
			x= Trm[2][0];
			y= Trm[2][1];
		}else{
			x = TrmWithRotationRemoved[2][0];
			y = TrmWithRotationRemoved[2][1];
		}

		//track text needs to be moved up in highlight
		if(Trm[1][0]<0 && Trm[0][1]>0 && Trm[1][1]==0 && Trm[0][0]==0)
			isTextShifted=true;

		/**set max height for CID of guess sensble figure for non-CID*/
		float max_height = fontSize ;

		//fix for Type3 and fontSize not always good guide
		if(type==StandardFonts.TYPE3 && fontSize>10)
			max_height=10;

		if (isCID)
			max_height = Trm[1][1];

		/**now work through all glyphs and render/decode*/
		int i = startCommand;

		int numOfPrefixes=0;
		while (i < dataPointer) {

			//extract the next binary index value and convert to char, losing any returns
			while(true){
				if(lastChar==92 && rawChar==92)//checks if \ has been escaped in '\\'=92
					lastChar=120;
				else
					lastChar = rawChar;

				rawInt = stream[i];
				if (rawInt < 0)
					rawInt = 256 + rawInt;
				rawChar = (char) rawInt;

				//eliminate escaped tabs and returns
				if((rawChar==92)&&(stream[i+1]==13 || stream[i+1]==10)){ // '\\'=92
					i++;
					rawInt = stream[i];
					if (rawInt < 0)
						rawInt = 256 + rawInt;
					rawChar = (char) rawInt;
				}

				//stop any returns in data stream getting through (happens in ghostscript)
				if(rawChar!=10 && rawChar!=13)
					break;

				i++;
			}

			/**flag if we have entered/exited text block*/
			if (inText) {
				//non CID deliminator (allow for escaped deliminator)
				if (lastChar != 92 && (rawChar==40 || rawChar==41)) {  // '\\'=92 ')'=41
					if(rawChar==40){
						numOfPrefixes++;
					}else if(rawChar==41){ //')'=41
						if(numOfPrefixes<=0){
							inText = false; //unset text flag
						}else {
							numOfPrefixes--;
						}
					}
				} else if (openChar == 60 && rawChar == 62)  // ie <01>tj  '<'=60 '<'=62
					inText = false; //unset text flag
			}

			/**either handle glyph, process leading or handle a deliminator*/
			if (inText) { //process if still in text

				lastTextChar = rawChar; //remember last char so we can avoid a rollon at end if its a space

				//convert escape or turn index into correct glyph allow for stream
				if (openChar == 60) { //'<'=60

					//get the hex value
					StringBuffer hexString = new StringBuffer(4);
					hexString.append(rawChar);

					for (int i2 = 1; i2 < charSize; i2++) {
						int nextInt = stream[i + i2];

						if(nextInt==62){ //allow for less than 4 chars at end of stream (ie 6c>)
							i2=4;
							charSize=2;
						}else if(nextInt==10 || nextInt==13){ //avoid any returns
							i++;
							i2--;
						}else{
							if (nextInt < 0)
								nextInt = 256 + nextInt;
							rawChar = (char) nextInt;
							hexString.append(rawChar);
						}
					}

					i = i + charSize-1; //move offset

					//convert to value
					rawInt = Integer.parseInt(hexString.toString(), 16);

					rawChar = (char) rawInt;
					displayValue =currentFontData.getGlyphValue(rawInt);

					if(textExtracted)
						unicodeValue =currentFontData.getUnicodeValue(displayValue,rawInt);

				} else if (isCID && currentFontData.isDoubleByte()){  //could be nonCID cid

					if(rawChar==92){ // '\\'=92

						//extract the next binary index value and convert to char, losing any returns
						while(true){
							//lastChar = rawChar;
							rawInt = stream[i];
							if (rawInt < 0)
								rawInt = 256 + rawInt;
							rawChar = (char) rawInt;

							if (rawInt < 0)
								rawInt = 256 + rawInt;
							rawChar = (char) rawInt;
							//handle escaped chars
							if(rawInt==92){
								i++;
								rawInt = stream[i];
								rawChar=(char)rawInt;

								if(rawChar=='n'){
									rawInt='\n';
								}else if(rawChar=='b'){
									rawInt='\b';
								}else if(rawChar=='t'){
									rawInt='\t';
								}else if(rawChar=='r'){
									rawInt='\r';
								}else if(rawChar=='f'){
									rawInt='\f';
								}  else if ((stream.length > (i + 2))&& (Character.isDigit((char) stream[i]))){

									//see how long number is
									int numberCount=1;
									if(Character.isDigit((char) stream[i + 1])){
										numberCount++;
										if(Character.isDigit((char) stream[i + 2]))
											numberCount++;
									}

									// convert octal escapes
									rawInt = readEscapeValue(i, numberCount, 8, stream);
									i = i + numberCount-1;

								}
							}

							//eliminate escaped tabs and returns
							if(rawChar!=10 && rawChar!=13)
								break;

							i++;
						}
					}

					{

						i++;
						//extract the next binary index value and convert to char, losing any returns
						int nextInt = stream[i];

						if (nextInt < 0)
							nextInt = 256 + nextInt;

						//handle escaped chars
						if(nextInt==92){
							i++;
							nextInt = stream[i];
							rawChar=(char)nextInt;
							if(rawChar=='n'){
								nextInt='\n';
							}else if(rawChar=='b'){
								nextInt='\b';
							}else if(rawChar=='t'){
								nextInt='\t';
							}else if(rawChar=='r'){
								nextInt='\r';
							}else if(rawChar=='f'){
								nextInt='\f';
							}  else if ((stream.length > (i + 2))&& (Character.isDigit((char) stream[i]))){

								//see how long number is
								int numberCount=1;
								if(Character.isDigit((char) stream[i + 1])){
									numberCount++;
									if(Character.isDigit((char) stream[i + 2]))
										numberCount++;
								}

								// convert octal escapes
								nextInt = readEscapeValue(i, numberCount, 8, stream);
								i = i + numberCount-1;

							}
						}

						rawInt=(rawInt*256)+nextInt;
					}

					rawChar = (char) rawInt;

					displayValue = String.valueOf(rawChar);
					unicodeValue =currentFontData.getUnicodeValue(displayValue,rawInt);

					//fix for \\) at end of stream
					if(rawChar==92)
						rawChar=120;

				}else if (rawChar == 92) { // any escape chars '\\'=92

					i++;
					lastChar=rawChar;//update last char as escape
					rawInt = stream[i];
					rawChar = (char) rawInt;

					if ((stream.length > (i + 2))&& (Character.isDigit((char) stream[i]))){

						//see how long number is
						int numberCount=1;
						if(Character.isDigit((char) stream[i + 1])){
							numberCount++;
							if(Character.isDigit((char) stream[i + 2]))
								numberCount++;
						}

						// convert octal escapes
						rawInt = readEscapeValue(i, numberCount, 8, stream);
						i = i + numberCount-1;

						if(rawInt>255)
							rawInt=rawInt-256;

						displayValue=currentFontData.getGlyphValue(rawInt);

						if(textExtracted)
							unicodeValue=currentFontData.getUnicodeValue(displayValue,rawInt);

						rawChar =(char)rawInt; //set to dummy value as may be / value

						//allow for \134 (ie \\)
						if(rawChar==92) // '\\'=92
							rawChar=120;

					} else {

						rawInt = stream[i];
						rawChar = (char) rawInt;

						if (rawChar == 'u') { //convert unicode of format uxxxx to char value
							rawInt =readEscapeValue(i + 1, 4, 16, stream);
							i = i + 4;
							//rawChar = (char) rawInt;
							displayValue =currentFontData.getGlyphValue(rawInt);
							if(textExtracted)
								unicodeValue =currentFontData.getUnicodeValue(displayValue,rawInt);

						} else {

							if(rawChar=='n'){
								rawInt='\n';
								rawChar='\n';
							}else if(rawChar=='b'){
								rawInt='\b';
								rawChar='\b';
							}else if(rawChar=='t'){
								rawInt='\t';
								rawChar='\t';
							}else if(rawChar=='r'){
								rawInt='\r';
								rawChar='\r';
							}else if(rawChar=='f'){
								rawInt='\f';
								rawChar='\f';
							}

							displayValue =currentFontData.getGlyphValue(rawInt);

                            if(textExtracted)
								unicodeValue =currentFontData.getUnicodeValue(displayValue,rawInt);
							if (displayValue.length() > 0) //set raw char
								rawChar = displayValue.charAt(0);
						}
					}

                } else if (isCID){  //could be nonCID cid
					displayValue = String.valueOf(rawChar);
					unicodeValue=displayValue;

				}else{

                    displayValue =currentFontData.getGlyphValue(rawInt);

                    if(textExtracted)
						unicodeValue =currentFontData.getUnicodeValue(displayValue,rawInt);
				}

                //Itext likes to use Tabs!
                if(currentFontData.isFontSubstituted() && rawInt==9){
                   rawInt=32;
                   displayValue=" ";
                   unicodeValue=" ";
                }

				//MOVE pointer to next location by updating matrix
				temp[0][0] = 1;
				temp[0][1] = 0;
				temp[0][2] = 0;
				temp[1][0] = 0;
				temp[1][1] = 1;
				temp[1][2] = 0;
				temp[2][0] = (currentWidth + leading); //tx;
				temp[2][1] = 0; //ty;
				temp[2][2] = 1;
				Trm = Matrix.multiply(temp, Trm); //multiply to get new Tm

				if(currentRotation!=0)
					TrmWithRotationRemoved = Matrix.multiply(temp, TrmWithRotationRemoved); //multiply to get new Tm

				/**save pointer in case its just multiple spaces at end*/
				if (rawChar == ' ' && lastChar != ' '){
					TrmBeforeSpace = Trm;

					if(currentRotation!=0)
						TrmBeforeSpaceWithRotationRemoved = TrmWithRotationRemoved;
				}

				leading = 0; //reset leading

				PdfJavaGlyphs glyphs =currentFontData.getGlyphData();

				int idx=rawInt;
				if(currentFontData.isCIDFont() && !glyphs.isIdentity()){
					int mappedIdx=glyphs.getConvertedGlyph(rawInt);

					if(mappedIdx!=-1){

						//System.out.println("mappedIdx="+mappedIdx+" idx="+idx);

						idx=mappedIdx;

						//displayValue=""+(char)rawInt;
						//unicodeValue=displayValue;
					}
				}

				currentWidth = currentFontData.getWidth(idx);
				
//				if( Trm[2][1]>744 && Trm[2][0]<58)
//					System.out.println(currentWidth+"=========="+" "+rawInt+" d="+displayValue+"< uni="+unicodeValue+"< "+currentFontData+" "+currentFontData.getFontName());
//                else
//                    return null;
				
                /**if we have a valid character and we are rendering, draw it */

				if (renderText && Tmode!=GraphicsState.INVISIBLE){

					//added to debug rotated text
					float[][] displayTrm=Trm;
					if(showTextAsRotated && currentRotation!=0)
						displayTrm=TrmWithRotationRemoved;

					if(isPrinting && javaFont!=null && (textPrint==PdfDecoder.STANDARDTEXTSTRINGPRINT ||
                            (textPrint==PdfDecoder.TEXTSTRINGPRINT || (useTextPrintingForNonEmbeddedFonts  &&
                                    (!currentFontData.isFontEmbedded || currentFontData.isFontSubstituted()))))){

						/**support for TR7*/
						if(Tmode==GraphicsState.CLIPTEXT){

							/**set values used if rendering as well*/
							boolean isSTD=PdfDecoder.isRunningOnMac ||StandardFonts.isStandardFont(currentFontData.getBaseFontName(),false);
							Area transformedGlyph2= glyphs.getStandardGlyph(displayTrm, rawInt, displayValue, currentWidth, isSTD);

							if(transformedGlyph2!=null){
								gs.addClip(transformedGlyph2);
								//current.drawClip(gs) ;
							}

                            if(renderDirectly)
                                current.renderClip(gs.getClippingShape(), null,null,g2);
                            else
                                current.drawClip(gs);

						}
                                       
						if(displayValue!=null && !displayValue.startsWith("&#"))
							current.drawText(Trm,displayValue,gs,displayTrm[2][0],-displayTrm[2][1],javaFont);
                
					}else if(((textPrint!=PdfDecoder.TEXTGLYPHPRINT)||(javaFont==null))&&(currentFontData.isFontEmbedded &&
							currentFontData.isFontSubstituted() &&(rawInt==9 || rawInt==10 || rawInt==13))){ //&&
						//lose returns which can cause odd display
					}else if(((textPrint!=PdfDecoder.TEXTGLYPHPRINT)||(javaFont==null))&&(currentFontData.isFontEmbedded)){ //&&
						//(!currentFontData.isFontSubstituted() || !displayValue.startsWith("&#"))){

						//get glyph if not CID
						String charGlyph="notdef";

						try{

							if(!currentFontData.isCIDFont())
								charGlyph=currentFontData.getMappedChar(rawInt,false);

							PdfGlyph glyph= null;

								glyph= glyphs.getEmbeddedGlyph( factory,charGlyph ,displayTrm, rawInt, displayValue, currentWidth,currentFontData.getEmbeddedChar(rawInt));


							//avoid null type 3 glyphs and set color if needed
							if(type==StandardFonts.TYPE3){

								if(glyph!=null && glyph.getmaxWidth()==0)
									glyph=null;
								else if(glyph!=null && glyph.ignoreColors()){

									glyph.lockColors(gs.getNonstrokeColor(),gs.getNonstrokeColor());
								}
							}

							if(glyph!=null){

								float[][] finalTrm={{displayTrm[0][0],displayTrm[0][1],0},
										{displayTrm[1][0],displayTrm[1][1] ,0},
										{displayTrm[2][0],displayTrm[2][1],1}};

								float[][] finalScale={{(float) currentFontData.FontMatrix[0],(float)currentFontData.FontMatrix[1],0},
										{(float) currentFontData.FontMatrix[2],(float) currentFontData.FontMatrix[3],0},
										{0,0,1}};

								//factor in fontmatrix (which may include italic)
								finalTrm=Matrix.multiply(finalTrm, finalScale);

								finalTrm[2][0]=displayTrm[2][0];
								finalTrm[2][1]=displayTrm[2][1];

								//manipulate matrix to get right rotation
								if(finalTrm[1][0]<0 && finalTrm[0][1]<0){
									finalTrm[1][0]=-finalTrm[1][0];
									finalTrm[0][1]=-finalTrm[0][1];
								}

								if(type==StandardFonts.TYPE3){

									float h=0;
									if(finalTrm[1][1]!=0)
										h=(fontSize*finalTrm[1][1]);
									else if(finalTrm[0][0]!=0)
										h=(fontSize*finalTrm[0][0]);
									else if(finalTrm[1][0]!=0)
										h=(fontSize*finalTrm[1][0]);

									if(h<0)
										h=-h;

									if(h>max_height)
										max_height=h;

								}

								//line width
								float lineWidth=0;//currentGraphicsState.getLineWidth();
								//if(Trm[0][0]!=0)
								//	lineWidth=lineWidth*Trm[0][0];
								//else if( Trm[0][1]!=0)
								//	lineWidth=lineWidth*Trm[0][1];
								//if(lineWidth<0)
								//	lineWidth=-lineWidth;

								if(lineWidth>0){

									//System.out.println(currentFontData.getBaseFontName());
									//System.out.println(displayValue+"------------------"+Trm[0][0]+" "+Trm[0][1]+" "+Trm[1][1]+" "+" width="+currentGraphicsState.getLineWidth()+" type="+currentGraphicsState.getTextRenderType());

//									if(((int)Trm[0][0])==57){

//									
//									lineWidth=104;
//									}else if(((int)Trm[0][0])==86){
//									lineWidth=115;
//									}else if(((int)Trm[0][0])==32){
//									lineWidth=215;
//									}else if(((int)Trm[0][0])==19){
//									lineWidth=104;
//									}else{
//									lineWidth=0;
//									}

//									if(lineWidth>0){
//									Matrix.show(currentGraphicsState.CTM);
//									System.out.println(lineWidth+" "+displayValue+"------------------"+Trm[0][0]+ ' ' +Trm[0][1]+ ' ' +Trm[1][0]+ ' ' +Trm[1][1]+ ' ' +" width="+currentGraphicsState.getLineWidth()+" type="+currentGraphicsState.getTextRenderType()
//									+ ' ' +currentFontData.FontMatrix[0]+ ' ' +currentFontData.FontMatrix[1]+ ' ' +currentFontData.FontMatrix[2]+ ' ' +currentFontData.FontMatrix[3]);
//									}
								}else
									lineWidth=0;

								//create shape for text using tranformation to make correct size
								AffineTransform at=new AffineTransform(finalTrm[0][0],finalTrm[0][1],finalTrm[1][0],finalTrm[1][1] ,finalTrm[2][0],finalTrm[2][1]);
//								AffineTransform at=new AffineTransform(Trm[0][0],Trm[0][1],Trm[1][0],Trm[1][1] ,Trm[2][0],Trm[2][1]);

//								if((type==StandardFonts.TYPE3)&&(renderDirectly)&& currentWidth!=0) //allow for rotated text with no width
//								at.scale((currentWidth/glyph.getmaxWidth()),currentFontData.FontMatrix[3]);
//								else
//								at.scale(currentFontData.FontMatrix[0],currentFontData.FontMatrix[3]);


								//add to renderer
								int fontType=type;
								if(type==StandardFonts.OPENTYPE){
									fontType=DynamicVectorRenderer.TYPE1C;

									//and fix for scaling in OTF
									float z=1000f/(glyph.getmaxWidth());
									at.scale(currentWidth*z, 1);

								}else if(type==StandardFonts.TRUETYPE || type==StandardFonts.CIDTYPE2 || (currentFontData.isFontSubstituted() && type!=StandardFonts.TYPE1)){
									fontType=DynamicVectorRenderer.TRUETYPE;
								}else if(type==StandardFonts.TYPE3){
									fontType=DynamicVectorRenderer.TYPE3;
								}else{
									fontType=DynamicVectorRenderer.TYPE1C;
								}

								//negative as flag to show we need to decode later
								if(generateGlyphOnRender)
									fontType=-fontType;

								/**
								 * add glyph outline to shape in TR7 mode
								 */
								if((Tmode==GraphicsState.CLIPTEXT)){

                                    if(glyph.getShape()!=null){ 
                                    	
                                    	Area glyphShape=(Area) (glyph.getShape()).clone();
                                        
                                    	glyphShape.transform(at);

                                    	if(glyphShape.getBounds().getWidth()>0 &&
                                    		glyphShape.getBounds().getHeight()>0){
                                        
                                    		gs.addClip(glyphShape);

                                            if(renderDirectly)
                                                current.renderClip(gs.getClippingShape(), null,null,g2);
								            else
                                                current.drawClip(gs);

                                    	}
									}
								}

								if(renderDirectly){

									PdfPaint strokeCol=null,fillCol=null;
									int text_fill_type = gs.getTextRenderType();

									//for a fill
									if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL)

										fillCol=gs.getNonstrokeColor();

									//and/or do a stroke
									if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE)
										strokeCol=gs.getStrokeColor();

									//set the stroke to current value
									Stroke newStroke=gs.getStroke();
									Stroke currentStroke=g2.getStroke();

									//avoid if stroke/fill
									if (text_fill_type == GraphicsState.STROKE)
										g2.setStroke(newStroke);


                                    if(lineWidth<1 && multiplyer>0)
                                        lineWidth=1f/multiplyer;
                                    
									current.renderEmbeddedText(gs,text_fill_type,glyph,fontType,g2,
											at,null,strokeCol,fillCol,
											gs.getStrokeAlpha(),
											gs.getNonStrokeAlpha(),null,(int)lineWidth) ;

									g2.setStroke(currentStroke);
								}else{
									if(isTextShifted)
										current.drawEmbeddedText( displayTrm,-fontSize,glyph,null,fontType,gs,at);
									else
										current.drawEmbeddedText( displayTrm,fontSize,glyph,null,fontType,gs,at);
								}
							}
						} catch (Exception e) {

							addPageFailureMessage("Exception "+e+" on embedded font renderer");

						}

					}else if(displayValue.length() > 0 && !displayValue.startsWith("&#")){

						/**set values used if rendering as well*/
						Object transformedGlyph2=null;

						{ //render now

							boolean isSTD=PdfDecoder.isRunningOnMac ||StandardFonts.isStandardFont(currentFontData.getBaseFontName(),false);

                            /////////////////////////////////

                            /**flush cache if needed*/
                            if(glyphs.lastTrm[0][0]!= displayTrm[0][0] || glyphs.lastTrm[1][0]!= displayTrm[1][0] || glyphs.lastTrm[0][1]!= displayTrm[0][1] || glyphs.lastTrm[1][1]!= displayTrm[1][1]){
                                glyphs.lastTrm = displayTrm;
                                glyphs.flush();
                            }

                            //either calculate the glyph to draw or reuse if already drawn
                            Area glyph = glyphs.getCachedShape(rawInt);
                            AffineTransform at= glyphs.getCachedTransform(rawInt);

                            if (glyph == null) {

                                double dY = -1,dX=1, y3 =0;

                                //allow for text running up the page
                                if ((displayTrm[1][0] < 0 && displayTrm[0][1] >= 0)||(displayTrm[0][1] < 0 && displayTrm[1][0] >= 0)) {
                                    dX=1f;
                                    dY=-1f;
                                }

                                if (isSTD) {

                                    glyph = glyphs.getGlyph(rawInt, displayValue, currentWidth);

                                    //hack to fix problem with Java Arial font
                                    if(glyph !=null && rawInt ==146 && glyphs.isArialInstalledLocally)
                                            y3 =-(glyph.getBounds().height- glyph.getBounds().y);
                                }else {

                                    //remap font if needed
                                    String xx= displayValue;
                                    if(glyphs.remapFont &&(glyphs.getUnscaledFont().canDisplay(xx.charAt(0))==false))
                                        xx= String.valueOf((char) (rawInt + 0xf000));

                                    GlyphVector gv1 =null;

                                    //do not show CID fonts as Lucida unless match
                                    if(!glyphs.isCIDFont|| glyphs.isFontInstalled)
                                        gv1= glyphs.getUnscaledFont().createGlyphVector(PdfJavaGlyphs.frc, xx);

                                    if(gv1!=null){

                                        glyph = new Area(gv1.getOutline());

                                        //put glyph into display position
                                        double glyphX=gv1.getOutline().getBounds2D().getX();

                                        //ensure inside box
                                        if(glyphX<0){
                                            glyphX=-glyphX;
                                            at =AffineTransform.getTranslateInstance(glyphX*2,0);
                                            glyph.transform(at);
                                            //x=-glyphX*2;
                                        }

                                        double glyphWidth=gv1.getVisualBounds().getWidth()+(glyphX*2);
                                        double scaleFactor= currentWidth /glyphWidth;
                                        if(scaleFactor<1)
                                            dX=dX*scaleFactor;
                                    }
                                }

                                //create shape for text using transformation to make correct size
                                at =new AffineTransform(dX* displayTrm[0][0],dX* displayTrm[0][1],dY* displayTrm[1][0],dY* displayTrm[1][1] ,0, y3);                                

                                //apply in old version
                                if(glyph !=null && !DynamicVectorRenderer.newCode2)
                                    glyph.transform(at);

                                //save so we can reuse if it occurs again in this TJ command
                                glyphs.setCachedShape(rawInt, glyph,at);
                            }

                            /////////////////////////////////
							if(glyph!=null){

								/**support for TR7*/
								if(Tmode==GraphicsState.CLIPTEXT && glyph.getBounds().width>0){


									Area glyphShape=(Area) glyph.clone();

                                    //we need to apply to make it all work
                                    if(DynamicVectorRenderer.newCode2)
                                    glyph.transform(at);

									//if its already generated we just need to move it
									if(!DynamicVectorRenderer.marksNewCode || renderDirectly){
										AffineTransform at2 =AffineTransform.getTranslateInstance(displayTrm[2][0],(displayTrm[2][1]));
										glyphShape.transform(at2);
									}

									gs.addClip(glyphShape);

                                    if(renderDirectly)
                                        current.renderClip(gs.getClippingShape(), null,null,g2);
                                    else
                                        current.drawClip(gs);
                                        
                                    if(renderDirectly)
										glyph=null;
								
								//if its already generated we just need to move it
								}else if(!DynamicVectorRenderer.marksNewCode){
									AffineTransform at2 =AffineTransform.getTranslateInstance(displayTrm[2][0],(displayTrm[2][1]));
									glyph.transform(at2);
								}
							}

							transformedGlyph2=glyph;

						}

						if(transformedGlyph2!=null){

							//add to renderer
							if(renderDirectly){
								PdfPaint currentCol=null,fillCol=null;
								int text_fill_type = gs.getTextRenderType();

								//for a fill
								if ((text_fill_type & GraphicsState.FILL) == GraphicsState.FILL)
									fillCol=gs.getNonstrokeColor();

								//and/or do a stroke
								if ((text_fill_type & GraphicsState.STROKE) == GraphicsState.STROKE)
									currentCol=gs.getStrokeColor();

								//set the stroke to current value
								Stroke newStroke=gs.getStroke();
								g2.setStroke(newStroke);

                                AffineTransform def=g2.getTransform();

                            if(DynamicVectorRenderer.marksNewCode)
			                    g2.translate(displayTrm[2][0], displayTrm[2][1]);

                            if(DynamicVectorRenderer.newCode2)
                                g2.scale(displayTrm[0][0],-displayTrm[1][1]);

                                current.renderText(displayTrm[2][0], displayTrm[2][1], text_fill_type,
										(Area) transformedGlyph2,g2,null,currentCol,fillCol,
										gs.getStrokeAlpha(),gs.getNonStrokeAlpha()) ;

                                g2.setTransform(def);
							}else{

								if(isTextShifted)
									current.drawEmbeddedText(displayTrm,-fontSize,null,transformedGlyph2,DynamicVectorRenderer.TEXT, gs,null);
								else
									current.drawEmbeddedText(displayTrm,fontSize,null,transformedGlyph2,DynamicVectorRenderer.TEXT, gs,null);
							}
						}
					}
				}

				/**track estimated heights of each letter on line to get maximum height for rectangluar outline*/
				if(legacyTextMode && textExtracted && !isCID){

					float h =PdfDecoder.currentHeightLookupData.getCharHeight(rawChar,fontSize);
					if (max_height < h)
						max_height = h;
				}

				/**now we have plotted it we update pointers and extract the text*/
				currentWidth = currentWidth + charSpacing;

				//see if about to add spaces
				boolean hasTextSpace=runningStoryPad && (charSpacing/spaceWidth)>1;

				if (rawChar == ' ') //add word spacing if
					currentWidth = currentWidth + wordSpacing;

				//workout gap between chars and decide if we should add a space
				currentGap = (width + charSpacing - lastWidth);
				String spaces="";
				if ((currentGap > 0) & (lastWidth > 0)) {

					float realGap=currentGap*fontScale;

					if(runningStoryPad && realGap>160 && fontSize>11){ //fix for way hermes rolls titles together

						/**calculate rectangular shape of text*/
						if(currentRotation ==0)
							calcCoordinates(x, Trm, isHorizontal, max_height, fontSize, y);
						else
							calcCoordinates(x, TrmWithRotationRemoved, isHorizontal, max_height, fontSize, y);

						if(isHorizontal)
							pdfData.addRawTextElement((charSpacing * THOUSAND),currentTextState.writingMode,
									font_as_string,currentFontData.getCurrentFontSpaceWidth(),
									currentTextState,x1,y1,x2-realGap,y2,moveCommand,textData,
									tokenNumber,textLength,currentColor,currentRotation);

						textData=new StringBuffer();
						textLength=-1;
						width=0;
						x=x2;
					}else{
						//textData.append(getSpaces(currentGap, spaceWidth, currentThreshold));
						spaces=getSpaces(currentGap, spaceWidth, currentThreshold);
					}
				}


				textLength++; //counter on chars in data
				width = width + currentWidth;
				lastWidth = width; //increase width by current char


				//add unicode value to our text data with embedded width
				if(textExtracted && unicodeValue.length() > 0) {

					//add character to text we have decoded with width
					//if large space separate out
					if (PdfDecoder.embedWidthData) {

						float xx=Trm[2][0];
						float yy=Trm[2][1];

						if(currentRotation!=0){
							xx=TrmWithRotationRemoved[2][0];
							yy=TrmWithRotationRemoved[2][1];
						}

						if(hasTextSpace && spaces.length()>0){
							textData.append(StoryData.marker);
							if((isHorizontal)|(PdfGroupingAlgorithms.oldTextExtraction))
								textData.append(xx-((charSpacing) * fontScale));
							else
								textData.append(yy-((charSpacing) * fontScale));

							textData.append(StoryData.marker);
							textData.append((charSpacing) * fontScale);
							textData.append(StoryData.marker);
						}

						textData.append(spaces);

						//embed width information in data
						if((isHorizontal)|(PdfGroupingAlgorithms.oldTextExtraction)){
							textData.append(StoryData.marker);
							textData.append(xx);
							textData.append(StoryData.marker);

						}else{
							textData.append(StoryData.marker);
							textData.append(yy);
							textData.append(StoryData.marker);
						}
						if(hasTextSpace)
							textData.append((currentWidth-charSpacing) * fontScale);
						else
							textData.append(currentWidth * fontScale);

						textData.append(StoryData.marker);

					}else
						textData.append(spaces);

					/**add data to output*/
					String current_value= unicodeValue;

                    //turn chars less than 32 into escape
					int length=current_value.length();
					char next;
					for (int ii = 0; ii < length; ii++) {
						next = current_value.charAt(ii);

						if((!runningStoryPad)||(next!=32 && next!=10 && next!=13))
							hasContent=true;

                        //map tab to space
                        if(next==9)
                        next=32;

                        if(PdfDecoder.isXMLExtraction()&&(next=='<'))
							textData.append("&lt;");
						else if(PdfDecoder.isXMLExtraction()&&(next=='>'))
							textData.append("&gt;");
                        else if(next==64258)
                            textData.append("fl");
                        else if (next > 31)
							textData.append(next);
						else
							textData.append(hex[next]);
					}
				}else
					textData.append(spaces);

			} else if (rawChar ==40 || rawChar == 60) { //start of text stream '('=40 '<'=60

				inText = true; //set text flag - no escape character possible
				openChar = rawChar;


			} else if ((rawChar == 41) || (rawChar == 62 && openChar==60)||((!inText)&&((rawChar=='-')||(rawChar>='0' && rawChar<='9')))) { // ')'=41 '>'=62 '<'=60


				//handle leading between text ie -100 in  (The)-100(text)

				float value = 0;
				i++;

				//allow for spaces
				while(stream[i]==32) //' '=32
					i++;

				nextChar = (char) stream[i];

				//allow for )( or >< (ie no value)
				if(nextChar==40 || nextChar==60){ //'('=40 '<'=60
					i--;
				}else if ((nextChar != 39)&&(nextChar != 34)&&(nextChar != 40)&& (nextChar != 93)&& (nextChar != 60)) { //leading so roll on char
					//'\''=39 '\"'=34 '('=40  //']'=93 '<'=60
					StringBuffer currentLeading = new StringBuffer(6);

					int leadingStart=i; //allow for failure
					boolean failed=false;
					boolean isMultipleValues=false;
					while (!failed) {
						rawChar = nextChar;
						if(rawChar!=10 && rawChar !=13)
							currentLeading.append(rawChar);
						nextChar = (char) stream[i + 1];

						if(nextChar==32)
							isMultipleValues=true;

						if (nextChar == 40 || nextChar == 60 || nextChar==']') // '('=40 '<'=60
							break;

						if(nextChar==45 || nextChar==46 || nextChar==32 || (nextChar>='0' && nextChar<='9')){
							//'-'=45 '.'=46 ' '=32
						}else
							failed=true;

						i++;
					}

					if(failed)
						i= leadingStart;
					else{

						//more than one value separated by space
						if(isMultipleValues){
							StringTokenizer values=new StringTokenizer(currentLeading.toString());
							value=0;
							while(values.hasMoreTokens())
								value =value +Float.parseFloat(values.nextToken());

							value =-value/ THOUSAND;
						}else if(currentLeading.length()>0)
							value = -Float.parseFloat(currentLeading.toString()) / THOUSAND;

					}
				}

				width = width + value;
				leading = leading + value; //keep count on leading

			}

            //textExtracted added by Mark
            //generate if we are in Viewer (do not bother if thumbnails)
			if(DynamicVectorRenderer.textBasedHighlight && textExtracted){
				if(displayValue.length()>0 && !displayValue.equals(" ")){
					
					float xx=((int)Trm[2][0]);
					float yy=((int)Trm[2][1]);
					
					float ww=((int)Trm[0][0]);
					if(ww==0)
						ww=((int)Trm[1][0]);
					
					float hh=((int)Trm[1][1]);
					if(hh==0)
						hh=((int)Trm[0][1]);
					
					if(ww<0){
						ww=-ww;
						xx = xx-ww;
					}
					if(hh<0){
						hh=-hh;
						yy = yy-hh;
					}
					
					Rectangle fontbb = currentFontData.getBoundingBox();

///////////@old version of text code 20090727
/**/
					float fy = fontbb.y;
					if(fy==0) //If no y set it may be embedded so we should guess a value
						fy = 100;
					if(fy<0)
						fy = -fy;

					float h = 1000+(fy);
					//Percentage of fontspace used compared to default
					h = 1000/h;
					float fontHeight = 0;
					switch(currentTextState.writingMode){
					case PdfData.HORIZONTAL_LEFT_TO_RIGHT : 
						fontHeight = (hh/h);
						yy = yy-(fontHeight-hh);
						hh = fontHeight;
						break;
					case PdfData.HORIZONTAL_RIGHT_TO_LEFT : 
						break;
					case PdfData.VERTICAL_TOP_TO_BOTTOM : 
						fontHeight = (ww/h);
						xx = xx-(fontHeight-ww);
						ww = fontHeight;					
						break;
					case PdfData.VERTICAL_BOTTOM_TO_TOP :
						fontHeight = (ww/h);
						ww = fontHeight;
						break;
					}
/**/
///////////
					/**
					float h = 0;
					float fy = 0;
					float fontHeight = 0;
					switch(currentTextState.writingMode){
					case PdfData.HORIZONTAL_LEFT_TO_RIGHT :
						fy = fontbb.y + fontbb.height;
						if(fy==0) //If no y set it may be embedded so we should guess a value
							fy = 100;
						if(fy<0)
							fy = -fy;

						//Percentage of fontspace used compared to default
						fy = 1000/fy;
						fontHeight = (hh*fy);
						
						//Do not use partial coords
						if((fontHeight-(int)fontHeight)>0){
							fontHeight = fontHeight+1;
						}
						
						yy = yy-(fontHeight-hh);
						hh = fontHeight;
						break;
					case PdfData.HORIZONTAL_RIGHT_TO_LEFT : 
						break;
					case PdfData.VERTICAL_TOP_TO_BOTTOM : 
						fy = fontbb.y;
						if(fy==0) //If no y set it may be embedded so we should guess a value
							fy = 100;
						if(fy<0)
							fy = -fy;

						h = 1000+(fy);
						//Percentage of fontspace used compared to default
						h = 1000/h;
						fontHeight = (ww/h);
						xx = xx-(fontHeight-ww);
						ww = fontHeight;				
						break;
					case PdfData.VERTICAL_BOTTOM_TO_TOP :
						fy = fontbb.y;
						if(fy==0) //If no y set it may be embedded so we should guess a value
							fy = 100;
						if(fy<0)
							fy = -fy;

						h = 1000+(fy);
						//Percentage of fontspace used compared to default
						h = 1000/h;
						fontHeight = (ww/h);
						ww = fontHeight;
						break;
					}
					/**/
					PdfHighlights.addToLineAreas(new Rectangle((int)xx ,(int)yy ,(int)ww ,(int)hh), currentTextState.writingMode);
//					current.addToLineAreas(new Rectangle((int)xx ,(int)yy ,(int)ww ,(int)hh), currentTextState.writingMode);
				}
			}
			
			i++;
		}

		/**all text is now drawn (if required) and text has been decoded*/

		//final move to get end of shape
		temp[0][0] = 1;
		temp[0][1] = 0;
		temp[0][2] = 0;
		temp[1][0] = 0;
		temp[1][1] = 1;
		temp[1][2] = 0;
		temp[2][0] = (currentWidth + leading); //tx;
		temp[2][1] = 0; //ty;
		temp[2][2] = 1;
		Trm = Matrix.multiply(temp, Trm); //multiply to get new Tm

		//update Tm to cursor
		currentTextState.Tm[2][0] = Trm[2][0];
		currentTextState.Tm[2][1] = Trm[2][1];

		if(currentRotation!=0){

			TrmWithRotationRemoved = Matrix.multiply(temp, TrmWithRotationRemoved); //multiply to get new Tm

			//update Tm to cursor
			currentTextState.TmNoRotation[2][0] = TrmWithRotationRemoved[2][0];
			currentTextState.TmNoRotation[2][1] = TrmWithRotationRemoved[2][1];

		}

		/**used to debug code by popping up window after glyph*
         Object[] options = { "OK" };
         int n =JOptionPane.showOptionDialog(null,null,"Storypad",JOptionPane.OK_OPTION,JOptionPane.INFORMATION_MESSAGE,null,options,options[0]);
         /***/

		/** now workout the rectangulat shape this text occupies
		 * by creating a box of the correct width/height and transforming it
		 * (this routine could undoutedly be better coded but it works and I
		 * don't want to break it!!)
		 */
		if(textExtracted){

			//subtract character spacing once to make correct number(chars-1)
			width = width - charSpacing;

			/**roll on if last char is not a space - otherwise restore to before spaces*/
			if (lastTextChar == ' '){
				Trm = TrmBeforeSpace;

				if(currentRotation!=0)
					TrmWithRotationRemoved = TrmBeforeSpaceWithRotationRemoved;
			}

			/**calculate rectangular shape of text*/
			if(currentRotation==0)
				calcCoordinates(x, Trm, isHorizontal, max_height, fontSize, y);
			else
				calcCoordinates(x, TrmWithRotationRemoved, isHorizontal, max_height, fontSize, y);

			//System.out.println(y1+" "+y2+" "+fontSize+" "+y);
			//Matrix.show(Trm);

			/**return null for no text*/
			if (textData.length() == 0 || !hasContent) //return null if no text
				textData = null;

			/**set textState values*/
			if(fontSize!=lastFontSize || font_as_string==null){
				currentTextState.setCurrentFontSize(Math.abs(fontSize));

				font_as_string =Fonts.createFontToken(currentFont,currentTextState.getCurrentFontSize());

				lastFontSize=fontSize;
			}


			/**

            if(textData!=null && org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(textData.toString()).toString().indexOf("his dad")!=-1){
                System.out.println(x1+" "+y1+","+x2+" "+y2+" "+org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(textData.toString())+" "+
                        max_height+" "+fontSize+" "+currentFontData.getFontName()+" "+Trm[0][0]);
                //System.out.println(this.moveCommand+"<<");//charSpacing+" "+currentFontData.getCurrentFontSpaceWidth());

            }

                        /***/

			//if(textData!=null)// && x1>320 && x2<400 && y1>880 && y1<885)
			//System.out.println(y1+" "+org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(textData.toString()));

            //if(org.jpedal.grouping.PdfGroupingAlgorithms.removeHiddenMarkers(textData.toString()).equals("C"))
            
			/**
			 * hack for Times
			 */
			if(runningStoryPad && (!isHorizontal))// || (Trm[1][0]!=0 || Trm[0][1]!=0)))
				textData=null;

			return textData;
		}else
			return null;

	}

	private void calcCoordinates(float x, float[][] rawTrm, boolean horizontal, float max_height, int fontSize, float y) {

		//clone data so we can manipulate
		float[][] trm=new float[3][3];
		for(int xx=0;xx<3;xx++){
			System.arraycopy(rawTrm[xx], 0, trm[xx], 0, 3);
		}

		x1 = x;
		x2 = trm[2][0] - (charSpacing * trm[0][0]);

		if (horizontal) {
			if (trm[1][0] < 0) {
				x1 = x + trm[1][0] - (charSpacing * trm[0][0]);
				x2 = trm[2][0];
			} else if (trm[1][0] > 0) {
				x1 = x;
				x2 = trm[2][0];
			}
		} else if (trm[1][0] > 0) {
			x1 = trm[2][0];
			x2 = x + trm[1][0] - (charSpacing * trm[0][0]);
		} else if (trm[1][0] < 0) {
			x2 = trm[2][0];
			x1 = x + trm[1][0] - (charSpacing * trm[0][0]);
		}

		/**any adjustments*/
		if (horizontal) {
			//workout the height ratio
			float s_height= 1.0f;
			if(legacyTextMode || currentFontData.getFontType()==StandardFonts.TYPE3)
				s_height=(max_height  / (fontSize));

			if (trm[0][1] != 0) {
				y1 =trm[2][1]- trm[0][1]+ ((trm[0][1] + trm[1][1]) * s_height);
				y2 = y;

			} else {
				y1 = y + (trm[1][1] * s_height);
				y2 = trm[2][1];
			}
		} else if (trm[0][1] <= 0) {
			y2 = trm[2][1];
			y1 = y;
		} else if (trm[0][1] > 0) {
			y1 = trm[2][1];
			y2 = y;
		}
	}

	/**
	 * workout spaces (if any) to add into content for a gap
	 * from user settings, space info in pdf
	 */
	static final private String getSpaces(
			float currentGap,
			float spaceWidth,
			float currentThreshold) {
		String space = "";

		if (spaceWidth > 0) {
			if ((currentGap > spaceWidth)) {
				while (currentGap >= spaceWidth) {
					space = ' ' + space;
					currentGap = currentGap - spaceWidth;
				}
			} else if (currentGap > spaceWidth * currentThreshold) {
				//ensure a gap of at least space_thresh_hold
				space = space + ' ';
			}
		}


		return space;
	}

	/**
	 * get unicode/escape value and convert to value
	 */
	static final private int readEscapeValue(
			int start,
			int count,
			int base,
			byte[] characterStream) {
		StringBuffer chars = new StringBuffer();
		for (int pointer = 0; pointer < count; pointer++)
			chars.append((char) characterStream[start + pointer]);

		return Integer.parseInt(chars.toString(), base);
	}

	/**
	 * Returns the fonts used in the file
	 */
	public String getFontsInFile() {
		return fontsInFile;
	}

	/**
	 * setup stream decoder to render directly to g2
	 * (used by image extraction)
	 */
	public void setDirectRendering(Graphics2D g2) {

		this.renderDirectly=true;
		this.g2=g2;
		this.defaultClip=g2.getClip();

	}

	/**
     shows if embedded fonts present
	 */
	public boolean hasEmbeddedFonts() {
		return hasEmbeddedFonts;
	}

	/**
	 * include image data in PdfData (used by Storypad, not part of API)
	 */
	public void includeImages(){
		includeImagesInData=true;
	}

	/**
	 * return object holding horizontal and vertical lines
	 */
	public PageLines getPageLines() {

		pageLines.lookForCompositeLines();
		return pageLines;
	}

	/**
	 * get page statuses
	 */
	public boolean getPageDecodeStatus(int status) {

		if(status==(DecodeStatus.PageDecodingSuccessful))
			return pageSuccessful;
		else if(status==(DecodeStatus.NonEmbeddedCIDFonts))
			return hasNonEmbeddedCIDFonts;
		else if(status==(DecodeStatus.ImagesProcessed))
			return imagesProcessedFully;
        else if(status==(DecodeStatus.YCCKImages))
			return hasYCCKimages;
		else
			new RuntimeException("Unknown paramter");

		return false;
	}

	/**
	 * get page statuses
	 */
	public String getPageDecodeStatusReport(int status) {

		if(status==(DecodeStatus.NonEmbeddedCIDFonts)){
			return nonEmbeddedCIDFonts.toString();
		}else
			new RuntimeException("Unknown paramter");

		return "";
	}

	/**
	 * flag to show if printing failed
	 * @deprecated use getPageDecodeStatus(DecodeStatus.PageDecodingSuccessful)
	 */
	public boolean isPageSuccessful() {

		return pageSuccessful;
	}

	/**
	 * return list of messages
	 */
	public String getPageFailureMessage() {
		return pageErrorMessages;
	}

	/**
	 * add message on printer problem
	 */
	public void addPageFailureMessage(String value) {
		pageSuccessful=false;
		pageErrorMessages=pageErrorMessages+value+ '\n';
	}

	public StringBuffer getlastTextStreamDecoded() {
		return textData;
	}


	public DynamicVectorRenderer getRenderer() {

		return this.current;
	}

	/**
	 * tells program to try and use Java's font printing if possible
	 * as work around for issue with PCL printing
	 */
	public void setTextPrint(int textPrint) {
		this.textPrint = textPrint;
	}

	/**flag to show if we suspect problem with some images
	 *
	 * @deprecated use getPageDecodeStatus(DecodeStatus.ImagesProcessed))
	 * */
	public boolean hasAllImages() {
		return imagesProcessedFully;
	}

	public void setExternalImageRender(ImageHandler customImageHandler) {
		this.customImageHandler = customImageHandler;
		if(this.customImageHandler!=null)
			keepRaw=true;

		//<start-jfr>
		if(customImageHandler!=null && current!=null)
			current.setCustomImageHandler(customImageHandler);
		//<end-jfr>
	}


	//<start-adobe>


	/**
	 * used internally for structured content extraction.
	 * Does not work on OS version
	 */
	public void setMapForMarkedContent(Object pageStream) {

		markedContentExtracted=true;

		contentHandler=new StructuredContentHandler(pageStream);

	}
	//<end-adobe>

	/**
	 * define stream as PATTERN or POSTSCRIPT or TYPE3 fonts
	 */
	public void setStreamType(int type) {
		streamType=type;

	}

	public void setLayers(PdfLayerList layers) {
		this.layers=layers;
	}

	public void setMultiplier(float multiplyer) {
		
		this.multiplyer=multiplyer;
		
	}

	public void dispose() {
		
		if(pdfData!=null)
		this.pdfData.dispose();
		
		this.pageLines=null;
		
		this.currentDrawShape=null;
		
	}
    /**
     private class TestShapeTracker implements ShapeTracker {
     public void addShape(int tokenNumber, int type, Shape currentShape, PdfPaint nonstrokecolor, PdfPaint strokecolor) {

     //use this to see type
     //Cmd.getCommandAsString(type);

     //print out details
     if(type==Cmd.S || type==Cmd.s){ //use stroke color to draw line
     System.out.println("-------Stroke-------PDF cmd="+Cmd.getCommandAsString(type));
     System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" stroke color="+strokecolor);

     }else if(type==Cmd.F || type==Cmd.f || type==Cmd.Fstar || type==Cmd.fstar){ //uses fill color to fill shape
     System.out.println("-------Fill-------PDF cmd="+Cmd.getCommandAsString(type));
     System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" fill color="+nonstrokecolor);

     }else{ //not yet implemented (probably B which is S and F combo)
     System.out.println("Not yet added");
     System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" type="+type+" "+Cmd.getCommandAsString(type));
     }
     }
     }

     /**
     * return details on page for type (defined in org.jpedal.constants.PageInfo) or null if no values
     * Unrecognised key will throw a RunTime exception
     */
    public Iterator getPageInfo(int type) {

        Iterator i=null;
        switch(type){
            case PageInfo.COLORSPACES:

                i=colorspacesUsed.keySet().iterator();
                break;

           default:
               throw new RuntimeException("Unrecognised key "+type);
        }
        return i;
    }
}
