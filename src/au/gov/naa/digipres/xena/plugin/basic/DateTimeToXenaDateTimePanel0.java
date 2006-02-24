package au.gov.naa.digipres.xena.plugin.basic;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.gui.GuiConfigureSubPanel;
import au.gov.naa.digipres.xena.javatools.JarPreferences;
import au.gov.naa.digipres.xena.kernel.XenaException;

/**
 * Panel 0 in the DateTimeToXenaDateTimeNormaliser configuration.
 *
 * @author Chris Bitmead
 */
public class DateTimeToXenaDateTimePanel0 extends JPanel implements GuiConfigureSubPanel {
	DateTimeToXenaDateTimeGuiConfigure configure;

	JPanel jPanel1 = new JPanel();

	JPanel jPanel2 = new JPanel();

	Box box4 = Box.createVerticalBox();

	BorderLayout borderLayout1 = new BorderLayout();

	Border border1;

	TitledBorder titledBorder1;

	Border border2;

	TitledBorder titledBorder2;

	Border border3;

	TitledBorder titledBorder3;

	BorderLayout borderLayout2 = new BorderLayout();

	Border border4;

	TitledBorder titledBorder4;

	JPanel jPanel5 = new JPanel();

	JComboBox inputTimeZoneComboBox = new JComboBox();

	JPanel jPanel6 = new JPanel();

	JComboBox inputFormatComboBox = new JComboBox();

	JLabel jLabel4 = new JLabel();

	JLabel timeZoneLabel = new JLabel();

	BorderLayout borderLayout3 = new BorderLayout();

	BorderLayout borderLayout4 = new BorderLayout();

	JCheckBox yearCheckBox = new JCheckBox();

	JCheckBox monthCheckBox = new JCheckBox();

	JCheckBox dateCheckBox = new JCheckBox();

	JCheckBox weekCheckBox = new JCheckBox();

	JCheckBox julianCheckBox = new JCheckBox();

	JCheckBox minuteCheckBox = new JCheckBox();

	JCheckBox hourCheckBox = new JCheckBox();

	JCheckBox secondCheckBox = new JCheckBox();

	JCheckBox fractionCheckBox = new JCheckBox();

	JCheckBox timeZoneCheckBox = new JCheckBox();

	private JPanel jPanel3 = new JPanel();

	private JCheckBox useTimeZonesCheckBox = new JCheckBox();

	private JPanel jPanel4 = new JPanel();

	private JLabel exampleLabel = new JLabel();

	private JLabel jLabel2 = new JLabel();

	private BorderLayout borderLayout5 = new BorderLayout();

	private JCheckBox dayOfWeekCheckBox = new JCheckBox();

	private BorderLayout borderLayout6 = new BorderLayout();

	private JLabel jLabel1 = new JLabel();

