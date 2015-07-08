package cz.tomkren.kutil2.shapes;

import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.F;
import cz.tomkren.kutil2.core.KObject;
import cz.tomkren.kutil2.core.KShape;
import cz.tomkren.kutil2.core.Kutil;
import cz.tomkren.kutil2.items.Int2D;
import net.phys2d.math.Vector2f;

import java.awt.*;
import java.util.List;


public class FunctionShape extends ConvexPolygonShape {

    private int numInputs, numOutputs;
    private String name;
    private List<AB<String,Integer>> targets;
    private Kutil kutil;



    public FunctionShape(int numInputs, int numOutputs, String name, List<AB<String,Integer>> targets, Kutil kutil) {
        super(null, new Int2D(0,0), null, Color.lightGray);
        this.numInputs  = numInputs;
        this.numOutputs = numOutputs;
        this.name = name;
        this.targets = targets;
        this.kutil = kutil;

        initVs(mkVs(numInputs, numOutputs));
    }

    private static final Color lineColor = new Color(129, 129, 129);
    private static final Color lineSelectColor = Color.red;

    private static final Color inputColor  = new Color(250,250,250);
    private static final Color outputColor = new Color(250,250,250);
    private static final Color bodyColor   = new Color(253,253,254);

    private static final Color linkColor = Color.gray;
    private static final Color littleArrowColor = Color.lightGray;




    private double zoom ;
    private int zoom_dx ;
    private int zoom_dy ;

    private void resetZoomData(double zoom, Int2D zoomCenter, Int2D center) {

        Int2D c = zoomCenter.plus(center);

        this.zoom = zoom;
        zoom_dx = (int) ((1 - zoom)*c.getX());
        zoom_dy = (int) ((1 - zoom)*c.getY());
    }

    private int xZoomIt(int x) {return (int)(x*zoom + zoom_dx);}
    private int yZoomIt(int y) {return (int)(y*zoom + zoom_dy);}

    private int xZoomInverse(int x) {return (int)((x-zoom_dx)/zoom);}
    private int yZoomInverse(int y) {return (int)((y-zoom_dy)/zoom);}

    private float xZoomIt(float x) {return (float)(x*zoom + zoom_dx);}
    private float yZoomIt(float y) {return (float)(y*zoom + zoom_dy);}


    private Int2D zoomIt(Int2D p) {return new Int2D(xZoomIt(p.getX()),yZoomIt(p.getY()));}

    private Vector2f zoomIt(Vector2f v) {return new Vector2f(xZoomIt(v.x),yZoomIt(v.y));}


    private void zoomFill(Graphics2D g, int[] xs, int[] ys) {
        int n = xs.length;

        int[] xs2 = new int[n];
        int[] ys2 = new int[n];

        for (int i = 0; i < n; i++) {
            xs2[i] = xZoomIt(xs[i]);
            ys2[i] = yZoomIt(ys[i]);
        }

        g.fillPolygon(xs2, ys2, n);
    }

