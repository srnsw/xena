package au.gov.naa.digipres.xena.demo.orgx.test;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.demo.orgx.DemoInfoProvider;
import au.gov.naa.digipres.xena.demo.orgx.InfoProvider;
import au.gov.naa.digipres.xena.demo.orgx.OrgXFileNamer;
import au.gov.naa.digipres.xena.demo.orgx.OrgXMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

public class NormaliseTester {
	public static void main(String[] argv) throws XenaException, IOException {
		Xena xena = new Xena();

		// our orgx jar will already be on the class path, so load it by name...
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au.gov.naa.digipres.xena.demo.orgx.OrgXPlugin");
		xena.loadPlugins(pluginList);

		// Ensure that the meta data wrapper and the file namer are using the same info provider
		// We have only loaded one of each, so they should both be active
		InfoProvider infoProvider = new DemoInfoProvider();
		AbstractMetaDataWrapper activeWrapper = xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
		if (activeWrapper instanceof OrgXMetaDataWrapper) {
			OrgXMetaDataWrapper orgXWrapper = (OrgXMetaDataWrapper) activeWrapper;
			orgXWrapper.setInfoProvider(infoProvider);
		}
		AbstractFileNamer activeNamer = xena.getPluginManager().getFileNamerManager().getActiveFileNamer();
		if (activeNamer instanceof OrgXFileNamer) {
			OrgXFileNamer orgXNamer = (OrgXFileNamer) activeNamer;
			orgXNamer.setInfoProvider(infoProvider);
		}

		// set the base path to be the current working directory
		String currentDir = System.getProperty("user.dir");
		xena.setBasePath(currentDir);
		System.out.println(System.getProperty("user.dir"));

		// create the new input source
		File f = new File("../../../data/example_file.foo");
		XenaInputSource xis = new XenaInputSource(f);

		// guess its type
		Guess fooGuess = xena.getBestGuess(xis);

		//print the guess...
		System.out.println("Here is the best guess returned by Xena: ");
		System.out.println(fooGuess.toString());
		System.out.println("-----------------------------------------");

		// normalise the file!
		NormaliserResults results = xena.normalise(xis, new File(currentDir), activeNamer, activeWrapper);
		System.out.println("Here are the results of the normalisation:");
		System.out.println(results.toString());
		System.out.println("-----------------------------------------");

		System.out.println("Meta data wrappers...");
		for (String metaDataWrapperName : xena.getPluginManager().getMetaDataWrapperManager().getMetaDataWrapperNames()) {
			//at this stage, we dont know if it is a denormaliser or normaliser...
			System.out.println(metaDataWrapperName);
		}

		System.out.println("Active wrapper:");
		System.out.println(xena.getPluginManager().getMetaDataWrapperManager().getActiveWrapperPlugin().getName());
		System.out.println("-----------------------------------------");

	}
}
