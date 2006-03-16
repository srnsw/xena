package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdom.Namespace;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import au.gov.naa.digipres.xena.gui.GuiConfigureNormaliser;
import au.gov.naa.digipres.xena.gui.GuiConfigureNormaliserManager;
import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.kernel.PluginManager;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.decoder.Decoder;
import au.gov.naa.digipres.xena.kernel.decoder.DecoderManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

/**
 *  Panel 1 to Configure the CsvToXenaDatasetNormaliser.
 *
 * @author Chris Bitmead
 */
public class CsvToDatasetGuiConfigurePanel1 extends JPanel implements GuiConfigureSubPanel {
	final static String URI = "http://preservation.naa.gov.au/dataset/1.0";

	final static String UNSPECIFIED = "Unspecified";

	Namespace ns = Namespace.getNamespace("dataset", URI);

	private final static int FIELD_CAPTION = 0;

	private final static int DECODER = 1;

	private final static int FILETYPE = 2;

	private final static int CONFIGURE = 3;

	private final static int NUMBER_HEADINGS = CONFIGURE + 1;

	private java.util.List dataFileList = new ArrayList();

	private JLabel lblName;

	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	private String[] columnArray;

	private Object[][] dataArray;

	private String[] rowName;

	private Object[][] rowLabels;

	private int classRowNo = 0;

	private List fieldNameList = new ArrayList();

	private JPanel jPanel1 = new JPanel();

	private BorderLayout borderLayout1 = new BorderLayout();

	private JScrollPane jScrollPane1;

	private JScrollPane jScrollPane2;

	private GuiConfigureNormaliser configure;

	private JTable rowNameTable = new JTable();

	private MyJTable table = new MyJTable();

	private int tableLength = 0;

	private int tableWidth = 0;

	CsvToXenaDatasetNormaliser normaliser;

	XenaInputSource input;