    private void zoomLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        g.setStroke(new BasicStroke( Math.max((float)zoom , 0.3f)) );
        g.drawLine(xZoomIt(x1), yZoomIt(y1), xZoomIt(x2), yZoomIt(y2));
        g.setStroke(new BasicStroke(1));
    }


    @Override
    public void draw(Graphics2D g, boolean isSel, String info, Int2D pos, Int2D center, double rot, boolean isRotable, double zoom, Int2D zoomCenter) {
        //super.draw(g, isSel, info, pos, center, rot, isRotable);

        resetZoomData(zoom, zoomCenter, center);



        int dx = center.getX() + pos.getX() - T.getX();
        int dy = center.getY() + pos.getY() - T.getY();

        int a_x= (        dx), a_y= (dy);
        int b_x= (w_up   +dx), b_y= (dy);
        int c_x= (w_up   +dx), c_y =( arrows_y+dy);
        int d_x= (down_x +dx), d_y =( help_y  +dy);
        int e_x= (down_x +dx), e_y =( basic_h +dy);
        int f_x= (delta_x+dx), f_y =( basic_h +dy);
        int g_x= (delta_x+dx), g_y =( help_y  +dy);
        int h_x= (        dx), h_y =( arrows_y+dy);


        int[] in_poly_x = new int[] {a_x, b_x, c_x, h_x};
        int[] in_poly_y = new int[] {a_y, b_y, c_y, h_y};

        int[] out_poly_x = new int[] {d_x, e_x, f_x, g_x};
        int[] out_poly_y = new int[] {d_y, e_y, f_y, g_y};

        int[] body_poly_x = new int[] {c_x, d_x, g_x, h_x};
        int[] body_poly_y = new int[] {c_y, d_y, g_y, h_y};



        g.setColor(inputColor);
        zoomFill(g, in_poly_x, in_poly_y);

        g.setColor(outputColor);
        zoomFill(g, out_poly_x, out_poly_y);

        g.setColor(bodyColor);
        zoomFill(g, body_poly_x, body_poly_y);


        g.setColor(lineColor);

        zoomLine(g, h_x, h_y, c_x, c_y);
        zoomLine(g, g_x, g_y, d_x, d_y);

        for (int i = 1; i < numInputs; i++) {
            int x = dx+basic_w*i;
            zoomLine(g, x, dy, x, c_y);
        }

        for (int i = 1; i < numOutputs; i++) {
            int x = f_x+basic_w*i;
            zoomLine(g, x, e_y, x, d_y);
        }

        g.setColor(isSel?lineSelectColor:lineColor);

        zoomLine(g, a_x, a_y, b_x, b_y);
        zoomLine(g, b_x, b_y, c_x, c_y);
        zoomLine(g, c_x, c_y, d_x, d_y);
        zoomLine(g, d_x, d_y, e_x, e_y);
        zoomLine(g, e_x, e_y, f_x, f_y);
        zoomLine(g, f_x, f_y, g_x, g_y);
        zoomLine(g, g_x, g_y, h_x, h_y);
        zoomLine(g, h_x, h_y, a_x, a_y);

        g.setColor(littleArrowColor);

        for (int i=0; i < numInputs; i++) {
            drawArrow(g, dx, dy, i);
        }

        for (int i=0; i < numOutputs; i++) {
            drawArrow(g, dx+delta_x, dy+help_y, i);
        }

        if (targets != null) {

            g.setColor(linkColor);

            int localOutputPort = 0;
            for (AB<String,Integer> t : targets) {
                if (t != null) {
                    String targetId = t._1();
                    int targetInputPort = t._2();

                    int x = (int) (f_x+basic_w*(0.5+localOutputPort));

                    KObject target = kutil.getIdDB().get(targetId); // TODO kešovat si, nehledat při každém překlesení, ale pak potřeba reagovat na změny

                    if (target != null) {

                        KShape tShape = target.shape();
                        Int2D targetDrawPos = tShape instanceof FunctionShape ? ((FunctionShape)tShape).getTargetDrawPos(targetInputPort) : Int2D.zero ;

                        if (targetDrawPos != null) {
                            targetDrawPos = center.plus(targetDrawPos).plus(target.pos());
                            zoomLine(g, x, e_y + 1, targetDrawPos.getX(), targetDrawPos.getY());
                        }
                    }

                }
                localOutputPort++;
            }

        }

        if (name != null) {


            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(9*zoom) );


            int nameWidth = (int) ( font.getStringBounds(name, g.getFontRenderContext()).getWidth() / zoom );

            int str_x = (w_up-nameWidth)/2 + dx;
            int str_y = font_y+dy;


            if (nameWidth > Math.max(w_up, w_down)) {



                a_x= str_x-3;              a_y=  ( str_y +4 );
                b_x= str_x+1+nameWidth; b_y=  ( str_y +4 );
                c_x= str_x+1+nameWidth; c_y = ( str_y -11);
                d_x= str_x-3;              d_y = ( str_y -11);

                int[] poly_x = new int[] {a_x, b_x, c_x, d_x};
                int[] poly_y = new int[] {a_y, b_y, c_y, d_y};

                g.setColor(bodyColor);
                zoomFill(g, poly_x, poly_y);

                g.setColor(Color.lightGray);

                zoomLine(g, a_x, a_y, b_x, b_y);
                zoomLine(g, b_x, b_y, c_x, c_y);
                zoomLine(g, c_x, c_y, d_x, d_y);
                zoomLine(g, d_x, d_y, a_x, a_y);

            }



            g.setFont(font);
            g.setColor(Color.black);
            g.drawString(name, xZoomIt(str_x), yZoomIt(str_y));

        }


    }

    private void drawArrow(Graphics2D g, int dx, int dy, int i) {
        int x = (int)(dx+basic_w*(i+0.5));
        int y1 = dy+6;
        int y2 = dy+4;
        int y3 = dy+2;
        zoomLine(g, x, y1, x, y3);
        zoomLine(g, x, y1, x - 2, y2);
        zoomLine(g, x, y1, x + 2, y2);
    }


    public Int2D getTargetDrawPos(int targetInputPort) {
        Int2D ret = T.negate();
        ret.adjustX((int)((0.5+targetInputPort)*basic_w));
        return ret;
    }


    public static final int arrows_y = 9;
    public static final int basic_w  = 33;
    public static final int basic_h  = 45;

    private static final int font_y  = basic_h/2 + 4;


    private int w_up;
    private int w_down;
    private int delta_x;
    private int down_x;
    private int help_y;

    private Int2D T;


    private Int2D[] mkVs(int numInputs, int numOutputs) {

        w_up   = basic_w * numInputs;
        w_down = basic_w * numOutputs;

        delta_x = (w_up - w_down)/2;
        down_x = w_down +delta_x;
        help_y = basic_h-arrows_y;

        T = new Int2D( w_up > w_down ? (int)(Math.max(w_up,w_down)*0.5) +17 : 33 ,25);

        initT(T);

        return new Int2D[]{
                new Int2D(0,0),
                new Int2D(w_up,0),
                new Int2D(down_x+arrows_y,basic_h),
                new Int2D(delta_x-arrows_y,basic_h)
        };
    }


}
