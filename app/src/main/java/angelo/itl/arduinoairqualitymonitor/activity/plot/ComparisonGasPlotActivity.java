package angelo.itl.arduinoairqualitymonitor.activity.plot;

import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.util.PixelUtils;

import java.util.List;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.database.DataBaseController;

/**
 * Created by Angelo on 20/12/2016.
 */
public class ComparisonGasPlotActivity extends AppCompatActivity {
    private PieChart[] pies;
    private Segment[][] segments;
    private SegmentFormatter[][] segmentsFormatter;
    public static final int SELECTED_SEGMENT_OFFSET = 50;

    private enum GasName {
        CO2_AND_ALL,
        ALL_OTHER
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparisson_gas);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getStringArray(R.array.drawer_items)[10]);

        pies = new PieChart[getAverageFromDatabase().length];

        /*Se crea un arreglo de arreglos de segmentos para guardar el valor de cada
        * pay y sus segmentos (3 pays[i], diferentes segmentos[k])*/
        segments = new Segment[getAverageFromDatabase().length][];
        segmentsFormatter = new SegmentFormatter[getAverageFromDatabase().length][];
        /*Se asigna el padding para todos los pays*/
        float padding = PixelUtils.dpToPix(30);

        for (int i = 0; i < pies.length; i++) {
            pies[i] = (PieChart) findViewById(getPieIds()[i]);
            pies[i].getPie().setPadding(padding, padding, padding, padding);
            /*El segmento del pay [i] se le asigna el numero encontrado en la base de deatos
            * del pay[i] y su valor k*/
            segments[i] = new Segment[getAverageFromDatabase()[i].length];
            for (int k = 0; k < segments[i].length; k++)
                segments[i][k] = new Segment(getSegmentsName()[i][k],
                        getAverageFromDatabase()[i][k]);

            EmbossMaskFilter emf = new EmbossMaskFilter(
                    new float[]{1, 1, 1}, 0.9f, 3f, 30f);
            /*Se crea un formato para cada segmento (3 pays con n segmentos cada uno)*/
            segmentsFormatter[i] = new SegmentFormatter[getAverageFromDatabase()[i].length];
            for (int j = 0; j < segmentsFormatter[i].length; j++) {
                segmentsFormatter[i][j] = new SegmentFormatter(Color.argb(
                        255,
                        ((int) (Math.random() * 255) + 1),
                        ((int) (Math.random() * 255) + 1),
                        ((int) (Math.random() * 255) + 1))
                );
                segmentsFormatter[i][j].getLabelPaint().setShadowLayer(3, 0, 0, Color.BLACK);
                float textsize = PixelUtils.dpToPix(12);
                segmentsFormatter[i][j].getLabelPaint().setTextSize(textsize);
                segmentsFormatter[i][j].getFillPaint().setMaskFilter(emf);
                pies[i].addSegment(segments[i][j], segmentsFormatter[i][j]);
            }
            pies[i].getBorderPaint().setColor(Color.TRANSPARENT);
            pies[i].getBackgroundPaint().setColor(Color.TRANSPARENT);
            pies[i].getRenderer(PieRenderer.class).setDonutSize(0, PieRenderer.DonutMode.PERCENT);
            pies[i].redraw();
            final int finalI = i;
            pies[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    PointF click = new PointF(motionEvent.getX(), motionEvent.getY());
                    if(pies[finalI].getPie().containsPoint(click)) {
                        Segment segment = pies[finalI].getRenderer(PieRenderer.class).getContainingSegment(click);

                        deselectAll();
                        if(segment != null) {
                            final boolean isSelected = getFormatter(segment).getOffset() != 0;
                            setSelected(segment, !isSelected);
                            pies[finalI].redraw();
                        }
                    }
                    return false;
                }

                private SegmentFormatter getFormatter(Segment segment) {
                    return pies[finalI].getFormatter(segment, PieRenderer.class);
                }

                private void deselectAll() {
                    List<Segment> segments = pies[finalI].getRegistry().getSeriesList();
                    for(Segment segment : segments) {
                        setSelected(segment, false);
                    }
                }

                private void setSelected(Segment segment, boolean isSelected) {
                    SegmentFormatter f = getFormatter(segment);
                    if(isSelected) {
                        f.setOffset(SELECTED_SEGMENT_OFFSET);
                    } else {
                        f.setOffset(0);
                    }
                }
            });
        }
    }

    public int[] getPieIds() {
        return new int[]{
                //R.id.pie_plot_all_gases,
                R.id.pie_plot_co2_and_all_gases,
                R.id.pie_plot_minor_gases
        };
    }

    public String[][] getSegmentsName() {
        return new String[][]{
                getGasName(GasName.CO2_AND_ALL),
                getGasName(GasName.ALL_OTHER)
        };
    }

    public String[] getGasName(GasName name) {
        String[] nameRequired;
        if (name == GasName.CO2_AND_ALL)
            return new String[]{
                    getResources().getStringArray(R.array.columns)[2],
                    getString(R.string.all_other_gas)};
        else {
            nameRequired = new String[getResources().getStringArray(R.array.columns).length - 5];
            int k = 0;
            for (int i = 1; i < getResources().getStringArray(R.array.columns).length - 3; i++)
                if (i != 2) {
                    nameRequired[k] = getResources().getStringArray(R.array.columns)[i];
                    k++;
                }
            return nameRequired;
        }
    }


    public double[][] getAverageFromDatabase() {
        DataBaseController dbController = new DataBaseController(this);
        dbController.open();
        double[][] values = {
                //dbController.getAverage(DataBaseController.Average.ALL_GAS),
                dbController.getAverage(DataBaseController.Average.CO2_AVG_AND_ALL_GAS),
                dbController.getAverage(DataBaseController.Average.ONLY_MINOR_GASES)
        };
        dbController.close();
        return values;
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
