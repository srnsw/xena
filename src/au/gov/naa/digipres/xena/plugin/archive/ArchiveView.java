package au.gov.naa.digipres.xena.plugin.archive;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.TableSorter;

/**
 * Display a Xena multipage instance page by page with First, Prev, Next and
 * Last buttons.
 *
 * @author Chris Bitmead
 */
public class ArchiveView extends XenaView {

	private ArchiveTableModel tableModel;
	private JTable emailTable;
	private TableSorter sorter;
	
	public ArchiveView() 
	{
		super();
		initGUI();
	}

	private void initGUI()
	{
		tableModel = new ArchiveTableModel();
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
						displayEntry(modelRow);
					}
					catch (Exception ex)
					{
						JOptionPane.showMessageDialog(ArchiveView.this, ex.getMessage(), "Mailbox View Exception", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
    		
    	});
	}
	
	private void displayEntry(int selectedRow) throws XenaException
	{
		String entryXmlFilename = tableModel.getSelectedFilename(selectedRow);
		File entryXmlFile = new File(getSourceDir(), entryXmlFilename);
		//ViewManager viewManager = ViewManager.singleton();
		NormalisedObjectViewFactory novFactory = new NormalisedObjectViewFactory(viewManager);
		XenaView entryView = novFactory.getView(entryXmlFile);
		
		// DPR shows the mailbox view in a dialog. This caused issues when opening up a new frame, 
		// I think due to modal issues. So the solution is to open the message view in another dialog,
		// This requires a search for the parent frame or dialog, so we can set the parent of the message
		// dialog correctly.
		Container parent = this.getParent();
		while (parent != null && !(parent instanceof Dialog || parent instanceof Frame))
		{
			parent = parent.getParent();
		}
		
		JDialog entryDialog;
		if (parent instanceof Dialog)
		{
			entryDialog = new JDialog((Dialog)parent);
		}
		else if (parent instanceof Frame)
		{
			entryDialog = new JDialog((Frame)parent);
		}
		else
		{
			// Fallback...
			entryDialog = new JDialog((Frame)null);
		}
		
		entryDialog.setLayout(new BorderLayout());
		entryDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		entryDialog.add(entryView, BorderLayout.CENTER);
		entryDialog.setSize(800, 600);
		entryDialog.setLocationRelativeTo(this);
		entryDialog.setVisible(true);		
		
	}

	public String getViewName() 
	{
		return "Archive Entry View";
	}

	public boolean canShowTag(String tag) 
	{
		return tag.equals(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ARCHIVE_TAG);
	}

	public ContentHandler getContentHandler() throws XenaException 
	{
		return new ArchiveViewHandler();
	}
	
	
	
	private class ArchiveViewHandler extends XMLFilterImpl
	{	
		public ArchiveViewHandler()
		{
			super();
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			
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
			
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
		{
			if (qName.equalsIgnoreCase(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_TAG))
			{
				String xenaFilename = atts.getValue(ArchiveNormaliser.ENTRY_OUTPUT_FILENAME);
				String originalPath = atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_PATH_ATTRIBUTE);
				ArchiveEntry entry = new ArchiveEntry(originalPath, xenaFilename);
				
				// Set original file date
				String originalDate = atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE);
				SimpleDateFormat dateFormat = new SimpleDateFormat(ArchiveNormaliser.DATE_FORMAT_STRING);
				Date date;
				try
				{
					date = dateFormat.parse(originalDate);
				}
				catch (ParseException e)
				{
					throw new SAXException("Problem parseing original date.", e);
				}
				entry.setOriginalFileDate(date);
				
				// Set original file size
				String fileSizeStr = atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_SIZE_ATTRIBUTE);
				entry.setOriginalSize(Long.parseLong(fileSizeStr));
				
				tableModel.addArchiveEntry(entry);
				
			}
		}
		
	}
}
