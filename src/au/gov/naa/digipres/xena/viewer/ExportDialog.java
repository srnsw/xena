/*
 * Created on 23/02/2007
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.util.FileAndDirectorySelectionPanel;
import au.gov.naa.digipres.xena.util.ProgressDialog;

public class ExportDialog extends JDialog
{
	private FileAndDirectorySelectionPanel itemSelectionPanel;
	private JTextField outputDirField;
	private Xena xena;
	
	public ExportDialog(Frame parent, Xena xena)
	{
		super(parent, "Export", true);
		this.xena = xena;
		initGUI();
	}
	
	private void initGUI()
	{
		this.setSize(500, 400);
		
		itemSelectionPanel = new FileAndDirectorySelectionPanel(new XenaFileFilter());
    	TitledBorder itemsBorder = new TitledBorder(new EtchedBorder(),"Items to Export");
    	itemsBorder.setTitleFont(itemsBorder.getTitleFont().deriveFont(13.0f));
    	itemSelectionPanel.setBorder(itemsBorder);
		
//		JLabel outputDirLabel = new JLabel("Output directory:");
		outputDirField = new JTextField(40);
		JButton browseButton = new JButton("Browse");
		JPanel outputDirPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    	TitledBorder outputDirBorder = new TitledBorder(new EtchedBorder(),"Output Directory");
    	outputDirBorder.setTitleFont(itemsBorder.getTitleFont().deriveFont(13.0f));
    	outputDirPanel.setBorder(outputDirBorder);
//		outputDirPanel.add(outputDirLabel);
		outputDirPanel.add(outputDirField);
		outputDirPanel.add(browseButton);
		
		JPanel inputPanel = new JPanel(new BorderLayout());
		inputPanel.add(itemSelectionPanel, BorderLayout.CENTER);
		inputPanel.add(outputDirPanel, BorderLayout.SOUTH);
		
		JButton exportButton = new JButton("Export");
		JButton cancelButton = new JButton("Cancel");
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(exportButton);
		buttonPanel.add(cancelButton);
		
		this.setLayout(new BorderLayout());
		this.add(inputPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		exportButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				doExport();
			}
		});
		
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				ExportDialog.this.setVisible(false);
			}
		});
		
		browseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				chooseOutputDir();
			}
		});
		
	}
	
	private void doExport()
	{
		List<File> itemList = itemSelectionPanel.getAllItems();
		List<File> fileList = getFileList(itemList);
		
		File outputDir = new File(outputDirField.getText());
		if (outputDir.exists() && outputDir.isDirectory())
		{
			ExportThread exportThread = new ExportThread(fileList, outputDir);
			exportThread.start();
		}
		else
		{
			JOptionPane.showMessageDialog(this,
			                              "Please enter a valid output directory.",
			                              "Invalid Output Directory",
			                              JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void chooseOutputDir()
	{
		JFileChooser chooser = new JFileChooser(new File(outputDirField.getText()));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retVal = chooser.showOpenDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			outputDirField.setText(chooser.getSelectedFile().getAbsolutePath());
		}		
	}
	
	/**
	 * Return the full list of files contained within the given list of files and directories.
	 * @param itemList
	 * @return
	 */
	private List<File> getFileList(List<File> itemList)
	{
		File[] itemArr = (File[])itemList.toArray(new File[0]);
		List<File> fileList = new ArrayList<File>();
		getFileSet(itemArr, fileList);
		return fileList;
	}
	
	
	/**
	 * Recursive method to retrieve the full list of files
	 * contained within the specified files and directories.
	 * The list of files is added to the given List.
	 * @param fileArr initial list of files and directories
	 * @param fileHashSet full list of files
	 */
	private void getFileSet(File[] itemArr, List<File> fileList)
	{
		for (File file : itemArr)
		{
			if (file.isDirectory())
			{
				getFileSet(file.listFiles(), fileList);
			}
			else
			{
				fileList.add(file);
			}
		}
	}
	
	
	private class ExportThread extends Thread
	{
		private List<File> fileList;
		private File exportDir;
		
		public ExportThread(List<File> fileList, File exportDir)
		{
			this.fileList = fileList;
			this.exportDir = exportDir;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			ProgressDialog progress = new ProgressDialog(ExportDialog.this, "Export Progress", 0, fileList.size());
			
			int exportCount = 0;
			for (File file : fileList)
			{
				try
				{
					progress.setNote("Exporting " + file.getName());
					xena.export(new XenaInputSource(file), exportDir, true);
					progress.setProgress(++exportCount);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(ExportDialog.this, 
					                              ex.getMessage(),
					                              "Xena",
					                              JOptionPane.ERROR_MESSAGE);
				}
			}
			
			progress.dispose();
			
			// Confirm export complete
			JOptionPane.showMessageDialog(ExportDialog.this,
			                              "All files exported successfully.",
			                              "Export Complete",
			                              JOptionPane.INFORMATION_MESSAGE);
			ExportDialog.this.setVisible(false);			
		}
		
	}

}
