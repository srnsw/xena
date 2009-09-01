
package com.softhub.ps.filter;

/**
 * Copyright 1998 by Christian Lehner.
 *
 * This file is part of ToastScript.
 *
 * ToastScript is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ToastScript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToastScript; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import com.softhub.ps.util.CharStream;
import java.io.IOException;

public class SubFileCodec extends AbstractCodec {

	private final static Class PARAMETERS[] = {Number.class, String.class};

	private int count;
	private char mark[];
	private char buffer[];
	private int low, high;

	public void open(CharStream stream, int mode) throws IOException {
		super.open(stream, mode);
		low = high = 0;
	}

	public int decode() throws IOException {
		if (low < high)
			return buffer[low++];
		do {
			low = high = 0;
			int c;
			while ((c = stream.getchar()) >= 0) {
				buffer[high] = (char) c;
				if (c != mark[high++])
					return buffer[low++];
				if (high >= buffer.length) {
					if (--count < 0) {
						endOfData = true;
					}
					break;
				}
			}
		} while (count >= 0);
		return EOD;
	}

	public void encode(int c) throws IOException {
		throw new IOException("SubFileCodec.encode not yet implemented");
	}

	public Class[] getOptionalParameterTypes() {
		return PARAMETERS;
	}

	public void setOptionalParameters(Object array[]) {
		count = ((Number) array[0]).intValue();
		mark = ((String) array[1]).toCharArray();
		buffer = new char[mark.length];
	}

}
