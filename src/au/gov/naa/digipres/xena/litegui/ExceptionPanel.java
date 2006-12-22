/*
 * Created on 5/10/2004
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
/**
 * 
 * An extension of JPanel to show an exception and a short explanation.
 * 
 * @author dan
 */
public class ExceptionPanel extends JPanel {

	private JLabel titleLabel = null;
	private JLabel detailsLabel = null;
	private JTextArea detailsTextArea = null;
	private final String explanation;
	/**
	 * This is the default constructor
	 */
	public ExceptionPanel() {
		this((Throwable)null, "");
	}
    
    public ExceptionPanel(Throwable t, String explanation){
        super();
        this.explanation = explanation;
        initialize();
        
        detailsTextArea.setText(t.toString());
        detailsTextArea.setCaretPosition(0);
    }
    
    public ExceptionPanel(String error, String explanation){
        super();
        this.explanation = explanation;
        initialize();
        if (error != null) {
            detailsTextArea.setText(error);
            detailsTextArea.setCaretPosition(0);
        }
    }
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private  void initialize() {
		detailsLabel = new JLabel();
		titleLabel = new JLabel();
		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		this.setSize(388, 200);
		gridBagConstraints5.gridx = 0;
		gridBagConstraints5.gridy = 0;
		gridBagConstraints5.gridwidth = 2;
		gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints5.insets = new java.awt.Insets(6,6,0,6);
		titleLabel.setText(explanation); //$NON-NLS-1$
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.gridy = 1;
		gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHEAST;
		gridBagConstraints6.insets = new java.awt.Insets(6,6,0,0);
		detailsLabel.setText("Details:"); //$NON-NLS-1$
		gridBagConstraints7.gridx = 1;
		gridBagConstraints7.gridy = 1;
		gridBagConstraints7.weightx = 1.0;
		gridBagConstraints7.weighty = 1.0;
		gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints7.insets = new java.awt.Insets(6,6,6,6);
		this.add(titleLabel, gridBagConstraints5);
		this.add(detailsLabel, gridBagConstraints6);
		this.add(new JScrollPane(getDetailsTextArea()), gridBagConstraints7);
	}
	/**
	 * This method initializes detailsTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	private JTextArea getDetailsTextArea() {
		if (detailsTextArea == null) {
			detailsTextArea = new JTextArea();
			detailsTextArea.setLineWrap(true);
			detailsTextArea.setWrapStyleWord(true);
			detailsTextArea.setEditable(false);
		}
		return detailsTextArea;
	}
 }  //  @jve:decl-index=0:visual-constraint="12,99"
