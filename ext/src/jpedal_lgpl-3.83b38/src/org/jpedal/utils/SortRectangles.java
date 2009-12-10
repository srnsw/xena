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
* SortRectangles.java
* ---------------
*/
package org.jpedal.utils;

/**
 * arranges rectangles into order by size width smallest first<br> Used
 * by grouping algorithms<br> Needs to copy data into separate array so
 * program does not alter order of original<br> We provide a sorted array
 * of indices so data itself is nor sorted<br>
 * <br><b>This class is NOT part of the API</b>
 */
public class SortRectangles
{
	public SortRectangles() 
	{
		
	}
	
	/////////////////////////////////////////////////////////////////////////
	/**
	 * quick sort list of rectangles into order of smallest first
	 */
	final static public int[] quicksort_rectangles( int end, int[] r_size )
	{
		
		//init list
		int[] size = new int[end];
		int[] ids = new int[end];
		for( int ii = 0;ii < end;ii++ )
		{
			ids[ii] = ii;
			size[ii] = r_size[ii];
		}
		
		//other variables
		int i, j, k, start = 0;
		int temp, id_temp;
		
		//sort
		for( i = start + 1;i < end;i++ )
		{
			temp = size[i];
			id_temp = ids[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( temp < size[j] )
				{
					size[j + 1] = size[j];
					ids[j + 1] = ids[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			size[k] = temp;
			ids[k] = id_temp;
		}
		return ids;
	}
	/////////////////////////////////////////////////////////////////////////
	/**
	 * quick sort list of rectangles into order of smallest first
	 */
	final static public int[] quicksort_rectangles( int end, float[] r_size )
	{
		
		//init list
		float[] size = new float[end];
		int[] ids = new int[end];
		for( int ii = 0;ii < end;ii++ )
		{
			ids[ii] = ii;
			size[ii] = r_size[ii];
		}
		
		//other variables
		int i, j, k, start = 0;
		int id_temp;
		float temp;
		
		//sort
		for( i = start + 1;i < end;i++ )
		{
			temp = size[i];
			id_temp = ids[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( temp < size[j] )
				{
					size[j + 1] = size[j];
					ids[j + 1] = ids[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			size[k] = temp;
			ids[k] = id_temp;
		}
		return ids;
	}
	/**
	 * quick sort list of lines into x order
	 */
	final static public int[] quicksort_lines( int end, int[] line_x )
	{
		
		//init list
		int[] x = new int[end];
		int[] ids = new int[end];
		for( int ii = 0;ii < end;ii++ )
		{
			ids[ii] = ii;
			x[ii] =line_x[ii];
		}
		
		//other variables
		int i, j, k, start = 0;
		int id_temp;
		int temp;
		
		//sort
		for( i = start + 1;i < end;i++ )
		{
			temp = x[i];
			id_temp = ids[i];
			k = start;
			for( j = i - 1;j >= start;j-- )
				if( temp < x[j] )
				{
					x[j + 1] = x[j];
					ids[j + 1] = ids[j];
				}
				else
				{
					k = j + 1;
					break;
				}
			x[k] = temp;
			ids[k] = id_temp;
		}
		return ids;
	}
}
