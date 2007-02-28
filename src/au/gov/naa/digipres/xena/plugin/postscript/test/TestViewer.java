
package au.gov.naa.digipres.xena.plugin.postscript.test;

/**
 *  @author Kamaj Jayakantha de Mel 
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import com.softhub.ts.ViewFrame;

public class TestViewer {

    public TestViewer() {
    	//Empty constructor    
    }    
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        JButton tempButton = new JButton("SHOW PS File");
        tempButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
			
				ViewFrame frame = new ViewFrame();
				frame.openAction(e, "c:\\golfer.ps");
				frame.setVisible(true);
			}
        });
        
        JPanel pannel = new JPanel();
        pannel.add(tempButton);
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JRootPane root = frame.getRootPane();
        root.setDefaultButton(tempButton);
        
        Container container = frame.getContentPane();
        container.add(pannel,BorderLayout.CENTER);
        frame.setSize(200,100);
        frame.setVisible(true);
        
    }
}
