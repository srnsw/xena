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
* FormUtils.java
* ---------------
*/
package org.jpedal.objects.acroforms.utils;

import javax.swing.*;

import org.jpedal.objects.raw.FormObject;

import java.util.Enumeration;
import java.awt.*;

/**
 * general purpose functions used in forms
 */
public class FormUtils {
    /**
     * sorts the buttongroup into ascending order, smallest area first
     */
    public static AbstractButton[] sortGroupSmallestFirst(ButtonGroup bg) {

        int items = bg.getButtonCount();
        AbstractButton[] buttons = new AbstractButton[items];

        Enumeration butGrp = bg.getElements();
        for (int i = 0; i < items; i++) {
            if (butGrp.hasMoreElements())
                buttons[i] = (AbstractButton) butGrp.nextElement();
        }

        //sort buttons array
        return (AbstractButton[]) sortCompsAscending(buttons);
    }

    /**
     * sorts as a tree like structure in array representation,
     * the Compo~nent array in ascending height order,
     * smallest height first
     */
    private static Component[] sortCompsAscending(Component[] primary) {
        //reference
        //Sorts.quicksort(new int[1],new int[1]);

        /** copy so we don't sort original */
        int items = primary.length;

        //pointer to left side of unsorted array
        int left = items / 2;
        //pointer to right side of unsorted array
        int right = items - 1;

        //sift through array into a heap
        while (left > 0) {

            left = left - 1;

            //go through tree starting with leaves and going up
            siftCompsAscending(primary, left, right);
        }

        //rearrange heap into a sorted array
        while (right > 0) {

            //assert: largest unsorted value is at a[0]
            //move largest item to right end
            Component tempA = primary[0];
            primary[0] = primary[right];
            primary[right] = tempA;
            //assert: a[right..] is sorted

            //right is largest and sorted decrement it
            right = right - 1;

            //get largest value in the tree to the leftMost position
            siftCompsAscending(primary, left, right);
        }
        //assert: right==0, therefore a[0..] is all sorted

        return primary;
    }

    /**
     * see sortCompsAscending(Component[])
     * This Is Called from That Method ONLY
     */
    private static void siftCompsAscending(Component[] primary, int left, int right) {
        int currentLeft;
        Component primaryTMP;
        int childL;

        //assign left to local
        currentLeft = left;
        //temp store of left item
        primaryTMP = primary[currentLeft];

        //Left child node of currentLeft
        childL = 2 * left + 1;

        //Find a[left]'s larger child
        if ((childL < right) && shouldSwapControlAscending(primary[childL], primary[childL + 1])) {
            childL = childL + 1;
        }
        //assert: a[childL] is larger child

        //sift temp to be in correct place in highest on leftMost and arranged as tree
        while ((childL <= right) && shouldSwapControlAscending(primaryTMP, primary[childL])) {
            //assign highest item to leftmost position
            primary[currentLeft] = primary[childL];
            currentLeft = childL;
            childL = 2 * childL + 1;

            //pick highest child
            if ((childL < right) && shouldSwapControlAscending(primary[childL], primary[childL + 1])) {
                childL = childL + 1;
            }
        }
        //put temp in the correct place in the sub-heap
        primary[currentLeft] = primaryTMP;
        //assert: a[left] is the root a sub-heap.
    }

    /**
     * the control of the order in the sortCompsAscending(Component[]) method
     */
    private static boolean shouldSwapControlAscending(Component arg1, Component arg2) {

        Rectangle first = arg1.getBounds();
        Rectangle second = arg2.getBounds();

        /**
         * sorts by area, same as acrobat
         return (first.width*first.height)<(second.width*second.height);
         */
        return (first.width * first.height) < (second.width * second.height);
    }

    /** 
     * if returnState is true it returns the state of the field,
     * else it returns the name of the field
     */
    public static String removeStateToCheck(String curCompName, boolean returnState) {
        if (curCompName != null) {
            int ptr = curCompName.indexOf("-(");
            /** NOTE if indexOf string changes change ptr+# to same length */
            if (ptr != -1) {
                if (returnState)
                    curCompName = curCompName.substring(ptr + 2, curCompName.length() - 1);
                else
                    curCompName = curCompName.substring(0, ptr);
            }
        }

        return curCompName;
    }

	/**
	 * sorts the integer array into the right order to read the 
	 * component array in size order largest first
	 * @param allFields 
	 */
	public static FormObject[] sortGroupLargestFirst(FormObject[] comps) {
		
		return sortCompsDesending(comps);
	}
	
	/**
     * sorts as a tree like structure in array representation,
     * the integer array in descending size order comparing the component size,
     */
    private static FormObject[] sortCompsDesending(FormObject[] array) {
        //reference
        //Sorts.quicksort(new int[1],new int[1]);

        /** copy so we don't sort original */
        int items = array.length;

        //pointer to left side of unsorted array
        int left = items / 2;
        //pointer to right side of unsorted array
        int right = items - 1;

        //sift through array into a heap
        while (left > 0) {

            left = left - 1;

            //go through tree starting with leaves and going up
            siftCompsDesending(array, left, right);
        }

        //rearrange heap into a sorted array
        while (right > 0) {

            //assert: largest unsorted value is at a[0]
            //move largest item to right end
        	FormObject tempA = array[0];
            array[0] = array[right];
            array[right] = tempA;
            //assert: a[right..] is sorted

            //right is largest and sorted decrement it
            right = right - 1;

            //get largest value in the tree to the leftMost position
            siftCompsDesending(array, left, right);
        }
        //assert: right==0, therefore a[0..] is all sorted

        return array;
    }

