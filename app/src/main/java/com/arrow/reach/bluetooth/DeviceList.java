package com.arrow.reach.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arrow.reach.R;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceList extends Activity
{

    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceList";

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    ProgressBar progressBar;
    TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");

        // Setup the window
        setContentView(R.layout.device_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
        title = (TextView) findViewById(R.id.toolbar_title);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapter
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        doDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        progressBar.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.scanning));


        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering())
        {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /**
     * The on-click listener for all devices in the ListViews
     * Cancels discovery because the user has chosen a device to connect to
     * this will
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();


                String address = info.substring(info.length() - 17);

                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();


        }
    };

    /**
     * The Broadcast Receiver listens for available bluetooth devices to connect to
     * ACTION_FOUND is a state in which the device has found another device that is available
     * BOND_BONDED is a state in which describes whether or not the device found is already paired
     * ACTION_DISCOVERY_FINISHED is where the discovery period to find devices has ended
     *
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                progressBar.setVisibility(View.INVISIBLE);
                title.setText(getString(R.string.select_device));
                if (mNewDevicesArrayAdapter.getCount() == 0)
                {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
