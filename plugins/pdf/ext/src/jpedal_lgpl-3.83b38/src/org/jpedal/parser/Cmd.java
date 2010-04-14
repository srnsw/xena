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
* Cmd.java
* ---------------
*/
package org.jpedal.parser;

/**
 * holds int value for every postscript command
 */
public class Cmd {

	final protected static int Tc = 21603;

	final protected static int Tw = 21623;

	final protected static int Tz = 21626;

	final protected static int TL = 21580;

	final protected static int Tf = 21606;

	final protected static int Tr = 21618;

	final protected static int Ts = 21619;

	final protected static int Td = 21604;

	final protected static int TD = 21572;

	final protected static int Tm = 21613;

	final protected static int Tstar = 21546;

	final protected static int Tj = 21610;

	final protected static int TJ = 21578;

	final protected static int quote = 39;

	final protected static int doubleQuote = 34;

	//////////////////////// /

	final protected static int BI = 16969;

	final protected static int ID = 18756;

	final protected static int m = 109;

	final protected static int l = 108;

	final protected static int c = 99;

	final protected static int d = 100;

	final protected static int v = 118;

	final protected static int y = 121;

	final protected static int h = 104;

	final protected static int re = 29285;

	final protected static int S = 83;

	final protected static int s = 115;

	final protected static int f = 102;

	final protected static int F = 70;

	final protected static int fstar = 26154;
	
	final protected static int Fstar = 17962;

	final protected static int B = 66;

	final protected static int Bstar = 16938;

	final protected static int b = 98;

	final protected static int bstar = 25130;

	final protected static int n = 110;

	final protected static int W = 87;

	final protected static int Wstar = 22314;

	//////////////////////// /

	final protected static int BT = 16980;

	final protected static int ET = 17748;

	final protected static int Do = 17519;

	final protected static int w = 119;

	final protected static int j = 106;

	final protected static int J = 74;

	final protected static int M = 77;

	final protected static int ri = 29289;

	final protected static int i = 105;

	final protected static int gs = 26483;

	final protected static int q = 113;

	final protected static int Q = 81;

	final protected static int cm = 25453;

	final protected static int d0 = 25648;

	final protected static int d1 = 25649;

	final protected static int cs = 25459;

	final protected static int CS = 17235;

	final protected static int sc = 29539;

	final protected static int scn = 7562094;

	final protected static int SC = 21315;

	final protected static int SCN = 5456718;

	final protected static int g = 103;

	final protected static int G = 71;

	final protected static int rg = 29287;

	final protected static int RG = 21063;

	final protected static int k = 107;

	final protected static int K = 75;

	final protected static int sh = 29544;

	final protected static int BMC = 4345155;

	final protected static int BDC = 4342851;

	final protected static int EMC = 4541763;

	final protected static int MP = 19792;

	final protected static int DP = 17488;

	final protected static int BX = 16984;

	final protected static int EX = 17752;

	/**identify if command*/
	protected static int getCommandID(int value) {
		int id = -1;

		switch (value) {

		case Tc:
			id = Tc;
			break;
		case Tw:
			id = Tw;
			break;
		case Tz:
			id = Tz;
			break;
		case TL:
			id = TL;
			break;
		case Tf:
			id = Tf;
			break;
		case Tr:
			id = Tr;
			break;
		case Ts:
			id = Ts;
			break;
		case Td:
			id = Td;
			break;
		case TD:
			id = TD;
			break;
		case Tm:
			id = Tm;
			break;
		case Tstar:
			id = Tstar;
			break;
		case Tj:
			id = Tj;
			break;
		case TJ:
			id = TJ;
			break;
		case quote:
			id = quote;
			break;
		case doubleQuote:
			id = doubleQuote;
			break;

		//////////////////////// /

		case BI:
			id = BI;
			break;
		case ID:
			id = ID;
			break;
		case m:
			id = m;
			break;
		case l:
			id = l;
			break;
		case c:
			id = c;
			break;
		case d:
			id = d;
			break;
		case v:
			id = v;
			break;
		case y:
			id = y;
			break;
		case h:
			id = h;
			break;
		case re:
			id = re;
			break;
		case S:
			id = S;
			break;
		case s:
			id = s;
			break;
		case f:
			id = f;
			break;
		case F:
			id = F;
			break;
		case fstar:
			id = fstar;
			break;
		case Fstar:
			id = Fstar;
			break;
		case B:
			id = B;
			break;
		case Bstar:
			id = Bstar;
			break;
		case b:
			id = b;
			break;
		case bstar:
			id = bstar;
			break;
		case n:
			id = n;
			break;
		case W:
			id = W;
			break;
		case Wstar:
			id = Wstar;
			break;

		//////////////////////// /

		case BT:
			id = BT;
			break;
		case ET:
			id = ET;
			break;
		case Do:
			id = Do;
			break;
		case w:
			id = w;
			break;
		case j:
			id = j;
			break;
		case J:
			id = J;
			break;
		case M:
			id = M;
			break;
		case ri:
			id = ri;
			break;
		case i:
			id = i;
			break;
		case gs:
			id = gs;
			break;
		case q:
			id = q;
			break;
		case Q:
			id = Q;
			break;
		case cm:
			id = cm;
			break;
		case d0:
			id = d0;
			break;
		case d1:
			id = d1;
			break;
		case cs:
			id = cs;
			break;
		case CS:
			id = CS;
			break;
		case sc:
			id = sc;
			break;
		case scn:
			id = scn;
			break;
		case SC:
			id = SC;
			break;
		case SCN:
			id = SCN;
			break;
		case g:
			id = g;
			break;
		case G:
			id = G;
			break;
		case rg:
			id = rg;
			break;
		case RG:
			id = RG;
			break;
		case k:
			id = k;
			break;
		case K:
			id = K;
			break;
		case sh:
			id = sh;
			break;
		case BMC:
			id = BMC;
			break;
		case BDC:
			id = BDC;
			break;
		case EMC:
			id = EMC;
			break;
		case MP:
			id = MP;
			break;
		case DP:
			id = DP;
			break;
		case BX:
			id = BX;
			break;
		case EX:
			id = EX;
			break;

		//////////////////////// /
		}

		return id;
	}

