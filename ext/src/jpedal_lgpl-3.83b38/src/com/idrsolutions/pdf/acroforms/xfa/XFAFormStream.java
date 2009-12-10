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

  * XFAFormStream.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package com.idrsolutions.pdf.acroforms.xfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.acroforms.utils.ConvertToString;
import org.jpedal.objects.raw.*;

import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/**
 *         XFA version to turn XFA data into Form Object
 */
public class XFAFormStream extends FormStream {

    public static boolean showBug=false;

    private static final boolean showmethods = false;

    private boolean calledOnce = false;
    private static final boolean debugXFAstream = false;

	private static final boolean useNewCode = false;
	private static final boolean showXFAdata = false;

    private Node config;
    private Node dataset;
    private Node template;

	/**used for the current formObject and has methods to help generate the field*/
    private XFAFormObject formObject;

	/** stores the array of components as they are read and setup */
    private LinkedList xfaFormList;

    private Node[] nodes;

	/** used to store the page number for each field */
    private String pagenum;

    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;

    /** node name to value map */
    private Map valueMap = new HashMap();

    public XFAFormStream(PdfObject acroFormObj, PdfObjectReader inCurrentPdfFile) {

        currentPdfFile = inCurrentPdfFile;

        readXFA(acroFormObj);

    }

