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
* PathSerializer.java
* ---------------
*/
package org.jpedal.io;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class will serialize and deserialize GeneralPath objects 
 *
 */
public class PathSerializer {

	/**
	 * method to serialize a path.  This method will iterate through the iterator passed
	 * in, and write out the base components of the path to the ObjectOutput
	 * 
	 * @param os - ObjectOutput to write to
	 * @param pi - PathIterator path components to iterate over
	 * @throws IOException
	 */
	public static void serializePath(ObjectOutput os, PathIterator pi) throws IOException{
		
		os.writeObject(new Integer(pi.getWindingRule()));
				
		List list = new ArrayList();
		
		while(!pi.isDone()){
			float[] array = new float[6];
			int type = pi.currentSegment(array);
			
			/** add details of each segment to the list*/
			list.add(new Integer(type));
			list.add(array);
			
			pi.next();
		}
		
		/** writes out the list which contains all the components of the path */
		os.writeObject(list);

	}
	
	/**
	 * method to deserialize a path from an ObjectInput.
	 * 
	 * @param os - ObjectInput that contains the serilized path
	 * @return - the deserialize GeneralPath
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static GeneralPath deserializePath(ObjectInput os) throws ClassNotFoundException, IOException{
		
		/** deserialize the windingRule for the path */
		Integer windingRule = (Integer) os.readObject();
		if(windingRule == null)
			return null;
		
		/** deserialize the list which contains all the raw path data */
		List list = (List) os.readObject();
		
		GeneralPath path = new GeneralPath();
		path.setWindingRule(windingRule.intValue());
		
		/**
		 * iterate over the list, and rebuild the path from the
		 * individual movements stored inside the list
		 */
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			int pathType = ((Integer)iter.next()).intValue();
			float[] array = (float[]) iter.next();
			
			switch (pathType) {
			case PathIterator.SEG_LINETO:
				path.lineTo(array[0], array[1]);
				break;
			case PathIterator.SEG_MOVETO:
				path.moveTo(array[0], array[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(array[0], array[1], array[2], array[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				path.curveTo(array[0], array[1], array[2], array[3], array[4], array[5]);
				break;
			case PathIterator.SEG_CLOSE:
				path.closePath();
				break;
			default:
				System.out.println("unrecognized general path type");
				
				break;
			}
		}
		
		/** return the rebuilt path */
		return path;
	}
}
