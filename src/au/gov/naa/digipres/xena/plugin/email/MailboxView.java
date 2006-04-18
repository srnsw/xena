package au.gov.naa.digipres.xena.plugin.email;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.TableSorter;
import au.gov.naa.digipres.xena.viewer.NormalisedObjectViewFrame;

/**
 * View to display the mailbox summary Xena file type.
 * A message summary for each messages is displayed in a table, 
 * and double-clicking a table entry will produce a window 
 * displaying the xena view for that message.
 *
 * @author Justin Waddell
 */
public class MailboxView extends XenaView 
{
	private MailboxTableModel tableModel;
	private JTable emailTable;
	private TableSorter sorter;
	
	public MailboxView() 
	{
		super();
		initGUI();
	}
	
	private void initGUI()
	{
		tableModel = new MailboxTableModel();
		sorter = new TableSorter(tableModel);
		emailTable = new JTable(sorter);
		sorter.setTableHeader(emailTable.getTableHeader());
		this.add(new JScrollPane(emailTable), BorderLayout.CENTER);
		
    	// Action Listeners
    	emailTable.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent e)
			{
				if (e.getModifiers() == MouseEvent.BUTTON1_MASK &&
					e.getClickCount() == 2)
				{
					try
					{
						int modelRow = sorter.modelIndex(emailTable.getSelectedRow());
						displayEmail(modelRow);
					}
					catch (Exception ex)
					{
						JOptionPane.showMessageDialog(MailboxView.this, ex.getMessage(), "Mailbox View Exception", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
    		
    	});
	}
	
	private void displayEmail(int selectedRow) throws XenaException
	{
		String emailXmlFilename = tableModel.getSelectedFilename(selectedRow);
		File emailXmlFile = new File(emailXmlFilename);
		//ViewManager viewManager = ViewManager.singleton();
		NormalisedObjectViewFactory novFactory = new NormalisedObjectViewFactory(viewManager);
		XenaView emailView = novFactory.getView(emailXmlFile);
		NormalisedObjectViewFrame viewFrame =
			new NormalisedObjectViewFrame(emailView,
			                              viewManager,
			                              emailXmlFile);
		
		// Display frame
		viewFrame.setLocation(this.getX()+50, this.getY()+50);
		viewFrame.setVisible(true);
	}

	public String getViewName() 
	{
		return "Mailbox View";
	}

	public boolean canShowTag(String tag) 
	{
		return tag.equals(EmailToXenaEmailNormaliser.MAILBOX_ROOT_TAG);
	}

	public ContentHandler getContentHandler() throws XenaException 
	{
		return new MailboxViewHandler();
	}
	
	
	
	private class MailboxViewHandler extends XMLFilterImpl
	{	
		StringBuffer buffer;
		
		private static final String XPATH_PREFIX =
			"/package/content/package/content/email/headers/header[@name='";
			
		private static final String XPATH_SUFFIX =
			"']/text()";
		
		private static final String FROM_XPATH_STRING = 
			XPATH_PREFIX + "From" + XPATH_SUFFIX;
		
		private static final String TO_XPATH_STRING = 
			XPATH_PREFIX + "To" + XPATH_SUFFIX;
		
		private static final String SUBJECT_XPATH_STRING = 
			XPATH_PREFIX + "Subject" + XPATH_SUFFIX;

		private static final String DATE_XPATH_STRING = 
			XPATH_PREFIX + "Date" + XPATH_SUFFIX;

		
		public MailboxViewHandler()
		{
			super();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			buffer.append(ch, start, length);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
		 */
		@Override
		public void endDocument() throws SAXException
		{
			
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) 
		throws SAXException
		{
			if (!qName.equalsIgnoreCase(EmailToXenaEmailNormaliser.MAILBOX_ROOT_TAG))
			{
				String msgFileName = buffer.toString();
				
				File msgXML = new File(msgFileName);
				if (msgXML.exists() && msgXML.isFile())
				{
					try
					{
						DocumentBuilder builder = 
							DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document xmlDoc = builder.parse(msgXML);
						
						XPath xpath = XPathFactory.newInstance().newXPath();
						
						String fromAddress = 
							xpath.evaluate(FROM_XPATH_STRING, xmlDoc);
						String toAddress = 
							xpath.evaluate(TO_XPATH_STRING, xmlDoc);
						String subject = 
							xpath.evaluate(SUBJECT_XPATH_STRING, xmlDoc);
						String dateStr = 
							xpath.evaluate(DATE_XPATH_STRING, xmlDoc);
						
						SimpleDateFormat dateFormat = new SimpleDateFormat(MessageNormaliser.DATE_FORMAT_STRING);
						Date date = dateFormat.parse(dateStr);
						
						MessageInfo msgInfo = 
							new MessageInfo(fromAddress, toAddress, subject, 
							                date, msgFileName);
						tableModel.addMessage(msgInfo);
					}
					catch (Exception ex)
					{
						throw new SAXException(ex);
					}
				}
				
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
		{
			if (!qName.equalsIgnoreCase(EmailToXenaEmailNormaliser.MAILBOX_ROOT_TAG))
			{
				buffer = new StringBuffer();				
			}
		}
	}
	
}
