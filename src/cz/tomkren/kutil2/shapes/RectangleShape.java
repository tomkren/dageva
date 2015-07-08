package cz.tomkren.kutil2.shapes;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.Function;

import cz.tomkren.helpers.F;
import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.DynamicShape;

import cz.tomkren.kutil2.core.KShape;
import cz.tomkren.kutil2.items.Int2D;


public class RectangleShape implements KShape {

    private Int2D size;
    private Color color;
    private Box   phys2DBox;

    private static final Color highlightColor = Color.red;
    private static final Font  idFont = new Font(Font.SANS_SERIF , Font.PLAIN , 10);

    public RectangleShape(Int2D size, Color color) {
        this.size     = size;
        this.color    = color;
        phys2DBox     = new Box( size.getX() , size.getY() );
    }

    public void setSize(Int2D s) {
        size      = s;
        phys2DBox = new Box( size.getX() , size.getY() );
    }


    private double zoom ;
    private int zoom_dx ;
    private int zoom_dy ;

    private void resetZoomData(double zoom, Int2D center) {
        this.zoom = zoom;
        zoom_dx = (int) ((1 - zoom)*center.getX());
        zoom_dy = (int) ((1 - zoom)*center.getY());
    }

    private int xZoomIt(int x) {return (int)(x*zoom + zoom_dx);}
    private int yZoomIt(int y) {return (int)(y*zoom + zoom_dy);}

    private float xZoomIt(float x) {return (float)(x*zoom + zoom_dx);}
    private float yZoomIt(float y) {return (float)(y*zoom + zoom_dy);}

    private Int2D zoomIt(Int2D p) {return new Int2D(xZoomIt(p.getX()),yZoomIt(p.getY()));}

    private Vector2f zoomIt(Vector2f v) {return new Vector2f(xZoomIt(v.x),yZoomIt(v.y));}




    @Override
    public void draw(Graphics2D g,
                     boolean isSelected,
                     String info ,
                     Int2D pos ,
                     Int2D center ,
                     double rot,
                     boolean isRotable,
                     double zoom, Int2D zoomCenter){



        Vector2f[] vs = phys2DBox.getPoints( getPhys2dCenter(pos) , (float) rot );

        Int2D drawPos = center.plus(pos);

        int dx = center.getX();
        int dy = center.getY();

        int[] xs = {(int)vs[0].x+dx,(int)vs[1].x+dx,(int)vs[2].x+dx,(int)vs[3].x+dx};
        int[] ys = {(int)vs[0].y+dy,(int)vs[1].y+dy,(int)vs[2].y+dy,(int)vs[3].y+dy};

        resetZoomData(zoom, zoomCenter);
        F.adjustArray(xs, this::xZoomIt);
        F.adjustArray(ys, this::yZoomIt);

        g.setColor( color );
        g.fillPolygon( xs , ys , 4 );

        if (color == Color.white) {
            g.setColor(Color.black);
            g.drawPolygon( xs , ys , 4 );
        }

        if (isSelected) {
            g.setColor( highlightColor );
            g.drawPolygon( xs , ys , 4 );
        }

        if (info != null) {
            g.setFont(idFont);
            g.drawString(info, drawPos.getX(), drawPos.getY()-3);
        }

    }

    @Override
    public boolean isHit(Int2D pos , Int2D clickPos , double rot, double zoom, Int2D zoomCenter) {

        Vector2f[] verts = phys2DBox.getPoints( getPhys2dCenter(pos) , (float) rot );
        Vector2f p = Int2D.toROVector2f(clickPos);

        resetZoomData(zoom, zoomCenter);
        adjustArray(verts, this::zoomIt);
        //p = zoomIt(p);

        // p is in the polygon if it is left of all the edges
        int l = verts.length;
        for ( int i = 0; i < verts.length; i++ ) {
            Vector2f x = verts[i];
            Vector2f y = verts[(i+1)%l];

            // does the 3d cross product point up or down?
            if ( (p.x-x.x)*(y.y-x.y)-(y.x-x.x)*(p.y-x.y) >= 0 )
                return false;
        }

        return true;
    }

    public DynamicShape getPhys2dShape(){
        return phys2DBox;
    }

    @Override
    public ROVector2f getPhys2dCenter(Int2D pos){
        return new Vector2f( pos.getX() + size.getX()/2f , pos.getY() + size.getY()/2f );
    }

    @Override
    public Int2D getPosByPhys2dCenter(ROVector2f v) {
        return new Int2D( (int)(v.getX() - size.getX()/2f) , (int)(v.getY() - size.getY()/2f)  );
    }


    private static void adjustArray(Vector2f[] xs, Function<Vector2f,Vector2f> f) {
        int len = xs.length;
        for (int i = 0; i < len; i++) {
            xs[i] = f.apply(xs[i]);
        }
    }

}
