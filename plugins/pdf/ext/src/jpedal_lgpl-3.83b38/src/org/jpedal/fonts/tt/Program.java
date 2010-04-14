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
* Program.java
* ---------------
*/
package org.jpedal.fonts.tt;

import java.util.StringTokenizer;


/**holds items for a parser*/
public class Program {
	
	private static int[] opCodes=new int[256];
	
	private static String[] op=new String[256];
	
	/**put op codes in array*/
	static{
		String[] rawValues={"NPUSHB[ ]","40",
				"NPUSHW[ ]","41",
				"AA[ ]","7F",
				"ABS[ ]","64",
				"ADD[ ]","60",
				"ALIGNPTS[ ]","27",
				"ALIGNRP[ ]","3C",
				"AND[ ]","5A",
				"CALL[ ]","2B",
				"CEILING[ ]","67",
				"CINDEX[ ]","25",
				"CLEAR[ ]","22",
				"DEBUG[ ]","4F",
				"DELTAC1[ ],","73",
				"DELTAC2[ ]","74",
				"DELTAC3[ ]","75",
				"DELTAP1[ ]","5D",
				"DELTAP2[ ]","71",
				"DELTAP3[ ]","72",
				"DEPTH[ ]","24",
				"DIV[ ]","62",
				"DUP[ ]","20",
				"EIF[ ]","59",
				"ELSE","1B",
				"ENDF[ ]","2D",
				"EQ[ ]","54",
				"EVEN[ ]","57",
				"FDEF[ ]","2C",
				"FLIPOFF[ ]","4E",
				"FLIPON[ ]","4D",
				"FLIPPT[ ]","80",
				"FLIPRGOFF[ ]","82",
				"FLIPRGON[ ]","81",
				"FLOOR[ ]","66",
				"GC[a]","46 - 47",
				"GETINFO[ ]","88",
				"GFV[ ]","0D",
				"GPV[ ]","0C",
				"GT[ ]","52",
				"GTEQ[ ]","53",
				"IDEF[ ]","89",
				"IF[ ]","58",
				"INSTCTRL","8E",
				"IP[ ]","39",
				"ISECT[ ]","0F",
				"IUP[a]","30",
				"IUP[a]","31",
				"JMPR","1C",
				"JROF[ ]","79",
				"JROT[ ]","78",
				"LOOPCALL[ ]","2A",
				"LT[ ]","50",
				"LTEQ[ ]","51",
				"MAX[ ]","8B",
				"MD[a]","49 - 4A",
				"MD[a]","49 - 4A",		
				"MDAP[ a ]","2E - 2F",
				"MDRP[abcde]","C0 - DF",
				"MIAP[a]","3E - 3F",
				"MIN[ ]","8C",
				"MINDEX[ ]","26",
				"MIRP[abcde]","E0 - FF",
				"MPPEM[ ]","4B",
				"MPS[ ]","4C",
				"MSIRP[a]","3A - 3B",
				"MUL[ ]","63",
				"NEG[ ]","65",
				"NEQ[ ]","55",
				"NOT[ ]","5C",
				"NROUND[ab]","6C - 6F",
				"ODD[ ]","56",
				"OR[ ]","5B",
				"POP[ ]","21",
				"PUSHB[abc]","B0 - B7",
				"PUSHW[abc]","B8 - BF",
				"RCVT[ ]","45",
				"RDTG[ ]","7D",
				"ROFF[ ]","7A",
				"ROLL","8a",
				"ROUND[ab]","68 - 6B",
				"RS[ ]","43",
				"RTDG[ ]","3D",
				"RTG[ ]","18",
				"RTHG[ ]","19",
				"RUTG[ ]","7C",
				"S45ROUND[ ]","77",
				"SANGW[ ]","7E",
				"SCANCTRL[ ]","85",
				"SCANTYPE[ ]","8D",
				"SCFS[ ]","48",
				"SCVTCI[ ]","1D",
				"SDB[ ]","5E",
				"SDPVTL[a]","86 - 87",
				"SDS[ ]","5F",
				"SFVFS[ ]","0B",
				"SFVTCA[a]","04 - 05",
				"SFVTL[a]","08 - 09",
				"SFVTPV[ ]","0E",
				"SHC[a]","34 - 35",
				"SHP[a]","32 - 33",
				"SHPIX[ ]","38",
				"SHZ[a]","36 - 37",
				"SLOOP[ ]","17",
				"SMD[ ]","1A",
				"SPVFS[ ]","0A",
				"SPVTCA[a]","02 - 03",
				"SPVTL[a]","06 - 07",
				"SROUND[ ]","76",
				"SRP0[ ]","10",
				"SRP1[ ]","11",
				"SRP2[ ]","12",
				"SSW[ ]","1F",
				"SSWCI[ ]","1E",
				"SUB[ ]","61",
				"SVTCA[a]","00 - 01",
				"SWAP[ ]","23",
				"SZP0[ ]","13",
				"SZP1[ ]","14",
				"SZP2[ ]","15",
				"SZPS[ ]","16",
				"UTP[ ]","29",
				"WCVTF[ ]","70",
				"WCVTP[ ]","44",
				"WS[ ]","42"
		};
		
		//read values - run of numbers of single value
		for(int i=0;i<rawValues.length;i=i+2){
			
			if(rawValues[i+1].indexOf('-')==-1){
				int opNum=Integer.parseInt(rawValues[i+1],16);
				opCodes[opNum]=opNum;
				op[opNum]=rawValues[i];
			}else{
				StringTokenizer vals=new StringTokenizer(rawValues[i+1]," -");
				int start=Integer.parseInt(vals.nextToken(),16);
				int end=Integer.parseInt(vals.nextToken(),16)+1;
				for(int j=start;j<end;j++){
					opCodes[j]=start;
					op[j]=rawValues[i];
				}
			}
		}
	}
	
}
