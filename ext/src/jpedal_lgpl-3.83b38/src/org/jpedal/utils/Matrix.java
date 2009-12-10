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
* Matrix.java
* ---------------
*/
package org.jpedal.utils;

/**
 provide matrix functionality used in pdf to calculate co-ords
 */
public class Matrix
{
	public Matrix() 
	{
		
	}
	
	//////////////////////////////////////////////////////////////////////////
	/**multiply two 3 * 3 matrices together & return result*/
	final public static float[][] multiply( float[][] matrix1, float[][] matrix2 )
	{
		
		//output matrix for results
		float[][] output_matrix = new float[3][3];
		
		//multiply
		for( int col = 0;col < 3;col++ )
		{
			for( int row = 0;row < 3;row++ ){
				output_matrix[row][col] = ( matrix1[row][0] * matrix2[0][col] ) + ( matrix1[row][1] * matrix2[1][col] ) + ( matrix1[row][2] * matrix2[2][col] );
				//allow for rounding errors
				/**
				if((output_matrix[row][col]>0.99)&&(output_matrix[row][col]<1))
					output_matrix[row][col]=1;
				else if((output_matrix[row][col]<-0.99)&&(output_matrix[row][col]>-1))
					output_matrix[row][col]=-1;
				else if((output_matrix[row][col]>0.0)&&(output_matrix[row][col]<0.001))
					output_matrix[row][col]=0;
				else if((output_matrix[row][col]<0.0)&&(output_matrix[row][col]>-0.001))
					output_matrix[row][col]=0;
					*/
				
				//if(Math.abs(output_matrix[row][col])<0.01)
					//output_matrix[row][col] =0;
			}
		}
		return output_matrix;
	}
	//////////////////////////////////////////////////////////////////////////
	/**show matrix (used to debug)*/
	final public static void show( float[][] matrix1 )
	{
		
		//show lines
		for( int row = 0;row < 3;row++ ){
			LogWriter.writeLog( row + "((" + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " ))" );
			//System.out.println( row + "(" + matrix1[row][0] + " , " + matrix1[row][1] + " , " + matrix1[row][2] + " )" );
		}
	}

	
	/**show matrix (used to debug)*/
	final public static boolean compare( float[][] m1,float[][] m2 )
	{
		
		boolean isIdentical=true;
		
		//show lines
		for( int r = 0;r < 3;r++ ){
			for( int c = 0;c < 3;c++ ){
				if(m1[r][c]!=m2[r][c])
				isIdentical=false;
			}
			
		}
		
		return isIdentical;
	}	
}
