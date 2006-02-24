package au.gov.naa.digipres.xena.plugin.dataset;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.xml.sax.InputSource;

import au.gov.naa.digipres.xena.gui.GuiConfigureNormaliser;
import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.javatools.Ascii;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 *  Panel 0 to Configure the CsvToXenaDatasetNormaliser.
 *
 * @author Chris Bitmead
 */
public class CsvToDatasetGuiConfigurePanel0 extends JPanel implements
	GuiConfigureSubPanel {
	private final static String NOQUOTE = "None";

	private final static String DBLQUOTE = "\"";

	private final static String SNGQUOTE = "\'";

	private GuiConfigureNormaliser configure;

	private boolean testOk = true;

	private FlowLayout fLay1 = new FlowLayout(FlowLayout.LEFT);

	private FlowLayout fLay2 = new FlowLayout(FlowLayout.LEFT);

	private java.util.List columnList = new ArrayList();

	private ButtonGroup guessGroup = new ButtonGroup();

	ButtonGroup headerButtonGroup = new ButtonGroup();

	private JPanel headerPanel = new JPanel();

	private JRadioButton firstRowNamesRadioButton = new JRadioButton();

	private JRadioButton noHeaderRadioButton = new JRadioButton();

	private GridLayout gridLayout1 = new GridLayout();

	private GridLayout gridLayout2 = new GridLayout();

	private JPanel cb4Panel1 = new JPanel(fLay2);

	private JRadioButton showAllTypes = new JRadioButton(
		"Show All Possible Types", true);

	private JPanel cb2Panel = new JPanel(fLay2);

	private JComboBox quoteCharacterComboBox = new JComboBox();

	private JLabel numberOfGuessRowsLabel = new JLabel(
		"Number Of Fields To Guess: ");

	private JTextField numberOfGuessRowsTextField = new JTextField();

	private JPanel delimPanel = new JPanel();

	private JPanel cb4Panel = new JPanel(fLay2);

	private JLabel fieldDelimiterLabel = new JLabel();

	private JComboBox fieldDelimiterComboBox = new JComboBox();

	private JPanel textDelimPanel = new JPanel(fLay1);

	private JRadioButton guessFirstRows = new JRadioButton("Guess Field Types");

	private JRadioButton guessAllRows = new JRadioButton(
		"Show All Possible Types", true);

	private JPanel fieldDelimPanel = new JPanel(fLay1);

	private JPanel radioPane = new JPanel();

	private JPanel noGuessesPanel = new JPanel(fLay2);

	private JLabel quoteCharacterLabel = new JLabel();

	private JRadioButton oneFieldHeaderRadioButton = new JRadioButton();

	private JComboBox headerFieldDelimiterComboBox = new JComboBox();

	private JLabel headerFieldDelimiterLabel = new JLabel();

	private JPanel headerFieldDelimPanel = new JPanel(fLay1);

	private BorderLayout borderLayout1 = new BorderLayout();

	private JLabel oneColumnLabel = new JLabel();

	private JPanel oneColumnPanel = new JPanel(fLay1);

	private BorderLayout borderLayout3 = new BorderLayout();

	private JTextField oneFieldNumber = new JTextField();

	private GridLayout gridLayout3 = new GridLayout();

	CsvToXenaDatasetNormaliser normaliser;

	XenaInputSource input;

	public CsvToDatasetGuiConfigurePanel0(GuiConfigureNormaliser configure, CsvToXenaDatasetNormaliser normaliser, InputSource input) {
		this.configure = configure;
		this.normaliser = normaliser;
		this.input = (XenaInputSource)input;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void finish() throws XenaException {
		normaliser.setFieldDelimiter((char)fieldDelimiterComboBox.
									 getSelectedIndex());
		normaliser.setUseQuoteCharacter(quoteCharacterComboBox.getSelectedItem().
										equals(CsvToDatasetGuiConfigurePanel0.
											   DBLQUOTE));
		if (quoteCharacterComboBox.getSelectedItem().equals(
			CsvToDatasetGuiConfigurePanel0.DBLQUOTE)) {
			normaliser.setQuoteCharacter('"');
		} else {
			if (quoteCharacterComboBox.getSelectedItem().equals(
				CsvToDatasetGuiConfigurePanel0.SNGQUOTE)) {
				normaliser.setQuoteCharacter('\'');
			} else {
				normaliser.setUseQuoteCharacter(false);
			}
		}
		normaliser.setFirstRowFieldNames(firstRowNamesRadioButton.isSelected());
		normaliser.setOneFieldHeader(this.oneFieldHeaderRadioButton.isSelected());
		normaliser.setHeaderFieldDelimiter((char)this.
										   headerFieldDelimiterComboBox.
										   getSelectedIndex());
		try {
			normaliser.setOneFieldHeaderNumber(Integer.parseInt(this.
																oneFieldNumber.getText()));
		} catch (NumberFormatException ex) {
			throw new XenaException(ex);
		}
		// XXXX bad format exception??
		if (guessAllRows.isSelected()) {
			normaliser.setGuessFields(CsvToXenaDatasetNormaliser.GUESS_ALL);
		} else if (guessFirstRows.isSelected()) {
			normaliser.setGuessFields(CsvToXenaDatasetNormaliser.GUESS_SOME);
		} else {
			normaliser.setGuessFields(CsvToXenaDatasetNormaliser.GUESS_NONE);
		}
		try {
			normaliser.setNumberOfGuessRows(Integer.parseInt(
				numberOfGuessRowsTextField.getText()));
		} catch (NumberFormatException ex) {
			throw new XenaException(ex);
		}
	}

	public void start() throws XenaException {
	}

	public void activate() throws XenaException {
		configure.nextOk(true);
		oneFieldHeaderRadioButton.setSelected(normaliser.isOneFieldHeader());
		headerFieldDelimiterComboBox.setSelectedIndex(normaliser.
													  getHeaderFieldDelimiter());
		oneFieldNumber.setText(Integer.toString(normaliser.
												getOneFieldHeaderNumber()));

		fieldDelimiterComboBox.setSelectedIndex(normaliser.getFieldDelimiter(input));
		if (normaliser.isUseQuoteCharacter()) {
			if (normaliser.getQuoteCharacter() == '"') {
				quoteCharacterComboBox.setSelectedItem(DBLQUOTE);
			} else {
				if (normaliser.getQuoteCharacter() == '\'') {
					quoteCharacterComboBox.setSelectedItem(SNGQUOTE);
				} else {
					throw new XenaException("Unknown Quote Character");
				}
			}
		} else {
			quoteCharacterComboBox.setSelectedItem(NOQUOTE);
		}
		firstRowNamesRadioButton.setSelected(normaliser.isFirstRowFieldNames());
		if (normaliser.getGuessFields() == CsvToXenaDatasetNormaliser.GUESS_ALL) {
			guessAllRows.setSelected(true);
		} else if (normaliser.getGuessFields() == CsvToXenaDatasetNormaliser.GUESS_SOME) {
			guessFirstRows.setSelected(true);
		} else if (normaliser.getGuessFields() == CsvToXenaDatasetNormaliser.GUESS_NONE) {
			showAllTypes.setSelected(true);
		}
		enableGuess();
		numberOfGuessRowsTextField.setText(Integer.toString(normaliser.
															getNumberOfGuessRows()));
	}

	void enableGuess() {
		this.numberOfGuessRowsLabel.setEnabled(guessFirstRows.isSelected());
		numberOfGuessRowsTextField.setEnabled(guessFirstRows.isSelected());
	}

	void guessFirstRows_actionPerformed(ActionEvent e) {
		enableGuess();
	}

	void jbInit() throws Exception {
		numberOfGuessRowsLabel = new JLabel("Number Of Fields To Guess: ");
		quoteCharacterLabel.setText("Quote Character");
		guessGroup.add(this.firstRowNamesRadioButton);
		guessGroup.add(this.noHeaderRadioButton);
		guessGroup.add(this.oneFieldHeaderRadioButton);
		this.headerButtonGroup.add(this.showAllTypes);
		this.headerButtonGroup.add(this.guessAllRows);
		this.headerButtonGroup.add(this.guessFirstRows);
		radioPane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		radioPane.setLayout(new BoxLayout(radioPane, BoxLayout.Y_AXIS));
		guessAllRows.setText("Guess with all rows");
		guessAllRows.setSelected(false);
		guessAllRows.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guessAllRows_actionPerformed(e);
			}
		});
		guessFirstRows.setText("Guess first \"n\" rows");
		guessFirstRows.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guessFirstRows_actionPerformed(e);
			}
		});
		fieldDelimiterComboBox.setFont(new java.awt.Font("Monospaced", 0, 12));
		fieldDelimiterLabel.setText("Field Delimiter     ");
		delimPanel.setLayout(gridLayout3);
		numberOfGuessRowsTextField.setText("10");
		numberOfGuessRowsTextField.setDocument(new PlainDocument() {
			public void insertString(
				int offset, String text, AttributeSet aset) throws
				BadLocationException {
				if (Character.isDigit(text.charAt(0))) {
					super.insertString(offset, text, aset);
				}
			}
		});
		numberOfGuessRowsTextField.setColumns(5);
		numberOfGuessRowsLabel.setText("Number Of Rows To Guess: ");
		numberOfGuessRowsLabel.setEnabled(guessFirstRows.isSelected());
		showAllTypes.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAllTypes_actionPerformed(e);
			}
		});
		this.setFont(new java.awt.Font("Monospaced", 0, 12));
		this.setLayout(gridLayout2);
		firstRowNamesRadioButton.setText("jRadioButton2");
		firstRowNamesRadioButton.addActionListener(new java.awt.event.
												   ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firstRowNamesRadioButton_actionPerformed(e);
			}
		});
		firstRowNamesRadioButton.setText("First row are column names");
		noHeaderRadioButton.setSelected(true);
		noHeaderRadioButton.setText("No headings in data");
		noHeaderRadioButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				noHeaderRadioButton_actionPerformed(e);
			}
		});
		headerPanel.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		gridLayout1.setRows(0);
		gridLayout2.setColumns(1);
		gridLayout2.setRows(0);

		Ascii ascii = new Ascii();
		for (int i = 0; i < 128; i++) {
			String s = ascii.getName(i) + " " + ascii.getSlashName(i) + " " +
				ascii.getOctal(i) + " " + ascii.getDecimal(i) + " " +
				ascii.getHexidecimal(i);
			fieldDelimiterComboBox.addItem(s);
			headerFieldDelimiterComboBox.addItem(s);
		}
		Dimension d = fieldDelimiterComboBox.getPreferredSize();
		enableGuess();
		oneFieldHeaderRadioButton.setText("Headings in one column");
		oneFieldHeaderRadioButton.addActionListener(new java.awt.event.
													ActionListener() {
			public void actionPerformed(ActionEvent e) {
				oneFieldHeaderRadioButton_actionPerformed(e);
			}
		});
		headerFieldDelimiterComboBox.setEnabled(false);
		headerFieldDelimiterComboBox.setFont(new java.awt.Font("Monospaced", 0,
															   12));
		headerFieldDelimiterLabel.setEnabled(false);
		headerFieldDelimiterLabel.setText("Header Field Delimiter     ");
		headerFieldDelimPanel.setLayout(borderLayout1);
		oneColumnLabel.setText("Column Number               ");
		oneColumnLabel.setEnabled(false);
		oneColumnPanel.setLayout(borderLayout3);
		oneFieldNumber.setEnabled(false);
		oneFieldNumber.setText("3");
		oneFieldNumber.setColumns(5);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(0);
		this.add(headerPanel, null);
		oneColumnPanel.add(oneColumnLabel, BorderLayout.WEST);
		oneColumnPanel.add(oneFieldNumber, BorderLayout.CENTER);
		headerFieldDelimPanel.add(headerFieldDelimiterLabel, BorderLayout.WEST);
		headerFieldDelimPanel.add(headerFieldDelimiterComboBox,
								  BorderLayout.CENTER);
		headerPanel.add(noHeaderRadioButton, null);
		headerPanel.add(firstRowNamesRadioButton, null);
		headerPanel.add(oneFieldHeaderRadioButton, null);
		headerPanel.add(oneColumnPanel, null);
		headerPanel.add(headerFieldDelimPanel, null);
		quoteCharacterComboBox.addItem(NOQUOTE);
		quoteCharacterComboBox.addItem(DBLQUOTE);
		quoteCharacterComboBox.addItem(SNGQUOTE);
		textDelimPanel.add(quoteCharacterLabel);
		textDelimPanel.add(quoteCharacterComboBox);
		delimPanel.add(fieldDelimPanel, null);
		fieldDelimPanel.add(fieldDelimiterLabel);
		fieldDelimPanel.add(fieldDelimiterComboBox);
		delimPanel.add(textDelimPanel, null);
		this.add(radioPane, null);
		radioPane.add(cb4Panel);
		cb4Panel.add(showAllTypes, FlowLayout.LEFT);
		radioPane.add(cb4Panel1, null);
		cb4Panel1.add(guessAllRows, FlowLayout.LEFT);
		radioPane.add(cb2Panel);
		cb2Panel.add(guessFirstRows, FlowLayout.LEFT);
		radioPane.add(noGuessesPanel);
		noGuessesPanel.add(numberOfGuessRowsLabel);
		noGuessesPanel.add(numberOfGuessRowsTextField);
		this.add(delimPanel, null);
	}

	void showAllTypes_actionPerformed(ActionEvent e) {
		enableGuess();
	}

	void guessAllRows_actionPerformed(ActionEvent e) {
		enableGuess();
	}

	void greyHeader() {
		this.oneColumnLabel.setEnabled(oneFieldHeaderRadioButton.isSelected());
		oneFieldNumber.setEnabled(oneFieldHeaderRadioButton.isSelected());
		this.headerFieldDelimiterLabel.setEnabled(oneFieldHeaderRadioButton.
												  isSelected());
		this.headerFieldDelimiterComboBox.setEnabled(oneFieldHeaderRadioButton.
													 isSelected());
	}

	void oneFieldHeaderRadioButton_actionPerformed(ActionEvent e) {
		greyHeader();
	}

	void firstRowNamesRadioButton_actionPerformed(ActionEvent e) {
		greyHeader();
	}

	void noHeaderRadioButton_actionPerformed(ActionEvent e) {
		greyHeader();
	}
}
