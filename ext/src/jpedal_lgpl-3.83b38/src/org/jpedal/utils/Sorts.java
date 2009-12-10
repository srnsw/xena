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
* Sorts.java
* ---------------
*/
package org.jpedal.utils;

/**
 * repository for general sorting routines
 * (needs other sort classes adding in)
 * <br><b>This class is NOT part of the API</b>
 */
public class Sorts
{
	//////////////////////////////////////////////////////////////
	/**
	 * quick sort list on 3 values - note it sorts BACKWARDS for y1
	 */
	final public static int[] quicksort( int[] primary, float[] secondary, float[] tertiary, int[] names )
	{

		//copy items so we don't sort originals
		int[] primary_values = (int[])primary.clone();
		float[] secondary_values = (float[])secondary.clone();
		float[] tertiary_values = (float[])tertiary.clone();
		int start = 0;
		int end = names.length - 1;
		int i, j, k;
		int temp;
		float temp2, temp3;
		int temp_name;
		for( i = start + 1;i <= end;i++ )
		{
			temp = primary_values[i];
			temp2 = secondary_values[i];
			temp3 = tertiary_values[i];
			temp_name = names[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( ( temp < primary_values[j] ) | ( ( temp == primary_values[j] ) & ( ( temp2 > secondary_values[j] ) | ( ( temp == primary_values[j] ) & ( temp2 == secondary_values[j] ) & ( temp3 > tertiary_values[j] ) ) ) ) )
				{
					primary_values[j + 1] = primary_values[j];
					secondary_values[j + 1] = secondary_values[j];
					tertiary_values[j + 1] = tertiary_values[j];
					names[j + 1] = names[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			primary_values[k] = temp;
			secondary_values[k] = temp2;
			tertiary_values[k] = temp3;
			names[k] = temp_name;
		}

		//return the sorted list
		return names;
	}
	//////////////////////////////////////////////////////////////////////
	/**
	 * quick sort list on 1 value with names as int
	 * primary is sorted into ascending order and names is joined to primary.
	 * so if primary[i] is moved to primary[j], then names[i] is moved to names[j]
	 */
	//#####################################################
	
	private static void sift(int[] primary, int[] names, int left, int right) {
//		variables for sift moved to reduce initialization
		int currentLeft;
//		variables for sift moved to reduce initialization
	    int primaryTMP;
//	  variables for sift moved to reduce initialization
	    int namesTMP;
//	  variables for sift moved to reduce initialization
	    int childL;
	    
    	//assign left to local
        currentLeft = left;
        //temp store of left item
        primaryTMP = primary[currentLeft];
        //temp store of left item
        namesTMP = names[currentLeft];
        
        //Left child node of currentLeft
       childL = 2*left+1;
        
		
        //Find a[left]'s larger child
        if ((childL<right) && (primary[childL]<primary[childL+1])) { 
            childL=childL+1;
        }
        //assert: a[childL] is larger child
        
        //sift temp to be in correct place in highest on leftMost and arranged as tree
        while ((childL<=right) && (primaryTMP<primary[childL])){
        	//assign highest item to leftmost position
            primary[currentLeft]=primary[childL];
            names[currentLeft]=names[childL];
            currentLeft=childL; 
            childL=2*childL+1;
            
            //pick highest child 
            if ((childL<right) && (primary[childL]<primary[childL+1])){ 	
                childL=childL+1;
            }
        }
        //put temp in the correct place in the sub-heap
        primary[currentLeft]=primaryTMP;
        names[currentLeft]=namesTMP;
        //assert: a[left] is the root a sub-heap.
    }
    
    /** sorts as a tree like structure in array representation */
	final public static int[] quicksort( int[] primaryIN, int[] names ){
		/** copy so we don't sort original */
		int items = primaryIN.length;
		int[] primary = new int[items];
        System.arraycopy(primaryIN, 0, primary, 0, items);
		
    	//pointer to left side of unsorted array
        int left = primary.length/2; 
        //pointer to right side of unsorted array 
        int right = primary.length-1;
        
        //sift through array into a heap
        while (left>0) {
        	
            left=left-1; 
            
            //go through tree starting with leaves and going up
            sift(primary,names, left, right);
        }
        
        //rearrange heap into a sorted array
        while (right>0) { 
        	
            //assert: largest unsorted value is at a[0]
            //move largest item to right end
            int tempA=primary[0];
            int tempB=names[0];
            primary[0]=primary[right]; 
            names[0]=names[right];
            primary[right]=tempA;
            names[right]=tempB;
            //assert: a[right..] is sorted
            
            //right is largest and sorted decrement it
            right=right-1;
            
            //get largest value in the tree to the leftMost position
            sift(primary,names, left, right);
        }
        //assert: right==0, therefore a[0..] is all sorted
        
        return names;
    }
    //#####################################################
	/**
	 * quick sort list on 2 values with names as int
	 */
	final public static int[] quicksort( int[] primary, int[] secondary, int[] names )
	{
		int items = names.length;

		//copy items so we don't sort originals
		int[] primary_values = new int[items];
		int[] secondary_values = new int[items];
		for( int i = 0;i < items;i++ )
		{
			primary_values[i] = primary[i];
			secondary_values[i] = secondary[i];
		}
		int start = 0;
		int end = items;
		int i, j, k;
		int temp, temp2;
		int temp_name = 0;
		for( i = start + 1;i < end;i++ )
		{
			temp = primary_values[i];
			temp2 = secondary_values[i];
			temp_name = names[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( ( temp < primary_values[j] ) | ( ( temp == primary_values[j] ) & ( temp2 < secondary_values[j] ) ) )
				{
					primary_values[j + 1] = primary_values[j];
					secondary_values[j + 1] = secondary_values[j];
					names[j + 1] = names[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			primary_values[k] = temp;
			secondary_values[k] = temp2;
			names[k] = temp_name;
		}

		//return the sorted list
		return names;
	}

	//////////////////////////////////////////////////////////////
	/**
	 * quick sort list on 3 values
	 */
	final public static String[] quicksort( int start, int end, int[] primary, int[] secondary, int[] tertiary, String[] names )
	{

		//copy items so we don't sort originals
		int[] primary_values = (int[])primary.clone();
		int[] secondary_values = (int[])secondary.clone();
		int[] tertiary_values = (int[])tertiary.clone();
		int i, j, k;
		int temp, temp2, temp3;
		String temp_name = "";
		for( i = start + 1;i <= end;i++ )
		{
			temp = primary_values[i];
			temp2 = secondary_values[i];
			temp3 = tertiary_values[i];
			temp_name = names[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( ( temp < primary_values[j] ) | ( ( temp == primary_values[j] ) & ( ( temp2 < secondary_values[j] ) | ( ( temp == primary_values[j] ) & ( temp2 == secondary_values[j] ) & ( temp3 < tertiary_values[j] ) ) ) ) )
				{
					primary_values[j + 1] = primary_values[j];
					secondary_values[j + 1] = secondary_values[j];
					tertiary_values[j + 1] = tertiary_values[j];
					names[j + 1] = names[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			primary_values[k] = temp;
			secondary_values[k] = temp2;
			tertiary_values[k] = temp3;
			names[k] = temp_name;
		}

		//return the sorted list
		return names;
	}
	//////////////////////////////////////////////////////////////
	/**
	 * quick sort list on 3 values - note it sorts BACKWARDS for y1
	 */
	final public static int[] quicksort( int[] primary, int[] secondary, int[] tertiary, int[] names )
	{

		//copy items so we don't sort originals
		int[] primary_values = (int[])primary.clone();
		int[] secondary_values = (int[])secondary.clone();
		int[] tertiary_values = (int[])tertiary.clone();
		int start = 0;
		int end = names.length - 1;
		int i, j, k;
		int temp, temp2, temp3;
		int temp_name;
		for( i = start + 1;i <= end;i++ )
		{
			temp = primary_values[i];
			temp2 = secondary_values[i];
			temp3 = tertiary_values[i];
			temp_name = names[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( ( temp < primary_values[j] ) | ( ( temp == primary_values[j] ) & ( ( temp2 > secondary_values[j] ) | ( ( temp == primary_values[j] ) & ( temp2 == secondary_values[j] ) & ( temp3 > tertiary_values[j] ) ) ) ) )
				{
					primary_values[j + 1] = primary_values[j];
					secondary_values[j + 1] = secondary_values[j];
					tertiary_values[j + 1] = tertiary_values[j];
					names[j + 1] = names[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			primary_values[k] = temp;
			secondary_values[k] = temp2;
			tertiary_values[k] = temp3;
			names[k] = temp_name;
		}

		//return the sorted list
		return names;
	}
}
