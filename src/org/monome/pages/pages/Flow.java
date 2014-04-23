package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JPanel;

import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.LEDBlink;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.gui.FlowGUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * 
 * 
 * Flow page - umut isik, based on midisequencerpage
 */

/*
 * Introduction and warnings,
 * For Keyboard mode functions, which start with keyboardMode, note number means the distance in notes in scale from the root note to the note
 * The sequencer code uses note_num for midi numbers. 
 * For keyboard mode, midinotenumber or midinumber is used.
 * 
 * */
public class Flow implements Page, Serializable {
    static final long serialVersionUID = 42L;
    
    int debugCount = 0;

	/**
	 * The MonomeConfiguration that this page belongs to
	 */
	MonomeConfiguration monome;

	/**
	 * The index of this page (the page number) 
	 */
	int index;

	FlowGUI gui;

	// modes
	private int mode=0;
	public int SEQUENCERMODE = 0;
	public int BANKMODE = 1;
	public int KEYBOARDMODE = 2;
	public int MATRIXMODE = 3;
	public int DEFAULTMODE = 0;
	
	// mode button x's (bottom row)
	public int ButtonNoBank = 13;
	public int ButtonNoKeyboard = 14;
	public int ButtonNoMatrix = 12;
	public int LastModeButton = 12; // this is used in keyboard more to tell after which buttons the presses should not play
	
	public int ButtonNoPattern = 0; // does not work in keyboard mode, pattern mode.
	
	public int ButtonNoVelocity = 11;
	public int ButtonNoMute = 10;
	public int ButtonNoClear = 5;
	// 0
	
	
	
	public static final int MAX_SEQUENCE_LENGTH = 256;
	public static final int NUMBER_OF_BANKS = 18;
	public static final int SEQUENCE_HEIGHT = 51;
	
	
	
	/**
	 * The current MIDI clock tick number (from 0 to 6)
	 */
	private int tickNum = 0;

	
	
	
	
/****************** keyboard mode vars (umut) ***************/
	/**
	 * Natural:0, Sharp:1, Flat:-1 
	 */
	private int accidental = 0;
	
	/**
	 * Number to transpose the keyboard by
	 */
	private int transpose = 0;
	

	public boolean keyboardRecordMode = false;
	public boolean quantizeToNextStepWhenRecording = false; //updated all the time to tell us where to quantize to
	

	
	

//	
/****************** end keyboard mode vars   ***************/

	
/****************** matrix mode vars   ***************/
	// steps top to bottom
	private int[] rowSwap = new int[SEQUENCE_HEIGHT]; 
	
	
/****************** end matrix mode vars   ***************/	
	
/************* bank mode vars *******************/
	private boolean bankCopyModeOn = false;
	private int bankCopyModeWhatChannel = 0;
	private int bankCopyModeWhatBank = 0;
	private boolean bankCopyModeAlreadyCopiedAtLeastOnce = false;
	
	
/**** midi note off scheduling *****/
	
	public static final int MAX_LEN_OF_MIDI_SCHEDULER = 2048; // in ticks!!!
	
	private ArrayList[] midiNoteOffSchedule = new ArrayList[MAX_LEN_OF_MIDI_SCHEDULER]; 
	
	private int midiSchedulerPosition; 
	
	public class FlowMidiEvent {
		public int onOff; // 0 for off
		public int midiChannel;
		public int midiNo;
		public int velocity;
		public int sourceChannelNumber = 0;
		
		public FlowMidiEvent(int note_num, int onOff, int vel, int midichannel) {
			this.midiChannel = midichannel;
			this.onOff = onOff;
			this.velocity = vel;
			this.midiNo = note_num;
		}
		
		public FlowMidiEvent(int note_num, int onOff, int vel, int midichannel, int sourceChannelNumber) {
			this.midiChannel = midichannel;
			this.onOff = onOff;
			this.velocity = vel;
			this.midiNo = note_num;
			this.sourceChannelNumber = sourceChannelNumber;
		}
	}
	 
/*********** */

	public static final int NUMBER_OF_CHANNELS = 8;
	
	
	
	
	/**  
	 * channels, each channel represents an instrument
	 * */
	public SequencerChannel[] channels = new SequencerChannel[NUMBER_OF_CHANNELS];
	public SequencerChannel selectedChannel;
	/**
	 * 1 = bank clear mode enabled
	 */
	private int bankClearMode = 0;

	/**
	 * Currently selected channel number
	 */
	private int selectedChannelNumber = 0;
	
	
	public int quantization = 6;

	/**
	 * Random number generator
	 */
	private Random generator = new Random();

	/**
	 * The name of the page 
	 */
	private String pageName = "Flow Page";

	private LEDBlink blinkThread;

	private int muteMode;

	private int velocityMode;

    private Dimension origGuiDimension;

    // brightness settings for different situations
    // sequenced notes
    private int briSeqNote = 12;
    private int briSeqOtherChannelNote = 6;
    // moving bar
    private int briSeqBar = 7;
    // note on
    private int briNoteOn = 15;
    private int briSeqNoteLen = 7;
    // keyboard mode brightnesses
    private int briKeybNotePressed = 15;
    private int briKeybNotePlaying = 10;
    private int briKeybRoot = 4;
    private int briKeybSameNote = 6;
    private int briKeybHarmonic = 3; // experimental
    private int briKeybOtherChannels = 6;
    // bank mode brightness
    private int briBankNonEmpty = 6;
    private int briBankPlaying = 9;
    private int briBankSelected = 9;
    private int briBankSelectedInSelectedChannel = 15;
    
    // matrix mode brightness levels
    private int briMatrixDefault = 6;
    private int briMatrixNoteOn = 15;
    
    // default on and off (off is for testing)
    private int briOn = 12;
    private int briOff = 0;
    private int briBackground = 2; // for background in extra toplayer panels
    
    public static final int DEPTHINCREMENT = 7; 
    
    ///////////////////////////////////////////////////////////////////////////////
    // these handle an additional top layer for editing parameters etc.
    // the way this works: flow_led takes care of all the LEDs
    // if the top layer is active, it doesn't draw things in the top level
    public static final int TopLayerInactive = 0;
    public static final int TopLayerPatternMode = 1; // a pattern is a sequence of 16
    public static final int TopLayerVelocityNoteLengthAndCCMode = 2; // this is for the velocities of individual notes
    
    private int topLayerMode = 0;
    //private int[][] patternModeLeds = new int[16][3];
    
    private int patternModeXStart = 0;
    private int patternModeXEnd = 16; // one more than the actual ending
    private int patternModeYStart = 12;
    private int patternModeYEnd = 16; // one more than the actual ending
    
    private boolean patternModeCopyOn = false;
    private int patternModeCopyWhat = 0;
    private boolean patternModeAlreadyCopiedAtLeastOnce = false;
    
    private void patternModeRedraw() {
    	if(this.topLayerMode != TopLayerPatternMode)
    		return;
    	
    	
    	int aX, aY; // a for actual position on monome
    	int x, y;
    	for(aX = patternModeXStart; aX < patternModeXEnd; aX++) {
    		x = aX - patternModeXStart;
    		for(aY = patternModeYStart; aY < patternModeYEnd; aY++) {
    			y = aY - patternModeYStart;
    			monome.vari_led(aX, aY, briBackground, this.index);
    		}
    	}
    	
    	x = selectedChannel.patternsStarting[selectedChannel.selectedBank];
    	y = 0;
    	aY = patternModeYStart + y;
    	while(true) {
    		x++;
    		if(x == selectedChannel.patternsEnding[selectedChannel.selectedBank])
    			break;
    		if(x >= this.monome.sizeX)
    			x=0;
    		aX = patternModeXStart + x;
    		monome.vari_led(aX, aY, briBankNonEmpty, this.index);
    		
    	}
    	
    	y = 0; 
    	x = this.selectedChannel.patternsStarting[selectedChannel.selectedBank]; 
		aY = patternModeYStart + y;
		aX = patternModeXStart + x;
		monome.vari_led(aX, aY, briBankSelected, this.index);
		
		
		y = 1; 
    	x = this.selectedChannel.patternsEnding[selectedChannel.selectedBank]-1; 
		aY = patternModeYStart + y;
		aX = patternModeXStart + x;
		monome.vari_led(aX, aY, briBankSelected, this.index);
		
		
		y = 2; 
    	x = this.selectedChannel.patternsSelected[selectedChannel.selectedBank]; 
		aY = patternModeYStart + y;
		aX = patternModeXStart + x;
		monome.vari_led(aX, aY, briBankSelected, this.index);
		
		y = 3; 
    	x = this.selectedChannel.depth + 2; 
		aY = patternModeYStart + y;
		aX = patternModeXStart + x;
		monome.vari_led(aX, aY, briBankSelected, this.index);

		
    	this.monome.vari_led(ButtonNoPattern, this.monome.sizeY-1, briOn, this.index);
    	
    	//debugCount++;
		//System.out.println("redraw" + debugCount);
		
    }
    
