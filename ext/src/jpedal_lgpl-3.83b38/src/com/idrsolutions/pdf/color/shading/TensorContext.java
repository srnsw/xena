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
* TensorContext.java
* ---------------
*/
package com.idrsolutions.pdf.color.shading;

import java.awt.PaintContext;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

public class TensorContext implements PaintContext {

	private byte[] stream;
	private byte[] currentTile = new byte[32];
	private byte[] lastTile;
	private byte[] currentColor = new byte[4];
	private byte[] lastColor;
	private byte flag = 0;
	private int pointer = 0;
    
    public TensorContext(byte[] stream){

        this.stream=stream;
    }
	
	public void beginMeshing(){
		flag = 0;
		pointer = 0;
		
		while((pointer+26)<stream.length){
			flag = stream[pointer];
			pointer++;
			
			switch(flag){
			case 0 : pointer = f0(stream, pointer);break;
			case 1 : pointer = f1(stream, pointer);break;
			case 2 : pointer = f2(stream, pointer);break;
			case 3 : pointer = f3(stream, pointer);break;
			default : break;
			}
		}
	}
	
	public int f0(byte[] stream, int pt){
		lastTile = currentTile;
		lastColor = currentColor;
		currentTile = new byte[32];
		for(int i=0;i!=32;i++)
			currentTile[i]=stream[pt+i];
		pt+=32;
		for(int i=0;i!=4;i++)
			currentColor[i]=stream[pt+i];
		pt+=4;
		return pt;
	}
	
	public int f1(byte[] stream, int pt){
		lastTile = currentTile;
		lastColor = currentColor;
		currentTile = new byte[32];
		
		for(int i=0;i!=8;i++)
			currentTile[i]=lastTile[6+i];
		
		for(int i=0;i!=24;i++)
			currentTile[8+i]=stream[pt+i];
		pt+=24;
		
		for(int i=0;i!=2;i++)
			currentColor[i]=lastColor[1+i];
		
		for(int i=0;i!=2;i++)
			currentColor[2+i]=stream[pt+i];
		
		pt+=2;
		
		return pt;
	}
	
	public int f2(byte[] stream, int pt){
		lastTile = currentTile;
		lastColor = currentColor;
		currentTile = new byte[32];
		
		for(int i=0;i!=8;i++)
			currentTile[i]=lastTile[12+i];
		
		for(int i=0;i!=24;i++)
			currentTile[8+i]=stream[pt+i];
		pt+=24;
		
		for(int i=0;i!=2;i++)
			currentColor[i]=lastColor[2+i];
		
		for(int i=0;i!=2;i++)
			currentColor[2+i]=stream[pt+i];
		
		pt+=2;
		
		return pt;
	}
	
	public int f3(byte[] stream, int pt){
		lastTile = currentTile;
		lastColor = currentColor;
		currentTile = new byte[32];
		
		for(int i=0;i!=6;i++)
			currentTile[i]=lastTile[18+i];
		
		for(int i=0;i!=2;i++)
			currentTile[i]=lastTile[i];
		
		for(int i=0;i!=24;i++)
			currentTile[8+i]=stream[pt+i];
		pt+=24;
		
		currentColor[0]=lastColor[3];
		currentColor[1]=lastColor[0];
		
		for(int i=0;i!=2;i++)
			currentColor[2+i]=stream[pt+i];
		
		pt+=2;
		
		return pt;
	}
	
	public static float BersteinPolynomials(int axisPt, int t){
		switch(axisPt){
		case 0 : return (1-t)^3;
		case 1 : return (3*t)*((1-t)^2);
		case 2 : return ((3*t)^2)*(1-t);
		case 3 : return t^3;
		default : return t;
		}
	}
	
	public float getValue(byte[][] value, int u, int v){
		//value = x[][] || y[][]
		float returnValue = 0;
		for(int i=0;i!=3;i++){
			for(int j=0;j!=3;j++){
				returnValue = returnValue + (float)value[i][j]*BersteinPolynomials(i, u)*BersteinPolynomials(j, v);
			}
		}
		return returnValue;
	}
	
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public ColorModel getColorModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public Raster getRaster(int arg0, int arg1, int arg2, int arg3) {
		
		
		
		return null;
	}

}
