
package com.softhub.ps.image;

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

class CMYKSingleSourceDecoder extends ImageDecoder implements CMYKPixelSource {

	private int state;

	CMYKSingleSourceDecoder(ImageDataProducer producer, Object proc, int bits) {
		super(producer, proc, bits);
	}

	CMYKSingleSourceDecoder(CharStream src, int bits) {
		super(src, bits);
	}

	public int nextCyanComponent() throws IOException {
		if ((state++ % 4) != 0)
			throw new IOException("illegal state");
		return nextPixel();
	}

	public int nextMagentaComponent() throws IOException {
		if ((state++ % 4) != 1)
			throw new IOException("illegal state");
		return nextPixel();
	}

	public int nextYellowComponent() throws IOException {
		if ((state++ % 4) != 2)
			throw new IOException("illegal state");
		return nextPixel();
	}

	public int nextBlackComponent() throws IOException {
		if ((state++ % 4) != 3)
			throw new IOException("illegal state");
		return nextPixel();
	}

}
