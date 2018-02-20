package angelo.itl.arduinoairqualitymonitor.activity.plot;

import android.app.ProgressDialog;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.Plot;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.R;

/**
 * Created by Angelo on 13/11/2016.
 */
public class HistoricalAllGasPlotActivity extends HistoricalCo2PlotActivity {
    private List<? extends Number> list;
    GetDataThread thread;

    private Number minX;
    private Number maxX;
    private Number minY;
    private Number maxY;
    private PointF minXY;
    private PointF maxXY;
    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_remaining_gases_plot);
        getSupportActionBar()
                .setTitle(getResources().getStringArray(R.array.drawer_items)[9]);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        setPlot((XYPlot) findViewById(R.id.history_all_gases_plot));
        globalVariable = globalVariable.getInstance();
        globalVariable.isAnotherActivityVisible(true);
        int k = 1;
        int n = 0;
        setDbController(new DataBaseController(HistoricalAllGasPlotActivity.this));
        if(!checkLength()) finish();
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog pDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(HistoricalAllGasPlotActivity.this);
                pDialog.setMessage(getString(R.string.dialog_loading_plot));
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                getDbController().open();
                for (int i = 0; i < getArrayOfArraysOfValues().length; i++) {
            /*Generate the array of arrays(for every column in database)*/
                    //k = (k == 2) ? 3 : k;//if k == 2 assign 3 else assign the same (k)
                    getArrayOfArraysOfValues()[i] = getSerie(getDbController().getGasesAverageByDate(),
                            false, orderByAlphaDrawColumns[i]);
                    //k++;
            /*Configure all arrayOfArraysOfValues*/
                    getSeries()[i] = new SimpleXYSeries(
                            Arrays.asList(getSerie(getDbController().getTimestampsByDate(), true, 0))
                            ,
                            Arrays.asList(getArrayOfArraysOfValues()[i]),
                            getResources().getStringArray(R.array.gases_by_alpha)[i]);
                }
                getDbController().close();
                for (int i = 0; i < getArrayOfArraysOfValues().length; i++) {
                    Log.d("GAS", getResources().getStringArray(R.array.gases)[i]);
            /*Configure formatter*/
                    getFormatter()[i] = configureFormatter(i);
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getPlot().addSeries(getSeries()[finalI], getFormatter()[finalI]);
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getPlot().setUserDomainOrigin(0);
                getPlot().setUserRangeOrigin(0);
                getPlot().setBorderStyle(Plot.BorderStyle.NONE, null, null);
                getPlot().calculateMinMaxVals();

                /*setMinX(getPlot().getgetCalculatedMinX());
                setMaxX(getPlot().getCalculatedMaxX());
                setMinY(getPlot().getCalculatedMinY());
                setMaxY(getPlot().getCalculatedMaxY());*/

                //setMinXY(new PointF(getMinX().floatValue(), getMinY().floatValue()));
                //setMaxXY(new PointF(getMaxX().floatValue(), getMaxY().floatValue()));
                // enable pan/zoom behavior:
                setPanZoom(PanZoom.attach(getPlot()/*, getMinX(), getMaxX(), getMinY(), getMaxY()*/));
                getPlot().redraw();
                pDialog.dismiss();
            }
        }.execute();
        configurePlot(getPlot(), 8, 20, 10, 60, 310, 8, new DecimalFormat("#"), new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

            @Override
            public StringBuffer format(Object obj, StringBuffer stringBuffer, FieldPosition fieldPosition) {
                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue();
                Date date = new Date(timestamp);

                return dateFormat.format(date, stringBuffer, fieldPosition);
            }

            @Override
            public Object parseObject(String s, ParsePosition parsePosition) {
                return null;
            }
        });

        //getPlot().setGridPadding(0, 0, 0, 0);
        //Log.d("DIMENSION", getResources().getDimension(R.dimen.plot_dimen) + "");
        /*getPlot().getGraphWidget().setPadding(
                getResources().getDimension(R.dimen.plot_dimen),
                0,
                0,
                getResources().getDimension(R.dimen.plot_dimen));*/
    }

    @Override
    public PointF getMinXY() {
        return minXY;
    }

    @Override
    public void setMinXY(PointF minXY) {
        this.minXY = minXY;
    }

    @Override
    public PointF getMaxXY() {
        return maxXY;
    }

    @Override
    public void setMaxXY(PointF maxXY) {
        this.maxXY = maxXY;
    }

    @Override
    public Number getMinX() {
        return minX;
    }

    @Override
    public void setMinX(Number minX) {
        this.minX = minX;
    }

    @Override
    public Number getMaxX() {
        return maxX;
    }

    @Override
    public void setMaxX(Number maxX) {
        this.maxX = maxX;
    }

    @Override
    public Number getMinY() {
        return minY;
    }

    @Override
    public void setMinY(Number minY) {
        this.minY = minY;
    }

    @Override
    public Number getMaxY() {
        return maxY;
    }

    @Override
    public void setMaxY(Number maxY) {
        this.maxY = maxY;
    }

    private class GetDataThread extends Thread {
        private List<? extends Number> list;

        @Override
        public void run() {
            this.list = Arrays.asList(generateSeriesArrays(0, true));
        }

        public List<? extends Number> getList() {
            return this.list;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
