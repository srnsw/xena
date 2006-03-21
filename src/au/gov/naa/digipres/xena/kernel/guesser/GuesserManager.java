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
import au.gov.naa.digipres.xena.kernel.PluginManager;
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
	
    protected List<Guesser> guessers = new ArrayList<Guesser>();
    private GuessRankerInterface guessRanker = new DefaultGuessRanker();
    private PluginManager pluginManager;
    

    public GuesserManager(PluginManager pluginManager) {
        // okay - here we initialise all of our default Xena guesses.
        // at the moment that is only the binary normaliser.
        this.pluginManager = pluginManager;
        try {
            Guesser binaryGuesser = new BinaryGuesser();
            binaryGuesser.initGuesser(this);
            guessers.add(binaryGuesser);
        } catch (XenaException xe) {
            logger.log(Level.FINER, 
                       "Unable to load binary guesser! Probably because the type was not loaded or some such...",
                       xe);
            xe.printStackTrace();
        }
    }
    

    /**
     * @return Returns the pluginManager.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * @param pluginManager The new value to set pluginManager to.
     */
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * toString the toString method.
     * @return A string representing the current instance of the guessermanager.
     */
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
            for (Guesser guesser: instances) {
                guesser.initGuesser(this);
            }
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

        
        //cycle through our guessers and get all of the guesses for this particular type...
        try {
            for (Iterator guesserIterator = guessers.iterator(); guesserIterator.hasNext();) {
            	Guesser guesser = (Guesser) guesserIterator.next();
            	try
            	{
	                Guess newGuess = guesser.guess(xenaInputSource);
	                // If we are not possible skip to the next guess!
	                if (newGuess.getPossible() != GuessIndicator.FALSE) {
	                    
	                    // now we have our guess, and it's a possible goer, lets get a ranking for it.
	                    Integer ranking = guessRanker.getRanking(newGuess);
	                    
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
	                    Integer ranking = guessRanker.getRanking(newGuess);
	                    
	                    // If this guess beats the current leader, 
	                    // it is now the new leader.
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
   
    
    /**
     * @return Returns the myGuessRanker.
     */
    public GuessRankerInterface getGuessRanker() {
        return guessRanker;
    }

    /**
     * @param myGuessRanker The new value to set myGuessRanker to.
     */
    public void setGuessRanker(GuessRankerInterface myGuessRanker) {
        this.guessRanker = myGuessRanker;
    }
    
 
}
