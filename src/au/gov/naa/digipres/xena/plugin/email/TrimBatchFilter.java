package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import au.gov.naa.digipres.xena.kernel.BatchFilter;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.email.trim.TrimAttachment;
import au.gov.naa.digipres.xena.plugin.email.trim.TrimAttachmentType;
import au.gov.naa.digipres.xena.plugin.email.trim.TrimMessage;

/**
 * Filters out the Trim mail attachments. When normalising a batch of files
 * exported from TRIM, some of the files are not stand-alone but are actually
 * attachments for the email .mbx files. We filter these out of consideration
 * because they will be bundled into the Xena email file.
 *
 * @author Chris Bitmead
 */
public class TrimBatchFilter extends BatchFilter 
{
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	
	public Map filter(Map files) throws XenaException 
	{
		Iterator it = files.entrySet().iterator();
		Set<String> removeList = new HashSet<String>();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry)it.next();
			BatchFilter.FileAndType fat = (BatchFilter.FileAndType)ent.getValue();
			if (fat.getType() instanceof TrimFileType && fat.getNormaliser() instanceof EmailToXenaEmailNormaliser) {
				try {
					TrimMessage tm = new TrimMessage(fat.getFile());
					Iterator it2 = tm.getAttachments().iterator();
					while (it2.hasNext()) {
						TrimAttachment ta = (TrimAttachment)it2.next();
//						it.remove();
						removeList.add(ta.getFile().getName());
//						System.out.println("done: " + done + " " + ta.getFileName());
					}
				} catch (MessagingException x) {
					throw new XenaException(x);
				}
			}
		}
		for (String str : removeList)
		{
			files.remove(str);
		}
		return files;
	}

	@Override
	public Map<XenaInputSource, NormaliserResults> 
		getChildren(Collection<XenaInputSource> xisColl) 
		throws XenaException
	{
		Map<XenaInputSource, NormaliserResults> childMap = 
			new HashMap<XenaInputSource, NormaliserResults>();
		for (XenaInputSource xis : xisColl)
		{
			if (xis.getType() instanceof TrimFileType)
			{
				File file = xis.getFile();
				if (file != null && file.exists())
				{
					try
					{
						TrimMessage tm = new TrimMessage(file);
						List<TrimAttachment> attachList = tm.getAttachments();
						for (TrimAttachment attachment : attachList)
						{
							File attachFile = attachment.getFile();
							
							XenaInputSource childXis = 
								new XenaInputSource(attachFile);
							
							// Create NormaliserResults object for the child,
							// so we have a link to the correct parent
							NormaliserResults results =
								new NormaliserResults(childXis);
							results.setChild(true);
							results.setParentSystemId(xis.getSystemId());
							results.setInputSystemId(attachFile.toURI().toASCIIString());
							Type trimType = new TrimAttachmentType();
							results.setInputType(trimType);
							childXis.setType(trimType);
							
							childMap.put(childXis, results);
						}
					}
					catch (Exception e)
					{
						// Just log exceptions so the normalisation process may continue
						logger.log(Level.FINER, 
						           "Problem checking for TRIM children for file " 
						           + file.getName(),
						           e);
					}
				}
			}
		}
		return childMap;
	}

	@Override
	public String getName()
	{
		return "Trim Batch Filter";
	}
	
}
