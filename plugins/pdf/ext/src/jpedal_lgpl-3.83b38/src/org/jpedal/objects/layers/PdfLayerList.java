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

  * OCLayerList.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.layers;

import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.OCObject;
import org.jpedal.objects.raw.PdfKeyPairsIterator;
import org.jpedal.io.PdfObjectReader;

import java.util.*;

public class PdfLayerList {

    private static boolean debug=false;

    private Map layerNames=new LinkedHashMap();

    private Map streamToName=new HashMap();

    private Map layersEnabled=new HashMap();

    private Map metaData=new HashMap();

    private Map layerLocks=new HashMap();

    private boolean changesMade=false;

    private Map propertyMap, refToPropertyID,refTolayerName,RBconstraints,isChildOfLayer;

    private Map minScale=new HashMap();
    private Map maxScale=new HashMap();

    private float scaling=1f;
    
    int layerCount=0;
    private Object[] order;


    PdfObjectReader currentPdfFile=null;
	
    /**
     * add layers and settings to list
     * @param OCProperties
     * @param PropertiesObj
     * @param currentPdfFile
     */
    public void init(PdfObject OCProperties, PdfObject PropertiesObj, PdfObjectReader currentPdfFile) {

        propertyMap=new HashMap();
        refToPropertyID =new HashMap();
        refTolayerName=new HashMap();
        RBconstraints=new HashMap();
        isChildOfLayer=new HashMap();

        this.currentPdfFile=currentPdfFile;

        if(PropertiesObj!=null)
            setupOCMaps(PropertiesObj, currentPdfFile);

        PdfObject layerDict=OCProperties.getDictionary(PdfDictionary.D);

        if(layerDict==null)
        return;

        int OCBaseState=layerDict.getNameAsConstant(PdfDictionary.BaseState);

        //if not set use default
        if(OCBaseState== PdfDictionary.Unknown);
        OCBaseState= PdfDictionary.ON;

        //read order first and may be over-written by ON/OFF
        order=layerDict.getObjectArray(PdfDictionary.Order);

        if(debug){
            System.out.println("PropertiesObj="+PropertiesObj);
            System.out.println("layerDict="+layerDict);
            System.out.println("propertyMap="+propertyMap);
            System.out.println("propertyMap="+propertyMap);
            System.out.println("refToPropertyID="+refToPropertyID);
            System.out.println("refTolayerName="+refTolayerName);

            System.out.println("OCBaseState="+OCBaseState+" (ON="+ PdfDictionary.ON+")");


            System.out.println("order="+order);
            System.out.println("ON="+layerDict.getKeyArray(PdfDictionary.ON));
            System.out.println("OFF="+layerDict.getKeyArray(PdfDictionary.OFF));
            System.out.println("RBGroups="+layerDict.getKeyArray(PdfDictionary.RBGroups));
        }
        /**
         * workout list of layers (can be in several places)
         */

        addLayer(OCBaseState, order,null);

        //read the ON and OFF values
        addLayer(PdfDictionary.ON, layerDict.getKeyArray(PdfDictionary.ON));
        addLayer(PdfDictionary.OFF, layerDict.getKeyArray(PdfDictionary.OFF));

        //set any locks
        setLocks(currentPdfFile,layerDict.getKeyArray(PdfDictionary.Locked));

        //any constraints
        setConstraints(layerDict.getKeyArray(PdfDictionary.RBGroups));

        //any Additional Dictionaries
        setAS(layerDict.getKeyArray(PdfDictionary.AS), currentPdfFile);


        /**
         * read any metadata
         */
        int[] keys={PdfDictionary.Name,PdfDictionary.Creator};
        String[] titles={"Name","Creator"};

        int count=keys.length;
        String val;
        for(int jj=0;jj<count;jj++){
            val= layerDict.getTextStreamValue(keys[jj]);
            if(val!=null)
                metaData.put(titles[jj],val);
        }

        //list mode if set
        val=layerDict.getName(PdfDictionary.ListMode);
        if(val!=null)
            metaData.put("ListMode",val);

    }

    /**
     * used by Javascript to flag that state has changed
     * @param flag
     */
    public void setChangesMade(boolean flag) {
        changesMade=flag;
    }

