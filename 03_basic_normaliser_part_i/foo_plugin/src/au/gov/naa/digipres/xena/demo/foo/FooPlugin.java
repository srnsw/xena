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
 */

package au.gov.naa.digipres.xena.demo.foo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class FooPlugin extends XenaPlugin {

	public static final String FOO_PLUGIN_NAME = "foo";

	@Override
	public String getName() {
		return FOO_PLUGIN_NAME;
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public List<Type> getTypes() {
		List<Type> typeList = new ArrayList<Type>();
		typeList.add(new FooFileType());
		return typeList;
	}

	@Override
	public List<Guesser> getGuessers() {
		List<Guesser> guesserList = new ArrayList<Guesser>();
		guesserList.add(new FooGuesser());
		return guesserList;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserInputMap() {
		Map<Object, Set<Type>> inputMap = new HashMap<Object, Set<Type>>();

		// Normaliser
		FooNormaliser normaliser = new FooNormaliser();
		Set<Type> normaliserSet = new HashSet<Type>();
		normaliserSet.add(new FooFileType());
		inputMap.put(normaliser, normaliserSet);

		return inputMap;
	}

	@Override
	public Map<Object, Set<Type>> getNormaliserOutputMap() {
		Map<Object, Set<Type>> outputMap = new HashMap<Object, Set<Type>>();

		// Normaliser
		FooNormaliser normaliser = new FooNormaliser();
		Set<Type> normaliserSet = new HashSet<Type>();
		normaliserSet.add(new XenaFooFileType());
		outputMap.put(normaliser, normaliserSet);

		return outputMap;
	}

}
