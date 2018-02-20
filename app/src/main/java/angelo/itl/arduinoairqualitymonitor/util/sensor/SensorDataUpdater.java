package angelo.itl.arduinoairqualitymonitor.util.sensor;

import android.content.Context;

import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;

/**
 * Created by Angelo on 15/12/2016.
 */

public class SensorDataUpdater {

    private StringBuilder message;
    private GlobalVariable global;
    private Context context;
    private GasAlarm gasAlarm;

    public SensorDataUpdater() {
        message = new StringBuilder();
        this.context = context;
        //gasAlarm = new GasAlarm(context);
    }


    public void debugMessage(String data) {
        message.append(data);
        int endOfLineIndex = message.indexOf("~");// determine the end-of-line

        if (endOfLineIndex > 0) { // make sure there data before ~
            String dataInPrint = message.substring(0, endOfLineIndex);// extract string

            if (message.charAt(0) == '#') {//if it starts with # we know it is what we are looking for
                int[] endOfValues = new int[7];//determine when a read value is finish
                int j = 0;
                for (int i = 1; i < dataInPrint.length(); i++) {
                    if (dataInPrint.charAt(i) == '+') {//if find a + save into an array that position
                        try {
                            endOfValues[j] = i;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            return;
                        }
                        j++;
                    }
                }
                try {
                    setData(endOfValues);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    return;
                }
            }
            message.delete(0, message.length());//clear all string data
        }
    }

    public void setData(int endOfValues[]) {
        global = global.getInstance();
        try {
            global.setHumidity(
                    Double.parseDouble(message.substring(1, endOfValues[0])));
            global.setTemperature(
                    Double.parseDouble(message.substring(endOfValues[0] + 1, endOfValues[1] - 1)));
            global.setPressure(
                    Double.parseDouble(message.substring(endOfValues[1] + 1, endOfValues[2] - 1)));
            global.setAltitude(
                    Double.parseDouble(message.substring(endOfValues[2] + 1, endOfValues[3] - 1)));
            global.setSeaLevel(
                    Double.parseDouble(message.substring(endOfValues[3] + 1, endOfValues[4] - 1)));
            global.setMQ135AnalogRead(
                    Double.parseDouble(message.substring(endOfValues[4] + 1, endOfValues[5] - 1)));
            global.setIsSmoke(
                    message.substring(endOfValues[5] + 1, endOfValues[6] - 1));
        /*global.setCo(message.substring(endOfValues[6] + 1, endOfValues[7] - 1));
        global.setCo2(message.substring(endOfValues[7] + 1, endOfValues[8] - 1));
        global.setEthanol(message.substring(endOfValues[8] + 1, endOfValues[9] - 1));
        global.setNh4(message.substring(endOfValues[9] + 1, endOfValues[10] - 1));
        global.setToluene(message.substring(endOfValues[10] + 1, endOfValues[11] - 1));
        global.setAcetone(message.substring(endOfValues[11] + 1, endOfValues[12] - 1));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