    /**
     * build a list of constraints using layer names so
     *  we can switch off if needed
     * @param layer
     */
    private void setConstraints(byte[][] layer) {

        if(layer ==null)
            return;

        int layerCount = layer.length;

        //turn into list of names
        String[] layers=new String[layerCount];
        for(int ii=0;ii< layerCount;ii++){

            String ref=new String(layer[ii]);
            layers[ii]=(String)this.refTolayerName.get(ref);
        }

        for(int ii=0;ii< layerCount;ii++){

            if(isLayerName(layers[ii])){

                String effectedLayers="";
                for(int ii2=0;ii2< layerCount;ii2++){

                    if(ii==ii2)
                        continue;


                    effectedLayers=effectedLayers+layers[ii2]+",";
                }

                RBconstraints.put(layers[ii],effectedLayers);


            }
        }
    }

    /**
     * create list for lookup
     */
    private void setupOCMaps(PdfObject propertiesObj, PdfObjectReader currentPdfFile) {

        PdfKeyPairsIterator keyPairs=propertiesObj.getKeyPairsIterator();

        String glyphKey,ref;
        PdfObject glyphObj;

        while(keyPairs.hasMorePairs()){

            glyphKey=keyPairs.getNextKeyAsString();

            glyphObj=keyPairs.getNextValueAsDictionary();
            ref=glyphObj.getObjectRefAsString();

            currentPdfFile.checkResolved(glyphObj);

            byte[][] childPairs=glyphObj.getKeyArray(PdfDictionary.OCGs);

            if(childPairs!=null)
                setupchildOCMaps(childPairs, glyphKey,currentPdfFile);
            else{
                propertyMap.put(ref,glyphObj);

                String currentNames=(String) refToPropertyID.get(ref);
                if(currentNames==null)
                    refToPropertyID.put(ref, glyphKey);
                else
                    refToPropertyID.put(ref, currentNames+","+glyphKey);
            }
            //roll on
            keyPairs.nextPair();
        }

    }

    private void setupchildOCMaps(byte[][] keys, String glyphKey, PdfObjectReader currentPdfFile) {

        String ref;
        PdfObject glyphObj;

        int count=keys.length;
        for(int ii=0;ii<count;ii++){

            ref=new String(keys[ii]);
            glyphObj=new OCObject(ref);

            currentPdfFile.readObject(glyphObj);

            currentPdfFile.checkResolved(glyphObj);

            byte[][] childPairs=glyphObj.getKeyArray(PdfDictionary.OCGs);

//System.out.println(glyphKey+" === "+glyphObj+" childPropertiesObj="+childPairs);

            if(childPairs!=null)
                setupchildOCMaps(childPairs, glyphKey, currentPdfFile);
            else{

                propertyMap.put(ref,glyphObj);
                String currentNames=(String) refToPropertyID.get(ref);
                if(currentNames==null)
                    refToPropertyID.put(ref, glyphKey);
                else
                    refToPropertyID.put(ref, currentNames+","+glyphKey);
                //System.out.println("Add key "+glyphKey+" "+refToPropertyID);
            }
        }
    }

    private void addLayer(int status, Object[] layer, String parentName) {

        if(layer ==null)
            return;

        int layers = layer.length;

        String ref,name,layerName=null;

        PdfObject nextObject;

        for(int ii=0;ii< layers;ii++){

            if(layer[ii] instanceof String){
                //ignore
            }else if(layer[ii] instanceof byte[]){

                byte[] rawRef=(byte[])layer[ii];
                ref =new String(rawRef);
                name=(String) refToPropertyID.get(ref);

                nextObject=(PdfObject)propertyMap.get(ref);

                if(nextObject==null && rawRef!=null && rawRef[rawRef.length-1]=='R'){
                    nextObject=new OCObject(ref);
                    currentPdfFile.readObject(nextObject);
                    name=ref;
                }

                if(nextObject!=null){

                    layerCount++;

                    layerName=nextObject.getTextStreamValue(PdfDictionary.Name);

                    if(debug)
                    System.out.println("[layer] add layer="+layerName);

                    refTolayerName.put(ref,layerName);

                    //and write back name value
                    layer[ii]=layerName;

                    //track inheritance
                    if(parentName!=null)
                        isChildOfLayer.put(layerName,parentName);

                    layerNames.put(layerName,new Integer(status));
                    if(name.indexOf(',')==-1){
                        String oldValue=(String)streamToName.get(name);
                        if(oldValue==null)
                            streamToName.put(name,layerName);
                        else
                            streamToName.put(name,oldValue+","+layerName);
                    }else{
                        StringTokenizer names=new StringTokenizer(name,",");
                        while(names.hasMoreTokens()){
                            name=names.nextToken();
                            String oldValue=(String)streamToName.get(name);
                            if(oldValue==null)
                                streamToName.put(name,layerName);
                            else
                                streamToName.put(name,oldValue+","+layerName);
                        }
                    }

                    //must be done as can be defined in order with default and then ON/OFF as well
                    if(status==PdfDictionary.ON)
                        layersEnabled.put(layerName,"x");
                    else
                        layersEnabled.remove(layerName);
                }
            }else
                addLayer(status, (Object[]) layer[ii],layerName);
        }
    }

