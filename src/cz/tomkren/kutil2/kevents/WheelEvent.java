package cz.tomkren.kutil2.kevents;

import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.kobjects.frame.Frame;

public class WheelEvent implements KEvent {

    private Int2D pos;
    private Frame frame;

    public WheelEvent(Int2D pos, Frame frame) {
        this.pos   = pos;
        this.frame = frame;
    }

    public Int2D getPos() {return pos;}

    public Frame getFrame() {return frame;}

    @Override
    public String toString() {return "[Wheeled on position : "+ pos +" ]";}

}