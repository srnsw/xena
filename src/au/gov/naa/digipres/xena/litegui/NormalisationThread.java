/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 2/12/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.FileUtils;
import au.gov.naa.digipres.xena.util.GlassPane;
import au.gov.naa.digipres.xena.util.ProgressDialog;

/**
 * Thread to normalise a set of files. If a directory is listed
 * in the input files, then all files in the directory and all of
 * the directory's sub-directories are added to the list of files
 * to be normalised.
 * If the "Binary Only" option is set, then the Binary Normaliser
 * is the only normaliser used for all the files. Otherwise no
 * normaliser is specified, and the normaliser to be used will be
 * guessed in the underlying API code.
 * The "Binary Normalise Errors" option retrieves the list of
 * items that were not successfully normalised in a previous 
 * attempt and binary normalises these files.
 * The thread itself loops through each file and normalises it. If
 * an error occurs, then the status label is coloured red, and the
 * error count on the label is incremented. 
 * After processing each file, the progress bar is updated.
 * The normalisation state is also updated appropriately, whether
 * it be from normal start and normal end (ie all items finished),
 * or from one of the buttons being pushed.
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormalisationThread extends Thread {
	// private static final String MULTI_PAGE_NORMALISER_NAME = "Multi-page";
	// private static final String MULTI_PAGE_TYPE_NAME = "MultiPage";

	private static final String BINARY_NORMALISER_NAME = "Binary";
	private static final String TEXT_AIP_DIR_NAME = "text-version";
	// Normalisation states
	public static final int ERROR = -1;
	public static final int UNSTARTED = 0;
	public static final int STOPPED = 1;
	public static final int RUNNING = 2;
	public static final int PAUSED = 3;

	private volatile int threadState = UNSTARTED;

	// Normalisation modes
	public static final int STANDARD_MODE = 0;
	public static final int BINARY_MODE = 1;
	public static final int BINARY_ERRORS_MODE = 2;

	private Xena xenaInterface;
	private NormalisationResultsTableModel tableModel;
	private File destinationDir;
	private List<File> itemList;
	private Map<String, Set<NormaliserResults>> parentToChildrenMap = new HashMap<String, Set<NormaliserResults>>();
	private int mode;
	private boolean retainDirectoryStructure, performTextNormalisation;
	private int index;
	private int errorCount;
	private Frame parentFrame;

	private ArrayList<NormalisationStateChangeListener> ntscListeners;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Initialises the thread, setting global variables and
	 * the progress bar, and creating the full file list
	 * 
	 * @param mode
	 * @param retainDirectoryStructure 
	 * @param performTextNormalisation 
	 * @param xenaInterface
	 * @param tableModel
	 * @param itemList
	 * @param destinationDir
	 */
	public NormalisationThread(int mode, boolean retainDirectoryStructure, boolean performTextNormalisation, Xena xenaInterface,
	                           NormalisationResultsTableModel tableModel, List<File> itemList, File destinationDir, Frame parentFrame) {
		this.xenaInterface = xenaInterface;
		this.tableModel = tableModel;
		this.itemList = itemList;
		this.destinationDir = destinationDir;
		this.mode = mode;
		this.retainDirectoryStructure = retainDirectoryStructure;
		this.performTextNormalisation = performTextNormalisation;
		this.parentFrame = parentFrame;

		ntscListeners = new ArrayList<NormalisationStateChangeListener>();

	}

	/**
	 * Start the Normalisation thread
	 */
	@Override
	public void run() {
		logger.finest("Normalisation thread started");

		try {
			// Check if we want to retain the original directory structure of the files. This will entail
			// determining the common base directory of all the files, and then storing the relative path
			// from the base directory to each file in the normalised file.
			if (retainDirectoryStructure) {
				File commonBasePath = null;

				// For each item, get the common base path between that item and the current common base path. The final result
				// will be the common base path for all items.
				for (File item : itemList) {
					// If this is the first item, commonBasePath will be null, so we just set commonBasePath to be the item's directory
					if (commonBasePath == null) {
						if (item.isFile()) {
							commonBasePath = item.getParentFile();
						} else {
							commonBasePath = item;
						}
					} else {
						commonBasePath = FileUtils.getCommonBaseDir(commonBasePath, item);
						if (commonBasePath == null) {
							break;
						}
					}
				}

				if (commonBasePath == null) {
					// Do nothing - if the base path is null then all normalised files will only have the file itself as the source path.
					logger.fine("No common base path for item list.");
				} else {
					logger.fine("Common base path is " + commonBasePath.getAbsolutePath());
					xenaInterface.setBasePath(commonBasePath.getAbsolutePath());
				}

			} else {
				// Do nothing - all normalised files will only have the file itself as the source path.
			}

			if (mode == BINARY_ERRORS_MODE) {
				normaliseErrors(mode);
			} else {
				normaliseStandard(mode);
			}
		} catch (Exception e) {
			fireNormalisationError("A problem occurred when normalising", e);
		}
	}

	/**
	 * Recursive method to retrieve the full list of files
	 * contained within the specified files and directories.
	 * The list of files is added to the given Set.
	 * @param fileArr initial list of files and directories
	 * @param fileHashSet full list of files
	 */
	private void getFileSet(File[] fileArr, Set<File> fileHashSet) {
		for (File file : fileArr) {
			if (file.isDirectory()) {
				getFileSet(file.listFiles(), fileHashSet);
			} else {
				fileHashSet.add(file);
			}
		}
	}

	/**
	 * Carries out normalisation for each of the files and directories
	 * contained in itemList.
	 * A Thread state change events is fired before the normalisation
	 * process begins for each file, so that the listening main frame
	 * can update its status bar. A final state change event is fired
	 * when the entire process has finished, either because all files
	 * have been processed or the user has manually stopped the process.
	 * @param modeParam
	 * @throws XenaException 
	 * @throws IOException 
	 */
	private void normaliseStandard(int modeParam) throws XenaException, IOException {
		// Have to use global variables for the indices as they are
		// updated in called methods.
		index = 0;
		errorCount = 0;
		threadState = RUNNING;

		// Create the full file list, recursively adding all
		// the files contained within any specified directories
		Set<File> fileSet = new TreeSet<File>();
		File[] fileArr = itemList.toArray(new File[0]);
		getFileSet(fileArr, fileSet);
		Set<XenaInputSource> xisSet = getXisSet(fileSet);

		// Guess the files, and filter out children
		if (modeParam == STANDARD_MODE) {
			setTypes(xisSet);
			doFiltering(xisSet);
		}

		for (XenaInputSource xis : xisSet) {
			fireStateChangedEvent(RUNNING, xisSet.size(), index - errorCount, errorCount, xis.getFile().getName());

			normaliseFile(xis, modeParam, -1, xisSet.size());

			// Check to see if thread has been stopped
			if (threadState == STOPPED) {
				logger.finest("Normalisation thread stopped");
				break;
			}
		}
		fireStateChangedEvent(STOPPED, xisSet.size(), index - errorCount, errorCount, null);
	}

	/**
	 * Binary normalise all items that have not been successfully
	 * normalised.
	 * A Thread state change events is fired before the normalisation
	 * process begins for each file, so that the listening main frame
	 * can update its status bar. A final state change event is fired
	 * when the entire process has finished, either because all files
	 * have been processed or the user has manually stopped the process.
	 * 
	 * @param modeParam
	 * @throws IOException 
	 */
	private void normaliseErrors(int modeParam) {
		index = 0;
		errorCount = 0;
		threadState = RUNNING;

		// Row indices of entries that were not normalised successfully
		List<Integer> errorIndices = tableModel.getErrorIndices();

		for (int errorResultIndex : errorIndices) {
			NormaliserResults results = tableModel.getNormaliserResults(errorResultIndex);
			XenaInputSource xis = new XenaInputSource(results.getInputSystemId(), results.getInputType());

			fireStateChangedEvent(RUNNING, errorIndices.size(), index - errorCount, errorCount, xis.getSystemId());

			normaliseFile(xis, modeParam, errorResultIndex, errorIndices.size());

			// Check to see if thread has been stopped
			if (threadState == STOPPED) {
				logger.finest("Normalisation thread stopped");
				break;
			}
		}
		fireStateChangedEvent(STOPPED, errorIndices.size(), index - errorCount, errorCount, null);

	}

	/**
	 * Normalise the given file.
	 * If the mode is BINARY_MODE or BINARY_ERRORS_MODE, then the 
	 * BinaryNormaliser is instantiated and is the Normaliser specified 
	 * when calling the API normalisation method.
	 * If not, then no Normaliser is specified when calling the API method,
	 * which means that the Normaliser to use will be guessed based on the
	 * input file.
	 * State change events are fired if the thread is paused or restarted
	 * by the user.
	 * 
	 * @param file
	 * @param modeParam
	 * @param modelIndex
	 * @param totalFileCount
	 */
	private void normaliseFile(XenaInputSource xis, int modeParam, int modelIndex, int totalFileCount) {
		try {
			NormaliserResults results = null;

			if (modeParam == BINARY_MODE || modeParam == BINARY_ERRORS_MODE) {
				// Instantiate BinaryNormaliser
				// AbstractNormaliser binaryNormaliser = NormaliserManager.singleton().lookup(BINARY_NORMALISER_NAME);
				// properly!
				AbstractNormaliser binaryNormaliser = xenaInterface.getPluginManager().getNormaliserManager().lookup(BINARY_NORMALISER_NAME);

				results = xenaInterface.normalise(xis, binaryNormaliser, destinationDir);
			} else {
				// Do not specify a Normaliser
				results = xenaInterface.normalise(xis, destinationDir);
			}

			if (results == null) {
				throw new XenaException("Normalisation failed, reason unknown");
			} else if (!results.isNormalised()) {
				errorCount++;

				logger.finer("Normalisation failed:\n" + "Source: " + results.getInputSystemId());
			}

			// Perform text normalisation if required
			boolean textAIPProduced = false;
			if (modeParam == STANDARD_MODE && performTextNormalisation) {
				// Text AIP dir is in the same directory as the normalisaed AIP dir
				File textAIPDestinationDir = new File(destinationDir, TEXT_AIP_DIR_NAME);

				try {
					PluginManager pluginManager = xenaInterface.getPluginManager();
					AbstractNormaliser textNormaliser = pluginManager.getNormaliserManager().lookupTextNormaliser(xis.getType());

					// We cnanot produce a text version for all file types, so the textNormaliser may be null. If so, do nothing.
					if (textNormaliser != null) {
						AbstractFileNamer defaultFileNamer = pluginManager.getFileNamerManager().getActiveFileNamer();
						AbstractMetaDataWrapper emptyWrapper = pluginManager.getMetaDataWrapperManager().getEmptyWrapper().getWrapper();
						if (!textAIPDestinationDir.exists()) {
							textAIPDestinationDir.mkdir();
						}
						NormaliserResults textNormaliserResults =
						    xenaInterface.normalise(xis, textNormaliser, textAIPDestinationDir, defaultFileNamer, emptyWrapper);

						if (textNormaliserResults.isNormalised()) {
							textAIPProduced = true;
						}
					}
				} catch (XenaException xex) {
					// If we encounter an exception, just log an error and do nothing.
					logger.log(Level.SEVERE, "Problem producing text AIP for " + xis.getSystemId(), xex);
				}
			}

			// Add results to display table. If we are in
			// BINARY_ERRORS_MODE, then the row is updated
			// rather than added.
			if (modeParam == BINARY_ERRORS_MODE) {
				tableModel.setNormalisationResult(modelIndex, results, new Date(), false);
			} else {
				tableModel.addNormalisationResult(results, new Date(), textAIPProduced);

				// Add any child results for this item
				Set<NormaliserResults> childSet = parentToChildrenMap.get(results.getInputSystemId());
				if (childSet != null) {
					for (NormaliserResults childResults : childSet) {
						childResults.setOutputFileName(results.getOutputFileName());
						childResults.setNormalised(true);
						childResults.setDestinationDirString(results.getDestinationDirString());
						childResults.setNormaliserName(results.getNormaliserName());
						tableModel.addNormalisationResult(childResults, new Date(), false);
					}
				}
			}

			tableModel.fireTableDataChanged();

			logger.finer("Normalisation successful:\n" + "Source: " + results.getInputSystemId() + "\n" + "Destination: "
			             + results.getDestinationDirString() + File.separator + results.getOutputFileName());

		} catch (Exception e) {
			// Status label is now red to indicate an error
			errorCount++;

			// Create a new NormaliserResults object to display
			// in the results table. Set all the data we can.
			NormaliserResults errorResults = new NormaliserResults();
			errorResults.setInputSystemId(xis.getSystemId());
			errorResults.setInputType(xis.getType());
			errorResults.addException(e);
			errorResults.setOutputFileName("");

			// Add results to display table. If we are in
			// BINARY_ERRORS_MODE, then the row is updated
			// rather than added.
			if (modeParam == BINARY_ERRORS_MODE) {
				tableModel.setNormalisationResult(modelIndex, errorResults, new Date(), false);
			} else {
				tableModel.addNormalisationResult(errorResults, new Date(), false);
			}

			tableModel.fireTableDataChanged();

			logger.finer("Normalisation failed for " + errorResults.getInputSystemId() + ": " + e);
		} finally {
			index++;
		}

		// Check to see if thread has been paused
		if (threadState == PAUSED) {
			logger.finest("Normalisation thread paused");
			fireStateChangedEvent(PAUSED, totalFileCount, index - errorCount, errorCount, xis.getSystemId());
			doPause();

			// Have returned from pause
			if (threadState == RUNNING) {
				logger.finest("Normalisation thread restarted");
				fireStateChangedEvent(RUNNING, totalFileCount, index - errorCount, errorCount, xis.getSystemId());
			}
		}

	}

	/**
	 * Removes from the given set all XISs which
	 * are not specified as "Children" by the Xena API.
	 * The global parent to children map is also set.
	 * 
	 * @param xisSet file set to filter
	 * @throws XenaException
	 */
	private void doFiltering(Set<XenaInputSource> xisSet) {

		// Get set of children
		Map<XenaInputSource, NormaliserResults> childrenXisMap = xenaInterface.getChildren(xisSet);

		parentToChildrenMap = getParentToChildrenMap(childrenXisMap);

		// Remove children from input set and return
		xisSet.removeAll(childrenXisMap.keySet());
	}

	private Map<String, Set<NormaliserResults>> getParentToChildrenMap(Map<XenaInputSource, NormaliserResults> childrenXisMap) {
		Map<String, Set<NormaliserResults>> retMap = new HashMap<String, Set<NormaliserResults>>();
		for (NormaliserResults results : childrenXisMap.values()) {
			if (retMap.containsKey(results.getParentSystemId())) {
				Set<NormaliserResults> resultsSet = retMap.get(results.getParentSystemId());
				resultsSet.add(results);
			} else {
				Set<NormaliserResults> resultsSet = new HashSet<NormaliserResults>();
				resultsSet.add(results);
				retMap.put(results.getParentSystemId(), resultsSet);
			}
		}
		return retMap;
	}

	private Set<XenaInputSource> getXisSet(Set<File> fileSet) throws IOException {
		// Convert set of Files into set of XenaInputSources
		Set<XenaInputSource> xisSet = new LinkedHashSet<XenaInputSource>();
		for (File file : fileSet) {
			xisSet.add(new XenaInputSource(file));
		}
		return xisSet;
	}

	private void setTypes(Set<XenaInputSource> xisSet) throws XenaException, IOException {
		GlassPane gp = GlassPane.mount(parentFrame, true);
		gp.setVisible(true);

		ProgressDialog progressDialog = new ProgressDialog(parentFrame, "Guessing...", 0, xisSet.size());

		int count = 0;
		for (XenaInputSource xis : xisSet) {
			try {
				String decodedFilename = URLDecoder.decode(xis.getSystemId(), "UTF-8");
				progressDialog.setNote(decodedFilename);
			} catch (UnsupportedEncodingException e1) {
				// UTF-8 is the inbuilt java default so this should never happen!
			}

			Type type = xenaInterface.getMostLikelyType(xis);
			if (type != null) {
				xis.setType(type);
			}
			progressDialog.setProgress(++count);
		}

		gp.setVisible(false);
	}

	/**
	 * Pause the normalisation process. Repeat a sleep period
	 * of 50 milliseconds, each time checking that the
	 * thread has not been restarted.
	 */
	private void doPause() {
		while (threadState == PAUSED) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// Stop normalisation
				threadState = STOPPED;
			}
		}
	}

	/**
	 * Broadcast to all listeners that the thread state has changed
	 * @param newState
	 */
	private void fireStateChangedEvent(int newState, int totalItems, int normalisedItems, int errorItems, String currentFile) {
		for (NormalisationStateChangeListener ntscl : ntscListeners) {
			ntscl.normalisationStateChanged(newState, totalItems, normalisedItems, errorItems, currentFile);
		}
	}

	private void fireNormalisationError(String message, Exception e) {
		for (NormalisationStateChangeListener ntscl : ntscListeners) {
			ntscl.normalisationError(message, e);
		}
	}

	/**
	 * Set the thread state to the given state
	 * @param threadState
	 */
	public void setThreadState(int threadState) {
		this.threadState = threadState;
	}

	/**
	 * Add a NormalisationStateChangeListener
	 * @param l
	 * @return
	 */
	public boolean add(NormalisationStateChangeListener l) {
		return ntscListeners.add(l);
	}

	/*
	 * Probably not going to use multi-page normalisation unless it's REALLY wanted... leave it here just in case.
	 * 
	 * private void normaliseMultiPage() { progressBar.setMaximum(1);
	 * 
	 * File[] fileArr = fileSet.toArray(new File[0]); MultiInputSource mis = new MultiInputSource(fileArr, null);
	 * 
	 * int errorCount = 0; try { Type multiType = TypeManager.singleton().lookup(MULTI_PAGE_TYPE_NAME);
	 * mis.setType(multiType);
	 * 
	 * AbstractNormaliser multiNormaliser = NormaliserManager.singleton().lookup(MULTI_PAGE_NORMALISER_NAME);
	 *  // Have to create log to avoid null pointer exception! XenaResultsLog log = new XenaResultsLog();
	 * log.setAutoLog(System.out); multiNormaliser.setProperty("http://xena/log", log);
	 * 
	 * NormaliserResults results = xenaInterface.normalise(mis, multiNormaliser, destinationDir);
	 * 
	 * if (!results.isNormalised()) { statusLabel.setForeground(Color.RED); errorCount++; }
	 * 
	 * tableModel.addNormalisationResult(results, new Date()); tableModel.fireTableDataChanged();
	 *  } catch (Exception e) { errorCount++; statusLabel.setForeground(Color.RED);
	 * 
	 * NormaliserResults errorResults = new NormaliserResults(); errorResults.setInputSystemId(mis.getSystemId());
	 * errorResults.addException(e); errorResults.setOutputFileName("");
	 * 
	 * tableModel.addNormalisationResult(errorResults, new Date()); tableModel.fireTableDataChanged(); } finally {
	 * progressBar.setValue(1);
	 * 
	 * String statusText = "1 of 1 completed (" + errorCount + " error(s))"; statusLabel.setText(statusText); } }
	 */

}
