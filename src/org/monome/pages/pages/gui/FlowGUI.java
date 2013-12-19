package org.monome.pages.pages.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import org.monome.pages.pages.Flow;

import java.io.Serializable;

public class FlowGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	Flow page;
	private JLabel pageLabel = null;
	public JComboBox rowCB = null;
	private JLabel rowLBL = null;
	public JTextField noteTF = null;
	
	//umut new 
	private JLabel scaleLBL;
	private JLabel rootLBL;
	private JComboBox scaleCB;
	private JTextField rootTF;
	private JButton generateScaleBtn;
	
	private JButton saveBtn = null;
	private JLabel bankSizeLBL = null;
	public JTextField bankSizeTF = null;
	private JLabel channelLBL = null;
	public JTextField channelTF = null;
	private JLabel holdModeLBL = null;
	private JCheckBox holdModeCB = null;
	private String[] rowChoices = {"Row 1", "Row 2", "Row 3", "Row 4", "Row 5", "Row 6",
			"Row 7", "Row 8", "Row 9", "Row 10", "Row 11", "Row 12", "Row 13", "Row 14",
			"Row 15", "Row 16"};
	private String[] scaleChoices = { "Major", "Minor", "Chromatic" };
	// the jump in semitones between notes in the scale, automatically cycles before the -99
	// the scale is kept in a different format in flow.java, it is converted to it when it is generated here
	private int[][] scaleJumps = { {2,2,1,2,2,2,1,-99,0,0,0,0,0}, 
								   {2,1,2,2,1,2,2,-99,0,0,0,0,0}, 
								   {1,1,1,1,1,1,1,1,1,1,1,1,-99} 
								 }; 
	public JComboBox quantCB = null;
	private JLabel quantLBL = null;

	/**
	 * This is the default constructor
	 * @param page 
	 */
	public FlowGUI(Flow page) {
		super();
		this.page = page;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		quantLBL = new JLabel();
		quantLBL.setBounds(new Rectangle(15, 135, 76, 16));
		quantLBL.setText("Quantization");
		quantLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		channelLBL = new JLabel();
		channelLBL.setBounds(new Rectangle(35, 80, 51, 21));
		channelLBL.setText("Channel");
		channelLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		bankSizeLBL = new JLabel();
		bankSizeLBL.setBounds(new Rectangle(30, 55, 56, 21));
		bankSizeLBL.setText("Bank Size");
		bankSizeLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		this.setSize(300, 300);
		this.setLayout(null);
		this.add(getPageLabel(), null);
		this.add(getNoteTF(), null);
		this.add(getRowCB(), null);
		this.add(getRowLBL(), null);
		this.add(getScaleLBL(), null);
		this.add(getRootLBL(), null);
		this.add(getScaleCB(), null);
		this.add(getRootTF(), null);
 		this.add(getGenerateScaleBtn(), null);
//		
		this.add(getSaveBtn(), null);
		this.add(bankSizeLBL, null);
		this.add(getBankSizeTF(), null);
		this.add(channelLBL, null);
		this.add(getChannelTF(), null);
		this.add(getHoldModeLBL(), null);
		this.add(getHoldModeCB(), null);
		setName("MIDI Sequencer");
		this.add(getQuantCB(), null);
		this.add(quantLBL, null);
		for (int i = 0; i < rowChoices.length; i++) {
			rowCB.addItem(rowChoices[i]);
		}
		for (int i = 0; i < scaleChoices.length; i++){
			scaleCB.addItem(scaleChoices[i]);
		}
		
		setScaleForKeyboardMode();
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}

	/**
	 * This method initializes pageLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getPageLabel() {
		if (pageLabel == null) {
			pageLabel = new JLabel();
			pageLabel.setBounds(new Rectangle(5, 5, 166, 21));
		}
		return pageLabel;
	}

	/**
	 * This method initializes rowCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getRowCB() {
		if (rowCB == null) {
			rowCB = new JComboBox();
			rowCB.setBounds(new Rectangle(85, 30, 71, 23));
			rowCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = rowCB.getSelectedIndex();
					String noteVal = page.numberToMidiNote(page.selectedChannel.noteNumbers[index]);
					noteTF.setText(noteVal);
				}
			});
		}
		return rowCB;
	}

	/**
	 * This method initializes rowLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getRowLBL() {
		if (rowLBL == null) {
			rowLBL = new JLabel();
			rowLBL.setText("Row");
			rowLBL.setBounds(new Rectangle(15, 30, 71, 21));
			rowLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return rowLBL;
	}

	/*
	 * This initializez scaleLBL
	 * 
	 */
	
	private JLabel getScaleLBL() {
		if(scaleLBL == null) {
			scaleLBL = new JLabel();
			scaleLBL.setText("Scale Type");
			scaleLBL.setBounds(new Rectangle(300, 30, 71, 21));
			scaleLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return scaleLBL;
	}
	
	/*
	 * This initializez rootLBL
	 * 
	 */
	
	private JLabel getRootLBL() {
		if(rootLBL == null) {
			rootLBL = new JLabel();
			rootLBL.setText("Root");
			rootLBL.setBounds(new Rectangle(300, 55, 71, 21));
			rootLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return rootLBL;
	}
	
	
	/**
	 * This method initializes scaleCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getScaleCB() {
		if (scaleCB == null) {
			scaleCB = new JComboBox();
			scaleCB.setBounds(new Rectangle(400, 30, 110, 23));
			scaleCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// int index = scaleCB.getSelectedIndex();
				    
				}
			});
		}
		return scaleCB;
	}
	
	/**
	 * This method initializes rootTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getRootTF() {
		if (rootTF == null) {
			rootTF = new JTextField();
			rootTF.setText("C-1");
			rootTF.setBounds(new Rectangle(400, 55, 46, 21));
		}
		return rootTF;
	}	
	
	/**
	 * This method initializes noteTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNoteTF() {
		if (noteTF == null) {
			noteTF = new JTextField();
			noteTF.setText("C-1");
			noteTF.setBounds(new Rectangle(160, 30, 76, 21));
		}
		return noteTF;
	}

	/**
	 * This method initializes saveBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton();
			saveBtn.setBounds(new Rectangle(22, 164, 151, 21));
			saveBtn.setText("Update Preferences");
			saveBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String midiNote = noteTF.getText();
					int index = rowCB.getSelectedIndex();
					page.selectedChannel.noteNumbers[index] = page.noteToMidiNumber(midiNote);
					if (quantCB.getSelectedIndex() == 0) {
						page.quantization = 96;
					} else if (quantCB.getSelectedIndex() == 1) {
						page.quantization = 48;
					} else if (quantCB.getSelectedIndex() == 2) {
						page.quantization = 24;
					} else if (quantCB.getSelectedIndex() == 3) {
						page.quantization = 12;
					} else if (quantCB.getSelectedIndex() == 4) {
						page.quantization = 6;
					} else if (quantCB.getSelectedIndex() == 5) {
						page.quantization = 3;
					}
					try {
						int bankSize = Integer.parseInt(bankSizeTF.getText());
						if (bankSize < 1 || bankSize > 64) {
							bankSizeTF.setText(""+page.selectedChannel.bankSize);
							return;
						}
						page.setBankSize(bankSize);
						int midiChannel = Integer.parseInt(channelTF.getText());
						if (midiChannel < 1 || midiChannel > 16) {
							bankSizeTF.setText(Integer.toString(page.selectedChannel.midiChannel));
							return;
						}
						page.setMidiChannel(channelTF.getText());
					} catch (NumberFormatException ex) {
						bankSizeTF.setText(""+page.selectedChannel.bankSize);
						return;
					}
				}
			});
		}
		return saveBtn;
	}

	/**
	 * 
	 *   This method initializes the generateScale button
	 * 
	 * */
	
	private JButton getGenerateScaleBtn() {
		if (generateScaleBtn == null) {
			generateScaleBtn = new JButton();
			generateScaleBtn.setBounds(new Rectangle(400, 80, 80, 21));
			generateScaleBtn.setText("Generate");
			generateScaleBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					// start with the first row and set it to the root 
					// and for 15 more rows, set the note number according to the scale selected
					int selectedScaleIndex = scaleCB.getSelectedIndex();
					String rootNoteText = rootTF.getText();
					int rootNoteNumber = page.noteToMidiNumber(rootNoteText);
					int scaleIndex=0;
					page.selectedChannel.noteNumbers[0] = rootNoteNumber;
					int rowIndex=1;
					while(rowIndex<16) {
						page.selectedChannel.noteNumbers[rowIndex] = page.selectedChannel.noteNumbers[rowIndex-1] + scaleJumps[selectedScaleIndex][scaleIndex];
						rowIndex++;
						scaleIndex++;
						if(scaleJumps[selectedScaleIndex][scaleIndex]==-99)
							scaleIndex=0;
					};
				    
					// set the scale for the keyboard mode
					setScaleForKeyboardMode();
					
					// update the selected row's value in the text field
					String noteVal = page.numberToMidiNote(page.selectedChannel.noteNumbers[rowCB.getSelectedIndex()]);
					noteTF.setText(noteVal);
				}
			});
		}
		return generateScaleBtn;
	}
	/**
	 * This method initializes bankSizeTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getBankSizeTF() {
		if (bankSizeTF == null) {
			bankSizeTF = new JTextField();
			bankSizeTF.setBounds(new Rectangle(85, 55, 31, 21));
		}
		return bankSizeTF;
	}

	/**
	 * This method initializes channelTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getChannelTF() {
		if (channelTF == null) {
			channelTF = new JTextField();
			channelTF.setBounds(new Rectangle(85, 80, 31, 21));
		}
		return channelTF;
	}

	/**
	 * This method initializes holdModeLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getHoldModeLBL() {
		if (holdModeLBL == null) {
			holdModeLBL = new JLabel();
			holdModeLBL.setText("Hold Mode");
			holdModeLBL.setBounds(new Rectangle(25, 105, 61, 21));
		}
		return holdModeLBL;
	}

	/**
	 * This method initializes holdModeCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getHoldModeCB() {
		if (holdModeCB == null) {
			holdModeCB = new JCheckBox();
			holdModeCB.setBounds(new Rectangle(85, 105, 21, 21));
		}
		return holdModeCB;
	}

	/**
	 * This method initializes quantCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getQuantCB() {
		if (quantCB == null) {
			quantCB = new JComboBox();
			quantCB.setBounds(new Rectangle(90, 130, 66, 25));
			quantCB.addItem("1 bar");
			quantCB.addItem("1/2");
			quantCB.addItem("1/4");
			quantCB.addItem("1/8");
			quantCB.addItem("1/16");
			quantCB.addItem("1/32");
		}
		return quantCB;
	}
	
	private void setScaleForKeyboardMode() {
		int selectedScaleIndex = scaleCB.getSelectedIndex();
		String rootNoteText = rootTF.getText();
		int rootNoteNumber = page.noteToMidiNumber(rootNoteText);
		int scaleIndex=0;
		int diffsCount=0;
		
		page.rootNoteMidiNumber = rootNoteNumber;
		// normalize the root note so that the bottom rows are still low
		while(page.rootNoteMidiNumber>47)
			page.rootNoteMidiNumber -= 12;
		
		do {
			page.scaleNoteDiffsToRoot[scaleIndex] = diffsCount; 
			diffsCount += this.scaleJumps[selectedScaleIndex][scaleIndex];
			scaleIndex++;
		} while(this.scaleJumps[selectedScaleIndex][scaleIndex] != -99);
		
		page.scaleLength = scaleIndex;
		page.scaleTotalInSemitones = diffsCount;	
//		System.out.println(page.scaleLength);
//		System.out.println(page.scaleTotalInSemitones);
//		System.out.println(page.scaleNoteDiffsToRoot[0]);
//		System.out.println(page.scaleNoteDiffsToRoot[1]);
//		System.out.println(page.scaleNoteDiffsToRoot[2]);
//		System.out.println(page.scaleNoteDiffsToRoot[3]);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
