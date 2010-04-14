/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
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

  * PdfObject.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.objects.raw;


import org.jpedal.color.ColorSpaces;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.PdfFilteredReader;

import java.lang.reflect.Field;

/**
 * holds actual data for PDF file to process
 */
public class PdfDictionary {

    final public static int Unknown=-1;
    
    final public static int URI=2433561;

    /**
     * all key values as hashed values
     */

    final public static int A=17;
    
    final public static int AA=4369;
    
    final public static int AC=4371;

    final public static int AcroForm=661816444;
    
    final public static int AIS=1120547;
    
    final public static int Alternate=2054519176;
    
    final public static int AlternateSpace=-1247101998;

    final public static int Annot=1044266837;
    
    final public static int Annots=1044338049;

    final public static int AntiAlias=2055039589;
    
    final public static int AP=4384;
    
    final public static int Array=1111634266;

    final public static int ArtBox=1142050954;

    final public static int AS=4387;

    final public static int Ascent=859131783;

    final public static int Author=1144541319;
    
    final public static int AvgWidth=1249540959;

    final public static int B=18;
    
    final public static int BlackPoint=1886161824;

    final public static int Background=1921025959;

    final public static int Base=305218357;
    
    final public static int BaseEncoding=1537782955;

    final public static int BaseFont=678461817;

    final public static int BaseState=1970567530;

    final public static int BBox=303185736;
    
    final public static int BC=4627;

    final public static int BDC=1184787;
    
    final public static int BG=4631;
    
    final public static int BI=4633;

    final public static int BitsPerComponent=-1344207655;

    final public static int BitsPerCoordinate=-335950113;

    final public static int BitsPerFlag=1500422077;
    
    final public static int BitsPerSample=-1413045608;
    
    final public static int Bl=4668;

    final public static int BlackIs1=1297445940;

    final public static int BleedBox=1179546749;
    
    final public static int Blend=1010122310;

    final public static int Bounds=1161709186;
    
    final public static int Border=1110722433;

    final public static int BM=4637;

    final public static int BPC=1187859;
    
    final public static int BS=4643;
    
    final public static int Btn=1197118;

    final public static int ByteRange=2055367785;

    final public static int C=19;

    final public static int C0=4864;

    final public static int C1=4865;
    
    final public static int C2=4866;
    
    final public static int CA=4881;
    
    final public static int ca=13105;

    final public static int CapHeight=1786204300;

    final public static int Catalog=827289723;

    final public static int Cert=322257476;
    
    final public static int CF=4886;
    
    final public static int CFM=1250845;
    
    final public static int Ch=4920;

    final public static int CIDSystemInfo=1972801240;

    final public static int CharProcs=2054190454;

    final public static int CharSet=1110863221;

    final public static int CIDFontType0C=-1752352082;

    final public static int CIDToGIDMap=946823533;
    
    final public static int ClassMap=1448698499;

    final public static int CMap=320680256;

    final public static int CMapName=827223669;
    
    //use ColorSpaces.DeviceCMYK for general usage
    final private static int CMYK=320678171;

    final public static int Colors=1010783618;

    final public static int ColorSpace=2087749783;

    final public static int ColorTransform=-1263544861;

    final public static int Columns=1162902911;
    
    final public static int Components=1920898752;
    
    final public static int CompressedObject=23;

    final public static int Configs=910980737;

    final public static int ContactInfo=1568843969;

    final public static int Contents=1216184967;

    final public static int Coords=1061308290;

    //final public static int Count=1061502551; //matches Sound so changed
    final public static int Count=1061502502;

    final public static int CreationDate=1806481572;

    final public static int Creator=827818359;

    final public static int CropBox=1076199815;

    final public static int CS=4899;

    final public static int CVMRC=639443494;

    final public static int D=20;
    
    final public static int DA=5137;

    final public static int DamagedRowsBeforeError=904541242;
    
    final public static int DC=5139;

    final public static int DCT=1315620;

    final public static int Decode=859785322;

    final public static int DecodeParms=1888135062;

    final public static int DescendantFonts=-1547306032;

    final public static int Descent=860451719;

    final public static int Dest=339034948;

    final public static int Dests=893600855;

    final public static int Differences=1954328750;

    final public static int Domain=1026641277;

    final public static int DP=5152;
    
    final public static int DR=5154;
    
    final public static int DS=5155;
    
    final public static int DV=5158;

    final public static int DW=5159;
    
    final public static int E=21;

    final public static int EarlyChange=1838971823;
    
    final public static int EF=5398;

    final public static int EFF=1381910;

    final public static int Encode=859785587;

    final public static int EncodedByteAlign=-823077984;

    final public static int Encoding=1232564598;

    final public static int Encrypt=1113489015;

    //final public static int Encryption=2004590012;

    final public static int EncryptMetadata=-1815804199;

    final public static int EndOfBlock=1885240971;

    final public static int EndOfLine=1517116800;
    
    final public static int Export=1077893004;

    final public static int Extend=1144345468;

    final public static int Extends=894663815;

    final public static int ExtGState=-1938465939;

    final public static int Event=1177894489;

    final public static int F=22;
    
    final public static int FDF=1446934;
    
    final public static int Ff=5686;
    
    final public static int Fields=893143676;
    
    final public static int FileAttachment=-1113876231;

    final public static int Filter=1011108731;

    final public static int First=960643930;

    final public static int FirstChar=1283093660;

    final public static int Fit=1456452;

    final public static int FitB=372851730;

    final public static int FitBH=960762414;

    final public static int FitBV=960762428;

    final public static int FitH=372851736;

    final public static int FitHeight=1920684175;

    final public static int FitR=372851746;

    final public static int FitV=372851750;

    final public static int FitWidth=1332578399;

    final public static int Flags=1009858393;
    
    final public static int Fo=5695;

    final public static int Font=373243460;

