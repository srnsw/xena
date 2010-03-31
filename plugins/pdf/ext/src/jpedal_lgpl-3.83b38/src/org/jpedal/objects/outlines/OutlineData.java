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
 * OutlineData.java
 * ---------------
 */
package org.jpedal.objects.outlines;

import org.jpedal.io.PdfObjectReader;
import org.jpedal.io.PdfReader;
import org.jpedal.objects.PageLookup;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.OutlineObject;
import org.jpedal.utils.LogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * encapsulate the Outline data
 */
public class OutlineData {

	private Document OutlineDataXML;

	/**locations of top target*/
	private float[] pagesTop;

	/**locations of top and bottom target*/
	private float[] pagesBottom;

	/**lookup for converting page to ref*/
	private String[] refTop;

	/**lookup for converting page to ref*/
	private String[] refBottom;

	private OutlineData(){}

	/**create list when object initialised*/
	public OutlineData(int pageCount){

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			OutlineDataXML=factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			System.err.println("Exception "+e+" generating XML document");
		}

		//increment so arrays correct size
		pageCount++;

		/**locations of top target*/
		pagesTop=new float[pageCount];

		/**locations of top and bottom target*/
		pagesBottom=new float[pageCount];

		/**lookup for converting page to ref*/
		refTop=new String[pageCount];

		/**lookup for converting page to ref*/
		refBottom=new String[pageCount];

	}

	/**return the list*/
	public Document getList(){
		return OutlineDataXML;
	}

	/**
	 * read the outline data
	 */
	public int readOutlineFileMetadata(PdfObject OutlinesObj, PdfObjectReader currentPdfFile, PageLookup pageLookup) {

		LogWriter.writeMethod("{readOutlineFileMetadata}",0);

		int count=OutlinesObj.getInt(PdfDictionary.Count);

		PdfObject FirstObj=OutlinesObj.getDictionary(PdfDictionary.First);
        currentPdfFile.checkResolved(FirstObj);
        if(FirstObj !=null){

			Element root=OutlineDataXML.createElement("root");

			OutlineDataXML.appendChild(root);

			int level=0;
			readOutlineLevel(root,currentPdfFile, pageLookup, FirstObj, level);

		}

		/**
		//build lookup table
		int pageCount=this.refTop.length;
		String lastLink=null,currentBottom;
		for(int i=1;i<pageCount;i++){

		    //if page has link use bottom
		    //otherwise last top
		    String link=this.refTop[i];

		    if(link!=null){
		        lookup[i]=link;
		    }else
		        lookup[i]=lastLink;

		    //System.out.println("Default for page "+i+" = "+lookup[i]+" "+refBottom[i]+" "+refTop[i]);
		    //track last top link
		    String top=this.refBottom[i];
		    if(top!=null){
		        lastLink=top;
		    }

		}

		/***/
		return count;
	}

	/**
	 * returns default bookmark to select for each page
	 * - not part of API and not live
	 *
	public Map getPointsForPage(){
	    return this.pointLookupTable;
	}*/


	/**
	 * read a level
	 */
	private void readOutlineLevel(Element root,PdfObjectReader currentPdfFile, PageLookup pageLookup, PdfObject outlineObj, int level) {

		String ID;
		float coord=0;
		int page=-1;

		Element child=OutlineDataXML.createElement("title");

		PdfObject FirstObj=null, NextObj;

		PdfArrayIterator DestObj=null;

		while(true){

			if(FirstObj!=null)
				outlineObj=FirstObj;

			ID=outlineObj.getObjectRefAsString();

			//set to -1 as default
			coord=-1;
			page=-1;

			/**
			 * process and move onto next value
			 */
			FirstObj=outlineObj.getDictionary(PdfDictionary.First);
            currentPdfFile.checkResolved(FirstObj);
            NextObj=outlineObj.getDictionary(PdfDictionary.Next);
            currentPdfFile.checkResolved(NextObj);

            //get Dest from Dest or A object
			DestObj=outlineObj.getMixedArray(PdfDictionary.Dest);

			if(DestObj==null || DestObj.getTokenCount()==0){
				PdfObject Aobj=outlineObj.getDictionary(PdfDictionary.A);

				if(Aobj!=null)
					DestObj=Aobj.getMixedArray(PdfDictionary.Dest);
			}

			String ref=null;

			//get coord & page from data
			if (DestObj != null && DestObj.getTokenCount()>0) {

				int count=DestObj.getTokenCount();

                if(count>0){
					if(DestObj.isNextValueRef())
						ref=DestObj.getNextValueAsString(true);
					else{ //its nameString name (name) linking to obj so read that
                        String nameString =DestObj.getNextValueAsString(true);

						ref=currentPdfFile.convertNameToRef(nameString);

                        PdfObject namedObj=new OutlineObject(ref);
                        currentPdfFile.readObject(namedObj);

                        DestObj=namedObj.getMixedArray(PdfDictionary.Dest);

                        if(DestObj!=null){
                            count=DestObj.getTokenCount();
    
                            if(count>0 && DestObj.isNextValueRef())
                            ref=DestObj.getNextValueAsString(true);
                        }
                    }
				}    				
			}

			if(ref!=null)
				page=pageLookup.convertObjectToPageNumber(ref);

			//add title to tree
			byte[] titleData=outlineObj.getTextStreamValueAsByte(PdfDictionary.Title);
			if(titleData !=null){

				String title= PdfReader.getTextString(titleData, false);

				//add node
				child=OutlineDataXML.createElement("title");
				root.appendChild(child);
				child.setAttribute("title",title);

			}

			/**
			 * just store page for moment
			 */
			if(page !=-1){

				child.setAttribute("page", String.valueOf(page));
				child.setAttribute("level", String.valueOf(level));
				child.setAttribute("objectRef",ID);

				/**
				 * set values
				 */
				//set defaults
				if(refTop[page]==null){
					pagesTop[page]=coord;
					refTop[page]=ID;
					pagesBottom[page]=coord;
					refBottom[page]=ID;
				}else{

					//set top point
					float last=pagesTop[page];
					if((last>coord)&&(last!=-1)){
						pagesTop[page]=coord;
						refTop[page]=ID;
					}

					//set bottom point
					last=pagesBottom[page];
					if((last<coord)&&(last!=-1)){
						pagesBottom[page]=coord;
						refBottom[page]=ID;
					}
				}
			}

			if(FirstObj!=null)
				readOutlineLevel(child,currentPdfFile, pageLookup, FirstObj, level+1);

			if(NextObj==null)
				break;

			FirstObj=NextObj;

		}
	}
}
