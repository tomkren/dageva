package cz.tomkren.helpers;

import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class MyGraph3 { //extends ApplicationFrame {

    public static void main(String[] args) {

        TimeSeriesCollection tsDataset = new TimeSeriesCollection();
        tsDataset.addSeries(mkS1_b());

        //MyGraph demo =
        new MyGraph3( tsDataset , new Opts("Se s tim smiř!","time","price",true) );

        TimeSeries ts = mkS2_b();
        tsDataset.addSeries(ts);

        XYSeriesCollection xyDataset = new XYSeriesCollection();

        new MyGraph3( xyDataset , new Opts("Klasickej graf!","x-vole","y-pičo",true) );

        XYSeries xys1 = new XYSeries("Key1");
        XYSeries xys2 = new XYSeries("serka2");

        xyDataset.addSeries(xys1);
        xyDataset.addSeries(xys2);


        int stepSize = 1000000;
        int steps = 100;

        for (int i = 0 ; i < steps*stepSize; i++) {
            if (i % stepSize == 0) {
                int x = i/stepSize;
                xys1.add(x, Math.random());

                if (Math.random() < 0.25) {
                    xys2.add(x,Math.random()/2);
                }
            }
        }

    }

    private final Opts opts;
    private final XYDataset series;

    public static class Opts {
        private String title;
        private String xLabel;
        private String yLabel;
        private boolean drawPoints;

        public Opts(String title) {
            this(title, "x", "y",false);
        }

        public Opts(String title, String xLabel, String yLabel, boolean drawPoints) {
            this.title = title;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            this.drawPoints = drawPoints;
        }

        public String getTitle() {
            return title;
        }
        public String getLabelX() {
            return xLabel;
        }
        public String getLabelY() {
            return yLabel;
        }
        public boolean isDrawPoints() {
            return drawPoints;
        }
    }

    public MyGraph3(XYSeries xys, Opts opts) {
        this(new XYSeriesCollection(xys),opts);
    }
    public MyGraph3(TimeSeries ts, Opts opts) {
        this(new TimeSeriesCollection(ts),opts);
    }

    public MyGraph3(XYDataset series, Opts opts) {

        this.opts = opts;
        this.series = series;


    }

    public void start(){
        JFrame frame = new JFrame(opts.getTitle()); // tady to je frame-title
        ChartPanel chartPanel = (ChartPanel) mkPanel(series,opts);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        frame.setContentPane(chartPanel);

        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public void start(int delay, Consumer<MyGraph3> updater) {

        MyGraph3 that = this;

        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame(opts.getTitle());
            ChartPanel chartPanel = (ChartPanel) mkPanel(series,opts);
            chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
            frame.setContentPane(chartPanel);

            frame.pack();
            RefineryUtilities.centerFrameOnScreen(frame);

            Timer timer = new Timer(delay, e -> updater.accept(that));
            timer.start();

            frame.setVisible(true);
        });
    }



    private static JPanel mkPanel(XYDataset series, Opts opts) {
        JFreeChart chart = createChart(series, opts);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    private static TimeSeries mkS1_b() {
        TimeSeries ret = new TimeSeries("LolStar Inc.");

        ret.add(new Day(Log.parseDate("2014-11-01")), 100);
        ret.add(new Day(Log.parseDate("2014-11-02")), 110);
        ret.add(new Day(Log.parseDate("2014-11-03")), 102);
        ret.add(new Day(Log.parseDate("2014-11-04")), 150);

        return ret;
    }

    private static TimeSeries mkS2_b() {
        TimeSeries ret = new TimeSeries("ROFL, spol. s r.o.");

        ret.add(new Day(Log.parseDate("2014-11-01")), 200);
        ret.add(new Day(Log.parseDate("2014-11-02")), 210);
        ret.add(new Day(Log.parseDate("2014-11-03")), 102);
        ret.add(new Day(Log.parseDate("2014-11-04")), 10);

        return ret;
    }



    /* -- tady pokračuje "původní" kód  */

    private static final long serialVersionUID = 1L;

    static {
        // set a theme using the new shadow generator feature available in
        // 1.0.14 - for backwards compatibility it is not enabled by default
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow",true));
    }



    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return A chart.
     */
    private static JFreeChart createChart(XYDataset dataset, Opts opts) {

        JFreeChart chart;

        // todo udelat nějak min halabala
        if (dataset instanceof TimeSeriesCollection) {
            chart = ChartFactory.createTimeSeriesChart(
                    opts.getTitle(),    // title
                    opts.getLabelX(),   // x-axis label
                    opts.getLabelY(),   // y-axis label
                    dataset,            // data
                    true,               // create legend?
                    true,               // generate tooltips?
                    false               // generate URLs?
            );

        } /*else if(dataset instanceof YIntervalSeriesCollection) {
            chart = ChartFactory.createXYLineChart(
                    opts.getTitle(),
                    opts.getLabelX(),
                    opts.getLabelY(),
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);
        }*/ else {
            chart = ChartFactory.createXYLineChart(
                    opts.getTitle(),
                    opts.getLabelX(),
                    opts.getLabelY(),
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
        }


        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        if(dataset instanceof YIntervalSeriesCollection) {
            /* todo rozahy začatek otazka esli neco nekazí - musí bejt v ifu ??? */
            DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
            //deviationrenderer.setSeriesStroke(0, new BasicStroke(3F, 1, 1));
            //deviationrenderer.setSeriesStroke(1, new BasicStroke(3F, 1, 1));
            deviationrenderer.setSeriesFillPaint(0, new Color(255, 200, 200));
            deviationrenderer.setSeriesFillPaint(1, new Color(200, 200, 255));
            plot.setRenderer(deviationrenderer);
            /*rozsahy konec*/
        }

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(opts.isDrawPoints());
            renderer.setBaseShapesFilled(opts.isDrawPoints());
            renderer.setDrawSeriesLineAsPath(true);
        }


        // todo tady taky halabla
        if (dataset instanceof TimeSeriesCollection) {
            DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(Log.dateFormat);
        }

        return chart;

    }







}