    final public static int FontBBox=676429196;

    final public static int FontDescriptor=-1044665361;

    final public static int FontFamily=2071816377;

    final public static int FontFile=746093177;

    final public static int FontFile2=2021292334;

    final public static int FontFile3=2021292335;

    final public static int FontMatrix=-2105119560;

    final public static int FontName=879786873;

    final public static int FontStretch=2038281912;

    final public static int FontWeight=2004579768;

    final public static int Form=373244477;

    final public static int FormType=982024818;

    final public static int FreeText=980909433;
    
    final public static int FS=5667;
    
    final public static int FT=5668;

    final public static int FullScreen=2121363126;

    final public static int Function=1518239089;

    final public static int Functions=2122150301;

    final public static int FunctionType=2127019430;

    final public static int G=23;

    final public static int Gamma=826096968;
    
    final public static int Goto=390022207;
    
    final public static int GoToR=1059340089;

    final public static int Group=1111442775;

    final public static int H=24;

    final public static int Height=959926393;
    
    final public static int Hide=406402101;

    final public static int Highlight=1919840408;

    final public static int hival=960901492;

    final public static int I=25;

    final public static int ID=6420;

    final public static int Identity=1567455623;

    final public static int Identity_H=2038913669;

    final public static int Identity_V=2038913683;

    final public static int IDTree=608325193;

    final public static int IF=6422;

    final public static int IM=6429;

    final public static int Image=1026635598;

    final public static int ImageMask=1516403337;

    final public static int Index=1043608929;

    //used to hold Indexed Colorspace read, not direct key in PDF
    final public static int Indexed=895578984;

    final public static int Info=423507519;

    final public static int Ink=1654331;

    final public static int Intent=1144346498;
    
    final public static int IT=6436;

    final public static int ItalicAngle=2055844727;

    final public static int JavaScript=-2006286978;

    final public static int JS=6691;

    final public static int JBIG2Globals=1314558361;

    final public static int K=27;

    final public static int Keywords=1517780362;

    final public static int Kids=456733763;

    final public static int Lang=472989239;

    final public static int Last=472990532;

    final public static int LastChar=795440262;

    final public static int LastModified=1873390769;

    final public static int Launch=1161711465;
    
    final public static int Layer=826881374;

    final public static int Leading=878015336;

    final public static int Length=1043816557;

    final public static int Length1=929066303;

    final public static int Length2=929066304;

    final public static int Length3=929066305;

    public static final int Link = 473513531;    

    final public static int ListMode=964196217;

    final public static int Location=1618506351;

    final public static int Lock=473903931;

    final public static int Locked=859525491;

    final public static int Lookup=1060856191;

    final public static int LW=7207;

    final public static int M=29;

    //use StandardFonts.MacExpert as public value
    final static int MacExpertEncoding=-1159739105;

    //use StandardFonts.MAC as public value
    final static int MacRomanEncoding=-1511664170;

    final public static int MarkInfo=913275002;

    final public static int Mask=489767739;

    final public static int Matrix=1145198201;

    final public static int max=4010312;

    final public static int MaxLen=1209815663;

    final public static int MaxWidth=1449495647;

    final public static int MCID=487790868;

    final public static int MediaBox=1313305473;

    final public static int Metadata =1365674082;

    final public static int min=4012350;

    final public static int MissingWidth=-1884569950;

    final public static int MK=7451;

    final public static int ModDate=340689769;

    final public static int Multiply=1451587725;

    final public static int N=30;

    final public static int Name=506543413;

    final public static int Named=826094930;

    final public static int Names=826094945;

    final public static int NeedAppearances=-1483477783;

    final public static int Next=506808388;

    final public static int NextPage=1046904697;

    final public static int NM=7709;

    final public static int None=507461173;

    final public static int Normal=1111314299;

    final public static int Nums=507854147;

    final public static int O=31;

    final public static int OC=7955;

    final public static int OCGs=521344835;

    final public static int OCProperties=-1567847737;

    final public static int OFF=2037270;

    final public static int Off=2045494;

    final public static int ON=7966;

    final public static int On=7998;

    final public static int OP=7968;

    final public static int op=16192;

    final public static int Open=524301630;

    final public static int OpenAction=2037870513;

    final public static int OPI=2039833;

    final public static int OPM=2039837;

    final public static int Opt=2048068;

    final public static int Order=1110717793;

    final public static int Ordering=1635480172;

    final public static int Outlines=1485011327;

    final public static int P=32;

    final public static int PaintType=1434615449;

    final public static int Page=540096309;

    final public static int PageLabels=1768585381;

    final public static int PageMode=1030777706;

    final public static int Pages=825701731;

    final public static int Params=1110531444;

    final public static int Parent=1110793845;

    final public static int ParentTree=1719112618;

    final public static int Pattern=1146450818;

    final public static int PatternType=1755231159;
    
    final public static int PC=8211;

    //use StandardFonts.PDF as public value
    final static int PDFDocEncoding=1602998461;

    final public static int Perms=893533539;
    
    final public static int Pg=8247;
    
    final public static int PI=8217;
    
    final public static int PO=8223;

    final public static int Popup=1061176672;

    final public static int Predictor=1970893723;

    final public static int Prev=541209926;

    final public static int Print=1111047780;

    final public static int PrintState=2104469658;

    final public static int Process=861242754;

    final public static int ProcSet=860059523;

    final public static int Producer=1702196342;

    final public static int Properties=-2089186617;
    
    final public static int PV=8230;

    final public static int Q=33;

    final public static int QuadPoints=1785890247;

    final public static int R=34;

    final public static int Range=826160983;

    final public static int RBGroups=1633113989;

    final public static int RC=8723;

    final public static int Reason=826499443;
    
    final public static int Rect=573911876;

    final public static int Reference=1786013849;

    final public static int Registry=1702459778;
    
