
package com.softhub.ts;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyProfile implements Profile {

	private Properties properties;

    public PropertyProfile() {
		this(new Properties());
    }

    public PropertyProfile(Properties properties) {
		this.properties = properties;
    }

    public PropertyProfile(File file)
		throws FileNotFoundException, IOException
	{
		this(new Properties());
		properties.load(new FileInputStream(file));
    }

    public PropertyProfile(String path)
		throws FileNotFoundException, IOException
	{
		this(new File(path));
    }

	public void save(File file, String title)
		throws FileNotFoundException, IOException
	{
		properties.store(new FileOutputStream(file), title);
	}

	public void save(String path, String title)
		throws FileNotFoundException, IOException
	{
		save(new File(path), title);
	}

	public void setString(String key, String value) {
		properties.setProperty(key, value);
	}

	public String getString(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public void setInteger(String key, int value) {
		properties.setProperty(key, String.valueOf(value));
	}

	public int getInteger(String key, int defaultValue) {
		int result = defaultValue;
		String s = properties.getProperty(key);
		if (s != null) {
			result = Integer.parseInt(s);
		}
		return result;
	}

	public void setFloat(String key, float value) {
		properties.setProperty(key, String.valueOf(value));
	}

	public float getFloat(String key, float defaultValue) {
		float result = defaultValue;
		String s = properties.getProperty(key);
		if (s != null) {
			result = Float.valueOf(s).floatValue();
		}
		return result;
	}

}
