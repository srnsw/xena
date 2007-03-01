/*
 * Created on 21/06/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.view.XenaView;
import au.gov.naa.digipres.xena.util.BinaryDeNormaliser;

public class AudioPlayerView extends XenaView
{
	private static final int LINE_BUFFER_SIZE = 128000;
	private static final int PLAYER_SAMPLE_SIZE_BITS = 16;
	
	private static final int STOPPED = 0;
	private static final int PLAYING = 1;
	private static final int PAUSED = 2;
	
	private int playerStatus = STOPPED;
	
	private File flacFile;
	private SourceDataLine sourceLine;

	public AudioPlayerView()
	{
		super();
		initGUI();
	}
	
	private void initGUI()
	{
		JPanel playerPanel = new JPanel(new FlowLayout());
		JButton playButton = new JButton("Play");
		JButton stopButton = new JButton("Stop");
		playerPanel.add(playButton);
		playerPanel.add(stopButton);
		this.add(playerPanel);
		
		
		playButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				playerStatus = PLAYING;
				
				if (sourceLine == null)
				{
					try
					{
						AudioInputStream flacStream = AudioSystem.getAudioInputStream(flacFile);
						initAudioLine(flacStream);						
					}
					catch (Exception ex)
					{
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
			}
			
		});
		
		stopButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				playerStatus = STOPPED;
			}
			
		});

	}
	
	private void initAudioLine(AudioInputStream audioStream) throws LineUnavailableException
	{
		
		AudioFormat audioFormat = audioStream.getFormat();
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
		
		if(!AudioSystem.isLineSupported(info))
		{
			AudioFormat	sourceFormat = audioFormat;
			AudioFormat	targetFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				sourceFormat.getSampleRate(),
				PLAYER_SAMPLE_SIZE_BITS,
				sourceFormat.getChannels(),
				sourceFormat.getChannels() * (PLAYER_SAMPLE_SIZE_BITS / 8),
				sourceFormat.getSampleRate(),
				sourceFormat.isBigEndian());
			
			audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
			audioFormat = audioStream.getFormat();
		}
		
		sourceLine = getSourceDataLine(audioFormat);
		sourceLine.start();
		
		LineWriterThread lwThread = new LineWriterThread(audioStream);
		lwThread.start();
	}

	// TODO: maybe can used by others. AudioLoop?
	// In this case, move to AudioCommon.
	private SourceDataLine getSourceDataLine(AudioFormat audioFormat) 
	throws LineUnavailableException
	{
		/*
		 *	Asking for a line is a rather tricky thing.
		 *	We have to construct an Info object that specifies
		 *	the desired properties for the line.
		 *	First, we have to say which kind of line we want. The
		 *	possibilities are: SourceDataLine (for playback), Clip
		 *	(for repeated playback)	and TargetDataLine (for
		 *	 recording).
		 *	Here, we want to do normal playback, so we ask for
		 *	a SourceDataLine.
		 *	Then, we have to pass an AudioFormat object, so that
		 *	the Line knows which format the data passed to it
		 *	will have.
		 *	Furthermore, we can give Java Sound a hint about how
		 *	big the internal buffer for the line should be. This
		 *	isn't used here, signaling that we
		 *	don't care about the exact size. Java Sound will use
		 *	some default value for the buffer size.
		 */
		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open();
		return line;
	}

	@Override
	public String getViewName()
	{
		return "audio";
	}

	@Override
	public boolean canShowTag(String tag) throws XenaException
	{
		String flacTag = AudioNormaliser.AUDIO_PREFIX + ":" + AudioNormaliser.FLAC_TAG;
		return tag.equals(flacTag);
	}
	
	
	
	public ContentHandler getContentHandler() throws XenaException 
	{
		FileOutputStream xenaTempOS = null;
        try
		{
    		flacFile = File.createTempFile("tmpview", ".flac");
    		flacFile.deleteOnExit();
	        xenaTempOS = new FileOutputStream(flacFile);
		}
		catch (IOException e)
		{
			throw new XenaException("Problem creating temporary xena output file", e);
		}
		BinaryDeNormaliser base64Handler = new BinaryDeNormaliser();
 		StreamResult result = new StreamResult(xenaTempOS);
 		base64Handler.setResult(result);
		return base64Handler;
	}	

	private class LineWriterThread extends Thread
	{
		private AudioInputStream audioStream;
		
		public LineWriterThread(AudioInputStream audioStream)
		{
			this.audioStream = audioStream;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			try
			{
				int bytesRead;
				byte[] buffer = new byte[LINE_BUFFER_SIZE];
				
				while (true)
				{
					if (playerStatus == PLAYING)
					{
						if (0 < (bytesRead = audioStream.read(buffer)))
						{
							sourceLine.write(buffer, 0, bytesRead);
						}
					}
					else
					{
						try
						{
							sleep(10);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			catch (IOException iex)
			{
				// Probably should do some kind of handling here...
				iex.printStackTrace();
			}
		}
		
		
	}
}

