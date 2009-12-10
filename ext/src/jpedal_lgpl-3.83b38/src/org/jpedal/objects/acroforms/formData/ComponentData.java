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
* ComponentData.java
* ---------------
*/
package org.jpedal.objects.acroforms.formData;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.objects.Javascript;

import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.utils.repositories.Vector_String;


/**holds all data not specific to Swing/SWT/ULC*/
public class ComponentData {

	public static final boolean newSetValueCode = true;
	
	public static final int TEXT_TYPE = 0;
	public static final int BUTTON_TYPE = 1;
	public static final int LIST_TYPE = 2;
	public static final int UNKNOWN_TYPE = -1;
	/**
	 * holds forms data array of formObjects data from PDF as Map for fast lookup
	 **/
	protected Map rawFormData=new HashMap(),convertFormIDtoRef=new HashMap(), nameToRef=new HashMap(),duplicateNames=new HashMap();
	private List namesMap=new ArrayList();


    protected Map componentsToIgnore=new HashMap();
    
	protected int insetW;

	protected int insetH;

	protected PdfPageData pageData;

	protected Javascript javascript;

	/**
	 * local copy needed in rendering
	 */
	protected int pageHeight, indent;

	protected int[] cropOtherY;
	/**
	 * track page scaling
	 */
	protected float displayScaling, scaling = 0;
	protected int rotation;

	/**
	 * used to only redraw as needed
	 */
	protected float lastScaling = -1, oldRotation = 0, oldIndent = 0;

	/**
	 * used for page tracking
	 */
	protected int startPage, endPage, currentPage;

	/**
	 * the last name added to the nameToCompIndex map
	 */
	protected String lastNameAdded = "";

	/** stores map of names to indexs of components in allfields*/
	protected Map duplicates = new HashMap();

	protected Map lastValidValue=new HashMap();
	protected Map lastUnformattedValue=new HashMap();

	/**
	 * stores the name and component index in allFields array
	 */
	protected Map nameToCompIndex;
	
	/**
	 * stores the Pdf Reference and component index in allFields array
	 */
	protected Map refToCompIndex;

	protected Map typeValues;

	/**
	 * next free slot
	 */
	protected int nextFreeField = 0;



	/**
	 * holds the location and size for each field,
	 * <br>
	 * [][0] = x1;
	 * [][1] = y1;
	 * [][2] = x2;
	 * [][3] = y2;
	 */
	protected float[][] boundingBoxs;
	
	protected float[][] popupBounds = new float[0][4];

	protected int[] fontSize;

	/**
	 * used to draw pages offset if not in SINGLE_PAGE mode
	 */
	protected int[] xReached, yReached;

	/**
	 * table to store if page components already built
	 */
	protected int[] trackPagesRendered;
	
    /** stores the forms in there original order, accessable by page */
    protected List[] formsUnordered;

	/**
	 * flag to show if component has been displayed for the first time
	 */
	protected boolean[] firstTimeDisplayed;

	/**
	 * array to store fontsizes as the field is set up for use on rendering
	 */
	protected int[] fontSizes;

	/**
	 * the reset to value of the indexed field
	 */
	protected String[] defaultValues;

	/**
	 * array to hold page for each component so we can scan quickly on page change
	 */
	public int[] pageMap;
    private int formCount;
    protected PdfLayerList layers;
    
    /**last value set by user in GUI or null if none*/
	public Object getLastValidValue(String fieldRef) {
		return lastValidValue.get(fieldRef);
	}

    public void setLayerData(PdfLayerList layers){
        this.layers=layers;
    }



    /**last value set by user in GUI or null if none*/
	public Object getLastUnformattedValue(String fieldRef) {
		return lastUnformattedValue.get(fieldRef);
	}

	public void reset() {

		lastValidValue.clear();
		lastUnformattedValue.clear();

    }

