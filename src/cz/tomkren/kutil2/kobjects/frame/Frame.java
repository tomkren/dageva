package cz.tomkren.kutil2.kobjects.frame;


import cz.tomkren.helpers.Log;
import cz.tomkren.kutil2.core.KAtts;
import cz.tomkren.kutil2.core.KObject;
import cz.tomkren.kutil2.core.Kutil;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.kutil2.items.KItem;
import cz.tomkren.kutil2.kevents.*;
import cz.tomkren.kutil2.kobjects.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Set;

/* TODO issues :
todo : Když je daný undo na minimal_problem.xml, tak se změní main object na který se dívá frame (Matrix mode vedle), což způsobí že frame se teď kouká na něco jinýho (na ten starej KObjekt)
*/


public class Frame extends KObject {

    private static final int maxFrameDepth = 100;



    private KItem<Int2D>   size;        // velikost okna
    private KItem<Int2D>   cam;         // pozice kamery v rámci vnitřku zobrazovaného objektu
    private KItem<String>  targetID;    // id objektu, jehož vnitřek zobrazuje tento frame
    private KItem<String>  title;       // titulek okna
    private KItem<Boolean> showXML;     // určuje zda okno zobrazuje XML reprezentaci targetu, nebo zda standardně ukazuje GUI
    private KItem<Boolean> movableCam;  // určuje, zda je možno posouvat kameru

    private KItem<Double>  zoom;

    private KObject  target; // objekt, jehož vnitřek zobrazujeme
    private Int2D    center; // střed okna

    private boolean  jFrameZatimNezalozen; // indikuje že pro tento frame nebyl zatím založen JFrame
    private boolean  isHighlightedFrame ;  // je frame označen jako aktuální frame

    private MyJFrame myJFrame;  // JFrame tohoto framu, používá se pokud je frame zároveň oknem programu
                                // tzn. nejedn se o vnořený frame
    private int steps = 0;

    private Color decorColor      = new Color(166,192,229); // původní modrá: 166,192,229 , ubuntu hnědá : 201,131,60
    private Color backgroundColor = Color.white;
    private boolean showZeroPoint = true;

    private static final Font  decorFont  = new Font( Font.SANS_SERIF , Font.BOLD, 11);
    private static final Font  xmlFont    = new Font( Font.SANS_SERIF , Font.PLAIN , 10 );
    private static final int   posunFont  = 4;
    private static final int   decorY     = 20;
    private static final Color highlightColor = Color.red;
    private static final Color highlightFrameColor = Color.green;
    private static final Int2D decorDelta = new Int2D( 0, decorY );
    private static final Font  idFont = new Font( Font.SANS_SERIF , Font.PLAIN , 10 );

    private static final Color zeroPointColor = Color.black;

    private Int2D camBefore; // pro toggleShowXML ...

    private void zoom(MouseWheelEvent e) {
        //Log.it("ZOOM in "+id()+" : "+e);


        double factor = 1.2;

        double signedFactor = e.getWheelRotation() < 0 ? factor : 1.0/factor;
        zoom.set( zoom.get()*signedFactor );

        //zoomCenter = wheelPos(e);

    }

    private Int2D zoomCenter = null;



    public Frame(KAtts kAtts, Kutil kutil) {
        super(kAtts, kutil);

        setType("frame");

        size       = items().addInt2D  (kAtts , "size"       , new Int2D(600, 600) );
        cam        = items().addInt2D  (kAtts , "cam"        , Int2D.zero()        );
        targetID   = items().addString (kAtts , "target"     , null                );
        title      = items().addString (kAtts , "title"      , targetID.get()      );
        showXML    = items().addBoolean(kAtts , "showXML"    , false               );
        movableCam = items().addBoolean(kAtts , "movableCam" , true                );
        zoom       = items().addDouble (kAtts , "zoom"       , 1.0                 );

        computeCenter();

        jFrameZatimNezalozen = true  ;
        isHighlightedFrame   = false ;
    }

