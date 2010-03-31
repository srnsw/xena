/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2008, IDRsolutions and Contributors.
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

  * PdfObjectFactory.java
  * ---------------
  * (C) Copyright 2008, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;

/**
 * return required object according to key
 */
public class ObjectFactory {
    public static PdfObject createObject(int id, String ref, int parentType, int parentID) {

        switch(id){

            case PdfDictionary.A:
            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref);
            	else if(parentType==PdfDictionary.MCID)
            		return new MCObject(ref);
            	else
            		return new OutlineObject(ref);

            case PdfDictionary.AA:
            	return new FormObject(ref);
            	
            case PdfDictionary.AP:
            	return new FormObject(ref);
            
            case PdfDictionary.BI:                
                return new FormObject(ref);
            
            case PdfDictionary.Bl:
                return new FormObject(ref);
                
            case PdfDictionary.BS:
                return new FormObject(ref);
            
            case PdfDictionary.CF:
        		return new EncryptionObject(ref);
		
            case PdfDictionary.AcroForm:
        		return new FormObject(ref);
        		
            case PdfDictionary.C:                
                return new FormObject(ref);

            //case PdfDictionary.C2:                
              //  return new FormObject(ref);

            case PdfDictionary.CharProcs:
        		return new FontObject(ref);

        	case PdfDictionary.CIDSystemInfo:
	    		return new FontObject(ref);

	        case PdfDictionary.CIDToGIDMap:
	    		return new FontObject(ref);

            case PdfDictionary.ColorSpace:
                return new ColorSpaceObject(ref);

            case PdfDictionary.ClassMap:
            	return new MCObject(ref);


            case PdfDictionary.D:
                
                if(parentType==PdfDictionary.Form)
            		return new FormObject(ref, parentID);
            	else
            		return new OCObject(ref);
                
            case PdfDictionary.DC:                
                return new FormObject(ref);

            case PdfDictionary.DecodeParms:
                return new DecodeParmsObject(ref);

            case PdfDictionary.DescendantFonts:
                return new FontObject(ref);

            case PdfDictionary.Dests:
                return new NamesObject(ref);
                
            case PdfDictionary.DP:                
                return new FormObject(ref);
                
            case PdfDictionary.DS:                
                return new FormObject(ref);

            case PdfDictionary.E:                
                return new FormObject(ref);
                
            case PdfDictionary.EF:
            	return new FSObject(ref);    
            	
            case PdfDictionary.Encoding:
                return new FontObject(ref);

            case PdfDictionary.Extends:
                return new CompressedObject(ref);

            case PdfDictionary.ExtGState:
                return new ExtGStateObject(ref);
                
            case PdfDictionary.F:                
                return new FormObject(ref);

            case PdfDictionary.First:
                return new OutlineObject(ref);
                
            case PdfDictionary.Fo:                
                return new FormObject(ref);

            case PdfDictionary.Font:
                return new FontObject(ref);

            case PdfDictionary.FontDescriptor:
                return new FontObject(ref);

            case PdfDictionary.FontFile:
                return new FontObject(ref);

            case PdfDictionary.FontFile2:
                return new FontObject(ref);

            case PdfDictionary.FontFile3:
                return new FontObject(ref);
                
            case PdfDictionary.FS:                
                return new FSObject(ref);
    
            case PdfDictionary.Function:
                return new FunctionObject(ref);

            case PdfDictionary.Group:
                return new PdfObject(ref);
                
            case PdfDictionary.I:
                return new FormObject(ref);

            case PdfDictionary.Info:
                return new InfoObject(ref);

            case PdfDictionary.JavaScript:
                return new NamesObject(ref);

            case PdfDictionary.JBIG2Globals:
                return new DecodeParmsObject(ref);

            case PdfDictionary.JS:
            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref);
            	else
            		return new NamesObject(ref);
                
            case PdfDictionary.K:
                if(parentType==PdfDictionary.MCID)
            		return new MCObject(ref);
            	else
            	    return new FormObject(ref);

            case PdfDictionary.Layer:
                return new OCObject(ref);

            case PdfDictionary.MarkInfo:
            	return new MCObject(ref);

            case PdfDictionary.Mask:
                return new MaskObject(ref);

            case PdfDictionary.Metadata:
                return new MetadataObject(ref);
                
            case PdfDictionary.MK:
                return new MKObject(ref);

            case PdfDictionary.N:
            	return new FormObject(ref,parentID);
            	
            case PdfDictionary.Names:
                return new NamesObject(ref);