    private void addLayer(int status, byte[][] layer) {

        if(layer ==null)
            return;

        int layers = layer.length;

        String ref,name;

        PdfObject nextObject;

        for(int ii=0;ii< layers;ii++){

            ref =new String(layer[ii]);
            name=(String) refToPropertyID.get(ref);
            nextObject=(PdfObject)propertyMap.get(ref);

            if(nextObject!=null){

                layerCount++;

                String layerName=nextObject.getTextStreamValue(PdfDictionary.Name);

                if(debug)
                System.out.println("[layer] add layer="+layerName);

                refTolayerName.put(ref,layerName);

                layerNames.put(layerName,new Integer(status));

                if(name.indexOf(',')==-1){
                    String oldValue=(String)streamToName.get(name);
                    if(oldValue==null)
                        streamToName.put(name,layerName);
                    else
                        streamToName.put(name,oldValue+","+layerName);
                }else{
                    StringTokenizer names=new StringTokenizer(name,",");
                    while(names.hasMoreTokens()){
                        name=names.nextToken();
                        String oldValue=(String)streamToName.get(name);
                        if(oldValue==null)
                            streamToName.put(name,layerName);
                        else
                            streamToName.put(name,oldValue+","+layerName);
                    }
                }

                //must be done as can be defined in order with default and then ON/OFF as well
                if(status==PdfDictionary.ON)
                    layersEnabled.put(layerName,"x");
                else
                    layersEnabled.remove(layerName);
            }
        }
    }

