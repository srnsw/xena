/*
 * Created on 17/10/2005
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.view.ViewManager;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * This is a factory class to produce a JPanel that contains the view of a
 * normalised xena object. The JPanel will contain the component that represents
 * the view. For example:<ul>
 *  <li>For XML tree view, return a JTree component.</li>
 *  <li>For plaintext, return a text field</li>
 *  <li>For image, return a canvas.</li>
 *  <li>For NAA Package view, return a split pane, the bottom one contains meta information, the top
 *    is the view that has been wrapped (image, plaintext etc).</li>
 *  </ul>
 * The factory should reference a XenaObject, so that it can get a view manager, and
 * can focus on creating the jpanel and not worrying about anything else (like loading
 * plugins etc)
 * 
 * @author Andrew Keeling
 * @author Justin Waddell
 * @version 0.1
 * 
 */
public class NormalisedObjectViewFactory {
    
    private ViewManager viewManager;
    private Xena xena;
    
    /**
     * Default constructor. Create a NormalisedObjectViewFactory with a reference to the supplied
     * Xena instance.
     * 
     * @param xena - the Xena object to control the NormalisedObjectViewFactory.
     */
    public NormalisedObjectViewFactory(Xena xena)
    {
        this.xena = xena;
        viewManager = this.xena.getPluginManager().getViewManager();
    }
    
    /**
     * Constructor that doesnot use Xena - just a view manager. 
     * 
     * @param viewManager - a direct reference to the viewManager that the view factory will use.
     */
    public NormalisedObjectViewFactory(ViewManager viewManager)
    {
    	this.viewManager = viewManager;
    }
    

    /**
     * This returns the default XenaView for a given Xena file.
     * 
     * @param xenaFile Xena file to display
     * @return XenaView (JPanel) containing the display of the normalised file
     */
    
    public XenaView getView(File xenaFile) throws XenaException
    {
        return getView(xenaFile, null);
    }
    
    /**
     * This returns a XenaView. If the given viewType is null, then the
     * default view type for the given xena file will be retrieved and used.
     * 
     * @param xenaFile Xena file to display
     * @param viewType represents a template for the type of view to display
     * @return XenaView (JPanel) containing the display of the normalised file
     */
    public XenaView getView(File xenaFile, XenaView viewType) throws XenaException 
    {
    	// JRW - Now returns XenaView instead of JPanel
    	// JRW Now accepts viewType - null will retrieve default
    	
        // check out our file here...
        // make sure it is the appropriate type etc.        
        
        XenaInputSource xis = null;
        //create the xis
        try {
             xis = new XenaInputSource(xenaFile);
        } catch (FileNotFoundException e) {
            // TODO lets do something pretty here.
            throw new XenaException("File not found exception thrown. Unable to create XenaInputSource from " + xenaFile);
        }
        xis.setEncoding("UTF-8");
        
        // If viewType is null, get the default view
        
        if (viewType == null)
        {
        	viewType = viewManager.getDefaultView(xis);
        }
        // use our view to display the xena file...
        //get our parser, and parse the thing. that needs parsing.        
        //TODO Fix up exception handling...
        //FIXME Please fix up exception handling!
        try {
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            
            // This causes problems with JdomXenaViews, as the localname is set to empty by SAXParser but
            // the SAXHandler attempts to create an Element with the localname as it's name
            // TODO: Find a solution that disables namespaces but doesn't cause any other problems!
// //       Don't want namespaces for viewing, as namespace problems would throw an exception...
            reader.setFeature("http://xml.org/sax/features/namespaces",true);
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes",true);
                        
            reader.setContentHandler(viewType.getContentHandler());
            reader.parse(xis);
            xis.close();
            viewType.closeContentHandler();
            viewType.initListenersAndSubViews();
            viewType.parse();
        } catch (IOException iox) {
            throw new XenaException(iox);
        } catch (SAXException sx) {
            throw new XenaException(sx);
        } catch (ParserConfigurationException pce) {
            throw new XenaException(pce);
        }
        
        return viewType;
    }	
    
    /**
     * This method returns all the available views that are available to display a Xena object.
     * 
     * @return List containing loads xena view instances.
     */
    public List<XenaView> getAvailableViews() {
        return viewManager.getAllViews();
    }
    
    
}
