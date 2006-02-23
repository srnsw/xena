/*
 * Created on 30/11/2005
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.viewer;

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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

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
public class ViewerPreferencesDialog extends JDialog
{
	private static final String DIALOG_TITLE = "XenaViewer Preferences";
	
	private String pluginDir;
	private String xenaDestDir;
	
	private JTextField pluginTF;
	private JTextField xenaDestTF;
	
	private boolean approved = false;

	public ViewerPreferencesDialog(Frame owner) throws HeadlessException
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
		
		prefsPanel.setBorder(new TitledBorder(new EtchedBorder(),
		                                      DIALOG_TITLE));
		prefsPanel.setLayout(new GridLayout(2, 1));
		
		JLabel pluginLabel = new JLabel("Xena plugins directory:");
		JLabel xenaDestLabel = new JLabel("Xena destination directory:");
		
		pluginTF = new JTextField(30);
		xenaDestTF = new JTextField(30);
		
		JButton pluginBrowseButton = new JButton("Browse");
		JButton xenaDestBrowseButton = new JButton("Browse");
		
		JPanel pluginPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pluginPanel.add(pluginLabel);
		pluginPanel.add(pluginTF);
		pluginPanel.add(pluginBrowseButton);
		
		JPanel xenaDestPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		xenaDestPanel.add(xenaDestLabel);
		xenaDestPanel.add(xenaDestTF);
		xenaDestPanel.add(xenaDestBrowseButton);
		
		prefsPanel.add(pluginPanel);
		prefsPanel.add(xenaDestPanel);
		
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
		
		pluginBrowseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				String chosenDir = getChosenDir(pluginDir);
				if (chosenDir != null)
				{
					setPluginDir(chosenDir);
				}
			}
			
		});
	
		xenaDestBrowseButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				String chosenDir = getChosenDir(xenaDestDir);
				if (chosenDir != null)
				{
					setXenaDestDir(chosenDir);
				}
			}
			
		});

		this.pack();
	}
	
	/**
	 * Displays a file chooser, starting at the given directory.
	 * Returns the chosen directory, or null if no choice made.
	 * 
	 * @param currentDir
	 * @return
	 */
	private String getChosenDir(String currentDir)
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File(currentDir));
		int retVal = fileChooser.showOpenDialog(this);
		
		// We have returned from the file chooser
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile().getPath();
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
		this.pluginTF.setText(pluginDir);
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
		this.xenaDestTF.setText(xenaDestDir);
	}

	/**
	 * @return Returns the approved.
	 */
	public boolean isApproved()
	{
		return approved;
	}
	

}
