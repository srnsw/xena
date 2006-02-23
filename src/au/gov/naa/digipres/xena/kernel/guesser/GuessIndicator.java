/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public final class GuessIndicator {

    public static GuessIndicator UNKNOWN    = new GuessIndicator("Unknown", -1);
    public static GuessIndicator FALSE      = new GuessIndicator("False", 0);
    public static GuessIndicator TRUE       = new GuessIndicator("True", 1);
    
    private int value;
    private String name;
    
    private GuessIndicator(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return name + " (id: " + value + ")";
    }

    public String getName() {
        return name;
    }
    
    public int getValue() {
        return value;
    }
    
    
}