	public DateTimeToXenaDateTimePanel0(DateTimeToXenaDateTimeGuiConfigure configure) {
		this.configure = configure;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void activate() throws XenaException {
		JarPreferences prefs = (JarPreferences)JarPreferences.userNodeForPackage(DateTimeToXenaDateTimePanel0.class);
		inputTimeZoneComboBox.setSelectedItem(prefs.get("defaultTimeZone", "Australia/ACT"));
	}

	public void start() throws XenaException {
		DateTimeToXenaDateTimeNormaliser n = (DateTimeToXenaDateTimeNormaliser)configure.getNormaliser();
		yearCheckBox.setSelected(n.isShowYear());
		monthCheckBox.setSelected(n.isShowMonth());
		dateCheckBox.setSelected(n.isShowDate());
		weekCheckBox.setSelected(n.isShowWeek());
		hourCheckBox.setSelected(n.isShowHour());
		minuteCheckBox.setSelected(n.isShowMinute());
		secondCheckBox.setSelected(n.isShowSecond());
		fractionCheckBox.setSelected(n.isShowFraction());
		julianCheckBox.setSelected(n.isShowJulian());
		timeZoneCheckBox.setSelected(n.isShowTimeZone());
		dayOfWeekCheckBox.setSelected(n.isShowDayOfWeek());
		if (n.getZoneName() != null) {
			inputTimeZoneComboBox.setSelectedItem(n.getZoneName());
		}
		if (n.getInputFormat() != null) {
			inputFormatComboBox.setSelectedItem(n.getInputFormat());
		}
		try {
			InputStream is = configure.getInputSource().getByteStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			is.close();
			exampleLabel.setText(line);
		} catch (IOException ex) {
			throw new XenaException(ex);
		}

	}

	public void finish() throws XenaException {
		DateTimeToXenaDateTimeNormaliser n = (DateTimeToXenaDateTimeNormaliser)configure.getNormaliser();
		n.setShowYear(yearCheckBox.isSelected());
		n.setShowMonth(monthCheckBox.isSelected());
		n.setShowDate(dateCheckBox.isSelected());
		n.setShowWeek(weekCheckBox.isSelected());
		n.setShowHour(hourCheckBox.isSelected());
		n.setShowMinute(minuteCheckBox.isSelected());
		n.setShowSecond(secondCheckBox.isSelected());
		n.setShowFraction(fractionCheckBox.isSelected());
		n.setShowJulian(julianCheckBox.isSelected());
		n.setShowTimeZone(timeZoneCheckBox.isSelected());
		n.setShowDayOfWeek(dayOfWeekCheckBox.isSelected());
		n.setZoneName((String)inputTimeZoneComboBox.getSelectedItem());
		n.setInputFormat((String)inputFormatComboBox.getSelectedItem());
	}

	void useTimeZonesCheckBox_actionPerformed(ActionEvent e) {
		inputTimeZoneComboBox.setEnabled(useTimeZonesCheckBox.isSelected());
		timeZoneLabel.setEnabled(useTimeZonesCheckBox.isSelected());
		timeZoneCheckBox.setSelected(true);
	}

	void monthCheckBox_actionPerformed(ActionEvent e) {
		this.weekCheckBox.setSelected(false);
		this.julianCheckBox.setSelected(false);
		this.dayOfWeekCheckBox.setSelected(false);
	}

	void weekCheckBox_actionPerformed(ActionEvent e) {
		this.monthCheckBox.setSelected(false);
		this.dateCheckBox.setSelected(false);
		this.julianCheckBox.setSelected(false);
	}

	void dateCheckBox_actionPerformed(ActionEvent e) {
		this.weekCheckBox.setSelected(false);
		this.julianCheckBox.setSelected(false);
		this.dayOfWeekCheckBox.setSelected(false);
	}

	void dayOfWeekCheckBox_actionPerformed(ActionEvent e) {
		this.monthCheckBox.setSelected(false);
		this.dateCheckBox.setSelected(false);
		this.julianCheckBox.setSelected(false);
	}

	void julianCheckBox_actionPerformed(ActionEvent e) {
		this.monthCheckBox.setSelected(false);
		this.dateCheckBox.setSelected(false);
		this.weekCheckBox.setSelected(false);
		this.dayOfWeekCheckBox.setSelected(false);
	}

	void secondCheckBox_actionPerformed(ActionEvent e) {
		if (!secondCheckBox.isSelected()) {
			fractionCheckBox.setSelected(false);
		}
	}

	void fractionCheckBox_actionPerformed(ActionEvent e) {
		if (fractionCheckBox.isSelected()) {
			secondCheckBox.setSelected(true);
		}
	}

	private void jbInit() throws Exception {
		border1 = BorderFactory.createEmptyBorder();
		titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(Color.white, 1), "Input Format");
		border2 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder2 = new TitledBorder(border2, "Output Format");
		border3 = BorderFactory.createLineBorder(SystemColor.controlText, 1);
		titledBorder3 = new TitledBorder(border3, "Output Format");
		border4 = BorderFactory.createEmptyBorder();
		titledBorder4 = new TitledBorder(border4, "XXX");
		this.setLayout(borderLayout1);
		jPanel1.setBorder(titledBorder1);
		jPanel1.setLayout(borderLayout2);
		jPanel2.setBorder(titledBorder3);
		String[] zones = TimeZone.getAvailableIDs();
		for (int i = 0; i < zones.length; i++) {
			inputTimeZoneComboBox.addItem(zones[i]);
		}
		inputTimeZoneComboBox.setEnabled(false);
		inputFormatComboBox.setEditable(true);
		inputFormatComboBox.addItem("yyyy/MM/dd'T'HH:mm:ss");
		inputFormatComboBox.addItem("yyyy/MM/dd HH:mm:ss");
		inputFormatComboBox.addItem("dd/MM/yyyy HH:mm:ss");
		inputFormatComboBox.addItem("MM/dd/yyyy HH:mm:ss");
		inputFormatComboBox.addItem("yyyy/MM/dd");
		inputFormatComboBox.addItem("dd/MM/yyyy");
		inputFormatComboBox.addItem("MM/dd/yyyy");
		inputFormatComboBox.addItem("HH:mm:ss");
		jLabel4.setText("      Format");
		timeZoneLabel.setEnabled(false);
		timeZoneLabel.setText("Time Zone");
		jPanel5.setLayout(borderLayout3);
		jPanel6.setLayout(borderLayout4);
		yearCheckBox.setText("Year");
		monthCheckBox.setText("Month");
		monthCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monthCheckBox_actionPerformed(e);
			}
		});
		dateCheckBox.setText("Date");
		dateCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dateCheckBox_actionPerformed(e);
			}
		});
		weekCheckBox.setText("Week");
		weekCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				weekCheckBox_actionPerformed(e);
			}
		});
		julianCheckBox.setText("Julian");
		julianCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				julianCheckBox_actionPerformed(e);
			}
		});
		minuteCheckBox.setText("Minute");
		hourCheckBox.setText("Hour");
		secondCheckBox.setText("Second");
		secondCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				secondCheckBox_actionPerformed(e);
			}
		});
		fractionCheckBox.setText("Second Fraction");
		fractionCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fractionCheckBox_actionPerformed(e);
			}
		});
		timeZoneCheckBox.setText("Time Zone");
		useTimeZonesCheckBox.setText("Use Time Zones");
		useTimeZonesCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useTimeZonesCheckBox_actionPerformed(e);
			}
		});
		jLabel2.setText("                    ");
		jPanel4.setLayout(borderLayout5);
		dayOfWeekCheckBox.setText("Day of Week");
		dayOfWeekCheckBox.addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dayOfWeekCheckBox_actionPerformed(e);
			}
		});
		jPanel3.setLayout(borderLayout6);
		jLabel1.setText("                   ");
		this.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(box4, BorderLayout.NORTH);
		jPanel3.add(jLabel1, BorderLayout.WEST);
		jPanel3.add(useTimeZonesCheckBox, BorderLayout.CENTER);
		box4.add(jPanel4, null);
		jPanel4.add(jLabel2, BorderLayout.WEST);
		jPanel4.add(exampleLabel, BorderLayout.CENTER);
		box4.add(jPanel6, null);
		box4.add(jPanel3, null);
		jPanel6.add(jLabel4, BorderLayout.WEST);
		jPanel6.add(inputFormatComboBox, BorderLayout.CENTER);
		box4.add(jPanel5, null);
		jPanel5.add(timeZoneLabel, BorderLayout.WEST);
		jPanel5.add(inputTimeZoneComboBox, BorderLayout.CENTER);
		this.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(yearCheckBox, null);
		jPanel2.add(monthCheckBox, null);
		jPanel2.add(dateCheckBox, null);
		jPanel2.add(weekCheckBox, null);
		jPanel2.add(dayOfWeekCheckBox, null);
		jPanel2.add(julianCheckBox, null);
		jPanel2.add(hourCheckBox, null);
		jPanel2.add(minuteCheckBox, null);
		jPanel2.add(secondCheckBox, null);
		jPanel2.add(fractionCheckBox, null);
		jPanel2.add(timeZoneCheckBox, null);
	}
}