    private Node toDocument(int type,byte[] xmlString){
//		NodeList nodes;
//		Element currentElement;


		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document content = null;
		try{
			content = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlString));

//			/**
//			 * get the print values and extract info
//			 */
//			nodes = doc.getElementsByTagName("print");
//
//			currentElement = (Element) nodes.item(0);
		}catch(Exception e){
			content = null;
		}

		/**
		 * SHOW Data
		 */
		if(showXFAdata){
			if(type==PdfDictionary.XFA_TEMPLATE){
			   System.out.println("xfaTemplate=================");
			}else  if(type==PdfDictionary.XFA_DATASET){
				System.out.println("XFA_DATASET=================");
			}else  if(type==PdfDictionary.XFA_CONFIG){
				System.out.println("xfaConfig=================");
			}


			InputStream stylesheet = this.getClass().getResourceAsStream("/org/jpedal/examples/text/xmlstyle.xslt");

			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			/**output tree*/
			try {
				Transformer transformer = transformerFactory.newTransformer(new StreamSource(stylesheet));

				//useful for debugging
				transformer.transform(new DOMSource(content), new StreamResult(System.out));

				System.out.println("/n==========================================");

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return content;

	}

    private void readXFA(PdfObject acroFormObj){

    	/** flag if XFA */
    	PdfObject XFAasStream=null;
    	PdfArrayIterator XFAasArray = null;
    	XFAasStream=acroFormObj.getDictionary(PdfDictionary.XFA);
    	if(XFAasStream==null){
    		XFAasArray=acroFormObj.getMixedArray(PdfDictionary.XFA);

    		//empty array
    		if(XFAasArray!=null && XFAasArray.getTokenCount()==0)
    			XFAasArray=null;
    	}

    	/** decide if ref to object or list of objects */
    	if(XFAasStream!=null){

    		byte[] decodedStream = XFAasStream.getDecodedStream();

    		if(debug) {
    			config = xmlToNode(PdfDictionary.XFA_CONFIG, decodedStream);
    			System.out.println("\nConfig");
    			System.out.println("config length = "+config.getChildNodes().getLength());
    			printNode(config);
    		}

    		if(debug) {
    			dataset = xmlToNode(PdfDictionary.XFA_DATASET, decodedStream);
    			System.out.println("\n\ndataset");
    			System.out.println("dataset length = "+dataset.getChildNodes().getLength());
    			printNode(dataset);
    		}

    		template = xmlToNode(PdfDictionary.XFA_TEMPLATE, decodedStream);

    		if(debug) {
    			System.out.println("\n\ntemplate");
    			System.out.println("template length = "+template.getChildNodes().getLength());
    			printNode(template);
    		}

    	}else{

    		/**
    		 * read XFA values
    		 */
    		PdfObject obj=null;
    		int type=0;

    		while (XFAasArray.hasMoreTokens()) {

    			type=XFAasArray.getNextValueAsConstant(true);

    			obj=new StreamObject(XFAasArray.getNextValueAsString(true));

    			currentPdfFile.readObject(obj);

    			byte[] objData=obj.getDecodedStream();

    			switch(type){

    			case PdfDictionary.XFA_CONFIG:
    				config=toDocument(PdfDictionary.XFA_CONFIG,objData);
    				break;

    			case PdfDictionary.XFA_DATASET:
    				dataset=toDocument(PdfDictionary.XFA_DATASET,objData);
    				break;

    			case PdfDictionary.XFA_TEMPLATE:
    				template=toDocument(PdfDictionary.XFA_TEMPLATE,objData);
    				break;

    			case PdfDictionary.XFA_PREAMBLE:
    				//currentAcroFormData.setXFAFormData(PdfFormData.XFA_PREAMBLE,objData);
    				break;

    			case PdfDictionary.XFA_LOCALESET:
    				//currentAcroFormData.setXFAFormData(PdfFormData.XFA_LOCALESET,objData);
    				break;

    			case PdfDictionary.XFA_PDFSECURITY:
    				//currentAcroFormData.setXFAFormData(PdfFormData.XFA_PDFSECURITY,objData);
    				break;

    			case PdfDictionary.XFA_XMPMETA:
    				//currentAcroFormData.setXFAFormData(PdfFormData.XFA_XMPMETA,objData);
    				break;

    			case PdfDictionary.XFA_XFDF:
    				//currentAcroFormData.setXFAFormData(PdfFormData.XFA_XFDF,objData);
    				break;

    			case PdfDictionary.XFA_POSTAMBLE:
    				//currentAcroFormData.setXFAFormData(PdfFormData.XFA_POSTAMBLE,objData);
    				break;

    			default:
    				// System.out.println("type="+type+" str="+str+" "+new String(objData));
    				break;
    			}
    		}
    	}
    }

	private void printNode(Node node) throws TransformerFactoryConfigurationError {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.newDocument();
			
			Node newRoot = document.importNode(node, true);
			document.appendChild(newRoot);
			
			printXMLDocument(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private Node xmlToNode(int xfaConfig, byte[] decodedStream) {
		
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(decodedStream));
		} catch (Exception e) {
			document = null;
		}
		
		switch (xfaConfig) {
			case PdfDictionary.XFA_CONFIG: {
				
				NodeList nodes = document.getElementsByTagName("config");
				
				
				for (int i = 0; i < nodes.getLength(); i++) {
					Element element = (Element) nodes.item(i);
					String att = element.getAttribute("xmlns");
					
					if(att.length() > 0) {
//						try {
//							
//							DocumentBuilderFactory f1 = DocumentBuilderFactory.newInstance();
//							Document d1 = f1.newDocumentBuilder().newDocument();
//							//document.removeChild(element);
//
//							Node clone = element.cloneNode(true);
//
//							System.out.println(clone.getChildNodes().getLength());
//							
//							Element root = d1.createElement("Root");
//							d1.appendChild(root);
//							
//							d1.insertBefore(newChild, refChild)
//
//							System.out.println("xml =====");
//							printXML(d1);
//							System.out.println("\n\nxml =====");
//							
//
//
//							//return d1;
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
						
//						config = element;
						
						return element;
						
//						System.out.println("config = "+config);
//						System.out.println("element = "+element);
//						
//						System.out.println("length = "+element.getChildNodes().getLength());
//						System.out.println("config length = "+config.getChildNodes().getLength());
						
					}
					
					
				}
				
				break;
			} case PdfDictionary.XFA_DATASET: {
				//xfa:datasets
				
				NodeList nodes = document.getElementsByTagName("xfa:datasets");
				
				Element element = (Element) nodes.item(0);
						
				return element;
				
				//break;
			} case PdfDictionary.XFA_TEMPLATE: {
				
				NodeList nodes = document.getElementsByTagName("template");
				
				for (int i = 0; i < nodes.getLength(); i++) {
					Element element = (Element) nodes.item(i);
					String att = element.getAttribute("xmlns:xfa");
					
					if(att.length() > 0) {
//						DocumentBuilderFactory f1 = DocumentBuilderFactory.newInstance();
//						
//						try {
//							Document d1 = f1.newDocumentBuilder().newDocument();
//							d1.importNode(element, true);
//							
//							return d1;
//						} catch (ParserConfigurationException e) {
//							e.printStackTrace();
//						}
						
						//template = element;
						
						return element;
					}
				}
				
				break;
			}
		}
		
		return null;
	}

	private void printXMLDocument(Document d1)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {
		InputStream stylesheet = this.getClass().getResourceAsStream(
				"/org/jpedal/examples/text/xmlstyle.xslt");

		TransformerFactory transformerFactory = TransformerFactory.newInstance();

		Transformer transformer = transformerFactory.newTransformer(new StreamSource(stylesheet));

		// useful for debugging
		transformer.transform(new DOMSource(d1), new StreamResult(System.out));
	}

    public FormObject[] createAppearanceString(FormObject[] forms) {

        if (!calledOnce) {
            calledOnce = true;
        } else {
            return null;
        }
        xfaFormList = new LinkedList();

        //create Raw XFA objects
        parseStream();

        /**
         * mix in with Form Objects
         *
         * matchedforms[i][0] contains original
         * matchedforms[i][1] contains XFA version 
         */

        int xfaListSize = xfaFormList.size();
        int listSize = forms.length < xfaListSize ? forms.length : xfaListSize;
        FormObject[][] matchedforms = new FormObject[listSize][2];
        XFAFormObject xfaForm;
        int i;
        String formName;
        for (i = 0; i < listSize; i++) {
            matchedforms[i][0] = forms[i];
			//System.out.println("annot="+forms[i].getFieldName()+"<");
            if (forms[i] != null) {
                formName = forms[i].getTextStreamValue(PdfDictionary.T);

                if (formName != null) {
                    int index2 = formName.lastIndexOf("[0]");
                    int index1 = formName.lastIndexOf('.', index2-1);
                    if (index1 != -1) {
                        if (index2 != -1) {
                            formName = formName.substring(index1 + 1, index2);
                        } else {
                            formName = formName.substring(index1 + 1);
                        }
                    } else {
                        if (index2 != -1) {
                            formName = formName.substring(0, index2);
                        } else {
                            //do NOT alter NOT needed.
                        }
                    }
                }
            } else {
                formName = null;
            }

            for (int k = 0; k < xfaListSize; k++) {
                xfaForm = (XFAFormObject) xfaFormList.get(k);
//				System.out.println("xfa="+xfaForm);
                if (xfaForm != null) {
//System.out.println("xfa="+xfaForm.getFieldName()+" formname="+formName);
                	String xfaName = xfaForm.getTextStreamValue(PdfDictionary.T);
                    if (xfaName.equals(formName)) {
                        matchedforms[i][1] = xfaForm;
                        break;
                    }
                }
            }

            if(formName!=null){
            	String newVal = (String) valueMap.get(formName.toLowerCase());

	            if(newVal!=null)
	            	matchedforms[i][0].setTextValue(newVal);
            }
        }

        //use either original or new version in which case add in data
        forms = new FormObject[listSize];
        for (i = 0; i < listSize; i++) {
            if (matchedforms[i][1] != null) {
                matchedforms[i][1].overwriteWith(matchedforms[i][0]);
                forms[i] = matchedforms[i][1];
                if(XFAFormStream.showBug)
                System.out.println("wrong "+i);
            } else {
                forms[i] = matchedforms[i][0];
                if(XFAFormStream.showBug)
                System.out.println("correct "+i);
            }
            if(XFAFormStream.showBug)
            System.out.println(i+" "+forms[i].getTextString());
        }
        return forms;
    }

    protected void parseStream() {
    	
    	if(useNewCode){
    		setupTemplate2(template);
    	}else {
	        ArrayList nodelist = new ArrayList();
	        parseNode(template, nodelist);
	
	        setupTemplate(nodelist.iterator());
	        
	        //datasets should be done after the template
	        ArrayList datalist = new ArrayList();
	        parseNode(dataset,datalist);
	        
	        //MUST be called first to populate the values map that is then called once field names are definded
	        setupDataSet(datalist.iterator());
    	}
    }

	private void setupTemplate2(Node rootObj) {
		NodeList kidObjs = rootObj.getChildNodes();
		for (int i = 0; i < kidObjs.getLength(); i++) {
            String nodeName = kidObjs.item(i).getNodeName();
            
            if(nodeName.equals("subform")){
            	subform(kidObjs.item(i));
            	break;
            }
        }
	}

	private void setupDataSet(Iterator nodeIterator) {
		while (nodeIterator.hasNext()) {
            Node node = (Node) nodeIterator.next();
            String nodeName = node.getNodeName();

            if (nodeName.equals("xfa:data")) {
                data(node, nodeIterator);

            } else {
            }
		}
	}

	private void data(Node node,Iterator nodeIterator) {
		NodeList childs = node.getChildNodes();
		for(int i=0;i<childs.getLength();i++){
			Node chnode = childs.item(i);
//			String nodeName = chnode.getNodeName();//comment out

			NodeList kidNodes = chnode.getChildNodes();
			if(kidNodes.getLength()>0){
				Node kidNode = kidNodes.item(0);
				kidNodes = kidNode.getChildNodes();
				String nodeName = kidNode.getNodeName();

				if(kidNodes.getLength()>0){

					Node valueNd = kidNodes.item(0);
					valueMap.put(nodeName.toLowerCase(),valueNd.getNodeValue());
				}
			}
		}
	}

	/**
     * creates a new formobject, used to help generate each field,
     * and appends old one to LinkedList sent in to this method but private to the parseStream method
     */
    private void nextFormObject() {
//		System.out.println("nextForm===================");
//		boolean test = false;
        if (formObject != null) {
//			System.out.println("added="+formObject);
            xfaFormList.add(formObject);
//			test = true;
        }
        formObject = new XFAFormObject();
//		
//		if(test){
//			System.out.println("TEST IF changed in linkedlist");
//			System.out.println("list="+xfaFormList.getLast());
//			System.out.println("current="+formObject);
//		}
    }

    private static void parseNode(Node nodeToParse, ArrayList nodeList) {
        nodeList.add(nodeToParse);
        NodeList setOfNodes = nodeToParse.getChildNodes();
        for (int i = 0; i < setOfNodes.getLength(); i++) {
            parseNode(setOfNodes.item(i), nodeList);
        }
    }

    /**
     * tests the name and parses its value
     */
    private void setupTemplate(Iterator nodeIterator) {
        while (nodeIterator.hasNext()) {
            Node node = (Node) nodeIterator.next();
            String nodeName = node.getNodeName();

            if (nodeName.equals("field")) {
                field(node, nodeIterator);

            } else if (nodeName.equals("pageArea")) {
                //store as the page these forms apply to
                if (debugXFAstream)
                    System.out.println("  pagearea=" + node.getNodeValue() + " att=" + node.getAttributes());

                NamedNodeMap att = node.getAttributes();

                //store page for this set of fields
                Node tmp = att.getNamedItem("id");
                pagenum = tmp.getNodeValue();
                pagenum = pagenum.substring(pagenum.indexOf("Page") + 4);

                if (debugXFAstream){
                	tmp = att.getNamedItem("name");
                    System.out.println("name=" + tmp.getNodeValue() + " att=" + tmp.getAttributes() + " childs=" + tmp.getChildNodes().getLength());
                }

            } else if (nodeName.equals("contentArea")) {
                NamedNodeMap att = node.getAttributes();
                if (att != null) {
                    //gather x,y,w,h,name from field
                    Node tmp;
                    if ((tmp = att.getNamedItem("x")) != null) {
                        contentX = resolveMeasurementToPoints(tmp.getNodeValue());
                    }
                    if ((tmp = att.getNamedItem("y")) != null) {
                        contentY = resolveMeasurementToPoints(tmp.getNodeValue());
                    }
                    if ((tmp = att.getNamedItem("w")) != null) {
                        contentW = resolveMeasurementToPoints(tmp.getNodeValue());
                    }
                    if ((tmp = att.getNamedItem("h")) != null) {
                        contentH = resolveMeasurementToPoints(tmp.getNodeValue());
                    }
                }

//	    	}else if(nodeName.equals("template")){
//				template(node,nodeIterator);
            } else if (nodeName.equals("#document")) {
                //ignore
            } else if (nodeName.equals("templateDesigner")) {
                //ignore
            } else {
            }
        }
        nextFormObject();
    }

    private void subform(Node subForm) {
    	NodeList kidObjs = subForm.getChildNodes();
    	for (int i = 0; i < kidObjs.getLength(); i++) {
            String nodeName = kidObjs.item(i).getNodeName();
            
            if(nodeName.equals("subform")){
            	subform(kidObjs.item(i));
            }else if(nodeName.equals("pageSet")){
//            	pageSet(kidObjs[i]);
            }
        }
	}

	private static int resolveMeasurementToPoints(String attValue) {
        int val = 0;

        if (attValue.endsWith("pt")) {//1 pt = 1 pt on screen
            val = new Double(attValue.substring(0, attValue.indexOf("pt"))).intValue();

        } else if (attValue.endsWith("in")) {//1 in = 72 pt
            val = (int) (72 * (Double.parseDouble(attValue.substring(0, attValue.indexOf("in")))));

        } else if (attValue.endsWith("cm")) {//1 cm = 28.35 pt
            val = (int) (28.35 * (Double.parseDouble(attValue.substring(0, attValue.indexOf("cm")))));

        } else if (attValue.endsWith("mm")) {//1 mm = 2.835 pt
            val = (int) (2.835 * (Double.parseDouble(attValue.substring(0, attValue.indexOf("mm")))));

        } else {
            LogWriter.writeFormLog("UNIMPLEMENTED type of y size=" + attValue, XFAFormStream.debugUnimplemented);
        }
        return val;
    }

    private void field(Node nodeToParse, Iterator nodeIterator) {
        //save current setup and move on to next field
        nextFormObject();

        //recall and save pagenumber for this field
        formObject.setPageNumber(pagenum);

        NamedNodeMap att = nodeToParse.getAttributes();
//		System.out.println("field rect="+att);
        if (att != null) {
            //gather x,y,w,h,name from field
            Node tmp;
            if ((tmp = att.getNamedItem("x")) != null) {
                formObject.setX(contentX + resolveMeasurementToPoints(tmp.getNodeValue()));
            }
            if ((tmp = att.getNamedItem("y")) != null) {
//				System.out.println("y="+contentY+" h="+contentH);
                formObject.setY(contentH - resolveMeasurementToPoints(tmp.getNodeValue()));
            }
            if ((tmp = att.getNamedItem("w")) != null) {
                //contentW
                formObject.setWidth(resolveMeasurementToPoints(tmp.getNodeValue()));
            }
            if ((tmp = att.getNamedItem("h")) != null) {
                //contentH
                formObject.setHeight(resolveMeasurementToPoints(tmp.getNodeValue()));
            }
            if ((tmp = att.getNamedItem("name")) != null) {
            	//- we now have annot and alt names in FormObject as well
            	formObject.setFieldName(tmp.getNodeValue());
            } else {
            	formObject.setFieldName("");
            }
        }

        //find ui node, should be next node
        //TODO reform to trap field or ui alone
        Node uiNode = null;
        while (nodeIterator.hasNext()) {
            uiNode = (Node) nodeIterator.next();
            if (uiNode.getNodeName().equals("ui")) {
                break;
            } else {
                uiNode = null;
            }
        }

        if (uiNode == null) {
            System.out.println("ERROR ERROR  ERROR no ui in field=" + ConvertToString.convertDocumentToString(nodeToParse));
        } else {
            if (debugXFAstream)
                System.out.println("ui=" + uiNode);
            Node tmpNode = null;
            String nodeName = null;
            while (nodeIterator.hasNext()) {
                tmpNode = (Node) nodeIterator.next();
                nodeName = tmpNode.getNodeName();

                if (nodeName.equals("checkButton")) {
                    checkButton(tmpNode, nodeIterator);
                } else if (nodeName.equals("button")) {
                    button(tmpNode, nodeIterator);
                } else if (nodeName.equals("choiceList")) {
                    choiceList(tmpNode, nodeIterator);
                } else if (nodeName.equals("textEdit")) {
                    textEdit(tmpNode, nodeIterator);
                } else {
                    LogWriter.writeFormLog("node not implemented nodename=" + nodeName, debugUnimplemented);
                }
            }
        }
    }

    private static Node findNodeIn(String searchName, Node nodeToParse) {
        findNodeIn:
        while (true) {
            NodeList nodes = nodeToParse.getChildNodes();
            Node node;
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                if (node.getNodeName().equals(searchName)) {
                    return node;
                }

                nodeToParse = node;
                continue findNodeIn;
            }
            return null;
        }
    }

    private void textEdit(Node nodeToParse, Iterator nodeIterator) {
        if (debugXFAstream)
            System.out.println("textEdit - ");

        formObject.setType(PdfDictionary.Tx, true);
        formObject.setFlag(12, true);
        formObject.setFlag(13, false);

        String nodeName = nodeToParse.getNodeName();
        Object nodeValue = nodeToParse.getNodeValue();
        if (debugXFAstream)
            System.out.println("textEdit="+nodeName + " = " + nodeValue);

        NamedNodeMap att = nodeToParse.getAttributes();
        if (att != null) {
            if (debugXFAstream)
                System.out.println(" attributes=" + att.toString());
        }

        Node tmpNode;
        String nodename;
        while (nodeIterator.hasNext()) {
            tmpNode = (Node) nodeIterator.next();
            nodename = tmpNode.getNodeName();

            if (nodename.equals("templateDesigner")) {
                //ignore
            } else if (nodename.equals("field")) {
                field(tmpNode, nodeIterator);

            } else if (nodename.equals("value")) {
                if (debugXFAstream)
                    System.out.println("  value=" + tmpNode.toString());

            } else if (nodename.equals("caption")) {
                if (debugXFAstream)
                    System.out.println("  caption=" + tmpNode.toString());

            } else if (nodename.equals("text")) {
//    			System.out.println("   text="+tmpNode);
                if (nodeIterator.hasNext()) {
                    Node tmp = (Node) nodeIterator.next();
                    if (debugXFAstream)
                        System.out.println("    text=" + tmp.getNodeValue());
                    //TODO @xfa text - this is broken
                    //if you open f1_60 (bottom right corner) ends up with Do I need an EIN
                    //which is actually from a text box on page 2
//                    System.out.println("setting text value="+tmp.getNodeValue());
//                    System.out.println("formobject="+formObject.getFieldName());
                    //formObject.setTextValue(tmp.getNodeValue());

 //                   System.out.println("name="+tmp.getNodeName());
//                    valueMap.put(key,tmp.getNodeValue());
                }

            } else if (nodename.equals("para")) {
                if (debugXFAstream)
                    System.out.println("  para=" + tmpNode.toString());

                NamedNodeMap var = tmpNode.getAttributes();
                Node tmp;
                if ((tmp = var.getNamedItem("hAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("horiz=" + tmp.getNodeValue());
                    formObject.setHorizontalAlign(tmp.getNodeValue());
                }
                if ((tmp = var.getNamedItem("vAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("vertic=" + tmp.getNodeValue());
                    formObject.setVerticalAllign(tmp.getNodeValue());
                }

            } else if (nodename.equals("font")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("typeface");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  font=" + tmp.getNodeValue());
                    //TODO @xfa font
//    				Font font = new Font(tmp.getNodeValue(),style,size);
//    				formObject.setTextFont();
                }
            } else if (nodename.equals("edge")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("stroke");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  edge=" + tmp.getNodeValue());
                    formObject.setBorderStroke(tmp.getNodeValue());
                }
            } else if (nodename.equals("border")) {
                if (debugXFAstream)
                    System.out.println("  border=" + tmpNode.toString());

            } else if (nodename.equals("margin")) {
                if (debugXFAstream)
                    System.out.println("  margin=" + tmpNode.toString());

            } else if (nodename.equals("proto")) {
                if (debugXFAstream)
                    System.out.println("  proto=" + tmpNode.toString());

            } else {
                LogWriter.writeFormLog("node name not implemented in textEdit name=" + nodename, debugUnimplemented);
            }
        }
    }

    private void choiceList(Node nodeToParse, Iterator nodeIterator) {
        if (debugXFAstream)
            System.out.println("choiceList - ");

        formObject.setType(PdfDictionary.Ch, true);
        formObject.setFlag(17, false);

        String nodeName = nodeToParse.getNodeName();
        Object nodeValue = nodeToParse.getNodeValue();
        if (debugXFAstream)
            System.out.println(nodeName + " = " + nodeValue);

        NamedNodeMap att = nodeToParse.getAttributes();
        if (att != null) {
            if (debugXFAstream)
                System.out.println(" attributes=" + att.toString());
            Node tmp;
            if ((tmp = att.getNamedItem("open")) != null) {
                formObject.setChoiceOpening(tmp.getNodeValue());
            }
        }

        Node tmpNode;
        String nodename;
        while (nodeIterator.hasNext()) {
            tmpNode = (Node) nodeIterator.next();
            nodename = tmpNode.getNodeName();

            if (nodename.equals("templateDesigner")) {
                //ignore
            } else if (nodename.equals("field")) {
                field(tmpNode, nodeIterator);

            } else if (nodename.equals("value")) {
                if (debugXFAstream)
                    System.out.println("  value=" + tmpNode.toString());

            } else if (nodename.equals("caption")) {
                if (debugXFAstream)
                    System.out.println("  caption=" + tmpNode.toString());

            } else if (nodename.equals("items")) {
//    			System.out.println("   items="+tmpNode);

            	/**
                NodeList items = tmpNode.getChildNodes();
                Node tmpItem;
                String[] listOfItems = new String[items.getLength()];
                for (int i = 0; i < items.getLength(); i++) {
                    tmpItem = items.item(i);
                    if (tmpItem.getNodeName().equals("text")) {
                        if (debugXFAstream)
                            System.out.println("text item" + i + '=' + tmpItem.getChildNodes().item(0));

                        NodeList kidNodes = tmpItem.getChildNodes();
                        if(kidNodes.getLength()>0)
                        	listOfItems[i] = kidNodes.item(0).getNodeValue();
                    }
                }
                //formObject.setlistOfItems(listOfItems, true);
                 /**/

            } else if (nodename.equals("text")) {
//    			System.out.println("   text="+tmpNode);
                if (nodeIterator.hasNext()) {
                    Node tmp = (Node) nodeIterator.next();
                    if (debugXFAstream)
                        System.out.println("    text=" + tmp);
                    formObject.setTextValue(tmp.getNodeValue());
                }

            } else if (nodename.equals("para")) {
                if (debugXFAstream)
                    System.out.println("  para=" + tmpNode.toString());

                NamedNodeMap var = tmpNode.getAttributes();
                Node tmp;
                if ((tmp = var.getNamedItem("hAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("horiz=" + tmp.getNodeValue());
                    formObject.setHorizontalAlign(tmp.getNodeValue());
                }
                if ((tmp = var.getNamedItem("vAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("vertic=" + tmp.getNodeValue());
                    formObject.setVerticalAllign(tmp.getNodeValue());
                }

            } else if (nodename.equals("font")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("typeface");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  font=" + tmp.getNodeValue());
//    				TODO @xfa font
//    				Font font = new Font(tmp.getNodeValue(),style,size);
//    				formObject.setTextFont();
                }

            } else if (nodename.equals("edge")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("stroke");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  edge=" + tmp.getNodeValue());
                    formObject.setBorderStroke(tmp.getNodeValue());
                }

            } else if (nodename.equals("border")) {
                if (debugXFAstream)
                    System.out.println("  border=" + tmpNode.toString());

            } else if (nodename.equals("margin")) {
                if (debugXFAstream)
                    System.out.println("  margin=" + tmpNode.toString());

            } else if (nodename.equals("draw")) {
                if (debugXFAstream)
                    System.out.println("  draw=" + tmpNode.toString());

            } else if (nodename.equals("rectangle")) {
                if (debugXFAstream)
                    System.out.println("  rectangle=" + tmpNode.toString());

            } else {
                LogWriter.writeFormLog("node name not implemented in choiceList name=" + nodename, debugUnimplemented);
            }
        }
    }

    private void button(Node nodeToParse, Iterator nodeIterator) {
        if (debugXFAstream)
            System.out.println("button - ");

        formObject.setType(PdfDictionary.Btn, true);
        formObject.setFlag(16, true);

        String nodeName = nodeToParse.getNodeName();
        Object nodeValue = nodeToParse.getNodeValue();
        if (debugXFAstream)
            System.out.println(nodeName + " = " + nodeValue);

        NamedNodeMap att = nodeToParse.getAttributes();
        if (att != null) {
            if (debugXFAstream)
                System.out.println(" attributes=" + att.toString());
        }

        Node tmpNode;
        String nodename;
        while (nodeIterator.hasNext()) {
            tmpNode = (Node) nodeIterator.next();
            nodename = tmpNode.getNodeName();

            if (nodename.equals("templateDesigner")) {
                //ignore
            } else if (nodename.equals("field")) {
                field(tmpNode, nodeIterator);

            } else if (nodename.equals("edge")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("stroke");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  edge node=" + tmp.getNodeValue());
                    formObject.setBorderStroke(tmp.getNodeValue());
                }
            } else if (nodename.equals("caption")) {
                if (debugXFAstream)
                    System.out.println("  caption node=" + tmpNode.toString());

            } else if (nodename.equals("value")) {
                if (debugXFAstream)
                    System.out.println("  value=" + tmpNode);

            } else if (nodename.equals("text")) {
//    			System.out.println("   text="+tmpNode);
                if (nodeIterator.hasNext()) {
                    Node tmp = (Node) nodeIterator.next();
                    if (debugXFAstream)
                        System.out.println("    text=" + tmp);
                    formObject.setNormalCaption(tmp.getNodeValue());
                }

            } else if (nodename.equals("para")) {
                if (debugXFAstream)
                    System.out.println("  para=" + tmpNode);
                NamedNodeMap var = tmpNode.getAttributes();
                Node tmp;
                if ((tmp = var.getNamedItem("hAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("horiz=" + tmp.getNodeValue());
                    formObject.setHorizontalAlign(tmp.getNodeValue());
                }
                if ((tmp = var.getNamedItem("vAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("vertic=" + tmp.getNodeValue());
                    formObject.setVerticalAllign(tmp.getNodeValue());
                }
            } else if (nodename.equals("font")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("typeface");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  font=" + tmp.getNodeValue());
//    				TODO @xfa font
//    				Font font = new Font(tmp.getNodeValue(),style,size);
//    				formObject.setTextFont();
                }

            } else if (nodename.equals("border")) {
                if (debugXFAstream)
                    System.out.println("  border=" + tmpNode);

            } else if (nodename.equals("fill")) {
                if (debugXFAstream)
                    System.out.println("  fill=" + tmpNode);

            } else if (nodename.equals("color")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("value");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  color=" + tmp.getNodeValue());
                    formObject.setBackgroundColor(tmp.getNodeValue());
                }

            } else if (nodename.equals("bind")) {
                if (debugXFAstream)
                    System.out.println("  bind=" + tmpNode);

            } else if (nodename.equals("event")) {
                if (debugXFAstream)
                    System.out.println("   event=" + tmpNode);
                Node tmp = tmpNode.getAttributes().getNamedItem("activity");
                formObject.setEventAction(tmp.getNodeValue());

            } else if (nodename.equals("script")) {
                if (debugXFAstream)
                    System.out.println("  script=" + tmpNode.toString());
                NamedNodeMap tmpatt = tmpNode.getAttributes();
                if (tmpatt != null) {
                    Node contentType = tmpatt.getNamedItem("contentType");
                    if (contentType != null)
                        formObject.setScriptType(contentType.getNodeValue());
                }

                if (nodeIterator.hasNext()) {
                    Node tmp = (Node) nodeIterator.next();
                    if (debugXFAstream)
                        System.out.println("    #text=" + tmp);

                    if (tmp != null)
                        formObject.setScript(tmp.getNodeValue());
                }
            } else if (nodename.equals("submit")) {
                NamedNodeMap subAtt = tmpNode.getAttributes();

                Node format = subAtt.getNamedItem("format");
                if(format!=null)
                	formObject.setSubmitFormat(format.getNodeValue());
                Node target = subAtt.getNamedItem("target");
                if(target!=null)
                	formObject.setSubmitURL(target.getNodeValue());
                Node textEncoding = subAtt.getNamedItem("textEncoding");
                if(textEncoding!=null)
                	formObject.setSubmitTextEncoding(textEncoding.getNodeValue());

                if (debugXFAstream){
                    System.out.println("   submit##=" + ConvertToString.convertDocumentToString(tmpNode));
                    ConvertToString.printStackTrace(1);
                }
            } else {
                LogWriter.writeFormLog("node name not implemented in button name=" + nodename, debugUnimplemented);
            }
        }
    }

    private void checkButton(Node nodeToParse, Iterator nodeIterator) {
        if (debugXFAstream)
            System.out.println("checkButton - ");

        formObject.setType(PdfDictionary.Ch, true);
        formObject.setFlag(17, true);

        String nodeName = nodeToParse.getNodeName();
        Object nodeValue = nodeToParse.getNodeValue();
        if (debugXFAstream)
            System.out.println(nodeName + " = " + nodeValue);

        NamedNodeMap att = nodeToParse.getAttributes();
        if (att != null) {
            if (debugXFAstream)
                System.out.println(" attributes=" + att.toString());
        }

        Node tmpNode;
        String nodename;
        while (nodeIterator.hasNext()) {
            tmpNode = (Node) nodeIterator.next();
            nodename = tmpNode.getNodeName();

            if (nodename.equals("templateDesigner")) {
                //ignore
            } else if (nodename.equals("field")) {
                field(tmpNode, nodeIterator);

            } else if (nodename.equals("value")) {
                if (debugXFAstream)
                    System.out.println("   value=" + tmpNode);

            } else if (nodename.equals("caption")) {
                if (debugXFAstream)
                    System.out.println("  caption node=" + tmpNode.toString());

            } else if (nodename.equals("text")) {
//    			System.out.println("   text="+tmpNode);
                if (nodeIterator.hasNext()) {
                    Node tmp = (Node) nodeIterator.next();
                    if (debugXFAstream)
                        System.out.println("    text=" + tmp);
                    formObject.setTextValue(tmp.getNodeValue());
                }

            } else if (nodename.equals("integer")) {
//    			System.out.println("   integer="+tmpNode);
                if (nodeIterator.hasNext()) {
                    Node tmp = (Node) nodeIterator.next();
                    if (debugXFAstream)
                        System.out.println("    integer=" + Integer.parseInt(tmp.getNodeValue()));
                    formObject.setIntegerValue(tmp.getNodeValue());
                    //TODO @xfa integer
                }

            } else if (nodename.equals("para")) {
                if (debugXFAstream)
                    System.out.println("   para=" + tmpNode);

                NamedNodeMap var = tmpNode.getAttributes();
                Node tmp;
                if ((tmp = var.getNamedItem("hAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("horiz=" + tmp.getNodeValue());
                    formObject.setHorizontalAlign(tmp.getNodeValue());
                }
                if ((tmp = var.getNamedItem("vAlign")) != null) {
                    if (debugXFAstream)
                        System.out.println("vertic=" + tmp.getNodeValue());
                    formObject.setVerticalAllign(tmp.getNodeValue());
                }

            } else if (nodename.equals("font")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("typeface");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  font=" + tmp.getNodeValue());
//    				TODO @xfa font
//    				Font font = new Font(tmp.getNodeValue(),style,size);
//    				formObject.setTextFont();
                }

            } else if (nodename.equals("edge")) {
                Node tmp = tmpNode.getAttributes().getNamedItem("stroke");
                if (tmp != null) {
                    if (debugXFAstream)
                        System.out.println("  edge node=" + tmp.getNodeValue());
                    formObject.setBorderStroke(tmp.getNodeValue());
                }

            } else if (nodename.equals("border")) {
                if (debugXFAstream)
                    System.out.println("   border=" + tmpNode);

            } else if (nodename.equals("fill")) {
                if (debugXFAstream)
                    System.out.println("   fill=" + tmpNode);

            } else if (nodename.equals("margin")) {
                if (debugXFAstream)
                    System.out.println("   margin=" + tmpNode);

            } else if (nodename.equals("event")) {
                if (debugXFAstream)
                    System.out.println("   event=" + tmpNode);

            } else if (nodename.equals("items")) {
//    			System.out.println("   items="+tmpNode);
/**
                NodeList items = tmpNode.getChildNodes();
                Node tmpItem;
                String[] listOfItems = new String[items.getLength()];
                for (int i = 0; i < items.getLength(); i++) {
                    tmpItem = items.item(i);
                    if (tmpItem.getNodeName().equals("integer")) {
                        if (debugXFAstream)
                            System.out.println("integer item" + i + '=' + tmpItem.getChildNodes().item(0));
                        listOfItems[i] = tmpItem.getChildNodes().item(0).getNodeValue();
                    }
                }
                formObject.setlistOfItems(listOfItems, true);
/**/
            } else if (nodename.equals("exclGroup")) {
                if (debugXFAstream)
                    System.out.println("   exclGroup=" + tmpNode);

            } else if (nodename.equals("proto")) {
                if (debugXFAstream)
                    System.out.println("   proto=" + tmpNode);

            } else {
                LogWriter.writeFormLog("node name not implemented in checkbutton name=" + nodename, debugUnimplemented);
            }
    	}
	}

    public boolean hasXFADataSet() {
        return this.dataset!=null;
    }
}
