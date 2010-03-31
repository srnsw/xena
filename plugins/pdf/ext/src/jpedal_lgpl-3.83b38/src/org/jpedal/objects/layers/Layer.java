
package org.jpedal.objects.layers;

/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2009, IDRsolutions and Contributors.
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

  * Layer.java
  * ---------------
  * (C) Copyright 2009, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */

/**
 * used by JavaScript for access
 */
public class Layer {

    public static boolean debugLayer=false;

    private PdfLayerList layerList;

    public String name;
    
    Layer(String  name, PdfLayerList layerList) {

        this.name=name;
        this.layerList=layerList;

    }

    public boolean getState(){
        return layerList.isVisible(name);
    }

    public void setState(boolean state){

        boolean currentValue=layerList.isVisible(name);

        layerList.setVisiblity(name,state);

        //tell JPedal we need to update
        if(currentValue!=state){

            if(debugLayer)
            System.out.println(name+" "+state);

            layerList.setChangesMade(true);
        }
    }

}
