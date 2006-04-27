/*
 * Created on 10/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A dialog which will display a method for modifying the property value for each 
 * XenaProperty in the given list (for example a simple text entry field for String properties, a text entry field
 * and a Browse button for File properties, etc).
 * @author justinw5
 * created 10/04/2006
 * xena
 * Short desc of class:
 */
public class PropertiesDialog extends JDialog
{
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	private PropertiesManager manager;
	private List<PropertyValuePanel> panelList;

	/**
	 * Create a new PropertiesDialog object, with a Frame as the dialog's parent
	 * @param parent
	 * @param properties
	 * @param manager
	 * @param title
	 */
	public PropertiesDialog(Frame parent,
						    List<XenaProperty> properties,
			 			    PropertiesManager manager,
			 			    String title)
	{
		super(parent, title, true);
		this.manager = manager;
		initGUI(properties);		
	}
	
	/**
	 * Create a new PropertiesDialog object, with a Dialog as the dialog's parent
	 * @param parent
	 * @param properties
	 * @param manager
	 * @param title
	 */
	public PropertiesDialog(Dialog parent,
		    				List<XenaProperty> properties,
		    				PropertiesManager manager,
		    				String title)
	{
		super(parent, title, true);
		this.manager = manager;
		initGUI(properties);		
	}


	/**
	 * Initialise and layout the PropertiesDialog components, based on the List
	 * of XenaProperties. A PropertyValuePanel is created for each XenaProperty,
	 * and each of these panels is added to the main panel using a GridBagLayout.
	 * @param properties
	 */
	private void initGUI(List<XenaProperty> properties)
	{
		this.setLayout(new BorderLayout());
		
		// Initialise properties panels
		JPanel mainPanel = new JPanel(new GridBagLayout());
		panelList = new ArrayList<PropertyValuePanel>();
		
		if (properties != null && properties.size() > 0)
		{
			int count = 0;
			for (XenaProperty property : properties)
			{
				JLabel descLabel = new JLabel(property.getDescription() + ": ");
				PropertyValuePanel valuePanel = new PropertyValuePanel(property);
				
				int gridY = 1;
				if (count == properties.size()-2) gridY = GridBagConstraints.RELATIVE;
				else if (count == properties.size()-1) gridY = GridBagConstraints.REMAINDER;
								
				addToGridBag(mainPanel, descLabel, 
				             0, count, GridBagConstraints.RELATIVE, gridY,
				             0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				             new Insets(8, 8, 0, 0), 0, 0);
				
				addToGridBag(mainPanel, valuePanel, 
				             1, count, GridBagConstraints.REMAINDER, gridY,
				             1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				             new Insets(8, 5, 0, 5), 0, 0);
				
				
				
				panelList.add(valuePanel);
				count++;
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
		
        // We don't want the window to be resizable, but we also want the icon
		// to appear (using setResizable(false) makes the icon disappear)...
		// so just pack every time the window is resized
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(ComponentEvent event)
			{
				PropertiesDialog.this.pack();
			}
		});

		
	}
	
	/**
	 * Add the given component to the given container using the given GridBagLayout constraint parameters
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
    private void addToGridBag(Container container, Component component,
			  				  int gridx, int gridy, int gridwidth, int gridheight,
			  				  double weightx, double weighty, int anchor, int fill,    						  
			  				  Insets insets, int ipadx, int ipady)
	{
		GridBagConstraints gbc = 
			new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
								   weightx, weighty, anchor, fill, 
								   insets, ipadx, ipady);
		container.add(component, gbc);
	}
	
	/**
	 * Called when the OK button is pressed. This will call the validate method on
	 * each property. If all properties validate successfully, then the property is
	 * set to the new value. If not, an error message is displayed.
	 * 
	 * If a PropertyMessageException is thrown, this means that the property has validated
	 * successfully, but a message should still be displayed to the user.
	 *
	 */
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
			catch (PropertyMessageException e)
			{
				displayMessage(e);
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
	
	private void displayMessage(PropertyMessageException pmEx)
	{
		logger.finer(pmEx.getMessage());
		JOptionPane.showMessageDialog(this, 
		                              pmEx.getMessage(),
		                              this.getTitle() + " Message",
		                              JOptionPane.INFORMATION_MESSAGE);
	}
	

}
