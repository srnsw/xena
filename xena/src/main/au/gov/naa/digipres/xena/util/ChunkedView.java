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

package au.gov.naa.digipres.xena.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.view.XenaView;

/**
 * A view that can display an XML document in chunks to avoid loading it all
 * into memory at the same time. Displays First, Next, Prev and Last buttons
 * at the top of the screen.
 *
 * The algorithm isn't particularly efficient in that you always have to
 * read from the beginning of the XML to find the right part. This is partly
 * because it is not particularly easy to do it any other way. Also, if the
 * file is small it is quick anyway. If it is not small, then you have to be
 * grateful that Xena doesn't just blow up from out of memory like it did
 * before. Hard to think of a good way of doing it better. We could I guess
 * have zillions of temp files each containing a different bit, but it has its
 * own problems.
 */
abstract public class ChunkedView extends XenaView {

	protected int currentChunk = 0;

	int totalChunks = 1;

	long minFreeMemory = -1;

	protected int linesPerChunk = -1;

	public ChunkedView() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public int getCurrentChunk() {
		return currentChunk;
	}

	public boolean displayChunkPanel() {
		return 1 < totalChunks;
	}

	public void setTotalChunks(int v) {
		this.totalChunks = v;
		ofTextField.setText(Integer.toString(v));
		this.remove(chunkButtonPanel);
		if (displayChunkPanel()) {
			this.add(chunkButtonPanel, java.awt.BorderLayout.NORTH);
		}
		enableButtons();
	}

	public long availableMemory() {
		Runtime rt = Runtime.getRuntime();
		long hardLimit = rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
		return hardLimit;
	}

	public void initMemory() {
		minFreeMemory = availableMemory() / 2;
		// minFreeMemory = (availableMemory() / 10) * 9;
	}

	public boolean memFull() {
		return availableMemory() < minFreeMemory;
	}

	public void resetPageNo() {
		pageTextField.setText(Integer.toString(currentChunk + 1));
	}