    private void patternModeHandlePress(int x, int y, int value) {
    	
    	if(value == 1) {
    		if(y == 0) {
    			// set the new starting position and schedule a reset at end of bar
    			selectedChannel.patternsStarting[selectedChannel.selectedBank] = x;
    			selectedChannel.scheduledChangeAtEndOfBarExists = true;
    			selectedChannel.scheduledChangeBank = selectedChannel.selectedBank;
    			patternModeRedraw();
    		} else if(y == 1) {
    			// if the new ending is sooner, schedule a restart at end of bar
    			if(x + 1 <= selectedChannel.patternsEnding[selectedChannel.selectedBank]) {
    				selectedChannel.scheduledChangeAtEndOfBarExists = true;
    				selectedChannel.scheduledChangeBank = selectedChannel.selectedBank;
    			}
    			selectedChannel.patternsEnding[selectedChannel.selectedBank] = x + 1;
    			patternModeRedraw();
    		} else if(y == 2) {
    			if(patternModeCopyOn) 	{ // a key is already pressed and not released
    				selectedChannel.copyPattern(patternModeCopyWhat, x);
    				patternModeAlreadyCopiedAtLeastOnce = true;
    			} else {
    				patternModeCopyOn = true;
    				patternModeAlreadyCopiedAtLeastOnce = false;
    				patternModeCopyWhat = x;
    			}
    			redrawDevice();
    		} else if(y == 3 && x > 1 && x < 8) {
    			selectedChannel.depth = x-2;
    			redrawDevice();
    		}
    	}
    	else { // button released
    		if(patternModeCopyOn && patternModeCopyWhat == x) {
    			if(patternModeAlreadyCopiedAtLeastOnce == false)
    				selectedChannel.patternsSelected[selectedChannel.selectedBank] = x;
    			patternModeCopyOn = false;
    			patternModeRedraw();
    		}
    	}
    	//System.out.print("a");
    	
    }
    
    private void patternModeLed(int x, int y, int bri) {
    	monome.vari_led(patternModeXStart+x, patternModeYStart+y, bri, this.index);
    }
    
    ////////// top layer velocityity, note length and CC mode
    private int velocityModeXStart = 14;
    private int velocityModeYStart = 0;
    private int velocityModeWidth = 2;
    private int velocityModeHeight = 16;
    private int velocityModeMoveLeftToPreventOverlap = 0;
    private boolean velocityModeSomethingWasChanged = false;
    private boolean velocityModeNoteWasJustCreated = false;
    
    private int velocityModeXofNoteSelected, velocityModeYofNoteSelected;
    
    private void velocityModeInit(int x,int y, boolean newnote) {
    	
    	// not available if velocitymode is disabled
    	if(this.velocityMode == 0)
    		return;
    	
    	if(x>15-velocityModeWidth)
    		velocityModeMoveLeftToPreventOverlap =  (16-x); 
    	
    	topLayerMode = TopLayerVelocityNoteLengthAndCCMode;
    	velocityModeXofNoteSelected = x;
    	velocityModeYofNoteSelected = y;
    	velocityModeNoteWasJustCreated = newnote; 
    	velocityModeSomethingWasChanged = false;
    }
    
    private void velocityModeExity() {
    	topLayerMode = TopLayerInactive;
    	velocityModeMoveLeftToPreventOverlap = 0;
    }
    
    private void velocityModeRedraw() {
    	if(this.topLayerMode != TopLayerVelocityNoteLengthAndCCMode)
    		return;
    	int x,y, aX, aY;
    	
    	for(x=0;x<velocityModeWidth;x++)
    		for(y=0;y<velocityModeHeight;y++)
    			velocityModeLed(x,y,briBackground);
    	
    	x=0;
    	int nl = selectedChannel.seqNoteLengths[selectedChannel.selectedBank][xToSeqX(velocityModeXofNoteSelected)][yToSeqY(velocityModeYofNoteSelected)];
    	//System.out.println(nl);
    	for(y=0;y<16;y++) {
    		if(16-y<=nl) 
    			velocityModeLed(x,y,briSeqNote);
    	}
    	
    	x=1;
    	int vl = selectedChannel.sequence[selectedChannel.selectedBank][xToSeqX(velocityModeXofNoteSelected)][yToSeqY(velocityModeYofNoteSelected)];
    	vl = 15 - ((vl-7)/8);
    	
    	//System.out.println(vl);
    	for(y=0;y<16;y++) {
    		if(y>=vl) 
    			velocityModeLed(x,y,briSeqNote);
    	}
    	
    }
    
    private void velocityModeHandlePress(int x, int y, int value) {
    	if(x < 0 || x >= velocityModeWidth || y<0 || y>=velocityModeHeight)
    		return;
    	//System.out.println(x + " " + y);
    	if(x == 0 && value == 1) {
    		selectedChannel.seqNoteLengths[selectedChannel.selectedBank][xToSeqX(velocityModeXofNoteSelected)][yToSeqY(velocityModeYofNoteSelected)] = 16-y;
    		selectedChannel.reGenerateNoteLengthArrayRow(selectedChannel.selectedBank, yToSeqY(velocityModeYofNoteSelected), xToSeqX(velocityModeXofNoteSelected), getSelectedPatternNumber()*16 + 16);
    		velocityModeSomethingWasChanged = true;
    		redrawDevice();
    	}
    	if(x == 1 && value == 1) {
    		selectedChannel.sequence[selectedChannel.selectedBank][xToSeqX(velocityModeXofNoteSelected)][yToSeqY(velocityModeYofNoteSelected)] = 7 + (15-y)*8;;
    		velocityModeSomethingWasChanged = true;
    		redrawDevice();
    	}
        	
    	
    }

    private void velocityModeLed(int x, int y, int bri) {
    	monome.vari_led(velocityModeXStart - velocityModeMoveLeftToPreventOverlap + x, velocityModeYStart+y, bri, this.index);
    }

    
    //////////////////////////////////////////////////////////////////////////////////
    