    @Override
    public void init() {
        super.init();
        if (targetID.get() == null) {
            target = this;
            if (title.get()==null) {
                title.set(id());
            }
        } else {
            target = idDB().get(targetID.get());
        }

        backgroundColor = showXML.get() ? Color.BLACK : target.bgcolor();
    }


    @Override
    public void step(){
        super.step();
        steps++;

        if( ! showXML.get() ){
            backgroundColor = target.bgcolor();
        }

        if (!(parent() instanceof Time)) return;

        if (jFrameZatimNezalozen) {
            myJFrame = new MyJFrame(this);
            jFrameZatimNezalozen = false;

            rucksack().setSelected(this);
            rucksack().setSelectedFrame(this);
        }

        myJFrame.myJPanel.paintScreen();
    }


    /**
     * Metoda sloužící k přenastavení zobrazovaného objektu.
     * @param newTargetID id nového targetu
     */
    public void resetTarget( String newTargetID ){
        target = idDB().get(newTargetID);

        if (title.get().equals(targetID.get())) {
            title.set(newTargetID);
        }

        targetID.set(newTargetID);


        if (!jFrameZatimNezalozen) {
            myJFrame.setTitle(title.get());
        }

        if (!showXML.get()) {
            backgroundColor = target.bgcolor();
        }
    }

    @Override
    public void resolveCopying_2(Set<String> ids) {
        String tid = targetID.get();
        if (ids.contains(tid)) {
            resetTarget(tid);
            Log.it("TARGET refrešnut!");
        }

        super.resolveCopying_2(ids);
    }

    private void computeCenter(){
        center = cam.get().negate();
    }

    public void resetSize(int x, int y) {
        size.get().setX(x);
        size.get().setY(y);
        computeCenter();
    }


    public void setHighlightedFrame(boolean isHighlightedFrame) {this.isHighlightedFrame = isHighlightedFrame;}

    public void setShowZeroPoint( boolean b ){showZeroPoint = b;}

    public String  getTitle() {return title.get();}
    public Int2D   getSize()  {return size .get();}
    public KObject getTarget(){return target;}

    public JPanel getJPanel(){return myJFrame.myJPanel;}


    /** Posouvá pozici kamery o vektor delta. */
    public void moveCam(Int2D delta) {
        if (movableCam.get()) {
            cam.set(cam.get().minus(delta));

            computeCenter();
        }
    }

    /**
     * Nastaví pozici kamery.
     * @param pos nová pozice kamery
     */
    public void setCam( Int2D pos ){
        if( movableCam.get() ){
            cam.set( pos );
            computeCenter();
        }
    }



    @Override
    public void drawOutside(Graphics2D g, Int2D c , int frameDepth, double zoom, Int2D zoomCenter) {

        Shape initClip = g.getClip();
        Int2D drawPos = c.plus( pos() );

        drawBackground(g, drawPos);
        setFrameClip( g , drawPos );
        drawFrame( g , drawPos.plus( center ).plus( decorDelta ) , drawPos.plus( decorDelta ) , frameDepth+1  );
        g.setClip(initClip);
        drawDecorFrame( g, drawPos );

        if (rucksack().showInfo()) {
            g.setColor(decorColor);
            g.setFont( idFont );
            g.drawString( id() , drawPos.getX() , drawPos.getY()-3 );
        }

        if (isHighlighted()) {
            g.setColor( highlightColor );
            g.drawRect( drawPos.getX(), drawPos.getY() , size.get().getX() , size.get().getY()+decorY );
        }

        if (isHighlightedFrame) {
            g.setColor(highlightFrameColor);
            g.drawRect(drawPos.getX() - 1, drawPos.getY() - 1, size.get().getX() + 2, size.get().getY() + decorY + 2);
        }
    }

