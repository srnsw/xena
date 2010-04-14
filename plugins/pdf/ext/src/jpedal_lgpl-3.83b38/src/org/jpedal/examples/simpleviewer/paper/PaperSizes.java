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
* PaperSizes.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.paper;

import java.util.Map;
import java.util.HashMap;
import java.awt.print.Paper;

public class PaperSizes {

    Map paperDefinitions=new HashMap();

    /**default for paper selection*/
    private int defaultPageIndex=0;

    public PaperSizes(){
        setPaperSizes();
    }

    public String[] getAvailablePaperSizes(){
        return (String[]) paperDefinitions.keySet().toArray(new String[paperDefinitions.keySet().size()]);
    }

    /**return selected Paper*/
    public Paper getSelectedPaper(Object id) {
        return (Paper) paperDefinitions.get(id);
    }

    /**
     * method to setup specific Paper sizes
     * - add your own here to extend list
     */
    private void setPaperSizes(){

    	String printDescription;
    	Paper paper;
    	//defintion for each Paper - must match

    	//set default value
    	defaultPageIndex=1;

    	/**
    	//A4 (border)
		printDescription= Messages.getMessage("PdfViewera4");
		paper = new Paper();
		paper.setSize(595, 842);
		paper.setImageableArea(43, 43, 509, 756);
		paperDefinitions.put(printDescription,paper);
	*/
		//A4 (borderless)
		printDescription="A4 (borderless)";
        paper = new Paper();
		paper.setSize(585, 832);
		paper.setImageableArea(0, 0, 585, 832);
		paperDefinitions.put(printDescription,paper);

		//A5
		printDescription="A5";
		paper = new Paper();
		paper.setSize(297, 421);
		paper.setImageableArea(23,23,254,378);
		paperDefinitions.put(printDescription,paper);
		
		//Added for Adobe
		printDescription="US Letter (8.5 x 11)";
		paper = new Paper();
		paper.setSize(612, 792);
		paper.setImageableArea(0,0,612,792);
		paperDefinitions.put(printDescription,paper);
	
		//custom
		printDescription="Custom 2.9cm x 8.9cm";
		int customW=(int) (29*2.83);
		int customH=(int) (89*2.83); //2.83 is scaling factor to convert mm to pixels
		paper = new Paper();
		paper.setSize(customW, customH);
		paper.setImageableArea(0,0,customW,customH); //MUST BE SET ALSO
		paperDefinitions.put(printDescription,paper);
		
		/** kept in but commented out for general usage
		//architectural D (1728x2592)
		printDescription="Architectural D";
		paper = new Paper();
		paper.setSize(1728, 2592);
		paper.setImageableArea(25,25,1703,2567);
		paperDefinitions.put(printDescription,paper);
		/**/

		//Add your own here

    }

    public int getDefaultPageIndex() {
    	return defaultPageIndex;
        
    }
}
