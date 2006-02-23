package au.gov.naa.digipres.xena.gui;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.kernel.view.XenaView;


public class ChooseViewTreePanel extends JPanel {
	protected BorderLayout borderLayout1 = new BorderLayout();

	protected ChooseViewTree viewTree = new ChooseViewTree();

	protected JPanel jPanel1 = new JPanel();

	protected JButton cancelButton = new JButton();

	protected JButton okButton = new JButton();

	public ChooseViewTreePanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void showDialog(Frame parent, String title, boolean modal) {
		final JDialog d = new JDialog(parent, title, modal);
		d.getContentPane().add(this);
		getCancelButton().addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});

		getOkButton().addActionListener(
			new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});
		set(MainFrame.singleton().getSelectedFrame());
		d.setLocationRelativeTo(parent);
		d.pack();
		getOkButton().requestFocusInWindow();
		d.setVisible(true);
	}

	public void set(InternalFrame ifr) {
		viewTree.set(ifr);
	}

	public XenaView getView() {
		XenaView view = viewTree.getSelectedView();
		return view;
	}

	public JButton getOkButton() {
		return okButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	protected void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		cancelButton.setText("Cancel");
		okButton.setText("OK");

		this.add(viewTree, BorderLayout.CENTER);
		this.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(okButton, null);
		jPanel1.add(cancelButton, null);
	}
}
