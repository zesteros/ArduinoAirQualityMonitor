package angelo.itl.arduinoairqualitymonitor.activity.plot;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.TextOrientation;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;

import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;

/**
 * Created by Angelo on 12/11/2016.
 */
public class HistoricalCo2PlotActivity extends AppCompatActivity {
    protected View view;
    private static XYPlot plot;
    private Number[][] arrayOfArraysOfValues;
    private static XYSeries[] series;
    private LineAndPointFormatter[] formatter;
    private DataBaseController dbController;
    private String[][] matrix;
    private Number[] co2Values;
    private Number[] dateValues;
    private SimpleXYSeries co2Serie;
    private LineAndPointFormatter co2Format;
    private String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private PointF minXY;
    private PointF maxXY;
    private Number minX;
    private Number maxX;
    private Number minY;
    private Number maxY;
    private PanZoom panZoom;
    //date:0, co:1, co2:2, etha:3, nh4:4, tol:5, ace:6
    //        co:0, etha:1, nh4:2, tol:3, ace:4
    protected byte[] orderByAlphaDrawColumns = {1, 4, 2, 3, 0};
    private GlobalVariable globalVariable;

    /**
     * Instantiate every array
     */
    public HistoricalCo2PlotActivity() {
        setArrayOfArraysOfValues(new Number[5][]);//create an array of arrays(all gases arrays)
        setSeries(new XYSeries[5]);
        setFormatter(new LineAndPointFormatter[5]);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historical_co2_plot);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.drawer_items)[8]);
        setPlot((XYPlot) findViewById(R.id.history_c02_plot));
        globalVariable = globalVariable.getInstance();
        globalVariable.isAnotherActivityVisible(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        //setMatrix(createArrayFromDatabase());//create all database array
        setDbController(new DataBaseController(this));
        if (!checkLength()) finish();
        getDbController().open();
        setCo2Values(getSerie(getDbController().getCo2AverageByDate(), false, 0));
        setDateValues(getSerie(getDbController().getTimestampsByDate(), true, 0));
        getDbController().close();
        setCo2Serie(new SimpleXYSeries(
                Arrays.asList(getDateValues()),
                Arrays.asList(getCo2Values()),
                getResources().getStringArray(R.array.gases)[1]));
        setCo2Format(configureFormatter(5));
        getPlot().addSeries(getCo2Serie(), getCo2Format());
        /**
         * @param plot               the plot to configure
         * @param domainStepValue    how much steps must move the plot in domain(x)
         * @param rangeStepValue     how much steps must move the plot in range(x)
         * @param ticksPerRangeLabel the ticks (lines) between one by one of range
         * @param dpPosititionHeight height of the series labels
         * @param dpWeight           weight of the labels
         * @param spTextSize         size of the text in labels
         */
        configurePlot(getPlot(), 8, 20, 10, 60, 310, 10, new DecimalFormat("#"),
                new Format() {
                    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM");

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
        /*getPlot().getGraphWidget().setPadding(
                getResources().getDimension(R.dimen.plot_dimen),
                0,
                0,
                getResources().getDimension(R.dimen.plot_dimen));*/
        getPlot().setUserDomainOrigin(0);
        getPlot().setUserRangeOrigin(0);
        getPlot().setBorderStyle(Plot.BorderStyle.NONE, null, null);
        getPlot().redraw();
        getPlot().calculateMinMaxVals();
        /*setMinXY(new PointF(
                getPlot().getCalculatedMinX().floatValue(),
                getPlot().getCalculatedMinY().floatValue()
        ));
        setMaxXY(new PointF(
                getPlot().getCalculatedMaxX().floatValue(),
                getPlot().getCalculatedMaxY().floatValue()
        ));
        getPlot().calculateMinMaxVals();

        setMinX(getPlot().getCalculatedMinX());
        setMaxX(getPlot().getCalculatedMaxX());
        setMinY(getPlot().getCalculatedMinY());
        setMaxY(getPlot().getCalculatedMaxY());
        */
        // setMinXY(new PointF(getMinX().floatValue(), getMinY().floatValue()));
        //setMaxXY(new PointF(getMaxX().floatValue(), getMaxY().floatValue()));
        // enable pan/zoom behavior:
        //getPlot().getOuterLimits().set(0, 5000, getPlot().get, getPlot().getca);
        getPlot().getRegistry().setEstimator(new ZoomEstimator());
        setPanZoom(PanZoom.attach(getPlot(), PanZoom.Pan.BOTH,PanZoom.Zoom.SCALE/*, getMinX(), getMaxX(), getMinY(), getMaxY()*/));
    }

    protected boolean checkLength() {
        getDbController().open();
        if (getSerie(dbController.getTimestampsByDate(), false, 0).length == 1) {
            Toast.makeText(
                    this,
                    R.string.historical_data_length_error,
                    Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        getDbController().close();
        return true;
    }

    /**
     * Generate all gases arrayOfArraysOfValues in arrays
     */
    protected Number[] generateSeriesArrays(int column, boolean isDate) {
        Number[] serie = new Number[getMatrix().length - 1];//create gas values array
        int k = 0;//counter for gas vales array
        /*If the wanted series is a number only (ppm)*/
        if (!isDate)
            for (int j = 1; j < getMatrix().length; j++) {
                if (getMatrix()[j][column].equals("null PPM")) j++;
                serie[k] = Double.parseDouble(getMatrix()[j][column].replace(" PPM", ""));
                k++;
            }
        /*If the series is date of register*/
        else {
            for (int j = 1; j < getMatrix().length; j++) {
                /*Get the date in normal format*/
                String someDate = getMatrix()[j][column];
                /*Create a new date format*/
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                Date date = null;//create a timestamp null
                try {
                    date = sdf.parse(someDate);//parse date to timestamp
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                serie[k] = date.getTime();//get timestamp
                k++;
            }
        }
        return serie;
    }


    public Number[] getSerie(Cursor cursor, boolean date, int column) {
        Number[] serie = new Number[cursor.getCount()];
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (date) serie[i] = cursor.getLong(0);
            else serie[i] = cursor.getFloat(column);
            cursor.moveToNext();
        }
        return serie;
    }

    public static int[] getFormatterColor() {
        int[] formatters = {
                Color.argb(20, 255, 128, 255),//ethanol-tol
                Color.argb(80, 128, 255, 255),//acetone-tol
                Color.argb(120, 128, 255, 128),//nh4
                Color.argb(150, 255, 255, 128),//toluene (yellow)-
                Color.argb(190, 255, 128, 128),//co color (like pink)-co2
                Color.argb(200, 50, 150, 255)//co2-co
        };
        return formatters;
    }

    public static int[] getFormatterColorForRedrawer() {
        int[] formatters = {
                Color.argb(255, 255, 0, 0),//el mas fuerte co2
                Color.argb(50, 255, 128, 128),//co color (like pink)-co2 el mas debil
                Color.argb(80, 255, 255, 128),//toluene (yellow)-ethanol el menos debil
                Color.argb(95, 128, 255, 128),//nh4
                Color.argb(140, 255, 128, 255),//ethanol-tol
                Color.argb(110, 128, 255, 255)//acetone-tol

        };
        return formatters;
    }

    /**
     * @return the formatter for the plot
     */
    protected LineAndPointFormatter configureFormatter(int i) {
        LineAndPointFormatter formatter = new LineAndPointFormatter(Color.rgb(0
                , 0, 0), null,
                getFormatterColor()[i], null);
        return formatter;
    }

    public void setBoundaries(int y1, int y2, int x1, int x2, BoundaryMode rangeBoundaryMode,
                              BoundaryMode domainBoundaryMode) {
        getPlot().setRangeBoundaries(y1, y2, rangeBoundaryMode);
        getPlot().setDomainBoundaries(x1, x2, domainBoundaryMode);
    }


    /**
     * @param plot               the plot to configure
     * @param domainStepValue    how much steps must move the plot in domain(x)
     * @param rangeStepValue     how much steps must move the plot in range(x)
     * @param ticksPerRangeLabel the ticks (lines) between one by one of range
     * @param dpPosititionHeight height of the series labels
     * @param dpWeight           weight of the labels
     * @param spTextSize         size of the text in labels
     * @param rangeValueFormat   type of format of range (number/date/etc)
     * @param domainValueFormat  type of format of domain (number/date/etc)
     */
    public void configurePlot(XYPlot plot, int domainStepValue, int rangeStepValue, int
            ticksPerRangeLabel, int dpPosititionHeight, int dpWeight, int spTextSize,
                              Format rangeValueFormat, Format domainValueFormat) {

        plot.setDomainStepMode(StepMode.SUBDIVIDE);
        plot.setRangeStepMode(StepMode.SUBDIVIDE);
        plot.setDomainStepValue(domainStepValue);

        //plot.setDrawRangeOriginEnabled(true);
        plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        plot.setRangeStepValue(rangeStepValue);
        plot.setLinesPerRangeLabel(ticksPerRangeLabel);
        plot.setDomainLabel(getString(R.string.plot_domain_label));
        //plot.getDomainLabelWidget().pack();
        plot.setRangeLabel(getString(R.string.ppm));
        //plot.getRangeLabelWidget().pack();
        plot.getDomainTitle().setOrientation(TextOrientation.VERTICAL_ASCENDING);
        //plot.getGraph().setDomainLabelOrientation(-60);***
        //plot.getGraphWidget().getDomainTickLabelPaint().setTextAlign(Paint.Align.CENTER);
        //plot.getLegendWidget().setSize(new Size(PixelUtils.dpToPix(dpPosititionHeight),
        //      SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(dpWeight), SizeLayoutType.ABSOLUTE));
        plot.getLegend().setSize(new Size(PixelUtils.dpToPix(dpPosititionHeight),
                SizeMode.ABSOLUTE, PixelUtils.dpToPix(dpWeight), SizeMode.ABSOLUTE));
        //plot.getLegendWidget().getTextPaint().setTextSize(PixelUtils.spToPix(spTextSize));
        plot.getLegend().getTextPaint().setTextSize(PixelUtils.spToPix(spTextSize));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.RIGHT).setFormat(rangeValueFormat);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(domainValueFormat);
        //plot.setRangeValueFormat(rangeValueFormat);
        //plot.setDomainValueFormat(domainValueFormat);
    }

    @Override
    public void onBackPressed() {
        finish();
        globalVariable.isAnotherActivityVisible(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public Number[] getCo2Values() {
        return co2Values;
    }

    public void setCo2Values(Number[] co2Values) {
        this.co2Values = co2Values;
    }

    public static XYPlot getPlot() {
        return plot;
    }

    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    public Number[][] getArrayOfArraysOfValues() {
        return arrayOfArraysOfValues;
    }

    public void setArrayOfArraysOfValues(Number[][] arrayOfArraysOfValues) {
        this.arrayOfArraysOfValues = arrayOfArraysOfValues;
    }

    public static XYSeries[] getSeries() {
        return series;
    }

    public void setSeries(XYSeries[] series) {
        this.series = series;
    }

    public LineAndPointFormatter[] getFormatter() {
        return formatter;
    }

    public void setFormatter(LineAndPointFormatter[] formatter) {
        this.formatter = formatter;
    }

    public DataBaseController getDbController() {
        return dbController;
    }

    public void setDbController(DataBaseController dbController) {
        this.dbController = dbController;
    }

    public String[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(String[][] matrix) {
        this.matrix = matrix;
    }

    public Number[] getDateValues() {
        return dateValues;
    }

    public void setDateValues(Number[] dateValues) {
        this.dateValues = dateValues;
    }

    public SimpleXYSeries getCo2Serie() {
        return co2Serie;
    }

    public void setCo2Serie(SimpleXYSeries co2Serie) {
        this.co2Serie = co2Serie;
    }

    public LineAndPointFormatter getCo2Format() {
        return co2Format;
    }

    public void setCo2Format(LineAndPointFormatter co2Format) {
        this.co2Format = co2Format;
    }

    public PointF getMinXY() {
        return minXY;
    }

    public void setMinXY(PointF minXY) {
        this.minXY = minXY;
    }

    public PointF getMaxXY() {
        return maxXY;
    }

    public void setMaxXY(PointF maxXY) {
        this.maxXY = maxXY;
    }

    public Number getMinX() {
        return minX;
    }

    public void setMinX(Number minX) {
        this.minX = minX;
    }

    public Number getMaxX() {
        return maxX;
    }

    public void setMaxX(Number maxX) {
        this.maxX = maxX;
    }

    public Number getMinY() {
        return minY;
    }

    public void setMinY(Number minY) {
        this.minY = minY;
    }

    public Number getMaxY() {
        return maxY;
    }

    public void setMaxY(Number maxY) {
        this.maxY = maxY;
    }

    public PanZoom getPanZoom() {
        return panZoom;
    }

    public void setPanZoom(PanZoom panZoom) {
        this.panZoom = panZoom;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
