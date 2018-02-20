package angelo.itl.arduinoairqualitymonitor.bluetooth;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.support.design.widget.Snackbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import angelo.itl.arduinoairqualitymonitor.*;
import angelo.itl.arduinoairqualitymonitor.activity.main.MainActivity;

/**
 * Class to instantiate available device dialog list to connect
 */
public class DeviceDialog extends MainActivity {
    private Context context;

    /**
     * @param context of the main activity
     */
    public DeviceDialog(Context context) {
        this.context = context;
    }

    /**
     * To instantiate and create device list dialog
     */
    public void showDeviceListDialog() {
        devicesArrayAdapter = createArrayAdapter(context, R.layout.device_name);
        //discoverArrayAdapter = createArrayAdapter(context, R.layout.device_name);
        dialogBuilder = new AlertDialog.Builder(context);//instantiate builder
        dialogView = inflateCustomView((Activity) context, R.layout.dialog_paired_devices);
        dialogBuilder.setView(dialogView);
        //instantiate and link components from class to custom view
        instantiateItems();
        //set custom views
        pairedDevices.setAdapter(devicesArrayAdapter);
        //availableDevices.setAdapter(discoverArrayAdapter);
        //enable again bt adapter
        setBluetoothAdapter();
        // Get a set of currently paired devices and append to 'pairedDevices'
        setBtPairedDevices = btAdapter.getBondedDevices();
        //instantiate a broadcast reciever to check bt discovering devices
        //btDevicesReceiver = new BluetoothCastReceiver();
        pairedDevicesReceiver = new BluetoothPairCastReceiver();
        instantiateFilter(pairedDevicesReceiver);
        buildAvailablePairedDevicesList();
        btDevicesDialog = dialogBuilder.create();
        btDevicesDialog.show();
        //add lists items click liste
        registerListeners();
        btDevicesDialog.setCanceledOnTouchOutside(false);//make not cancel when press outside
    }

    public void buildAvailablePairedDevicesList() {
        try {
            try {
                //Log.d("RECEIVER", btDevicesReceiver+"");
                //context.registerReceiver(btDevicesReceiver, availableDevicesFilter);//register listeners and the filters
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                btAdapter.startDiscovery();//start bt devices discovery
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (setBtPairedDevices.size() > 0) { // Add previosuly paired devices to the array
                for (BluetoothDevice device : setBtPairedDevices)
                    devicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            } else {//if isnt devices available show only one item
                hideBluetoothConnectionUI();
                devicesArrayAdapter.add(context.getString(R.string.dialog_devices_not_found));
            }
        } catch (Exception e) {e.printStackTrace();
        }
    }

    public void instantiateFilter(BroadcastReceiver mPairReceiver) {
        pairedDevicesFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mPairReceiver, pairedDevicesFilter);

        availableDevicesFilter = new IntentFilter();//instatiate a availableDevicesFilter for listen bt changes
        availableDevicesFilter.addAction(BluetoothDevice.ACTION_FOUND);//add the actions
        availableDevicesFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        availableDevicesFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public void registerListeners() {
        pairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                deviceInfo = ((TextView) v).getText().toString();
                if (!deviceInfo.equals(context.getString(R.string.dialog_devices_not_found))) {//if arent empty paired dev list
                    btDevicesDialog.dismiss();
                    connectToDevice(deviceInfo.substring(deviceInfo.length() - 17), btAdapter);
                    //context.unregisterReceiver(btDevicesReceiver);
                    context.unregisterReceiver(pairedDevicesReceiver);
                }
            }
        });

        /*availableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                deviceInfo = ((TextView) v).getText().toString();
                if (!deviceInfo.equals(context.getString(R.string.dialog_discovery_devices_not_found))) {
                    btDevice = btAdapter.getRemoteDevice(deviceInfo
                            .substring(deviceInfo.length() - 17));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Method method = btDevice.getClass().getMethod("createBond"
                                        , (Class[]) null);
                                method.invoke(btDevice, (Object[]) null);
                                btDevicesDialog.dismiss();
                                global.setAddress(deviceInfo.substring(deviceInfo.length() - 17));
                                Log.d("adress", global.getAddress());
                                context.unregisterReceiver(btDevicesReceiver);
                                context.unregisterReceiver(pairedDevicesReceiver);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });*/
        btDevicesDialog.setOnKeyListener(new Dialog.OnKeyListener() {//handle back button
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideBluetoothConnectionUI();
                    btDevicesDialog.dismiss();
                    context.unregisterReceiver(pairedDevicesReceiver);
                    //context.unregisterReceiver(btDevicesReceiver);
                }
                return true;
            }
        });
    }

    public ArrayAdapter<String> createArrayAdapter(Context context, int res) {
        return new ArrayAdapter<String>(context, res);
    }

    public View inflateCustomView(Activity activity, int layoutid) {
        LayoutInflater inflater = activity.getLayoutInflater();//get inflater for custom
        return inflater.inflate(layoutid, null);//inflate view;
    }

    public void instantiateItems() {
        //connectionSwitch = (Switch) ((Activity) context).findViewById(R.id.bluetooth_switch);
        connectionTextView = (TextView) ((Activity) context).findViewById(R.id.bluetooth_tv);
        pairedDevices = (ListView) dialogView.findViewById(R.id.paired_devices);
        //availableDevices = (ListView) dialogView.findViewById(R.id.available_devices);
        //discoveringProgressBar = (ProgressBar) dialogView.findViewById(R.id.discover_progressbar);
        //discoveringProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private class BluetoothCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("HI","broadcast true");
            String action = intent.getAction();//get actual action
            Log.d("ACTION",action);
            //switch action to execute instructions
            /*If the action of discover devices started, so show searching progress bar*/
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                discoveringProgressBar.setVisibility(ProgressBar.VISIBLE);
            /*Else if the discover action finished show message that doesn't find devices*/
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoverArrayAdapter.isEmpty())
                    discoverArrayAdapter.add(context.getString(R.string.dialog_discovery_devices_not_found));
                discoveringProgressBar.setVisibility(ProgressBar.INVISIBLE);
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceInfo = btDevice.getName() + "\n" + btDevice.getAddress();
                Log.d("DEVICE FOUND", deviceInfo);
                if (discoverArrayAdapter.getPosition(deviceInfo) < 0){
                    Log.d("DEVICE FOUND", deviceInfo);
                    discoverArrayAdapter.add(deviceInfo);}
            }
        }
    }

    private class BluetoothPairCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra
                        (BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                int prevState = intent.getIntExtra
                        (BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                    connectToDevice(global.getAddress(), btAdapter);
                else hideBluetoothConnectionUI();
            }
        }
    }
    @Override
    public void showToast(int id) {
        View view = ((MainActivity)context).findViewById(android.R.id.content);
        /*Show the snackbar*/
        Snackbar.make(view, context.getString(id), Snackbar.LENGTH_LONG).show();
    }

}
