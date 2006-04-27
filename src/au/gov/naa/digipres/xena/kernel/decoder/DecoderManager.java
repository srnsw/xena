package au.gov.naa.digipres.xena.kernel.decoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.LoadManager;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * Class for managing decoders available to Xena at runtime. The concept of a
 * decoder is a kind of pre-processing step to make the actual data format
 * available. An example would be where you had a binary raw data field in a
 * database that was base64 encoded. Within the encoding it could be anything,
 * like an office document. The role of decoders would be to un-base64 the data.
 *
 * The concept of decoders is currently only used in the dataset plugin so that
 * you can tell Xena of the encoding within a database field. However there are
 * a lot of potential future uses. For example, the ability to re-process
 * existing binary wrapped objects could be assisted by the use of decoders. A
 * special decoder for sucking out the binary stream for Xena wrapped  files would
 * be written and somehow the core of Xena would be changed to be able to use
 * the decoder to get the raw data into the normal channels.
 *
 * One could think of decoders as an intermediatory normalisation step. One could
 * envisage a scenario where data went through several decoders before being
 * put into a normaliser. The fundamental difference between a decoder and a
 * normaliser is that a decoder's input and output is binary, whereas a
 * normaliser's output is always XML.
 *
 * @author     Chris Bitmead
 * @created    May 29, 2002
 */
public class DecoderManager implements LoadManager {

//    static DecoderManager theSingleton = new DecoderManager();
//    /**
//     * @return    singleton implementation for DecoderManager Class
//     */
//    public static DecoderManager singleton() {
//        return theSingleton;
//    }

    private PluginManager pluginManager;
    
    public DecoderManager(PluginManager pluginManager){
        this.pluginManager = pluginManager;
    }
    
    
    
    protected List decoders = new ArrayList();

	/**
	 *  Constructer for manager
	 */
	public DecoderManager() {
	}


	public List getAllDecoders() {
		return decoders;
	}

	/**
	 *  Loads decoders listed in properties available at runtime
	 *
	 * @param  props                properties available at runtime
	 * @exception  XenaException
	 */
	public boolean load(JarPreferences props) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(props);
			List loaders = loader.loadInstances("decoders");
			decoders.addAll(loaders);
			return!loaders.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	/**
	 *  Iterator for available decoders
	 *
	 * @return    decoder Iterator
	 */
	public Iterator iterator() {
		return decoders.iterator();
	}

	public void complete() {
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
}
