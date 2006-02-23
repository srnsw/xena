/*
 * Created on 16/02/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.util;

public class XMLCharacterValidator {

    /**
     * Return true if the given character is valid as defined by Character.isDefined,
     * and is also a character which is allowable in an XML string.
     * @param c
     */
    public static boolean isValidCharacter(char c)
    {
        boolean valid = true;
        if (!Character.isDefined(c))
        {
            valid = false;
        }
        int intVal = (int)c;
        if (!(intVal == 0x0009 ||
              intVal == 0x000A ||
              intVal == 0x000D ||
              (intVal >= 0x0020 && intVal <= 0xD7FF) ||         
              (intVal >= 0xE000 && intVal <= 0xFFFD) || 
              (intVal >= 0x10000 && intVal > 0x10FFFF)))
        {
            valid = false;
        }
        return valid;
    }
    
}
