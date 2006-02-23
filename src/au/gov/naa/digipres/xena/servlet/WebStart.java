package au.gov.naa.digipres.xena.servlet;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.gov.naa.digipres.xena.javatools.Props;
import au.gov.naa.digipres.xena.kernel.PluginManager;

/**
 * A servlet that can be deployed in a servlet engine in order to call
 * webstart for Xena, automatically generating all the correct command line
 * arguments according to the plugins installed.
 *
 * @author Chris Bitmead
 */
public class WebStart extends HttpServlet {
	private final static String CONTENT_TYPE = "application/x-java-jnlp-file";

	/**
	 *  Initialize global variables
	 *
	 * @exception  ServletException  Description of Exception
	 */
	public void init() throws ServletException {
	}

	/**
	 *  Process the HTTP Get request
	 *
	 * @param  request               Description of Parameter
	 * @param  response              Description of Parameter
	 * @exception  ServletException  Description of Exception
	 * @exception  IOException       Description of Exception
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType(CONTENT_TYPE);
		Props p = Props.singleton("webstart");
		PrintWriter out = response.getWriter();
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		Element jnlp = new Element("jnlp");
		jnlp.setAttribute("spec", "1.0+");
		jnlp.setAttribute("codebase", p.getProperty("codebase", "http://chris.bitmead.com/xanadu/"));
//		jnlp.setAttribute("href", "xena.jnlp");
		Element information = new Element("information");
		jnlp.addContent(information);
		Element title = new Element("title");
		information.addContent(title);
		title.setText("Xena");
		Element vendor = new Element("vendor");
		information.addContent(vendor);
		vendor.setText("National Archive of Australia");
		Element homepage = new Element("homepage");
		information.addContent(homepage);
		homepage.setAttribute("href", "docs/help.html");
		Element description = new Element("description");
		information.addContent(description);
		description.setText("XML Application for Normalising, Archiving and Displaying Universally");
		Element icon = new Element("icon");
		information.addContent(icon);
		icon.setAttribute("href", "xena.jpg");
		Element offlineAllowed = new Element("offline-allowed");
		information.addContent(offlineAllowed);
		Element security = new Element("security");
		jnlp.addContent(security);
		Element allPermissions = new Element("all-permissions");
		security.addContent(allPermissions);
		Element resources = new Element("resources");
		jnlp.addContent(resources);
		Element j2se = new Element("j2se");
		resources.addContent(j2se);
		j2se.setAttribute("version", p.getProperty("j2se", "1.3"));
//		j2se.setAttribute("href", "http://java.sun.com/products/autodl/j2se");
		Element jar = new Element("jar");
		resources.addContent(jar);
		jar.setAttribute("href", "xena.jar");
		Element applicationDesc = new Element("application-desc");
		jnlp.addContent(applicationDesc);
		applicationDesc.setAttribute("main-class", "xena.gui.Main");
		String plugins = p.getProperty("pluginDir", "plugins");
		File pluginsDir = new File(plugins);
		File[] pluginFiles = pluginsDir.listFiles();
		for (int i = 0; i < pluginFiles.length; i++) {
			File pluginFile = pluginFiles[i];
			String pluginName = PluginManager.getPluginName(pluginFile);
			jar = new Element("jar");
			resources.addContent(jar);
			jar.setAttribute("href", plugins + "/" + pluginFile.getName());
			Element argument = new Element("argument");
			applicationDesc.addContent(argument);
			argument.setText("-p" + pluginName);
		}
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//		outputter.setIndent(true);
//		outputter.setNewlines(true);
//		outputter.setLineSeparator("\n");
		outputter.output(jnlp, out);
	}
}
