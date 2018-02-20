/********************************************************************************
*	Copyright 2016 Angelo de Jesus Loza Martinez								*
*	Licensed under the Apache License, Version 2.0 (the "License");				*
*	you may not use this file except in compliance with the License.			*
*	You may obtain a copy of the License at										*
*																				*
*   http://www.apache.org/licenses/LICENSE-2.0									*
*																				*
*	Unless required by applicable law or agreed to in writing, software			*
*	distributed under the License is distributed on an "AS IS" BASIS,			*
*	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.	*
*	See the License for the specific language governing permissions and			*
*	limitations under the License.												*
*																				*
*********************************************************************************/

package angelo.itl.arduinoairqualitymonitor.util;


import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Button;

import java.net.Socket;

public class GlobalVariable {

    private static GlobalVariable instance;

    public double humidity;
    public double pressure;
    public double mq135voltage;
    public double temperature;
    public double seaLevel;
    public double altitude;
    public String isSmoke;
    public boolean btConnected;
    public BluetoothSocket btSocket;
    public String address;
    private boolean dbIsEmpty;
    private Context context;
    private int timeout;
    private boolean anotherActivityVisible;
    private Button sConnectButton;
    private boolean bluetooth;
    private Socket socket;

    public static synchronized GlobalVariable getInstance() {
        if (instance == null) instance = new GlobalVariable();
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        return btConnected;
    }

    public void isConnected(boolean connected) {
        this.btConnected = connected;
    }

    public String getIsSmoke() {
        return isSmoke;
    }

    public void setIsSmoke(String isSmoke) {
        this.isSmoke = isSmoke;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getMQ135AnalogRead() {
        return mq135voltage;
    }

    public void setMQ135AnalogRead(double mq135voltage) {
        this.mq135voltage = mq135voltage;
    }

    public double getSeaLevel() {
        return seaLevel;
    }

    public void setSeaLevel(double seaLevel) {
        this.seaLevel = seaLevel;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }

    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isEmptyDatabase() {
        return dbIsEmpty;
    }

    public void isEmptyDatabase(boolean dbIsEmpty) {
        this.dbIsEmpty = dbIsEmpty;
    }
    /*public String[] getAllGasesAsStringArray() {
        String[] values = {
                getCo(),
                getCo2(),
                getEthanol(),
                getNh4(),
                getToluene(),
                getAcetone()};
        return values;
    }*/

    /*public float[] getAllGasesAsFloatArray() {
        float values[] = new float[getAllGasesAsStringArray().length];
        try {
            for (int i = 0; i < getAllGasesAsStringArray().length; i++)
                values[i] = Float.parseFloat(getAllGasesAsStringArray()[i]);
        }catch(Exception e){}
        return values;
    }*/

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isAnotherActivityVisible() {
        return anotherActivityVisible;
    }

    public void isAnotherActivityVisible(boolean anotherActivityVisible) {
        this.anotherActivityVisible = anotherActivityVisible;
    }

    public Button getConnectButton() {
        return sConnectButton;
    }

    public void setConnectButton(Button sConnectButton) {
        this.sConnectButton = sConnectButton;
    }

    public boolean isBluetooth() {
        return bluetooth;
    }

    public void isBluetooth(boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
