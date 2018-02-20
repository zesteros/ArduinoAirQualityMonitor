package angelo.itl.arduinoairqualitymonitor.activity.plot;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;

import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.util.*;
import com.androidplot.xy.*;

import java.text.DecimalFormat;

import angelo.itl.arduinoairqualitymonitor.*;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.util.sensor.GasComputer;

public class RemainingGasesRealTimePlotActivity extends AppCompatActivity {

    protected XYPlot plot = null;

    protected SimpleXYSeries[] serie = new SimpleXYSeries[6];
    protected String[] names;
    protected Redrawer redrawer;
    protected Updater updater;
    private Thread updateThread;
    private final int
            y1 = 0, //start y1 plot
            y2 = 200, //end y2 plot
            x1 = 0, //start x1 plot
            x2 = 100;//end x2 plot
    protected View view = null;
    private PanZoom panZoom;
    protected GlobalVariable global;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remaining_gases_real_time_plot);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_plot_all_gases);
        plot = (XYPlot) findViewById(R.id.gas_plot);
        global = global.getInstance();
        global.isAnotherActivityVisible(true);
        instantiatePlotSeries();
        //setBoundaries(y1, y2, x1, x2, BoundaryMode.FIXED, BoundaryMode.FIXED);
        addSeriesToPlot(plot, serie);
        //XYPlot plot, int domainStepValue, int rangeStepValue, int
        // ticksPerRangeLabel, int dpPosititionHeight, int dpWeight, int spTextSize
        configurePlot(plot, 10, 20, 10, 60, 310, 8);

        updater = new Updater();
        redrawer = new Redrawer(plot, 100, false);
        panZoom = PanZoom.attach(plot/*, x1, x2, y1, y2*/);
        //plot.setDomainBoundaries(0,100,BoundaryMode.FIXED);
    }

    public void getGasNamesAsArray() {
        names = getResources().getStringArray(R.array.gases);
    }

    public void instantiatePlotSeries() {
        getGasNamesAsArray();
        int j = 0;
        for (int i = 0; i < serie.length; i++) {
            serie[i] = new SimpleXYSeries(names[j]);
            serie[i].useImplicitXVals();
            j++;
        }
    }

    public void addSeriesToPlot(XYPlot plot, SimpleXYSeries[] serie) {
        for (int i = 0; i < serie.length; i++)
            plot.addSeries(serie[i], configureFormatter(i));
    }

    public void setBoundaries(int y1, int y2, int x1, int x2, BoundaryMode rangeBoundaryMode,
                              BoundaryMode domainBoundaryMode) {
        plot.setRangeBoundaries(y1, y2, rangeBoundaryMode);
        plot.setDomainBoundaries(x1, x2, domainBoundaryMode);
    }

    public void configurePlot(XYPlot plot, int domainStepValue, int rangeStepValue, int
            ticksPerRangeLabel, int dpPosititionHeight, int dpWeight, int spTextSize) {

        plot.setDomainStepMode(StepMode.SUBDIVIDE);
        plot.setDomainStepValue(domainStepValue);
        plot.setRangeStepValue(rangeStepValue);
        plot.setLinesPerRangeLabel(ticksPerRangeLabel);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setTextSize(PixelUtils.dpToPix(10));
        plot.setDomainLabel(getString(R.string.plot_domain_label));
        //plot.getDomainLabelWidget().pack();
        plot.setRangeLabel(getString(R.string.ppm));
        //plot.getRangeLabelWidget().pack();
        plot.getLegend().setSize(new Size(PixelUtils.dpToPix(dpPosititionHeight),
                     SizeMode.ABSOLUTE, PixelUtils.dpToPix(dpWeight), SizeMode.ABSOLUTE));
        //plot.getLegendWidget().setSize(new Size(PixelUtils.dpToPix(dpPosititionHeight),
          //      SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(dpWeight), SizeLayoutType.ABSOLUTE));***
        plot.getLegend().getTextPaint().setTextSize(PixelUtils.spToPix(spTextSize));
        //plot.getLegendWidget().getTextPaint().setTextSize(PixelUtils.spToPix(spTextSize));***
        //plot.setRangeValueFormat(new DecimalFormat("#"));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("#"));
        //plot.setDomainValueFormat(new DecimalFormat("#"));
        //plot.setGridPadding(0,0,0,0);
        /*plot.getGraphWidget().setPadding(
                getResources().getDimension(R.dimen.plot_dimen_rt),
                0,
                0,
                getResources().getDimension(R.dimen.plot_dimen_rt));*/
    }


    protected LineAndPointFormatter configureFormatter(int i) {
        LineAndPointFormatter formatter = new LineAndPointFormatter(Color.rgb(0
                , 0, 0), null,
                HistoricalCo2PlotActivity.getFormatterColorForRedrawer()[i], null);

        return formatter;
    }

    /*protected int[] getXMLResourcesAsIntArray() {
        int res[] = {
                R.xml.co_line_point_formatter_with_labels,
                R.xml.co2_line_point_formatter_with_labels,
                R.xml.eth_line_point_formatter_with_labels,
                R.xml.nh4_line_point_formatter_with_labels,
                R.xml.tol_line_point_formatter_with_labels,
                R.xml.ace_line_point_formatter_with_labels};
        return res;
    }*/

    @Override
    public void onResume() {
        updateThread = new Thread(updater);
        updateThread.start();
        super.onResume();
        redrawer.start();
    }

    @Override
    public void onPause() {
        updater.stopThread();
        global.isAnotherActivityVisible(false);
        redrawer.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        redrawer.finish();
        updater.stopThread();
        super.onDestroy();
    }

    protected class Updater implements Runnable {
        private boolean keepRunning;

        @Override
        public void run() {
            keepRunning = true;
            while (keepRunning) {
                try {
                    Thread.sleep(100);

                    GlobalVariable global = null;
                    global = global.getInstance();
                    double [] values = GasComputer.getGasesPPM();

                    // get rid the oldest sample in history:
                    if (serie[0].size() > 100)
                        for (int i = 0; i < serie.length; i++) serie[i].removeFirst();

                    // add the latest history sample:
                    for (int i = 0; i < serie.length; i++) {
                        serie[i].addLast(null, values[i]);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopThread() {
            keepRunning = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)finish();
        return super.onOptionsItemSelected(item);
    }
}