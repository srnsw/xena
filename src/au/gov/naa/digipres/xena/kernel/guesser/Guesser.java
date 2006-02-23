/*
 * Created on 10/01/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

public abstract class Guesser implements Comparable {
    
	private Guess bestPossibleGuess;
	
    
    public Guesser()
    {
    	bestPossibleGuess = createBestPossibleGuess();
    }
    
    public abstract String getName();
    public abstract Guess guess(XenaInputSource xenaInputSource) 
    	throws XenaException, IOException;
    protected abstract Guess createBestPossibleGuess();

    public int getMaximumRanking()
    {
    	return GuesserManager.getRanking(bestPossibleGuess);
    }
    
    
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Guesser)
		{
			Guesser compareGuesser = (Guesser)obj;
			if (this.compareTo(compareGuesser) == 0)
			{
				return this.getName().equals(compareGuesser.getName());
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object obj)
	{
		int result;
		if (obj == null)
		{
			result = 1;
		}
		else if (obj instanceof Guesser)
		{
			Guesser compareGuesser = (Guesser)obj;
			result = this.getMaximumRanking() - compareGuesser.getMaximumRanking();
			if (result == 0)
			{
				result = this.getName().compareTo(compareGuesser.getName());
			}
		}
		else
		{
			result = this.hashCode() - obj.hashCode();
		}
		
		return result;
	}

    
}
