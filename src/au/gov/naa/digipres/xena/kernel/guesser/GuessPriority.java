/*
 * Created on 11/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public class GuessPriority implements Comparable {

    public static GuessPriority LOW     = new GuessPriority("LOW", 0);
    public static GuessPriority DEFAULT = new GuessPriority("Default", 1);
    public static GuessPriority HIGH    = new GuessPriority("High", 2);
    
    private int value;
    private String name;
    
    private GuessPriority(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return name + " (id: " + value + ")";
    }

    public int getValue() {
        return value;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GuessPriority)) {
            return false;
        }
        GuessPriority  otherOne = (GuessPriority)o;
        if (this.value == otherOne.getValue() ) {
            return true;
        }
        return false;
    }
    
    public int compareTo(Object o) {
        if (o instanceof GuessPriority) {
            GuessPriority otherPriority = (GuessPriority)o;
            if (this.value < otherPriority.getValue() ) {
                return -1;
            }
            if (this.value > otherPriority.getValue() ) {
                return 1;
            }
            return 0;
        }
        throw new IllegalArgumentException();
    }

    
}
