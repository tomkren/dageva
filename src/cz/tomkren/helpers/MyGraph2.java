package cz.tomkren.helpers;

import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;


// todo : Příprava na thread safe variantu grafu

public class MyGraph2 extends JPanel {

    private final DynamicTimeSeriesCollection dataset;

    public MyGraph2(final String title) {
        dataset = new DynamicTimeSeriesCollection(1, 1000, new Second());
        dataset.setTimeBase(new Second(0, 0, 0, 23, 1, 2014));
        dataset.addSeries(new float[1], 0, title);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, "Time", title, dataset, true, true, false);
        final XYPlot plot = chart.getXYPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setFixedAutoRange(10000);
        axis.setDateFormatOverride(new SimpleDateFormat("ss.SS"));
        final ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel);
    }

    public void update(float value) {
        float[] newData = new float[1];
        newData[0] = value;
        dataset.advanceTime();
        dataset.appendData(newData);
    }



    public void start(int delay, Consumer<MyGraph2> updater) {

        MyGraph2 that = this;

        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("testing");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);//(JFrame.EXIT_ON_CLOSE);

            frame.add(that);
            frame.pack();

            Timer timer = new Timer(delay, e -> updater.accept(that));
            timer.start();

            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new TestCase();
    }

    public static class TestCase {

        private int i,j; // TODO nemělo by to bejt nějaký thred-protected nebo tak něco

        public TestCase() {

            i = j = 0;

            MyGraph2 g = new MyGraph2("Pokusnej");
            g.start(100, gr ->  {
                gr.update( (float) i );
            });

            int steps = 10000000;
            while (true) {
                if (i % steps == 0) {
                    Log.it(j);
                    j++;
                }
                i++;
            }

        }



    }

}

