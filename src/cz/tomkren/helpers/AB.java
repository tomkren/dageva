package cz.tomkren.helpers;

import cz.tomkren.typewars.eva.Probable;

// modifikovatelný Pár
public class AB<A,B> implements Probable {

    private A a;
    private B b;

    public AB(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A,B> AB<A,B> mk(A a, B b) {
        return new AB<>(a,b);
    }

    public A _1() {return a;}
    public B _2() {return b;}

    public void set_1(A a) {this.a = a;}
    public void set_2(B b) {this.b = b;}

    @Override
    public String toString() {
        return "<" + a +","+ b + ">";
    }

    @Override
    public double getProbability() {
        return (Double) b; //TODO unsafe here...
    }
}
