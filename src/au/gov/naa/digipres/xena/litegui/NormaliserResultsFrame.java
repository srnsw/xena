/*
 * Created on 5/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.viewer.NormalisedObjectViewFrame;

/**
 * Display the given NormaliserResults object in a frame.
 * The source file, destination file, Normaliser and GuessedType
 * are all displayed in fields, and any messages are displayed in a
 * TextArea. If the normalisation was successful and an output file
 * exists, then the output file may be opened using the XenaViewer.
 * @author justinw5
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public class NormaliserResultsFrame extends JFrame
{
	NormaliserResults results;
	Xena xenaInterface;
	
	/**
	 * Creates a new NormaliserResultsFrame, and initialises
	 * the GUI.
	 * 
	 * @param results
	 * @param xenaInterface
	 */
	public NormaliserResultsFrame(NormaliserResults results,
								  Xena xenaInterface)
	{
		super();
		this.results = results;
		this.xenaInterface = xenaInterface;
		
		initGUI();
		this.validate();
	}

	/**
	 * One-time GUI initialisation
	 *
	 */
	private void initGUI()
	{
		this.setTitle("Normaliser Results");
		this.setSize(640, 480);
		this.setResizable(false);
		Font labelFont = new Font("Arial", Font.BOLD, 14);
		
		// Source File label, with a text area to display the file name
		JLabel sourceLabel = new JLabel("Source: ");
		sourceLabel.setFont(labelFont);
		JTextArea sourceText = new JTextArea(3, 60);
		sourceText.setText(results.getInputSystemId());
		sourceText.setEditable(false);
		sourceText.setBackground(sourceLabel.getBackground());
		sourceText.setWrapStyleWord(true);
		sourceText.setLineWrap(true);
		
		// Destination File label, with a text area to display the file name
		JLabel destLabel = new JLabel("Destination File: ");
		destLabel.setFont(labelFont);
		JTextArea destText = new JTextArea(3, 60);
		
		String destDir = results.getDestinationDirString();
		String destFile = results.getOutputFileName();		
		String destPathStr = (destDir == null || destDir.equals("") ||
							  destFile == null || destFile.equals(""))
							  ? "Output file not created"
							  : destDir + File.separator + destFile;
		destText.setText(destPathStr);
		destText.setEditable(false);
		destText.setBackground(sourceLabel.getBackground());
		destText.setWrapStyleWord(false);
		destText.setLineWrap(true);
		
		// Guessed Type label, with another label to display the type
		JLabel typeLabel = new JLabel("Guessed Type: ");
		typeLabel.setFont(labelFont);
		JLabel typeText = new JLabel();
		typeText.setFont(sourceText.getFont());
		String guessedType = 
			results.getInputType() != null ? results.getInputType().getName()
										   : "Unknown";
		typeText.setText(guessedType);
		
		// Normaliser label, with another label to display the normaliser name
		JLabel normaliserLabel = new JLabel("Normaliser: ");
		normaliserLabel.setFont(labelFont);
		JLabel normaliserText = new JLabel();
		normaliserText.setFont(sourceText.getFont());
		String normaliser =
			results.getNormaliser() != null ? results.getNormaliser().getName()
											: "Unknown";
		normaliserText.setText(normaliser);
		
		// Messages text area
		JTextArea messageText = new JTextArea(5, 60);
		messageText.setEditable(false);
		messageText.setText(results.getErrorMessages());
		messageText.setWrapStyleWord(true);
		messageText.setLineWrap(true);
		JScrollPane messageSP = new JScrollPane(messageText);
		messageSP.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), 
		                                     "Messages"));
		
		// Buttons
		JButton viewButton = new JButton("Xena View");
		if (!results.isNormalised() || 
			results.getOutputFileName() == null)
		{
			viewButton.setEnabled(false);
		}
		JButton closeButton = new JButton("Close");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(viewButton);
		buttonPanel.add(closeButton);
		
		// Main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder(new EtchedBorder(),
		                                     "Normaliser Results"));
		
		// Add source label and text to main panel
		addToGridBag(mainPanel, sourceLabel, 0, 0, GridBagConstraints.RELATIVE, 1,
		             0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		             new Insets(10, 10, 10, 10), 0, 0);
		addToGridBag(mainPanel, sourceText, 1, 0, GridBagConstraints.REMAINDER, 1,
		             1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
		             new Insets(10, 10, 10, 10), 0, 0);

		// Add destination label and text to main panel
		addToGridBag(mainPanel, destLabel, 0, 1, GridBagConstraints.RELATIVE, 1,
		             0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		             new Insets(10, 10, 10, 10), 0, 0);
		addToGridBag(mainPanel, destText, 1, 1, GridBagConstraints.REMAINDER, 1,
		             1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
		             new Insets(10, 10, 10, 10), 0, 0);

		// Add type label and text to main panel
		addToGridBag(mainPanel, typeLabel, 0, 2, GridBagConstraints.RELATIVE, 1,
		             0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		             new Insets(10, 10, 10, 10), 0, 0);
		addToGridBag(mainPanel, typeText, 1, 2, GridBagConstraints.REMAINDER, 1,
		             1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
		             new Insets(10, 10, 10, 10), 0, 0);

		// Add normaliser label and text to main panel
		addToGridBag(mainPanel, normaliserLabel, 0, 3, GridBagConstraints.RELATIVE, 
		             GridBagConstraints.RELATIVE, 0.0, 0.0, GridBagConstraints.NORTHWEST, 
		             GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0);
		addToGridBag(mainPanel, normaliserText, 1, 3, GridBagConstraints.REMAINDER, 
		             GridBagConstraints.RELATIVE, 1.0, 0.0, GridBagConstraints.NORTHWEST, 
		             GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0);
		
		// Add message text to main panel
		addToGridBag(mainPanel, messageSP, 0, 4, GridBagConstraints.REMAINDER, 
		             GridBagConstraints.REMAINDER, 1.0, 1.0, GridBagConstraints.NORTHWEST, 
		             GridBagConstraints.BOTH, new Insets(10, 5, 10, 5), 0, 0);

		// Content pane layout
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		// Action Listeners
		
		closeButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				closeWindow();
			}
			
		});
		
		viewButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				viewXenaFile();
			}
			
		});
	}
		
	private void closeWindow()
	{	
		this.setVisible(false);
		this.dispose();
		
		// Garbage collection requested here as large normalised
		// files can take up a lot of memory
		System.gc();
	}
	
	/**
	 * Display the Xena output file in a NormalisedObjectViewFrame.
	 *
	 */
	private void viewXenaFile()
	{
		XenaView xenaView;
		try
		{
			File xenaFile = new File(results.getDestinationDirString() + 
			                         File.separator +
			                         results.getOutputFileName());
			
			NormalisedObjectViewFactory novFactory =
				new NormalisedObjectViewFactory(xenaInterface);
			
			xenaView = novFactory.getView(xenaFile);
			
			NormalisedObjectViewFrame viewFrame =
				new NormalisedObjectViewFrame(xenaView,
				                              xenaInterface,
				                              xenaFile);
			
			// Display frame
			viewFrame.setLocation(this.getX()+50, this.getY()+50);
			viewFrame.setVisible(true);
		}
		catch (XenaException e)
		{
			handleXenaException(e);
		}
	}

	/**
	 * Displays a message dialog containing the given exception
	 * @param ex
	 */
	private void handleXenaException(Exception ex)
	{		
		ex.printStackTrace();
		JOptionPane.showMessageDialog(this, 
		                              ex.getMessage(),
		                              "Xena",
		                              JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Convenience method for adding a component to a container
	 * which is using a GridBagLayout
	 * 
	 * @param container
	 * @param component
	 * @param gridx
	 * @param gridy
	 * @param gridwidth
	 * @param gridheight
	 * @param weightx
	 * @param weighty
	 * @param anchor
	 * @param fill
	 * @param insets
	 * @param ipadx
	 * @param ipady
	 */
    private void addToGridBag(Container container,
							  Component component,
							  int gridx,
							  int gridy,
							  int gridwidth,
							  int gridheight,
							  double weightx,
							  double weighty,
							  int anchor,
							  int fill,    						  
							  Insets insets,
							  int ipadx,
							  int ipady)
	{
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
												gridwidth, gridheight,
												weightx, weighty,
												anchor, fill, 
												insets, 
												ipadx,
												ipady);
		container.add(component, gbc);
	}

	
	
	// Test harness for NormaliserResultsFrame
	public static void main(String[] args)
	{
		NormaliserResults results = new NormaliserResults();
		
		
		results.setInputSystemId("[file:/hsdfsdh fusdhfuahsdfas doufhasuodhf aousdh " +
		                         "foaushdfsauodh fuoashd fuoahsdf],[file sdfoh " +
		                         "asuodfh oausdh fuoasdh uofahsd uofha suodfhaousdh]" );
		results.setOutputFileName("546423564dfs76.xena");
		results.setDestinationDirString("d:\\workspace\\xena\\dest");
		
		NormaliserResultsFrame frame = new NormaliserResultsFrame(results,
		                                                          new Xena());
		frame.setLocation(150, 150);
		frame.setVisible(true);
	}
	
}
