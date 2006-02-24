package au.gov.naa.digipres.xena.plugin.office;
import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserUtils;

/**
 * Guesser for office file types.
 *
 * @author Chris Bitmead
 */
public class OfficeMagicNumber {

    protected static byte[] officemagic = { 
        (byte) 0xd0,
        (byte) 0xcf,
        (byte) 0x11,
        (byte) 0xe0,
        (byte) 0xa1,
        (byte) 0xb1,
        (byte) 0x1a,
        (byte) 0xe1 
        };
    
    public static boolean checkForOfficeMagicNumber(XenaInputSource xis) throws IOException {
        byte[] first = new byte[officemagic.length];
        xis.getByteStream().read(first);
        if (GuesserUtils.compareByteArrays(first, officemagic)) {
            return true;
        }
        return false;
    }
    
}