	protected void jbInit() throws Exception {
		pageTextField.setColumns(8);
		ofTextField.setColumns(8);
		contentPane.setLayout(borderLayout1);
		lastButton.setText("Last");
		lastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lastChunk();
			}
		});
		nextButton.setText("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextChunk();
			}
		});
		prevButton.setEnabled(false);
		prevButton.setText("Prev");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prevChunk();
			}
		});
		firstButton.setEnabled(false);
		firstButton.setText("First");
		firstButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firstChunk();
			}
		});
		pageLabel.setText(" Chunk: ");
		ofLabel.setText(" of ");
		ofTextField.setEnabled(false);
		ofTextField.setText("");
		pageTextField.setText("1");
		pageTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int page = Integer.parseInt(pageTextField.getText());
					if (page < 1) {
						JOptionPane.showMessageDialog(ChunkedView.this, "Page number must be 1 or greater");
						resetPageNo();
						return;
					} else if (totalChunks < page) {
						JOptionPane.showMessageDialog(ChunkedView.this, "Page number must be less than or equal to " + totalChunks);
						resetPageNo();
						return;
					}
					pageChanged(page - 1);
				} catch (NumberFormatException x) {
					JOptionPane.showMessageDialog(ChunkedView.this, "Bad Number");
					resetPageNo();
				}
			}
		});
		this.add(contentPane, java.awt.BorderLayout.CENTER);
		chunkButtonPanel.add(firstButton);
		chunkButtonPanel.add(prevButton);
		chunkButtonPanel.add(pageLabel);
		chunkButtonPanel.add(pageTextField);
		chunkButtonPanel.add(ofLabel);
		chunkButtonPanel.add(ofTextField);
		chunkButtonPanel.add(nextButton);
		chunkButtonPanel.add(lastButton);
		// this.add(chunkButtonPanel, java.awt.BorderLayout.NORTH);
	}

	private JPanel contentPane = new JPanel();

	public JPanel getContentPane() {
		return contentPane;
	}

	private JToolBar chunkButtonPanel = new JToolBar();

	private BorderLayout borderLayout1 = new BorderLayout();

	private JButton lastButton = new JButton();

	private JButton nextButton = new JButton();

	private JButton prevButton = new JButton();

	private JButton firstButton = new JButton();

	protected JLabel pageLabel = new JLabel();

	private JLabel ofLabel = new JLabel();

	private JTextField ofTextField = new JTextField();

	private JTextField pageTextField = new JTextField();

	public void firstChunk() {
		currentChunk = 0;
		pageChanged(currentChunk);
		/*
		 * firstButton.setEnabled(false); prevButton.setEnabled(false); lastButton.setEnabled(true);
		 * nextButton.setEnabled(true);
		 */
	}

	public void prevChunk() {
		currentChunk--;
		/*
		 * if (currentChunk == 0) { firstChunk(); } else {
		 */
		pageChanged(currentChunk);
		/*
		 * lastButton.setEnabled(true); nextButton.setEnabled(true); }
		 */
	}

	public void nextChunk() {
		currentChunk++;
		/*
		 * if (currentChunk == totalChunks - 1) { lastChunk(); } else {
		 */
		pageChanged(currentChunk);
		/*
		 * firstButton.setEnabled(true); prevButton.setEnabled(true); }
		 */
	}

	void enableButtons() {
		firstButton.setEnabled(0 < currentChunk);
		prevButton.setEnabled(0 < currentChunk);
		lastButton.setEnabled(currentChunk < totalChunks - 1);
		nextButton.setEnabled(currentChunk < totalChunks - 1);
	}

	public void lastChunk() {
		currentChunk = totalChunks - 1;
		pageChanged(currentChunk);
		// firstButton.setEnabled(true);
		// prevButton.setEnabled(true);
		// lastButton.setEnabled(false);
		// nextButton.setEnabled(false);
	}

	public void pageChanged(int page) {
		try {
			currentChunk = page;
			rewind();
			resetPageNo();
			enableButtons();
			// this.invalidate();
			// this.validate();
		} catch (Exception x) {
			JOptionPane.showMessageDialog(ChunkedView.this, x.getMessage());
		}
	}

	public class ChunkedCounter {
		protected int lineNo = 0;

		int startLine = 0;

		int endLine = -1;

		protected boolean found = false;

		boolean full = false;

		public ChunkedCounter() {
			initMemory();
			if (0 < linesPerChunk) {
				startLine = getCurrentChunk() * linesPerChunk;
				endLine = startLine + linesPerChunk;
			}
		}

		public boolean checkStart() {
			found = !full && (endLine < 0 || (startLine <= lineNo && lineNo < endLine));
			lineNo++;
			if (found) {
				if (endLine < 0) {
					full = memFull();
					if (full) {
						linesPerChunk = lineNo;
						endLine = lineNo - 1;
					}
				}
			}
			return found;
		}

		public boolean inProgress() {
			return found;
		}

		public boolean checkEnd() {
			/*
			 * if (found) { if (endLine < 0) { full = memFull(); if (full) { linesPerChunk = lineNo; endLine = lineNo -
			 * 1; } } found = false; }
			 */
			boolean rtn = found;
			found = false;
			return rtn;
		}

		public void end() {
			int nc;
			if (0 <= linesPerChunk) {
				nc = lineNo / linesPerChunk;
				if (lineNo % linesPerChunk != 0) {
					nc++;
				}
			} else {
				nc = 1;
			}
			setTotalChunks(nc);
		}
	}

	public class ChunkedContentHandler extends XMLFilterImpl {

		String tagName;

		ChunkedCounter counter = new ChunkedCounter();

		public ChunkedContentHandler() {

		}

		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

		@Override
        public void startDocument() {
		}

		@Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			if (qName.equals(tagName)) {
				start(namespaceURI, localName, qName, atts);
			}
		}

		public void start(String namespaceURI, String localName, String qName, Attributes atts) {
			if (counter.checkStart()) {
				doStart(namespaceURI, localName, qName, atts);
				// buf = new StringBuffer();
			}
			// lineNo++;
		}

		@Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			end(namespaceURI, localName, qName);
		}

		public void end(String namespaceURI, String localName, String qName) {
			if (counter.checkEnd()) {
				doEnd(namespaceURI, localName, qName);
			}
		}

		public void doStart(String namespaceURI, String localName, String qName, Attributes atts) {

		}

		public void doEnd(String namespaceURI, String localName, String qName) {

		}

		@Override
        public void endDocument() {
			counter.end();
		}
	};
}
