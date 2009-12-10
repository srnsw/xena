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
* PropertiesFile.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.io.ObjectStore;
import org.jpedal.gui.ShowGUIMessage;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
                                      
/**holds values stored in XML file on disk*/
public class PropertiesFile {
	
	private String separator=System.getProperty( "file.separator" );
	private String userDir=System.getProperty("user.dir");
	private String configFile=userDir+separator+".properties.xml";

	private boolean isTest=false;
	
	private boolean refactorProperties = false;
	
	private Document doc;
	
	private int noOfRecentDocs = 6;
	
	private String[] properties = {
			"showfirsttimepopup", "false",
			"showrhinomessage", "false",
			"showsaveformsmessage", "true",
			"showitextmessage", "true",
			"showddmessage", "true",
			"searchWindowType", "0",
			"borderType", "1",
			"useHiResPrinting", "true",
			"showDownloadWindow", "true",
			"DPI", "110",
			"autoScroll", "true",
			"pageMode", "1",
			"displaytipsonstartup", "false",
			"automaticupdate", "true",
			"currentversion", PdfDecoder.version,
			"showtiffmessage", "true",
			"maxmultiviewers", "20",
			"MenuBarMenu", "true",
			"FileMenu", "true",
			"OpenMenu", "true",
			"Open", "true",
			"Openurl", "true",
			"ENDCHILDREN",
			"Save", "true",
			"Resaveasforms", "true",
			"Find", "true",
			"Documentproperties", "true",
			"Print", "true",
			"Recentdocuments", "true",
			"Exit", "true",
			"ENDCHILDREN",
			"EditMenu", "true",
			"Copy", "true",
			"Selectall", "true",
			"Deselectall", "true",
			"Preferences", "true",
			"ENDCHILDREN",
			"ViewMenu", "true",
			"GotoMenu", "true",
			"Firstpage", "true",
			"Backpage", "true",
			"Forwardpage", "true",
			"Lastpage", "true",
			"Goto", "true",
			"Previousdocument", "true",
			"Nextdocument", "true",
			"ENDCHILDREN",
			"PagelayoutMenu", "true",
			"Single", "true",
			"Continuous", "true",
			"Facing", "true",
			"Continuousfacing", "true",
			"SideScroll", "true",
			"ENDCHILDREN",
			"Fullscreen", "true",
			"ENDCHILDREN",
			"WindowMenu", "true",
			"Cascade", "true",
			"Tile", "true",
			"ENDCHILDREN",
			"ExportMenu", "true",
			"PdfMenu", "true",
			"Oneperpage", "true",
			"Nup", "true",
			"Handouts", "true",
			"ENDCHILDREN",
			"ContentMenu", "true",
			"Images", "true",
			"Text", "true",
			"ENDCHILDREN",
			"Bitmap", "true",
			"ENDCHILDREN",
			"PagetoolsMenu", "true",
			"Rotatepages", "true",
			"Deletepages", "true",
			"Addpage", "true",
			"Addheaderfooter", "true",
			"Stamptext", "true",
			"Stampimage", "false",
			"Crop", "true",
			"ENDCHILDREN",
			"HelpMenu", "true",
			"Visitwebsite", "true",
			"Tipoftheday", "true",
			"Checkupdates", "true",
			"About", "true",
			"ENDCHILDREN",
			"ENDCHILDREN",
			"ButtonsMenu", "true",
			"Openfilebutton", "true",
			"Printbutton", "true",
			"Searchbutton", "true",
			"Propertiesbutton", "true",
			"Aboutbutton", "true",
			"Snapshotbutton", "true",
			"CursorButton", "true",
			"ENDCHILDREN",
			"DisplayOptionsMenu", "true",
			"Scalingdisplay", "true",
			"Rotationdisplay", "true",
			"Imageopdisplay", "true",
			"Progressdisplay", "true",
			"ENDCHILDREN",
			"NavigationBarMenu", "true",
			"Memorybottom", "true",
			"Firstbottom", "true",
			"Back10bottom", "true",
			"Backbottom", "true",
			"Gotobottom", "true",
			"Forwardbottom", "true",
			"Forward10bottom", "true",
			"Lastbottom", "true",
			"Singlebottom", "true",
			"Continuousbottom", "true",
			"Continuousfacingbottom", "true",
			"Facingbottom", "true",
			"SideScrollbottom", "true",
			"ENDCHILDREN",
			"SideTabBarMenu", "true",
			"Pagetab", "true",
			"Bookmarkstab", "true",
			"Layerstab", "true",
			"Signaturestab", "true",
			"ENDCHILDREN",
			"ShowMenubar", "true",
			"ShowButtons", "true",
			"ShowDisplayoptions", "true",
			"ShowNavigationbar", "true",
			"ShowSidetabbar", "true",
			"highlightBoxColor","-16777216",
			"highlightTextColor","16750900",
			"highlightComposite","0.35",
			"invertHighlights","false",
			"openLastDocument","false",
			"lastDocumentPage","1",
			"ENDCHILDREN"
	};
	
