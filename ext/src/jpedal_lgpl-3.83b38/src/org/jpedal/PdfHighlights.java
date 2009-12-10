package org.jpedal;

import java.awt.Rectangle;

public class PdfHighlights {
	public static Rectangle[] lineAreas;
	public static int[] lineWritingMode;
	
	public static void addToLineAreas(Rectangle area, int writingMode) {
		boolean addNew = true;
		
		if(lineAreas==null){ //If null, create array
			
			//Set area
			lineAreas = new Rectangle[1];
			lineAreas[0] = area;
			
			//Set writing direction
			lineWritingMode = new int[1];
			lineWritingMode[0] = writingMode;
			
		}else{
			Rectangle[] lastAreas = lineAreas;
			int[] lastWritingMode = lineWritingMode;
			//Check for objects close to or intersecting each other
			if(area!=null){ //Ensure actual area is selected
				for(int i=0; i!= lastAreas.length; i++){
					int lx = lastAreas[i].x;
					int ly = lastAreas[i].y;
					int lw = lastAreas[i].width;
					int lh = lastAreas[i].height;
					int cx = area.x;
					int cy = area.y;
					int cw = area.width;
					int ch = area.height;
					
					int lwm = lastWritingMode[i];
					int cwm = writingMode;
					
					if(writingMode!=0)
					switch(writingMode){
					case 1 :
						lx = lastAreas[i].x;
						ly = lastAreas[i].y;
						lw = lastAreas[i].width;
						lh = lastAreas[i].height;
						cx = area.x;
						cy = area.y;
						cw = area.width;
						ch = area.height;
						break;
					case 2 : 
						lx = lastAreas[i].y;
						ly = lastAreas[i].x;
						lw = lastAreas[i].height;
						lh = lastAreas[i].width;
						cx = area.y;
						cy = area.x;
						cw = area.height;
						ch = area.width;
						break;
					case 3 : 
						lx = lastAreas[i].y;
						ly = lastAreas[i].x;
						lw = lastAreas[i].height;
						lh = lastAreas[i].width;
						cx = area.y;
						cy = area.x;
						cw = area.height;
						ch = area.width;
						break;
					default : break;
					}
										
					if(lwm==cwm && ((ly>(cy-5)) && (ly<(cy+5)) && lh<=(ch+(ch/5)) && lh>=(ch-(ch/5))) && //Ensure this is actually the same line and are about the same size
							(((lx>(cx + cw-5)) && (lx<(cx + cw+5))) || //Check for object at end of this object
									((lx + lw>(cx-5)) && (lx + lw<(cx+5))) ||//Check for object at start of this object
									lastAreas[i].intersects(area))//Check to see if it intersects at all
					){
						addNew = false;
						
						//No need to reset the writing mode as already set
						lastAreas[i]=mergePartLines(lastAreas[i], area);
					}
				}

				//If no object near enough to merge, start a new area
				if(addNew){
					lineAreas = new Rectangle[lastAreas.length+1];
					for(int i=0; i!= lastAreas.length; i++){
						lineAreas[i] = lastAreas[i];
					}
					lineAreas[lineAreas.length-1] = area;
					
					lineWritingMode = new int[lastWritingMode.length+1];
					for(int i=0; i!= lastAreas.length; i++){
						lineWritingMode[i] = lastWritingMode[i];
					}
					lineWritingMode[lineWritingMode.length-1] = writingMode;
				}
			}
		}
	}
	
	private static Rectangle mergePartLines(Rectangle lastArea, Rectangle area){
		/**
		 * Check coords from both areas and merge them to make
		 * a single larger area containing contents of both
		 */
		int x1 =area.x;
		int x2 =area.x + area.width;
		int y1 =area.y;
		int y2 =area.y + area.height;
		int lx1 =lastArea.x;
		int lx2 =lastArea.x + lastArea.width;
		int ly1 =lastArea.y;
		int ly2 =lastArea.y + lastArea.height;

		//Ensure the highest and lowest values are selected
		if(x1<lx1)
			area.x = x1;
		else
			area.x = lx1;

		if(y1<ly1)
			area.y = y1;
		else
			area.y = ly1;

		if(y2>ly2)
			area.height = y2 - area.y;
		else
			area.height = ly2 - area.y;

		if(x2>lx2)
			area.width = x2 - area.x;
		else
			area.width = lx2 - area.x;

		return area;
	}
	

	public static int[] getLineWritingMode() {
		
		if(lineWritingMode==null)
			return null;
		else{
			int count=lineWritingMode.length;
			
			int[] returnValue=new int[count];

            System.arraycopy(lineWritingMode, 0, returnValue, 0, count);
						
			return returnValue;
		}
	}



	public static void setLineWritingMode(int[] lineOrientation) {
		lineWritingMode = lineOrientation;
	}


	public static Rectangle[] getLineAreas() {
		
		if(lineAreas==null)
			return null;
		else{
			int count=lineAreas.length;
			
			Rectangle[] returnValue=new Rectangle[count];
			
			for(int ii=0;ii<count;ii++){
				if(lineAreas[ii]==null)
					returnValue[ii]=null;
				else
					returnValue[ii]=new Rectangle(lineAreas[ii].x,lineAreas[ii].y,
							lineAreas[ii].width,lineAreas[ii].height);
			}
			
			return returnValue;
		}
	}



	public static void setLineAreas(Rectangle[] la) {
		lineAreas = la;
	}
}
