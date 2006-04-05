package au.gov.naa.digipres.xena.plugin.office;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;

/**
 * Make necessary changes so that OpenOffice.org 1.1.4 can work with Xena.
 *
 * The two changes we need are:
 *
 * 1) Aff flat XML high in the priority list in the TypeDetection config file so that they
 * will open properly when the OooView tries to write and open an OOo file.
 * 2) Enable OOo's socket facility in Setup so that we can talk to OOo and get it to do
 * stuff over sockets.
 *
 * @author Chris Bitmead
 */
public class ConfigOpenOffice {

	static final String ALREADY_DONE = "OpenOffice.org has already been configured for use with Xena.";

	public static final String SETUP = "/share/registry/data/org/openoffice/Setup.xcu";

	private java.io.File installDir;

	public ConfigOpenOffice() 
	{
		
	}

	public void modify() throws IOException, AlreadyDoneException, PropertyMessageException {
		modifySetup();
		modifyTypeDetection();
		throw new PropertyMessageException("OpenOffice.org has now been configured. " +
		                                   "If OpenOffice.org or its Quickstarter are running, restart before using Xena.");
	}

	public void selectDir() {
		JFileChooser fc = new javax.swing.JFileChooser();
		fc.setCurrentDirectory(installDir);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showDialog(null, "Select OpenOffice.org Install Directory") != JFileChooser.APPROVE_OPTION) {
			throw new RuntimeException("Cancelled");
		}
		installDir = fc.getSelectedFile();
	}

	/**
	 *  We need to add some config lines to Setup.xcu in order to get OpenOffice to
	 *  listen to commands on a socket.
	 *
	 * @throws  IOException
	 */
	public void modifySetup() throws IOException, AlreadyDoneException {
		String fileName = installDir + SETUP;
		File file = new File(fileName);
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		File tmpFile = new File(fileName + ".tmp");
		FileWriter writer = new FileWriter(tmpFile);
		String line;
		try {
			while ((line = br.readLine()) != null) {
				writer.write(line);
				writer.write("\n");
				if (line.matches(".*oor:name=\"Office\".*")) {
					line = br.readLine();
					if (line.matches(".*oor:name=\"ooSetupConnectionURL\".*")) {
						tmpFile.delete();
						throw new AlreadyDoneException();
					}
					writer.write("  <prop oor:name=\"ooSetupConnectionURL\">\n");
					writer.write("    <value>socket,host=0,port=8100;urp;StarOffice.ServiceManager</value>\n");
					writer.write("  </prop>\n");
					writer.write(line);
					writer.write("\n");
				}
			}
			writer.close();
			fr.close();
			file.delete();
			tmpFile.renameTo(file);
		} finally {
			writer.close();
			br.close();
			fr.close();
			tmpFile.delete();
		}
	}
	
	/**
	 *  We need to modify TypeDetection.xcu to put the DocBook type after the
	 *  FlatXml type. Otherwise Openoffice doesn't open flatXml properly. A nicer
	 *  programmatic solution would be preferable, but I am not aware of one.
	 *
	 * @throws  IOException
	 */
	public void modifyTypeDetection() throws IOException, AlreadyDoneException 
	{
		// Don't think this is needed any more...
	}

	public void setInstallDir(java.io.File installDir) {
		this.installDir = installDir;
	}

	public java.io.File getInstallDir() {
		return installDir;
	}

	public class AlreadyDoneException extends Exception {
	}
}
