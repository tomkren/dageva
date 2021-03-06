package cz.tomkren.kutil2.kevents;


import cz.tomkren.kutil2.items.Int2D;

/**
 * ClickEvent představuje soubor informací podstatných při reakci na pustění myši nad KObjektem,
 * tyto informace sestávají pouze z pozice.
 * @author Tomáš Křen
 */
public class ReleaseEvent implements KEvent {

    private Int2D pos;

    public ReleaseEvent(Int2D pos){this.pos   = pos;}

    public Int2D getPos(){return pos;}

    @Override
    public String toString() {return "[Released on position : "+ pos +" ]";}

}
