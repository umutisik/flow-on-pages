import org.monome.pages.api.Command
import org.monome.pages.api.GroovyAPI
import org.monome.pages.pages.ArcPage
import org.monome.pages.pages.arc.GroovyPage
import org.monome.pages.configuration.ArcConfiguration
import org.monome.pages.configuration.ArcConfigurationFactory

class MIDILoopPage extends GroovyAPI {

    def buffers = []
    def oldBuffers = []
    def programNums = []
    int activeBuffer = -1
    int tickNum = -1
    def lengths = []
    int baseMidiChannel = 12

    String prefix = "/m0000226"

    void init() {
        log("MIDILoopPage starting up")
        for (int i = 0; i < sizeX(); i++) {
            buffers[i] = new MIDIBuffer()
        }
        for (int i = 0; i < sizeX() / 2; i++) {
            programNums[i] = 0
            lengths[i] = 192
        }
        redraw()
    }

    void stop() {
        log("MIDILoopPage shutting down")
    }

    void press(int x, int y, int val) {
        if (val == 1) {
            if (y < sizeY() - 4) {
                sendCommandToArc(new Command("stopCC", x))
                int oldX = x - (x % 2)
                if (programNums[(int) (x / 2)] >= 4) {
                    oldX++
                }
                int oldY = (programNums[(int) (x / 2)] % 4)
                led(oldX, oldY, 0)
                int progNum = y + ((x % 2) * 4)
                log("program change on " + ((int) (x / 2) + 2) + " / " + progNum)
                programChange(progNum, progNum, (int) (x / 2) + 2)
                programNums[(int) (x / 2)] = progNum
                led(x, y, 1)
            }
            if (y > sizeY() - 5 && y < sizeY() - 1) {
                if (oldBuffers[x] != null && oldBuffers[x][2 - (y - sizeY() + 4)] != null) {
                    int channel = 2 + (activeBuffer / 2)
                    for (int i = 0; i < buffers[activeBuffer].playingNotes.size(); i++) {
                        MIDINote note = buffers[activeBuffer].playingNotes[i]
                        noteOut(note.note, note.velocity, channel, 0)
                    }
                    buffers[x].playingNotes = []
                    MIDIBuffer tmpBuffer = buffers[x]
                    buffers[x] = oldBuffers[x][2 - (y - sizeY() + 4)]
                    oldBuffers[x][2 - (y - sizeY() + 4)] = tmpBuffer
                    if (x == activeBuffer) {
                        buffers[x].recording = 1
                    } else {
                        buffers[x].recording = 0
                    }
                }
            }
            if (y == sizeY() - 1) {
                activateBuffer(x)
                redraw()
            }
        }
    }

    void activateBuffer(int buf) {
        if (activeBuffer == buf) {
            int channel = (activeBuffer / 2) + 2
            for (int i = 0; i < buffers[activeBuffer].playingNotes.size(); i++) {
                MIDINote note = buffers[activeBuffer].playingNotes[i]
                noteOut(note.note, note.velocity, channel, 0)
            }
            buffers[activeBuffer].playingNotes = []
            buffers[activeBuffer].recording = 0
            if (oldBuffers[activeBuffer] == null) {
                oldBuffers[activeBuffer] = []
            }
            oldBuffers[activeBuffer][2] = oldBuffers[activeBuffer][1]
            oldBuffers[activeBuffer][1] = oldBuffers[activeBuffer][0]
            oldBuffers[activeBuffer][0] = buffers[activeBuffer]
            buffers[activeBuffer] = new MIDIBuffer()
            buffers[activeBuffer].length = lengths[(int) (activeBuffer / 2)]
        }
        buffers[activeBuffer].recording = 0
        buffers[buf].recording = 1
        activeBuffer = buf
        sendCommandToArc(new Command("activeLooper", buf))
    }