    final public static int ResetForm=1266841507;

    final public static int Resources=2004251818;

    //convert to DeviceRGB
    final public static int RGB=2234130;

    final public static int RD=8724;

  	final public static int Root=574570308;

    final public static int RoleMap=893350012;

    final public static int Rotate=1144088180;

    final public static int Rows=574572355;

    final public static int RV=8742;

    final public static int S=35;

    final public static int SA=8977;

    final public static int SaveAs=1177891956;
    
    final public static int SetOCGState=1667731612;

    final public static int Square=1160865142;

    final public static int Shading=878474856;

    final public static int ShadingType=1487255197;

    final public static int Sig=2308407;

    final public static int SigFlags=1600810585;

    final public static int Signed=926832749;
    
    final public static int Size=590957109;

    final public static int SM=8989;

    final public static int SMask=489767774;

    final public static int Sound=1061502534;

    final public static int Stamp=1144077667;
    
    final public static int Standard=1467315058;

    //use StandardFonts.STD as public value
    final static int StandardEncoding=-1595087640;

    final public static int State=1144079448;
    
    final public static int StemH=1144339771;

    final public static int StemV=1144339785;

    final public static int StmF=591674646;

    final public static int StrF=591675926;

    final public static int StructParent=-1732403014;

    final public static int StructParents=-1113539877;

    final public static int StructTreeRoot=-2000237823;

    final public static int Style=1145650264;

    final public static int SubFilter=-2122953826;

    final public static int Subj=591737402;

    final public static int Subject=978876534;

    final public static int SubmitForm=1216126662;
    
    final public static int Subtype=1147962727;

    final public static int Supplement=2104860094;

    final public static int T=36;

    final public static int Tabs=607203907;

    final public static int Text=607471684;

    final public static int TI=9241;

    final public static int TilingType=1619174053;

    final public static int tintTransform=-1313946392;

    final public static int Title=960773209;

    final public static int TM=9245;

    final public static int Toggle=926376052;

    final public static int ToUnicode=1919185554;

    final public static int TP=9248;

    final public static int TR=9250;

    final public static int Trapped=1080325989;

    final public static int TrimBox=1026982273;
    
    final public static int Tx=9288;

    final public static int TxFontSize=964209857;

    final public static int TxOutline=-2074573923;

    final public static int TU=9253;

    final public static int Type=608780341;

    final public static int U=37;
    
    final public static int UF=9494;

    final public static int Uncompressed=-1514034520;

    final public static int Unsigned=1551661165;
    
    final public static int Usage=1127298906;

    final public static int V=38;

    final public static int Widget=876043389;

    final public static int View=641283399;

    final public static int W=39;

    final public static int WhitePoint=2021497500;

    final public static int Win=2570558;
    
    //use StandardFonts.WIN as public value
    final static int WinAnsiEncoding = 1524428269;

    final public static int Width=959726687;

    final public static int Widths=876896124;
    
    final public static int WP=10016;
    
    final public static int WS=10019;
    
    final public static int X=40;

    final public static int XFA=2627089;

    final public static int XHeight=962547833;

    final public static int XObject=979194486;

    final public static int XRefStm=910911090;

    final public static int XStep=591672680;
    
    final public static int XYZ=2631978;

    final public static int YStep=591672681;

    final public static int Zoom=708788029;

    final public static int ZoomTo=1060982398;

    final public static int Unchanged=2087349642;

    /**
     * types of Object value found
     */

    public static final int VALUE_IS_DICTIONARY = 1;

    public static final int VALUE_IS_DICTIONARY_PAIRS = 2;

    public static final int VALUE_IS_STRING_CONSTANT = 3;

    public static final int VALUE_IS_STRING_KEY = 4;

    public static final int VALUE_IS_UNREAD_DICTIONARY = 5;

    public static final int VALUE_IS_INT = 6;

    public static final int VALUE_IS_FLOAT = 7;

    public static final int VALUE_IS_BOOLEAN = 8;

    public static final int VALUE_IS_INT_ARRAY = 9;

    public static final int VALUE_IS_FLOAT_ARRAY = 10;

    public static final int VALUE_IS_BOOLEAN_ARRAY = 12;

    public static final int VALUE_IS_KEY_ARRAY = 14;

    public static final int VALUE_IS_DOUBLE_ARRAY = 16;

    public static final int VALUE_IS_MIXED_ARRAY = 18;

    public static final int VALUE_IS_STRING_ARRAY = 20;

    public static final int VALUE_IS_OBJECT_ARRAY = 22;

    public static final int VALUE_IS_TEXTSTREAM=25;

    public static final int VALUE_IS_NAME = 30;

    public static final int VALUE_IS_NAMETREE = 35;

    public static final int VALUE_IS_VARIOUS = 40;

    final public static int XFA_TEMPLATE=1013350773;

    final public static int XFA_DATASET=1130793076;

    final public static int XFA_CONFIG=1043741046;

    final public static int XFA_PREAMBLE=1031041382;

    final public static int XFA_LOCALESET=1951819392;

    final public static int XFA_PDFSECURITY=1701743524;

    final public static int XFA_XMPMETA=1026916721;

    final public static int XFA_XFDF=3552310;

    final public static int XFA_POSTAMBLE=2088075366;
    final static public int STANDARD=0;
    final static public int LOWERCASE=1;
    final static public int REMOVEPOSTSCRIPTPREFIX=2;

    /**
     * convert stream int key for dictionary entry
     */
    public static Object getKey(int keyStart, int keyLength, byte[] raw) {

        //save pair and reset
        byte[] bytes=new byte[keyLength];

        System.arraycopy(raw,keyStart,bytes,0,keyLength);

        return new String(bytes);
    }

