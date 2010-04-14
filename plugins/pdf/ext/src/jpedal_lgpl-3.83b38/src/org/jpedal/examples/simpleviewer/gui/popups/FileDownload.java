package org.jpedal.examples.simpleviewer.gui.popups;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;
import org.jpedal.io.ObjectStore;

public class FileDownload {
	//Load file from URL into file then open file
	File tempURLFile;

	//Components required to make the download window for online pdfs
	boolean downloadCreated = false;
	JFrame download = null;
	JPanel p;
	JProgressBar pb;
	JLabel downloadMessage;
	JLabel downloadFile;
	JLabel turnOff;
	int downloadCount = 0;
	boolean visible = true;
	String progress = "";

//	Coords to display the window at
	Point coords = null;

	public FileDownload(boolean showWindow, Point pos){

		visible = showWindow;
		coords = pos;
		
		if(visible){
			download = new JFrame();
			p = new JPanel(new GridBagLayout());
			pb = new JProgressBar();
			downloadMessage = new JLabel();
			downloadFile = new JLabel();
			turnOff = new JLabel();

			download.setResizable(false);
			download.setTitle(Messages.getMessage("PageLayoutViewMenu.DownloadWindowTitle"));

			//BoxLayout bl = new BoxLayout(p, BoxLayout.X_AXIS);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill=GridBagConstraints.BOTH;
			gbc.gridy=0;
			gbc.gridx=0;
			gbc.gridwidth=2;
			//gbc.fill = GridBagConstraints.BOTH;
			downloadFile.setSize(250,downloadFile.getHeight());
			downloadFile.setMinimumSize(new Dimension(250,15));
			downloadFile.setMaximumSize(new Dimension(250,15));
			downloadFile.setPreferredSize(new Dimension(250,15));
			p.add(downloadFile, gbc);

			gbc.gridy=1;
			downloadMessage.setSize(250,downloadFile.getHeight());
			downloadMessage.setMinimumSize(new Dimension(250,15));
			downloadMessage.setMaximumSize(new Dimension(250,15));
			downloadMessage.setPreferredSize(new Dimension(250,15));
			p.add(downloadMessage,gbc);

			gbc.gridy=2;
			pb.setSize(260,downloadFile.getHeight());
			pb.setMinimumSize(new Dimension(260,20));
			pb.setMaximumSize(new Dimension(260,20));
			pb.setPreferredSize(new Dimension(260,20));
			p.add(pb,gbc);

			gbc.gridy=3;
			p.add(turnOff,gbc);

			download.getContentPane().add(p);
			download.setSize(320, 100);

			downloadCreated = true;
		}
	}

	public File createWindow(String pdfUrl){		
		URL url;
		InputStream is;

		try {
            int fileLength = 0;

            String str;
            if(pdfUrl.startsWith("jar:/")) {
                str= "file.pdf";//SimpleViewer.file;
                is=this.getClass().getResourceAsStream(pdfUrl.substring(4));

                //fileLength=is.available();
                //System.out.println(str+">>"+pdfUrl.substring(4)+"<<>>"+is);
            }else{
                url = new URL(pdfUrl);

			    is = url.openStream();

                str=url.getPath().substring(url.getPath().lastIndexOf('/')+1);
                fileLength = url.openConnection().getContentLength();

            }
			final String filename = str;

			tempURLFile = File.createTempFile(filename.substring(0, filename.lastIndexOf('.')), filename.substring(filename.lastIndexOf('.')),new File(ObjectStore.temp_dir));

			FileOutputStream fos = new FileOutputStream(tempURLFile);

			//<start-adobe><start-thin>
			if(visible){
				download.setLocation((coords.x-(download.getWidth()/2)), (coords.y-(download.getHeight()/2)));
				download.setVisible(true);
			}
			//<end-thin><end-adobe>

			if(visible){
				pb.setMinimum(0);
				pb.setMaximum(fileLength);
				//saveLocal.setEnabled(false);

				String message = Messages.getMessage("PageLayoutViewMenu.DownloadWindowMessage");
				message = message.replaceAll("FILENAME", filename);
				downloadFile.setText(message);

				Font f = turnOff.getFont();
				turnOff.setFont(new Font(f.getName(), f.getStyle(),  8));
				turnOff.setAlignmentY(JLabel.RIGHT_ALIGNMENT);
				turnOff.setText(Messages.getMessage("PageLayoutViewMenu.DownloadWindowTurnOff"));
			}
			//download.setVisible(true);
			// Download buffer
			byte[] buffer = new byte[4096];
			// Download the PDF document
			int read;
			int current = 0;

			String rate = "kb"; //mb
			int mod = 1000; //1000000

			if(fileLength>1000000){
				rate = "mb";
				mod = 1000000;
			}

			if(visible){
				progress = Messages.getMessage("PageLayoutViewMenu.DownloadWindowProgress");
				if(fileLength<1000000)
					progress = progress.replaceAll("DVALUE", (fileLength/mod)+" "+rate);
				else{
					String fraction = String.valueOf(((fileLength % mod) / 10000));
					if(((fileLength%mod)/10000)<10)
						fraction = "0"+fraction;
					
					progress = progress.replaceAll("DVALUE", (fileLength/mod)+"."+fraction+" "+rate);
				}
			}

			while ((read = is.read(buffer)) != -1) {
				current = current + read;
				downloadCount = downloadCount+read;

				if(visible){
					if(fileLength<1000000)
						downloadMessage.setText(progress.replaceAll("DSOME", (current/mod)+" "+rate));
					else{
						String fraction = String.valueOf(((current % mod) / 10000));
						if(((current%mod)/10000)<10)
							fraction = "0"+fraction;

						downloadMessage.setText(progress.replaceAll("DSOME", (current/mod)+"."+fraction+" "+rate));
					}
					pb.setValue(current);

					download.repaint();
				}

				fos.write(buffer, 0, read);
			}
			fos.flush();
			// Close streams
			is.close();
			fos.close();

			//File completed download, show the save button
			if(visible)
				downloadMessage.setText("Download of "+filename+" is complete.");
			//saveLocal.setEnabled(true);

		} catch (Exception e) {
			LogWriter.writeLog("[PDF] Exception " + e + " opening URL "+ pdfUrl);
            e.printStackTrace();
		}

		if(visible)
			download.setVisible(false);

		return tempURLFile;
	}

}
