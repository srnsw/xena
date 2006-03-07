package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Manages instances of Guesser objects.
 * 
 * @see Guesser
 * @author Chris
 * @created March 22, 2002
 */
public class GuesserManager implements LoadManager {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
    static GuesserManager theSingleton = new GuesserManager();

    protected List<Guesser> guessers = new ArrayList<Guesser>();

    private GuesserManager() {
    }

    public static GuesserManager singleton() {
        return theSingleton;
    }

    public String toString() {
        StringBuilder retStringBuilder = new StringBuilder("Guesser Manager. The following guesser have been loaded:" + System.getProperty("line.separator"));

        for (Guesser guesser : guessers) {
            retStringBuilder.append(guesser.toString() + System.getProperty("line.separator"));
        }
        return new String(retStringBuilder);

    }

    public boolean load(JarPreferences props) throws XenaException {
        try {
            PluginLoader loader = new PluginLoader(props);
            List<Guesser> instances = loader.loadInstances("guessers");
            guessers.addAll(instances);
            return !instances.isEmpty();
        } catch (ClassNotFoundException e) {
            throw new XenaException(e);
        } catch (IllegalAccessException e) {
            throw new XenaException(e);
        } catch (InstantiationException e) {
            throw new XenaException(e);
        }
    }

    
    
    /** 
     * Get a list of guess objects.
     * Sort them so that the zeroth element is the most likely guess, and the last element
     * in the list is the least likely.
     * 
     * The ranking of the guesses occurs in the getRanking method.
     * 
     */
    public List<Guess> getGuesses(XenaInputSource xenaInputSource) throws IOException, XenaException {
        /* set up a sorted map, which contains all the guesses that are created for the input source.
         * Store the guesses in the map according to their ranking, thus:
         * R1 - [G0, G1, G2]
         * R2 - [G4]
         * R3 - [G3, G5]
         *
         * Where R1, R2, R3 are rankings (in this case Integer objects)
         * and G0, G1, ... G5 are the guesses that have been returned.
         *
         */
        Map<Integer,List<Guess>> guessMap = new TreeMap<Integer,List<Guess>>();

        
        //sysout
        System.out.println("Guessing for xis: " + xenaInputSource.toString());
        //cycle through our guessers and get all of the guesses for this particular type...
        try {
            for (Iterator guesserIterator = guessers.iterator(); guesserIterator.hasNext();) {

            	Guesser guesser = (Guesser) guesserIterator.next();
            	try
            	{
	                Guess newGuess = guesser.guess(xenaInputSource);
	                
	                
	                // If we are not possible skip to the next guess!
	                if (newGuess.getPossible() != GuessIndicator.FALSE) {
	                
	                    
	                    System.out.println(newGuess.getType().getName() + "   " + guesser.getName());
	                    
	                    
	                    // now we have our guess, and it's a possible goer, lets get a ranking for it.
	                    Integer ranking = getRanking(newGuess);
	                    
	                    // if it is less than 0, forgedaboudit.
	                    if (ranking >= 0 ) {
	                    
	                        // now we have a ranking, stick it into the appropriate list in the map.
	                        // if the list doesnt exist, create it for that ranking.
	                        List<Guess> guessesWithThisRank = guessMap.get(ranking);
	                        if (guessesWithThisRank == null) {
	                            guessesWithThisRank = new ArrayList<Guess>();
	                            
	                        }
	                        // add our latest guess to the list...
	                        guessesWithThisRank.add(newGuess);
	                        // and then finally put our list back in the map.
	                        guessMap.put(ranking, guessesWithThisRank);
	                    }
	                }
            	}
            	// Just log exceptions as we want the other guessers to have a chance
            	catch (IOException iex)
            	{
            		logger.log(Level.FINER,
            		           "Exception thrown in guesser " + guesser.getName(),
            		           iex);
            	}
            	catch (XenaException xex)
            	{
            		logger.log(Level.FINER,
            		           "Exception thrown in guesser " + guesser.getName(),
            		           xex);
            	}
            }

            //sysout
            System.out.println("Guessed");
            System.out.println("------------------------------------------");
            
            
            
        } finally {
            xenaInputSource.close();
        }
        
        
        /*
         * our sorted map will return a list of list<guess> when we get it's
         * value (resultsMap.values() ) unfortunately - the list will be ordered
         * the wrong way (0 is least likely, 999 is most likely), so we have to
         * reverse the list.
         */
        
        // create our final guess list...
        List<Guess> sortedGuessList = new ArrayList<Guess>();
        
        // get our list of guesses at each level - a list of lists!
        List<List<Guess>> listOfGuessesAtEachLevel = new ArrayList<List<Guess>>(guessMap.values());
        // reverse our list of lists of that the list with the highest ranking is the first list, least likely is our last list.
        Collections.reverse(listOfGuessesAtEachLevel);
        
        // now, starting at the list of guesses that have the highest ranking, insert all of our guesses into our
        // final sorted guess list. Hooray!
        for (List<Guess> guessesAtCurrentLevel : listOfGuessesAtEachLevel) {
            for (Guess currentGuess : guessesAtCurrentLevel) {
            	sortedGuessList.add(currentGuess);
            }
        }
        return sortedGuessList;
    }
 


    
    public void complete() {
    }

