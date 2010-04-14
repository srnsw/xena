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
* Configuration.java
* ---------------
*/
package org.jpedal.storypad;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jpedal.gui.ShowGUIMessage;

/**
 * generic configuration 
 */
public class Configuration {

    /**path to config dir*/
    protected String configDir="";

    /**
     * actual name of section
     */
    protected String sectionName="general";

    boolean debug=false;

    /**
     * holds general values
     */
    Map values=new HashMap();

    static private boolean flagged=false;

    public Configuration(){}

    /**initialise category and load or create config file*/
    public Configuration(String configDir){

       debug=System.getProperty("dev")!=null && System.getProperty("dev").toLowerCase().equals("true");

        /**loadConfig if present*/
        boolean fileExists=loadValues(configDir);

        if(!fileExists)
            saveValues();

        loadValues(configDir);

        //alter for dev version
        if(debug){

            if(!flagged){
                flagged=true;
                System.out.println("{WARNING} Hard-coded values on DEV version");
            }

            values.remove("targetTXTOutputDir");
            values.remove("custom_output");
            
        }
    }

    /**
     * write out a config file
     */
    public boolean saveValues() {

        //if(debug)
        //	throw new RuntimeException("NOT ALLOWED IN DEV VERSION");
        

        try{
            //create doc and set root
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Node root=doc.createElement(sectionName);

            //add comments
            Node creation=doc.createComment("Created "+org.jpedal.utils.TimeNow.getShortTimeNow());

            doc.appendChild(creation);

            doc.appendChild(root);

            //write out values
            Iterator keys=this.getKeysAsOrderedList().iterator();
            while(keys.hasNext()){
                String currentKey=(String) keys.next();
                Element section=doc.createElement(currentKey);

                //add values
                section.setAttribute("value", String.valueOf(values.get(currentKey)));
                root.appendChild(section);

            }

            String fileName=configDir+sectionName+".xml";

            //write out
            //use System.out for FileOutputStream to see on screen
            InputStream stylesheet = this.getClass().getResourceAsStream("/res/xmlstyle.xslt");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(new StreamSource(stylesheet));
            transformer.transform(new DOMSource(doc), new StreamResult(fileName));


            System.out.println("Created "+fileName);

           // ShowGUIMessage.showGUIMessage("Saved config", "Configuration saved to "+fileName);

        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }

    /**
     * see if xml file exists and load it or return false
     */
    protected boolean loadValues(String configDir) {

        this.configDir=configDir;

        //make sure the config dir exists
        File testDirExists=new File(configDir);
        if(!testDirExists.exists())
            testDirExists.mkdir();

        
        boolean isLoaded=false;

        /**see if exists and load*/
        String fileName=configDir+sectionName+".xml";
        //System.out.println("Loading config from "+fileName);
        File testForFile=new File(fileName);
        if(testForFile.exists()){

            try{
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                Document doc = factory.newDocumentBuilder().parse(new File(configDir+sectionName+".xml"));

                /**
                 * get the values and extract info
                 */
                NodeList Allnodes=doc.getChildNodes();
                NodeList nodes=doc.getElementsByTagName(sectionName);
                Element currentElement = (Element) nodes.item(0);
                NodeList catNodes=currentElement.getChildNodes();

                List catValues = getChildValues(catNodes);

                /**use to set values*/
                int size=catValues.size();

                for(int i=0;i<size;i++){
                    Element next=(Element) catValues.get(i);
                    String key=next.getNodeName();
                    String value=next.getAttribute("value");
                    this.setValue(key,value);
                }

                isLoaded=true;

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return isLoaded;
    }

    /**
     * @return keys as an ordered list
     */
    public List getKeysAsOrderedList() {

        List ret = Arrays.asList(values.keySet().toArray());
        Collections.sort(ret);

        return ret;
    }

    /**
     * @param catNodes
     */
    protected static List getChildValues(NodeList catNodes) {
        /**
         * get all valid elements into list
         */
        List catValues=new ArrayList();
        int items=catNodes.getLength();
        for(int i=0;i<items;i++){
            Node next=catNodes.item(i);

            if(next instanceof Element)
                catValues.add(next);
        }
        return catValues;
    }

    /**
     * Returns the value stored internally
     */
    public String getValue(String key) {
        return (String) values.get(key);
    }

    /**
     * set internally stored value
     */
    public void setValue(String key,String value) {
        values.put(key,value);
    }
    
    /**
     * Get Config Directory
     */
    public String getConfig() {
       return configDir;
    }
}
