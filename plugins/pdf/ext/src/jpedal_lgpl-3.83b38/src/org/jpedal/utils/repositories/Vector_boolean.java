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
* Vector_boolean.java
* ---------------
*/
package org.jpedal.utils.repositories;

import java.io.Serializable;

/**
 * Provides the functionality/convenience of a Vector for boolean
 *
 * Much faster because not synchronized and no cast
 * Does not double in size each time
 */
public class Vector_boolean implements Serializable {


    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item = 0;

    //current max size
    int  max_size = 250;

    //holds the data
	private boolean[] items = new boolean[max_size];
	

	//default size
	public Vector_boolean() 
	{
		
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
	public Vector_boolean( int number ) 
	{
		max_size = number;
		items = new boolean[max_size];
	}
	///////////////////////////////////
	/**
	 * extract underlying data
	 */
	final public boolean[] get()
	{
		return items;
	}
	///////////////////////////////////
	/**
	 * set an element
	 */
	final public void setElementAt( boolean new_name, int id )
	{
		if( id >= max_size )
		checkSize( id );
		items[id] = new_name;
	}
	///////////////////////////////////
	/**
	 * return the size
	 */
	final public int size()
	{
		return items.length;
	}
	///////////////////////////////////
	/**
	 * add an item
	 */
	final public void addElement( boolean value )
	{
		checkSize( current_item );
		items[current_item] = value;
		current_item++;
	}
	///////////////////////////////////
	/**
	 * replace underlying data
	 */
	final public void set( boolean[] new_items )
	{
		items = new_items;
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
            System.arraycopy(items, id + 1, items, id, max_size - 2 - id);
			
			//flush last item
			items[max_size - 1] = false;
		}
		else
			items[0] = false;
		
		//reduce counter
		current_item--;
	}
	///////////////////////////////////
	/**
	 * clear the array
	 */
	final public void clear()
	{
		//items = null;
		//holds the data
		//items = new boolean[max_size];
		if(current_item>0){
			for(int i=0;i<current_item;i++)
				items[i]=false;
		}else{
			for(int i=0;i<max_size;i++)
				items[i]=false;
		}
		current_item = 0;
	}
	///////////////////////////////////
	/**
	 * get element at
	 */
	final public boolean elementAt( int id )
	{
		if( id >= max_size )
			return false;
		else
			return items[id];
	}
	
	///////////////////////////////////
	/**
	 * set an element
	 */
	final public void insertElementAt( boolean new_name, int id )
	{
		current_item = items.length;
		checkSize( current_item + 1 );
		
		//move up one
		//copy all items back one to over-write
		for( int i = current_item;i > id;i-- )
			items[i + 1] = items[i];
		items[id] = new_name;
		current_item++;
	}
	
	////////////////////////////////////
	/**
	 * check the size of the array and increase if needed
	 */
	final private void checkSize( int i )
	{
		/**
		if( i >= max_size )
		{
			int old_size = max_size;
			max_size = max_size + increment_size;
			
			//allow for it not creating space
			if( max_size <= i )
			max_size = i +increment_size+ 2;
			boolean[] temp = items;
			items = new boolean[max_size];
			System.arraycopy( temp, 0, items, 0, old_size );
			
			//increase size increase for next time
			if( increment_size < 500 )
				increment_size = increment_size * 2;
		}*/
		
		if( i >= max_size )
		{
			int old_size = max_size;
			max_size = max_size + increment_size;
			
			//allow for it not creating space
			if( max_size <= i )
			max_size = i +increment_size+ 2;
			boolean[] temp = items;
			items = new boolean[max_size];
			
			/**
			//add a default value
			if(defaultValue!=0){
			    for(int i1=old_size;i1<max_size;i1++)
			        temp[i1]=defaultValue;
			}*/
			
			System.arraycopy( temp, 0, items, 0, old_size );
			
			increment_size=incrementSize(increment_size);
			
//if( increment_size <= i ){
//			    if(increment_size<2500)
//			       increment_size=increment_size*4;
//			    else if(increment_size<10000)
//			       increment_size=increment_size*2;
//		        else
//		            increment_size=increment_size+2000;
//			//max_size = i +increment_size+ 2;
//			}
		}
	
	}
	
	public void trim(){
		
		boolean[] newItems = new boolean[current_item];
		
		System.arraycopy(items,0,newItems,0,current_item);
		
		items=newItems;
		max_size=current_item;
	}

    /**reset pointer used in add to remove items above*/
    public void setSize(int currentItem) {
        current_item=currentItem;
    }
}