    /**
     * convert stream int key for dictionary entry
     */
    public static int getIntKey(int keyStart, int keyLength, byte[] raw) {

        /**

        byte[] a="EndOfBlock".getBytes();

        keyStart=0;
        keyLength=a.length;
        raw=a;
        //PdfObject.debug=true;

    	byte[] bytes=new byte[keyLength];

        System.arraycopy(raw,keyStart,bytes,0,keyLength);

        System.out.println("final public static int "+new String(bytes)+"="+generateChecksum(keyStart, keyLength, raw)+";");
         if(1==1)
                 throw new RuntimeException("xx");
         /**/

        //get key
        int id = generateChecksum(keyStart, keyLength, raw);
        int PDFkey=id;// standard setting is to use value

        /**
         * non-standard values
         */
        switch(id){

            case BPC:
                PDFkey=BitsPerComponent;
                break;

            case CMYK:
            	PDFkey=ColorSpaces.DeviceCMYK;
            	break;

            case CS:
                PDFkey=ColorSpace;
                break;

            case DCT:
            	return PdfFilteredReader.DCTDecode;

            case DP:
                PDFkey=DecodeParms;
                break;

            case PdfFilteredReader.Fl:
                PDFkey=PdfFilteredReader.FlateDecode;
                break;

            case IM:
                PDFkey=ImageMask;
                break;

            case I:
                PDFkey=Indexed;
                break;

            case MacExpertEncoding:
            	PDFkey=StandardFonts.MACEXPERT;
            	break;

            case MacRomanEncoding:
                PDFkey=StandardFonts.MAC;
                break;

            case Params:
                PDFkey=DecodeParms;
                break;

            case PDFDocEncoding:
                PDFkey=StandardFonts.PDF;
                break;

            case RGB:
            	PDFkey=ColorSpaces.DeviceRGB;
            	break;

            case StandardEncoding:
                PDFkey=StandardFonts.STD;
                break;

            case WinAnsiEncoding:
                PDFkey=StandardFonts.WIN;
                break;
        }

        return PDFkey;
    }

	public static int generateChecksum(int keyStart, int keyLength, byte[] raw) {
		//convert token to unique key
        int id=0,x=0,next;

        for(int i2=keyLength-1;i2>-1;i2--){
            next=raw[keyStart+i2];

            next=next-48;

            id=id+((next)<<x);

            x=x+8;
        }

        if(id==1061502551 || id==-2006286936){ //its a duplicate so different formula:-(

           // System.out.println((char)raw[keyStart]+" "+(char)raw[keyStart+keyLength-1]);
            //System.out.println((int)raw[keyStart]+" "+(int)raw[keyStart+keyLength-1]);

            id=id+raw[keyStart]-raw[keyStart+keyLength-1];
        }
        return id;
	}

    /**
     * get type of object
     */