	/**convert command into string*/
	protected static String getCommandAsString(int value) {
		String id = "";

		switch (value) {

		case Tc:
			id = "Tc";
			break;
		case Tw:
			id = "Tw";
			break;
		case Tz:
			id = "Tz";
			break;
		case TL:
			id = "TL";
			break;
		case Tf:
			id = "Tf";
			break;
		case Tr:
			id = "Tr";
			break;
		case Ts:
			id = "Ts";
			break;
		case Td:
			id = "Td";
			break;
		case TD:
			id = "TD";
			break;
		case Tm:
			id = "Tm";
			break;
		case Tstar:
			id = "Tstar";
			break;
		case Tj:
			id = "Tj";
			break;
		case TJ:
			id = "TJ";
			break;
		case quote:
			id = "'";
			break;
		case doubleQuote:
			id = "\"";
			break;

		////////////////////////	 	 	/

		case BI:
			id = "BI";
			break;
		case ID:
			id = "ID";
			break;
		case m:
			id = "m";
			break;
		case l:
			id = "l";
			break;
		case c:
			id = "c";
			break;
		case d:
			id = "d";
			break;
		case v:
			id = "v";
			break;
		case y:
			id = "y";
			break;
		case h:
			id = "h";
			break;
		case re:
			id = "re";
			break;
		case S:
			id = "S";
			break;
		case s:
			id = "s";
			break;
		case f:
			id = "f";
			break;
		case F:
			id = "F";
			break;
		case fstar:
			id = "f*";
			break;
		case Fstar:
			id = "F*";
			break;
		case B:
			id = "B";
			break;
		case Bstar:
			id = "B*";
			break;
		case b:
			id = "b";
			break;
		case bstar:
			id = "b*";
			break;
		case n:
			id = "n";
			break;
		case W:
			id = "W";
			break;
		case Wstar:
			id = "W*";
			break;

		////////////////////////	 	 	/

		case BT:
			id = "BT";
			break;
		case ET:
			id = "ET";
			break;
		case Do:
			id = "Do";
			break;
		case w:
			id = "w";
			break;
		case j:
			id = "j";
			break;
		case J:
			id = "J";
			break;
		case M:
			id = "M";
			break;
		case ri:
			id = "ri";
			break;
		case i:
			id = "i";
			break;
		case gs:
			id = "gs";
			break;
		case q:
			id = "q";
			break;
		case Q:
			id = "Q";
			break;
		case cm:
			id = "cm";
			break;
		case d0:
			id = "d0";
			break;
		case d1:
			id = "d1";
			break;
		case cs:
			id = "cs";
			break;
		case CS:
			id = "CS";
			break;
		case sc:
			id = "sc";
			break;
		case scn:
			id = "scn";
			break;
		case SC:
			id = "SC";
			break;
		case SCN:
			id = "SCN";
			break;
		case g:
			id = "g";
			break;
		case G:
			id = "G";
			break;
		case rg:
			id = "rg";
			break;
		case RG:
			id = "RG";
			break;
		case k:
			id = "k";
			break;
		case K:
			id = "K";
			break;
		case sh:
			id = "sh";
			break;
		case BMC:
			id = "BMC";
			break;
		case BDC:
			id = "BDC";
			break;
		case EMC:
			id = "EMC";
			break;
		case MP:
			id = "MP";
			break;
		case DP:
			id = "DP";
			break;
		case BX:
			id = "BX";
			break;
		case EX:
			id = "EX";
			break;

		////////////////////////	 	 	/

		}

		return id;
	}

}
