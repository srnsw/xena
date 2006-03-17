/*
 * Created on 15/03/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class SplashScreen 
{
	private JDialog splashDialog;
	private JTextArea logTextArea;
	private Handler logHandler;
	
	public SplashScreen(ImageIcon icon, String version)
	{
		initGUI(icon, version);
		logHandler = new SplashLogHandler();
	}
	
	private void initGUI(ImageIcon icon, String version)
	{
		JLabel logoLabel = new JLabel(icon);
		logTextArea = new JTextArea(3, 30);
		logTextArea.setEditable(false);
		logTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		JScrollPane logSP = new JScrollPane(logTextArea, 
		                                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		logSP.setBorder(new EmptyBorder(0, 0, 0, 0));

		
		JLabel versionLabel = new JLabel(version);
		versionLabel.setBackground(logTextArea.getBackground());
		versionLabel.setForeground(logTextArea.getForeground());
		versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD));
		versionLabel.setOpaque(true);
				
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(versionLabel, BorderLayout.NORTH);
		bottomPanel.add(logSP, BorderLayout.CENTER);
		bottomPanel.setBorder(new LineBorder(logTextArea.getBackground(), 6));
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new LineBorder(Color.BLACK));
		mainPanel.add(logoLabel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		splashDialog = new JDialog((Frame)null, "", false);
		splashDialog.setUndecorated(true);
		splashDialog.add(mainPanel, BorderLayout.CENTER);
		splashDialog.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension splashSize = splashDialog.getSize();
		splashDialog.setLocation((screenSize.width - splashSize.width) / 2, (screenSize.height - splashSize.height) / 2);
		splashDialog.pack();
	}
	
	public void setVisible(boolean show)
	{
		splashDialog.setVisible(show);
	}
	
	public void dispose()
	{
		splashDialog.dispose();
	}
	
	public Handler getLogHandler()
	{
		return logHandler;
	}
	
	
	private class SplashLogHandler extends Handler
	{
		private boolean handlerClosed = false;
		
		public SplashLogHandler()
		{
			this.setLevel(Level.ALL);
		}

		@Override
		public void publish(LogRecord record)
		{
			if (!handlerClosed)
			{
				logTextArea.append(record.getMessage() + "\n");
				logTextArea.setCaretPosition(logTextArea.getDocument().getLength()-1);
				logTextArea.repaint();
			}
		}

		@Override
		public void flush()
		{
			logTextArea.validate();
		}

		@Override
		public void close() throws SecurityException
		{
			handlerClosed = true;
		}
		
	}
	

}
