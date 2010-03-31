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
* MergedOutlines.java
* ---------------
*/
package org.jpedal.objects;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Shape;

/**
 * @author markee
 *
 * holds outlines of shapes so we can show - used internally by IDR (not part of API)
 */
public class MergedOutlines {
    
    /**initial size of all arrays*/
    int initialSize=1000;
    
    public static int maxAlgorithmCount=50;
    
    private int[] debugCount=new int[maxAlgorithmCount];
    
    //debugging allowing us to see what is happening
    public static boolean show_merging = false;
    
    //  store merges for debugging
    private Vector_Int merge_level = new Vector_Int(initialSize);

    private Vector_Shape merge_outline = new Vector_Shape(initialSize);
    
    /**count number of times we use this grouping level*/
    public void increaseDebugCount(int current_level){
    		debugCount[current_level]++;
    }
    
    public void resetDebugCount(int algorithmCount){
		debugCount = new int[maxAlgorithmCount];
    }
    
    /**
     * get number of grouping operations for each level
     */
    public int[] getDebugCount() {

        return debugCount;
    }
    
    /**
     * reset on page change
     */
    final public void resetPageShapes() {

    }

    /**
     * log merged shapes so we can debug merging routines
     */
    final public void addMergedShape(int current_level, float x1, float y1, float x2, float y2) {

    }
    
    /**
     * @return Returns the merge_level.
     */
    public Vector_Int getMergeLevel() {
        return merge_level;
    }

    /**
     * @return Returns the merge_outline.
     */
    public Vector_Shape getMergeOutline() {
        return merge_outline;
    }

}