    /**
     * see sortCompsDesending(Component[])
     * This Is Called from That Method ONLY
     */
    private static void siftCompsDesending(FormObject[] array, int left, int right) {
        int currentLeft;
        FormObject primaryTMP;
        int childL;

        //assign left to local
        currentLeft = left;
        //temp store of left item
        primaryTMP = array[currentLeft];

        //Left child node of currentLeft
        childL = 2 * left + 1;

        //Find a[left]'s larger child
        if ((childL < right) && shouldSwapControlDesending(array[childL], array[childL + 1])) {
            childL = childL + 1;
        }
        //assert: a[childL] is larger child

        //sift temp to be in correct place in highest on leftMost and arranged as tree
        while ((childL <= right) && shouldSwapControlDesending(primaryTMP, array[childL])) {
            //assign highest item to leftmost position
        	array[currentLeft] = array[childL];
            currentLeft = childL;
            childL = 2 * childL + 1;

            //pick highest child
            if ((childL < right) && shouldSwapControlDesending(array[childL], array[childL + 1])) {
                childL = childL + 1;
            }
        }
        //put temp in the correct place in the sub-heap
        array[currentLeft] = primaryTMP;
        //assert: a[left] is the root a sub-heap.
    }
    
    /**
     * the control of the order in the sortCompsDesending(Component[]) method
     */
    private static boolean shouldSwapControlDesending(FormObject arg1, FormObject arg2) {
    	if(arg1==null){
    		if(arg2==null)
    			return false;
    		else
    			return true;
    	}else {
    		if(arg2==null)
    			return false;
    		else{
		    	Rectangle first = arg1.getBoundingRectangle();
		        Rectangle second = arg2.getBoundingRectangle();
		        
		        /**
		         * sorts by area, same as acrobat
		         return (first.width*first.height)>(second.width*second.height);
		         */
		        return (first.width * first.height) < (second.width * second.height);
    		}
    	}
    }

//    /**sorts the given array into ascending order, but returns an array with indexs of the right order of the array
//     * e.g. inArray= 10,15,5 outArray = 3,1,2
//     */
//	public static int[] sortIndexesinOrder(int[] indexes) {
//		int[] sortedIndexes = new int[indexes.length];
//		for (int i = 0; i < sortedIndexes.length; i++) {
//			sortedIndexes[i] = i;
//		}
//		return sortIndexesAscending(sortedIndexes,indexes);
//	}
//	
//	/**
//     * sorts as a tree like structure in array representation,
//     * the integer array in descending size order comparing the component size,
//     */
//    private static int[] sortIndexesAscending(int[] indexes,final int[] NONchangeArray) {
//
//        /** copy so we don't sort original */
//        int items = indexes.length;
//
//        //pointer to left side of unsorted array
//        int left = items / 2;
//        //pointer to right side of unsorted array
//        int right = items - 1;
//
//        //sift through array into a heap
//        while (left > 0) {
//
//            left = left - 1;
//
//            //go through tree starting with leaves and going up
//            siftIndexesAscending(indexes, NONchangeArray, left, right);
//        }
//
//        //rearrange heap into a sorted array
//        while (right > 0) {
//
//            //assert: largest unsorted value is at a[0]
//            //move largest item to right end
//        	int tempA = indexes[0];
//        	indexes[0] = indexes[right];
//        	indexes[right] = tempA;
//            //assert: a[right..] is sorted
//
//            //right is largest and sorted decrement it
//            right = right - 1;
//
//            //get largest value in the tree to the leftMost position
//            siftIndexesAscending(indexes, NONchangeArray, left, right);
//        }
//        //assert: right==0, therefore a[0..] is all sorted
//
//        return indexes;
//    }
//
//    /**
//     * see sortCompsDesending(Component[])
//     * This Is Called from That Method ONLY
//     */
//    private static void siftIndexesAscending(int[] indexes,final int[] NONchangeArray, int left, int right) {
//        int currentLeft;
//        int primaryTMP;
//        int childL;
//
//        //assign left to local
//        currentLeft = left;
//        //temp store of left item
//        primaryTMP = indexes[currentLeft];
//
//        //Left child node of currentLeft
//        childL = 2 * left + 1;
//
//        //Find a[left]'s larger child
//        if ((childL < right) && (NONchangeArray[indexes[childL]]> NONchangeArray[indexes[childL + 1]])) {
//            childL = childL + 1;
//        }
//        //assert: a[childL] is larger child
//
//        //sift temp to be in correct place in highest on leftMost and arranged as tree
//        while ((childL <= right) && (primaryTMP> NONchangeArray[indexes[childL]])) {
//            //assign highest item to leftmost position
//        	indexes[currentLeft] = indexes[childL];
//            currentLeft = childL;
//            childL = 2 * childL + 1;
//
//            //pick highest child
//            if ((childL < right) && (NONchangeArray[indexes[childL]]> NONchangeArray[indexes[childL + 1]])) {
//                childL = childL + 1;
//            }
//        }
//        //put temp in the correct place in the sub-heap
//        indexes[currentLeft] = primaryTMP;
//        //assert: a[left] is the root a sub-heap.
//    }

}