    public static int getKeyType(int id, int type) {

        int PDFkey=-1;


        switch(id){

        	case A:
        		return VALUE_IS_DICTIONARY;

        	case AA:
                return VALUE_IS_UNREAD_DICTIONARY;

        	case AC:
        		return VALUE_IS_TEXTSTREAM;

            case AcroForm:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Alternate:
	        	return VALUE_IS_STRING_CONSTANT;

        	case AIS:
        		return VALUE_IS_BOOLEAN;

            case Annots:
                return VALUE_IS_KEY_ARRAY;

            case AntiAlias:
                return VALUE_IS_BOOLEAN;

            case AP:
            	return VALUE_IS_DICTIONARY;

            case Array:
                return VALUE_IS_FLOAT_ARRAY;

            case ArtBox:
                return VALUE_IS_FLOAT_ARRAY;

            case AS:
                if(type==PdfDictionary.OCProperties)
                    return VALUE_IS_KEY_ARRAY;
            	else
                    return VALUE_IS_NAME;

            case Author:
                return VALUE_IS_TEXTSTREAM;

            case B:
                if(type==PdfDictionary.Sound)
                    return VALUE_IS_INT;
                //else
                  //  return PDFkey;
                break;

            case Background:
                return VALUE_IS_FLOAT_ARRAY;

            case Base:
                return VALUE_IS_TEXTSTREAM;

            case BaseEncoding:
                return VALUE_IS_STRING_CONSTANT;

            case BaseFont:
                return VALUE_IS_NAME;

            case BaseState:
                return VALUE_IS_NAME;

            case BBox:
                return VALUE_IS_FLOAT_ARRAY;

            case BC:
            	return VALUE_IS_FLOAT_ARRAY;

            case BG:
            	if(type==PdfDictionary.MK)
            		return VALUE_IS_FLOAT_ARRAY;
            	else
            		return VALUE_IS_UNREAD_DICTIONARY;

            case BI:
            	return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case BitsPerComponent:
                    return VALUE_IS_INT;

            case BitsPerCoordinate:
                    return VALUE_IS_INT;

            case BitsPerFlag:
                    return VALUE_IS_INT;

            case BitsPerSample:
            	return VALUE_IS_INT;

            case Bl:
            	return VALUE_IS_DICTIONARY;
            	
            case BlackIs1:
            	return VALUE_IS_BOOLEAN;

            case BlackPoint:
                return VALUE_IS_FLOAT_ARRAY;

            case BleedBox:
                return VALUE_IS_FLOAT_ARRAY;

            case Blend:
                return VALUE_IS_INT;

            case Border:
                return VALUE_IS_MIXED_ARRAY;

            case Bounds:
                return VALUE_IS_FLOAT_ARRAY;

            case BM:
                return VALUE_IS_MIXED_ARRAY;

            case BS:
            	return VALUE_IS_DICTIONARY;

            case ByteRange:
                return VALUE_IS_INT_ARRAY;

            case C:
            	if(type==PdfDictionary.Form || type==PdfDictionary.MCID)
            		return PdfDictionary.VALUE_IS_VARIOUS;  
            	else if(type==PdfDictionary.Sound)
            		return PdfDictionary.VALUE_IS_INT;  
            	else
            		return VALUE_IS_FLOAT_ARRAY;

            case C0:
                return VALUE_IS_FLOAT_ARRAY;

            case C1:
            	//if(type==PdfDictionary.Form)
            		//return PdfDictionary.VALUE_IS_DICTIONARY;  
            	//else
            		return VALUE_IS_FLOAT_ARRAY;

            //case C2:
            	//if(type==PdfDictionary.Form)
            	//	return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case CA:
            	if(type==Form || type==PdfDictionary.MK)
            		return VALUE_IS_TEXTSTREAM;
            	else
            		return VALUE_IS_FLOAT;

            case ca:
                return VALUE_IS_FLOAT;

            case Cert:
                return VALUE_IS_VARIOUS;

            case CF:
            	return VALUE_IS_DICTIONARY_PAIRS;

            case CFM:
                return VALUE_IS_NAME;

            case CharProcs:
                return VALUE_IS_DICTIONARY_PAIRS;

            case CharSet:
                return VALUE_IS_TEXTSTREAM;

            case ClassMap:
                return VALUE_IS_DICTIONARY;

            case CMapName:
                return VALUE_IS_NAME;

            case Colors:
                return VALUE_IS_INT;

            case ColorTransform:
                return VALUE_IS_INT;

            case ColorSpace:
                if(type==XObject)
                    return VALUE_IS_UNREAD_DICTIONARY;
                else
                    return VALUE_IS_DICTIONARY;

            case Columns:
                return VALUE_IS_INT;

            case Configs:
                return VALUE_IS_KEY_ARRAY;

            case ContactInfo:
                return VALUE_IS_TEXTSTREAM;

            case Contents:
            	if(type==Form)
            		return VALUE_IS_TEXTSTREAM;
            	else
            		return VALUE_IS_KEY_ARRAY;

            case Coords:
                return VALUE_IS_FLOAT_ARRAY;

            case Count:
                return VALUE_IS_INT;

            case CreationDate:
            	return VALUE_IS_TEXTSTREAM;

            case Creator:
                return VALUE_IS_TEXTSTREAM;

            case CropBox:
                return VALUE_IS_FLOAT_ARRAY;

            case CIDSystemInfo:
                return VALUE_IS_DICTIONARY;

            case CIDToGIDMap:
                return VALUE_IS_DICTIONARY;

            case CVMRC:
            	return VALUE_IS_STRING_CONSTANT;

            case D:
            	//if(type==PdfDictionary.OCProperties || type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;

            case DA:
//                if(type==Form)
//                    return VALUE_IS_DICTIONARY;
//                else
            	return VALUE_IS_TEXTSTREAM;

            case DamagedRowsBeforeError:
                return VALUE_IS_INT;

            case DC:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case Decode:
                return VALUE_IS_FLOAT_ARRAY;

            case DecodeParms:
                return VALUE_IS_DICTIONARY;

            case DescendantFonts:
                return VALUE_IS_DICTIONARY;

            case Dest:
                return VALUE_IS_MIXED_ARRAY;

            case Dests:
                return VALUE_IS_DICTIONARY;

            case Differences:
                return VALUE_IS_MIXED_ARRAY;

            case Domain:
                return VALUE_IS_FLOAT_ARRAY;

            case DP:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case DR:
                return VALUE_IS_UNREAD_DICTIONARY;

            case DV:
            	return VALUE_IS_VARIOUS;

            case DS:
            	if(type==PdfDictionary.Form)
            		return VALUE_IS_VARIOUS;//TEXTSTREAM;
            	else
            		return VALUE_IS_TEXTSTREAM;

            case DW:
                return VALUE_IS_INT;
                
            case E:
            	//if(type==PdfDictionary.OCProperties || type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_VARIOUS;    

            case EF:
            	return VALUE_IS_UNREAD_DICTIONARY;

            		
            case EarlyChange:
                return VALUE_IS_INT;

            case EncodedByteAlign:
                return VALUE_IS_BOOLEAN;

            case Encode:
                return VALUE_IS_FLOAT_ARRAY;

            case Encoding:
                return VALUE_IS_DICTIONARY;

            case Encrypt:
                return VALUE_IS_UNREAD_DICTIONARY;

            case EncryptMetadata:
            	return VALUE_IS_BOOLEAN;

            case EndOfBlock:
                return VALUE_IS_BOOLEAN;

            case EndOfLine:
                return VALUE_IS_BOOLEAN;
            
            case Event:
            	return VALUE_IS_STRING_CONSTANT;

            case Extend:
                return VALUE_IS_BOOLEAN_ARRAY;

            case Extends:
                return VALUE_IS_DICTIONARY;

            case ExtGState:
                return VALUE_IS_DICTIONARY;

            case F:
            	if (type==PdfDictionary.Form || type==PdfDictionary.Outlines ||type==PdfDictionary.FS)
           			return PdfDictionary.VALUE_IS_VARIOUS;
            	else if (type==PdfDictionary.FDF)
            		return PdfDictionary.VALUE_IS_TEXTSTREAM;
                else
            		return PdfDictionary.VALUE_IS_INT;

            case Ff:
            	return VALUE_IS_INT;

            case Fields:
            	if(type==PdfDictionary.FDF)
            		return VALUE_IS_KEY_ARRAY;
            	else
            		return VALUE_IS_MIXED_ARRAY;

            case Filter:
                return VALUE_IS_MIXED_ARRAY;

            case First:
            	if (type==PdfDictionary.FS)
    	        	return VALUE_IS_VARIOUS;
    	        else if (type==PdfDictionary.Outlines)
    	        	return VALUE_IS_UNREAD_DICTIONARY;
	        	else
	                return VALUE_IS_INT;

            case FirstChar:
                return VALUE_IS_INT;

            case Flags:
                return VALUE_IS_INT;

            case Fo:
            	return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case Font:
                return VALUE_IS_DICTIONARY;

            case FontBBox:
                return VALUE_IS_FLOAT_ARRAY;

            case FontDescriptor:
                return VALUE_IS_DICTIONARY;

            case FontFile:
                return VALUE_IS_DICTIONARY;

            case FontFile2:
                return VALUE_IS_DICTIONARY;

            case FontFile3:
                return VALUE_IS_DICTIONARY;

            case FontMatrix:
                return VALUE_IS_DOUBLE_ARRAY;

            case FontName:
                return VALUE_IS_NAME;

            case FontStretch:
            	return VALUE_IS_NAME;

            case FormType:
                return VALUE_IS_INT;

            case FS:
            	return VALUE_IS_DICTIONARY;

            case FT:
            	return VALUE_IS_NAME;

            case Function:
                return VALUE_IS_DICTIONARY;

            case Functions:
                return VALUE_IS_KEY_ARRAY;

            case FunctionType:
                return VALUE_IS_INT;

            case Gamma:
                return VALUE_IS_FLOAT_ARRAY;

            case Group:
                return VALUE_IS_UNREAD_DICTIONARY;

            case H:
            	if(type==Form)
                	return VALUE_IS_VARIOUS;
            	else if(type==Outlines)
            		return VALUE_IS_BOOLEAN;

            case Height:
                return VALUE_IS_INT;

            case I:

            	if(type==PdfDictionary.Form)
            		return VALUE_IS_INT_ARRAY;
            	else if(type==PdfDictionary.MK)
            		return VALUE_IS_UNREAD_DICTIONARY;
            	else if(type==PdfDictionary.Page)
            		return VALUE_IS_BOOLEAN;
            	else if(1==2) //need to find type
            		return VALUE_IS_DICTIONARY;
            	else
            		return VALUE_IS_BOOLEAN;

            case ID:
                if(type==PdfDictionary.MCID) 
                 return VALUE_IS_TEXTSTREAM;
               else if(type==PdfDictionary.CompressedObject)
                 return VALUE_IS_STRING_ARRAY;

            case IF:
            	return VALUE_IS_UNREAD_DICTIONARY;

			case IDTree:
            	return VALUE_IS_NAMETREE;

            case Index:
            	return VALUE_IS_INT_ARRAY;

            case Info:
                //if(type==Encrypt)
                return VALUE_IS_UNREAD_DICTIONARY;

            case ImageMask:
            	return VALUE_IS_BOOLEAN;

            case Intent:
            	return VALUE_IS_NAME;

            case IT:
            	return VALUE_IS_NAME;

            case JavaScript:
                	return VALUE_IS_DICTIONARY;

            case JS:
                return VALUE_IS_VARIOUS;

            case K:
            	if(type==XObject)
            		return VALUE_IS_VARIOUS;
            	else if(type==Form)
            		return VALUE_IS_DICTIONARY;//KEY_ARRAY;
            	else if(type==MCID)
            		return VALUE_IS_VARIOUS;//KEY_ARRAY;
                else if(type==OCProperties)
            		return VALUE_IS_VARIOUS;//KEY_ARRAY;
            	else
                return VALUE_IS_INT;

            case Keywords:
                return VALUE_IS_TEXTSTREAM;

            case Kids:
                return VALUE_IS_KEY_ARRAY;

            case JBIG2Globals:
                return VALUE_IS_DICTIONARY;

            case Lang:
                if(type==PdfDictionary.MCID || type==PdfDictionary.Page)
            	return VALUE_IS_TEXTSTREAM;
            	
            case Last:
                return VALUE_IS_UNREAD_DICTIONARY;

            case LastChar:
                return VALUE_IS_INT;

            case LastModified:
                return VALUE_IS_TEXTSTREAM;
            
            case Layer:
                return VALUE_IS_DICTIONARY;

            case Length:
                return VALUE_IS_INT;

            case Length1:
                return VALUE_IS_INT;

            case Length2:
                return VALUE_IS_INT;

            case Length3:
                return VALUE_IS_INT;

            case Location:
                return VALUE_IS_TEXTSTREAM;

            case Lock:
            	return VALUE_IS_UNREAD_DICTIONARY;
            	
            case Locked:
            	return VALUE_IS_KEY_ARRAY;

            case LW:
                return VALUE_IS_FLOAT;

            case M:
            	if(type==Form)
            		return VALUE_IS_VARIOUS;
            	else if(type==Sig)
            		return VALUE_IS_TEXTSTREAM;
            	else
            	    return VALUE_IS_TEXTSTREAM;
            	
            case MarkInfo:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Mask:
                return VALUE_IS_DICTIONARY;

            case Matrix:
                return VALUE_IS_FLOAT_ARRAY;

            case max:
                return VALUE_IS_FLOAT;
           
            case MaxLen:
            	return VALUE_IS_INT;

			case MCID:
            	return VALUE_IS_INT;

            case MediaBox:
            	return VALUE_IS_FLOAT_ARRAY;

            case Metadata:
                return VALUE_IS_UNREAD_DICTIONARY;

            case MissingWidth:
                return VALUE_IS_INT;
                
            case MK:
                return VALUE_IS_DICTIONARY;

            case ModDate:
                return VALUE_IS_TEXTSTREAM;

            case N:
            	if(type==PdfDictionary.CompressedObject)
            		return VALUE_IS_INT;
            	else if(type==PdfDictionary.Form || type==PdfDictionary.MK)
            		return VALUE_IS_VARIOUS;
            	else
            		return VALUE_IS_NAME;
                
            case Name:
                return VALUE_IS_NAME;

            case Names:
                if(type==PdfDictionary.Names)
                    return VALUE_IS_MIXED_ARRAY;
                else
                    return VALUE_IS_UNREAD_DICTIONARY;
                
            case NeedAppearances:
            	return VALUE_IS_BOOLEAN;

            case Next:
                if(type==PdfDictionary.Form)
                    return VALUE_IS_DICTIONARY;
                else
                    return VALUE_IS_UNREAD_DICTIONARY;
                
            case NM:
            	return VALUE_IS_TEXTSTREAM;

            case Nums:
                return VALUE_IS_KEY_ARRAY;

            case min:
                return VALUE_IS_FLOAT;

            case O:
            	if(type==Form)
            		return VALUE_IS_DICTIONARY;
            	else
            		return VALUE_IS_TEXTSTREAM;

            case OC:
            	if(type==Form)
            		return VALUE_IS_DICTIONARY;
            	else
            		return VALUE_IS_NAME;

            case OCGs:
                return VALUE_IS_KEY_ARRAY;

            case OCProperties:
                return VALUE_IS_UNREAD_DICTIONARY;

            case OFF:
            	return VALUE_IS_KEY_ARRAY;

            case Off:
            	return VALUE_IS_UNREAD_DICTIONARY;
            	
            case ON:
            	return VALUE_IS_KEY_ARRAY;
            	
            case On:
            	return VALUE_IS_UNREAD_DICTIONARY;	

            case OP:
                if(type==Form)
            		return VALUE_IS_VARIOUS;
            	else
            	    return VALUE_IS_BOOLEAN;
            	
            case op:
            	return VALUE_IS_BOOLEAN;
            	
            case Open:
            	return VALUE_IS_BOOLEAN;	
            	
            case OpenAction:
                return VALUE_IS_VARIOUS;
            	
            case OPI:
            	return VALUE_IS_DICTIONARY;
            	
            case OPM:
            	return VALUE_IS_FLOAT;

            //breaks /PDFdata/baseline_screens/shading/Lggningsanvisningar01.pdf
            //case Order:
            //	return VALUE_IS_KEY_ARRAY;
            	
            case Opt:
            	return VALUE_IS_OBJECT_ARRAY;

            case Ordering:
                return VALUE_IS_TEXTSTREAM;

            case Outlines:
                return VALUE_IS_UNREAD_DICTIONARY;
             
            case P:
                if(type==Form)
                	return VALUE_IS_UNREAD_DICTIONARY;
                else if(type==PdfDictionary.Metadata)
                    return VALUE_IS_DICTIONARY;
                else	
                	return VALUE_IS_INT;

            case PageLabels:
                return VALUE_IS_UNREAD_DICTIONARY;
            
            case PageMode:
	        	return VALUE_IS_STRING_CONSTANT;

            case Pages:
                return VALUE_IS_DICTIONARY;

            case PaintType:
                return VALUE_IS_INT;
                
            case ParentTree:
            	return VALUE_IS_DICTIONARY;

            case Pattern:
                return VALUE_IS_DICTIONARY;

            case PatternType:
                return VALUE_IS_INT;

            case Parent:
                return VALUE_IS_STRING_KEY;
              
            case PC:
            	return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case Perms:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Prev:
                return VALUE_IS_INT;
                
            case Pg:
                return VALUE_IS_UNREAD_DICTIONARY;

            case PI:
            	return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case PO:
            	return PdfDictionary.VALUE_IS_DICTIONARY;  

            case Process:
                return VALUE_IS_DICTIONARY;

            case Popup:
            	return VALUE_IS_UNREAD_DICTIONARY;
            	
            case Predictor:
                return VALUE_IS_INT;

            case Print:
                return VALUE_IS_DICTIONARY;

            case PrintState:
                return VALUE_IS_STRING_CONSTANT;

            case ProcSet:
                return VALUE_IS_MIXED_ARRAY;

            case Producer:
                return VALUE_IS_TEXTSTREAM;

            case Properties:
                return VALUE_IS_DICTIONARY_PAIRS;
            
            case PV:
            	return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case Q:
            	return VALUE_IS_INT;
            	
            case QuadPoints:
            	return VALUE_IS_FLOAT_ARRAY;
            	
            case R:
            	if(type==Form || type==PdfDictionary.MK)
            		return VALUE_IS_VARIOUS;
            	else
            		return VALUE_IS_INT;

            case Range:
                return VALUE_IS_FLOAT_ARRAY;

            case RBGroups:
            	return VALUE_IS_KEY_ARRAY;
            	
            case RC:
            	return VALUE_IS_TEXTSTREAM;	
            
            case RD:
            	return VALUE_IS_FLOAT_ARRAY;

            case Reason:
                return VALUE_IS_TEXTSTREAM;

            case Reference:
                return VALUE_IS_OBJECT_ARRAY;

            case Registry:
                return VALUE_IS_TEXTSTREAM;

            case Resources:
                return VALUE_IS_UNREAD_DICTIONARY;

            case RoleMap:
                return VALUE_IS_DICTIONARY;
            
            case Rotate:
                return VALUE_IS_INT;

            case Rect:
            	return  VALUE_IS_FLOAT_ARRAY;

            case Root:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Rows:
                return VALUE_IS_INT;
            
            case RV:
                return VALUE_IS_TEXTSTREAM;	
            	
            case Shading:
                return VALUE_IS_DICTIONARY;
                
            case S:
                return VALUE_IS_NAME;

            case SA:
                return VALUE_IS_BOOLEAN;

            case ShadingType:
                return VALUE_IS_INT;

            case SigFlags:
                return VALUE_IS_INT;
            
            case Size:
            	if(type==PdfDictionary.CompressedObject)
            		return PdfDictionary.VALUE_IS_INT;
            	else
            		return VALUE_IS_INT_ARRAY;

            case SMask:
                return VALUE_IS_DICTIONARY;

            case Sig:
                return VALUE_IS_UNREAD_DICTIONARY;

            case Sound:
                return VALUE_IS_UNREAD_DICTIONARY;
            
            case State:
                return VALUE_IS_MIXED_ARRAY;

            case StemV:
                return VALUE_IS_INT;

            case PdfDictionary.StmF:
                return VALUE_IS_NAME;

            case PdfDictionary.StrF:
                return VALUE_IS_NAME;
            
            case StructParent:
            	return VALUE_IS_INT;
            	
            case StructParents:
            	return VALUE_IS_INT;

            case StructTreeRoot:
            	return VALUE_IS_UNREAD_DICTIONARY;
                
            case Style:
            	if(type==Font)
            		return VALUE_IS_DICTIONARY;
            	else
            		return VALUE_IS_TEXTSTREAM;

            case SubFilter:
                return VALUE_IS_NAME;

            case Subj:
            	return VALUE_IS_TEXTSTREAM;

            case Subject:
                return VALUE_IS_TEXTSTREAM;

            case Subtype:
                return VALUE_IS_STRING_CONSTANT;

            case Supplement:
                return VALUE_IS_INT;

            case T:
            	if(type==Form || type==MCID)
            		return VALUE_IS_TEXTSTREAM;
            	else
            		return VALUE_IS_INT;

            case Tabs:
                return VALUE_IS_NAME;

            case TI:
        		return VALUE_IS_INT; 	
        		
            case TP:
        		return VALUE_IS_INT; 	
                
            case TilingType:
            	return VALUE_IS_INT;

            case Title:
                return VALUE_IS_TEXTSTREAM;
                  
            case TM:
                return VALUE_IS_TEXTSTREAM;
                
            case ToUnicode:
                return VALUE_IS_DICTIONARY;

            case TR:
                return VALUE_IS_DICTIONARY;

            case Trapped:
                return VALUE_IS_NAME;

            case TrimBox:
            	return VALUE_IS_FLOAT_ARRAY;

            case TU:
                return VALUE_IS_TEXTSTREAM;

            case TxOutline:
                return VALUE_IS_BOOLEAN;

            case TxFontSize:
                return VALUE_IS_FLOAT;
                
            case Type:
                return VALUE_IS_STRING_CONSTANT;
                
            case U:
            	if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;   
            	else
            		return VALUE_IS_TEXTSTREAM;

            case UF:
            	return VALUE_IS_VARIOUS;

            case Uncompressed:
                return VALUE_IS_BOOLEAN;
                 
            case URI:
                return VALUE_IS_TEXTSTREAM;
   
            case Usage:
                return VALUE_IS_DICTIONARY;

            case V:
            	if(type==PdfDictionary.Form)
            		return VALUE_IS_VARIOUS;
            	else
            		return VALUE_IS_INT;

                //hack as odd structure
            case W:
            	if(type==PdfDictionary.CompressedObject) // int not int array
            		return PdfDictionary.VALUE_IS_INT_ARRAY;
            	else if(type==PdfDictionary.Form)
            		return VALUE_IS_VARIOUS;
            	else
            		return VALUE_IS_TEXTSTREAM;

            case Win:
        		return VALUE_IS_DICTIONARY;

            case WhitePoint:
                return VALUE_IS_FLOAT_ARRAY;

            case Width:
                return VALUE_IS_INT;

            case Widths:
                return VALUE_IS_FLOAT_ARRAY;

            case WP:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;  
            
            case WS:
            	//if(type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;  
            	
            case X:
            	//if(type==PdfDictionary.OCProperties || type==PdfDictionary.Form)
            		return PdfDictionary.VALUE_IS_DICTIONARY;    
    
            case XFA:
                return VALUE_IS_VARIOUS;

            case XObject:
                return VALUE_IS_UNREAD_DICTIONARY;

            case XRefStm:
                return VALUE_IS_INT;
            
            case XStep:
            	return VALUE_IS_FLOAT;
            	
            case YStep:
            	return VALUE_IS_FLOAT;

            case Zoom:
            	return VALUE_IS_DICTIONARY;
            	
            default:

                if(PdfObject.debug){
                    System.out.println("No type value set for "+id+" getKeyType(int id) in PdfDictionay");
                  
                }

                break;

        }


        return PDFkey;
    }


