package au.gov.naa.digipres.xena.plugin.office;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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

	public ConfigOpenOffice() {
	}

	public static void main(String[] args) {
		ConfigOpenOffice conf = new ConfigOpenOffice();
		if (1 < args.length) {
			System.err.println("usage: ConfigOpenOffice [pathname]");
			System.exit(1);
		} else if (1 == args.length) {
			conf.installDir = new File(args[0]);
			conf.modify();
		} else {
			conf.run();
		}
		System.exit(0);
	}

	public void run() {
		try {
			selectDir();
		} catch (Exception ex) {
			return;
		}
		modify();
	}

	public void modify() {
		try {
			modifySetup();
			modifyTypeDetection();
			JOptionPane.showMessageDialog(null,
										  "OpenOffice.org has now been configured. If OpenOffice.org or its Quickstarter are running, restart before using Xena.",
										  "Complete", JOptionPane.INFORMATION_MESSAGE);
		} catch (AlreadyDoneException ex) {
			JOptionPane.showMessageDialog(null, ALREADY_DONE, "Already Configured", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.toString(), ex.toString(), JOptionPane.ERROR_MESSAGE);
		}
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
	public void modifyTypeDetection() throws IOException, AlreadyDoneException {
		String fileName = installDir + "/share/registry/data/org/openoffice/Office/TypeDetection.xcu";
		File file = new File(fileName);
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		File tmpFile = new File(fileName + ".tmp");
		FileWriter writer = new FileWriter(tmpFile);
		String line;
		List<String> docBookLines = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				if (line.matches(".*<node.*oor:name=\"Types\".*")) {
					writer.write(line);
					writer.write("\n");
					writer.write("  <node oor:name=\"calc_Flat_XML_File\" oor:op=\"replace\">\n");
					writer.write("   <prop oor:name=\"UIName\">\n");
					writer.write("    <value xml:lang=\"en-US\">Flat Xml (Calc)</value>\n");
					writer.write("   </prop>\n");
					writer.write("   <prop oor:name=\"Data\">\n");
					writer.write("    <value>1,,,," + OooView.CALC_EXT + ",20002,</value>\n");
					writer.write("   </prop>\n");
					writer.write("  </node>\n");
					writer.write("  <node oor:name=\"impress_Flat_XML_File\" oor:op=\"replace\">\n");
					writer.write("   <prop oor:name=\"UIName\">\n");
					writer.write("    <value xml:lang=\"en-US\">Flat Xml (Impress)</value>\n");
					writer.write("   </prop>\n");
					writer.write("   <prop oor:name=\"Data\">\n");
					writer.write("    <value>1,,,," + OooView.IMPRESS_EXT + ",20002,</value>\n");
					writer.write("   </prop>\n");
					writer.write("  </node> \n");
				} else if (line.matches(".*<node.*oor:name=\"Filters\".*")) {
					writer.write(line);
					writer.write("\n");
					writer.write("  <node oor:name=\"Flat XML File (Calc)\" oor:op=\"replace\">\n");
					writer.write("   <prop oor:name=\"Installed\">\n");
					writer.write("    <value>true</value>\n");
					writer.write("   </prop>\n");
					writer.write("   <prop oor:name=\"UIName\">\n");
					writer.write("    <value xml:lang=\"en-US\">Flat XML (Calc)</value>\n");
					writer.write("   </prop>\n");
					writer.write("   <prop oor:name=\"Data\">\n");
					writer.write("    <value>0,calc_Flat_XML_File,com.sun.star.sheet.SpreadsheetDocument,com.sun.star.comp.Writer.XmlFilterAdaptor,524355,com.sun.star.documentconversion.XFlatXml;;com.sun.star.comp.Calc.XMLImporter;com.sun.star.comp.Calc.XMLExporter,0,,</value>\n");
					writer.write("   </prop>\n");
					writer.write("  </node>\n");
					writer.write("  <node oor:name=\"Flat XML File (Impress)\" oor:op=\"replace\">\n");
					writer.write("   <prop oor:name=\"Installed\">\n");
					writer.write("    <value>true</value>\n");
					writer.write("   </prop>\n");
					writer.write("   <prop oor:name=\"UIName\">\n");
					writer.write("    <value xml:lang=\"en-US\">Flat XML (Impress)</value>\n");
					writer.write("   </prop>\n");
					writer.write("   <prop oor:name=\"Data\">\n");
					writer.write("    <value>0,impress_Flat_XML_File,com.sun.star.presentation.PresentationDocument,com.sun.star.comp.Writer.XmlFilterAdaptor,524355,com.sun.star.documentconversion.XFlatXml;;com.sun.star.comp.Impress.XMLImporter;com.sun.star.comp.Impress.XMLExporter,0,,</value>\n");
					writer.write("   </prop>\n");
					writer.write("  </node>\n");
				} else if (line.matches(".*<node.*oor:name=\"writer_Flat_XML_File\".*")) {
					writer.write(line);
					writer.write("\n");
					while (!((line = br.readLine()).matches(".*</node>.*"))) {
						if (line.matches(".*<value>.*,xml,.*</value>")) {
							writer.write("    <value>1,,doctype:office:document,," + OooView.WRITER_EXT + ",20002,</value>");
						} else {
							writer.write(line);
						}
						writer.write("\n");
					}
					writer.write(line);
					writer.write("\n");
				} else {
					if (line.matches(".*<node.*oor:name=\"writer_DocBook_File\".*")) {
						docBookLines.add(line);
						while (!((line = br.readLine()).matches(".*</node>.*"))) {
							docBookLines.add(line);
						}
						docBookLines.add(line);
					} else {
						if (line.matches(".*oor:name=\"Flat XML File\".*")) {
							if (docBookLines.size() == 0) {
								tmpFile.delete();
								throw new AlreadyDoneException();
							}
							writer.write(line);
							writer.write("\n");
							writer.write("   <prop oor:name=\"Installed\" oor:type=\"xs:boolean\">\n");
							writer.write("    <value>true</value>\n");
							writer.write("   </prop>\n");
							while (!((line = br.readLine()).matches(".*</node>.*"))) {
								writer.write(line);
								writer.write("\n");
							}
							writer.write(line);
							writer.write("\n");
							for (String docBookLine : docBookLines)
							{
								writer.write(docBookLine + "\n");
							}
						} else {
							writer.write(line);
							writer.write("\n");
						}
					}
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

	public void setInstallDir(java.io.File installDir) {
		this.installDir = installDir;
	}

	public java.io.File getInstallDir() {
		return installDir;
	}

	public class AlreadyDoneException extends Exception {
	}
}
