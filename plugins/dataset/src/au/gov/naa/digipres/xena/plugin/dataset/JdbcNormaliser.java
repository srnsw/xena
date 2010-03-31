package au.gov.naa.digipres.xena.plugin.dataset;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XmlList;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * Normaliser for creating a Xena dataset from a JDBC connection.
 *
 * @author Chris Bitmead
 */
public class JdbcNormaliser extends AbstractNormaliser {
	final static String DBURI = "http://preservation.naa.gov.au/database/1.0";

	final static String DBPREFIX = "database";

	final static String DSURI = "http://preservation.naa.gov.au/dataset/1.0";

	final static String DSPREFIX = "dataset";

	public String jar;

	public String driver;

	public String url;

	public String user;

	public String password;

	public XmlList queries;

	public String getName() {
		return "JDBC";
	}

	public void setQueries(List queries) {
		this.queries = new XmlList(queries);
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setJar(String jar) {
		this.jar = jar;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getDriver() {
		return this.driver;
	}

	public String getJar() {
		return this.jar;
	}

	public Connection getConnection() throws SAXException {
		Connection conn = null;
		try {
			/*			if (jar == null || jar.equals("")) { jar = "c:/cvs/external/postgresql.jar"; }
				 if (driver == null || driver.equals("")) {driver = "org.postgresql.Driver"; }
				 if (url == null || url.equals("")) { url = "jdbc:postgresql://tech.com.au/chris";	}
				 if (user == null || user.equals("")) { user = "chris"; }
				 if (password == null) {	password = ""; }
				if (jar == null || jar.equals("")) { jar = "c:/cvs/external/mysql.jar"; }
				 if (driver == null || driver.equals("")) {driver = "com.mysql.jdbc.Driver"; }
				 if (url == null || url.equals("")) { url = "jdbc:mysql://tech.com.au/test";	}
				 if (user == null || user.equals("")) { user = "root"; }
				 if (password == null) {	password = "chris"; } */

			File file = new File(jar);
			URLClassLoader cl = new URLClassLoader(new URL[] {file.toURL()}
												   , null);

			try {
				Properties prop = new Properties();
				prop.setProperty("user", user);
				prop.setProperty("password", password);
				if (jar == null || jar.equals("")) {
					conn = DriverManager.getConnection(getUrl(), prop);
				} else {
					Class drvCls = cl.loadClass(driver);
					Driver drv = (Driver)drvCls.newInstance();
					try {
						conn = drv.connect(getUrl(), prop);
					} catch (SQLException e) {
						throw new XenaException("Cannot connect: " + e.getMessage(), e);
					}
				}
			} catch (ClassNotFoundException e) {
				throw new XenaException("Driver class " + driver + "cannot be found", e);
			} catch (ClassCastException e) {
				throw new XenaException("Driver class specified is not an SQL driver", e);
			}
		} catch (MalformedURLException e) {
			throw new SAXException(e);
		} catch (Exception e) {
			throw new SAXException(e);
		}
		return conn;
	}

	public String getUser() {
		return user;
	}

	public String getUrl() {
		return url;
	}

	public List getQueries() {
		return queries;
	}

	public String getPassword() {
		return password;
	}

	public void parse(InputSource input, NormaliserResults results) 
	throws IOException, SAXException {
		ContentHandler ch = this.getContentHandler();
		AttributesImpl empty = new AttributesImpl();
		ch.startElement(DBURI, "database", "database:database", empty);
		try {
			Iterator it = queries.iterator();
			Connection conn = getConnection();
			while (it.hasNext()) {
				ch.startElement(DSURI, "dataset", "dataset:dataset", empty);
				String query = (String)it.next();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery();
				ResultSetMetaData md = rs.getMetaData();
				ch.startElement(DSURI, "definitions", "dataset:definitions", empty);
				char[] tableName = getTableNameFromQuery(query).toCharArray();
				ch.startElement(DSURI, "definitions", "dataset:name", empty);
				ch.characters(tableName, 0, tableName.length);
				ch.endElement(DSURI, "definitions", "dataset:name");
				ch.startElement(DSURI, "field-definitions", "dataset:field-definitions", empty);
				for (int i = 1; i <= md.getColumnCount(); i++) {
					AttributesImpl fdatt = new AttributesImpl();
					fdatt.addAttribute(DSURI, "id", "dataset:id", "ID", "f" + i);
					fdatt.addAttribute(DSURI, "type", "dataset:type", "CDATA", sqlToBasic(md.getColumnType(i)));
					ch.startElement(DSURI, "field-definition", "dataset:field-definition", fdatt);
					ch.startElement(DSURI, "field-name", "dataset:field-name", empty);
					char[] nm = md.getColumnName(i).toCharArray();
					ch.characters(nm, 0, nm.length);
					ch.endElement(DSURI, "field-name", "dataset:field-name");
					ch.startElement(DSURI, "field-caption", "dataset:field-caption", empty);
					char[] cp = md.getColumnLabel(i).toCharArray();
					ch.characters(cp, 0, cp.length);
					ch.endElement(DSURI, "field-caption", "dataset:field-caption");
					ch.endElement(DBURI, "field-definition", "dataset:field-definition");
				}
				ch.endElement(DBURI, "field-definitions", "dataset:field-definitions");
				ch.endElement(DBURI, "definitions", "dataset:definitions");
				ch.startElement(DSURI, "records", "dataset:records", empty);
				while (rs.next()) {
					ch.startElement(DSURI, "record", "dataset:record", empty);
					for (int i = 1; i <= md.getColumnCount(); i++) {
						String str = rs.getString(i);
						if (str != null) {
							AttributesImpl fdatt = new AttributesImpl();
							fdatt.addAttribute(DSURI, "idref", "dataset:idref", "IDREF", "f" + i);
							ch.startElement(DSURI, "field", "dataset:field", fdatt);
							String type = sqlToBasic(md.getColumnType(i));
							ch.startElement("http://preservation.naa.gov.au/" + type + "/1.0", type, type + ":" + type, empty);
							char[] cstr = str.toCharArray();
							ch.characters(cstr, 0, cstr.length);
							ch.endElement("http://preservation.naa.gov.au/" + type + "/1.0", type, type + ":" + type);
							ch.endElement(DSURI, "field", "dataset:field");
						}
					}
					ch.endElement(DSURI, "record", "dataset:record");
				}
				ch.endElement(DSURI, "records", "dataset:records");
				ch.endElement(DBURI, "dataset", "dataset:dataset");
			}
		} catch (SQLException e) {
			throw new SAXException(e);
		}
		ch.endElement(DBURI, "database", "database:database");
	}

	String getTableNameFromQuery(String query) throws SAXException {
		Pattern p = Pattern.compile(
			"[Ss][Ee][Ll][Ee][Cc][Tt].*?[Ff][Rr][Oo][Mm]\\p{Space}*([A-Za-z_]*)\\p{Space}*(AS\\p{Space}*([A-Za-z_]*))?.*");
		Matcher m = p.matcher(query);
		if (m.matches()) {
			if (m.group(3) != null) {
				return m.group(3);
			} else {
				return m.group(1);
			}
		} else {
			throw new SAXException("Can't discover table name from select statement.");
		}
	}

	String sqlToBasic(int type) {
		switch (type) {
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.SMALLINT:
			return "integer";
		case Types.VARCHAR:
		case Types.CHAR:
		default:
			return "string";
		}
	}
}
