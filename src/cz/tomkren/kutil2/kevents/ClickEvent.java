package cz.tomkren.kutil2.kevents;


import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.kobjects.frame.Frame;

/**
 * ClickEvent představuje soubor informací podstatných při reakci na kliknutí na KObjekt,
 * tyto informace jsou pozice kliknutí a Frame ve kterém bylo kliknuto.
 * @author Tomáš Křen
 */
public class ClickEvent implements KEvent {

    private Int2D pos;
    private Frame frame;

    public ClickEvent(Int2D pos, Frame frame){
        this.pos   = pos;
        this.frame = frame;
    }

    public Int2D getPos() {return pos;}

    public Frame getFrame() {return frame;}

    @Override
    public String toString() {return "[Clicked on position : "+ pos +" ]";}

}
