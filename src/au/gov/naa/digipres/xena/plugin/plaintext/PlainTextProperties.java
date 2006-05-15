/*
 * Created on 13/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.plaintext;

import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class PlainTextProperties extends PluginProperties
{

    public static final String PLUGIN_NAME = "plaintext";
    public static final String TAB_SIZE = "Tab Size";
    public static final String CHARSET = "Charset";
    public static final String EXCEPTION_ON_ILLEGAL_CHAR = "Exception on illegal characters";
    public static final String ENCLOSE_ILLEGAL_CHAR = "Enclose illegal characters";
    
	private List<XenaProperty> properties;
	
	public PlainTextProperties()
	{
		
	}
	
	public void initialiseProperties()
	{
		properties = new ArrayList<XenaProperty>();
		
		// Tab size property
		XenaProperty tabSizeProperty = new XenaProperty(TAB_SIZE,
		                                                "Number of spaces per tab character",
		                                                XenaProperty.PropertyType.INT_TYPE,
		                                                this.getName());
		this.getManager().loadProperty(tabSizeProperty);
        properties.add(tabSizeProperty);


//        // Charset property
//        XenaProperty charsetProperty = new XenaProperty(CHARSET,
//                                                        "Default charset for export output",
//                                                        XenaProperty.PropertyType.SINGLE_OPTION_TYPE,
//                                                        this.getName());
//        List<Object> charsetOptions = new ArrayList<Object>();
//        charsetOptions.add("UTF-8");
//        charsetOptions.add("US-ASCII");
//        charsetOptions.add("ISO-8859-1");
//        charsetOptions.add("UTF-16BE");
//        charsetOptions.add("UTF-16LE");
//        charsetOptions.add("UTF-16");
//        charsetProperty.setListOptions(charsetOptions);
//        this.getManager().loadProperty(charsetProperty);
//        properties.add(charsetProperty);

        //Exception on bad character property...
        XenaProperty exceptionOnBadCharacters = new XenaProperty(EXCEPTION_ON_ILLEGAL_CHAR,
                "Throw exception on illegal character",
                XenaProperty.PropertyType.BOOLEAN_TYPE,
                this.getName());
        this.getManager().loadProperty(exceptionOnBadCharacters);
        properties.add(exceptionOnBadCharacters);
        
        //Enclose bad characters in XML
        XenaProperty encloseBadCharacters = new XenaProperty(ENCLOSE_ILLEGAL_CHAR,
                "Enclose illegal characrers in XML tags",
                XenaProperty.PropertyType.BOOLEAN_TYPE,
                this.getName());
        
        this.getManager().loadProperty(encloseBadCharacters);
        properties.add(encloseBadCharacters);

    
    }

	public String getName()
	{
		return PLUGIN_NAME;
	}

	public List<XenaProperty> getProperties()
	{
		return properties;
	}

}