    /**
     * allow user to lookup page with name of Form.
     * @param formName
     * @return page number or -1 if no page found
     */
    public int getPageForFormObject(String formName) {

        //Object checkObj;
        FormObject formObj=null;

		if (formName.indexOf("R") != -1) {
		//	checkObj = refToCompIndex.get(formName);
            formObj = ((FormObject)rawFormData.get(formName));

		}else {
		//	checkObj = nameToCompIndex.get(formName);
            String ref= (String) nameToRef.get(formName);
            if(ref!=null)
            formObj = ((FormObject)rawFormData.get(ref));

		}

        if(formObj == null)
			return -1;
        else
            return formObj.getPageNumber();

	}

    /**
     * returns the Type of pdf form, of the named field
     */
    public Integer getTypeValueByName(String fieldName) {

        Object key=typeValues.get(fieldName);
        if(key==null)
            return  FormFactory.UNKNOWN;
        else
            return (Integer) typeValues.get(fieldName);
    }

	/**
	 * return next ID for this page and also set pointer
	 * @param page
	 * @return
	 */
	private int setStartForPage(int page) {

		//flag start
        trackPagesRendered[page] = nextFreeField;

		return nextFreeField;
	}
	
	public void setUnsortedListForPage(int page,List unsortedComps){
		formsUnordered[page] = unsortedComps;
	}
	
	/**
	 * get next free field slot
	 * @return
	 */
	public int getNextFreeField() {
		return nextFreeField;
	}

    /**
	 * max number of form slots
	 */
	public int getMaxFieldSize() {
		return formCount;
	}

    /**
     * return start component ID or -1 if not set or -999 if trackPagesRendered not initialised
     * @param page
     * @return
     */
	public int getStartComponentCountForPage(int page) {
		if(trackPagesRendered==null)
			return -999;
		else if(trackPagesRendered.length>page)
			return trackPagesRendered[page];
		else
			return -1;
	}

	/**
	 * setup values needed for drawing page
	 * @param pageData
	 * @param page
	 */
	public void initParametersForPage(PdfPageData pageData, int page){

        //ensure setup
		if(cropOtherY==null || cropOtherY.length<=page)
			this.resetComponents(0, page+1, false);
		
		int mediaHeight = pageData.getMediaBoxHeight(page);
		int cropTop = (pageData.getCropBoxHeight(page) + pageData.getCropBoxY(page));

		//take into account crop		
		if (mediaHeight != cropTop)
			cropOtherY[page] = (mediaHeight - cropTop);
		else
			cropOtherY[page] = 0;
		
		this.pageHeight = mediaHeight;
		this.currentPage = page; //track page displayed

		//set for page
		setStartForPage(page);

	}

	/**
	 * used to flush/resize data structures on new document/page
	 * @param formCount
	 * @param pageCount
	 * @param keepValues
	 */
	public void resetComponents(int formCount,int pageCount,boolean keepValues) {

		nextFreeField = 0;
        this.formCount=formCount;

		if(!keepValues){

			refToCompIndex = new HashMap(formCount+1);
			nameToCompIndex = new HashMap(formCount + 1);
			typeValues = new HashMap(formCount+1);

			pageMap = new int[formCount + 1];
			fontSize = new int[formCount + 1];

			//start up boundingBoxs
			boundingBoxs = new float[formCount + 1][4];
			popupBounds = new float[0][4];

			fontSizes = new int[formCount + 1];

			defaultValues = new String[formCount + 1];

			firstTimeDisplayed = new boolean[formCount + 1];

			//flag all fields as unread
			trackPagesRendered = new int[pageCount + 1];
			for (int i = 0; i < pageCount + 1; i++)
				trackPagesRendered[i] = -1;
			
			formsUnordered = new List[pageCount+1];
			
			//reset offsets
			cropOtherY=new int[pageCount+1];
			
		}else if(pageMap!=null){

			boolean[] tmpFirstTimeDisplayed = firstTimeDisplayed;
			int[] tmpMap = pageMap;
			int[] tmpSize = fontSize;
			String[] tmpValues = defaultValues;
			float[][] tmpBoxs = boundingBoxs;
			int[] tmpSizes = fontSizes;

			firstTimeDisplayed = new boolean[formCount + 1];
			// allFields = new Component[formCount + 1];
			pageMap = new int[formCount + 1];
			fontSize = new int[formCount + 1];
			defaultValues = new String[formCount + 1];

			//start up boundingBoxs
			boundingBoxs = new float[formCount + 1][4];

			fontSizes = new int[formCount + 1];

			int origSize=tmpMap.length;

			//populate
			for (int i = 0; i < formCount+1; i++) {

				if(i==origSize)
					break;
				//if (tmpFields[i] == null)
				//     break;

				firstTimeDisplayed[i] = tmpFirstTimeDisplayed[i];
				pageMap[i] = tmpMap[i];

				fontSize[i] = tmpSize[i];
				defaultValues[i] = tmpValues[i];


				System.arraycopy(tmpBoxs[i], 0, boundingBoxs[i], 0, 4);

				fontSizes[i] = tmpSizes[i];

				nextFreeField++;
			}
		}
	}

