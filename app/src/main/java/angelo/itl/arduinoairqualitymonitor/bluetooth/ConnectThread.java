package angelo.itl.arduinoairqualitymonitor.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;

public class ConnectThread extends Thread {

    public BluetoothSocket btSocket = null;
    private BluetoothAdapter btAdapter = null;
    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//standart uuid
    private BluetoothConnectionThread mConnectedThread;
    private GlobalVariable btData;
    private MainActivity main;


    public ConnectThread(BluetoothAdapter adapter, MainActivity main) {
        this.main = main;
        btData = btData.getInstance();
        this.btAdapter = adapter;
    }

    public void run() {
        //get btDevice
        BluetoothDevice device = btAdapter.getRemoteDevice(btData.getAddress());
        try {
            btSocket = createBluetoothSocket(device);//create socket
        } catch (IOException e) {
            Log.d("THREAD RUN","cant create socket");
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();//connect socket
            Log.e("SOCKET",btSocket+"");
        } catch (IOException e) {
            try {
                btSocket.close();//close socket
            } catch (IOException e2) {
                e.printStackTrace();
            }
        }
        if(btSocket.isConnected()){
            btData.setBtSocket(btSocket);
            btData.isConnected(true);
        } else btData.isConnected(false);
    }

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT btDevice using UUID
    }
    public void startConnectedThread(){
        //connectedTask = new ConnectedTask(btSocket, bluetoothIn, main);
        //connectedTask.execute();
        mConnectedThread = new BluetoothConnectionThread(btSocket, main);//call to connected thread
        mConnectedThread.start();
    }

    public void closeConnection(){
        try {
            if(btSocket != null)btSocket.close();
            btSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
