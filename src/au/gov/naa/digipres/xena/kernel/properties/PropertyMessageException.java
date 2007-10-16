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

/*
 * Created on 3/04/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

/**
 * Indicates that an operation on a plugin property has triggered a message
 * which should be displayed, logged etc by the calling application.
 * 
 * created 3/04/2006
 * xena
 * Short desc of class:
 */
public class PropertyMessageException extends Exception {
	/**
	 * @param message
	 */
	public PropertyMessageException(String message) {
		super(message);
	}
}
