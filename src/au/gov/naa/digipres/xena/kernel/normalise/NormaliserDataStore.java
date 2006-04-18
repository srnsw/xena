package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

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
    public NormaliserDataStore(TransformerHandler contentHandler,
            File outputFile, File configFile, OutputStream out,
            boolean existsAlready) {
        this.transformerHandler = contentHandler;
        this.outputFile = outputFile;
        this.configFile = configFile;
        this.out = out;
        this.existsAlready = existsAlready;
    }
    
    public String toString(){
        
        String foo = "transformerHandler:[" + transformerHandler + "]\n" + 
        "outputfile:[" + outputFile + "]\n"+ 
        "configfile:[" + configFile + "]\n"+
        "existsAlready:["+existsAlready + "]";
        
        return foo;
        
    }
    
    
    /**
     * @return Returns the configFile.
     * @deprecated
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * @return Returns the contentHandler.
     * @deprecated
     */
    public ContentHandler getTransformerHandler() {
        return transformerHandler;
    }

    /**
     * @return Returns the existsAlready.
     * @deprecated
     */
    public boolean getExistsAlready() {
        return existsAlready;
    }

    /**
     * @return Returns the out.
     * @deprecated
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * @return Returns the outputFile.
     * @deprecated
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * @param out
     *            The out to set.
     * @deprecated
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    /**
     * @return Returns the inputSourceName.
     * @deprecated
     */
    public String getInputSourceName() {
        return inputSourceName;
    }

    /**
     * @param inputSourceName The inputSourceName to set.
     * @deprecated
     */
    public void setInputSourceName(String inputSourceName) {
        this.inputSourceName = inputSourceName;
    }

    /**
     * @return Returns the normaliserName.
     * @deprecated
     */
    public String getNormaliserName() {
        return normaliserName;
    }

    /**
     * @param normaliserName The normaliserName to set.
     * @deprecated
     */
    public void setNormaliserName(String normaliserName) {
        this.normaliserName = normaliserName;
    }

    /**
     * @return Returns the guessedType.
     * @deprecated
     */
    public String getGuessedType() {
        return guessedType;
    }

    /**
     * @param guessedType The guessedType to set.
     * @deprecated
     */
    public void setGuessedType(String guessedType) {
        this.guessedType = guessedType;
    }
}