    void redraw() {
        for (int y = 0; y < 4; y++) {
            row(y, 0, 0)
        }
        for (int chan = 0; chan < sizeX() / 2; chan++) {
            int x = chan * 2
            int y = programNums[chan]
            if (y >= 4) {
                x++
                y -= 4
            }
            led(x, y, 1)
        }
        // old buffers
        for (int x = 0; x < sizeX(); x++) {
            for (int i = 0; i < 3; i++) {
                if (oldBuffers[x] != null && oldBuffers[x][i] != null && oldBuffers[x][i].hasNotes) {
                    led(x, sizeY() - 2 - i, 1)
                } else {
                    led(x, sizeY() - 2 - i, 0)
                }
            }
        }
        // bottom row
        for (int x = 0; x < sizeX(); x++) {
            if (buffers[x].recording) {
                if (tickNum % 24 < 12) {
                    led(x, sizeY() - 1, 1)
                } else {
                    led(x, sizeY() - 1, 0)
                }
            } else {
                if (buffers[x].hasNotes) {
                    led(x, sizeY() - 1, 1)
                } else {
                    led(x, sizeY() - 1, 0)
                }
            }
        }
    }

    void note(int num, int velo, int chan, int on) {
        if (chan != 12) return
        if (velo < 40) velo = 40
        if (activeBuffer >= 0) {
            int channel = (activeBuffer / 2) + baseMidiChannel
            buffers[activeBuffer].setNote(tickNum % buffers[activeBuffer].length, new MIDINote(num, velo, on))
            noteOut(num, velo, channel, on)
        } else {
            noteOut(num, velo, baseMidiChannel, on)
        }
    }

    void cc(int num, int val, int chan) {
    }

    void clock() {
        tickNum++
        // max length
        if (tickNum == 96*8) {
            tickNum = 0
        }
        for (int x = 0; x < sizeX(); x++) {
            if (buffers[x] == null) continue
            if (buffers[x].recording) {
                if (tickNum % 24 < 12) {
                    led(x, sizeY() - 1, 1)
                } else {
                    led(x, sizeY() - 1, 0)
                }
            }
            def notes = buffers[x].notes[tickNum % buffers[x].length]
            if (notes != null) {
                for (int i = 0; i < notes.size(); i++) {
                    int channel = (x / 2) + baseMidiChannel
                    noteOut(notes[i].note, notes[i].velocity, channel, notes[i].state)
                    if (notes[i].state == 1) {
                        buffers[x].playingNotes.push(notes[i])
                    } else {
                        for (int j = 0; j < buffers[x].playingNotes.size(); j++) {
                            if (buffers[x].playingNotes[j].note == notes[i].note) {
                                buffers[x].playingNotes -= buffers[x].playingNotes[j]
                            }
                        }
                    }
                }
            }
        }
    }

    void clockReset() {
        tickNum = -1
    }
    
    void sendCommand(Command cmd) {
        if (cmd.getCmd().equalsIgnoreCase("length")) {
            buffers[activeBuffer].length = (Integer) cmd.getParam()
            lengths[(int) (activeBuffer / 2)] = (Integer) cmd.getParam()
            if (tickNum > buffers[activeBuffer].length) tickNum = tickNum % buffers[activeBuffer].length
            redraw()
        }
    }

    void sendCommandToArc(Command command) {
        ArcConfiguration arc = getMyArc()
        if (arc == null) return
        ArcPage page = arc.pages.get(arc.curPage)
        if (page instanceof GroovyPage) {
            ((GroovyPage) page).theApp.sendCommand(command)
        }
    }

    ArcConfiguration getMyArc() {
        ArcConfiguration arc = ArcConfigurationFactory.getArcConfiguration(prefix)
        return arc
    }

    class MIDIBuffer {
        public int length = 2*96
        public int recording = 0
        public int hasNotes = 0
        public int instrument = 0
        public notes = []
        public playingNotes = []
        
        void setNote(int position, MIDINote note) {
            if (notes[position] == null) {
                notes[position] = []
            }
            notes[position].push(note)
            hasNotes = 1
        }

        void clear() {
            notes = []
            hasNotes = 0
        }
    }
    
    class MIDINote {
        public int note;
        public int velocity;
        public int state;
        public MIDINote(int note, int velocity, int state) {
            this.note = note
            this.velocity = velocity
            this.state = state
        }
    }
}