    private void setAS(byte[][] AS, PdfObjectReader currentPdfFile) {

        if(AS ==null)
            return;

        int layers = AS.length,event=-1;

        String ref, name,layerName;

        byte[][] OCGs;

        PdfObject nextObject;

        for(int ii=0;ii< layers;ii++){

            ref =new String(AS[ii]);

            nextObject=new OCObject(ref);
            if(AS[ii][0]=='<')
                nextObject.setStatus(PdfObject.UNDECODED_DIRECT);
            else
                nextObject.setStatus(PdfObject.UNDECODED_REF);

            //must be done AFTER setStatus()
            nextObject.setUnresolvedData(AS[ii], PdfDictionary.AS);
            currentPdfFile.checkResolved(nextObject);
            
            event=nextObject.getParameterConstant(PdfDictionary.Event);
            if(nextObject!=null){

                if(event==PdfDictionary.View){
                    OCGs=nextObject.getKeyArray(PdfDictionary.OCGs);

                    if(OCGs!=null){

                        int childCount=OCGs.length;
                        for(int jj=0;jj<childCount;jj++){

                            ref =new String(OCGs[jj]);
                            nextObject=new OCObject(ref);
                            if(OCGs[jj][0]=='<'){
                                nextObject.setStatus(PdfObject.UNDECODED_DIRECT);
                            }else
                                nextObject.setStatus(PdfObject.UNDECODED_REF);

                            //must be done AFTER setStatus()
                            nextObject.setUnresolvedData(OCGs[jj], PdfDictionary.OCGs);
                            currentPdfFile.checkResolved(nextObject);

                            layerName =nextObject.getTextStreamValue(PdfDictionary.Name);
                            name=(String) refToPropertyID.get(ref);
                            
                            streamToName.put(name,layerName);

                            //System.out.println((char)OCGs[jj][0]+" "+ref+" "+" "+nextObject+" "+nextObject.getTextStreamValue(PdfDictionary.Name));
                            
                            PdfObject usageObj=nextObject.getDictionary(PdfDictionary.Usage);

                            if(usageObj!=null){
                                PdfObject zoomObj=usageObj.getDictionary(PdfDictionary.Zoom);

                                //set zoom values
                                if(zoomObj!=null){
                                    float min=zoomObj.getFloatNumber(PdfDictionary.min);
                                    if(min!=0){
                                        minScale.put(layerName,new Float(min));
                                    }
                                    float max=zoomObj.getFloatNumber(PdfDictionary.max);

                                    if(max!=0){
                                        maxScale.put(layerName,new Float(max));
                                    }
                                }
                            }
                        }
                    }
                }else{
                }
                //layerCount++;

                //String layerName=nextObject.getTextStreamValue(PdfDictionary.Name);

                //if(debug)
                //System.out.println("[AS] add AS="+layerName);

                //refTolayerName.put(ref,layerName);

                //layerNames.put(layerName,new Integer(status));

//                if(layerName.indexOf(",")==-1){
//                    String oldValue=(String)streamToName.get(layerName);
//                    if(oldValue==null)
//                        streamToName.put(layerName,layerName);
//                    else
//                        streamToName.put(layerName,oldValue+","+layerName);
//                }else{
//                    StringTokenizer names=new StringTokenizer(layerName,",");
//                    while(names.hasMoreTokens()){
//                        layerName=names.nextToken();
//                        String oldValue=(String)streamToName.get(layerName);
//                        if(oldValue==null)
//                            streamToName.put(layerName,layerName);
//                        else
//                            streamToName.put(layerName,oldValue+","+layerName);
//                    }
//                }


            }
        }
    }

    private void setLocks(PdfObjectReader currentPdfFile, byte[][] layer) {

        if(layer ==null)
            return;

        int layerCount = layer.length;

        for(int ii=0;ii< layerCount;ii++){

            String nextValue=new String(layer[ii]);

            PdfObject nextObject=new OCObject(nextValue);

            currentPdfFile.readObject(nextObject);

            String layerName=nextObject.getTextStreamValue(PdfDictionary.Name);

            layerLocks.put(layerName,"x");

        }
    }

    public Map getMetaData() {
        return metaData;
    }

    public Object[] getDisplayTree(){

        if(order!=null)
            return order;
        else
            return getNames();
    }

    /**
     * return list of layer names as String array
     */
    public String[] getNames() {

        int count=layerNames.size();
        String[] nameList=new String[count];

        Iterator names=layerNames.keySet().iterator();

        int jj=0;
        while(names.hasNext()){
            nameList[jj]=names.next().toString();
            jj++;
        }


        return nameList;
    }

    /**
     * will display only these layers and hide all others and will override
     * any constraints.
     * If you pass null in, all layers will be removed
     * @param layerNames
     */
    public void setVisibleLayers(String[] layerNames) {

    	layersEnabled.clear();

    	if(layerNames!=null){

	    	int count=layerNames.length;
	    	for(int ii=0;ii<count;ii++)
	    		layersEnabled.put(layerNames[ii],"x");
    	}

        //flag it has been altered
        changesMade=true;
    }


    /**
     * Used internally only.
     * takes name in Stream (ie MC7 and works out if we need to decode)  if isID==true
     */
    public boolean decodeLayer(String name, boolean isID) {

        if(layerCount==0)
            return true;

        boolean isLayerVisible =false;

        String layerName=name;

        //see if match found otherwise assume name
        if(isID){
            String mappedName=(String)streamToName.get(name);

            if(mappedName!=null)
            layerName=mappedName;
        }

        if(layerName ==null)
            return false;
        else{

            //if multiple layers  them comma separated list
            if(layerName.indexOf(',')==-1){
                isLayerVisible =layersEnabled.containsKey(layerName);
            }else{
                StringTokenizer names=new StringTokenizer(layerName,",");
                while(names.hasMoreTokens()){
                    isLayerVisible =layersEnabled.containsKey(names.nextToken());

                    if(isLayerVisible) //exit on first match
                    break;
                }
            }

            if(debug)
            System.out.println("[layer] "+name+" decode="+ isLayerVisible +" enabled="+layersEnabled+" layerName="+layerName+" isEnabled="+this.layersEnabled);
            //System.out.println("stream="+streamToName);

            //check not disabled by Parent up tree
            if(isLayerVisible){

                String parent= (String) isChildOfLayer.get(layerName);

                while(parent!=null && isLayerVisible){

                    isLayerVisible=decodeLayer(parent,false);
                    parent= (String) isChildOfLayer.get(parent);

                }

            }
            return isLayerVisible;
        }
    }

