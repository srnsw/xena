package au.gov.naa.digipres.xena.javatools;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class ListChooser extends JPanel {
	private BorderLayout borderLayout = new BorderLayout();

	private JSplitPane splitPane = new JSplitPane();

	private JPanel leftPanel = new JPanel();

	private BorderLayout leftBorderLayout = new BorderLayout();

	private JScrollPane leftScrollPane = new JScrollPane();

	private JList leftList = new JList();

	private JPanel leftButtonPanel = new JPanel();

	private JButton leftButton = new JButton();

	private JPanel rightPanel = new JPanel();

	private JScrollPane rightScrollPane = new JScrollPane();

	private JPanel rightButtonPanel = new JPanel();

	private JButton rightButton = new JButton();

	private BorderLayout rightBorderLayout = new BorderLayout();

	private JLabel leftLabel = new JLabel();

	private JList rightList = new JList();

	private JLabel rightLabel = new JLabel();

	private List leftToRightListeners = new ArrayList();

	private List rightToLeftListeners = new ArrayList();

	public ListChooser() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addLeftToRightActionListener(ActionListener l) {
		leftToRightListeners.add(l);
	}

	public void addRightToLeftActionListener(ActionListener l) {
		rightToLeftListeners.add(l);
	}

	protected void execute(List listeners) {
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			ActionListener l = (ActionListener)it.next();
			l.actionPerformed(null);
		}
	}

	void jbInit() throws Exception {
		leftList.setModel(new DefaultListModel());
		rightList.setModel(new DefaultListModel());
		this.setLayout(borderLayout);
		leftPanel.setLayout(leftBorderLayout);
		leftButton.setText("<<");
		leftButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				leftButton_actionPerformed(e);
			}
		});
		rightButton.setText(">>");
		rightButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rightButton_actionPerformed(e);
			}
		});
		rightPanel.setLayout(rightBorderLayout);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		leftLabel.setText("");
		rightLabel.setText("");
		this.add(splitPane, BorderLayout.CENTER);
		splitPane.add(leftPanel, JSplitPane.TOP);
		leftPanel.add(leftScrollPane, BorderLayout.CENTER);
		leftPanel.add(leftButtonPanel, BorderLayout.SOUTH);
		leftButtonPanel.add(leftButton, null);
		leftPanel.add(leftLabel, BorderLayout.NORTH);
		splitPane.add(rightPanel, JSplitPane.BOTTOM);
		rightPanel.add(rightButtonPanel, BorderLayout.SOUTH);
		rightButtonPanel.add(rightButton, null);
		rightPanel.add(rightScrollPane, BorderLayout.CENTER);
		rightPanel.add(rightLabel, BorderLayout.NORTH);
		rightScrollPane.getViewport().add(rightList, null);
		leftScrollPane.getViewport().add(leftList, null);
	}

	public void setLeftTitle(String v) {
		leftLabel.setText(v);
	}

	public void setRightTitle(String v) {
		rightLabel.setText(v);
	}

	void leftButton_actionPerformed(ActionEvent e) {
		Object[] o = rightList.getSelectedValues();
		for (int i = 0; i < o.length; i++) {
			((DefaultListModel)rightList.getModel()).removeElement(o[i]);
			((DefaultListModel)leftList.getModel()).addElement(o[i]);
		}
		if (o.length != 0) {
			execute(leftToRightListeners);
		}
	}

	void rightButton_actionPerformed(ActionEvent e) {
		Object[] o = leftList.getSelectedValues();
		for (int i = 0; i < o.length; i++) {
			((DefaultListModel)leftList.getModel()).removeElement(o[i]);
			((DefaultListModel)rightList.getModel()).addElement(o[i]);
		}
		if (o.length != 0) {
			execute(rightToLeftListeners);
		}
	}

	public JList getLeftList() {
		return leftList;
	}

	public JList getRightList() {
		return rightList;
	}

	public static void main(String[] args) {
		JFrame fr = new JFrame();
		ListChooser lc = new ListChooser();
		Object xxx = lc.getLeftList().getModel();
		DefaultListModel llm = new DefaultListModel();
		DefaultListModel rlm = new DefaultListModel();
		lc.getLeftList().setModel(llm);
		lc.getRightList().setModel(rlm);
		llm.addElement("Line 1");
		llm.addElement("Line 2");
		llm.addElement("Line 3");
		rlm.addElement("Row 1");
		rlm.addElement("Row 2");
		rlm.addElement("Row 3");
		lc.setLeftTitle("From");
		lc.setRightTitle("To");
		fr.getContentPane().add(lc);
		fr.pack();
		fr.setVisible(true);
	}

	public JButton getLeftButton() {
		return leftButton;
	}

	public JButton getRightButton() {
		return rightButton;
	}
}
