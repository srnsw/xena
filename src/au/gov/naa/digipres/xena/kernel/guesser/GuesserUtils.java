/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public class GuesserUtils {

    public static boolean compareByteArrays(byte[] b1, byte[] b2) {
        for (int i = 0; i < b2.length && i < b1.length; i++) {
            if (b2[i] != b1[i]) {
                return false;
            }
        }
        return true;
    }
    
}
