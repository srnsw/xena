package org.jfr.examples;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.jfr.parser.GenericStreamDecoder;
import org.jpedal.color.PdfColor;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.PdfPageData;
import org.jpedal.render.DynamicVectorRenderer;

public class FontDisplay {

	int pageWidth =600, pageHeight =800,rotation=0,insetW=5,insetH=5;

	float scaling=1.0f;

	String font;

	JpedalLabel d[] = new JpedalLabel[62];
	
	int fontSize=48;

	public void setupViewer(){

		final GridBagConstraints gbc = new GridBagConstraints();
		final JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new GridBagLayout());
		d[1] = new JpedalLabel();
		final JComboBox combo = new JComboBox(d[1].getFonts());
		combo.setSelectedIndex(1);
		font = (String) combo.getSelectedItem();
		combo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				font = (String) combo.getSelectedItem();
				try {
					drawMatrix();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		for(int i=0; i!=62; i++){
			gbc.gridx = (i%10);
			gbc.gridy = (i/10)+1;
			d[i] = new JpedalLabel();
			frame.add(d[i], gbc);
		}

		frame.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {System.exit(1);}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});
		
		gbc.gridy=0;
		gbc.gridx=0;
		frame.getContentPane().add(combo, gbc);
		
		drawMatrix();
		
		//frame.setMinimumSize(new Dimension(1070,770));
		//frame.setMaximumSize(new Dimension(1070,770));
		//frame.setPreferredSize(new Dimension(1070,770));
		frame.setSize(1070, 770);
		frame.setVisible(true);

	}

	private void drawMatrix(){
		

		for(int i=0; i!=10; i++){
			d[i].setFont(font, fontSize);
			d[i].setText(String.valueOf(i));
		}

		for(int i=0; i!=26; i++){
			d[i+10].setFont(font, fontSize);
			d[i+10].setText(String.valueOf(((char) ('a' + i))));
		}

		for(int i=0; i!=26; i++){
			d[i+36].setFont(font, fontSize);
			d[i+36].setText(String.valueOf(((char) ('A' + i))));
			
		}		
	}

}
