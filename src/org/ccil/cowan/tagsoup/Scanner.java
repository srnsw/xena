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
// Scanner

package org.ccil.cowan.tagsoup;
import java.io.IOException;
import java.io.Reader;
import org.xml.sax.SAXException;

/**
An interface allowing Parser to invoke scanners.
**/

public interface Scanner {

	/**
	Invoke a scanner.
	@param r A source of characters to scan
	@param h A ScanHandler to report events to
	**/

	public void scan(Reader r, ScanHandler h) throws IOException, SAXException;

	/**
	Reset the embedded locator.
	@param publicid The publicid of the source
	@param systemid The systemid of the source
	**/

	public void resetDocumentLocator(String publicid, String systemid);

	/**
	Signal to the scanner to start CDATA content mode.
	**/

	public void startCDATA();

	}
