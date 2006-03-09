package au.gov.naa.digipres.xena.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.javatools.PluginLoader;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Manages all the BatchFilters installed in the system.
 *
 * @see BatchFilter
 * @author Chris Bitmead
 */
public class BatchFilterManager implements LoadManager {
	protected List<BatchFilter> filters = new ArrayList<BatchFilter>();

	static BatchFilterManager theSingleton = new BatchFilterManager();
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public BatchFilterManager() {
	}

	public static BatchFilterManager singleton() {
		return theSingleton;
	}

	public boolean load(JarPreferences preferences) throws XenaException {
		try {
			PluginLoader loader = new PluginLoader(preferences);
			List transes = loader.loadInstances("batchFilters");
			Iterator it = transes.iterator();

			while (it.hasNext()) {
				BatchFilter filter = (BatchFilter)it.next();
				filters.add(filter);
			}
			return!transes.isEmpty();
		} catch (ClassNotFoundException e) {
			throw new XenaException(e);
		} catch (IllegalAccessException e) {
			throw new XenaException(e);
		} catch (InstantiationException e) {
			throw new XenaException(e);
		}
	}

	/**
	 * Apply all available filters to the input files to remove the files to be
	 * ignored.
	 * @param files list of files to process
	 * @return list of files after removing unnecessary ones
	 */
	public Map filter(Map files) throws XenaException {
		for (BatchFilter filter : filters)
		{
			files = filter.filter(files);
		}
		return files;
	}
	
	public Map<XenaInputSource, NormaliserResults> 
		getChildren(Collection<XenaInputSource> xisColl)
	{
		Map<XenaInputSource, NormaliserResults> childMap = 
			new HashMap<XenaInputSource, NormaliserResults>();
		for (BatchFilter filter : filters)
		{
			// Error in one batch filter should not stop the whole process -
			// Just log error
			try
			{
				childMap.putAll(filter.getChildren(xisColl));
			}
			catch (XenaException ex)
			{
				logger.log(Level.FINER, "Problem with batch filter " + filter,
				           ex);
			}
		}
		return childMap;
	}

	public void complete() {}
}