	/**
	 * @param monome The MonomeConfiguration that this page belongs to
	 * @param index The index of this page (the page number)
	 */
    // constructor
	public Flow(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;

		for(int i=0;i<NUMBER_OF_CHANNELS;i++) {
			channels[i] = new SequencerChannel(this.monome, this.index);
			channels[i].number = i;
			channels[i].midiChannel = i;
			generateIsMidiNumberInScale(channels[i]);
		}

		
		selectedChannelNumber = 0;
		selectedChannel = channels[0];
		channels[0].playingBank = 0;
		
		//initialize step swapping
		for(int i=0;i<SEQUENCE_HEIGHT;i++)
			this.rowSwap[i]=i;

		// initialize midi note off scheduler
		midiSchedulerPosition = 0;
		for(int i = 0; i<MAX_LEN_OF_MIDI_SCHEDULER; i++) {
			midiNoteOffSchedule[i] = new ArrayList<FlowMidiEvent>();
		}

		

		// setup default notes
		//gui.channelTF.setText(Integer.toString(channels[0].midiChannel));
		// debuggg

		this.gui = new FlowGUI(this);
		
		//gui.channelTF.setText("0");
		//gui.bankSizeTF.setText("0");
		
		this.setQuantization("6");

		origGuiDimension = gui.getSize();
		
		
    }

    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {
		int x_seq;
		int y_seq;
		
		// top layer
		// pattern mode button still held, these only work in sequencer mode
		if(this.mode == SEQUENCERMODE) {
			
			
			if(topLayerMode == TopLayerPatternMode) {
				// if pattern mode button was just released
				if(y == (this.monome.sizeY-1) && x == this.ButtonNoPattern && value == 0) {
					topLayerMode = TopLayerInactive;
					redrawDevice();
					
				} else {
					// pattern mode buttons
					patternModeHandlePress(x-patternModeXStart, y-patternModeYStart, value);
				}
				return;
			}

			if(topLayerMode == TopLayerVelocityNoteLengthAndCCMode) {
				//things that happen in velocity editing
				velocityModeHandlePress(x-velocityModeXStart+velocityModeMoveLeftToPreventOverlap, y-velocityModeYStart, value);
				if(!(x < velocityModeXStart - velocityModeMoveLeftToPreventOverlap 
						|| x >= velocityModeXStart - velocityModeMoveLeftToPreventOverlap + velocityModeWidth 
						|| y < velocityModeYStart || y >= velocityModeYStart + velocityModeHeight) || y == this.monome.sizeY-1) { 
					return;
				}   
				//if(x != velocityModeXofNoteSelected || y!=velocityModeYofNoteSelected || value!=0) 
				//	return;
			}
			
			// pattern mode button was just pressed
			if(y == (this.monome.sizeY-1) && x == this.ButtonNoPattern) {
				if(topLayerMode == 0) {
					topLayerMode = TopLayerPatternMode;
					redrawDevice();
					return;
				}
			}
			
			
			
		} // end of top layer
		
		// mode changes
		if(value == 1 && y == (this.monome.sizeY-1)) { 
			if(x == ButtonNoKeyboard) {
				if(this.mode != KEYBOARDMODE) 
					this.mode = KEYBOARDMODE;
				else 
					this.mode = DEFAULTMODE;
				this.redrawDevice();
				return;
			}
			// matrix mode
			if(x == ButtonNoMatrix) {
				if(this.mode != MATRIXMODE)
					this.mode = MATRIXMODE;
				else
					this.mode = DEFAULTMODE;
				this.redrawDevice();
				return;
			}
			if(x == ButtonNoBank) {
				if(this.mode != BANKMODE)
					this.mode = BANKMODE;
				else
					this.mode = DEFAULTMODE;
				this.redrawDevice();
				return;
			}
		}
			
		

		// handle press or release events in keyboard mode
		if(this.mode == KEYBOARDMODE) {
			if(x==0 && y==0) {
				if(value==1) {
					if(!keyboardRecordMode) {

						keyboardRecordMode = true;
						flow_led(0,0,briNoteOn,this.index);
					} else {
						keyboardRecordMode = false;
						// any notes still being held at this point will not be recorded.
						flow_led(0,0,briOff,this.index);
					}
				}
				return;
			}
				
			if (y != (this.monome.sizeY - 1) || x < LastModeButton) { //if page change or keyboard mode button aren't the ones pressed
				int velocity = value * 127;
				int channel = 1;
				
				int midi_num = this.keyboardModeNoteNumberToMidiNumber((x - selectedChannel.rootNoteX) + selectedChannel.rowOffset*(selectedChannel.rootNoteY-y), selectedChannel);
				if (midi_num > 127) midi_num = 127;
				if (midi_num < 0) midi_num = 0;
				this.playNote(midi_num, velocity, channel, value);
				keyboardModeLedFromNoteNumber((x - selectedChannel.rootNoteX) + selectedChannel.rowOffset*(selectedChannel.rootNoteY-y), briKeybSameNote*value, selectedChannel);
				// extra brightness for the key actually pressed
				if(value>0)
					flow_led(x, y, this.briKeybNotePressed*value, this.index);
				else
					keyboardModeRedrawXYToDefault(x,y);	
			}
			return;
		}
		else if(this.mode == MATRIXMODE) {
			if(y !=this.monome.sizeY-1) // not a bottom row button
			{
				flow_led(x, seqYtoY(this.rowSwap[yToSeqY(x)]), 0, this.index);
				flow_led(x, y, briMatrixDefault, this.index);
				rowSwap[yToSeqY(x)]=seqYtoY(y);
			}
			return;	    
		}
		else if (this.mode == BANKMODE) { //handle press in bank mode

			if (value == 1 && this.blinkThread != null) {
				this.blinkThread.cancel();
			}
			
			if(y == (this.monome.sizeY - 1)) {
				if (value == 1) {
					if (x < 2) {
						
					}
					if (x == 2) {
						this.stopNotes();
						this.generateSequencerPattern();
					}
					if (x == 3) {
						this.stopNotes();
						this.alterSequencerPattern();
					}

					if (x == ButtonNoClear) {
						if (this.bankClearMode == 1) {
							this.bankClearMode = 0;
							flow_led(x, this.monome.sizeY-1, this.briOff, this.index);
						} else {
							this.bankClearMode = 1;
							flow_led(x, this.monome.sizeY-1, this.briOn, this.index);
						}
					}

					if (x == ButtonNoMute) {
						if (this.muteMode == 0) {
							this.muteMode = 1;
						} else {
							this.muteMode = 0;
						}
						this.redrawDevice();
					}
				}
			} else { // in bank mode but not bottom row. i.e. a bank button is pressed or released
				if (value == 1 && bankClearMode == 1) {
					this.bankClearMode = 0;
					channels[x].clearBank(y);
					if (channels[x].playingBank == y) {
						channels[x].playingBank = 17;
					}
					this.redrawDevice();
				} else {
					if(value == 1) {
						if(bankCopyModeOn == false) {
							bankCopyModeOn = true; //any button press triggers this!
							bankCopyModeAlreadyCopiedAtLeastOnce = false;
							bankCopyModeWhatChannel = x;
							bankCopyModeWhatBank = y;
						} else {
							if(bankCopyModeWhatChannel ==  x) { //copy only in same channel
								copyBank(bankCopyModeWhatChannel, bankCopyModeWhatBank, x, y);
								bankCopyModeAlreadyCopiedAtLeastOnce = true;
							}
						}
					}
					else { //value==0
						
							if(bankCopyModeAlreadyCopiedAtLeastOnce == false) {
								channels[x].scheduledChangeAtEndOfBarExists = true;
								channels[x].scheduledChangeBank = y;
								channels[x].selectedBank = y;
								this.selectedChannelNumber = x;
								this.selectedChannel = this.channels[this.selectedChannelNumber];
							}
							this.redrawDevice();
							if(x == bankCopyModeWhatChannel && y == bankCopyModeWhatBank) {
								bankCopyModeOn = false;	
							}
					}					
				}
			}
			
//			else { // bank mode release events
//				
//			}
		} else if(this.mode == SEQUENCERMODE) { // sequence edit mode
			// only on press events
			if (value == 1) {
				if (y == this.monome.sizeY - 1) {
					// debug: notelength
					if(x == 7)
						selectedChannel.newNoteLength++;
					if(x == 6 && selectedChannel.newNoteLength>1) {
						selectedChannel.newNoteLength--;
					}
					
					if (x == ButtonNoMute) {
						if (this.muteMode == 0) {
							this.muteMode = 1;
						} else {
							this.muteMode = 0;
						}
						this.redrawDevice();
					}

					if (x == ButtonNoVelocity) {
						if (this.velocityMode == 0) {
							this.velocityMode = 1;
						} else {
							this.velocityMode = 0;
						}
						this.redrawDevice();
					}

				// record button press to the sequence
				} else { // if not a last row button
					x_seq = xToSeqX(x);
					y_seq = yToSeqY(y);
					if (selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] == 0) {
						// add the note to the sequencer
						if (this.velocityMode == 1) {
							selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] = 103;
						} else {
							selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] = 103;
						}
						
						// store note length
						// if there is a previous note that would end after this new one, 
						// then make the endings of the two notes the same (i.e. don't change note length)
						if(selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq] < selectedChannel.newNoteLength)
							selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq] = selectedChannel.newNoteLength;
						
						selectedChannel.reGenerateNoteLengthArrayRow(selectedChannel.selectedBank, y_seq, x_seq, x_seq+16);
						
