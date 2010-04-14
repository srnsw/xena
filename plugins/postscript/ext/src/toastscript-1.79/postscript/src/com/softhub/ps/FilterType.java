
package com.softhub.ps;

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

import com.softhub.ps.filter.Codec;
import java.io.IOException;

public class FilterType extends FileType {

	/**
	 * Construct a filter object.
	 */
	public FilterType(VM vm, CharSequenceType stream, Codec codec, int mode) {
		super(vm, createNode(vm, stream, codec, mode));
		try {
		    codec.open(stream, mode);
		} catch (IOException ex) {
			throw new Stop(IOERROR, ex.toString());
		}
	}

	public CharSequenceType getSourceStream() {
		if (node instanceof ReadFilterNode)
		    return ((ReadFilterNode) node).stream;
		if (node instanceof WriteFilterNode)
		    return ((WriteFilterNode) node).stream;
		throw new Stop(INTERNALERROR);
	}

	private static FileNode createNode(VM vm, CharSequenceType stream, Codec codec, int mode) {
		switch (mode) {
		case READ_MODE:
			return new ReadFilterNode(vm, stream, codec);
		case WRITE_MODE:
			return new WriteFilterNode(vm, stream, codec);
		default:
			throw new Stop(INTERNALERROR);
		}
	}

	static class ReadFilterNode extends ReadFileNode {

		/**
		 * The stream we are reading from.
		 */
		private CharSequenceType stream;

		/**
		 * The encoder/decoder.
		 */
		private Codec codec;

		ReadFilterNode(VM vm, CharSequenceType stream, Codec codec) {
			super(vm, "filter");
			this.stream = stream;
			this.codec = codec;
		}

		protected int rawRead() throws IOException {
			return codec.decode();
		}

		protected void rawWrite(int c) throws IOException {
			throw new IOException();
		}

		protected void close() throws IOException {
		    codec.close();
			closed = true;
		}

	}

	static class WriteFilterNode extends WriteFileNode {

		/**
		 * The stream we are reading from.
		 */
		private CharSequenceType stream;

		/**
		 * The encoder/decoder.
		 */
		private Codec codec;

		WriteFilterNode(VM vm, CharSequenceType stream, Codec codec) {
			super(vm, "filter");
			this.stream = stream;
			this.codec = codec;
		}

		protected int rawRead() throws IOException {
			throw new IOException();
		}

		protected void rawWrite(int c) throws IOException {
			codec.encode(c);
		}

		protected void close() throws IOException {
		    codec.close();
			closed = true;
		}

	}

}