    /**
     * switch on/off layers based on Zoom
     * @param scaling
     */
    public boolean setZoom(float scaling) {

        String layerName="";
        Iterator minZoomLayers=minScale.keySet().iterator();
        while(minZoomLayers.hasNext()){

            layerName=(String) minZoomLayers.next();
            Float minScalingValue= (Float) minScale.get(layerName);

            //Zoom off
            if(minScalingValue!=null){

                System.out.println(layerName+" "+scaling+" "+minScalingValue);

                if(scaling<minScalingValue.floatValue()){
                    layersEnabled.remove(layerName);
                    changesMade=true;
                }else if(!layersEnabled.containsKey(layerName)){
                    layersEnabled.put(layerName,"x");
                    changesMade=true;
                }
            }
        }


        Iterator maxZoomLayers=maxScale.keySet().iterator();
        while(maxZoomLayers.hasNext()){

            layerName=(String) minZoomLayers.next();
            Float maxScalingValue= (Float) maxScale.get(layerName);
            if(maxScalingValue!=null){
                if(scaling>maxScalingValue.floatValue()){
                    layersEnabled.remove(layerName);
                    changesMade=true;
                }else if(!layersEnabled.containsKey(layerName)){
                    layersEnabled.put(layerName,"x");
                    changesMade=true;
                }
            }
        }

        return changesMade;
    }

    public boolean isVisible(String layerName) {

        return layersEnabled.containsKey(layerName);
    }

    public void setVisiblity(String layerName, boolean isVisible) {

        if(debug)
        System.out.println("[layer] setVisiblity="+layerName+" isVisible="+isVisible);

        if(isVisible){
            layersEnabled.put(layerName,"x");

            //disable any other layers
            String layersToDisable=(String)RBconstraints.get(layerName);
            if(layersToDisable!=null){
                StringTokenizer layers=new StringTokenizer(layersToDisable,",");
                while(layers.hasMoreTokens())
                    layersEnabled.remove(layers.nextToken());
            }
        }else
            layersEnabled.remove(layerName);

        //flag it has been altered
        changesMade=true;
    }

    public boolean isLocked(String layerName) {
        
        return layerLocks.containsKey(layerName);  //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * show if decoded version  match visibility flags which can be altered by user
     */
    public boolean getChangesMade() {
        return changesMade;
    }

    /**
     * show if is name of layer (as opposed to just label)
     */
    public boolean isLayerName(String name) {
        return layerNames.containsKey(name);
    }

    /**
     * number of layers setup
     */
    public int getLayersCount() {
        return layerCount;
    }

    public String getNameFromRef(String ref) {
        return (String) refTolayerName.get(ref);
    }

    public void setScaling(float scaling) {
        this.scaling=scaling;
    }


    /**JS returns all the OCG objects in the document. */
	public Object[] getOCGs(){ return getOCGs(-1); }

	/**JS
	 * Gets an array of OCG objects found on a specified page.
	 *
	 * @param page - (optional) The 0-based page number. If not specified, all the OCGs found in the document are returned.
	 * If no argument is passed, returns all OCGs listed in alphabetical order, by name. If nPage is passed, this method returns the OCGs for that page, in the order they were created.
	 * @return - An array of OCG objects or null if no OCGs are present.
	 */
	public Object[] getOCGs(int page){

        int count=layerNames.size();

        //create array of values with access to this so we can reset
        Layer[] layers=new Layer[count];

        Iterator layersIt=layerNames.keySet().iterator();
        int ii=0;
        String name="";
        while(layersIt.hasNext()){
            name=(String)layersIt.next();
            
            layers[ii]=new Layer(name,this);
            ii++;
        }
		
		return layers;
	}


}
