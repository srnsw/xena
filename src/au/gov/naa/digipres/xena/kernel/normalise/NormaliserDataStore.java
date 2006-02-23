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
 * An object that just stores a collection of relevant object related to the
 * normalisation process.
 * 
 * XXX: AAK - THE NAME OF THIS CLASS MAY NEED TO BE CHANGED.
 */
public class NormaliserDataStore {
    private TransformerHandler transformerHandler;
    private File outputFile;
    private File configFile;
    private OutputStream out;
    private boolean existsAlready;
    
    private String inputSourceName;
    private String normaliserName;
    private String guessedType;
    

    // ADDED AAK...
    private XenaInputSource xenaInputSource;

    public NormaliserDataStore(TransformerHandler contentHandler,
            File outputFile, File configFile, OutputStream out,
            boolean existsAlready) {
        this.transformerHandler = contentHandler;
        this.outputFile = outputFile;
        this.configFile = configFile;
        this.out = out;
        this.existsAlready = existsAlready;
    }
    
    /**
     * Constructor...
     * @param normaliser
     * @param xenaInputSource
     * @param outputFile
     * @param configFile
     * @throws XenaException
     * @throws FileNotFoundException
     */
    public NormaliserDataStore(XMLReader normaliser, XenaInputSource xenaInputSource, File outputFile)
    throws XenaException, FileNotFoundException {
        this.xenaInputSource = xenaInputSource;
        this.outputFile = outputFile;
        
        SAXTransformerFactory transformFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        try {
            transformerHandler = transformFactory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new XenaException("Unable to create transformerHandler due to transformer configuruation exception.");
        }
        OutputStream out = null;
        existsAlready = false;
        out = new FileOutputStream(outputFile);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
            StreamResult streamResult = new StreamResult(osw);
            transformerHandler.setResult(streamResult);
        } catch (UnsupportedEncodingException e) {
            throw new XenaException("Unsupported encoder for output stream writer.");
        }
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
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * @return Returns the contentHandler.
     */
    public ContentHandler getTransformerHandler() {
        return transformerHandler;
    }

    /**
     * @return Returns the existsAlready.
     */
    public boolean getExistsAlready() {
        return existsAlready;
    }

    /**
     * @return Returns the out.
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * @return Returns the outputFile.
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * @param out
     *            The out to set.
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    /**
     * @return Returns the inputSourceName.
     */
    public String getInputSourceName() {
        return inputSourceName;
    }

    /**
     * @param inputSourceName The inputSourceName to set.
     */
    public void setInputSourceName(String inputSourceName) {
        this.inputSourceName = inputSourceName;
    }

    /**
     * @return Returns the normaliserName.
     */
    public String getNormaliserName() {
        return normaliserName;
    }

    /**
     * @param normaliserName The normaliserName to set.
     */
    public void setNormaliserName(String normaliserName) {
        this.normaliserName = normaliserName;
    }

    /**
     * @return Returns the guessedType.
     */
    public String getGuessedType() {
        return guessedType;
    }

    /**
     * @param guessedType The guessedType to set.
     */
    public void setGuessedType(String guessedType) {
        this.guessedType = guessedType;
    }
}