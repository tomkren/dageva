package cz.tomkren.helpers;

public class Paar<A,B> {

    private final A a;
    private final B b;

    public Paar(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A _1() {return a;}
    public B _2() {return b;}

    public static <A,B> Paar mk(A a, B b) {
        return new Paar<>(a,b);
    }

    @Override
    public String toString() {
        return "<" + a +","+ b + ">";
    }
}