						int note_len = selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq];
						// redraw this part of the row
						for(int i=0;i < note_len; i++) {
							if(x_seq + i >= MAX_SEQUENCE_LENGTH)
								break;
							if(selectedChannel.sequence[selectedChannel.selectedBank][x_seq+i][y_seq] == 0) {
								if(x+i <= 15) { 
									if(this.selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq+i][y_seq] == 0) 
										flow_led(x+i, y, briOff, this.index);
									else
										flow_led(x+i, y, briSeqNoteLen, this.index);
								}
							} else { // if there is a note here
								if(x+i <= 15) 
									flow_led(x+i, y, briSeqNote, this.index);
							}
						}
						
							
						// update the led
						flow_led(x, y, briSeqNote, this.index);
						
						
						//topLayer
						velocityModeInit(x,y,true);
						redrawDevice();
					
				
					// remove note
					} else { 
						if(velocityMode == 1) 
							velocityModeInit(x,y,false);
						else { // remove note in the case of: velocity editing mode off 
							x_seq = xToSeqX(x);
							y_seq = yToSeqY(y);
							this.selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] = 0;
							flow_led(x, y, 0, this.index);
							int note_len = selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq];
							
							selectedChannel.reGenerateNoteLengthArrayRow(selectedChannel.selectedBank, y_seq, x_seq, x_seq+16);
							
							// redraw the remaining part of the row
							for(int i=0;i < 16; i++) {
								if(x_seq + i >= MAX_SEQUENCE_LENGTH || x + i > 15)
									break;
								if(selectedChannel.sequence[selectedChannel.selectedBank][x_seq+i][y_seq] == 0) {
										if(this.selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq+i][y_seq] == 0) 
											flow_led(x+i, y, briOff, this.index);
										else
											flow_led(x+i, y, briSeqNoteLen, this.index);
								} else { // if there is a note here
										flow_led(x+i, y, briSeqNote, this.index);
								}
							}
							
						} 
							
						redrawDevice();
						
					}
				}
			} else if(y<this.monome.sizeY-1) { //sequencermode button release events
				if(velocityMode == 1) {
					x_seq = xToSeqX(x);
					y_seq = yToSeqY(y);
					if(velocityModeSomethingWasChanged == false && velocityModeNoteWasJustCreated == false) { // if nothing was changed, then we can remove the note 
						
						this.selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] = 0;
						flow_led(x, y, 0, this.index);
						int note_len = selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq];

						selectedChannel.reGenerateNoteLengthArrayRow(selectedChannel.selectedBank, y_seq, x_seq, x_seq+16);

						// redraw the remaining part of the row
						for(int i=0;i < 16; i++) {
							if(x_seq + i >= MAX_SEQUENCE_LENGTH || x + i > 15)
								break;
							if(selectedChannel.sequence[selectedChannel.selectedBank][x_seq+i][y_seq] == 0) {
								if(this.selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq+i][y_seq] == 0) 
									flow_led(x+i, y, briOff, this.index);
								else
									flow_led(x+i, y, briSeqNoteLen, this.index);
							} else { // if there is a note here
								flow_led(x+i, y, briSeqNote, this.index);
							}
						}

						velocityModeExity();
						redrawDevice();
					}
					else { //velocity mode something was changed
						velocityModeExity();
						redrawDevice();
					}
				}

			}
		}
	}

	

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick(MidiDevice device)
	 */
	public void handleTick(MidiDevice device) {
		
		// if there are note-off messages scheduled for this time, do that.
		turnOffScheduledNotes();
		
		if (this.tickNum >= quantization) {
			this.tickNum = 0;
			this.quantizeToNextStepWhenRecording = false;
		}
		
		if(2 * this.tickNum > quantization) {
			this.quantizeToNextStepWhenRecording = true;
		}
		
		// send a note on for lit leds on this sequence position
		if (this.tickNum == 0) {
			// play notes for all channels, ci=channel index
			for(int ci = 0; ci<NUMBER_OF_CHANNELS; ci++)
			{
				SequencerChannel loopChan = channels[ci];
				
				// if at the end of a bar (16 notes)
				if(loopChan.sequencePosition % this.monome.sizeX == 0) {
					// perform any necessary loopings/changes for the sequencer positions
					if(loopChan.scheduledChangeAtEndOfBarExists) {
						loopChan.scheduledChangeAtEndOfBarExists = false;
						loopChan.playingBank = loopChan.scheduledChangeBank;
						loopChan.sequencePosition = loopChan.sequencePosition = loopChan.patternsStarting[loopChan.playingBank]*this.monome.sizeX;
						if(this.mode == BANKMODE)
							this.redrawDevice();
					}
					else {
						// loopings
						if (loopChan.sequencePosition == loopChan.patternsEnding[loopChan.playingBank]*this.monome.sizeX) {
							loopChan.sequencePosition = loopChan.patternsStarting[loopChan.playingBank]*this.monome.sizeX;
						}

						if (loopChan.sequencePosition == MAX_SEQUENCE_LENGTH) {
							loopChan.sequencePosition = 0;
							//System.out.println("Looped over sequence" + selectedChannelNumber + " " + selectedChannel.selectedBank);
						}
					}
				}
				
				
				// play
				this.playNotes(loopChan, loopChan.sequencePosition, 127);
				if(this.mode == KEYBOARDMODE)
					this.lightUpNotesOnKeyboard(loopChan, loopChan.sequencePosition, briKeybNotePlaying);
				
			}
			
			
			// deal with selected channel and leds
			
			if (this.selectedChannel.sequencePosition >= (getSelectedPatternNumber() * (this.monome.sizeX)) && this.selectedChannel.sequencePosition < ((getSelectedPatternNumber() + 1) * (this.monome.sizeX))) {
				if(this.mode == SEQUENCERMODE) {
					int col = this.selectedChannel.sequencePosition % (this.monome.sizeX);
					int x_seq = getSelectedPatternNumber() * (this.monome.sizeX) + col;
					this.sequencerModeRedrawCol(col, 1);
				}
			}
			
			if(this.mode == MATRIXMODE) {
				this.lightUpNotesInMatrixMode(selectedChannel, selectedChannel.sequencePosition, this.briMatrixNoteOn);
			}
			
				
		}

		// turn off the leds at the end of the time allotted for this column
		if (this.tickNum == quantization - 1) {
			if (this.selectedChannel.sequencePosition >= (getSelectedPatternNumber() * (this.monome.sizeX)) && this.selectedChannel.sequencePosition < ((getSelectedPatternNumber() + 1) * (this.monome.sizeX))) {
				if(this.mode == SEQUENCERMODE) {
					// old code for when the whole col was sent as one message
//					ArrayList<Integer> colArgs = new ArrayList<Integer>();
//					colArgs.add(this.selectedChannel.sequencePosition % (this.monome.sizeX));
//					for(int i=0;i<this.monome.sizeY;i++) {
//						colArgs.add(0);
//					}
//					flow_led_col(colArgs, this.index);
//					this.sequencerModeRedrawCol(this.selectedChannel.sequencePosition % (this.monome.sizeX), 0);

					this.sequencerModeRedrawCol(this.selectedChannel.sequencePosition % (this.monome.sizeX), 0);
				}
			}
			
			// this is now done in the midi note off scheduler
			//this.playNotes(this.sequencePosition, 0);  //now handled with scheduler
			//			if(this.mode == KEYBOARDMODE)
			//				this.lightUpNotesOnKeyboard(this.sequencePosition, 0);
			
			if(this.mode == MATRIXMODE) 
				this.lightUpNotesInMatrixMode(selectedChannel, this.selectedChannel.sequencePosition, this.briMatrixDefault);
			
			for(int ci = 0; ci<NUMBER_OF_CHANNELS; ci++)
			{
				SequencerChannel loopChan = channels[ci];
				loopChan.sequencePosition++;
			}
		}

		if (this.mode == BANKMODE && this.tickNum % quantization == 0) {
			//blinking the active bank
			
			//			int x = selectedChannelNumber;
			//			int y = selectedChannel.playingBank;
			//			if (this.blinkThread != null) {
			//				this.blinkThread.cancel();
			//			}
			//			this.blinkThread = new LEDBlink(monome, x, y, 20, this.index);
			//			new Thread(this.blinkThread).start();
		}
		
		this.tickNum++;
	}

	public void turnOffScheduledNotes() {
		midiSchedulerPosition++;
		while(midiSchedulerPosition >= MAX_LEN_OF_MIDI_SCHEDULER)
			midiSchedulerPosition -= MAX_LEN_OF_MIDI_SCHEDULER;
		
		int now = midiSchedulerPosition;
		
		ShortMessage note_out = new ShortMessage();
		
		for(int i=0; i < midiNoteOffSchedule[now].size(); i++) {
			FlowMidiEvent midiev = (FlowMidiEvent) midiNoteOffSchedule[now].get(i);
			int midiChannel = midiev.midiChannel;
			int midino = midiev.midiNo;
			
			try {
				
				note_out.setMessage(ShortMessage.NOTE_OFF, midiChannel, midino, 0);
				
				String[] midiOutOptions = monome.getMidiOutOptions(this.index);
				for (int j = 0; j < midiOutOptions.length; j++) {
					if (midiOutOptions[j] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[j]);
					if (recv != null) {
						recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
					}
				}
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
			
			if(this.channels[midiev.sourceChannelNumber].showInKeyboardMode && this.mode == KEYBOARDMODE) {
				keyboardModeLedFromMidiNumber(midino, 0, selectedChannel);
			}
		}
		
		midiNoteOffSchedule[now].clear();
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		this.tickNum = 0;
		for(int ci = 0; ci<NUMBER_OF_CHANNELS; ci++)
		{
			SequencerChannel loopChan = channels[ci];
				loopChan.sequencePosition = 16*loopChan.patternsSelected[loopChan.selectedBank];
		}
		this.redrawDevice();
	}

	/**
	 * Redraws a column as the sequence position indicator passes by.
	 * 
	 * @param col The column number to redrawDevice
	 * @param val The value of the led_col message that triggered this redrawDevice
	 */
	private void sequencerModeRedrawCol(int col, int val) {
		if(this.mode != SEQUENCERMODE)
			return;
		int x_seq = (getSelectedPatternNumber() * (this.monome.sizeX)) + col;
		
		if(val > 0) {
			for(int i=0;i<this.monome.sizeY;i++) {
				if(selectedChannel.sequence[selectedChannel.selectedBank][x_seq][yToSeqY(i)]>0) { 
					flow_led(col, i,briNoteOn, this.index);
				} else if(this.selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][yToSeqY(i)] > 0) {
					flow_led(col, i,briNoteOn, this.index);
				} else {
					flow_led(col, i,briSeqBar, this.index);
				}
			}						
		}
		else if (val == 0) {
			for (int y = 0; y < (this.monome.sizeY - 1); y++) {
				int y_seq =yToSeqY(y);
				if (this.selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] > 0) {
					flow_led(col, y, this.briSeqNote, this.index);
				} else if(this.selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq] > 0) {
					flow_led(col, y, this.briSeqNoteLen, this.index);
				}
				else {
					flow_led(col, y, this.briOff, this.index);
				}
			}