	/**
	 * pass in current values used for all components
	 * @param scaling
	 * @param rotation
	 */
	public void setPageValues(float scaling, int rotation) {

		this.scaling=scaling;
		this.rotation=rotation;
		this.displayScaling=scaling;

	}

	/**
	 * used to pass in offsets and PdfPageData object so we can access in rendering
	 * @param pageData
	 * @param insetW
	 * @param insetH
	 */
	public void setPageData(PdfPageData pageData, int insetW, int insetH) {

		//track inset on page
		this.insetW = insetW;
		this.insetH = insetH;

		this.pageData = pageData;

	}

	/**
	 * returns the default values for all the forms in this document
	 */
	public String[] getDefaultValues() {
		return defaultValues;
	}

	/**
	 * offsets for forms in multi-page mode
	 */
	public void setPageDisplacements(int[] xReached, int[] yReached) {

		this.xReached = xReached;
		this.yReached = yReached;

	}

	/**
	 * provide access to Javascript object
	 * @param javascript
	 */
	public void setJavascript(Javascript javascript) {
		this.javascript=javascript;

	}

	public void resetDuplicates() {
		duplicates.clear();

	}

     protected boolean isFormNotPrinted(int currentComp) {

        // get correct key to lookup form data
        String ref = this.convertIDtoRef(currentComp);


        //System.out.println(currentComp+" "+comp.getLocation()+" "+comp);
        Object rawForm = this.getRawForm(ref);

        FormObject form=null;

        if (rawForm instanceof FormObject) {
            form = (FormObject) rawForm;
        }else{ //several values as children
            form = (FormObject) ((Object[])rawForm)[0];
        }

        if(form!=null){
            boolean isNotPrinted= componentsToIgnore!=null &&
                    (componentsToIgnore.containsKey(new Integer(form.getParameterConstant(PdfDictionary.Subtype))) ||
            componentsToIgnore.containsKey(new Integer(form.getParameterConstant(PdfDictionary.Type))));

            return isNotPrinted;
        }else
            return false;
    }
	
	/**
	 * store form data and allow lookup by PDF ref or name 
	 * (name may not be unique)
	 * @param formObject
	 */
	public void storeRawData(FormObject formObject) {
		
		String fieldName=formObject.getTextStreamValue(PdfDictionary.T);
		//add names to an array to track the kids
		if(fieldName!=null)
			namesMap.add(fieldName);

		String ref=formObject.getObjectRefAsString();
		nameToRef.put(fieldName,ref);
		rawFormData.put(ref,formObject);
		
		/**
		 * track duplicates
		 */
		String duplicate=(String) duplicateNames.get(fieldName);
		if(duplicate==null){ //first case
			duplicateNames.put(fieldName,ref);
		}else{ //is a duplicate
			duplicate=duplicate+","+ref; // comma separated list
			duplicateNames.put(fieldName,duplicate);
		}
		
	}
	
	public void flushFormData() {
		
		nameToRef.clear();
		rawFormData.clear();
		duplicateNames.clear();
        convertFormIDtoRef.clear();
        namesMap.clear();

        lastNameAdded="";
    }

    /**
     * convert ID used for GUI components to PDF ref for underlying object used
     * so we can access form object knowing ID of component
     * @param objectID
     * @return
     */
    public String convertIDtoRef(int objectID){
        return (String)convertFormIDtoRef.get(new Integer(objectID));
    }


