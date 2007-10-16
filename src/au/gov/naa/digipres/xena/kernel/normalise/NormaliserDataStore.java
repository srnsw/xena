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

package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.OutputStream;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.ContentHandler;

/**
 * @deprecated
 * An object that just stores a collection of relevant object related to the
 * normalisation process.
 * 
 * XXX: AAK - THE NAME OF THIS CLASS MAY NEED TO BE CHANGED.
 * @deprecated
 */
@Deprecated
public class NormaliserDataStore {
	private TransformerHandler transformerHandler;
	private File outputFile;
	private File configFile;
	private OutputStream out;
	private boolean existsAlready;

	private String inputSourceName;
	private String normaliserName;
	private String guessedType;

	/**
	 * 
	 * @deprecated
	 * @param contentHandler
	 * @param outputFile
	 * @param configFile
	 * @param out
	 * @param existsAlready
	 */
	@Deprecated
    public NormaliserDataStore(TransformerHandler contentHandler, File outputFile, File configFile, OutputStream out, boolean existsAlready) {
		this.transformerHandler = contentHandler;
		this.outputFile = outputFile;
		this.configFile = configFile;
		this.out = out;
		this.existsAlready = existsAlready;
	}

	@Override
    public String toString() {

		String foo =
		    "transformerHandler:[" + transformerHandler + "]\n" + "outputfile:[" + outputFile + "]\n" + "configfile:[" + configFile + "]\n"
		            + "existsAlready:[" + existsAlready + "]";

		return foo;

	}

	/**
	 * @return Returns the configFile.
	 * @deprecated
	 */
	@Deprecated
    public File getConfigFile() {
		return configFile;
	}

	/**
	 * @return Returns the contentHandler.
	 * @deprecated
	 */
	@Deprecated
    public ContentHandler getTransformerHandler() {
		return transformerHandler;
	}

	/**
	 * @return Returns the out.
	 * @deprecated
	 */
	@Deprecated
    public OutputStream getOut() {
		return out;
	}

	/**
	 * @return Returns the outputFile.
	 * @deprecated
	 */
	@Deprecated
    public File getOutputFile() {
		return outputFile;
	}

	/**
	 * @param out
	 *            The out to set.
	 * @deprecated
	 */
	@Deprecated
    public void setOut(OutputStream out) {
		this.out = out;
	}

	/**
	 * @return Returns the inputSourceName.
	 * @deprecated
	 */
	@Deprecated
    public String getInputSourceName() {
		return inputSourceName;
	}

	/**
	 * @param inputSourceName The inputSourceName to set.
	 * @deprecated
	 */
	@Deprecated
    public void setInputSourceName(String inputSourceName) {
		this.inputSourceName = inputSourceName;
	}

	/**
	 * @return Returns the normaliserName.
	 * @deprecated
	 */
	@Deprecated
    public String getNormaliserName() {
		return normaliserName;
	}

	/**
	 * @param normaliserName The normaliserName to set.
	 * @deprecated
	 */
	@Deprecated
    public void setNormaliserName(String normaliserName) {
		this.normaliserName = normaliserName;
	}

	/**
	 * @return Returns the guessedType.
	 * @deprecated
	 */
	@Deprecated
    public String getGuessedType() {
		return guessedType;
	}

	/**
	 * @param guessedType The guessedType to set.
	 * @deprecated
	 */
	@Deprecated
    public void setGuessedType(String guessedType) {
		this.guessedType = guessedType;
	}
}