    private void setFrameClip( Graphics2D g, Int2D drawPos ){
        Rectangle clip = g.getClipBounds();

        int clipX1 = drawPos.getX();
        int clipY1 = drawPos.getY();
        int clipX2 = drawPos.getX() + size.get().getX();
        int clipY2 = drawPos.getY() + size.get().getY();

        if( clipX1 < clip.getX() ) clipX1 = (int) clip.getX();
        if( clipY1 < clip.getY() ) clipY1 = (int) clip.getY();
        if( clipX2 < clip.getX() ) clipX2 = (int) clip.getX();
        if( clipY2 < clip.getY() ) clipY2 = (int) clip.getY();
        if( clipX1 > clip.getX() + clip.getWidth()  )
            clipX1 = (int) (clip.getX() + clip.getWidth()  );
        if( clipY1 > clip.getY() + clip.getHeight()  -decorY )
            clipY1 = (int) (clip.getY() + clip.getHeight()  -decorY );
        if( clipX2 > clip.getX() + clip.getWidth()  )
            clipX2 = (int) (clip.getX() + clip.getWidth()  );
        if( clipY2 > clip.getY() + clip.getHeight() -decorY )
            clipY2 = (int) (clip.getY() + clip.getHeight() -decorY );

        g.setClip(  clipX1 , clipY1 , clipX2-clipX1 , clipY2-clipY1+decorY );
    }


    private void drawDecorFrame(Graphics2D g, Int2D drawPos ){
        g.setColor( decorColor );
        g.drawRect( drawPos.getX(), drawPos.getY()+decorY , size.get().getX()   , size.get().getY() );
        g.fillRect( drawPos.getX(), drawPos.getY()        , size.get().getX()+1 , decorY +1   );

        g.setColor(Color.white);
        g.setFont(decorFont);
        g.drawString(title.get(), drawPos.getX() + 3 + posunFont, drawPos.getY() + decorY - 1 - posunFont);
    }



    protected void paintScreen(Graphics2D g) {
        drawFrame(g, center, Int2D.zero, 0);
    }

    private void drawFrame(Graphics2D g, Int2D c, Int2D corner, int frameDept) {

        drawProgressBar(g, corner);

        if (showXML.get()) {

            g.setColor(Color.green);
            g.setFont(xmlFont);

            String[] rows = target.toXml().toString().split("\n");

            for (int i=0; i<rows.length; i++) {
                g.drawString(rows[i], c.getX()+4, c.getY()+11*(i+1));
            }

        } else {
            if (frameDept < maxFrameDepth) {
                drawBackground(g, corner.minus(decorDelta));

                if (showZeroPoint) {
                    drawZeroPoint(g , c);
                }

                target.drawInside(g, c, frameDept, zoom.get(), zoomCenter == null ? c : zoomCenter);
            }
        }

        if (!jFrameZatimNezalozen) {

            g.setColor(Color.black);

            if (isHighlighted()) {
                g.setColor(highlightColor);
                g.drawRect(0, 0, size.get().getX()-1, size.get().getY()-1 );
            }

            if (rucksack().showInfo()) {
                g.setFont(idFont);
                g.drawString( id() , 3 , 12 );
            }

            if( isHighlightedFrame ){
                g.setColor( highlightFrameColor );
                g.drawRect( 1, 1 , size.get().getX()-3 , size.get().getY()-3 );
            }
        }
    }




    private void drawProgressBar( Graphics2D g, Int2D corner ){
        g.setColor(Color.green);
        g.fillRect( corner.getX() , corner.getY()+ size.get().getY()-1 , steps % size.get().getX() , 1 );
    }

    private void drawZeroPoint(Graphics2D g, Int2D c) {
        g.setColor(zeroPointColor);
        g.drawLine(c.getX() - 10, c.getY(), c.getX() + 10, c.getY());
        g.drawLine(c.getX(), c.getY() - 10, c.getX(), c.getY() + 10);
    }

    private void drawBackground(Graphics2D g, Int2D drawPos) {
        g.setColor(backgroundColor);
        g.fillRect(drawPos.getX(), drawPos.getY() + decorY, size.get().getX(), size.get().getY());
    }






