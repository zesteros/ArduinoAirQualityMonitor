package angelo.itl.arduinoairqualitymonitor.activity.plot;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.XYPlot;

import angelo.itl.arduinoairqualitymonitor.R;

public class CarbonDioxideRealTimePlotActivity extends RemainingGasesRealTimePlotActivity {
    PanZoom panZoom;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_plot_co2);
        setContentView(R.layout.activity_co2_real_time_plot);
        plot = (XYPlot) findViewById(R.id.co2_plot);
        global = global.getInstance();
        global.isAnotherActivityVisible(true);
        instantiatePlotSeries();
        setBoundaries(0, 5000, 0, 100, BoundaryMode.FIXED, BoundaryMode.FIXED);
        plot.addSeries(serie[1], configureFormatter(5));
        //XYPlot plot, int domainStepValue, int rangeStepValue, int,
        //ticksPerRangeLabel, int dpPosititionHeight, int dpWeight, int spTextSize
        configurePlot(plot, 10, 20, 10, 50, 10, 15);
        updater = new Updater();
        redrawer = new Redrawer(plot, 100, false);
        //plot.setbound
        panZoom = PanZoom.attach(plot, PanZoom.Pan.BOTH,PanZoom.Zoom.SCALE/*, 0, 100, 0, 5000*/);
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
