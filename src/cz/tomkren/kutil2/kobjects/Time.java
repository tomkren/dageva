package cz.tomkren.kutil2.kobjects;

import cz.tomkren.kutil2.core.KAtts;
import cz.tomkren.kutil2.core.KObject;
import cz.tomkren.kutil2.core.Kutil;
import cz.tomkren.kutil2.items.KItem;

public class Time extends KObject {

    private KItem<Integer> iterations ; // kolik se má udělat ještě iterací

    private long period ; // milliseconds

    public Time(KAtts kAtts, Kutil kutil) {
        super(kAtts, kutil);
        setType("time");

        KItem<Double> ups = items().addDouble(kAtts, "ups", 80.0);
        iterations = items().addInteger( kAtts , "iterations" , null ); //null znamená nekonečno

        period = Math.round( 1000.0 / ups.get() ) ;
    }

    public long getPeriod(){return period;}

    public void decrementIterations() {
        if (iterations.get() == null) return;
        iterations.set(iterations.get() - 1);
    }

    public boolean isFinished() {
        return iterations.get() != null && (iterations.get() <= 0);
    }

}
