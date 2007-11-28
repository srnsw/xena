package au.gov.naa.digipres.xena.demo.orgx.test;

import java.util.Vector;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class PluginLoadTester {

	public static void main(String[] argv) {
		Xena xena = new Xena();
		// our foo jar will already be on the class path, so load it by name...
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au.gov.naa.digipres.xena.demo.foo.FooPlugin");

		try {
			xena.loadPlugins(pluginList);
		} catch (XenaException e) {
			System.out.println("Could not load xena plugins:" + e);
			e.printStackTrace();
		}

		System.out.println("Types...");
		for (Type newType : xena.getPluginManager().getTypeManager().allTypes()) {
			System.out.println(newType.toString());
		}
		System.out.println("----------------------------->>>><<<<<--------------------");

		System.out.println("Guessers...");
		for (Guesser newGuesser : xena.getPluginManager().getGuesserManager().getGuessers()) {
			System.out.println(newGuesser.getName());
		}
		System.out.println("---------------------------->>>><<<<<--------------------");

		System.out.println("Normalisers...");
		for (AbstractNormaliser normaliser : xena.getPluginManager().getNormaliserManager().getAllNormalisers()) {
			System.out.println(normaliser.getName());
		}
		System.out.println("---------------------------->>>><<<<<--------------------");
	}

}