	public PropertiesFile(){
		
	}
	
	public PropertiesFile(String config){
		configFile = config;
	}
	
	/**
	 * Please use loadProperties()
	 * @param configFile
	 * @deprecated 
	 */
	public void setupProperties(){
		loadProperties();
	}
	
	public void loadProperties(){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			File config = new File(configFile);
			
			if(config.exists() && config.length()>0){
				try{
                    doc =  db.parse(config);
				}catch(Exception e){
					doc =  db.newDocument();
				}
			}else
				doc =  db.newDocument();
			
			boolean hasAllElements=checkAllElementsPresent();
			
			if(refactorProperties){
				//Reset to start of properties file
				position = 0;
				
				//Delete old config file
				config.delete();
				//config.createNewFile();
				
				
				Document oldDoc =  (Document)doc.cloneNode(true);
				doc =  db.newDocument();
				checkAllElementsPresent();
				
				/**
				 * Move RecentFiles List Over to new properties
				 */
				//New Properties
				NodeList newRecentFiles =doc.getElementsByTagName("recentfiles");
				Element newRecentRoot=(Element) newRecentFiles.item(0);
				
				//Old Properties
				NodeList oldRecentFiles =oldDoc.getElementsByTagName("recentfiles");
				Element oldRecentRoot=(Element) oldRecentFiles.item(0);
				
				//Get children elements
				NodeList children = oldRecentRoot.getChildNodes();
				for(int i=0; i!=children.getLength(); i++){
					if(!children.item(i).getNodeName().equals("#text")){//Ignore this element
						Element e = doc.createElement("file");
						e.setAttribute("name", ((Element)children.item(i)).getAttribute("name"));
						newRecentRoot.appendChild(e);
					}
				}		        

				for(int i=0; i!=properties.length; i++){
					if(!properties[i].equals("ENDCHILDREN")){
						NodeList nl =doc.getElementsByTagName(properties[i]);
						Element element=(Element) nl.item(0);
						if(element==null){
							ShowGUIMessage.showGUIMessage("The property "+properties[i]+" was either not found in the properties file.", "Property not found.");
						}else{
							NodeList l = oldDoc.getElementsByTagName(properties[i]);
							Element el = (Element)l.item(0);
							if(el!=null)
								element.setAttribute("value",el.getAttribute("value"));
						}
						i++;
					}
				}
				
				if(!isTest){
					writeDoc();
				}
				
			}
			
            //only write out if needed
            if(!hasAllElements)
            	writeDoc();
            
		
            config=null;
    		
		}catch(Exception e){
			LogWriter.writeLog("Exception " + e + " generating properties file");
		}
		
		
	}

	public void removeRecentDocuments() {
		// remove docs from list
		Element recentDocsElement = (Element) doc.getElementsByTagName("recentfiles").item(0);
		NodeList childNodes = recentDocsElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			recentDocsElement.removeChild(childNodes.item(i));
		}
	}
	
	public String[] getRecentDocuments(){
		String[] recentDocuments=new String[noOfRecentDocs];
		
		try{
			NodeList nl =doc.getElementsByTagName("recentfiles");
			List fileNames = new ArrayList();
			
			if(nl != null && nl.getLength() > 0) {
				NodeList allRecentDocs = ((Element) nl.item(0)).getElementsByTagName("*");
				
				for(int i=0;i<allRecentDocs.getLength();i++){
					Node item = allRecentDocs.item(i);
					NamedNodeMap attrs = item.getAttributes();
					fileNames.add(attrs.getNamedItem("name").getNodeValue());
				}
			}
			
			//prune unwanted entries
			while(fileNames.size() > noOfRecentDocs){
				fileNames.remove(0);
			}
			
			Collections.reverse(fileNames);
			
			recentDocuments = (String[]) fileNames.toArray(new String[noOfRecentDocs]);
		}catch(Exception e){
			LogWriter.writeLog("Exception " + e + " getting recent documents");
        	return null;
		}
		
		return recentDocuments;
	}
	
	public void addRecentDocument(String file){
		try{
			Element recentElement = (Element) doc.getElementsByTagName("recentfiles").item(0);
			
			checkExists(file, recentElement);
			
			Element elementToAdd=doc.createElement("file");
			elementToAdd.setAttribute("name",file);
			
			recentElement.appendChild(elementToAdd);
			
			removeOldFiles(recentElement);
			
			//writeDoc();
		}catch(Exception e){
			LogWriter.writeLog("Exception " + e + " adding recent document to properties file");
		}
	}
	
	public void setValue(String elementName, String newValue) {
		
		try {
			NodeList nl =doc.getElementsByTagName(elementName);
			Element element=(Element) nl.item(0);
			if(element==null || newValue==null){
				ShowGUIMessage.showGUIMessage("The property "+elementName+" was either not found in the properties file or the value "+newValue+" was not set.", "Property not found.");
			}else{
				element.setAttribute("value",newValue);
			}
			
			
			writeDoc();
		}catch(Exception e){
			LogWriter.writeLog("Exception " + e + " setting value in properties file");
		}
	}

    public String getGUIValue(String elementName){
        String menu=getValue(elementName);
        if(menu.length()==0)
            return "false";
        else
            return menu.toLowerCase();


    }
    
    public NodeList getChildren(String item){
    	return ((Element)doc.getElementsByTagName(item).item(0)).getChildNodes();
    }
    
    public String getValue(String elementName){
		NamedNodeMap attrs = null;
		try {
			NodeList nl =doc.getElementsByTagName(elementName);
			Element element=(Element) nl.item(0);
			if(element==null)
				return "";
			attrs = element.getAttributes();
			
		}catch(Exception e){
			LogWriter.writeLog("Exception " + e + " generating properties file");
			return "";
		}
		
		return attrs.getNamedItem("value").getNodeValue();
	}
	
	private void removeOldFiles(Element recentElement) throws Exception{
		NodeList allRecentDocs = recentElement.getElementsByTagName("*");
		
		while(allRecentDocs.getLength() > noOfRecentDocs){
			recentElement.removeChild(allRecentDocs.item(0));
		}	
	}
	
	private static void checkExists(String file, Element recentElement) throws Exception{
		NodeList allRecentDocs = recentElement.getElementsByTagName("*");
		
		for(int i=0;i<allRecentDocs.getLength();i++){
			Node item = allRecentDocs.item(i);
			NamedNodeMap attrs = item.getAttributes();
			String value = attrs.getNamedItem("name").getNodeValue();
			
			if(value.equals(file))
				recentElement.removeChild(item);
		}
	}

    //
    public void writeDoc() throws Exception{
		
		InputStream stylesheet = this.getClass().getResourceAsStream("/org/jpedal/examples/simpleviewer/res/xmlstyle.xslt");
	
		StreamResult str=new StreamResult(configFile);
		StreamSource ss=new StreamSource(stylesheet);
		DOMSource dom= new DOMSource(doc);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer(ss);
		transformer.transform(dom, str);
		
		transformer=null;
		stylesheet.close();
		if(ss!=null)
		ss.getInputStream().close();
		//if(str!=null)
		//str.getOutputStream().close();
		ss=null;
		str=null;
		dom=null;
		

	}
    
    public void dispose(){
    	
    	
    	doc=null;
    	properties=null;
    	this.configFile=null;
    	
    }
	
	private boolean checkAllElementsPresent() throws Exception{
		
        //assume true and set to false if wrong
        boolean hasAllElements=true;

        NodeList allElements = doc.getElementsByTagName("*");
		List elementsInTree=new ArrayList(allElements.getLength());
		
		for(int i=0;i<allElements.getLength();i++)
			elementsInTree.add(allElements.item(i).getNodeName());
		
		Element propertiesElement = null;
		
		if(elementsInTree.contains("properties")){
			propertiesElement = (Element) doc.getElementsByTagName("properties").item(0);
		}else{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			doc =  db.newDocument();	
			
			propertiesElement = doc.createElement("properties");
			doc.appendChild(propertiesElement);
			
			allElements = doc.getElementsByTagName("*");
			elementsInTree=new ArrayList(allElements.getLength());
			
			for(int i=0;i<allElements.getLength();i++)
				elementsInTree.add(allElements.item(i).getNodeName());

            hasAllElements=false;
        }
		
		if(!elementsInTree.contains("recentfiles")){
			Element recent = doc.createElement("recentfiles");
			propertiesElement.appendChild(recent);

            hasAllElements=false;
        }
		
		hasAllElements = addProperties(elementsInTree, propertiesElement);
		
        return hasAllElements;
    }
	
	//Keep track of position in the properties array
	int position = 0;
	
	private boolean addMenuElement(List tree, Element menu){
		boolean hasAllElements = true;
		
		//System.out.println("MENU == "+properties[position]);
		
		if(!tree.contains(properties[position])){
			Element property = doc.createElement(properties[position]);
			
			//Increment to property value
			position++;

			property.setAttribute("value",properties[position]);
			menu.appendChild(property);
			
			//update position in array
			position++;
			
			//Start on children of menu
			addProperties(tree, property);
			
			hasAllElements=false;
		}else{ //Increment passed value to next property
			Element property = (Element) doc.getElementsByTagName(properties[position]).item(0);
			position++;
			position++;
			addProperties(tree, property);
		}
		
		return hasAllElements;
	}
	private boolean addChildElements(List tree, Element menu){
		boolean hasAllElements = true;
		
		//System.out.println("Child == "+properties[position]);
		//Not at the end of the children so keep adding
		if(!properties[position].equals("ENDCHILDREN")){
			if(!tree.contains(properties[position])){
//				if(properties[i].equals("currentversion")){
//					refactorProperties  = true;
//				}
				
				Element property = doc.createElement(properties[position]);

				//Increment to property value
				position++;

				property.setAttribute("value",properties[position]);
				menu.appendChild(property);

				hasAllElements=false;
			}else{
				
				//Check version number for refactoring and updating
				if(properties[position].equals("currentversion")){
					
					//Get store value for the current version
					NodeList nl =doc.getElementsByTagName(properties[position]);
					Element element=(Element) nl.item(0);
					
					if(element==null){
						//Element not found in tree, should never happen
						ShowGUIMessage.showGUIMessage("The property "+properties[position]+" was either not found in the properties file.", "Property not found.");
					}else{
						
						//Is it running in the IDE
						if(properties[position+1].equals("3.83b38")){
							//Do nothing as we are in the IDE
							//Refactor for testing purposes
							//refactorProperties  = true;
//							isTest=true;
							
						}else{//Check versions of released jar
							
							//Program Version
							float progVersion = Float.parseFloat(PdfDecoder.version.substring(0, 4));

							//Ensure properties is a valid value
							String propVer = "0";
							String version = element.getAttribute("value");
							if(version.length()>3)
								propVer = version.substring(0, 4);

							//Properties Version
							float propVersion = Float.parseFloat(propVer);

							//compare version, only update on newer version
							if(progVersion>propVersion){
								element.setAttribute("value", PdfDecoder.version);
								refactorProperties  = true;
							}
						}
					}
				}
				
				 //Increment passed value to next property
				position++;
			}
		}else{
			endMenu = true;
		}
		position++;
		return hasAllElements;
	}
	
	private boolean endMenu = false;
	
	private boolean addProperties(List tree, Element menu){
		boolean hasAllElements=true;
		
		while(position<properties.length){
			boolean value = true;
			//Add menu to properties
			if(properties[position].endsWith("Menu")){
				value = addMenuElement(tree, menu);
			}else{
				value = addChildElements(tree, menu);
				if(endMenu){
					endMenu=false;
					return hasAllElements;
				}
			}
			
			if(!value)
				hasAllElements = false;
		}
		return hasAllElements;
	}
	
	public int getNoRecentDocumentsToDisplay() {
		return this.noOfRecentDocs;
	}

	public String getConfigFile() {
		return configFile;
	}
	
	/**
	 * Please use loadProperties(String configFile)
	 * @param configFile
	 * @deprecated 
	 */
	public void setConfigFile(String configFile){
		loadProperties(configFile);
	}
	
	public void loadProperties(String configFile) {
		
		if(configFile.startsWith("jar:")){
			configFile = configFile.substring(4);

			InputStream is = this.getClass().getResourceAsStream(configFile);
			if(is!=null){
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				try {
					File con = File.createTempFile("config", ".xml",new File(ObjectStore.temp_dir));
					FileOutputStream fos = new FileOutputStream(con);
					OutputStreamWriter osw=new OutputStreamWriter(fos); // read bytes and thus output bytes
					while(br.ready()){
						osw.write(br.read());
					}
					
					//Close input streams and reader
					is.close();
					br.close();
					isr.close();
					
					//Flush the output writers
					osw.flush();
					fos.flush();
					
					//Close the output writers
					osw.close();
					fos.close();
					
					this.configFile = con.getAbsolutePath();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				throw new RuntimeException();
			}
		}else{
			File p = new File(configFile);
			if(p.exists()){
				this.configFile = configFile;
			}else{
				throw new RuntimeException();
			}
		}

		loadProperties();
	}
}
