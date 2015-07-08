package cz.tomkren.kutil2.shapes;


import cz.tomkren.kutil2.core.KShape;
import cz.tomkren.kutil2.items.Int2D;
import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.shapes.ConvexPolygon;
import net.phys2d.raw.shapes.DynamicShape;

import java.awt.*;

/**
 * Tvar pro vytváření tvaru konvexního polygonu.
 * @author Tomáš Křen
 */
public class ConvexPolygonShape implements KShape {

    private   Color         color;
    private   ConvexPolygon convexPolygon;
    protected Int2D         posPoint;
    protected Int2D         t;


    private static final Color highlightColor = Color.red;
    private static final Font  idFont = new Font( Font.SANS_SERIF , Font.PLAIN , 10 );


    public ConvexPolygonShape(Int2D[] vs, Int2D posPoint, Int2D t, Color c){
        color = c;
        //this.t = t;
        this.posPoint = posPoint;

        // todo tady je to nějaký megazmatený pořádně rozmyslet
        // přepočítáme souřadnice tak aby těžiště bylo na souřadnici [0,0]
        //posPoint = posPoint.minus(t);

        if (vs != null) {
            initVs(vs);
        }
        if (t!= null) {
            initT(t);
        }
    }

    public void initT(Int2D t) {
        this.t = t;
    }

    public void initVs(Int2D[] vs) {

        for (int i=0; i<vs.length; i++) {
            vs[i] = vs[i].minus(t) ;
        }
        convexPolygon = new ConvexPolygon( Int2D.toROVector2f(vs) );
    }

    @Override
    public void draw(Graphics2D g, boolean isSel, String info , 
                     Int2D pos , Int2D center , double rot,boolean isRotable, double zoom, Int2D zoomCenter){

        Vector2f[] verts = convexPolygon.getVertices( getPhys2dCenter(pos) , (float) rot );
        Int2D drawPos = center.plus(pos);

        int numVertices = convexPolygon.NumVertices();

        int[] polyXs = new int[numVertices];
        int[] polyYs = new int[numVertices];

        int dx = center.getX();
        int dy = center.getY();

        for (int i = 0 ; i < verts.length; i++) {
            polyXs[i] = (int) (0.5f + verts[i].getX()) + dx;
            polyYs[i] = (int) (0.5f + verts[i].getY()) + dy;
        }

        g.setColor(color);
        g.fillPolygon(polyXs, polyYs, numVertices);

        if (isSel) {
            g.setColor(highlightColor);

            for ( int i = 0, j = verts.length-1; i < verts.length; j = i, i++ ) {
                g.drawLine(     (int) (0.5f + verts[i].getX()) + dx,
                                (int) (0.5f + verts[i].getY()) + dy,
                                (int) (0.5f + verts[j].getX()) + dx,
                                (int) (0.5f + verts[j].getY()) + dy );
            }
        }

        if (info != null) {
            g.setFont(idFont);
            g.drawString( info , drawPos.getX() , drawPos.getY()-3 );
        }

    }

    @Override
    public boolean isHit(Int2D pos , Int2D clickPos , double rot, double zoom, Int2D zoomCenter) {

        if (Math.abs(zoom - 1.0) > 0.000001) {return false;} // TODO docasne neřešíme označení na jinejch zoomech než 1

        Vector2f[] verts = convexPolygon.getVertices( getPhys2dCenter(pos) , (float) rot );

        Vector2f p = Int2D.toROVector2f( clickPos );

        // p is in the polygon if it is left of all the edges
        int l = verts.length;
        for ( int i = 0; i < verts.length; i++ ) {
                Vector2f x = verts[i];
                Vector2f y = verts[(i+1)%l];
                Vector2f z = p;

                // does the 3d cross product point up or down?
                if ( (z.x-x.x)*(y.y-x.y)-(y.x-x.x)*(z.y-x.y) >= 0 )
                        return false;
        }

        return true;
    }

    @Override
    public DynamicShape getPhys2dShape(){
        return convexPolygon;
    }

    @Override
    public ROVector2f getPhys2dCenter(Int2D pos) {
        return Int2D.toROVector2f( pos.minus(posPoint) );
    }

    @Override
    public Int2D getPosByPhys2dCenter(ROVector2f v) {
        return Int2D.fromROVector2f(v) .plus( posPoint );
    }


}