    /**
     * @return Returns the guessers.
     */
    public List getGuessers() {
        return guessers;
    }

    /**
     * Return our best guess as to the type of a file.
     * 
     * @param source
     *            source of the data
     * @return Best guess of the type of input.
     * @throws IOException
     */
    public FileType mostLikelyType(XenaInputSource source) throws IOException, XenaException {
        List guesses = getGuesses(source);
        if (0 < guesses.size()) {
            Guess guess = (Guess) guesses.get(0);
            return (FileType) guess.getType();
        } else {
            return null;
        }
    }
    
    /**
     * Get the best guess for the given XIS, while making as few guesses as possible.
     * The guessers are first sorted in order of the maximum ranking that each can
     * produce. The guessers then guess the type in turn, with guessers with the higher
     * maximum possible ranking guessing first. The current leading guess ranking is
     * tracked, and if this leading ranking becomes higher than the maximum possible
     * ranking of the current guesser, then there is no point going any further as it
     * is impossible for the current leading ranking to be beaten by any of the remaining
     * guessers.
     * 
     * @param source
     * @return best guess, or null if no guessers available
     * @throws IOException
     */
    public Guess getBestGuess(XenaInputSource source) throws IOException
    {
    	return getBestGuess(source, new ArrayList<String>());
    }
   
    
    /**
     * Get the best guess for the given XIS, while making as few guesses as possible.
     * The guessers are first sorted in order of the maximum ranking that each can
     * produce. The guessers then guess the type in turn, with guessers with the higher
     * maximum possible ranking guessing first. The current leading guess ranking is
     * tracked, and if this leading ranking becomes higher than the maximum possible
     * ranking of the current guesser, then there is no point going any further as it
     * is impossible for the current leading ranking to be beaten by any of the remaining
     * guessers.
     * 
     * The given list contains the names of types that are to be disabled, or ignored.
     * The guessed type thus cannot be one of these types.
     * 
     * @param source
     * @return best guess, or null if no guessers available
     * @throws IOException
     */
    public Guess getBestGuess(XenaInputSource source, List<String> disabledTypeList) throws IOException
    {
    	Guess leadingGuess = null;
    	int leadingRanking = Integer.MIN_VALUE;
    	if (disabledTypeList == null)
    	{
    		disabledTypeList = new ArrayList<String>();
    	}
    	
        //cycle through our guessers and get all of the guesses for this particular type...
        try 
        {
        	TreeSet<Guesser> sortedSet = new TreeSet<Guesser>(guessers);
        	
        	// Reverse so higher-ranked guessers at start of list
        	ArrayList<Guesser> sortedGuessers = new ArrayList<Guesser>(sortedSet);
        	Collections.reverse(sortedGuessers);
        	
        	for (Guesser guesser : sortedGuessers)
        	{
        		if (disabledTypeList.contains(guesser.getType().getName()))
        		{
        			// This guesser has been disabled
        			continue;
        		}
        		
        		if (leadingRanking < guesser.getMaximumRanking())
        		{
		        	try
		        	{
		                Guess newGuess = guesser.guess(source);
		                
	                    // now we have our guess, and it's a possible goer, lets get a ranking for it.
	                    Integer ranking = getRanking(newGuess);
	                    
	                    // if it is less than 0, forgedaboudit.
	                    if (ranking > leadingRanking ) {
	                    	leadingGuess = newGuess;
	                    	leadingRanking = ranking;
	                    }
		        	}
		        	// Just log exceptions as we want the other guessers to have a chance
		        	catch (IOException iex)
		        	{
		        		logger.log(Level.FINER,
		        		           "Exception thrown in guesser " + guesser.getName(),
		        		           iex);
		        	}
		        	catch (XenaException xex)
		        	{
		        		logger.log(Level.FINER,
		        		           "Exception thrown in guesser " + guesser.getName(),
		        		           xex);
		        	}
        		}
        		else
        		{
        			// No point checking the rest of the guessers as they 
        			// cannot beat the current best guess
        			break;
        		}
        	}
        } 
        finally 
        {
            source.close();
        }
        
        logger.finest("XIS " + source.getSystemId() + 
                      " guessed as type " + 
                      leadingGuess.getType().getName());
        
        return leadingGuess;
    }

    /**
     * Return a list of all the possible types this could be, sorted in order of
     * likelihood, and within that sorted by plugin load order.
     * 
     * @param source
     *            source of data
     * @return List of types ordered by most likely and within that, plugin
     *         order.
     * @throws IOException
     */
    public List<Type> getPossibleTypes(XenaInputSource source)
            throws IOException, XenaException {
        List<Guess> guesses = getGuesses(source);
        List<Type> typeList = new ArrayList<Type>();

        for (Iterator iter = guesses.iterator(); iter.hasNext();) {
            Guess guess = (Guess) iter.next();
            typeList.add(guess.getType());
        }
        return typeList;
    }
   
    
    
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
    
    public static Integer getRanking(Guess guess) {
        
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
        
        // notout
//        System.out.println("Guess: " + guess.getType().getName() + " has ranking: " + ranking);
        
        return new Integer(ranking);
    }
    
    
}
