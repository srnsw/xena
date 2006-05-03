/*
 * Created on 10/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

import java.util.HashMap;
import java.util.Map;

import au.gov.naa.digipres.xena.kernel.type.DefaultFileType;
import au.gov.naa.digipres.xena.kernel.type.Type;

public final class Guess {
    
    public static final int UNKNOWN = 0;
    public static final int FALSE   = 1;
    public static final int TRUE    = 2;
    
    public static final int LOW     = 0;
    public static final int DEFAULT = 1;
    public static final int HIGH    = 2;
    
    
    //type for this guess
    private Type type;
    
    // THE GUESS INDICATORS
    private GuessIndicator possible        = GuessIndicator.UNKNOWN;
    private GuessIndicator dataMatch       = GuessIndicator.UNKNOWN;
    private GuessIndicator magicNumber     = GuessIndicator.UNKNOWN;
    private GuessIndicator extensionMatch  = GuessIndicator.UNKNOWN;
    private GuessIndicator mimeMatch       = GuessIndicator.UNKNOWN;
    private GuessIndicator certain         = GuessIndicator.UNKNOWN;
    private GuessPriority  priority        = GuessPriority.DEFAULT;
    //future guess indicators to be added in time....
    
    private Map guessProperties = new HashMap();
    
    
    public Guess()
    {
        //empty constructor for the moment...
        type = new DefaultFileType();
    }
    
    public Guess(Type guessedType) {
        this.type = guessedType;
    }


    public String toString() {
        return "Guess... type: " + type.getName() + "\n" +
                " possible: " +  possible.getName() +  "\n" +
                " dataMatch:" + dataMatch.getName() +  "\n" +
                " magicNumber: " + magicNumber.getName() +  "\n" +
                " extensionMatch: " + extensionMatch.getName() +  "\n" +
                " mimeMatch: " + mimeMatch.getName() +  "\n" +
                " certain: " + certain.getName() +  "\n" +
                " priority: " + priority.getName();
       
    }
    
    /**
     * @return Returns the guessedType.
     */
    public Type getType() {
        return type;
    }

    /**
     * @param guessedType The guessedType to set.
     */
    public void setType(Type guessedType) {
        this.type = guessedType;
    }

    
    /**
     * @return Returns the certain indicator.
     */
    public GuessIndicator getCertain() {
        return certain;
    }

    
    /**
     * @param certain The certain indicator to set.
     */
    public void setCertain(GuessIndicator certain) {
        this.certain = certain;
    }
    public void setCertain(boolean certain) {
        if (certain == true) {
            this.certain = GuessIndicator.TRUE;
        } else {
            this.certain = GuessIndicator.FALSE;
        }
    }
    
    
    /**
     * @return Returns the dataMatch indicator.
     */
    public GuessIndicator getDataMatch() {
        return dataMatch;
    }

    /**
     * @param dataMatch The dataMatch to set.
     */
    public void setDataMatch(GuessIndicator dataMatch) {
        this.dataMatch = dataMatch;
    }
    public void setDataMatch(boolean dataMatch) {
        if (dataMatch == true) {
            this.dataMatch = GuessIndicator.TRUE;
        } else {
            this.dataMatch = GuessIndicator.FALSE;
        }
    }

    /**
     * @return Returns the extensionMatch indicator.
     */
    public GuessIndicator getExtensionMatch() {
        return extensionMatch;
    }

    
    /**
     * @param extensionMatch The extensionMatch to set.
     */
    public void setExtensionMatch(GuessIndicator extensionMatch) {
        this.extensionMatch = extensionMatch;
    }
    public void setExtensionMatch(boolean extensionMatch) {
        if (extensionMatch == true) {
            this.extensionMatch = GuessIndicator.TRUE;
        } else {
            this.extensionMatch = GuessIndicator.FALSE;
        }
    }
    
    /**
     * @return Returns the magicNumber.
     */
    public GuessIndicator getMagicNumber() {
        return magicNumber;
    }

    /**
     * @param magicNumber The magicNumber to set.
     */
    public void setMagicNumber(GuessIndicator magicNumber) {
        this.magicNumber = magicNumber;
    }
    
    public void setMagicNumber(boolean magicNumber){
        if (magicNumber == true) {
            this.magicNumber = GuessIndicator.TRUE;
        } else {
            this.magicNumber = GuessIndicator.FALSE;
        }
    }

    
    /**
     * @return Returns the mimeMatch indicator
     */
    public GuessIndicator getMimeMatch() {
        return mimeMatch;
    }

    /**
     * @param mimeMatch The mimeMatch indicator to set.
     */
    public void setMimeMatch(GuessIndicator mimeMatch) {
        this.mimeMatch = mimeMatch;
    }
    public void setMimeMatch(boolean mimeMatch) {
        if (mimeMatch == true) {
            this.mimeMatch = GuessIndicator.TRUE;
        } else {
            this.mimeMatch = GuessIndicator.FALSE;
        }
    }

    /**
     * @return Returns the possible.
     */
    public GuessIndicator getPossible() {
        return possible;
    }

    /**
     * @param possible The possible to set.
     */
    public void setPossible(GuessIndicator possible) {
        this.possible = possible;
    }
    public void setPossible(boolean possible) {
        if (possible == true) {
            this.possible = GuessIndicator.TRUE;
        } else {
            this.possible = GuessIndicator.FALSE;
        }
    }

    /**
     * @return Returns the priority.
     */
    public GuessPriority getPriority() {
        return priority;
    }

    /**
     * @param priority The priority to set.
     */
    public void setPriority(GuessPriority priority) {
        this.priority = priority;
    }

    
    /**
     * @return Returns the guessProperties.
     */
    public Map getGuessProperties() {
        return guessProperties;
    }

    /**
     * @param guessProperties The new value to set guessProperties to.
     */
    public void setGuessProperties(Map guessProperties) {
        this.guessProperties = guessProperties;
    }
}
