/*
 * Created on 24/04/2006
 * andrek24
 * 
 */
package au.gov.naa.digipres.xena.demo.orgx;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.util.SourceURIParser;
import au.gov.naa.digipres.xena.util.TagContentFinder;

public class OrgXMetaDataWrapper extends AbstractMetaDataWrapper {

	/*
	 * DEFINE ALL OUR ORG_X TAG NAMES...
	 */

	public static final String ORGX_OPENING_TAG = "orgx";

	public static final String ORGX_META_TAG = "meta";

	public static final String ORGX_DEPARTMENT_TAG = "department";

	public static final String ORGX_USER_TAG = "user_name";

	public static final String ORGX_INPUT_NAME_TAG = "input_name";

	public static final String ORGX_CONTENT_TAG = "record_data";

	public static final String ORGX_ID_TAG = "orgx_id";

	/*
	 * PROVIDE SOME SENSIBLE DEFAULTS...
	 */

	private InfoProvider myInfoProvider = new DemoInfoProvider();

	public void setInfoProvider(InfoProvider infoProvider) {
		myInfoProvider = infoProvider;
	}

	@Override
	public String getOpeningTag() {
		return ORGX_OPENING_TAG;
	}

	@Override
	public String getSourceId(XenaInputSource input) throws XenaException {
		return TagContentFinder.getTagContents(input, ORGX_ID_TAG);
	}

	@Override
	public String getSourceName(XenaInputSource input) throws XenaException {
		return TagContentFinder.getTagContents(input, ORGX_INPUT_NAME_TAG);
	}

	@Override
	public void startDocument() throws SAXException {

		String departmentName = myInfoProvider.getDepartmentName();
		String userName = myInfoProvider.getUserName();
		String fileName = "";
		try {
			XenaInputSource xis = (XenaInputSource) getProperty("http://xena/input");
			if (xis != null) {
				fileName = SourceURIParser.getRelativeSystemId(xis, metaDataWrapperManager.getPluginManager());
			}
		} catch (SAXException saxe) {
			fileName = "Unknown";
		}

		super.startDocument();
		ContentHandler th = getContentHandler();
		AttributesImpl att = new AttributesImpl();
		th.startElement(null, ORGX_OPENING_TAG, ORGX_OPENING_TAG, att);
		th.startElement(null, ORGX_META_TAG, ORGX_META_TAG, att);

		// department name
		th.startElement(null, ORGX_DEPARTMENT_TAG, ORGX_DEPARTMENT_TAG, att);
		th.characters(departmentName.toCharArray(), 0, departmentName.toCharArray().length);
		th.endElement(null, ORGX_DEPARTMENT_TAG, ORGX_DEPARTMENT_TAG);

		// user name
		th.startElement(null, ORGX_USER_TAG, ORGX_USER_TAG, att);
		th.characters(userName.toCharArray(), 0, userName.toCharArray().length);
		th.endElement(null, ORGX_USER_TAG, ORGX_USER_TAG);

		// input name
		th.startElement(null, ORGX_INPUT_NAME_TAG, ORGX_INPUT_NAME_TAG, att);
		th.characters(fileName.toCharArray(), 0, fileName.toCharArray().length);
		th.endElement(null, ORGX_INPUT_NAME_TAG, ORGX_INPUT_NAME_TAG);

		// org x ID
		th.startElement(null, ORGX_ID_TAG, ORGX_ID_TAG, att);
		String orgx_id = fileName + "_" + departmentName + "_" + userName + "_";
		th.characters(orgx_id.toCharArray(), 0, orgx_id.toCharArray().length);
		th.endElement(null, ORGX_ID_TAG, ORGX_ID_TAG);

		th.endElement(null, ORGX_META_TAG, ORGX_META_TAG);
		th.startElement(null, ORGX_CONTENT_TAG, ORGX_CONTENT_TAG, att);

	}

	@Override
	public void endDocument() throws org.xml.sax.SAXException {
		ContentHandler th = getContentHandler();
		th.endElement(null, ORGX_CONTENT_TAG, ORGX_CONTENT_TAG);
		th.endElement(null, ORGX_OPENING_TAG, ORGX_OPENING_TAG);
		super.endDocument();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper#getName()
	 */
	@Override
	public String getName() {
		return "OrgX Meta Data Wrapper";
	}

}
