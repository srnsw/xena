/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.image.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.plugin.image.JpegGuesser;

public class JpegGuesserTester {

    
    
    
    public static void main(String[] argv){
        

        JpegGuesser guesser = new JpegGuesser();
        XenaInputSource xis = null;
        
        List<String> fileNameList = new ArrayList<String>();
        
        fileNameList.add("d:\\test_data\\dino.jpg");
        fileNameList.add("d:\\test_data\\dino.jpg");
        fileNameList.add("d:\\test_data\\dino.jpg");
        fileNameList.add("d:\\test_data\\dino.jpg");
        
        
        for (Iterator iter = fileNameList.iterator(); iter.hasNext();) {
            String fileName = (String) iter.next();
            
            try {
                xis = new XenaInputSource(new File(fileName));
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(1);
            }
            
            try {
                Guess guess = guesser.guess(xis);
                
                System.out.println(guess.toString());
                
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (XenaException xe) {
                xe.printStackTrace();
            }
        }
        
    }
    
    
}
