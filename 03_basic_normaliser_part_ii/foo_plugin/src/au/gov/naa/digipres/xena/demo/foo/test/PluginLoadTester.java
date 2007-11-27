package au.gov.naa.digipres.xena.demo.foo.test;

import java.util.Vector;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class PluginLoadTester {

	public static void main(String[] argv) throws XenaException {
		Xena xena = new Xena();
		// our foo jar will already be on the class path, so load it by name...
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au/gov/naa/digipres/xena/demo/foo");

		xena.loadPlugins(pluginList);

		System.out.println("Types...");
		for (Object element : xena.getPluginManager().getTypeManager().allTypes()) {
			Type newType = (Type) element;
			System.out.println(newType.toString());
		}
		System.out.println("----------------------------->>>><<<<<--------------------");

		System.out.println("Guessers...");
		for (Object element : xena.getPluginManager().getGuesserManager().getGuessers()) {
			Guesser newGuesser = (Guesser) element;
			System.out.println(newGuesser.getName());
		}
		System.out.println("---------------------------->>>><<<<<--------------------");

		System.out.println("Normalisers...");
		for (Object element : xena.getPluginManager().getNormaliserManager().getAll()) {
			AbstractNormaliser normaliser = (AbstractNormaliser) element;
			System.out.println(normaliser.getName());
		}
		System.out.println("---------------------------->>>><<<<<--------------------");
	}

}
