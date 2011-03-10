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
 * Created on 1/12/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * TableModel for the Normalisation Results table.
 * Data is stored in an ArrayList of Hashtables. Each Hashtable
 * represents a row, with the column name mapped to the column value.
 * This enables the column order to be set by the order of the 
 * COLUMN_TITLES array, and the order need not be referred to anywhere else.
 * 
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormalisationResultsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	// Column headings
	private static final String SOURCE_TITLE = "Source";
	private static final String GUESSED_TYPE_TITLE = "Guessed Type";
	private static final String NORMALISER_TITLE = "Normaliser";
	private static final String SUCCESS_TITLE = "Success";
	private static final String DESTINATION_TITLE = "Destination";
	private static final String MESSAGE_TITLE = "Message";
	private static final String DATE_TITLE = "Date Normalised";
	private static final String TEXT_AIP_TITLE = "Text Version Produced";

	// Sets the order of the columns
	public static final String[] COLUMN_TITLES =
	    {SOURCE_TITLE, GUESSED_TYPE_TITLE, NORMALISER_TITLE, SUCCESS_TITLE, DESTINATION_TITLE, DATE_TITLE, MESSAGE_TITLE, TEXT_AIP_TITLE};

	private ArrayList<Hashtable<String, Object>> tableData;

	// Raw results, with rows in same order as the tableData
	private ArrayList<NormaliserResults> resultList;

	public NormalisationResultsTableModel() {
		super();
		tableData = new ArrayList<Hashtable<String, Object>>();
		resultList = new ArrayList<NormaliserResults>();
	}

	/**
	 * Adds corresponding entries to the table data and results list.
	 * Fields of the NormaliserResults object are added to a new 
	 * Hashtable using the addResultToHash method. The hashtable
	 * is then added to the tableData ArrayList. The raw NormaliserResults
	 * object is added directly to the resultList ArrayList.
	 * @param results
	 * @param dateNormalised
	 * @param textVersionProduced 
	 */
	public void addNormalisationResult(NormaliserResults results, Date dateNormalised, boolean textVersionProduced) {
		Hashtable<String, Object> entryHash = new Hashtable<String, Object>();
		addResultToHash(entryHash, results, dateNormalised, textVersionProduced);
		tableData.add(entryHash);

		// Store original results object for later display
		resultList.add(results);
	}

	/**
	 * Update the data for the given row index with the given
	 * NormaliserResults object.
	 *  
	 * @param index
	 * @param results
	 * @param dateNormalised
	 */
	public void setNormalisationResult(int index, NormaliserResults results, Date dateNormalised, boolean textVersionProduced) {
		Hashtable<String, Object> entryHash = tableData.get(index);
		entryHash.clear();
		addResultToHash(entryHash, results, dateNormalised, textVersionProduced);
		resultList.set(index, results);
	}

	/**
	 * Add the data contained in the given NormaliserResults object
	 * to the given Hashtable, using the appropriate column title
	 * as a key for each data item.
	 * 
	 * @param entryHash
	 * @param results
	 * @param dateNormalised
	 * @param textVersionProduced 
	 */
	private void addResultToHash(Hashtable<String, Object> entryHash, NormaliserResults results, Date dateNormalised, boolean textVersionProduced) {
		// Check for nulls
		String inputType = results.getInputType() != null ? results.getInputType().getName() : "";
		String normaliser = results.getNormaliserName() != null ? results.getNormaliserName() : "";

		// Add column data
		entryHash.put(SOURCE_TITLE, results.getInputSystemId());
		entryHash.put(GUESSED_TYPE_TITLE, inputType);
		entryHash.put(NORMALISER_TITLE, normaliser);
		entryHash.put(SUCCESS_TITLE, new Boolean(results.isNormalised()));
		entryHash.put(DESTINATION_TITLE, results.getOutputFileName());
		entryHash.put(MESSAGE_TITLE, results.getErrorMessage());
		entryHash.put(DATE_TITLE, dateNormalised);
		entryHash.put(TEXT_AIP_TITLE, new Boolean(textVersionProduced));
	}

	/**
	 * Clear all table data
	 */
	public void clear() {
		tableData.clear();
		resultList.clear();
	}

	/**
	 * Return complete list of raw NormaliserResults
	 * @return
	 */
	public List<NormaliserResults> getAllNormaliserResults() {
		return resultList;
	}

	/**
	 * Return the specified NormaliserResults object
	 * 
	 * @param index
	 * @return
	 */
	public NormaliserResults getNormaliserResults(int index) {
		return resultList.get(index);
	}

	/**
	 * Return the indices of all items that were not successfully
	 * normalised.
	 * 
	 * @return
	 */
	public List<Integer> getErrorIndices() {
		int index = 0;
		List<Integer> indices = new ArrayList<Integer>();

		for (NormaliserResults result : resultList) {
			if (!result.isNormalised()) {
				indices.add(index);
			}
			index++;
		}
		return indices;
	}

	// Implemented abstract methods

	public int getRowCount() {
		return tableData.size();
	}

	public int getColumnCount() {
		return COLUMN_TITLES.length;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (SUCCESS_TITLE.equals(COLUMN_TITLES[columnIndex]) || TEXT_AIP_TITLE.equals(COLUMN_TITLES[columnIndex])) {
			return Boolean.class;
		}
		return String.class;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_TITLES[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		// Finds the appropriate Hashtable (representing a row),
		// and returns the appropriate object from this Hashtable.
		Object data = tableData.get(rowIndex).get(COLUMN_TITLES[columnIndex]);

		if (DATE_TITLE.equals(COLUMN_TITLES[columnIndex])) {
			// Date needs to be formatted
			Date date = (Date) data;

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			return dateFormat.format(date);
		} else if (SOURCE_TITLE.equals(COLUMN_TITLES[columnIndex])) {
			// Remove URL encoding from source ID
			String decodedID;
			try {
				decodedID = URLDecoder.decode(data.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// If an exception occurs, just return original data
				decodedID = data.toString();
			}
			return decodedID;
		} else {
			return data;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// Finds the appropriate Hashtable (representing a row),
		// and sets the appropriate object to the given value
		Hashtable<String, Object> rowData = tableData.get(rowIndex);
		rowData.put(COLUMN_TITLES[columnIndex], aValue);
	}

}
