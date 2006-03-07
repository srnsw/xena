/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PropertiesDialog extends JDialog
{
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private PropertiesManager manager;
	private List<PropertyValuePanel> panelList;

	public PropertiesDialog(Frame parent,
						    List<XenaProperty> properties,
			 			    PropertiesManager manager,
			 			    String title)
	{
		super(parent, title, true);
		this.manager = manager;
		initGUI(properties);		
	}
	
	public PropertiesDialog(Dialog parent,
		    				List<XenaProperty> properties,
		    				PropertiesManager manager,
		    				String title)
	{
		super(parent, title, true);
		this.manager = manager;
		initGUI(properties);		
	}


	
	private void initGUI(List<XenaProperty> properties)
	{
		this.setLayout(new BorderLayout());
		
		// Initialise properties panels
		JPanel mainPanel = new JPanel(new GridLayout(properties.size(), 2));
		panelList = new ArrayList<PropertyValuePanel>();
		if (properties != null && properties.size() > 0)
		{
			for (XenaProperty property : properties)
			{
				JLabel descLabel = new JLabel(property.getDescription() + ": ");
				PropertyValuePanel valuePanel = new PropertyValuePanel(property);
				mainPanel.add(descLabel);
				mainPanel.add(valuePanel);
				
				panelList.add(valuePanel);
			}
		}
		else
		{
			// Handle null or empty properties list
			JLabel noPropertiesLabel = new JLabel("No properties available");
			this.add(noPropertiesLabel, BorderLayout.CENTER);
		}
		
		// Initialise buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);		
		
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		cancelButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				PropertiesDialog.this.setVisible(false);
			}
			
		});
		
		okButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				saveProperties();
			}
			
		});
		
	}
	
	private void saveProperties()
	{
		for (PropertyValuePanel panel : panelList)
		{
			XenaProperty property = panel.getProperty();
			
			try
			{
				property.validate(panel.getValue());
			}
			catch (InvalidPropertyException e)
			{
				displayException(e);
				return;
			}
		}
		
		for (PropertyValuePanel panel : panelList)
		{
			XenaProperty property = panel.getProperty();
			property.setValue(panel.getValue());
			manager.saveProperty(property);
		}

		// If we reach here, we have finished with the dialog
		PropertiesDialog.this.setVisible(false);		
	}

	private void displayException(Exception ex)
	{
		logger.log(Level.FINER, ex.toString(), ex);
		JOptionPane.showMessageDialog(this, 
		                              ex.getMessage(),
		                              this.getTitle() + " Error",
		                              JOptionPane.ERROR_MESSAGE);
	}
	
	

}
