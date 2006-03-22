/*
 * Created on 21/02/2006
 * justinw5
 * 
 */
package au.gov.naa.digipres.xena.util;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ProgressDialog extends JDialog
{
    private long            startTime;
    private int             millisToDecideToPopup = 500;
    private int             millisToPopup = 2000;
    private int             minVal;
    private int             maxVal;
    private int             lastDisp;
    private int             reportDelta;
    
    private boolean dialogDisplayed = false;
	
	private JProgressBar myBar;
	private JTextArea statusText;

	public ProgressDialog(Frame owner,
						  String title,
						  int min,
						  int max) throws HeadlessException
	{
		super(owner, title, false);
				
		this.minVal = min;
		this.maxVal = max;
		
		initGUI(owner);

        reportDelta = (max - min) / 100;
        if (reportDelta < 1) reportDelta = 1;
		
		startTime = System.currentTimeMillis();
	}

    private void initGUI(Frame owner)
	{
    	// Used to make the text area look like a label
    	JLabel referenceLabel = new JLabel();
    	
		statusText = new JTextArea(3, 60);

		statusText.setEditable(false);
		statusText.setBackground(referenceLabel.getBackground());
		statusText.setWrapStyleWord(true);
		statusText.setLineWrap(true);
		statusText.setFont(referenceLabel.getFont());
		statusText.setBorder(new EmptyBorder(15, 15, 15, 15));

    	myBar = new JProgressBar(minVal, maxVal);    	
    	JPanel progressBarPanel = new JPanel();
    	progressBarPanel.add(myBar);
    	
    	this.setLayout(new BorderLayout());
    	this.add(statusText, BorderLayout.CENTER);
    	this.add(progressBarPanel, BorderLayout.SOUTH);
    	
    	this.setSize(400, 100);
    	this.setResizable(false);
    	this.setLocationRelativeTo(owner);
    	this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    	this.setAlwaysOnTop(true);
    	
	}

	/** 
     * Indicate the progress of the operation being monitored.
     * If the specified value is >= the maximum, the progress
     * monitor is closed. 
     * @param newValue an int specifying the current value, between the
     *        maximum and minimum specified for this component
     * @see #setMinimum
     * @see #setMaximum
     * @see #close
     */
    public void setProgress(int newValue) 
    {
        if (newValue >= maxVal) 
        {
            this.setVisible(false);
            this.dispose();
        }
        else if (newValue >= lastDisp + reportDelta) 
        {
            lastDisp = newValue;
            if (dialogDisplayed) 
            {
                myBar.setValue(newValue);
            }
            else 
            {
                long T = System.currentTimeMillis();
                long dT = (int)(T-startTime);
                if (dT >= millisToDecideToPopup) 
                {
                    int predictedCompletionTime;
                    if (newValue > minVal) 
                    {
                        predictedCompletionTime = (int)((long)dT *
                                                        (maxVal - minVal) /
                                                        (newValue - minVal));
                    }
                    else 
                    {
                        predictedCompletionTime = millisToPopup;
                    }
                    if (predictedCompletionTime >= millisToPopup) {
                    	this.setVisible(true);
                        myBar.setValue(newValue);
                        dialogDisplayed = true;
                    }
                }
            }
        }
    }
    
    public void setNote(String note)
    {
    	statusText.setText(note);
    }

	/**
	 * @return Returns the maxVal.
	 */
	public int getMaxVal()
	{
		return maxVal;
	}

	/**
	 * @param maxVal The maxVal to set.
	 */
	public void setMaxVal(int maxVal)
	{
		this.maxVal = maxVal;
	}

	/**
	 * @return Returns the millisToDecideToPopup.
	 */
	public int getMillisToDecideToPopup()
	{
		return millisToDecideToPopup;
	}

	/**
	 * @param millisToDecideToPopup The millisToDecideToPopup to set.
	 */
	public void setMillisToDecideToPopup(int millisToDecideToPopup)
	{
		this.millisToDecideToPopup = millisToDecideToPopup;
	}

	/**
	 * @return Returns the millisToPopup.
	 */
	public int getMillisToPopup()
	{
		return millisToPopup;
	}

	/**
	 * @param millisToPopup The millisToPopup to set.
	 */
	public void setMillisToPopup(int millisToPopup)
	{
		this.millisToPopup = millisToPopup;
	}

	/**
	 * @return Returns the minVal.
	 */
	public int getMinVal()
	{
		return minVal;
	}

	/**
	 * @param minVal The minVal to set.
	 */
	public void setMinVal(int minVal)
	{
		this.minVal = minVal;
	}
	
	

}
