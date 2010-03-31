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
* Vector_Object.java
* ---------------
*/
package org.jpedal.utils.repositories;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.jpedal.color.PdfTexturePaint;
import org.jpedal.fonts.glyph.T1Glyph;
import org.jpedal.fonts.glyph.T3Glyph;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.io.PathSerializer;
import org.jpedal.render.DynamicVectorRenderer;


/**
 * Provides the functionality/convenience of a Vector for objects - 
 *
 * Much faster because not synchronized and no cast - 
 * Does not double in size each time
 */
public class Vector_Object implements Serializable
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item = 0;

    //current max size
    int  max_size = 250;

    /**
	 * flags to indicate which type of custom serialization is taking place
	 */
	private static final Integer GENERIC = new Integer(1);
	private static final Integer BASICSTROKE =new Integer(2);
	private static final Integer BUFFERED_IMAGE =new Integer(3);
	private static final Integer GENERAL_PATH =new Integer(4);
	private static final Integer T1GLYPH =new Integer(5);
	private static final Integer TTGLYPH =new Integer(6);
	private static final Integer AREA =new Integer(7);
	private static final Integer RECT =new Integer(8);
	private static final Integer T3GLYPH=new Integer(9);
	
	private static final Integer TEXTUREDPAINT=new Integer(10);
	
	//holds the data
	private Object[] items = new Object[max_size];
	
	//default size
	public Vector_Object() 
	{
		
	}


    private int checkPoint=-1;

    /**
     * used to store end of PDF components
     */
    public void resetToCheckpoint() {
        if(checkPoint!=-1)
        current_item=checkPoint;

        checkPoint=-1;
    }

    /**
     * used to rollback array to point
     */
    public void setCheckpoint() {
        if(checkPoint==-1)
        current_item=checkPoint=current_item;
    }

    protected static int incrementSize(int increment_size){

		if(increment_size<8000)
		       increment_size=increment_size*4;
		    else if(increment_size<16000)
		       increment_size=increment_size*2;
	        else
	            increment_size=increment_size+2000;
		return increment_size;
	}
   
	//set size
	public Vector_Object( int number ) 
	{
		max_size = number;
		items = new Object[max_size];
	}
	
	

	///////////////////////////////////
	/**
	 * extract underlying data
	 */
	final public Object[] get()
	{
		return items;
	}
	
	/////////////////////////////////////
	/**
	 * pull item from top as in LIFO stack
	 */
	final public Object pull()
	{
		
		if(current_item>0)
			current_item--;

		return ( items[current_item] );
	}
	
	/////////////////////////////////////
	/**
	 * put item at top as in LIFO stack
	 */
	final public void push( Object value )
	{
		
		checkSize( current_item );
		items[current_item] = value;
		
		current_item++;
	}
	////////////////////////////////////
	/**
	 * see if value present
	 */
	final public boolean contains( Object value )
	{
		boolean flag = false;
		for( int i = 0;i < current_item;i++ )
		{
			if( items[i].equals( value ) )
			{
				i = current_item + 1;
				flag = true;
			}
		}
		return flag;
	}
	///////////////////////////////////
	/**
	 * add an item
	 */
	final public void addElement( Object value )
	{

        //if(current_item>6370 && value instanceof Shape)
        //System.out.println("Added into array"+current_item+" "+((Shape)value).getBounds());

        checkSize( current_item );
		items[current_item] = value;
		current_item++;


    }
	///////////////////////////////////
	/**
	 * set an element
	 */
	final public void setElementAt( Object new_name, int id )
	{
		if( id >= max_size )
		checkSize( id );
		
		items[id] = new_name;
	}
	///////////////////////////////////
	/**
	 * remove element at
	 */
	final public Object elementAt( int id )
	{
		if( id >= max_size )
			return null;
		else
			return items[id];
	}
	///////////////////////////////////
	/**
	 * replace underlying data
	 */
	final public void set( Object[] new_items )
	{
		items = new_items;
	}
	///////////////////////////////////
	/**
	 * clear the array
	 */
	final public void clear()
	{
		checkPoint = -1;
		//items = null;
		
		//holds the data
		//items = new Object[max_size];
		if(current_item>0){
			for(int i=0;i<current_item;i++)
				items[i]=null;
		}else{
			for(int i=0;i<max_size;i++)
				items[i]=null;
		}
		current_item = 0;
	}
	///////////////////////////////////
	/**
	 * return the size
	 */
	final public int size()
	{
		return current_item + 1;
	}
	///////////////////////////////////
	/**
	 * remove element at
	 */
	final public void removeElementAt( int id )
	{
		if( id >= 0 )
		{
			//copy all items back one to over-write
            System.arraycopy(items, id + 1, items, id, current_item - 1 - id);
			
			//flush last item
			items[current_item - 1] = null;
		}
		else
			items[0] = null;
		
		//reduce counter
		current_item--;
	}
	////////////////////////////////////
	/**
	 * check the size of the array and increase if needed
	 */
	private void checkSize( int i ){

        if( i >= max_size ){
            
            int old_size = max_size;
			max_size = max_size + increment_size;
			
			//allow for it not creating space
			if( max_size <= i )
			max_size = i +increment_size+ 2;
			Object[] temp = items;
			items = new Object[max_size];
			System.arraycopy( temp, 0, items, 0, old_size );
			
			//increase size increase for next time
			increment_size=incrementSize(increment_size);
			
		}
	}
	
	/**
	 * method to serialize each element in this collection
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param bos - the output stream to write the objects out to
	 * @throws IOException
	 */
	public void writeToStream(ByteArrayOutputStream bos) throws IOException{
		
		ObjectOutput os=new ObjectOutputStream(bos);
		
		//size of array as first item
		os.writeObject(new Integer(max_size));
		
		int basicStrokes=0,bufferedImages=0,paints=0,generalPaths=0,t3glyphs=0,t1glyphs=0,ttglyphs=0,areas=0,generics=0,nullGenerics=0;
		int totalGeneric=0;
		/**
		 * iterate through each item in the collection, and if its an object that
		 * wont serialize automaticaly, then perform a custom serialization, otherwise
		 * let the default serialization handle it
		 */
		for (int i = 0; i < max_size; i++) {
			Object nextObj=items[i];
			
			if(nextObj instanceof BasicStroke){
				basicStrokes++;
				/** write out a flag to indicate this is a BasicStroke object */
				os.writeObject(BASICSTROKE);
				
				BasicStroke stroke=(BasicStroke) items[i];
				
				/** write out the raw elements of the BasicStroke */
				os.writeFloat(stroke.getLineWidth());
				os.writeInt(stroke.getEndCap());
				os.writeInt(stroke.getLineJoin());
				os.writeFloat(stroke.getMiterLimit());
				os.writeObject(stroke.getDashArray());
				os.writeFloat(stroke.getDashPhase());
			}else if(nextObj instanceof Rectangle2D){
				basicStrokes++;
				/** write out a flag to indicate this is a BasicStroke object */
				os.writeObject(RECT);

				Rectangle2D rect=(Rectangle2D) items[i];

				/** write out the raw elements of the Rect */
				os.writeDouble(rect.getBounds2D().getX());
				os.writeDouble(rect.getBounds2D().getY());
				os.writeDouble(rect.getBounds2D().getWidth());
				os.writeDouble(rect.getBounds2D().getHeight());


			}else if(nextObj instanceof BufferedImage){
				bufferedImages++;
				/** write out a flag to indicate this is a BufferedImage object */
				os.writeObject(BUFFERED_IMAGE);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				/** store the image in a byte array */
				ImageIO.write((BufferedImage) nextObj, "png", baos);
				
				/** write the image as a byte array to the stream */
				os.writeObject(baos.toByteArray());
				
			}else if(nextObj instanceof GeneralPath){
				generalPaths++;
				/** write out a flag to indicate this is a GeneralPath object */
				os.writeObject(Vector_Object.GENERAL_PATH);
				
				/** use out custom path serializer to serialize the path */
				PathSerializer.serializePath(os, ((GeneralPath)items[i]).getPathIterator(new AffineTransform()));
				
			}else if(nextObj instanceof T1Glyph){
				t1glyphs++;
				/** write out a flag to indicate this is a T1Glyph object */
				os.writeObject(T1GLYPH);

				((T1Glyph)nextObj).flushArea();

				/** use default serialization to serialize the object except the paths */
				os.writeObject(nextObj);
				
				/** now serialize the paths to the stream */
				((T1Glyph)nextObj).writePathsToStream(os);
					
			}else if(nextObj instanceof TTGlyph){
				ttglyphs++;
				/** write out a flag to indicate this is a TTGlyph object */
				os.writeObject(TTGLYPH);

				((TTGlyph)nextObj).flushArea();

				/** use default serialization to serialize the object except the paths */
				os.writeObject(nextObj);
				
				/** now serialize the paths to the stream */
				((TTGlyph)nextObj).writePathsToStream(os);
				
			}else if(nextObj instanceof T3Glyph){
				
				t3glyphs++;
				
				/** write out a flag to indicate this is a TTGlyph object */
				os.writeObject(T3GLYPH);

				//((T3Glyph)nextObj).flushArea();  not needed at present
				
				/**convert to data stream and save*/
				((T3Glyph)nextObj).writePathsToStream(os);
			
			
			}else if(nextObj instanceof org.jpedal.color.PdfTexturePaint){
				
				paints++;
				
				/** write out a flag to indicate this is a Texture object */
				os.writeObject(TEXTUREDPAINT);
				
				/** write the image as a byte array to the stream */
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				/** store the image in a byte array */
				ImageIO.write(((org.jpedal.color.PdfTexturePaint)nextObj).getImage(), "png", baos);		
				os.writeObject(baos.toByteArray());
				
				/**and anchor as well*/
				Rectangle2D rect=((org.jpedal.color.PdfTexturePaint)nextObj).getAnchorRect();
				os.writeDouble(rect.getBounds2D().getX());
				os.writeDouble(rect.getBounds2D().getY());
				os.writeDouble(rect.getBounds2D().getWidth());
				os.writeDouble(rect.getBounds2D().getHeight());
				
			}else if(nextObj instanceof Area){
				areas++;
				/** write out a flag to indicate this is a TTGlyph object */
				os.writeObject(AREA);
				
				Area area=(Area) items[i];
				
				/** extract the PathIterator from the Area object */ 
				PathIterator pathIterator = area.getPathIterator(new AffineTransform());
				
				/** use out custom path serializer to serialize the path */
				PathSerializer.serializePath(os,pathIterator);
				
			}else{
				
				if(nextObj==null)
					nullGenerics++;
				else{
					generics++;
				}
				/**
				 * if the object is not on our list to custom serialize, then let the
				 * default serialization handle it 
				 */
				
				try{
					
					int start = bos.size();
					
					/** write out a flag to indicate this is a generic object */
					os.writeObject(GENERIC);
					
					/** serialize the object to the stream */
					os.writeObject(nextObj);
					
					int end = bos.size();
					totalGeneric+=(end-start);
				}catch(Exception e){
					System.err.println("nextObj = "+nextObj);
					e.printStackTrace();
				}
			}
		}
		
		if(DynamicVectorRenderer.debugStreams){
			System.out.println("  basicStrokes = "+basicStrokes);
			System.out.println("  bufferedImages = "+bufferedImages);
			System.out.println("  generalPaths = "+generalPaths);
			System.out.println("  paints = "+paints);
			System.out.println("  t1glyphs = "+t1glyphs);
			System.out.println("  ttglyphs = "+ttglyphs);
			System.out.println("  t3glyphs = "+t3glyphs);
			System.out.println("  areas = "+areas);
			System.out.println("  generics = "+generics);
			System.out.println("  nullGenerics = "+nullGenerics);
			System.out.println("  totalGeneric = "+totalGeneric);
		}
		os.close();
	}
	
	/**
	 * method to deserialize each object in the input stream 
	 * 
	 * NOT PART OF API and subject to change (DO NOT USE)
	 * 
	 * @param bis - the input stream to read from
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void restoreFromStream(ByteArrayInputStream bis) throws IOException, ClassNotFoundException{
		
		ObjectInput os=new ObjectInputStream(bis);
		
		/** read fromt the stream the number of objects in this collection */
		int size= ((Integer) os.readObject()).intValue();
				
		max_size = size;
		
		items=new Object[size];
		
		Object nextObject=null;
		Integer type=null;
		
		/** 
		 * iterate through each object in the input stream, and store into our 
		 * internal collection
		 */
		for (int i = 0; i < size; i++) {
			
			/** read what type this object is going to be */
			type=(Integer) os.readObject();
			
			if(type.compareTo(BASICSTROKE)==0){
				
				/** retrieve the raw data from the BasicStroke */
				float w = os.readFloat();
				int current_line_cap_style = os.readInt();
				int current_line_join_style = os.readInt();
				float mitre_limit = os.readFloat();
				float[] current_line_dash_array = (float[]) os.readObject();
				float current_line_dash_phase = os.readFloat();
				
				/** create a new BasicStroke object based on the raw data */
				nextObject = new BasicStroke( w, current_line_cap_style, current_line_join_style, mitre_limit, current_line_dash_array, current_line_dash_phase );
			}else if (type.compareTo(RECT)==0){

				double x = os.readDouble();
				double y = os.readDouble();
				double w = os.readDouble();
				double h = os.readDouble();

				nextObject=new Rectangle2D.Double(x,y,w,h);

			}else if(type.compareTo(BUFFERED_IMAGE)==0){
				
				/** read in the bytes that make up this image */
				byte[] bytes = (byte[]) os.readObject();
				
				/** read the bytes in and rebuild the image */
				nextObject = ImageIO.read(new ByteArrayInputStream(bytes));
				
			}else if(type.compareTo(GENERAL_PATH)==0){
				
				/** use our custom path deserializer to read in the path */
				nextObject = PathSerializer.deserializePath(os);
				
			}else if(type.compareTo(T1GLYPH)==0){
				
				/** read in the entire glyph, except the paths */
				T1Glyph glyph = (T1Glyph) os.readObject();
				
				/**
				 * we now need to extract the paths for this glyph from the stream
				 */
				
				/** the number of paths in the glyph */
				int count = ((Integer)os.readObject()).intValue();
				
				GeneralPath[] paths = new GeneralPath[count];
				
				/** iterate through the stream, and deserialize each path */
				for(int j=0; j<count; j++){
					paths[j] = PathSerializer.deserializePath(os);
				}
				
				/**
				 * now we need to create a new Vector_Path object, and pass in the
				 * paths we have just exrtracted from the sream
				 */
				Vector_Path vp = new Vector_Path();
				vp.set(paths);
				vp.setCurrent_item(paths.length);
				
				/** set the Vector_Path object in the glyph */
				glyph.setPaths(vp);
				
				nextObject = glyph;
				
			}else if(type.compareTo(TTGLYPH)==0){
				
				/** read in the entire glyph, except the paths */
				TTGlyph glyph = (TTGlyph) os.readObject();
				
				/**
				 * we now need to extract the paths for this glyph from the stream
				 */
				
				/** the number of paths in the glyph */
				int count = ((Integer)os.readObject()).intValue();
				
				GeneralPath[] paths = new GeneralPath[count];
				
				/** iterate through the stream, and deserialize each path */
				for(int j=0; j<count; j++){
					paths[j] = PathSerializer.deserializePath(os);
				}
				
				/**
				 * now we need to create a new Vector_Path object, and pass in the
				 * paths we have just exrtracted from the sream
				 */
				Vector_Path vp = new Vector_Path();
				vp.set(paths);
				vp.setCurrent_item(paths.length);
				
				/** set the Vector_Path object in the glyph */
				glyph.setPaths(vp);
				
				nextObject = glyph;
			}else if(type.compareTo(T3GLYPH)==0){
				
				nextObject=new T3Glyph(os);
				
			}else if(type.compareTo(TEXTUREDPAINT)==0){
				
				//restore image
				byte[] bytes = (byte[]) os.readObject();
				
				/** read the bytes in and rebuild the image */
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
				
				/**and the rectangle*/
				double x = os.readDouble();
				double y = os.readDouble();
				double w = os.readDouble();
				double h = os.readDouble();

				Rectangle2D anchor=new Rectangle2D.Double(x,y,w,h);

				nextObject=new PdfTexturePaint(img,anchor);
				
			}else if(type.compareTo(AREA)==0){
				
				/** deserialize the path that makes up this Area */
				GeneralPath path = PathSerializer.deserializePath(os);
				
				/** create a new Area based on the path */
				nextObject = new Area(path);
				
			}else{
			
				/**
				 * this object has been serialized using the default mechanism,
				 * so just deserialize normaly
				 */
				
				nextObject = os.readObject();
				
			}
				
			items[i]=nextObject;
			
		}
	}
	
	public void trim(){
		
		Object[] newItems = new Object[current_item];
		
		System.arraycopy(items,0,newItems,0,current_item);
		
		items=newItems;
		max_size=current_item;
	}
	
    /**reset pointer used in add to remove items above*/
    public void setSize(int currentItem) {
        current_item=currentItem;
    }
}
