
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

public class PixelSourceFactory {

	public static GrayPixelSource createGrayPixelSource(
		ImageDataProducer ip, Object proc, int bits
	) {
		return new ImageDecoder(ip, proc, bits);
	}

	public static GrayPixelSource createGrayPixelSource(
		CharStream src, int bits
	) {
		return new ImageDecoder(src, bits);
	}

	public static RGBPixelSource createRGBPixelSource(
		ImageDataProducer ip, Object proc, int bits
	) {
		return new RGBSingleSourceDecoder(ip, proc, bits);
	}

	public static RGBPixelSource createRGBPixelSource(
		CharStream src, int bits
	) {
		return new RGBSingleSourceDecoder(src, bits);
	}

	public static RGBPixelSource createRGBPixelSource(
		ImageDataProducer ip, Object procs[/* 3 */], int bits
	) {
		return new RGBMultipleSourceDecoder(ip, procs, bits);
	}

	public static RGBPixelSource createRGBPixelSource(
		CharStream sources[], int bits
	) {
		return new RGBMultipleSourceDecoder(sources, bits);
	}

	public static CMYKPixelSource createCMYKPixelSource(
		ImageDataProducer ip, Object proc, int bits
	) {
		return new CMYKSingleSourceDecoder(ip, proc, bits);
	}

	public static CMYKPixelSource createCMYKPixelSource(
		CharStream src, int bits
	) {
		return new CMYKSingleSourceDecoder(src, bits);
	}

	public static CMYKPixelSource createCMYKPixelSource(
		ImageDataProducer ip, Object procs[/* 4 */], int bits
	) {
		return new CMYKMultipleSourceDecoder(ip, procs, bits);
	}

	public static CMYKPixelSource createCMYKPixelSource(
		CharStream sources[], int bits
	) {
		return new CMYKMultipleSourceDecoder(sources, bits);
	}

}
