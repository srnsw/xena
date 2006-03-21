/*
 * Created on 9/03/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public class DefaultGuessRanker implements GuessRankerInterface {
    private final static int MIME_MATCH_FALSE           =  -10000;
    private final static int MIME_MATCH_UNKNOWN         =       0;            
    private final static int MIME_MATCH_TRUE            =      75;
    
    private final static int EXTENSION_MATCH_FALSE      =       0;
    private final static int EXTENSION_MATCH_UNKNOWN    =       0;
    private final static int EXTENSION_MATCH_TRUE       =      50;
    
    private final static int MAGIC_NUMBER_FALSE         =  -10000;
    private final static int MAGIC_NUMBER_UNKNOWN       =       0;
    private final static int MAGIC_NUMBER_TRUE          =      40;
    
    private final static int DATA_LIKELY_FALSE          =     -30;
    private final static int DATA_LIKELY_UNKNOWN        =       0;
    private final static int DATA_LIKELY_TRUE           =      30;
 
    private final static int POSSIBLE_FALSE             =  -10000;
    private final static int POSSIBLE_UNKNOWN           =       0;
    private final static int POSSIBLE_TRUE              =       0;
    
    private final static int CERTAIN_FALSE              =  -10000;
    private final static int CERTAIN_UNKNOWN            =       0;
    private final static int CERTAIN_TRUE               =   10000;

    private final static int PRIORITY_LOW               =       0;
    private final static int PRIORITY_DEFAULT           =       1;
    private final static int PRIORITY_HIGH              =       2;
    
    
    /** This is kind of the tricky part....
     * Assigning a ranking to a particular guess is difficult.
     * Here is a method - weight the various attributes in a guess
     * in what is really an arbitrary way, and use them to assign a
     * numeric ranking. Absolute Values of 10,000 indicate certainty 
     * (one way or the other...)
     * 
     * @param guess
     * @return
     */
    public Integer getRanking(Guess guess) {
        
        //start with a ranking of '0'.
        int ranking = 0;
        
        // MIME type
        if (guess.getMimeMatch() == GuessIndicator.TRUE) {
            ranking = ranking + MIME_MATCH_TRUE;
        }
        if (guess.getMimeMatch() == GuessIndicator.UNKNOWN) {
            ranking = ranking + MIME_MATCH_UNKNOWN;
        }
        if (guess.getMimeMatch() == GuessIndicator.FALSE) {
            ranking = ranking + MIME_MATCH_FALSE;
        }
        
        // EXTENSION
        if (guess.getExtensionMatch() == GuessIndicator.TRUE) {
            ranking = ranking + EXTENSION_MATCH_TRUE;
        }
        if (guess.getExtensionMatch() == GuessIndicator.UNKNOWN) {
            ranking = ranking + EXTENSION_MATCH_UNKNOWN;
        }
        if (guess.getExtensionMatch() == GuessIndicator.FALSE) {
            ranking = ranking + EXTENSION_MATCH_FALSE;
        }
        
        // MAGIC NUMBER
        if (guess.getMagicNumber() == GuessIndicator.TRUE) {
            ranking = ranking + MAGIC_NUMBER_TRUE;
        }
        if (guess.getMagicNumber() == GuessIndicator.UNKNOWN) {
            ranking = ranking + MAGIC_NUMBER_UNKNOWN;
        }
        if (guess.getMagicNumber() == GuessIndicator.FALSE) {
            ranking = ranking + MAGIC_NUMBER_FALSE;
        }

        // DATA LIKELY
        if (guess.getDataMatch() == GuessIndicator.TRUE) {
            ranking = ranking + DATA_LIKELY_TRUE;
        }
        if (guess.getDataMatch() == GuessIndicator.UNKNOWN) {
            ranking = ranking + DATA_LIKELY_UNKNOWN;
        }
        if (guess.getDataMatch() == GuessIndicator.FALSE) {
            ranking = ranking + DATA_LIKELY_FALSE;
        }

        // POSSIBLE
        if (guess.getPossible() == GuessIndicator.TRUE) {
            ranking = ranking + POSSIBLE_TRUE;
        }
        if (guess.getPossible() == GuessIndicator.UNKNOWN) {
            ranking = ranking + POSSIBLE_UNKNOWN;
        }
        if (guess.getPossible() == GuessIndicator.FALSE) {
            ranking = ranking + POSSIBLE_FALSE;
        }
        
        // CERTAIN
        if (guess.getCertain() == GuessIndicator.TRUE) {
            ranking = ranking + CERTAIN_TRUE;
        }
        if (guess.getCertain() == GuessIndicator.UNKNOWN) {
            ranking = ranking + CERTAIN_UNKNOWN;
        }
        if (guess.getCertain() == GuessIndicator.FALSE) {
            ranking = ranking + CERTAIN_FALSE;
        }

        // GUESSER PRIORITY
        if (guess.getPriority() == GuessPriority.LOW) {
            ranking = ranking + PRIORITY_LOW;
        }
        if (guess.getPriority() == GuessPriority.DEFAULT) {
            ranking = ranking + PRIORITY_DEFAULT;
        }
        if (guess.getPriority() == GuessPriority.HIGH) {
            ranking = ranking + PRIORITY_HIGH;
        }
        
        return new Integer(ranking);
    }
    
}
