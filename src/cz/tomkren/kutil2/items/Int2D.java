package cz.tomkren.kutil2.items;


import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;

public class Int2D {

    private int x, y;

    public Int2D( int x , int y ){
        this.x = x;
        this.y = y;
    }

    public Int2D copy() {return new Int2D(x, y);}

    @Override
    public String toString() {return x + " " + y;}

    public int getX() {return x;}
    public int getY() {return y;}

    public void setX( int newX ){x = newX;}
    public void setY( int newY ){y = newY;}


    public static Int2D parseInt2D(String str) {
        String[] arr = str.trim().split("\\s+", 2);
        if (arr.length != 2) {return new Int2D(0,0);} // todo možná lepší pro 1D vrátit 1D
        return new Int2D(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
    }

    public static final Int2D zero = new Int2D(0,0);
    public static Int2D zero(){return new Int2D(0,0);}


    public Int2D times( double q ){
        Int2D ret = new Int2D(x, y);
        ret.x = (int) Math.round(q * x);
        ret.y = (int) Math.round(q * y);
        return ret;
    }

    public Int2D plus( Int2D i ){
        Int2D ret = new Int2D(x,y);
        ret.x += i.x;
        ret.y += i.y;
        return ret;
    }

    public Int2D minus( Int2D i ){
        Int2D ret = new Int2D(x,y);
        ret.x -= i.x;
        ret.y -= i.y;
        return ret;
    }

    public void adjustX( int dx ){
        x += dx;
    }
    public void adjustY( int dy ){
        y += dy;
    }

    public Int2D negate() {return new Int2D(-x, -y);}


    public static boolean rectangleHit( Int2D clickPos , Int2D cornerPos , int width , int height ){
        if( clickPos.getX() < cornerPos.getX()          ) return false;
        if( clickPos.getY() < cornerPos.getY()          ) return false;
        if( clickPos.getX() > cornerPos.getX() + width  ) return false;
        if( clickPos.getY() > cornerPos.getY() + height ) return false;
        return true;
    }

    public static Vector2f toROVector2f(Int2D int2D) {
        return new Vector2f(int2D.getX(), int2D.getY());
    }

    public static ROVector2f[] toROVector2f(Int2D[] vs) {
        ROVector2f[] ret = new ROVector2f[vs.length] ;
        for (int i=0; i< vs.length; i++) {
            ret[i] = new Vector2f(vs[i].getX(), vs[i].getY());
        }
        return ret;
    }


    public static Int2D fromROVector2f(ROVector2f rov){
        return new Int2D((int)rov.getX(), (int)rov.getY());
    }


}