    /**
     * přepíná mód zobrazování mezi XML reprezentací cílového objektu a klasickým zobrazením GUI
     */
    public void toggleShowXML( ){

        showXML.set( ! showXML.get() );

        if( showXML.get() ){
            camBefore = cam.get();
            backgroundColor = Color.BLACK;
        }
        else{
            backgroundColor = target.bgcolor();
        }

        cam.set((showXML.get() ? Int2D.zero() : camBefore));
        computeCenter();
    }







    private void frameEvent(KEvent kEvent) {
        target.event(kEvent, zoom.get(), center);
    }

    @Override
    public boolean isHit(Int2D clickPos, double zoom, Int2D center) {
        return Int2D.rectangleHit(clickPos, pos() , size.get().getX() , size.get().getY()+decorY ) ;
    }

    @Override
    public void drag(Int2D clickPos, Int2D delta , Frame f) {
        if( isDecorHit(clickPos) ){
            super.drag(clickPos, delta,f);
            if( ! jFrameZatimNezalozen ){
                myJFrame.setLocation( pos().getX() , pos().getY() );
            }
        } else if (showXML.get()) {
            moveCam(delta);
        } else {
            frameEvent(
                    new DragEvent(
                            clickPos.minus(pos().plus(decorDelta)).minus(center) ,
                            delta,
                            this
                    )
            );
        }
    }

    @Override
    public void click(Int2D clickPos) {
        if (showXML.get() || isDecorHit(clickPos)) {
            super.click(clickPos);
        } else {
            frameEvent(new ClickEvent(clickPos.minus(pos().plus(decorDelta)).minus(center) , this));
        }
    }

    @Override
    public void wheel(Int2D wheelPos) {
        if (showXML.get() || isDecorHit(wheelPos)) {
            super.wheel(wheelPos);
        } else {
            zoomCenter = wheelPos.minus(pos().plus(decorDelta)).minus(center);
            frameEvent(new WheelEvent(zoomCenter , this));
        }
    }

    @Override
    public void release( Int2D clickPos ,KObject obj ) {
        frameEvent( new ReleaseEvent(clickPos.minus(pos().plus(decorDelta)).minus(center) ) );
    }


    /**
     * Vrací zda byl kliknutím zasažena vrchní lišta okna.
     */
    public boolean isDecorHit(Int2D clickPos) {
        return clickPos.minus( pos() ).getY() < decorY;
    }

    private Int2D clickPos(MouseEvent e) {
        return new Int2D(e.getX(), e.getY()).minus(center);
    }

    private Int2D wheelPos(MouseWheelEvent e) {
        return clickPos(e);
    }

    // --- MOUSE EVENTS ---

    protected void mousePressed(MouseEvent e){}

    protected void mouseWheel(MouseWheelEvent e) {
        frameEvent(new WheelEvent(wheelPos(e), this));
        rucksack().getSelectedFrame().zoom(e);
        Log.it( rucksack().getSelectedFrame().zoomCenter );
    }

    protected void mouseClicked(MouseEvent e) {
        frameEvent( new ClickEvent( clickPos(e) , this ) );
        rucksack().onClick(e);
        if( e.getButton() == MouseEvent.BUTTON3 ){
            rucksack().clickedRightMouseButton( this , e.getX() , e.getY() );
        }
    }

    protected void mouseReleased(MouseEvent e){
        if( e.getButton() == MouseEvent.BUTTON3 ){return;}
        frameEvent(new ReleaseEvent(clickPos(e)));
    }

    protected void mouseDragged( MouseEvent e , Int2D delta ){
        frameEvent(new DragEvent(clickPos(e), delta, this));
        rucksack().onDrag(e);
        rucksack().onMove(e);
        if( e.getButton() == MouseEvent.BUTTON3 ){
            rucksack().clickedRightMouseButton( this , e.getX() , e.getY() );
        }
    }

    protected void mouseMoved( MouseEvent e ){
        rucksack().onMove(e);
    }

}
