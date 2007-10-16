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

package au.gov.naa.digipres.xena.kernel.decoder;

import java.io.IOException;

import org.jdom.Element;

import au.gov.naa.digipres.xena.kernel.ToXml;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XmlSerializable;

/**
 *  Super-class for decoders. Allows a source to be decoded into another source
 *  typically Ascii.
 *
 * @see xena.kernel.DecoderManager
 * @created    2 July 2002
 */

public abstract class Decoder implements XmlSerializable {
	/**
	 * @return    The name of the decoder
	 */
	public abstract String getName();

	/**
	 * @param  source           Source to be decoded
	 * @return                  decoded Source
	 */
	public abstract XenaInputSource decode(XenaInputSource source) throws IOException;

	@Override
    public String toString() {
		return getName();
	}

	public Element toXml() {
		return ToXml.toXmlBasic(this);
	}

	public void fromXml(Element element) {
		// Nothing
	}
}
