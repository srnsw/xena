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
 */

package au.gov.naa.digipres.xena.plugin.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.naa.digipres.xena.kernel.batchfilter.BatchFilter;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * @author Justin Waddell
 *
 */
public class EmailPlugin extends XenaPlugin {

	public static final String EMAIL_PLUGIN_NAME = "email";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return EMAIL_PLUGIN_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public List<BatchFilter> getBatchFilters() {
		List<BatchFilter> filterList = new ArrayList<BatchFilter>();
		filterList.add(new TrimBatchFilter());
		return filterList;
	}

	@Override
	public List<Guesser> getGuessers() {
		List<Guesser> guesserList = new ArrayList<Guesser>();
		guesserList.add(new TrimGuesser());
		guesserList.add(new MboxGuesser());
		guesserList.add(new PstGuesser());
		guesserList.add(new MsgGuesser());
		return guesserList;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// Normaliser
		EmailToXenaEmailNormaliser normaliser = new EmailToXenaEmailNormaliser();
		Set<Type> normaliserSet = new HashSet<Type>();
		normaliserSet.add(new TrimFileType());
		normaliserSet.add(new MboxFileType());
		normaliserSet.add(new ImapType());
		normaliserSet.add(new XenaMailboxFileType());
		normaliserSet.add(new PstFileType());
		normaliserSet.add(new MboxDirFileType());
		normaliserSet.add(new MsgFileType());
		inputMap.put(normaliser, normaliserSet);

		// Message denormaliser
		EmailDeNormaliser messageDenormaliser = new EmailDeNormaliser();
		Set<Type> messageDenormaliserSet = new HashSet<Type>();
		messageDenormaliserSet.add(new XenaEmailFileType());
		inputMap.put(messageDenormaliser, messageDenormaliserSet);

		// Mailbox denormaliser
		MailboxDeNormaliser mailboxDenormaliser = new MailboxDeNormaliser();
		Set<Type> mailboxDenormaliserSet = new HashSet<Type>();
		mailboxDenormaliserSet.add(new XenaMailboxFileType());
		inputMap.put(mailboxDenormaliser, mailboxDenormaliserSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// Normaliser
		EmailToXenaEmailNormaliser normaliser = new EmailToXenaEmailNormaliser();
		Set<Type> normaliserSet = new HashSet<Type>();
		normaliserSet.add(new XenaEmailFileType());
		outputMap.put(normaliser, normaliserSet);

		// Message denormaliser
		EmailDeNormaliser messageDenormaliser = new EmailDeNormaliser();
		Set<Type> messageDenormaliserSet = new HashSet<Type>();
		messageDenormaliserSet.add(new EmailXmlType());
		outputMap.put(messageDenormaliser, messageDenormaliserSet);

		// Mailbox denormaliser
		MailboxDeNormaliser mailboxDenormaliser = new MailboxDeNormaliser();
		Set<Type> mailboxDenormaliserSet = new HashSet<Type>();
		mailboxDenormaliserSet.add(new XenaMailboxFileType());
		outputMap.put(mailboxDenormaliser, mailboxDenormaliserSet);

		return outputMap;
	}

	@Override
	public List<PluginProperties> getPluginPropertiesList() {
		List<PluginProperties> propertiesList = new ArrayList<PluginProperties>();
		propertiesList.add(new EmailProperties());
		return propertiesList;
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();

		typeList.add(new TrimFileType());
		typeList.add(new MboxFileType());
		typeList.add(new ImapType());
		typeList.add(new XenaMailboxFileType());
		typeList.add(new PstFileType());
		typeList.add(new MboxDirFileType());
		typeList.add(new MsgFileType());
		typeList.add(new XenaEmailFileType());
		typeList.add(new XenaMailboxFileType());
		typeList.add(new EmailXmlType());

		return typeList;
	}

	@Override
	public List<XenaView> getViews() {
		List<XenaView> viewList = new ArrayList<XenaView>();
		viewList.add(new EmailView());
		viewList.add(new MailboxView());
		return viewList;
	}

}