            case PdfDictionary.Next:
            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref);
            	else
            		return new OutlineObject(ref);
           
            case PdfDictionary.O:                
                return new FormObject(ref);

            case PdfDictionary.OCProperties:
                return new OCObject(ref);

            case PdfDictionary.OpenAction:
                return new FormObject(ref);
                
            case PdfDictionary.OPI:
                return new XObject(ref);

            case PdfDictionary.Outlines:
                return new OutlineObject(ref);

            case PdfDictionary.Pages:
                return new PageObject(ref);
                
            case PdfDictionary.ParentTree:
            	return new MCObject(ref);

            case PdfDictionary.Pattern:
                return new PatternObject(ref);
              
            case PdfDictionary.PC:                
                return new FormObject(ref);
                
            case PdfDictionary.Pg:
                return new PageObject(ref);
	
            case PdfDictionary.PI:                
                return new FormObject(ref);
                
            case PdfDictionary.PO:                
                return new FormObject(ref);

            case PdfDictionary.Popup:                
                PdfObject obj= new FormObject(ref);
                obj.setBoolean(PdfDictionary.Open,false);
                return obj;

            case PdfDictionary.Properties:
                return new OCObject(ref);
                
            case PdfDictionary.PV:                
                return new FormObject(ref);
            
            case PdfDictionary.R:                
                return new FormObject(ref, parentID);
                
            case PdfDictionary.Resources:
                return new ResourcesObject(ref);

            case PdfDictionary.RoleMap:
            	return new MCObject(ref);

            case PdfDictionary.Root:
            	return new PageObject(ref);
            	
            case PdfDictionary.Shading:
                return new ShadingObject(ref);

            case PdfDictionary.SMask:
                return new MaskObject(ref);

            case PdfDictionary.Sound:
                return new SoundObject(ref);
            
            case PdfDictionary.StructTreeRoot:
            	return new MCObject(ref);

            case PdfDictionary.Style:
                return new FontObject(ref);    
                
            case PdfDictionary.ToUnicode:
                return new FontObject(ref); 
                
            case PdfDictionary.TR:
            	return new MaskObject(ref); 
            	
            case PdfDictionary.U:                
                return new FormObject(ref);

            case PdfDictionary.Usage:
                return new OCObject(ref);
                
            case PdfDictionary.V:                
                return new FormObject(ref);

            case PdfDictionary.Win:
            	return new FormObject(ref);

                
            case PdfDictionary.WP:                
                return new FormObject(ref);
                
            case PdfDictionary.WS:                
                return new FormObject(ref);
                
            case PdfDictionary.X:                
                return new FormObject(ref);
            
            case PdfDictionary.XObject:
                return new XObject(ref); 

            case PdfDictionary.Zoom:
                return new OCObject(ref);
            
            default:

            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref);
            
        }
        
        return new PdfObject(ref);
    }

     public static PdfObject createObject(int id, int ref,int gen, int parentType) {

        switch(id){

        	
            case PdfDictionary.A:
            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref, gen);
            	else if(parentType==PdfDictionary.MCID)
            		return new MCObject(ref, gen);
            	else
            		return new OutlineObject(ref, gen);
	    		
            case PdfDictionary.AA:
            	return new FormObject(ref,gen);

            case PdfDictionary.AP:
            	return new FormObject(ref,gen);

            case PdfDictionary.BI:
            	return new FormObject(ref, gen);
            		
            case PdfDictionary.Bl:
                return new FormObject(ref, gen);
            	
            case PdfDictionary.BS:
                return new FormObject(ref, gen);
                
            case PdfDictionary.C:
            	return new FormObject(ref, gen);

            //case PdfDictionary.C2:
            	//return new FormObject(ref, gen);
            	
            case PdfDictionary.CF:
	    		return new EncryptionObject(ref, gen);
    		
        	case PdfDictionary.CharProcs:
        		return new FontObject(ref, gen);

        	case PdfDictionary.CIDSystemInfo:
        		return new FontObject(ref, gen);

        	case PdfDictionary.CIDToGIDMap:
        		return new FontObject(ref, gen);

            case PdfDictionary.ColorSpace:
                return new ColorSpaceObject(ref,gen);

            case PdfDictionary.ClassMap:
                return new MCObject(ref,gen);

            case PdfDictionary.D:
            	if(parentType==PdfDictionary.Form){
                    return new FormObject(ref, gen);
                }else
            		return new OCObject(ref, gen);

            case PdfDictionary.DC:
            	return new FormObject(ref, gen);
            		
            case PdfDictionary.DecodeParms:
               return new DecodeParmsObject(ref, gen);

            case PdfDictionary.DescendantFonts:
                return new FontObject(ref, gen);

            case PdfDictionary.Dests:
                return new NamesObject(ref,gen);
                
            case PdfDictionary.DP:
            	return new FormObject(ref, gen);
            
            case PdfDictionary.DS:
            	return new FormObject(ref, gen);
            	
            case PdfDictionary.E:
            	return new FormObject(ref, gen);
            
            case PdfDictionary.EF:
            	return new FSObject(ref, gen);
            	
            case PdfDictionary.Encoding:
                 return new FontObject(ref, gen);

            case PdfDictionary.Extends:
                return new CompressedObject(ref, gen);
            
            case PdfDictionary.ExtGState:
                return new ExtGStateObject(ref,gen);
                
            case PdfDictionary.F:
            	return new FormObject(ref, gen);
            
            case PdfDictionary.First:
                return new OutlineObject(ref, gen);
                
            case PdfDictionary.Fo:
            	return new FormObject(ref, gen);
            	
            case PdfDictionary.FontDescriptor:
                return new FontObject(ref, gen);

            case PdfDictionary.FontFile:
                return new FontObject(ref, gen);

            case PdfDictionary.FontFile2:
                return new FontObject(ref, gen);

            case PdfDictionary.FontFile3:
                return new FontObject(ref, gen);

            case PdfDictionary.FS:
            	return new FSObject(ref, gen);
            	
            case PdfDictionary.Function:
                return new FunctionObject(ref,gen);
               
            case PdfDictionary.Group:
                return new PdfObject(ref, gen);

            case PdfDictionary.I:
                return new FormObject(ref, gen);

            case PdfDictionary.Info:
                return new InfoObject(ref, gen);
    
            case PdfDictionary.JavaScript:
                return new NamesObject(ref, gen);

            case PdfDictionary.JBIG2Globals:
                return new DecodeParmsObject(ref,gen);

            case PdfDictionary.JS:
            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref, gen);
            	else
            		return new NamesObject(ref, gen);
                
            case PdfDictionary.K:
                if(parentType==PdfDictionary.MCID)
            		return new MCObject(ref, gen);
            	else
            	    return new FormObject(ref, gen);
            
            case PdfDictionary.Layer:
                return new OCObject(ref, gen);

            case PdfDictionary.MarkInfo:
            	return new MCObject(ref,gen);

            case PdfDictionary.Mask:
                return new MaskObject(ref,gen);

            case PdfDictionary.Metadata:
                return new MetadataObject(ref,gen);

            case PdfDictionary.MK:
                return new MKObject(ref, gen);
                
            case PdfDictionary.N:
            	return new FormObject(ref,gen);
               
            case PdfDictionary.Names:
                return new NamesObject(ref,gen);

            case PdfDictionary.Next:
            	if(parentType==PdfDictionary.Form)
            		return new FormObject(ref, gen);
            	else
            		return new OutlineObject(ref, gen);
                
            case PdfDictionary.O:
            	return new FormObject(ref, gen);
               
            case PdfDictionary.OpenAction:
                return new FormObject(ref,gen);
            
            case PdfDictionary.OCProperties:
                return new OCObject(ref, gen);
                        
            case PdfDictionary.OPI:
                return new XObject(ref,gen);

            case PdfDictionary.Outlines:
                return new OutlineObject(ref,gen);

            case PdfDictionary.Pages:
                return new PageObject(ref,gen);

            case PdfDictionary.ParentTree:
            	return new MCObject(ref, gen);
            	
            case PdfDictionary.Pattern:
                return new PatternObject(ref,gen);
              
            case PdfDictionary.PC:
            	return new FormObject(ref, gen);
            	
            case PdfDictionary.Pg:
                return new PageObject(ref,gen);
	
            	
            case PdfDictionary.PI:
            	return new FormObject(ref, gen);
            		
            case PdfDictionary.PO:
            	return new FormObject(ref, gen);

            case PdfDictionary.Popup:
                PdfObject obj= new FormObject(ref, gen);
                obj.setBoolean(PdfDictionary.Open,false);
                return obj;
            
            case PdfDictionary.Properties:
                return new OCObject(ref, gen);

            case PdfDictionary.PV:
            	return new FormObject(ref, gen);
            
            case PdfDictionary.R:
            	return new FormObject(ref, gen);
            	
            case PdfDictionary.Resources:
                return new ResourcesObject(ref,gen);

            case PdfDictionary.RoleMap:
                return new MCObject(ref,gen);

            case PdfDictionary.Root:
            	return new PageObject(ref,gen);
            
            case PdfDictionary.Shading:
                return new ShadingObject(ref, gen);

            case PdfDictionary.SMask:
                return new MaskObject(ref,gen);   

            case PdfDictionary.Sound:
                return new SoundObject(ref,gen);

            case PdfDictionary.StructTreeRoot:
            	return new MCObject(ref,gen);

            case PdfDictionary.TR:
                return new MaskObject(ref,gen);

            case PdfDictionary.ToUnicode:
                return new FontObject(ref,gen); 
                
            case PdfDictionary.U:
            	return new FormObject(ref, gen);
            
            case PdfDictionary.Usage:
                return new OCObject(ref, gen);
               
            case PdfDictionary.V:
                return new FormObject(ref,gen);

            case PdfDictionary.Win:
            	return new FormObject(ref, gen);

            case PdfDictionary.WP:
            	return new FormObject(ref, gen);
            	    
            case PdfDictionary.WS:
            	return new FormObject(ref, gen);
            	 	
            case PdfDictionary.X:
            	return new FormObject(ref, gen);
                 
                
            case PdfDictionary.XObject:
        		return new XObject(ref, gen);

            case PdfDictionary.Zoom:
                return new OCObject(ref, gen);

            default:

            	//if(parentType==PdfDictionary.Form)
            		//return new FormObject(ref, gen);
            

        }

        return new PdfObject(ref, gen);
    }
}
