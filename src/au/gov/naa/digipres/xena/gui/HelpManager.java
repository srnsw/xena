package au.gov.naa.digipres.xena.gui;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.LoadManager;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 *  Help manager for Xena and plugins available at runtime
 *
 * @author     Chris Bitmead
 * @created    2 July 2002
 */
public class HelpManager implements LoadManager {
	static HelpManager theSingleton = new HelpManager();

	// Main HelpSet & Broker
	transient HelpSet mainHS = null;

	transient HelpBroker mainHB;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 */
	public HelpManager() {
		String helpsetName = "XenaHelp";
		try {
			ClassLoader cl = getClass().getClassLoader();
			URL url = HelpSet.findHelpSet(cl, helpsetName);
            if (url == null) {
                url = HelpSet.findHelpSet(cl, "doc/XenaHelp");
            }
            if (url == null) {
                throw new NullPointerException();
            }
            
            
			mainHS = new HelpSet(cl, url);
            
		} catch (Exception ee) {
            
			logger.log(Level.FINER, "Help Set " + helpsetName + " not found", ee);
			return;
		} catch (ExceptionInInitializerError ex) {
			logger.log(Level.FINER, "initialization error:", ex);
		}
		mainHB = mainHS.createHelpBroker();
        
    }

	/**
	 * @return    Description of the Returned Value
	 */
	public static HelpManager singleton() {
		return theSingleton;
	}

	/**
	 * @return    a singleton class for this class
	 */
	public HelpBroker getHelpBroker() {
		return mainHB;
	}

	/**
	 * @param  props                properties available at runtime (main and
	 *      plugin)
	 * @exception  XenaException
	 */
	public boolean load(JarPreferences props) throws XenaException {
		try {
			StringTokenizer st = new StringTokenizer(props.get("helpsets", "")); ;
			boolean rtn = false;
			while (st.hasMoreTokens()) {
				rtn = true;
				String helpsetname = st.nextToken();
				//notout
                //System.err.println("Help set name:"+helpsetname);
                URL url = HelpSet.findHelpSet(props.getClassLoader(), helpsetname);
                
                //TODO: ugly hack for helpset locations...
                //this is to allow helpsets, which are normally in the etc dir of a plugin
                //be found if they are put in the etc dir of the XENA project.
                if (url == null){
                    url = HelpSet.findHelpSet(props.getClassLoader(), "doc/" + helpsetname);
                }
                if (url == null){
                    String changedHelpSetName = helpsetname;
                    if (helpsetname.indexOf("doc/") != -1){
                        changedHelpSetName = helpsetname.substring(new String("doc/").length());
                    }
                    url = HelpSet.findHelpSet(props.getClassLoader(), changedHelpSetName);
                }
                
                
                if (url == null){
                    logger.finer("Could not load helpset for:" + helpsetname);
                    return false;
                }
                HelpSet subhelp = new HelpSet(props.getClassLoader(), url);
				mainHS.add(subhelp);
			}
			return rtn;
		} catch (HelpSetException e) {
			throw new XenaException(e);
		}
	}

	public void complete() {
	}
}
