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

package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.batchfilter.BatchFilter;
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
 */
public class TrimBatchFilter extends BatchFilter {
	Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public Map<String, FileAndType> filter(Map<String, FileAndType> files) throws XenaException {
		Set<String> removeList = new HashSet<String>();
		for (FileAndType fileAndType : files.values()) {
			if (fileAndType.getType() instanceof TrimFileType && fileAndType.getNormaliser() instanceof EmailToXenaEmailNormaliser) {
				try {
					TrimMessage message = new TrimMessage(fileAndType.getFile());
					for (TrimAttachment messageAttachment : message.getAttachments()) {
						removeList.add(messageAttachment.getFile().getName());
					}
				} catch (MessagingException x) {
					throw new XenaException(x);
				}
			}
		}
		for (String fileToRemove : removeList) {
			files.remove(fileToRemove);
		}
		return files;
	}

	@Override
	public Map<XenaInputSource, NormaliserResults> getChildren(Collection<XenaInputSource> xisColl) {
		Map<XenaInputSource, NormaliserResults> childMap = new HashMap<XenaInputSource, NormaliserResults>();
		for (XenaInputSource xis : xisColl) {
			if (xis.getType() instanceof TrimFileType) {
				File file = xis.getFile();
				if (file != null && file.exists()) {
					try {
						TrimMessage tm = new TrimMessage(file);
						List<TrimAttachment> attachList = tm.getAttachments();
						for (TrimAttachment attachment : attachList) {
							File attachFile = attachment.getFile();

							XenaInputSource childXis = new XenaInputSource(attachFile);

							// Create NormaliserResults object for the child,
							// so we have a link to the correct parent
							NormaliserResults results = new NormaliserResults(childXis);
							results.setChild(true);
							results.setParentSystemId(xis.getSystemId());
							results.setInputSystemId(attachFile.toURI().toASCIIString());
							Type trimType = new TrimAttachmentType();
							results.setInputType(trimType);
							childXis.setType(trimType);

							childMap.put(childXis, results);
						}
					} catch (Exception e) {
						// Just log exceptions so the normalisation process may continue
						logger.log(Level.FINER, "Problem checking for TRIM children for file " + file.getName(), e);
					}
				}
			}
		}
		return childMap;
	}

	@Override
	public String getName() {
		return "Trim Batch Filter";
	}

}
