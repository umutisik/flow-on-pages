Flow 
=======

Flow is a music composition tool for the monome built on the [pages application](https://github.com/dinchak/pages-2) by Tom Dinchak.

Currently it supports only the Monome 256 with variable brightness.


Flow's different modes
=====================

The default mode is the sequence editor mode. Other modes can be selected from the right side of the bottom row. 
Please see the [Flow_layout.pdf](Flow_layout.pdf) file for the layout of the buttons in each mode. 

* Sequence editor:
The x axis are the time steps and the y axis are the notes. You can add and remove notes by using the corresponding buttons. If the velocity button is pressed, then pressing a note will bring up the length/velocity selection on the right side of the monome. 

The bottom left button of the monome will bring up an editor where you can set the starting and stopping points of your sequence as well as the octave of the displayed notes. 

* Keyboard mode:
Currently playing notes in all sequences are lightly lit up on the keyboard so you can also see what is happening. 
You can enable the recording of the notes into the sequence by pressing the top left button of the monome. 

* Clips mode:
Each column corresponds to a different MIDI channel. You can push and hold a button and then push another button without releasing it to copy the sequence of the first clip onto the 2nd clip. 

* Matrix mode:
Allows you to transpose notes in the sequence.


Installation and set-up
=======================

Please see http://monome.org/docs/setup/ on how to set-up the monome. If you have Java installed, then downloading the [dist/pages.zip](dist/pages.zip) and running its contents should hopefully work. 


