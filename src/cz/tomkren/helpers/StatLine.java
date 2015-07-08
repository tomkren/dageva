package cz.tomkren.helpers;


import org.jfree.data.xy.*;

import java.util.ArrayList;
import java.util.List;

public class StatLine {

    //private YIntervalSeries series;

    private boolean showMinMax;

    private XYSeries avgSeries;
    private XYSeries maxSeries;
    private XYSeries minSeries;
    private List<Double>  sums;
    private List<Integer> ns;

    public StatLine(String name) {
        this(name,true);
    }

    public StatLine(String name, boolean showMinMax) {

        this.showMinMax = showMinMax;

        //series = new YIntervalSeries(name);

        avgSeries = new XYSeries(name+"-avg");
        maxSeries = new XYSeries(name+"-max");
        minSeries = new XYSeries(name+"-min");
        sums = new ArrayList<>();
        ns   = new ArrayList<>();

    }


    public void clear() {

        //series.clear();

        avgSeries.clear();
        maxSeries.clear();
        minSeries.clear();
        sums.clear();
        ns.clear();
    }

    public double getLastAvg() {
        int index = sums.size() - 1;
        if (index < 0) {throw new Error("StatLine is empty");}
        return sums.get(index) / ns.get(index);
    }

    public void add(int i, double val) {

        int len = avgSeries.getItemCount();

        if (i > len) {

            throw new TODO("Do AvgLine se zatím musí přidávat postupně. (i: "+i+" len: "+len+")");

        } else if (i == len) {

            avgSeries.add(i, val);
            maxSeries.add(i, val);
            minSeries.add(i, val);
            sums.add(val);
            ns.add(1);

        } else {


            XYDataItem item = avgSeries.getDataItem(i);

            double x = item.getXValue();

            if (x != (double)i) {throw new Error("in add i must be = x");}

            int    n   = 1   + ns.get(i);
            double sum = val + sums.get(i);
            double avg = sum / n;

            ns.set(i,n);
            sums.set(i,sum);
            avgSeries.update(x,avg);

            if (val > maxSeries.getDataItem(i).getYValue()) { maxSeries.update(x,val);}
            if (val < minSeries.getDataItem(i).getYValue()) { minSeries.update(x,val);}

        }

    }

/*
    public void add2(int i, double val) {

        int len = series.getItemCount();

        if (i > len) {
            throw new TODO("Do AvgLine se zatím musí přidávat postupně. (i: "+i+" len: "+len+")");
        } else if (i == len) {
            series.add(i, val, val, val);
            sums.add(val);
            ns.add(1);
        } else {

            //XYDataItem item = avgSeries.getDataItem(i);

            double x = (double) series.getX(i);
            //double x = item.getXValue();

            if (x != (double)i) {throw new Error("in add i must be = x | x "+x+", i "+i);}

            int    n   = 1   + ns.get(i);
            double sum = val + sums.get(i);
            double avg = sum / n;

            ns.set(i,n);
            sums.set(i,sum);

            series.remove(x);

            double max = Math.max(val, series.getYHighValue(i)  );
            double min = Math.min(val, series.getYLowValue(i)   );

            series.add(x,avg, min,max);

            //avgSeries.update(x,avg);
            //if (val > maxSeries.getDataItem(i).getYValue()) { maxSeries.update(x,val);}
            //if (val < minSeries.getDataItem(i).getYValue()) { minSeries.update(x,val);}

        }

    }
*/
    public void addThereSeries(XYSeriesCollection dataset) {
        dataset.addSeries(avgSeries);
        if (showMinMax) {
            dataset.addSeries(maxSeries);
            dataset.addSeries(minSeries);
        }
    }


/*
    public void addThere2(YIntervalSeriesCollection dataset) {
        dataset.addSeries(series);
    }
*/

    public static void main(String[] args) {

        StatLine statLine = new StatLine("Pokusná");

        statLine.add(0,100);
        statLine.add(1,150);
        statLine.add(0,300);
        statLine.add(2,150);
        statLine.add(1,25);


        XYSeriesCollection data = new XYSeriesCollection();
        statLine.addThereSeries(data);
        new MyGraph(data, new MyGraph.Opts("AvgLine pokusy"));
/*

        StatLine statLine2 = new StatLine("Pokusná2");

        statLine2.add2(0, 100);
        statLine2.add2(1, 150);
        statLine2.add2(0, 300);
        statLine2.add2(2, 150);
        statLine2.add2(1, 25);


        YIntervalSeriesCollection datas = new YIntervalSeriesCollection();
        statLine2.addThere2(datas);
        new MyGraph(datas, new MyGraph.Opts("AvgLine pokusy 2"));
*/
        /*

        YIntervalSeries test = new YIntervalSeries("lol");
        test.add(0,2,1,3);
        test.add(1,20,10,300);
        test.add(2,15,10,30);

        YIntervalSeries test2 = new YIntervalSeries("lol2");
        test2.add(0,25,15,35);
        test2.add(1,205,150,350);
        test2.add(2,155,105,350);

        YIntervalSeriesCollection col = new YIntervalSeriesCollection();
        col.addSeries(test);
        col.addSeries(test2);

        new MyGraph(col, new MyGraph.Opts("AvgLine pokus 2")); // TODO asi s tim nepočítá MyGraf

        */

    }

}
