package au.gov.naa.digipres.xena.plugin.postscript.test;

import java.io.File;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import au.gov.naa.digipres.xena.plugin.postscript.PostscriptFrame;

import com.softhub.ts.ViewFrame;
import com.softhub.ts.PostScriptPane;
import com.softhub.ps.util.Console;

public class ViewFrameTest extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JScrollPane scrollPane;
	private PostScriptPane pspane;
	//private ViewFrame viewFrame;
	private PostscriptFrame viewFrame;
	private JButton launchButton;
	private JFileChooser chooser;
	
	private File psfile;
	
	public ViewFrameTest() {
		super("ViewFrame Test");
		
		chooser = new JFileChooser(new File("/home/matt"));
	
		viewFrame = new PostscriptFrame();
		
		pspane = new PostScriptPane();
		scrollPane = new JScrollPane(viewFrame.getContentPane());
		chooser.showOpenDialog(chooser);
		psfile = chooser.getSelectedFile();
		
		//pspane.run(psfile);
		viewFrame.loadPSFile(psfile);
		
		
		launchButton = new JButton("Launch...");
		launchButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				viewFrame.setVisible(true);
			}
		});
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(launchButton, BorderLayout.SOUTH);
				
		
		//add(viewFrame, BorderLayout.CENTER);
		setBounds(100, 100, 200, 200);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		ViewFrameTest test = new ViewFrameTest();
	}

}
