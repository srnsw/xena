/**
 * This file is part of naa.
 * 
 * naa is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * naa is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with naa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.naa;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;

/**
 * @author Justin Waddell
 *
 */
public class NaaTextAIPWrapper extends AbstractMetaDataWrapper {

	public static final String NAA_TEXT_AIP_WRAPPER_NAME = "NAA Text AIP Wrapper";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper#getName()
	 */
	@Override
	public String getName() {
		return NAA_TEXT_AIP_WRAPPER_NAME;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper#getOpeningTag()
	 */
	@Override
	public String getOpeningTag() {
		return "";
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper#getSourceId(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getSourceId(XenaInputSource input) throws XenaException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper#getSourceName(au.gov.naa.digipres.xena.kernel.XenaInputSource)
	 */
	@Override
	public String getSourceName(XenaInputSource input) throws XenaException {
		// TODO Auto-generated method stub
		return null;
	}

}
