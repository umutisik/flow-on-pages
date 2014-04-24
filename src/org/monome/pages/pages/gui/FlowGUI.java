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
	public JComboBox channelCB = null;
	public JComboBox keyboardRowOffsetCB;
	public JLabel keyboardRowOffsetLBL;
	private JLabel rowLBL = null;
	
	//umut new 
	private JLabel scaleLBL;
	private JLabel rootLBL;
	private JComboBox scaleCB;
	private JTextField rootTF;
	private JButton generateScaleBtn;
	private JLabel channelLBL = null;
	
	
	
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
//		quantLBL = new JLabel();
//		quantLBL.setBounds(new Rectangle(15, 135, 76, 16));
//		quantLBL.setText("Quantization");
//		quantLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		this.setSize(300, 300);
		this.setLayout(null);
		this.add(getPageLabel(), null);
		this.add(getChannelCB(), null);
		this.add(getKeyboardRowOffsetLBL(), null);
		this.add(getKeyboardRowOffsetCB(), null);
		this.add(getScaleLBL(), null);
		
		this.add(getChannelLBL(), null);
		
		this.add(getRootLBL(), null);
		this.add(getScaleCB(), null);
		this.add(getRootTF(), null);
 		this.add(getGenerateScaleBtn(), null);
//		
		setName("Flow Page");
		for (int i = 0; i < page.scaleChoices.length; i++){
			scaleCB.addItem(page.scaleChoices[i]);
		}
		for(int i=0; i<16; i++)
		{
			int numma = i+1;
			channelCB.addItem(Integer.toString(numma));
		}
		
		for(int i=0; i<17; i++)
		{
			keyboardRowOffsetCB.addItem(Integer.toString(i));
		}
		keyboardRowOffsetCB.setSelectedIndex(7);

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
	private JComboBox getKeyboardRowOffsetCB() {
		if (keyboardRowOffsetCB == null) {
			keyboardRowOffsetCB = new JComboBox();
			keyboardRowOffsetCB.setBounds(new Rectangle(400, 80, 71, 23));
			keyboardRowOffsetCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					page.channels[channelCB.getSelectedIndex()].rowOffset = keyboardRowOffsetCB.getSelectedIndex(); 
				}
			});
		}
		return keyboardRowOffsetCB;
	}
	
	/**
	 * This method initializes channelCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getChannelCB() {
		if (channelCB == null) {
			channelCB = new JComboBox();
			channelCB.setBounds(new Rectangle(400, 5, 71, 23));
			channelCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = channelCB.getSelectedIndex();
					// load up selected channel's information
					scaleCB.setSelectedIndex(page.channels[index].selectedScaleIndex);
					rootTF.setText(page.channels[index].rootNoteText);
					keyboardRowOffsetCB.setSelectedIndex(page.channels[index].keyboardRowOffset);
				}
			});
		}
		return channelCB;
	}
	
	
	private JLabel getChannelLBL() {
		if(channelLBL == null) {
			channelLBL = new JLabel();
			channelLBL.setText("Channel");
			channelLBL.setBounds(new Rectangle(300, 5, 71, 21));
			channelLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return channelLBL;
	}

	
	
	private JLabel getKeyboardRowOffsetLBL() {
		if(keyboardRowOffsetLBL == null) {
			keyboardRowOffsetLBL = new JLabel();
			keyboardRowOffsetLBL.setText("Keyb Row Offset:");
			keyboardRowOffsetLBL.setBounds(new Rectangle(300, 80, 71, 21));
			keyboardRowOffsetLBL.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return keyboardRowOffsetLBL;
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
	 * This method initializes saveBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
//	private JButton getSaveBtn() {
//		if (saveBtn == null) {
//			saveBtn = new JButton();
//			saveBtn.setBounds(new Rectangle(22, 164, 151, 21));
//			saveBtn.setText("Update Preferences");
//			saveBtn.addActionListener(new java.awt.event.ActionListener() {
//				public void actionPerformed(java.awt.event.ActionEvent e) {
//					//page.superdebug();
//					String midiNote = noteTF.getText();
//					int index = rowCB.getSelectedIndex();
//					page.selectedChannel.noteNumbers[index] = page.noteToMidiNumber(midiNote);
//					if (quantCB.getSelectedIndex() == 0) {
//						page.quantization = 96;
//					} else if (quantCB.getSelectedIndex() == 1) {
//						page.quantization = 48;
//					} else if (quantCB.getSelectedIndex() == 2) {
//						page.quantization = 24;
//					} else if (quantCB.getSelectedIndex() == 3) {
//						page.quantization = 12;
//					} else if (quantCB.getSelectedIndex() == 4) {
//						page.quantization = 6;
//					} else if (quantCB.getSelectedIndex() == 5) {
//						page.quantization = 3;
//					}
//					try {
//						int bankSize = Integer.parseInt(bankSizeTF.getText());
//						if (bankSize < 1 || bankSize > 64) {
//							bankSizeTF.setText("00");
//							return;
//						}
//						//page.setBankSize(bankSize);
//						int midiChannel = Integer.parseInt(channelTF.getText());
//						if (midiChannel < 1 || midiChannel > 16) {
//							bankSizeTF.setText(Integer.toString(page.selectedChannel.midiChannel));
//							return;
//						}
//						page.setMidiChannel(channelTF.getText());
//					} catch (NumberFormatException ex) {
//						bankSizeTF.setText("00");
//						return;
//					}
//				}
//			});
//		}
//		return saveBtn;
//	}


	/**
	 * 
	 *   This method initializes the generateScale button
	 * 
	 * */
	
	private JButton getGenerateScaleBtn() {
		if (generateScaleBtn == null) {
			generateScaleBtn = new JButton();
			generateScaleBtn.setBounds(new Rectangle(400, 105, 80, 21));
			generateScaleBtn.setText("Update");
			generateScaleBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					int guiSelectedChannelIndex = channelCB.getSelectedIndex();
					
					// start with the first row and set it to the root 
					// and for 15 more rows, set the note number according to the scale selected
					page.channels[guiSelectedChannelIndex].selectedScaleIndex = scaleCB.getSelectedIndex();
					
					page.channels[guiSelectedChannelIndex].rootNoteText = rootTF.getText();
					
					page.channels[guiSelectedChannelIndex].setScale();
				}
			});
		}
		return generateScaleBtn;
	}
	
	
//	/**
//	 * This method initializes quantCB	
//	 * 	
//	 * @return javax.swing.JComboBox	
//	 */
//	private JComboBox getQuantCB() {
//		if (quantCB == null) {
//			quantCB = new JComboBox();
//			quantCB.setBounds(new Rectangle(90, 130, 66, 25));
//			quantCB.addItem("1 bar");
//			quantCB.addItem("1/2");
//			quantCB.addItem("1/4");
//			quantCB.addItem("1/8");
//			quantCB.addItem("1/16");
//			quantCB.addItem("1/32");
//		}
//		return quantCB;
//	}
//	

}  //  @jve:decl-index=0:visual-constraint="10,10"
