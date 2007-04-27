// This file is part of TagSoup.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.  You may also distribute
// and/or modify it under version 2.1 of the Academic Free License.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// 
// 
// Interface to objects that translate InputStreams to Readers by auto-detection

package org.ccil.cowan.tagsoup;
import java.io.Reader;
import java.io.InputStream;

/**
Classes which accept an InputStream and provide a Reader which figures
out the encoding of the InputStream and reads characters from it should
conform to this interface.
@see java.io.InputStream
@see java.io.Reader
*/

public interface AutoDetector {

	/**
	Given an InputStream, return a suitable Reader that understands
	the presumed character encoding of that InputStream.
	If bytes are consumed from the InputStream in the process, they
	<i>must</i> be pushed back onto the InputStream so that they can be
	reinterpreted as characters.
	@param i The InputStream
	@return A Reader that reads from the InputStream
	*/

	public Reader autoDetectingReader(InputStream i);

	}
