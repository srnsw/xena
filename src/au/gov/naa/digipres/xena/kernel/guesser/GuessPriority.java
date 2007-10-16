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
 * @author Dan Spasojevic
 * @author Justin Waddell
 */

/*
 * Created on 11/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public class GuessPriority implements Comparable {

	@Override
    public int hashCode() {
		return value;
	}

	public static GuessPriority LOW = new GuessPriority("LOW", 0);
	public static GuessPriority DEFAULT = new GuessPriority("Default", 1);
	public static GuessPriority HIGH = new GuessPriority("High", 2);

	private int value;
	private String name;

	private GuessPriority(String name, int value) {
		this.name = name;
		this.value = value;
	}

	@Override
    public String toString() {
		return name + " (id: " + value + ")";
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	@Override
    public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GuessPriority)) {
			return false;
		}
		GuessPriority otherOne = (GuessPriority) o;
		if (this.value == otherOne.getValue()) {
			return true;
		}
		return false;
	}

	public int compareTo(Object o) {
		if (o instanceof GuessPriority) {
			GuessPriority otherPriority = (GuessPriority) o;
			if (this.value < otherPriority.getValue()) {
				return -1;
			}
			if (this.value > otherPriority.getValue()) {
				return 1;
			}
			return 0;
		}
		throw new IllegalArgumentException();
	}

}
