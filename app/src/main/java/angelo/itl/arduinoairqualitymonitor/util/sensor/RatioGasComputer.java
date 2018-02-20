package angelo.itl.arduinoairqualitymonitor.util.sensor;

import android.content.Context;

import angelo.itl.arduinoairqualitymonitor.R;

/**
 * Created by Angelo on 06/12/2016.
 */

public class RatioGasComputer {
    private Context context;

    public RatioGasComputer(Context context) {
        this.context = context;
    }

    public String getGasRatioAsString(int ratio, float[] defaultValues) {
        //create a new limits array
        /*Dangerous values are normal x13 times*/
        String text = "";
        String[] gasNames = context.getResources().getStringArray(R.array.gases);
        //divide the ratio into 10 if ratio comes to 100 the value of ratio is 1
        float newNormalValue = 0;
        float newDangerValue = 0;
        for (int i = 0; i < defaultValues.length; i++) {
            newNormalValue = (float) (defaultValues[i] * ((double) ratio / 100));
            newDangerValue = (float) ((defaultValues[i] * 13) * ((double) ratio / 100));
            text += gasNames[i] + context.getString(R.string.normal_value) + newNormalValue +" -"+
                    context.getString(R.string.max_value) + newDangerValue + "\n";
        }
        text = text.substring(0, text.length() - 1);
        text += ((float)ratio/100f) != 1.0f?"\n\n"+((double)ratio/100f)+" "+context.getString(R.string.time_normal_values):
                "\n\n"+((double)ratio/100f)+" "+context.getString(R.string.time_normal_value);
        return text;
    }

    public double[][] getGasRatioAsFloatArray(int ratio, float[] defaultValues) {
        //create a new limits array
        double[][] computedValues = new double[2][];
        double[] normalValues = new double[defaultValues.length];
        double[] dangerValues = new double[defaultValues.length];
        //divide the ratio into 10 if ratio comes to 100 the value of ratio is 1
        for (int i = 0; i < defaultValues.length; i++){
            normalValues[i] = (defaultValues[i] * ((double) ratio / 100));
            dangerValues[i] = ((defaultValues[i] * 13) * ((double) ratio / 100));
        }
        computedValues[0] = normalValues;
        computedValues[1] = dangerValues;
        return computedValues;
    }
}
