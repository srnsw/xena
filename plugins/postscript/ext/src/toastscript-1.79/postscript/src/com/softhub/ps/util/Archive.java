
package com.softhub.ps.util;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Archive {

	private String name;
	private String path;
	private ZipFile zipfile;

	public Archive(String name) throws IOException {
		this.name = name;
		int i, j = 0, n = name.length();
		for (i = 0; i < n; i++) {
			char ch = name.charAt(i);
			if (ch == File.separatorChar || ch == '/') {
				if (i > j) {
					String s = name.substring(j, i);
					if (zipfile == null) {
						if (s.endsWith(".zip")) {
							zipfile = new ZipFile(name.substring(0, i));
						} else if (s.endsWith(".jar")) {
							zipfile = new JarFile(name.substring(0, i));
						}
						if (zipfile != null && i < n) {
							path = name.substring(i+1, n);
							break;
						}
					}
				}
				j = i+1;
			}
		}
		if (zipfile == null || path == null) {
			throw new IOException();
		}
	}

	public InputStream openStream() throws IOException {
		ZipEntry entry = zipfile.getEntry(path);
		if (entry == null)
			throw new IOException(path + " not found in " + name);
		return zipfile.getInputStream(entry);
	}

}
