/*
 * Created on 13/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.kernel.properties;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel containing specific component(s) to change the value of this property.
 * 
 * @author justinw5
 * created 10/04/2006
 * xena
 * Short desc of class:
 */
public class PropertyValuePanel extends JPanel
{
	private JComponent component;
	private XenaProperty property;

	/**
	 * Create a new PropertyValuePanel based on the given XenaProperty
	 * @param XenaProperty representing a property of a plugin
	 */
	public PropertyValuePanel(XenaProperty property)
	{
		super(new FlowLayout(FlowLayout.LEFT));
		this.property = property;
		initGUI();
	}

	/**
	 * Layout and initialise the components of the panel.
	 * Each type of property will have its own specific component to 
	 * change the value of the property.
	 *
	 */
	private void initGUI()
	{
		switch(property.getType())
		{
		case BOOLEAN_TYPE:
			JCheckBox checkbox = new JCheckBox();
			checkbox.setSelected(new Boolean(property.getValue()));
			component = checkbox;
			this.add(component);
			break;
		case DIR_TYPE:
		case FILE_TYPE:
			JTextField fileField = new JTextField(30);
			fileField.setText(property.getValue());
			component = fileField;
			JButton browseButton = new JButton("Browse");
			browseButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e)
				{
					handleBrowse((JTextField)component);
				}
				
			});
			this.add(component);
			this.add(browseButton);
			break;
		case INT_TYPE:
			JTextField intField = new JTextField(10);
			intField.setText(property.getValue());
			component = intField;
			this.add(component);
			break;
		case STRING_TYPE:
			JTextField stringField = new JTextField(30);
			stringField.setText(property.getValue());
			component = stringField;
			this.add(component);
			break;
		case SINGLE_OPTION_TYPE:
			List<Object> optionList = property.getListOptions();
			if (optionList != null && !optionList.isEmpty())
			{
				JComboBox combo = new JComboBox(optionList.toArray());
				
				// Check that the current value is actually in the list, and if so, set it.
				// If not, the first entry in the list will be selected.
				if (optionList.contains(property.getValue()))
				{
					combo.setSelectedItem(property.getValue());
				}				
				
				component = combo;
				this.add(component);
			}
			else
			{
				throw new IllegalArgumentException("Invalid option list for " + property.getName() + " property");
			}
			break;
		case MULTI_OPTION_TYPE:
			// TODO: MULTI_OPTION property type
			throw new IllegalArgumentException("Multiple Option Type not yet implemented");
		}
	}
	
	/**
	 * Method called when the browse button is pressed for File and Directory
	 * property types.
	 * @param field JTextField which will display the file or directory
	 * selected in this method
	 */
	protected void handleBrowse(JTextField field)
	{
		JFileChooser chooser = new JFileChooser(property.getValue().toString());
		chooser.setMultiSelectionEnabled(false);
		if (property.getType() == XenaProperty.PropertyType.DIR_TYPE)
		{
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else if (property.getType() == XenaProperty.PropertyType.FILE_TYPE)
		{
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		else
		{
			throw new IllegalStateException("Cannot call handleBrowse with a property " +
			                                "type other than DIR or FILE");
		}
		
		int retValue = chooser.showOpenDialog(this);
		
		// Have returned from file dialog
		if (retValue == JFileChooser.APPROVE_OPTION)
		{
			field.setText(chooser.getSelectedFile().getAbsolutePath());
		}
		
	}

	/**
	 * Returns the current value of this property as a String.
	 * @return current property value as currently displayed in the panel
	 */
	public String getValue()
	{
		String retStr = "";
		switch(property.getType())
		{
		case BOOLEAN_TYPE:
			JCheckBox checkbox = (JCheckBox)component;
			retStr = checkbox.isSelected() 
				? Boolean.TRUE.toString() 
				: Boolean.FALSE.toString();
			break;
		case DIR_TYPE:
		case FILE_TYPE:
			JTextField fileField = (JTextField)component;
			retStr = fileField.getText();
			break;
		case INT_TYPE:
			JTextField intField = (JTextField)component;
			retStr = intField.getText();
			break;
		case STRING_TYPE:
			JTextField stringField = (JTextField)component;
			retStr = stringField.getText();
			break;
		case SINGLE_OPTION_TYPE:
			JComboBox combo = (JComboBox)component;
			retStr = combo.getSelectedItem().toString();
			break;
		case MULTI_OPTION_TYPE:
			// TODO: MULTI_OPTION property type
			throw new IllegalArgumentException("Multiple Option Type not yet implemented");
		}
		return retStr;
	}

	/**
	 * @return Returns the property.
	 */
	public XenaProperty getProperty()
	{
		return property;
	}

	/**
	 * @param property The property to set.
	 */
	public void setProperty(XenaProperty property)
	{
		this.property = property;
	}

}
