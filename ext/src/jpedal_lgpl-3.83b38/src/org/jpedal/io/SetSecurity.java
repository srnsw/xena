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
* SetSecurity.java
* ---------------
*/
package org.jpedal.io;

import org.jpedal.utils.LogWriter;

import java.security.Security;

public class SetSecurity {

	
	public static void init(){

        //allow user to over-ride
        String altSP=System.getProperty("org.jpedal.securityprovider");

        if(altSP==null)
            altSP="org.bouncycastle.jce.provider.BouncyCastleProvider";

        try {

            Class c = Class.forName(altSP);
            java.security.Provider provider = (java.security.Provider) c.newInstance();

            Security.addProvider(provider);
        } catch (Exception e) {

            LogWriter.writeLog("Unable to run custom security provider " + altSP);
            LogWriter.writeLog("Exception " + e);

            throw new RuntimeException("This PDF file is encrypted and JPedal needs an additional library to decode on the classpath (we recommend bouncycastle library)." +
                    "There is additional explanation at http://www.jpedal.org/support_AddJars.php"+"\n"+e);


        }
    }
}
