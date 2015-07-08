package cz.tomkren.kutil2.core;

import cz.tomkren.helpers.F;
import cz.tomkren.kutil2.kobjects.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Scheduler je jednoduchý plánovač, který můžeme chápat jako kořen celé hierarchie KObjectů,
 * který volá u nejvyšších KObjectů z hierarchie metodu step().
 * Toto volání pak kaskádovitě projde celou hierarchií a má za důsledek všechny
 * akce provedené v tomto kroku běhu virtuálního světa.
 * Všechny akce pak jsou projevem jednotlivých KObjectů,
 * které mají možnost manipulovat i se strukturou hierarchie.
 * @author Tomáš Křen
 */
public class Scheduler {

    private PriorityQueue<CalendarEvent> calendar;
    private boolean doTerminate;
    private List<Time> times;

    public Scheduler(KAtts kAtts, Kutil kutil) {
        kutil.setScheduler(this);
        calendar = new PriorityQueue<>(3, new CalendarEventComparator());
        doTerminate = false;

        List<KObject> ts = kAtts.getList("times");
        times = F.map(ts, o->(Time)o);

        times.forEach(o -> calendar.add(new CalendarEvent(o)));
        start();
    }

    private void start() {
        while (true) {
            long actionStart = time();
            if (calendar.isEmpty()) break;

            CalendarEvent ce = calendar.poll(); //vyndáme co bylo naplánováno na teď.
            Time time = ce.time;

            //pokud tento time byl už vyčerpán, neděláme nic a přeskočíme zbytek cyklu, abychom mohli vybrat další událost v pořadí
            if (time.isFinished()) {continue;}

            time.decrementIterations(); //snížíme iterator času

            time.step(); //zde se provádí step(), což je raison d'etre celého scheduleru

            calendar.add( new CalendarEvent(time, ce.when + time.getPeriod()) ); //naplánujeme další událost tohoto time

            long deltaTime = time() - actionStart; //spočteme jak dlouho nám trvala tato událost

            if (calendar.isEmpty() || doTerminate) {break;} //pokud už není nic v kalendáři, žádná událost - opustíme smyčku

            sleep( calendar.peek().when - (ce.when + deltaTime) ); //na dobu, než je naplánována další událost usneme
        }
    }

    public void terminate() {
        doTerminate = true;
    }


    public void forEachTime(Consumer<Time> f) {
        times.stream().forEach(f);
    }

    /**
     * Bezpečný sleep (pro záporné hodnoty nespí).
     * @param milliseconds počet milisekund
     */
    private static void sleep(long milliseconds) {
        if (milliseconds <= 0) {return;}

        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    private static long time() {return System.nanoTime() / 1000000L;}

    private class CalendarEvent {

        public long when; //(relativní) čas kdy má být uskutečněna tato událost
        public Time time; //KObject time v kterym se bude stepovat

        public CalendarEvent(Time t) {
            time = t;
            when = t.getPeriod();
        }

        public CalendarEvent(Time t, long w) {
            time = t;
            when = w;
        }
    }

    class CalendarEventComparator implements Comparator<CalendarEvent> {
        @Override
        public int compare(CalendarEvent x, CalendarEvent y) {
            if (x.when < y.when) {return -1;}
            if (x.when > y.when) {return  1;}
            return 0;
        }
    }

}
