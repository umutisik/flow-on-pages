package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

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
	public int ButtonNoBank = 12;
	public int ButtonNoKeyboard = 14;
	public int ButtonNoMatrix = 13;
	public int LastModeButton = 12;
	
	public int ButtonNoVelocity = 10;
	public int ButtonNoMute = 11;
	public int ButtonNoCopy = 4;
	public int ButtonNoClear = 5;
	// 0
	/**
	 * The current MIDI clock tick number (from 0 to 6)
	 */
	private int tickNum = 0;

	/**
	 * The current position in the sequence (from 0 to 31)
	 */
	private int sequencePosition = 0;

	/**
	 * The selected pattern (0 to 3) 
	 */
	private int pattern = 0;

	
/****************** keyboard mode vars (umut) ***************/
	/**
	 * Natural:0, Sharp:1, Flat:-1 
	 */
	private int accidental = 0;
	
	/**
	 * Number to transpose the keyboard by
	 */
	private int transpose = 0;
	
	private int rootNoteX = 2; //placement of the root note in keyboard mode
	private int rootNoteY = 14;
	private int rowOffset = 7; //in number of notes in scale not semitones!
	
	public int rootNoteMidiNumber = 36; // default is C1
	public int[] scaleNoteDiffsToRoot = {0,2,4,5,7,9,11,-99,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // default is major 
	public int scaleLength = 7;
	public int scaleTotalInSemitones = 12;
	

//	
/****************** end keyboard mode vars   ***************/

	
/****************** matrix mode vars   ***************/
	// steps top to bottom
	private int[] rowSwap = new int[16]; 
	
	
/****************** end matrix mode vars   ***************/	
	
/**** midi note off scheduling *****/
	
	public static final int MAX_LEN_OF_MIDI_SCHEDULER = 2048; // in ticks!!!
	
	private ArrayList[] midiNoteOffSchedule = new ArrayList[MAX_LEN_OF_MIDI_SCHEDULER]; 
	
	private int midiSchedulerPosition; 
	
	 
/*********** */
	/**
	 * sequence[bank_number][width][height] - the currently programmed sequences 
	 */
	public static final int MAX_SEQUENCE_LENGTH = 128;
	public static final int NUMBER_OF_BANKS = 260;
	
	private int[][][] sequence = new int[NUMBER_OF_BANKS][MAX_SEQUENCE_LENGTH][16];
	private int[][][] seqNoteLengths = new int[NUMBER_OF_BANKS][MAX_SEQUENCE_LENGTH][16]; // in steps
	
	private int newNoteLength = 4; // in number of steps
	
	/**
	 * flashSequence[bank_number][width][height] - the flashing state of leds 
	 */
	private int[][][] flashSequence = new int[240][64][16];
	
	/**
	 * heldNotes[bank_number][note] - whether or not each note is currently held 
	 */
	private int[] heldNotes = new int[16];
	
	/**
	 * noteNumbers[row] - midi note numbers that are sent for each row in the sequencer
	 */
	public int[] noteNumbers = new int[16];

	/**
	 * 64/40h/128 only, 1 = edit the 2nd page of sequence lanes 
	 */
	private int depth = 0;

	/**
	 * 1 = bank clear mode enabled
	 */
	private int bankClearMode = 0;

	/**
	 * 1 = bank copy mode enabled 
	 */
	private int bankCopyMode = 0;

	/**
	 * Currently selected bank number
	 */
	private int bank = 0;
	
	/**
	 * The size of each bank in steps
	 */
	public int bankSize = 32;

	/**
	 * 1 = pattern copy mode enabled
	 */
	private int copyMode = 0;

	/**
	 * 1 = pattern clear mode enabled
	 */
	private int clearMode = 0;
	
	public int quantization = 6;

	/**
	 * Random number generator
	 */
	private Random generator = new Random();

	public String midiChannel = "1";

	/**
	 * The name of the page 
	 */
	private String pageName = "Flow Page";

	private LEDBlink blinkThread;

	private int muteMode;

	private int velocityMode;

    private Dimension origGuiDimension;

    // brighness settins for different situations
    // sequenced notes
    private int briSeqNote = 12;
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
    
    
    // matrix mode brightness levels
    private int briMatrixDefault = 6;
    private int briMatrixNoteOn = 15;
    
    // default on and off (off is for testing)
    private int briOn = 12;
    private int briOff = 0;
    
    
    
	/**
	 * @param monome The MonomeConfiguration that this page belongs to
	 * @param index The index of this page (the page number)
	 */
	public Flow(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		this.gui = new FlowGUI(this);
		// setup default notes
		gui.channelTF.setText(midiChannel);
		gui.bankSizeTF.setText(""+bankSize);
		this.noteNumbers[0] = this.noteToMidiNumber("C-1");
		this.noteNumbers[1] = this.noteToMidiNumber("D-1");
		this.noteNumbers[2] = this.noteToMidiNumber("E-1");
		this.noteNumbers[3] = this.noteToMidiNumber("F-1");
		this.noteNumbers[4] = this.noteToMidiNumber("G-1");
		this.noteNumbers[5] = this.noteToMidiNumber("A-1");
		this.noteNumbers[6] = this.noteToMidiNumber("B-1");
		this.noteNumbers[7] = this.noteToMidiNumber("C-2");
		this.noteNumbers[8] = this.noteToMidiNumber("D-2");
		this.noteNumbers[9] = this.noteToMidiNumber("E-2");
		this.noteNumbers[10] = this.noteToMidiNumber("F-2");
		this.noteNumbers[11] = this.noteToMidiNumber("G-2");
		this.noteNumbers[12] = this.noteToMidiNumber("A-2");
		this.noteNumbers[13] = this.noteToMidiNumber("B-2");
		this.noteNumbers[14] = this.noteToMidiNumber("C-3");
		this.noteNumbers[15] = this.noteToMidiNumber("D-3");
		this.setQuantization("6");
        origGuiDimension = gui.getSize();

        //initialize sequence note lengths record
        
        for(int i=0;i<NUMBER_OF_BANKS;i++)
        	for(int j=0; j<MAX_SEQUENCE_LENGTH;j++)
        		for(int k=0;k<16;k++) {
        			this.seqNoteLengths[i][j][k] = 0;
        			this.sequence[i][j][k] = 0; 
        		}
        			
        //initialize step swapping
    	for(int i=0;i<this.monome.sizeX;i++)
    		this.rowSwap[i]=i;

    	// initialize midi note off scheduler
		midiSchedulerPosition = 0;
		for(int i = 0; i<MAX_LEN_OF_MIDI_SCHEDULER; i++) {
			midiNoteOffSchedule[i] = new ArrayList();
		}

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
			
			if (y != (this.monome.sizeY - 1) || x < LastModeButton) { //if page change or keyboard mode button aren't the ones pressed
				int velocity = value * 127;
				int channel = 1;
				
				int midi_num = this.keyboardModeNoteNumberToMidiNumber((x - rootNoteX) + rowOffset*(rootNoteY-y));
				if (midi_num > 127) midi_num = 127;
				if (midi_num < 0) midi_num = 0;				
				this.playNote(midi_num, velocity, channel, value);
				keyboardModeLedFromNoteNumber((x - rootNoteX) + rowOffset*(rootNoteY-y), briKeybSameNote*value);
				// extra brightness for the key actually pressed
				if(value>0)
					this.monome.vari_led(x, y, this.briKeybNotePressed*value, this.index);
				else
					keyboardModeRedrawXYToDefault(x,y);	
					
			}
			return;
		}
		else if(this.mode == MATRIXMODE) {
			if(y !=this.monome.sizeY-1) // not a bottom row button
			{
				this.monome.vari_led(x, this.rowSwap[x], 0, this.index);
				this.monome.vari_led(x, y, briMatrixDefault, this.index);
				rowSwap[x]=y;
			}
			return;	    
		}
		else if (this.mode == BANKMODE) {
			// only on press events
			if (value == 1) {
				if (this.blinkThread != null) {
					this.blinkThread.cancel();
				}
				if (y == (this.monome.sizeY - 1)) {
					if (x < 2) { // depth setting for 64 or 128
						if (this.monome.sizeY == 8) {
							this.depth = x;
							this.redrawDevice();
						}
					}
					if (x == 2) {
						this.stopNotes();
						this.generateSequencerPattern();
					}
					if (x == 3) {
						this.stopNotes();
						this.alterSequencerPattern();
					}
					if (x == ButtonNoClear && this.bankClearMode == 0) {
						if (this.bankCopyMode == 1) {
							this.bankCopyMode = 0;
							this.monome.vari_led(x, this.monome.sizeY-1, this.briOff, this.index);
						} else {
							this.bankCopyMode = 1;
							this.monome.vari_led(x, this.monome.sizeY-1, this.briOn, this.index);
						}
					}
					if (x == ButtonNoCopy && this.bankCopyMode == 0) {
						if (this.bankClearMode == 1) {
							this.bankClearMode = 0;
							this.monome.vari_led(x, this.monome.sizeY-1, this.briOff, this.index);
						} else {
							this.bankClearMode = 1;
							this.monome.vari_led(x, this.monome.sizeY-1, this.briOn, this.index);
						}
					}

					if (x == ButtonNoMute && this.copyMode == 0 && this.clearMode == 0) {
						if (this.muteMode == 0) {
							this.muteMode = 1;
						} else {
							this.muteMode = 0;
						}
						this.redrawDevice();
					}

					if (x == ButtonNoClear && this.copyMode == 0 && this.clearMode == 0) {
						this.sequencerClearBank(bank);
						this.redrawDevice();
					}
				} else { // in bank mode but not bottom row. i.e. a bank button is pressed
					if (this.bankCopyMode == 1) {
						this.bankCopyMode = 0;
						this.sequencerCopyBank(this.bank, (y * (this.monome.sizeX)) + x);
						this.redrawDevice();
					} else if (bankClearMode == 1) {
						this.bankClearMode = 0;
						this.sequencerClearBank((y * (this.monome.sizeX)) + x);
						if (this.bank == (y * (this.monome.sizeX)) + x) {
							this.stopNotes();
						}
						this.redrawDevice();
					} else {
						this.bank = (y * (this.monome.sizeX)) + x;
						this.stopNotes();
						this.redrawDevice();
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
						this.newNoteLength++;
					if(x == 6)
						this.newNoteLength--;
					
					// pattern select
					if (x < 4) {
						if (this.copyMode == 1) {
							this.copyMode = 0;
							this.sequencerCopyPattern(this.pattern, x);
						}
						if (this.clearMode == 1) {
							this.clearMode = 0;
							if (x == this.pattern) {
								this.stopNotes();
							}
							this.sequencerClearPattern(x);
						}
						this.pattern = x;
						this.redrawDevice();
					}
					// copy mode
					if (x == ButtonNoCopy && this.clearMode == 0 && this.mode != BANKMODE) {
						if (this.copyMode == 1) {
							this.copyMode = 0;
							this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOff, this.index);
						} else {
							this.copyMode = 1;
							this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
						}
					}
					// clear mode
					if (x == ButtonNoClear && this.copyMode == 0 && this.mode != BANKMODE) {
						if (this.clearMode == 1) {
							this.clearMode = 0;
							this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOff, this.index);
						} else {
							this.clearMode = 1;
							this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
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

					if (x == ButtonNoVelocity) {
						if (this.velocityMode == 0) {
							this.velocityMode = 1;
						} else {
							this.velocityMode = 0;
						}
						this.redrawDevice();
					}

				// record button press to the sequence
				} else {
					x_seq = (pattern * (this.monome.sizeX)) + x;
					y_seq = (depth * (this.monome.sizeY - 1)) + y;
					if (this.sequence[this.bank][x_seq][y_seq] == 0) {
						// add the note to the sequencer
						if (this.velocityMode == 1) {
							this.sequence[this.bank][x_seq][y_seq] = 1;
						} else {
							this.sequence[this.bank][x_seq][y_seq] = 2;
						}
						
						// store note length
						// if there is a previous note that would end after this new one, 
						// then make the endings of the two notes the same (i.e. don't change note length)
						if(seqNoteLengths[this.bank][x_seq][y_seq] < this.newNoteLength)
							this.seqNoteLengths[this.bank][x_seq][y_seq] = this.newNoteLength;
						reGenerateNoteLengthArrayRow(this.bank, y_seq, x_seq, x_seq+16);
						
						int note_len = this.seqNoteLengths[this.bank][x_seq][y_seq];
						// redraw this part of the row
						for(int i=0;i < note_len; i++) {
							if(x_seq + i >= this.MAX_SEQUENCE_LENGTH)
								break;
							if(sequence[this.bank][x_seq+i][y_seq] == 0) {
								if(x+i <= 15) { 
									if(this.seqNoteLengths[this.bank][x_seq+i][y_seq] == 0) 
										this.monome.vari_led(x+i, y, briOff, this.index);
									else
										this.monome.vari_led(x+i, y, briSeqNoteLen, this.index);
								}
							} else { // if there is a note here
								if(x+i <= 15) 
									this.monome.vari_led(x+i, y, briSeqNote, this.index);
							}
						}
						
//						// the note length recording done here doesn't wrap around when there is a loop!
//						for(int i=1;i<note_len;i++) {
//							if(x_seq + i > this.MAX_SEQUENCE_LENGTH)
//								break;
//							if(this.sequence[this.bank][x_seq+i][y_seq] > 0) {
//								i += this.seqNoteLengths[this.bank][x_seq+i][y_seq] - 1;
//								continue;
//							}
//							this.seqNoteLengths[this.bank][x_seq + i][y_seq] = note_len-i;
//							if(x + i <= 15)
//								this.monome.vari_led(x+i, y, briSeqNoteLen, this.index);
//						}
							
						// update the led
						this.monome.vari_led(x, y, briSeqNote, this.index);
						
						//debug
						System.out.println("---");System.out.println("---");
						for(int i=0;i<3;i++) {
							for(int j=0;j<16;j++)
								System.out.print(seqNoteLengths[this.bank][j][i]);
							System.out.println("aa");
						}
						
						
					// change velocity	
					} else if (this.sequence[this.bank][x_seq][y_seq] == 1) {
						this.sequence[bank][x_seq][y_seq] = 2;
						this.monome.vari_led(x, y, briSeqNote, this.index);
					// remove note
					} else if (this.sequence[this.bank][x_seq][y_seq] == 2) {
						this.sequence[bank][x_seq][y_seq] = 0;
						this.monome.vari_led(x, y, 0, this.index);
						int note_len = seqNoteLengths[this.bank][x_seq][y_seq];
						
						reGenerateNoteLengthArrayRow(this.bank, y_seq, x_seq, x_seq+16);
						
						//debug
						System.out.println("---");System.out.println("---");
						for(int i=0;i<3;i++) {
							for(int j=0;j<16;j++)
								System.out.print(seqNoteLengths[this.bank][j][i]);
							System.out.println("aa");
						}
						
						//debug
						System.out.println("---");System.out.println("---");
						for(int i=0;i<3;i++) {
							for(int j=0;j<16;j++)
								System.out.print(this.sequence[this.bank][j][i]);
							System.out.println("ss");
						}
						
						// redraw the remaining part of the row
						for(int i=0;i < 16; i++) {
							if(x_seq + i >= this.MAX_SEQUENCE_LENGTH || x + i > 15)
								break;
							if(sequence[this.bank][x_seq+i][y_seq] == 0) {
									if(this.seqNoteLengths[this.bank][x_seq+i][y_seq] == 0) 
										this.monome.vari_led(x+i, y, briOff, this.index);
									else
										this.monome.vari_led(x+i, y, briSeqNoteLen, this.index);
							} else { // if there is a note here
									this.monome.vari_led(x+i, y, briSeqNote, this.index);
							}
						}
						
//						for(int i=0;i < note_len; i++) {
//							if(x_seq + i >= this.MAX_SEQUENCE_LENGTH)
//								break;
//							if(sequence[this.bank][x_seq+i][y_seq] == 0) {
//								seqNoteLengths[this.bank][x_seq+i][y_seq] = 0;
//								if(x+i <= 15)
//									this.monome.vari_led(x+i, y, briOff, this.index);
//							} else {
//								i += seqNoteLengths[this.bank][x_seq+i][y_seq]-1;
//							}
//						}
						
						
					}
				}
			}
		}
	}

	/**
	 * Clear a pattern in the currently selected bank.
	 * 
	 * @param dst destination pattern to clear (0-3)
	 */
	private void sequencerClearPattern(int dst) {
		for (int x = 0; x < (this.monome.sizeX); x++) {
			for (int y = 0; y < 15; y++) {
				int x_dst = x + (dst * (this.monome.sizeX));
				sequence[bank][x_dst][y] = 0;
			}
		}
	}

	/**
	 * Copies src pattern to dst pattern.
	 * 
	 * @param src The source pattern to copy (0-3)
	 * @param dst The destination to copy the source pattern to (0-3)
	 */
	private void sequencerCopyPattern(int src, int dst) {
		for (int x = 0; x < (this.monome.sizeX); x++) {
			for (int y = 0; y < 15; y++) {
				int x_src = x + (src * (this.monome.sizeX));
				int x_dst = x + (dst * (this.monome.sizeX));
				sequence[bank][x_dst][y] = sequence[bank][x_src][y];
			}
		}
	}

	/**
	 * Copies src bank to dst bank.
	 *
	 * @param src The source bank to copy
	 * @param dst The destination to copy the source bank to
	 */
	public void sequencerCopyBank(int src, int dst) {
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				sequence[dst][x][y] = sequence[src][x][y];
			}
		}
	}

	/**
	 * Clears a bank.
	 * 
	 * @param dst The bank number to clear.
	 */
	public void sequencerClearBank(int dst) {
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				sequence[dst][x][y] = 0;
			}
		}
	}
	
	/**
	 * Flashes LEDs for each sequence value of 2
	 */
	private void flashNotes() {
		int x_seq;
		int y_seq;
		if (this.mode != BANKMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				x_seq = (this.pattern * (this.monome.sizeX)) + x;
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					y_seq = (this.depth * (this.monome.sizeY - 1)) + y;
					if (this.sequence[bank][x_seq][y_seq] == 1) {
						if (this.flashSequence[bank][x_seq][y_seq] == 0) {
							this.flashSequence[bank][x_seq][y_seq] = 1;
							this.monome.vari_led(x, y, this.briNoteOn, this.index);
						} else {
							this.flashSequence[bank][x_seq][y_seq] = 0;
							this.monome.vari_led(x, y, 0, this.index);
						}
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
		
		if (this.tickNum % 3 == 0) {
			this.flashNotes();
		}
		
		if (this.tickNum >= quantization) {
			this.tickNum = 0;
		}
		
		// send a note on for lit leds on this sequence position
		if (this.tickNum == 0) {
			if (this.sequencePosition == this.bankSize) {
				this.sequencePosition = 0;
			}
			if (this.sequencePosition >= (this.pattern * (this.monome.sizeX)) && this.sequencePosition < ((this.pattern + 1) * (this.monome.sizeX))) {
				if(this.mode == SEQUENCERMODE) {
					ArrayList<Integer> colArgs = new ArrayList<Integer>();
					int col = this.sequencePosition % (this.monome.sizeX);
					int x_seq = this.pattern * (this.monome.sizeX) + col; 
					colArgs.add(col);
					for(int i=0;i<this.monome.sizeY;i++) {
						if(sequence[bank][x_seq][i]>0) { 
							colArgs.add(briNoteOn);
						} else if(this.seqNoteLengths[bank][x_seq][i] > 0) {
							colArgs.add(this.briNoteOn);
						} else {
							colArgs.add(this.briSeqBar);
						}
					}						
					this.monome.vari_led_col(colArgs, this.index);
					this.sequencerModeRedrawCol(this.sequencePosition % (this.monome.sizeX), 255);
				}
			}
			this.playNotes(this.sequencePosition, 127);
			
			if(this.mode == KEYBOARDMODE)
				this.lightUpNotesOnKeyboard(this.sequencePosition, this.briKeybNotePlaying);
			else if(this.mode == MATRIXMODE) {
				this.lightUpNotesInMatrixMode(this.sequencePosition, this.briMatrixNoteOn);
			}
			
				
		}

		// turn off the leds at the end of the time allotted for this column
		if (this.tickNum == quantization - 1) {
			if (this.sequencePosition >= (this.pattern * (this.monome.sizeX)) && this.sequencePosition < ((this.pattern + 1) * (this.monome.sizeX))) {
				if(this.mode == SEQUENCERMODE) {
					ArrayList<Integer> colArgs = new ArrayList<Integer>();
					colArgs.add(this.sequencePosition % (this.monome.sizeX));
					for(int i=0;i<this.monome.sizeY;i++) {
						colArgs.add(0);
					}
					this.monome.vari_led_col(colArgs, this.index);
					this.sequencerModeRedrawCol(this.sequencePosition % (this.monome.sizeX), 0);
				}
			}
			
			//this.playNotes(this.sequencePosition, 0);  //now handled with scheduler
			if(this.mode == KEYBOARDMODE)
				this.lightUpNotesOnKeyboard(this.sequencePosition, 0);
			else if(this.mode == MATRIXMODE) 
				this.lightUpNotesInMatrixMode(this.sequencePosition, this.briMatrixDefault);
			this.sequencePosition++;
		}

		if (this.mode == BANKMODE && this.tickNum % quantization == 0) {
			int x = bank % this.monome.sizeX;
			int y = bank / this.monome.sizeX;
			if (this.blinkThread != null) {
				this.blinkThread.cancel();
			}
			this.blinkThread = new LEDBlink(monome, x, y, 20, this.index);
			new Thread(this.blinkThread).start();
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
			int midino = (Integer) midiNoteOffSchedule[now].get(i);
			
			try {
				
				note_out.setMessage(ShortMessage.NOTE_OFF, Integer.parseInt(midiChannel), midino, 0);
				
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
		}
		
		midiNoteOffSchedule[now].clear();
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		this.tickNum = 0;
		this.sequencePosition = 0;
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
		
		if (val == 0) {
			int x_seq = (this.pattern * (this.monome.sizeX)) + col;
			for (int y = 0; y < (this.monome.sizeY - 1); y++) {
				int y_seq = (this.depth * (this.monome.sizeY - 1)) + y;
				if (this.sequence[bank][x_seq][y_seq] > 0) {
					this.monome.vari_led(col, y, this.briSeqNote, this.index);
				} else if(this.seqNoteLengths[bank][x_seq][y_seq] > 0) {
					this.monome.vari_led(col, y, this.briSeqNoteLen, this.index);
				}
			}
			if (col == this.pattern) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), briOn, this.index);
			}
			else if (col == ButtonNoCopy && this.copyMode == 1) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), briOn, this.index);
			}
			else if (col == ButtonNoClear && this.clearMode == 1) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), briOn, this.index);
			}
			else if (col == ButtonNoBank && this.mode == BANKMODE) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), briOn, this.index);
			}
			else if (col == ButtonNoKeyboard && this.mode == KEYBOARDMODE) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), briOn, this.index);
			} 
			else if (col == ButtonNoMatrix && this.mode == MATRIXMODE) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), this.briOn, this.index);
			}
			else if(col == ButtonNoVelocity && velocityMode == 1) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), this.briOn, this.index);
			}
			else if(col == ButtonNoMute && muteMode == 1) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), this.briOn, this.index);
			}
			else if (col == (this.monome.sizeX - 1)) {
				this.monome.vari_led(col, (this.monome.sizeY - 1), 0, this.index);
			} 
			else {
					this.monome.vari_led(col, (this.monome.sizeY - 1), 0, this.index);
			}
		}
	}
	
	public void stopNotes() {
		ShortMessage note_out = new ShortMessage();
		for (int i=0; i < 16; i++) {
			if (this.heldNotes[i] == 1) {
				this.heldNotes[i] = 0;
				int note_num = this.getNoteNumber(i);
				try {
					note_out.setMessage(ShortMessage.NOTE_OFF, 0, note_num, 0);
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
			}
		}
		
		for(int i=0; i < MAX_LEN_OF_MIDI_SCHEDULER; i++)
			midiNoteOffSchedule[i].clear();
		midiSchedulerPosition = 0;
	}

	/**
	 * Send MIDI note messages based on the sequence position.  If on = 0, note off will be sent.
	 * 
	 * @param seq_pos The sequence position to play notes for
	 * @param on Whether to turn notes on or off, a value of 1 means play notes
	 */
	public void playNotes(int seq_pos, int on) {
		if (muteMode == 1) {
			return;
		}
		ShortMessage note_out = new ShortMessage();
		int note_num;
		int velocity;
		int midiChannel = Integer.parseInt(this.midiChannel) - 1;
		for (int y = 0; y < 16; y++) {
		// there used to be a hold mode
		// now only normal mode 
				if (sequence[this.bank][seq_pos][y] > 0) {
					if (on > 0) {
						velocity = (this.sequence[this.bank][seq_pos][y] * 64) - 1;
					} else {
						velocity = 0;
					}
					note_num = this.getNoteNumber(y);
					if (note_num < 0) {
						continue;
					}
					try {
						if (velocity == 0) {
							note_out.setMessage(ShortMessage.NOTE_OFF, midiChannel, note_num, velocity);
							this.heldNotes[y] = 0;
						} else {
							note_out.setMessage(ShortMessage.NOTE_ON, midiChannel, note_num, velocity);
							this.heldNotes[y] = 1;
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
					
					// schedule the note off for this one
					int newPos = midiSchedulerPosition + this.seqNoteLengths[this.bank][seq_pos][y]*this.quantization;
					while(newPos >= MAX_LEN_OF_MIDI_SCHEDULER)
						newPos -= MAX_LEN_OF_MIDI_SCHEDULER;
					midiNoteOffSchedule[newPos].add(note_num);
				}
			
		}
	}
	
	public void lightUpNotesOnKeyboard(int seq_pos, int value) {
		if (muteMode == 1) {
			return;
		}
		
		int note_midi_num;
		
		for (int y = 0; y < 16; y++) {
			if (sequence[this.bank][seq_pos][y] > 0) {
				note_midi_num = this.getNoteNumber(y);
				keyboardModeLedFromNoteNumber(keyboardModeNoteNumberFromMidiNumber(note_midi_num), value);
			}
		}
	}

	public void  lightUpNotesInMatrixMode(int seq_pos, int value) {
		if (muteMode == 1) {
			return;
		}
		
		for (int y = 0; y < 16; y++) {
			if (sequence[this.bank][seq_pos][y] > 0) {
				this.monome.vari_led(y, rowSwap[y], value, this.index);
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
	public int noteToMidiNumber(String convert_note) {		
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
	 * Get the MIDI note number for a sequence lane (row)
	 * 
	 * @param y The row / sequence lane to get the MIDI note number for
	 * @return The MIDI note number assigned to that row / sequence lane
	 */
	public int getNoteNumber(int y) {
		return noteNumbers[rowSwap[y]];
	}

	/**
	 * Set row number num to midi note value value.
	 * 
	 * @param num The row number to set (0 = Row 1)
	 * @param value The MIDI note value to set the row to
	 */
	
	public void setNoteValue(int num, int value) {
		this.noteNumbers[num] = value;
		if (num == gui.rowCB.getSelectedIndex()) {
			gui.noteTF.setText(this.numberToMidiNote(value));
		}
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
			this.monome.vari_led(ButtonNoKeyboard, this.monome.sizeY-1, briOn*(this.mode == KEYBOARDMODE ? 1 : 0), this.index);
		}
		// redraw in matrix mode
		else if(this.mode == MATRIXMODE) {
			int y;
			for (int x = 0; x < (this.monome.sizeX); x++) {
				for (y = 0; y < (this.monome.sizeY - 1); y++) {
					if(y == this.rowSwap[x])
						this.monome.vari_led(x, y, this.briMatrixDefault, this.index);
					else
						this.monome.vari_led(x, y, 0, this.index);
				}
			}
			y = this.monome.sizeY;
			for (int x = 0; x < (this.monome.sizeX); x++)
				this.monome.vari_led(x, y, 0, this.index);
			// light up matrix mode key
			this.monome.vari_led(ButtonNoMatrix, this.monome.sizeY-1, this.briOn*(this.mode == MATRIXMODE ? 1 : 0), this.index);
			this.sequencerRedrawBottomRow();
		}
		// redrawDevice if we're in bank mode
		else if (this.mode == BANKMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					int curBank = (y * this.monome.sizeX) + x;
					boolean bankData = false;
					
					search:
					for (int seqX = 0; seqX < 64; seqX++) {
						for (int seqY = 0; seqY < 16; seqY++) {
							if (this.sequence[curBank][seqX][seqY] > 0) {
								bankData = true;
								break search;
							} 
						}
					}
					if (bankData) {
						this.monome.vari_led(x, y, this.briOn, this.index);
					} else if (curBank != this.bank) {
						this.monome.vari_led(x, y, this.briOff, this.index);
					}
				}
			}
			// redrawDevice the bottom row
			this.sequencerRedrawBottomRow();
		// redrawDevice if we're in sequence mode
		} else if(this.mode == this.SEQUENCERMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				x_seq = (this.pattern * (this.monome.sizeX)) + x;
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					y_seq = (this.depth * (this.monome.sizeY - 1)) + y;
					int value = 0;
					if (this.sequence[bank][x_seq][y_seq] > 0) {
						value = this.briSeqNote;
					} else if(this.seqNoteLengths[bank][x_seq][y_seq] > 0)
						value = this.briSeqNoteLen;
					this.monome.vari_led(x, y, value, this.index);
				}
			}
			// redrawDevice the bottom row
			this.sequencerRedrawBottomRow();
		}
		
	}

	/**
	 * Redraws the bottom row of the sequencer page on the monome.
	 */
	public void sequencerRedrawBottomRow() {
			
		// bottom row redraw in keyboard mode means just update the keyboard mode button
//		if(this.mode == KEYBOARDMODE) {
//			this.monome.vari_led(14, this.monome.sizeY-1, (this.mode == KEYBOARDMODE ? 1 : 0)*briOn, this.index);
//		}
//		else if(this.mode == MATRIXMODE) {
//			this.monome.vari_led(13, this.monome.sizeY-1, (this.mode == MATRIXMODE ? 1 : 0)*briOn, this.index);
//		}
		// redrawDevice this way if we're in bank mode
		if (this.mode == BANKMODE) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				if (x < 4) {
					if (this.depth == x) {
						this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
					} else {
						this.monome.vari_led(x, (this.monome.sizeY - 1) , 0, this.index);
					}
				}
				else if (x == ButtonNoCopy) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), this.bankCopyMode*this.briOn, this.index);
				}
				else if (x == ButtonNoClear) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), this.bankClearMode*this.briOn, this.index);
				}
				else if (x == ButtonNoBank) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), (this.mode == BANKMODE ? 1 : 0)*this.briOn, this.index);
				}
				else if (x == ButtonNoMute) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), this.muteMode*this.briOn, this.index);
				}
				else if (x == ButtonNoVelocity) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), this.velocityMode*this.briOn, this.index);
				}
				else {
					this.monome.vari_led(x, (this.monome.sizeY - 1), 0, this.index);
				}
			}
			// redrawDevice this way if we're not in bank mode (usually sequencer mode)
		} else {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				if (x < 4) {
					if (this.pattern == x) {
						this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
					} else {
						this.monome.vari_led(x, (this.monome.sizeY - 1), 0, this.index);
					}
				}
				else if (x == ButtonNoCopy) {
					if (copyMode == 1) {
						this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
					} else {
						this.monome.vari_led(x, (this.monome.sizeY - 1), 0, this.index);
					}
				}
				else if (x == ButtonNoClear) {
					if (clearMode == 1) {
						this.monome.vari_led(x, (this.monome.sizeY - 1), this.briOn, this.index);
					} else {
						this.monome.vari_led(x, (this.monome.sizeY - 1), 0, this.index);
					}
				}
				else if (x == ButtonNoMute) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), this.muteMode*this.briOn, this.index);
				}
				else if (x == ButtonNoVelocity) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), this.velocityMode*this.briOn, this.index);
				}
				else if(x == ButtonNoMatrix) {
					this.monome.vari_led(x, (this.monome.sizeY - 1), (this.mode == MATRIXMODE ? 1 : 0)*this.briOn, this.index);
				}
				else {
					this.monome.vari_led(x, (this.monome.sizeY - 1), 0, this.index);
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
		int[][] p1 = {
				{2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0}, // 1
				{0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,1,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,2,0,1,0}, // 2
				{0,0,2,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,2,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 3
				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 4
				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,1,0}, // 5
				{0,0,0,0,0,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0}, // 6
				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,1, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,1}, // 7
				{0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 8
				{2,1,0,0,2,0,2,0, 2,0,2,0,2,1,0,0, 1,2,0,0,0,1,2,1, 2,0,1,0,0,2,0,1, 2,1,0,0,2,0,2,0, 2,0,2,0,2,1,0,0, 1,2,0,0,0,1,2,1, 2,0,1,0,0,2,0,1}, // 9
				{0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,1, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,2,1}, // 10
				{0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,2,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,0,0,0,0,0,0}, // 11
				{2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 12
				{0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0, 0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,0, 2,0,0,0,0,0,0,0, 0,0,0,0,0,0,0,0}, // 13
				{0,0,2,0,0,0,0,0, 0,0,1,0,0,0,0,0, 1,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, 0,0,2,0,0,0,0,0, 0,0,1,0,0,0,0,0, 1,0,0,0,0,0,0,0, 0,0,0,0,1,0,0,0}  // 14
		};
		// randomly turn things on and off
		for (int x = 0; x < this.bankSize; x++) {
			for (int y = 0; y < 14; y++) {
				sequence[bank][x][y] = p1[y][x];
				if (generator.nextInt(20) == 1) {
					sequence[bank][x][y] = 1;
				}
				if (generator.nextInt(10) == 1) {
					sequence[bank][x][y] = 2;
				}
				if (generator.nextInt(6) == 1) {
					sequence[bank][x][y] = 0;
				}
			}
		}

	}

	/**
	 * Alters the current sequencer patterns. 
	 */
	private void alterSequencerPattern() {
		// randomly turn things on or off
		for (int x = 0; x < this.bankSize; x++) {
			for (int y = 0; y < 15; y++) {
				if (sequence[bank][x][y] > 0) {
					if (generator.nextInt(30) == 1) {
						sequence[bank][x][y] = generator.nextInt(3);
					}
				}
				if (sequence[bank][x][y] == 0) {
					if (generator.nextInt(150) == 1) {
						sequence[bank][x][y] = generator.nextInt(3);
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
		xml.append("      <banksize>" + this.bankSize + "</banksize>\n");
		xml.append("      <midichannel>" + this.midiChannel + "</midichannel>\n");
		xml.append("      <sequencerQuantization>" + this.quantization + "</sequencerQuantization>\n");
		xml.append("      <muteMode>" + this.muteMode + "</muteMode>\n");
		xml.append("      <velocityMode>" + this.velocityMode + "</velocityMode>\n");
		for (int i=0; i < 16; i++) {
			xml.append("      <row>" + String.valueOf(this.noteNumbers[i]) + "</row>\n");
		}
		for (int i=0; i < 240; i++) {
			xml.append("      <sequence>");
			for (int j=0; j < 64; j++) {
				for (int k=0; k < 16; k++) {
					xml.append(this.sequence[i][j][k]);	
				}
			}
			xml.append("</sequence>\n");
		}
		return xml.toString();
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#actionPerformed(java.awt.event.ActionEvent)
	 */
	/*
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if (e.getActionCommand().equals("Set MIDI Output")) {
			String[] midiOutOptions = this.monome.getMidiOutOptions();
			String deviceName = (String)JOptionPane.showInputDialog(
					Main.getDesktopPane(),
					"Choose a MIDI Output to use",
					"Set MIDI Output",
					JOptionPane.PLAIN_MESSAGE,
					null,
					midiOutOptions,
					"");

			if (deviceName == null) {
				return;
			}

			this.addMidiOutDevice(deviceName);
		}
		if (e.getActionCommand().equals("Update Preferences")) {
			this.noteNumbers[0] = this.noteToMidiNumber(this.row1tf.getText());
			this.noteNumbers[1] = this.noteToMidiNumber(this.row2tf.getText());
			this.noteNumbers[2] = this.noteToMidiNumber(this.row3tf.getText());
			this.noteNumbers[3] = this.noteToMidiNumber(this.row4tf.getText());
			this.noteNumbers[4] = this.noteToMidiNumber(this.row5tf.getText());
			this.noteNumbers[5] = this.noteToMidiNumber(this.row6tf.getText());
			this.noteNumbers[6] = this.noteToMidiNumber(this.row7tf.getText());
			this.noteNumbers[7] = this.noteToMidiNumber(this.row8tf.getText());
			this.noteNumbers[8] = this.noteToMidiNumber(this.row9tf.getText());
			this.noteNumbers[9] = this.noteToMidiNumber(this.row10tf.getText());
			this.noteNumbers[10] = this.noteToMidiNumber(this.row11tf.getText());
			this.noteNumbers[11] = this.noteToMidiNumber(this.row12tf.getText());
			this.noteNumbers[12] = this.noteToMidiNumber(this.row13tf.getText());
			this.noteNumbers[13] = this.noteToMidiNumber(this.row14tf.getText());
			this.noteNumbers[14] = this.noteToMidiNumber(this.row15tf.getText());
			this.midiChannel  = this.channelTF.getText();
			try {
				this.setBankSize(Integer.parseInt(this.bankSizeTF.getText()));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
	}
	*/
	
	public void setBankSize(int banksize) {
		if (banksize > 64) {
			banksize = 64;
		} else if (banksize < 1) {
			banksize = 1;
		}
		this.sequencePosition = 0;
		this.bankSize = banksize;
		this.gui.bankSizeTF.setText(String.valueOf(banksize));
	}

	/**
	 * Loads a sequence from a configuration file.  Called from GUI on open configuration action.
	 * 
	 * @param l
	 * @param sequence2
	 */
	public void setSequence(int l, String sequence2) {
		int row = 0;
		int pos = 0;
		for (int i=0; i < sequence2.length(); i++) {

			if (row == 16) {
				row = 0;
				pos++;
			}

			if (sequence2.charAt(i) == '0') {
				this.sequence[l][pos][row] = 0;
			} else if (sequence2.charAt(i) == '1') {
				this.sequence[l][pos][row] = 1;
			} else if (sequence2.charAt(i) == '2') {
				this.sequence[l][pos][row] = 2;
			}
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
		this.midiChannel = midiChannel2;
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
		this.setBankSize(Integer.parseInt(this.monome.readConfigValue(pageElement, "banksize")));
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
		for (int l=0; l < seqNL.getLength(); l++) {		
			Element el = (Element) seqNL.item(l);		
			NodeList nl = el.getChildNodes();		
			String sequence = ((Node) nl.item(0)).getNodeValue();		
			this.setSequence(l, sequence);		
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
	public void playNote(int note_num, int velocity, int channel, int onOff) {
		ShortMessage note_out = new ShortMessage();
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
	
	public int keyboardModeNoteNumberToMidiNumber(int nn) {
		int offset = 0;
		int note;
		
		int transp = this.transpose * 12;
		
		if(nn>=0) { 
			while (nn >= scaleLength) {
				nn -= scaleLength;
				offset += this.scaleTotalInSemitones;
			}
		} else {
			while (nn<0) {
				nn += scaleLength;
				offset -= this.scaleTotalInSemitones;
			}
		}
				
		note = this.rootNoteMidiNumber + this.scaleNoteDiffsToRoot[nn] + offset;
		note += (transp + this.accidental);
		
		return note;
	}

	public int midiNumberToX(int num) {
		
		return 0;
	}
	
	public int midiNumberToY(int num) {
		
		return 0;
	}

	public int keyboardModeNoteNumberFromMidiNumber(int num) {
		int offset = 0;
		//bring the number between rootNoteMidiNumber and rootNoteMidiNumber+scaletotalinsemitones
		while(num >= this.rootNoteMidiNumber + this.scaleTotalInSemitones) {
			num -= this.scaleTotalInSemitones;
			offset += scaleLength;
		}
		while(num < this.rootNoteMidiNumber) {
			num += this.scaleTotalInSemitones;
			offset -= this.scaleLength;
		}
		//calculate difference to the root
		num -= this.rootNoteMidiNumber;
		for(int i=0; i<this.scaleLength; i++) {
			if(this.scaleNoteDiffsToRoot[i] == num) {
				return i + offset;
			}
		}
		return -100;
	}
	
	public int keyboardModeXYToNoteNumber(int x, int y) {
		return (x - this.rootNoteX) + rowOffset*(this.rootNoteY-y);
	}
	
	// turns on/off all led's corresponding to the note (number not midi number)
	public void keyboardModeLedFromNoteNumber(int num, int value) {
		
		int x=0;
		int y=monome.sizeY-1;
		int numBottomLeft = keyboardModeXYToNoteNumber(0, this.monome.sizeY-1);
		int dumNum=0;
		
		if(num < numBottomLeft) // note too low to exist on grid
			return;
		
		//find x,y for note with smallest x
		if(rowOffset == 0) { // no need to make this work if offset is zero
			// not tested
			y = 0;
			x = this.rootNoteX + num;
			
			//don't change the mode buttons
			if(y == this.monome.sizeY && x > 11)
				return;
			
			if(value>0)
				this.monome.vari_led(x, y, value, this.index);
			else
				keyboardModeRedrawXYToDefault(x,y);
		}
		else {
			while(numBottomLeft <= num) {
				numBottomLeft += this.rowOffset;
				y-=1;
			}
			y++; 
			x = num - (numBottomLeft - this.rowOffset);
			dumNum = 0;
			// light up or down all corresponding notes
			while(y<this.monome.sizeY && x < this.monome.sizeX) {
				//don't change the mode buttons
				if(y == this.monome.sizeY && x > 11)
					continue;
				if(value>0)
					this.monome.vari_led(x, y, value, this.index);
				else
					keyboardModeRedrawXYToDefault(x,y);	
				y++;
				x += this.rowOffset;
			}	
		}
	}	


	public void keyboardModeRedrawXYToDefault(int x, int y) {
		if(keyboardModeXYToNoteNumber(x, y) % this.scaleLength == 0) {
			this.monome.vari_led(x, y, briKeybRoot, this.index);
		}
		else {
			this.monome.vari_led(x, y, briOff, this.index);
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
		
		if(xend == this.MAX_SEQUENCE_LENGTH) 
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
} // end of class


