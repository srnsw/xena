
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

class ImageDecoder implements GrayPixelSource {

	private ImageDataProducer producer;
	private Object procedure;
	private CharStream src;
	private int bits;
	private int scale;
	private int shift;
	private int mirror;
	private int currentByte;
	private int bitIndex;
	private int bitMask;
	private int pixelsPerByte;

	ImageDecoder(ImageDataProducer producer, Object procedure, int bits) {
		this(producer.getImageData(procedure), bits);
		this.producer = producer;
		this.procedure = procedure;
	}

	ImageDecoder(CharStream src, int bits) {
		this.src = src;
		this.bits = bits;
		pixelsPerByte = 8 / bits;
		shift = bitsToShift(bits);
		mirror = 8 - bits;
		scale = (1 << shift) - 1;
		bitMask = ((1 << bits) - 1) << mirror;
	}

	public int nextPixel() throws IOException {
		if (bitIndex == 0) {
			currentByte = next();
		}
		if (currentByte < 0)
			return -1;
		int val = ((currentByte & (bitMask >> bitIndex)) >> (mirror-bitIndex)) * scale;
		bitIndex += bits;
		if (bitIndex >= 8) {
			bitIndex = 0;
		}
		return val;
	}

	protected int next() throws IOException {
		int c = src.getchar();
		if (c < 0) {
			if (producer != null) {
				src = producer.getImageData(procedure);
				c = src.getchar();
			}
		}
		return c;
	}

	private static int bitsToShift(int bits) {
		switch (bits) {
		case 1:
			return 8;
		case 2:
			return 6;
		case 4:
			return 4;
		case 8:
			return 1;
		default:
			throw new IllegalArgumentException("bits: " + bits);
		}
	}

}
