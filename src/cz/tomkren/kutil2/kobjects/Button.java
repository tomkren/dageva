package cz.tomkren.kutil2.kobjects;



import cz.tomkren.kutil2.core.KAtts;
import cz.tomkren.kutil2.core.KObject;
import cz.tomkren.kutil2.core.Kutil;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.items.KItem;
import cz.tomkren.kutil2.kobjects.frame.Frame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;


/**
 * Objekt virtuálního světa fungující jako součást GUI - tlačítko.
 * Po kliknutí na tlačítko se provede specifikovaná akce.
 * @author Tomáš Křen
 */
public class Button extends KObject {

    private KItem<String> title;
    private KItem<String> cmd;

    private Int2D size;


    private static final Font  idFont     = new Font( Font.SANS_SERIF , Font.PLAIN , 10 );
    private static final Font  buttonFont = new Font( Font.MONOSPACED , Font.PLAIN , 12 );

    private static final Color bgColor      = Color.white;
    private static final Color textColor    = Color.blue;
    private static final Color textSelColor = new Color(85,26,139);



    /**
     * Vytvoří Button podle KAtts.
     * Přidává položky title - nápis na tlačítku
     * a cmd - příkaz, který se provede.
     */
    public Button(KAtts kAtts, Kutil kutil){
        super(kAtts, kutil);
        setType("button");

        title = items().addString( kAtts, "title", "" );
        cmd   = items().addString( kAtts, "cmd"  , "" );

        size  = new Int2D( title.get().length()*7 + 16 , 20 );

        setMovable(false);
        setIsGuiStuff(true);
    }

    @Override
    public void drawOutside(Graphics2D g , Int2D center , int frameDepth, double zoom, Int2D zoomCenter) {

        Int2D drawPos = center.plus( pos() );

        g.setColor( bgColor   );
        g.fillRect( drawPos.getX(), drawPos.getY() , size.getX() , size.getY() );

        if( rucksack().showInfo() ){
            g.setFont(idFont);
            g.drawString( id() , drawPos.getX() , drawPos.getY()-3 );
        }

        if( isHighlighted() ){
            g.setColor( Color.red );
            g.drawRect( drawPos.getX(), drawPos.getY() , size.getX() , size.getY() );

            g.setColor( textSelColor );
        }
        else{
            g.setColor( textColor );
        }

        g.setFont(buttonFont);
        g.drawString( title.get() , drawPos.getX()+8 , drawPos.getY()+12 );
        g.drawLine( drawPos.getX()+8 , drawPos.getY()+15 , drawPos.getX()+size.getX()-10 , drawPos.getY()+15 );
    }

    @Override
    public boolean isHit( Int2D clickPos , double zoom, Int2D center){
        return Int2D.rectangleHit(clickPos, pos(), size.getX(), size.getY()) ;
    }

    @Override
    public void click(Int2D clickPos) {
        super.click(clickPos);
        rucksack().cmd(cmd.get());
    }

    @Override
    public void drag(Int2D clickPos, Int2D delta , Frame f) {
        super.drag(clickPos, delta , f);
        rucksack().cmd(cmd.get());
    }





}