//			if (col == this.pattern) {
//				flow_led(col, (this.monome.sizeY - 1), briOn, this.index);
//			}
			if (col == ButtonNoBank && this.mode == BANKMODE) {
				flow_led(col, (this.monome.sizeY - 1), briOn, this.index);
			}
			else if (col == ButtonNoKeyboard && this.mode == KEYBOARDMODE) {
				flow_led(col, (this.monome.sizeY - 1), briOn, this.index);
			} 
			else if (col == ButtonNoMatrix && this.mode == MATRIXMODE) {
				flow_led(col, (this.monome.sizeY - 1), this.briOn, this.index);
			}
			else if(col == ButtonNoVelocity && velocityMode == 1) {
				flow_led(col, (this.monome.sizeY - 1), this.briOn, this.index);
			}
			else if(col == ButtonNoMute && muteMode == 1) {
				flow_led(col, (this.monome.sizeY - 1), this.briOn, this.index);
			}
			else if (col == (this.monome.sizeX - 1)) {
				flow_led(col, (this.monome.sizeY - 1), 0, this.index);
			} 
			else {
					flow_led(col, (this.monome.sizeY - 1), 0, this.index);
			}
		}
	}
	
	public void stopNotes() {

	}

	/**
	 * Send MIDI note messages based on the sequence position.  If on = 0, note off will be sent.
	 * 
	 * @param seq_pos The sequence position to play notes for
	 * @param on Whether to turn notes on or off, a value of 1 means play notes
	 */
	public void playNotes(SequencerChannel chan, int seq_pos, int on) {
		if (muteMode == 1) {
			return;
		}
		
		ShortMessage note_out = new ShortMessage();
		int note_num;
		int velocity;
		int midiChannel = chan.midiChannel;
		for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
		// there used to be a hold mode
		// now only normal mode 
				if (chan.sequence[chan.playingBank][seq_pos][y] > 0) {
					if (on > 0) {
						velocity = (chan.sequence[chan.playingBank][seq_pos][y]);
					} else {
						velocity = 0;
					}
					note_num = chan.getNoteNumber(y);
					
					if (note_num < 0) {
						continue;
					}
					try {
						if (velocity == 0) {
							chan.heldNotes[y] = 0;
						} else {
							//System.out.print(y + "-" + note_num);
							note_out.setMessage(ShortMessage.NOTE_ON, midiChannel, note_num, velocity);
							chan.heldNotes[y] = 1;
						}
						String[] midiOutOptions = monome.getMidiOutOptions(this.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
							}
						}
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
					
					// light up notes on keyboard if necessary
					//					if (chan.showInKeyboardMode && this.mode == KEYBOARDMODE) {
					//						int bribri = briKeybOtherChannels;
					//						if(chan == selectedChannel)
					//							bribri = briKeybNotePlaying;
					//						
					//						keyboardModeLedFromNoteNumber(keyboardModeNoteNumberFromMidiNumber(note_num), bribri);
					//					}
					
					// schedule the note off for this one
					int newPos = midiSchedulerPosition + chan.seqNoteLengths[chan.playingBank][seq_pos][y]*this.quantization;
					while(newPos >= MAX_LEN_OF_MIDI_SCHEDULER)
						newPos -= MAX_LEN_OF_MIDI_SCHEDULER;
					FlowMidiEvent noteOff = new FlowMidiEvent(note_num, 0, 0, chan.midiChannel, chan.number);
					midiNoteOffSchedule[newPos].add(noteOff);
				}
			
		}
	}
	
	public void lightUpNotesOnKeyboard(SequencerChannel chan, int seq_pos, int value) {
		
		if (muteMode == 1 || !chan.showInKeyboardMode || chan.isDrums) {
			return;
		}
		
		int note_midi_num;
		
		for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
			if (chan.sequence[chan.playingBank][seq_pos][y] > 0) {
				note_midi_num = chan.getNoteNumber(y);
				if(value > 0) {
					if(chan != selectedChannel)
						value = briKeybOtherChannels;
				}
				keyboardModeLedFromMidiNumber(note_midi_num, value, this.selectedChannel);
			}
		}
	}

	public void  lightUpNotesInMatrixMode(SequencerChannel chan, int seq_pos, int value) {
		if (muteMode == 1) {
			return;
		}
		
		for (int y = 0; y < 16; y++) {
			int seqy = yToSeqY(y);
			if (chan.sequence[chan.playingBank][seq_pos][seqy] > 0) {
				flow_led(y, seqYtoY(rowSwap[seqy]), value, this.index);
			}
		}
	}

	/**
	
 * Convert a MIDI note number to a string, ie. "C-3".
	 * 
	 * @param noteNum The MIDI note number to convert
	 * @return The converted representation of the MIDI note number (ie. "C-3")
	 */
	public String numberToMidiNote(int noteNum) {
		int n = noteNum % 12;
		String note = "";
		switch (n) {
		case 0:
			note = "C"; break;
		case 1:
			note = "C#"; break;
		case 2:
			note = "D"; break;
		case 3:
			note = "D#"; break;
		case 4:
			note = "E"; break;
		case 5:
			note = "F"; break;
		case 6:
			note = "F#"; break;
		case 7:
			note = "G"; break;
		case 8: 
			note = "G#"; break;
		case 9:
			note = "A"; break;
		case 10:
			note = "A#"; break;
		case 11:
			note = "B"; break;
		}

		int o = (noteNum / 12) - 2;
		note = note.concat("-" + String.valueOf(o));
		return note;
	}

	/**
	 * Converts a note name to a MIDI note number (ie. "C-3").
	 * 
	 * @param convert_note The note to convert (ie. "C-3")
	 * @return The MIDI note value of that note
	 */
	public static int noteToMidiNumber(String convert_note) {		
		for (int n=0; n < 12; n++) {
			String note = "";
			switch (n) {
			case 0:
				note = "C"; break;
			case 1:
				note = "C#"; break;
			case 2:
				note = "D"; break;
			case 3:
				note = "D#"; break;
			case 4:
				note = "E"; break;
			case 5:
				note = "F"; break;
			case 6:
				note = "F#"; break;
			case 7:
				note = "G"; break;
			case 8: 
				note = "G#"; break;
			case 9:
				note = "A"; break;
			case 10:
				note = "A#"; break;
			case 11:
				note = "B"; break;
			}
			for (int o=0; o < 8; o++) {
				int note_num = (o * 12) + n;
				if (note_num == 128) {
					break;
				}
				String note_string = note + "-" + String.valueOf(o - 2);
				if (note_string.compareTo(convert_note) == 0) {
					return note_num;
				}
			}
		}
		return -1;
	}


	/**
	 * Set row number num to midi note value value.
	 * 
	 * @param num The row number to set (0 = Row 1)
	 * @param value The MIDI note value to set the row to
	 */
	
	public void setNoteValue(int num, int value) {
//		selectedChannel.noteNumbers[num] = value;
//		if (num == gui.rowCB.getSelectedIndex()) {
//			gui.noteTF.setText(this.numberToMidiNote(value));
//		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		int x_seq;
		int y_seq;
		
		// redraw in keyboard mode
		if(this.mode == KEYBOARDMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				for (int y = 0; y < (this.monome.sizeY); y++) {
					keyboardModeRedrawXYToDefault(x,y);					
				}
			}
			//put the keyboard mode light on
			flow_led(ButtonNoKeyboard, this.monome.sizeY-1, briOn*(this.mode == KEYBOARDMODE ? 1 : 0), this.index);
			flow_led(0,0,(keyboardRecordMode ? briOn : 0), this.index);
		}
		// redraw in matrix mode
		else if(this.mode == MATRIXMODE) {
			int y;
			for (int x = 0; x < (this.monome.sizeX); x++) {
				for (y = 0; y < (this.monome.sizeY - 1); y++) {
					if(y == seqYtoY(this.rowSwap[yToSeqY(x)]))
						flow_led(x, y, this.briMatrixDefault, this.index);
					else
						flow_led(x, y, 0, this.index);
				}
			}
			y = this.monome.sizeY;
			for (int x = 0; x < (this.monome.sizeX); x++)
				flow_led(x, y, 0, this.index);
			// light up matrix mode key
			flow_led(ButtonNoMatrix, this.monome.sizeY-1, this.briOn*(this.mode == MATRIXMODE ? 1 : 0), this.index);
			this.sequencerRedrawBottomRow();
		}
		// redrawDevice if we're in bank mode
		else if (this.mode == BANKMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					SequencerChannel curChannel = channels[x];
					int curBank = y;
					boolean bankData = false;
					
					if(curChannel == selectedChannel && curChannel.selectedBank == y) {
						flow_led(x, y, this.briBankSelectedInSelectedChannel, this.index);
					}
					else if(curChannel.playingBank == y) {
						flow_led(x, y, this.briBankPlaying, this.index);
					} else {
//						search:
//							for (int seqX = 0; seqX < SequencerChannel.MAX_SEQUENCE_LENGTH; seqX++) {
//								for (int seqY = 0; seqY < 16; seqY++) {
//									if (channels[x].sequence[curBank][seqX][seqY] > 0) {
//										bankData = true;
//										break search;
//									} 
//								}
//							}
//						
						if (bankData) {
							flow_led(x, y, this.briBankNonEmpty, this.index);
						} else {
							flow_led(x, y, this.briOff, this.index);
						}
					}
				}
			}
			// redrawDevice the bottom row
			this.sequencerRedrawBottomRow();
		// redrawDevice if we're in sequence mode
		} else if(this.mode == this.SEQUENCERMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				x_seq = xToSeqX(x);
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					y_seq = yToSeqY(y);
					int value = 0;
					if (this.selectedChannel.sequence[selectedChannel.selectedBank][x_seq][y_seq] > 0) {
						value = this.briSeqNote;
					} else if(this.selectedChannel.seqNoteLengths[selectedChannel.selectedBank][x_seq][y_seq] > 0)
						value = this.briSeqNoteLen;
					flow_led(x, y, value, this.index);
				}
			}
			// top layer
			patternModeRedraw();
			velocityModeRedraw();
			// redrawDevice the bottom row
			this.sequencerRedrawBottomRow();
		}
		
		
		
	}

	/**
	 * Redraws the bottom row of the sequencer page on the monome.
	 */
	public void sequencerRedrawBottomRow() {
		if (this.mode == BANKMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				if (x < 4) 
						flow_led(x, (this.monome.sizeY - 1) , 0, this.index);
				else if (x == ButtonNoClear) {
					flow_led(x, (this.monome.sizeY - 1), this.bankClearMode*this.briOn, this.index);
				}
				else if (x == ButtonNoBank) {
					flow_led(x, (this.monome.sizeY - 1), (this.mode == BANKMODE ? 1 : 0)*this.briOn, this.index);
				}
				else if (x == ButtonNoMute) {
					flow_led(x, (this.monome.sizeY - 1), this.muteMode*this.briOn, this.index);
				}
				else if (x == ButtonNoVelocity) {
					flow_led(x, (this.monome.sizeY - 1), this.velocityMode*this.briOn, this.index);
				}
				else {
					flow_led(x, (this.monome.sizeY - 1), 0, this.index);
				}
			}
			// bottom row is this way if we're not in bank mode (usually sequencer mode)
		} else {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				if(x == ButtonNoPattern) {
					if(topLayerMode == TopLayerPatternMode)
						flow_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
					else 
						flow_led(x, (this.monome.sizeY - 1), 0, this.index);
				}
				else if (x == ButtonNoMute) {
					flow_led(x, (this.monome.sizeY - 1), this.muteMode*this.briOn, this.index);
				}
				else if (x == ButtonNoVelocity) {
					flow_led(x, (this.monome.sizeY - 1), this.velocityMode*this.briOn, this.index);
				}
				else if(x == ButtonNoMatrix) {
					flow_led(x, (this.monome.sizeY - 1), (this.mode == MATRIXMODE ? 1 : 0)*this.briOn, this.index);
				}
				else {
					flow_led(x, (this.monome.sizeY - 1), 0, this.index);
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getName()
	 */	
	public String getName() 
	{		
		return pageName;
	}
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#setName()
	 */
	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getPanel()
	 */
	public JPanel getPanel() {
		return gui;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		return;
	}

	/**
	 * Generates a random sequencer pattern on the current bank.
	 */
	private void generateSequencerPattern() {
		// pattern template to use
//		int[][] p1 = {
//				{2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0}, // 1
//				{0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,1,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,1,0}, // 2
//				{0,0,2,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 3
//				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 4
//				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0}, // 5
//				{0,0,0,0,0,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0}, // 6
//				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,1, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,1}, // 7
//				{0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 8
//				{2,1,0,0,2,0,2,0, 2,0,2,0,2,1,0,0, 1,2,0,0,0,1,2,1, 2,0,1,0,0,2,0,1, 2,1,0,0,2,0,2,0, 2,0,2,0,2,1,0,0, 1,2,0,0,0,1,2,1, 2,0,1,0,0,2,0,1}, // 9
//				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,1, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,1}, // 10
//				{0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,0,0,0,0,0,0}, // 11
//				{2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 12
//				{0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 13
//				{0,0,2,0,0,0,0,0, 0,0,1,0,0,0,0,0, 1,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,2,0,0,0,0,0, 0,0,1,0,0,0,0,0, 1,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0}  // 14
//		};
//		// randomly turn things on and off
//		for (int x = 0; x < this.selectedChannel.MAX_SEQUENCE_LENGTH; x++) {
//			for (int y = 0; y < 14; y++) {
//				selectedChannel.sequence[selectedChannel.selectedBank][x][y] = p1[y][x];
//				if (generator.nextInt(20) == 1) {
//					selectedChannel.sequence[selectedChannel.selectedBank][x][y] = 1;
//				}
//				if (generator.nextInt(10) == 1) {
//					selectedChannel.sequence[selectedChannel.selectedBank][x][y] = 2;
//				}
//				if (generator.nextInt(6) == 1) {
//					selectedChannel.sequence[selectedChannel.selectedBank][x][y] = 0;
//				}
//			}
//		}

	}

	/**
	 * Alters the current sequencer patterns. 
	 */
	private void alterSequencerPattern() {
		// randomly turn things on or off
		for (int x = 0; x < MAX_SEQUENCE_LENGTH; x++) {
			for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
				if (selectedChannel.sequence[selectedChannel.selectedBank][x][y] > 0) {
					if (generator.nextInt(30) == 1) {
						selectedChannel.sequence[selectedChannel.selectedBank][x][y] = generator.nextInt(127);
					}
				}
				if (selectedChannel.sequence[selectedChannel.selectedBank][x][y] == 0) {
					if (generator.nextInt(150) == 1) {
						selectedChannel.sequence[selectedChannel.selectedBank][x][y] = generator.nextInt(127);
					}
				}

			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		StringBuffer xml = new StringBuffer();
		int holdmode = 0;
		xml.append("      <name>MIDI Sequencer</name>\n");
		xml.append("      <pageName>" + this.pageName + "</pageName>\n");
		if (this.gui.getHoldModeCB().isSelected() == true) {
			holdmode = 1;
		}
		xml.append("      <holdmode>" + holdmode + "</holdmode>\n");
		xml.append("      <banksize>" + MAX_SEQUENCE_LENGTH + "</banksize>\n");
		xml.append("      <midichannel>" + this.selectedChannel.midiChannel + "</midichannel>\n");
		xml.append("      <sequencerQuantization>" + this.quantization + "</sequencerQuantization>\n");
		xml.append("      <muteMode>" + this.muteMode + "</muteMode>\n");
		xml.append("      <velocityMode>" + this.velocityMode + "</velocityMode>\n");
		for (int i=0; i < SEQUENCE_HEIGHT; i++) {
			xml.append("      <row>" + String.valueOf(this.selectedChannel.noteNumbers[i]) + "</row>\n");
		}
		for(int chnum = 0; chnum < NUMBER_OF_CHANNELS; chnum++) {
			for (int i=0; i < NUMBER_OF_BANKS; i++) {
				xml.append("      <sequence>");
				for (int j=0; j < MAX_SEQUENCE_LENGTH; j++) {
					for (int k=0; k < SEQUENCE_HEIGHT; k++) {
						xml.append(this.channels[0].sequence[i][j][k]);
						xml.append(" ");
						xml.append(this.channels[0].seqNoteLengths[i][j][k]);
						xml.append(" ");
					}
				}
				xml.append("</sequence>\n");
			}
		}
		return xml.toString();
	}


	/**
	 * Loads a sequence from a configuration file.  Called from GUI on open configuration action.
	 * 
	 * @param chnum
	 * @param bnum
	 * @param sequence2
	 */
	public void setSequence(int chnum, int bnum, String sequence2) {
		int row = 0;
		int pos = 0;
		
		//System.out.println(sequence2);
		//System.out.println("");
		Scanner scan = new Scanner(sequence2);
		
		for (int i=0; i < MAX_SEQUENCE_LENGTH*SEQUENCE_HEIGHT; i++) {

			if (row == SEQUENCE_HEIGHT) {
				row = 0;
				pos++;
			}
			
			this.channels[chnum].sequence[bnum][pos][row] = scan.nextInt();
			this.channels[chnum].seqNoteLengths[bnum][pos][row] = scan.nextInt();
			//System.out.print(channels[0].sequence[l][pos][row] + " ");
			row++;
		}
	}

	
		
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getCacheDisabled()
	 */
	public boolean getCacheDisabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#destroyPage()
	 */
	public void destroyPage() {
		return;
	}
	
	public void setHoldMode(String holdmode) {
		if (holdmode.equals("1")) {
			this.gui.getHoldModeCB().doClick();
		}
	}
	
	public void setMidiChannel(String midiChannel2) {
		this.selectedChannel.midiChannel = Integer.parseInt(midiChannel2);
		this.gui.channelTF.setText(midiChannel2);
	}
	
		
	public void setIndex(int index) {
		this.index = index;
	}

	public void handleADC(int adcNum, float value) {
		// TODO Auto-generated method stub
		
	}
	public void handleADC(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	public boolean isTiltPage() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		this.setHoldMode(this.monome.readConfigValue(pageElement, "holdmode"));
		//this.setBankSize(Integer.parseInt(this.monome.readConfigValue(pageElement, "banksize")));
		this.setMidiChannel(this.monome.readConfigValue(pageElement, "midichannel"));
		this.setQuantization(this.monome.readConfigValue(pageElement, "sequencerQuantization"));
		String sMuteMode = this.monome.readConfigValue(pageElement, "muteMode");
		if (sMuteMode != null) {
			this.muteMode = Integer.parseInt(sMuteMode);
		}
		String sVelocityMode = this.monome.readConfigValue(pageElement, "velocityMode");
		if (sVelocityMode != null) {
			this.velocityMode = Integer.parseInt(sVelocityMode);
		}
		
		NodeList rowNL = pageElement.getElementsByTagName("row");		
		for (int l=0; l < rowNL.getLength(); l++) {		
			Element el = (Element) rowNL.item(l);		
			NodeList nl = el.getChildNodes();		
			String midiNote = ((Node) nl.item(0)).getNodeValue();		
			this.setNoteValue(l, Integer.parseInt(midiNote));		
		}		
		
		NodeList seqNL = pageElement.getElementsByTagName("sequence");
		for(int chnum=0; chnum < NUMBER_OF_CHANNELS; chnum++) {
			for (int bnum=0; bnum < NUMBER_OF_BANKS; bnum++) {
				Element el = (Element) seqNL.item(bnum);		
				NodeList nl = el.getChildNodes();		
				String sequence = ((Node) nl.item(0)).getNodeValue();		
				this.setSequence(chnum, bnum, sequence);		
			}
		}
		this.redrawDevice();
	}

	private void setQuantization(String quantization) {
	    try {
	        this.quantization = Integer.parseInt(quantization);
	        if (this.quantization == 96) {
	            this.gui.quantCB.setSelectedIndex(0);
	        } else if (this.quantization == 48) {
	            this.gui.quantCB.setSelectedIndex(1);
	        } else if (this.quantization == 24) {
	            this.gui.quantCB.setSelectedIndex(2);
	        } else if (this.quantization == 12) {
	            this.gui.quantCB.setSelectedIndex(3);
	        } else if (this.quantization == 6) {
	            this.gui.quantCB.setSelectedIndex(4);
	        } else if (this.quantization == 3) {
	            this.gui.quantCB.setSelectedIndex(5);
	        }
	    } catch (NumberFormatException e) {
	        return;
	    }
	}
	
	

	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}
	
	public void handleAbletonEvent() {
	}

	public void onBlur() {
		// TODO Auto-generated method stub
		
	}
	
    public void handleRecordedPress(int x, int y, int val, int pattNum) {
        handlePress(x, y, val);
    }

	public void handleTilt(int n, int x, int y, int z) {
		// TODO Auto-generated method stub
		
	}

	// keyboard mode helper functions, onOff==0 for note off message
	public void playNote(int note_num, int velocity, int flowChannel, int onOff) {
		ShortMessage note_out = new ShortMessage();
		int channel = selectedChannel.midiChannel;
		try {
			if (onOff == 0) {
				note_out.setMessage(ShortMessage.NOTE_OFF, channel, note_num, velocity);				
			} else {
				note_out.setMessage(ShortMessage.NOTE_ON, channel, note_num, velocity);				
			}
			String[] midiOutOptions = monome.getMidiOutOptions(this.index);
			for (int i = 0; i < midiOutOptions.length; i++) {
				if (midiOutOptions[i] == null) {
					continue;
				}
				Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
				if (recv != null) {
					recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
				}
			}
			
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public int keyboardModeNoteNumberToMidiNumber(int nn, SequencerChannel chan) {
		int offset = 0;
		int note;
		
		int transp = this.transpose * 12;
		
		if(nn>=0) { 
			while (nn >= chan.scaleLength) {
				nn -= chan.scaleLength;
				offset += chan.scaleTotalInSemitones;
			}
		} else {
			while (nn<0) {
				nn += chan.scaleLength;
				offset -= chan.scaleTotalInSemitones;
			}
		}
				
		note = chan.rootNoteMidiNumber + chan.scaleNoteDiffsToRoot[nn] + offset;
		note += (transp + this.accidental);
		
		return note;
	}

	public int midiNumberToX(int num) {
		
		return 0;
	}
	
	public int midiNumberToY(int num) {
		
		return 0;
	}

	public int keyboardModeNoteNumberFromMidiNumber(int num, SequencerChannel chan) {
		int offset = 0;
		//bring the number between rootNoteMidiNumber and rootNoteMidiNumber+scaletotalinsemitones
		while(num >= chan.rootNoteMidiNumber + chan.scaleTotalInSemitones) {
			num -= chan.scaleTotalInSemitones;
			offset += chan.scaleLength;
		}
		while(num < chan.rootNoteMidiNumber) {
			num += chan.scaleTotalInSemitones;
			offset -= chan.scaleLength;
		}
		//calculate difference to the root
		num -= chan.rootNoteMidiNumber;
		for(int i=0; i<chan.scaleLength; i++) {
			if(chan.scaleNoteDiffsToRoot[i] == num) {
				return i + offset;
			}
		}
		return -100;
	}
	
	public int keyboardModeXYToNoteNumber(int x, int y, SequencerChannel chan) {
		return (x - chan.rootNoteX) + chan.rowOffset*(chan.rootNoteY-y);
	}
	
	public void generateIsMidiNumberInScale(SequencerChannel chan) {
		int start = chan.rootNoteMidiNumber;
		while(start>0) {
			start -= chan.scaleTotalInSemitones;
		}
		start += chan.scaleTotalInSemitones;
		
		int i = 0;
		for(i=0;i<128;i++)
			chan.isMidiNumberInScale[i] = false;
		
		int oct = 0;
		int scaleIndex = 0;
		int midinum = start;
		
		while(midinum < 128) {
			chan.isMidiNumberInScale[midinum] = true;
			scaleIndex++;
			if(scaleIndex>=chan.scaleLength) {
				scaleIndex = 0;
				oct++;
			}
			midinum = start + oct*chan.scaleTotalInSemitones + chan.scaleNoteDiffsToRoot[scaleIndex];
		}
	}
	
	public void keyboardModeLedFromMidiNumber(int num, int value, SequencerChannel chan) {
		if(chan.isMidiNumberInScale[num])
			keyboardModeLedFromNoteNumber(keyboardModeNoteNumberFromMidiNumber(num, chan), value, chan);
	}
	
	// turns on/off all led's corresponding to the note (number not midi number)
	public void keyboardModeLedFromNoteNumber(int num, int value, SequencerChannel chan) {
		
		int x=0;
		int y=monome.sizeY-1;
		int numBottomLeft = keyboardModeXYToNoteNumber(0, this.monome.sizeY-1, chan);
		int dumNum=0;
		
		if(num < numBottomLeft) // note too low to exist on grid
			return;
		
		//find x,y for note with smallest x
		if(chan.rowOffset == 0) { // no need to make this work if offset is zero
			// not tested
			y = 0;
			x = chan.rootNoteX + num;
			
			//don't change the mode buttons
			if(y == this.monome.sizeY && x > 11)
				return;
			
			if(value>0)
				flow_led(x, y, value, this.index);
			else
				keyboardModeRedrawXYToDefault(x,y);
		}
		else {
			while(numBottomLeft <= num) {
				numBottomLeft += chan.rowOffset;
				y-=1;
			}
			y++; 
			x = num - (numBottomLeft - chan.rowOffset);
			dumNum = 0;
			// light up or down all corresponding notes
			while(y<this.monome.sizeY && x < this.monome.sizeX) {
				//don't change the mode buttons
				if(y == this.monome.sizeY && x > 11)
					continue;
				if(value>0)
					flow_led(x, y, value, this.index);
				else
					keyboardModeRedrawXYToDefault(x,y);	
				y++;
				x += chan.rowOffset;
			}	
		}
	}	


	public void keyboardModeRedrawXYToDefault(int x, int y) {
		if(keyboardModeXYToNoteNumber(x, y, this.selectedChannel) % selectedChannel.scaleLength == 0) {
			flow_led(x, y, briKeybRoot, this.index);
		}
		else {
			flow_led(x, y, briOff, this.index);
		}
	}

	
//	public void keyboardModeRecordNote() {
//		
//	}
//	
	
	
	/**
	 * Copies src bank to dst bank.
	 *
	 * @param src The source bank to copy
	 * @param dst The destination to copy the source bank to
	 */
	
	public void copyBank(int srcChannel, int srcBank, int dstChannel, int dstBank) {
		for (int x = 0; x < MAX_SEQUENCE_LENGTH; x++) { //replace with SequencerChannel.MAX_SEQUENCE_LENGTH
			for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
				channels[dstChannel].sequence[dstBank][x][y] = channels[srcChannel].sequence[srcBank][x][y];
				channels[dstChannel].seqNoteLengths[dstBank][x][y] = channels[srcChannel].seqNoteLengths[srcBank][x][y];
			}
		}
	}

	
	// converts x coord on monome to coord on sequence
	public int xToSeqX(int x) {
		return selectedChannel.patternsSelected[selectedChannel.selectedBank]*this.monome.sizeX + x;
	}
	
	// converts y coord on monome to y coord on sequence
	public int yToSeqY(int y) {
		return selectedChannel.depth*DEPTHINCREMENT + y;
	}
	
	public int seqYtoY(int seqy) {
		if(seqy<yToSeqY(0) || seqy+1 > yToSeqY(this.monome.sizeY-1)) 
			return -1;
		else 
			return seqy - yToSeqY(0); 
	}
	
	
	// sequencer channels, these are the columns of the bank mode
		// a single one can be played at a time, much like ableton live's clip launcher
		public class SequencerChannel {
			
			
			private MonomeConfiguration monome;
			private int index;
			
			
			public boolean isDrums = false;
// these are to save the gui info for reloading
//			public int selectedScaleIndex;
//			public int guiSelectedChannelIndex;
//			public String rootNoteText;
			
			/**
			 * selectedChannel.sequence[bank_number][width][height] - the currently programmed sequences 
			 */
			private int[][][] sequence = new int[NUMBER_OF_BANKS][MAX_SEQUENCE_LENGTH][SEQUENCE_HEIGHT];
			
			private int[][][] seqNoteLengths = new int[NUMBER_OF_BANKS][MAX_SEQUENCE_LENGTH][SEQUENCE_HEIGHT]; // in steps
			
			private int selectedBank;
			private int playingBank;
			
			private int newNoteLength = 1; // in number of steps

			/**
			 * The current position in the sequence (from 0 to 31)
			 */
			private int sequencePosition = 0;

			/**
			 * heldNotes[note] - whether or not each note is currently held 
			 */
			private int[] heldNotes = new int[SEQUENCE_HEIGHT];
			
			/**
			 * noteNumbers[row] - midi note numbers that are sent for each row in the sequencer
			 */
			public int[] noteNumbers = new int[SEQUENCE_HEIGHT];
			
			// keyboard mode scale data
			private int rootNoteX = 2; //placement of the root note in keyboard mode
			private int rootNoteY = 14;
			public int rowOffset = 7; //in number of notes in scale not semitones!
			
			public int rootNoteMidiNumber = 24; // default is C1
			public int[] scaleNoteDiffsToRoot = {0,2,4,5,7,9,11,-99,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // default is major 
			public int scaleLength = 7;
			public int scaleTotalInSemitones = 12;
			
			public boolean[] isMidiNumberInScale = new boolean[128];
			
			/**
			 * 64/40h/128 only, 1 = edit the 2nd page of sequence lanes 
			 */
			private int depth = 0;
			
			
			/**
			 * The size of each bank in steps
			 */
			//public int bankSize = 16;

			public int midiChannel = 1;
			
			public boolean showInKeyboardMode = true;
			
			public int number = 0; // the index of this channel in array, useful for midi note off scheduler
			
			public int[] patternsSelected = new int[NUMBER_OF_BANKS];
			public int[] patternsStarting = new int[NUMBER_OF_BANKS];
			public int[] patternsEnding = new int[NUMBER_OF_BANKS]; // one more than the last pattern played
			
			// scheduled changes so things don't change immediately when switching
			public boolean scheduledChangeAtEndOfBarExists = false;
			public int scheduledChangeBank = 0;
			
			public SequencerChannel(MonomeConfiguration mnm, int index)
			{
				this.selectedBank = 0;
				this.playingBank = 17;
				this.index = index;
				this.monome = mnm;
				
				for(int i=0;i<NUMBER_OF_BANKS;i++) {
	        		for(int j=0; j<MAX_SEQUENCE_LENGTH;j++)
	        			for(int k=0;k<SEQUENCE_HEIGHT;k++) {
	        				seqNoteLengths[i][j][k] = 0;
	        				sequence[i][j][k] = 0; 
	        			}
	        		
	        		patternsSelected[i] = 0;
	        		patternsStarting[i] = 0;
	        		patternsEnding[i] = 1; // one more than the last pattern to be played
				}
				
				
				int seqy = 0;
				outerloop:
				for(int oct=0;oct<100;oct++) {
					for(int i=0;i<scaleLength;i++) {
						this.noteNumbers[seqy] = rootNoteMidiNumber + oct*scaleTotalInSemitones + scaleNoteDiffsToRoot[i];
						//System.out.println(seqy + " " + noteNumbers[seqy]);
						seqy++;
						if(seqy>=SEQUENCE_HEIGHT)
							break outerloop;
					}
				}				
			}

			/**
			 * Get the MIDI note number for a sequence lane (row)
			 * 
			 * @param y The row / sequence lane to get the MIDI note number for
			 * @return The MIDI note number assigned to that row / sequence lane
			 */
			public int getNoteNumber(int y) {
				//System.out.println(y + " rsto " + rowSwap[y] + "  noteto " + noteNumbers[rowSwap[y]]);
				return noteNumbers[rowSwap[y]];
			}


			/**
			 * Clears a bank.
			 * 
			 * @param dst The bank number to clear.
			 */
			public void clearBank(int dst) {
				for (int x = 0; x < MAX_SEQUENCE_LENGTH; x++) {
					for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
						sequence[dst][x][y] = 0;
						seqNoteLengths[dst][x][y] = 0;
					}
				}
			}
			
			/**
			 * Clear a pattern in the currently selected bank.
			 * 
			 * @param dst destination pattern to clear (0-3)
			 */
			private void clearPattern(int dst) {
				for (int x = 0; x < this.monome.sizeX; x++) {
					for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
						int x_dst = x + (dst * (this.monome.sizeX));
						sequence[this.selectedBank][x_dst][y] = 0;
						seqNoteLengths[this.selectedBank][x_dst][y] = 0;
					}
				}
			}

			/**
			 * Copies src pattern to dst pattern.
			 * 
			 * @param src The source pattern to copy (0-15)
			 * @param dst The destination to copy the source pattern to (0-15)
			 * these move multiples of monome.sizeX (assumed = 16)
			 */
			private void copyPattern(int src, int dst) {

				for (int y = 0; y < SEQUENCE_HEIGHT; y++) {
					for (int x = 0; x < (this.monome.sizeX); x++) {
						int x_src = x + (src * (this.monome.sizeX));
						int x_dst = x + (dst * (this.monome.sizeX));
						sequence[selectedBank][x_dst][y] = sequence[selectedBank][x_src][y];
						seqNoteLengths[selectedBank][x_dst][y] = seqNoteLengths[selectedBank][x_src][y];
					}
					reGenerateNoteLengthArrayRow(selectedBank, y, dst * (this.monome.sizeX), ((dst+1) * (this.monome.sizeX)));
				}
			}

			
			// fixes a row's note length data, to be used in the case of changes where the note length data is altered
			// eg: xstart and xend would be 0 to 16, this would stop at 15
			public void reGenerateNoteLengthArrayRow(int bnk, int yseq, int xstart, int xend) {
				int xseq;
				
				int i = 0;
				
				if(xstart == 0) {
					if(this.sequence[bnk][xstart][yseq] == 0)
						this.seqNoteLengths[bnk][xstart][yseq] = 0;
					i = 1;
				} 
				
				for(; xstart + i < xend; i++) {
					xseq = xstart + i;
					if(this.sequence[bnk][xseq][yseq] == 0) {
						if(this.seqNoteLengths[bnk][xseq-1][yseq] > 0) {
							// look at the previous one and act accordingly
							seqNoteLengths[bnk][xseq][yseq] = this.seqNoteLengths[bnk][xseq-1][yseq] - 1;
						} else {
							seqNoteLengths[bnk][xseq][yseq] = 0;
						}
					} else { // if there is a note at xseq,yseq
						
					}
				}
				
				if(xend == MAX_SEQUENCE_LENGTH) 
					return;
				
				// keep going if there is still a note to continue
				if(this.seqNoteLengths[bnk][xend-1][yseq] > 1) {
					for(i = 1; i < this.seqNoteLengths[bnk][xend-1][yseq]; i++) {
						xseq = xend - 1 + i;
						if(this.sequence[bnk][xseq][yseq] > 0)
							break;
						if(this.seqNoteLengths[bnk][xseq - 1][yseq] > 0)
							this.seqNoteLengths[bnk][xseq][yseq] = this.seqNoteLengths[bnk][xseq - 1][yseq] - 1;
						else
							break;
					}
				}
					
			}
	
			public int getPlayingPatternEnding() {
				return patternsEnding[selectedChannel.playingBank];
			}

			public int getPlayingPatternStarting() {
				return patternsStarting[selectedChannel.playingBank];
			}

		} // end of sequencerchannel class

		
	public int getSelectedPatternEnding() {
		return selectedChannel.patternsEnding[selectedChannel.selectedBank];
	}

	public int getSelectedPatternStarting() {
		return selectedChannel.patternsStarting[selectedChannel.selectedBank];
	}
	
	public int getSelectedPatternNumber() {
		return selectedChannel.patternsSelected[selectedChannel.selectedBank];
	}

		
	
	
	// new function for LED on-off.
	// for situations when everything runs normally, but part of the monome is beiing used for something else
	// such as parameter editing
	public void flow_led(int x, int y, int value, int index) {
		if(topLayerMode == TopLayerInactive)
			this.monome.vari_led(x, y, value, index);
		else {
			if(topLayerMode == TopLayerVelocityNoteLengthAndCCMode) {
				if(x < velocityModeXStart - velocityModeMoveLeftToPreventOverlap 
						|| x >= velocityModeXStart - velocityModeMoveLeftToPreventOverlap + velocityModeWidth
						|| y < velocityModeYStart || y >= velocityModeYStart + velocityModeHeight)
					this.monome.vari_led(x, y, value, index);
				else {
					return;
				}
			} else if(topLayerMode == TopLayerPatternMode) {
				if(x < patternModeXStart || x >= patternModeXEnd || y < patternModeYStart || y >= patternModeYEnd)
					this.monome.vari_led(x, y, value, index);
				else {
					// in pattern mode and one of the top layer places was active, ignore
					return;
				}
			} 
		}
	}
	
	public void flow_led_col(ArrayList<Integer> intArgs, int index) {
		this.monome.vari_led_col(intArgs, index);
	}
	
	public void superdebug() {
		for(int i=0;i<SEQUENCE_HEIGHT;i++)
			System.out.println(selectedChannel.noteNumbers[i]);
		for(int i=0;i<4;i++)
			System.out.println(channels[i].depth);
	}
	
	
} // end of class


