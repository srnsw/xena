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
* PdfImageData.java
* ---------------
*/
package org.jpedal.objects;
import java.util.HashMap;
import java.util.Map;

import org.jpedal.utils.repositories.Vector_Float;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_String;

/**
 * holds metadata on images extracted from the PDF file. <P>
 * Images are generally stored in the temp directory and only their meta
 * data held to reduce memory needs.<P>
 */
public class PdfImageData
{
	
	/**page id for image*/
	private Vector_Int object_page_id = new Vector_Int( 100 );
	
	/**x co-ord for image*/
	private Vector_Float x = new Vector_Float( 100 );
	
	/**y co-ord for image*/
	private Vector_Float y = new Vector_Float( 100 );
	
	/**width for image*/
	private Vector_Float w = new Vector_Float( 100 );
	
	/**height */
	private Vector_Float h = new Vector_Float( 100 );
	
	/**image name*/
	private Vector_String object_image_name = new Vector_String( 100 );
	
	/**xform name*/
	private Map xformName = new HashMap();
	
	/**xform data*/
	private Map xformData = new HashMap();
	
	/**count on images*/
	private int current_item = 0;
	///////////////////////////////////////////////////////////////////////////
	/**
	 * <p>add an item (used internally as PDF page decoded).
	 */
	final public void setImageInfo( String image_name, int current_page_id, float x1, float y1, float w1, float h1,String xform )
	{
		object_page_id.addElement( current_page_id );
		
		//name of image
		object_image_name.addElement( image_name );
		
		//store shape co-ords
		x.addElement( x1 );
		y.addElement( y1 );
		h.addElement( h1 );
		w.addElement( w1 );
		
		if(xform!=null)
		xformName.put(image_name,xform);
		
		current_item++;
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get Y co-ord for image in pixels (user coords)
	 */
	final public float getImageYCoord( int i )
	{
		return y.elementAt( i );
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get width for image in pixels
	 */
	final public float getImageWidth( int i )
	{
		return w.elementAt( i );
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get height for image in pixels
	 */
	final public float getImageHeight( int i )
	{
		return h.elementAt( i );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get object page id (ie sequential
	 * number of page)
	 */
	final public int getImagePageID( int i )
	{
		return object_page_id.elementAt( i );
	}
	//////////////////////////////////////////////////////////////////////////
	/**
	 * get image name created from raw data
	 */
	final public String getImageName( int i )
	{
		return object_image_name.elementAt( i );
	}
	
	/////////////////////////////////////////////////////////////////////////
	/**
	 * clear object and reset (does not flush images from disk cache held by ObjectStore)
	 */
	final public void clearImageData()
	{
		object_image_name.clear();
		xformName.clear();
		xformData.clear();
		object_page_id.clear();
		x.clear();
		y.clear();
		w.clear();
		h.clear();
		current_item = 0;
	}
	///////////////////////////////////////////////////////////////////
	/**
	 * get X co-ord for image in pixels (user coords)
	 */
	final public float getImageXCoord( int i )
	{
		return x.elementAt( i );
	}
	////////////////////////////////////////////////////////////////////////
	/**
	 * <p>return the number of images. 
	 * <p>Note image1 is item 0, image2 is item 1 forget methods
	 */
	final public int getImageCount()
	{
		return current_item;
	}
	
	/**returns name of xform associated with this image*/
	public String getParentXForm(String image_name) {
		
		return (String) xformName.get(image_name);
	}
	
	public void setXformData(String lastFormID, Map formData) {

		xformData.put(lastFormID,formData);
		
	}
	
	/**
	 * return OPI data stored in Map. Null reference returns null.
	 */
	public Map getXFormData(String parentXform) {
		
		if(parentXform==null)
			return null;
		
		return (Map) xformData.get(parentXform);
	}
	
}
