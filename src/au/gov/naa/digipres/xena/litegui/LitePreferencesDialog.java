/*
 * Created on 06/12/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Simple dialog to set preferences for the Xena Viewer. Currently
 * there are only two preferences that can be set - the Xena plugin
 * directory, and the xena destination directory. Each of these options
 * has an associated entry field, and a browse button which will bring
 * up a file chooser that populates the appropriate entry field with the 
 * selected directory.
 * The entry fields can be pre-populated from the calling window, and
 * thus previously saved preferences can be automatically restored.
 * @author justinw5
 * created 1/12/2005
 * xena
 * Short desc of class:
 */
public class LitePreferencesDialog extends JDialog
{
	private static final String DIALOG_TITLE = "Xena Lite Preferences";
	
	private String pluginDir;
	private JTextField pluginTF;

	private String xenaDestDir;
	private JTextField xenaDestTF;

	private String xenaLogFile;	
	private JTextField xenaLogTF;
	
	private boolean approved = false;

	public LitePreferencesDialog(Frame owner) throws HeadlessException
	{
		super(owner);
		initGUI();
	}
	
	/**
	 * One-time GUI initialisation
	 */
	private void initGUI()
	{
		this.setModal(true);
		this.setTitle(DIALOG_TITLE);
		this.setResizable(false);

		JPanel prefsPanel = new JPanel(new BorderLayout());
		prefsPanel.setBorder(new EtchedBorder());
		prefsPanel.setLayout(new GridLayout(3, 1));
		
		
		// Plugin directory preference
		JLabel pluginLabel = new JLabel("Xena plugins directory:");
		pluginTF = new JTextField(30);
		JButton pluginBrowseButton = new JButton("Browse");
		JPanel pluginPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pluginPanel.setBorder(new EmptyBorder(6, 6, 0, 6));
		pluginPanel.add(pluginLabel);
		pluginPanel.add(pluginTF);
		pluginPanel.add(pluginBrowseButton);
		prefsPanel.add(pluginPanel);
		pluginBrowseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				String chosenDir = getChosenPath(pluginDir, true);
				if (chosenDir != null)
				{
					setPluginDir(chosenDir);
				}
			}
			
		});

		// Xena destination directory preference
		JLabel xenaDestLabel = new JLabel("Xena destination directory:");
		xenaDestTF = new JTextField(30);
		JButton xenaDestBrowseButton = new JButton("Browse");
		JPanel xenaDestPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		xenaDestPanel.setBorder(new EmptyBorder(4, 6, 0, 6));
		xenaDestPanel.add(xenaDestLabel);
		xenaDestPanel.add(xenaDestTF);
		xenaDestPanel.add(xenaDestBrowseButton);
		prefsPanel.add(xenaDestPanel);
		xenaDestBrowseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				String chosenDir = getChosenPath(xenaDestDir, true);
				if (chosenDir != null)
				{
					setXenaDestDir(chosenDir);
				}
			}
			
		});
		
		// Log file preference
		JLabel xenaLogLabel = new JLabel("Xena Log File:");
		xenaLogTF = new JTextField(30);
		JButton xenaLogBrowseButton = new JButton("Browse");
		JPanel xenaLogPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		xenaLogPanel.setBorder(new EmptyBorder(4, 6, 6, 6));
		xenaLogPanel.add(xenaLogLabel);
		xenaLogPanel.add(xenaLogTF);
		xenaLogPanel.add(xenaLogBrowseButton);
		prefsPanel.add(xenaLogPanel);
		xenaLogBrowseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				String chosenFile = getChosenPath(xenaLogFile, false);
				if (chosenFile != null)
				{
					setXenaLogFile(chosenFile);
				}
			}
			
		});
				
		// Main layout
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		this.add(prefsPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		// Action Listeners
		this.addWindowListener(new WindowAdapter(){

			public void windowClosing(WindowEvent e)
			{
				doCloseDialog();
			}
			
		});
		
		okButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				pluginDir = pluginTF.getText();
				xenaDestDir = xenaDestTF.getText();
				xenaLogFile = xenaLogTF.getText();
				approved = true;
				doCloseDialog();
			}
			
		});

		cancelButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				doCloseDialog();
			}
			
		});
				
		this.pack();
	}
	
	/**
	 * Displays a file chooser, starting at the given directory.
	 * Returns the chosen directory or file, or null if no choice made.
	 * 
	 * @param currentDir
	 * @param chooseDir
	 * @return
	 */
	private String getChosenPath(String currentDir, boolean chooseDir)
	{
		JFileChooser fileChooser = new JFileChooser();
		
		// If chooseDir is true, a directory is to be selected.
		// Otherwise, a file is to be selected.
		if (chooseDir)
		{
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else
		{
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		
		// Initialises the file chooser to start at the given directory
		fileChooser.setCurrentDirectory(new File(currentDir));
		
		int retVal = fileChooser.showOpenDialog(this);
		
		// We have returned from the file chooser
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile().toString();
		}
		else
		{
			return null;
		}
	}
	
	private void doCloseDialog()
	{
		this.setVisible(false);
	}

	/**
	 * @return Returns the pluginDir.
	 */
	public String getPluginDir()
	{
		return pluginDir;
	}

	/**
	 * @param pluginDir
	 * The pluginDir to set.
	 */
	public void setPluginDir(String pluginDir)
	{
		this.pluginDir = pluginDir;
		pluginTF.setText(pluginDir);
	}

	/**
	 * @return Returns the xenaDestDir.
	 */
	public String getXenaDestDir()
	{
		return xenaDestDir;
	}

	/**
	 * @param xenaDestDir
	 * The xenaDestDir to set.
	 */
	public void setXenaDestDir(String xenaDestDir)
	{
		this.xenaDestDir = xenaDestDir;
		xenaDestTF.setText(xenaDestDir);
	}

	/**
	 * @return Returns the xenaLogFile.
	 */
	public String getXenaLogFile()
	{
		return xenaLogFile;
	}

	/**
	 * @param xenaLogFile The xenaLogFile to set.
	 */
	public void setXenaLogFile(String xenaLogFile)
	{
		this.xenaLogFile = xenaLogFile;
		xenaLogTF.setText(xenaLogFile);
	}

	/**
	 * @return Returns the approved.
	 */
	public boolean isApproved()
	{
		return approved;
	}
	

}
