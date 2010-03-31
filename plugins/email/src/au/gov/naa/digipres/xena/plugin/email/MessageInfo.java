/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 14/03/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.email;

import java.util.Date;

public class MessageInfo {
	private String fromAddress;
	private String toAddress;
	private String subject;
	private Date emailDate;
	private String outputFile;

	/**
	 * @param fromAddress
	 * @param toAddress
	 * @param subject
	 * @param emailDate
	 * @param outputFile
	 */
	public MessageInfo(String fromAddress, String toAddress, String subject, Date emailDate, String outputFile) {
		// TODO Auto-generated constructor stub
		this.fromAddress = fromAddress;
		this.toAddress = toAddress;
		this.subject = subject;
		this.emailDate = emailDate;
		this.outputFile = outputFile;
	}

	@Override
    public String toString() {
		return "From: " + fromAddress + "\n" + "To: " + toAddress + "\n" + "Subject: " + subject + "\n" + "Date: " + emailDate + "\n";
	}

	/**
	 * @return Returns the fromAddress.
	 */
	public String getFromAddress() {
		return fromAddress;
	}

	/**
	 * @param fromAddress The fromAddress to set.
	 */
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	/**
	 * @return Returns the receivedDate.
	 */
	public Date getEmailDate() {
		return emailDate;
	}

	/**
	 * @param receivedDate The receivedDate to set.
	 */
	public void setEmailDate(Date receivedDate) {
		this.emailDate = receivedDate;
	}

	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return Returns the toAddress.
	 */
	public String getToAddress() {
		return toAddress;
	}

	/**
	 * @param toAddress The toAddress to set.
	 */
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	/**
	 * @return Returns the outputFile.
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * @param outputFile The outputFile to set.
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}
