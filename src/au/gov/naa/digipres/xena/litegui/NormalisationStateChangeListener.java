/*
 * Created on 8/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.util.EventListener;

/**
 * Simple listener class to enable the normalisation thread to
 * broadcast changes to its state, eg RUNNING, PAUSED, STOPPED.
 * 
 * @author justinw5
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public interface NormalisationStateChangeListener extends EventListener
{
	public void normalisationStateChanged(int newState,
										  int totalItems,
										  int normalisedItems,
										  int errorItems,
										  String currentFile);
	
	public void normalisationError(String message,
								   Exception e);
		
}
