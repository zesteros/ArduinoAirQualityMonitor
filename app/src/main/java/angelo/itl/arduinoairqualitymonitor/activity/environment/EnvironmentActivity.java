package angelo.itl.arduinoairqualitymonitor.activity.environment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.ListView;
import android.widget.ProgressBar;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.adapter.main.CustomListAdapterSimple;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;

public class EnvironmentActivity extends AppCompatActivity {

    private ProgressBar altimeter;
    private ListView altimeterPropList;
    private String[] title;
    private String[] values;
    private CustomListAdapterSimple adapter;
    private GlobalVariable global;
    private static SharedPreferences prefs;

    private String tempUnit, pressureUnit, distanceUnit;

    private enum ConvertTo {NONE, FAHRENHEIT, PSI, PA, MILES}

    private ConvertTo optionTemperature, optionPressure, optionDistance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviroment);
        getSupportActionBar().setTitle(R.string.title_enviroment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        altimeter = (ProgressBar) findViewById(R.id.altimeter_progressbar);
        altimeterPropList = (ListView) findViewById(R.id.altimeter_list_view);

        title = getResources().getStringArray(R.array.titles_altimeter);

        getPreferences();
        getBarometerData();
    }

    public void getPreferences() {
        tempUnit = prefs.getString("temp_unity", "°C");
        pressureUnit = prefs.getString("pressure_unity", "hPA");
        distanceUnit = prefs.getString("length_unity", "mt");

        if (tempUnit.equals("°F"))
            optionTemperature = ConvertTo.FAHRENHEIT;
        else
            optionTemperature = ConvertTo.NONE;

        if (pressureUnit.equals("PA"))
            optionPressure = ConvertTo.PA;
        else if (pressureUnit.equals("PSI"))
            optionPressure = ConvertTo.PSI;
        else
            optionPressure = ConvertTo.NONE;

        if (distanceUnit.equals("miles"))
            optionDistance = ConvertTo.MILES;
        else
            optionDistance = ConvertTo.NONE;
    }

    public String[] getDataAsStringArray(GlobalVariable global) {
        String[] values = {
                global.getHumidity() + "",
                global.getTemperature() + "",
                global.getPressure() + "",
                global.getAltitude() + "",
                global.getSeaLevel() + ""
        };
        return values;
    }

    public String convertData(ConvertTo convertTo, String value) {
        switch (convertTo) {
            case FAHRENHEIT:
                return (Float.parseFloat(value) * 1.8) + 32 + " °F";
            case PSI:
                return Float.parseFloat(value) / 6894.75 + " PSI";
            case PA:
                return Float.parseFloat(value) + " PA";
            case MILES:
                return Float.parseFloat(value) * 0.00062137 + " MI";
            case NONE:
                return value;
        }
        return null;
    }

    void getFinalData() {
        values[0] += " " + getString(R.string.porcent);
        if (optionTemperature == ConvertTo.FAHRENHEIT)
            values[1] = convertData(ConvertTo.FAHRENHEIT, values[1]);
        else if (optionTemperature == ConvertTo.NONE)
            values[1] += " °C";

        if (optionPressure == ConvertTo.PSI) {
            values[2] = convertData(ConvertTo.PSI, values[2]);
            values[4] = convertData(ConvertTo.PSI, values[4]);
        } else if (optionPressure == ConvertTo.PA) {
            values[2] = convertData(ConvertTo.PA, values[2]);
            values[4] = convertData(ConvertTo.PA, values[4]);
        } else if (optionPressure == ConvertTo.NONE) {
            float hpa1 = Float.parseFloat(values[2]) / 100;
            values[2] = hpa1 + " hPa";
            float hpa2 = Float.parseFloat(values[4]) / 100;
            values[4] = hpa2 + " hPa";
        }
        if (optionDistance == ConvertTo.MILES)
            values[3] = convertData(ConvertTo.MILES, values[3]);
        else if (optionDistance == ConvertTo.NONE)
            values[3] += " mt";
    }

    private void getBarometerData() {
        try {
            global = global.getInstance();
            values = getDataAsStringArray(global);

            getFinalData();

            adapter = new CustomListAdapterSimple(this, title, values);
            altimeterPropList.setAdapter(adapter);

            //if 2000 = 100% how much is 1815
            float humidity = 0;
            float temperature = 0;
            try {
                humidity = ((float) global.getHumidity() * 100) / 50;
                temperature = ((float) global.getTemperature() * 100) / 50;
                final float finalTemp = temperature;
                final float finalHum = humidity;
                altimeter.setProgress((int) finalTemp);
                altimeter.setSecondaryProgress((int) finalHum);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_altimeter, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_alt) getBarometerData();
        else if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
