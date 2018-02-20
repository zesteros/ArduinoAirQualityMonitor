package angelo.itl.arduinoairqualitymonitor.web;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

import angelo.itl.arduinoairqualitymonitor.R;
import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;
import angelo.itl.arduinoairqualitymonitor.util.sensor.GasAlarm;
import angelo.itl.arduinoairqualitymonitor.util.GlobalVariable;
import angelo.itl.arduinoairqualitymonitor.util.NotificationHelper;
import angelo.itl.arduinoairqualitymonitor.util.sensor.SensorDataUpdater;

/**
 * Created by Angelo on 14/12/2016.
 */

public class InternetConnectionThread extends Thread {
    private Context mContext;
    private String mIp;
    private int mPort;
    private Socket mSocket;
    private BufferedReader mInput;
    private GlobalVariable global;
    private MainActivity mMain;
    private SensorDataUpdater updater;
    private GasAlarm alarm;


    public InternetConnectionThread(Context context, String ip, int port) {
        this.mContext = context;
        this.mMain = (MainActivity) context;
        this.mIp = ip;
        this.mPort = port;
        this.global = global.getInstance();
        updater = new SensorDataUpdater();
        alarm = new GasAlarm(context);
    }

    public boolean testConnection() {
        try {
            mSocket = new Socket(mIp, mPort);
            mSocket.setSoTimeout(3000);
            global.setSocket(mSocket);
        } catch (ConnectException ce) {
            if(ce.toString().contains(mContext.getString(
                    R.string.network_unreachable_exception)
            ))notifyConnectionError(ConnectionStatus.INTERNET_DISCONNECTED);
            else if(ce.toString().contains(mContext.getString(
                    R.string.no_route_to_host_exception)
            ))notifyConnectionError(ConnectionStatus.NO_ROUTE_TO_HOST);
            ce.printStackTrace();
            Log.e("error",ce+"");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if (testConnection()) {
            showConnectedState();
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mMain.findViewById(android.R.id.content),
                            R.string.connection_to_server_success, Snackbar.LENGTH_LONG).show();
                }
            });
            try {
                while (true) {
                    try {
                        global.isConnected(true);
                        mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                        updater.debugMessage(mInput.readLine());
                        alarm.checkAlarm();
                    } catch (Exception e) {
                        mInput.close();
                        mSocket.close();
                        e.printStackTrace();
                        notifyConnectionError(ConnectionStatus.LOST);
                        break;
                    }
                }
            } catch (ConnectException ce) {
                ce.printStackTrace();
                notifyConnectionError(ConnectionStatus.IP_PORT_ERROR);
            } catch (Exception e) {
                notifyConnectionError(ConnectionStatus.LOST);
                e.printStackTrace();
            }
        }
    }

    public void notifyConnectionError(final ConnectionStatus status) {
        global.isConnected(false);
        mMain.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMain.hideBluetoothConnectionUI();
                mMain.notification.getNotificationManager().cancel(NotificationHelper.RECEIVING_DATA);
                switch (status){
                    case NO_ROUTE_TO_HOST:
                        mMain.showToast(R.string.no_route_to_host);
                        break;
                    case LOST:
                        mMain.showToast(R.string.conection_lost);
                        break;
                    case IP_PORT_ERROR:
                        mMain.showToast(R.string.ip_port_error);
                        break;
                    case INPUT_ERROR:
                        mMain.showToast(R.string.input_error);
                        break;
                    case INTERNET_DISCONNECTED:
                        mMain.showToast(R.string.internet_disconnected);
                }
            }
        });
    }

    public void showConnectedState() {
        mMain.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMain.showBluetoothConnectingUI();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mMain.showBluetoothConnectedUI();
            }
        });
    }
}