	/** returns all the formObjects by the specified name */
	public Object getRawForm(String objectName) {
		//if name see if duplicates
		String matches = (String) duplicateNames.get(objectName);
		
		if(matches==null || (matches.indexOf(',')==-1)){//single form
			
			//convert to PDFRef if name first
			String possRef=(String) nameToRef.get(objectName);
			if(possRef!=null)
				objectName=possRef;
			
			//@chris change
			//return new Object[]{rawFormData.get(objectName)};
			return rawFormData.get(objectName);
			
		}else{//duplicates
			
			StringTokenizer comps=new StringTokenizer(matches,",");
			int count=comps.countTokens();
			Object[] values=new Object[count];
			
			for(int ii=0;ii<count;ii++){
                values[ii] = rawFormData.get(comps.nextToken());
			}
			return values;
		}
	}
	
	/** returns the rawformdata map, the key is the pdfRef for whichever object you want */
	public Map getRawFormData() {
		return rawFormData;
	}
	
	/**
	 * this takes in a name with a . at the end and returns the kids of that object
	 */
	public String[] getChildNames(String name){
		//CHRIS childnames not working
		//we need to find all fullyqualifiednames and check them.
		
		//check if the name is within the list we currently have
		if(namesMap.toString().contains(name)){
			Vector_String childNames = new Vector_String();
			
			//scan over the list and find the child names
			Iterator iter = namesMap.iterator();
			while(iter.hasNext()){
				String val = (String)iter.next();
				if(val.contains(name)){
					// add them to our arrayList
					childNames.addElement(val);
				}
			}
			
			//return the Vector of childnames as a String[] 
			//NOTE: remember to trim first otherwise you get a massive array
			childNames.trim();
			return childNames.get();
		}else {
			// if there is no name within our list return null
			return null;
		}
	}
	
	/** works out if the popup is within the page crop bounds and if not moves it within them */
	protected float[] checkPopupBoundsOnPage(float[] floatArray) {
	
        //if popups locations are off page move to be on page
		int cropX = pageData.getCropBoxX(currentPage);
		int cropY = pageData.getCropBoxY(currentPage);
		int cropX2 = cropX+pageData.getCropBoxWidth(currentPage);
		int cropY2 = cropY+pageData.getCropBoxHeight(currentPage);
		
		if(floatArray[0]<cropX){
			//if too far left move to cropX and move second X point along by difference
			floatArray[2] += (cropX-floatArray[0]);
			floatArray[0] = cropX;
		}
		if(floatArray[1]<cropY){
			//if too far down move to cropY and move second Y point up by difference
			floatArray[3] += (cropY-floatArray[1]);
			floatArray[1] = cropY;
		}
		if(floatArray[2]>cropX2){
			//if too far right move to rightmost point and move first X point back by difference
			floatArray[0] -= (floatArray[2]-cropX2);
			floatArray[2] = cropX2;
		}
		if(floatArray[3]>cropY2){
			//if too far up move to highest point and move first Y point back by difference
			floatArray[1] = (floatArray[3]-cropY2);
			floatArray[3] = cropY2;
		}
		
		return floatArray;
	}
	
	public Object setValue(String ref, Object value, boolean isValid, boolean isFormatted, boolean reset, Object oldValue){
		// track so we can reset if needed
		if (!reset && isValid) {
			lastValidValue.put(ref, value);
		}
		// save raw version before we overwrite
		if (!reset && isFormatted) {
			lastUnformattedValue.put(ref, oldValue);
		}

		Object checkObj;
		if (ref.indexOf("R") != -1) {
			checkObj = refToCompIndex.get(ref);
		} else {
			checkObj = nameToCompIndex.get(ref);
		}
		
		//Fix null exception in /PDFdata/baseline_screens/forms/406302.pdf
		if(checkObj==null)
			return null;
		
		// Now set the formObject value so we keep track of the current field value within our FormObject
		String pdfRef = convertIDtoRef(((Integer)checkObj).intValue());
		FormObject form = ((FormObject)rawFormData.get(pdfRef));
		form.setValue((String)value);
		
		return checkObj;
	}
}