	public CsvToDatasetGuiConfigurePanel1(GuiConfigureNormaliser configure, CsvToXenaDatasetNormaliser normaliser, InputSource input) {
		this.configure = configure;
		this.normaliser = normaliser;
		this.input = (XenaInputSource)input;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setUpTypeRow(ArrayList fields) throws IOException, SAXException, XenaException {
		List alltypes = PluginManager.singleton().getTypeManager().allNonXenaFileTypes();
		alltypes.add(0, UNSPECIFIED);
		for (int i = 0; i < tableWidth; i++) {
			List set;
			if (i < fields.size()) {
				set = (ArrayList)fields.get(i);
				set.add(0, UNSPECIFIED);
			} else {
				set = alltypes;
			}
			JComboBox typeComboBox = new JComboBox(set.toArray());
			table.addEditorForTypeColumn(i, typeComboBox);
			if (normaliser.getFileType(i) == null) {
				typeComboBox.setSelectedItem(set.get(set == alltypes ? 0 : 1));
			} else {
				typeComboBox.setSelectedItem(normaliser.getFileType(i));
			}
			dataArray[FILETYPE][i] = typeComboBox.getSelectedItem();
		}
	}

	public boolean isTypeConfigurable(Object type) throws XenaException {
		if (type == null || type.equals(UNSPECIFIED)) {
			return false;
		} else {
			XMLReader subTrans = (XMLReader)PluginManager.singleton().getNormaliserManager().lookup((Type)type);
			return GuiConfigureNormaliserManager.singleton().lookup(subTrans.getClass(), (Type)type) != null;
		}
	}

	public void finish() {
		for (int i = 0; i < tableWidth; i++) {
			Object type = table.getValueAt(FILETYPE, i);
			if (!type.equals(UNSPECIFIED)) {
				normaliser.setFileType(i, (FileType)type);
			}
			normaliser.setDecoder(i, (Decoder)table.getValueAt(DECODER, i));
			String caption = (String)table.getValueAt(FIELD_CAPTION, i);
			if (caption != null && !caption.trim().equals("")) {
				normaliser.setFieldCaption(i, caption);
			}
		}
	}

	public void start() {
	}

	public static int max(int a, int b) {
		if (a > b) {
			return a;
		}
		return b;
	}

	public void activate() throws XenaException {
		configure.nextOk(true);
		try {
			ArrayList guessTypes = new ArrayList();
			List records = new ArrayList();
			CsvTokenizer tok = null;
			if (input == null) {
				tableWidth = max(normaliser.getCaptionList().size(), normaliser.getfileTypeList().size());
			} else {
				tok = normaliser.getTokenizer(input);
				for (int i = 0; i < 10 || i < normaliser.getNumberOfGuessRows(); i++) {
					List rec = tok.nextRecord();
					if (rec == null) {
						break;
					} else {
						records.add(rec);
						if (normaliser.getGuessFields() != CsvToXenaDatasetNormaliser.GUESS_NONE && i < normaliser.getNumberOfGuessRows()) {
							normaliser.getPossibleFileTypes(rec, guessTypes);
						}
						if (tableWidth < rec.size()) {
							tableWidth = rec.size();
						}
					}
				}
			}
			columnArray = new String[tableWidth];
			tableLength = records.size();
			if (tok == null || tok.getHeader() == null) {
				for (int i = 0; i < tableWidth; i++) {
					columnArray[i] = " ";
				}
			} else {
				Iterator it = tok.getHeader().iterator();
				for (int i = 0; i < tableWidth; i++) {
					String head = null;
					if (it.hasNext()) {
						head = (String)it.next();
					}
					if (head == null) {
						columnArray[i] = " ";
					} else {
						columnArray[i] = head;
					}
				}
			}
			dataArray = new Object[NUMBER_HEADINGS + tableLength][tableWidth];
			rowLabels = new Object[NUMBER_HEADINGS + tableLength][1];
			Iterator rit = records.iterator();
			for (int recNo = 0; rit.hasNext(); recNo++) {
				List rec = (List)rit.next();
				Iterator fit = rec.iterator();
				for (int col = 0; fit.hasNext(); col++) {
					String s = (String)fit.next();
					dataArray[NUMBER_HEADINGS + recNo][col] = s;
				}
			}

			rowName = new String[1];
			rowName[0] = "Name";
			rowLabels[FIELD_CAPTION][0] = "Caption";
			rowLabels[DECODER][0] = "Encoding";
			rowLabels[FILETYPE][0] = "Data Type";
			rowLabels[CONFIGURE][0] = "";
			for (int i = NUMBER_HEADINGS, j = 1; j <= tableLength; i++, j++) {
				rowLabels[i][0] = Integer.toString(j) + OrdinalPostfix.postfix(j);
			}

			/*
			 *  Decoders
			 */
			Iterator dit = PluginManager.singleton().getDecoderManager().iterator();
			JComboBox encComboBox = new JComboBox(PluginManager.singleton().getDecoderManager().getAllDecoders().toArray());
			table.addEditorForRow(DECODER, encComboBox);
			for (int i = 0; i < tableWidth; i++) {
				dataArray[FILETYPE][i] = normaliser.getFileType(i);
				dataArray[DECODER][i] = normaliser.getDecoder(i);
				dataArray[FIELD_CAPTION][i] = normaliser.getFieldCaption(i);
			}

			setUpTypeRow(guessTypes);
			MyTableModel myModel = new MyTableModel(dataArray, columnArray);
			LabelTableModel labelModel = new LabelTableModel(rowLabels, rowName);
			table.setModel(myModel);
			rowNameTable.setModel(labelModel);

			TableColumnModel colmod = table.getColumnModel();

			for (int i = 0; i < tableWidth; i++) {
				TableColumn c = colmod.getColumn(i);
				TableCellEditor ed = c.getCellEditor();
				c.setPreferredWidth(120);
				c.setCellRenderer(new MyTableCellRenderer());
			}
			colmod = rowNameTable.getColumnModel();
			TableColumn c = colmod.getColumn(0);
			c.setPreferredWidth(120);
		} catch (IOException e) {
			throw new XenaException(e);
		} catch (SAXException e) {
			throw new XenaException(e);
		}
	}

	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		splitPane.setDividerLocation(120);
		jScrollPane1 = new JScrollPane(table);
		jScrollPane2 = new JScrollPane(rowNameTable);
		final JViewport jvp1 = jScrollPane1.getViewport();
		final JViewport jvp2 = jScrollPane2.getViewport();
		jvp1.addChangeListener(
			new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				jvp2.setViewPosition(new Point(0, (int)jvp1.getViewPosition().getY()));

			}
		});
		jScrollPane1.setSize(400, 400);
		jScrollPane2.setSize(120, 400);
		jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPane2.setVerticalScrollBarPolicy(jScrollPane2.VERTICAL_SCROLLBAR_NEVER);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		splitPane.setDividerLocation(120);
		splitPane.add(jScrollPane1, JSplitPane.RIGHT);
		splitPane.add(jScrollPane2, JSplitPane.LEFT);
		splitPane.setDividerSize(1);
		this.add(splitPane, BorderLayout.CENTER);
		splitPane.setSize(table.getSize());
		jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		table.addMouseListener(new MyTableListener(table));
	}

	public class MyJTable extends JTable {
		protected Map data = new HashMap();

		protected Map typeData = new HashMap();

		public TableCellEditor getCellEditor(int row, int col) {
			TableCellEditor rtn = null;
			if (row == FILETYPE) {
				rtn = (DefaultCellEditor)typeData.get(new Integer(col));
			}
			if (rtn == null) {
				rtn = (DefaultCellEditor)data.get(new Integer(row));
			}
			if (rtn == null) {
				rtn = super.getCellEditor(row, col);
			}
			return rtn;
		}

		public void addEditorForRow(int row, JComboBox e) {
			data.put(new Integer(row), new DefaultCellEditor(e));
		}

		public void addEditorForTypeColumn(int col, JComboBox e) {
			typeData.put(new Integer(col), new DefaultCellEditor(e));
		}
	}

	class MyTableListener extends MouseAdapter {
		private JTable table;

		MyTableListener(JTable table) {
			this.table = table;
		}

		public void mouseClicked(MouseEvent evt) {
			int row = table.rowAtPoint(evt.getPoint());
			int col = table.columnAtPoint(evt.getPoint());

			try {
				if (row == CONFIGURE && SwingUtilities.isLeftMouseButton(evt) && isTypeConfigurable(table.getValueAt(FILETYPE, col))) {
					Object type = table.getValueAt(FILETYPE, col);
					if (!(type instanceof FileType)) {
						type = null;
					}
					normaliser.setFileType(col, (FileType)type);
					XMLReader subNormaliser = (XMLReader)normaliser.getNormaliser(col);
					// We need to do this to force remembering the same object.
					// Is this the best way of doing it?
					normaliser.setNormaliser(col, subNormaliser);
					GuiConfigureNormaliserManager.singleton().configure(subNormaliser, null);
				}
			} catch (XenaException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (SAXException ex) {
				ex.printStackTrace();
			}
		}
	}

	class MyTableModel extends DefaultTableModel {
		public MyTableModel(Object[][] data, String[] columnNames) {
			super(data, columnNames);
		}

		public boolean isCellEditable(int row, int col) {
			if (row == FIELD_CAPTION || row == DECODER || row == FILETYPE) {
				return true;
			} else {
				return false;
			}

		}
	}

	class LabelTableModel extends DefaultTableModel {
		LabelTableModel(Object[][] data, String[] columnNames) {
			super(data, columnNames);
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	class MyTableCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
													   Object value,
													   boolean isSelected,
													   boolean hasFocus,
													   int row,
													   int column) {
			try {
				if (row == CONFIGURE) {
					JButton br = new JButton("configure");
					br.repaint();
					br.setEnabled(isTypeConfigurable(table.getValueAt(FILETYPE, column)));
					return br;
				} else {
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			} catch (XenaException e) {
				e.printStackTrace();
			}
			return this;
		}
	}
}
