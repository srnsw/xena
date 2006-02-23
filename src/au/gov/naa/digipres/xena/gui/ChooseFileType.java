package au.gov.naa.digipres.xena.gui;
import java.awt.BorderLayout;
import java.awt.Dialog;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * A Dialog for  selecting a  file type.
 * @author     Chris Bitmead
 * @created    1 July 2002
 */
public class ChooseFileType extends JDialog {

	protected JPanel panel1 = new JPanel();

	protected BorderLayout borderLayout1 = new BorderLayout();

	protected JOptionPane jOptionPane1 = new JOptionPane();

	protected JScrollPane jScrollPane1 = new JScrollPane();

	protected JList fileTypeList = new JList();

	public ChooseFileType(Object[] guessers) {
		super((Dialog)null, "Choose File Type", false);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void jbInit() throws Exception {
		panel1.setLayout(borderLayout1);
		fileTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jOptionPane1.add(jScrollPane1, null);
		jScrollPane1.getViewport().add(fileTypeList, null);
		getContentPane().add(panel1);
		panel1.add(jOptionPane1, BorderLayout.CENTER);
	}
}