    /**
     * use reflection to show actual Constant for Key or return null if no value
     * @param parameterConstant
     * @return String or null
     */
    public static String showAsConstant(int parameterConstant) {

    	Field[] ts = PdfDictionary.class.getFields();
    	int count=ts.length;
    	String type=null;

    	for(int ii=0;ii<count;ii++){
    		try{
    			//if(ts[ii] instanceof Integer){
    				int t=ts[ii].getInt(new PdfDictionary());

    				if(t==parameterConstant){
    					type="PdfDictionary."+ts[ii].getName();
    					count=ii;
    				}
    			//}
    		}catch(Exception ee){
    			ee.printStackTrace();
    		}
    	}

    	return type; 
    }

    /**
     * used in debugging
     * @param type
     * @return String representation of type
     */
    public static String showArrayType(int type) {
        
        switch(type){
            case VALUE_IS_INT_ARRAY:
                return "VALUE_IS_INT_ARRAY";

            case VALUE_IS_BOOLEAN_ARRAY:
                return "VALUE_IS_BOOLEAN_ARRAY";

            case VALUE_IS_KEY_ARRAY:
                return "VALUE_IS_KEY_ARRAY";

            case VALUE_IS_DOUBLE_ARRAY:
                return "VALUE_IS_DOUBLE_ARRAY";

            case VALUE_IS_MIXED_ARRAY:
                return "VALUE_IS_MIXED_ARRAY";

            case VALUE_IS_STRING_ARRAY:
                return "VALUE_IS_STRING_ARRAY";

            case VALUE_IS_OBJECT_ARRAY:
                return "VALUE_IS_OBJECT_ARRAY";

            default:
                return "not set";
        }
    }
}