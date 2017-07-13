package com.example.a85314.meshnetwork.Bluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import com.example.a85314.meshnetwork.NodeData.NodeData;
import com.example.a85314.meshnetwork.Notifications.Notifications;
import com.example.a85314.meshnetwork.Notifications.NotificationsPermission;
import com.example.a85314.meshnetwork.R;
import com.example.a85314.meshnetwork.Database.DatabaseContract;
import java.util.ArrayList;

//TODO: data may not have ; make sure to check for that

public class Bluetooth extends AppCompatActivity
{



    private NotificationManager notifications;
    public static final String MY_PREFS_NAME = "Notifications";
    SharedPreferences sharedpreferences;

    private ArrayList<TextView> TextViewRef;


    private static final String TAG = "Bluetooth";


    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private Button scan_button;
    private DatabaseContract dbHelper;
    private LinearLayout Nodes;


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter BAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    /*
    * TODO: find way to delete dynamic reference to nodes if nodes haven't sent
    * TODO: updates for information in a couple of pings
     */

    /**
     * Setting layout views, requesting permissions during runtime, initializing variables,
     * getting bluetooth adapter,
     * @param savedInstanceState
     */

    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        context = getApplicationContext();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

        /** initializing Database helper object **/
        dbHelper = new DatabaseContract(getApplicationContext(), "pulling sensor data", null, 1);


        /**initializing variables used in layout **/
        Nodes = (LinearLayout) findViewById(R.id.Nodes);
        scan_button = (Button) findViewById(R.id.scan_button);
        //TextViewRef = new ArrayList<TextView>();

        /** fetching local Bluetooth adapter **/
        BAdapter = BluetoothAdapter.getDefaultAdapter();


        /**checking if device is Bluetooth compatible **/
        if (BAdapter == null) {

            new AlertDialog.Builder(this)
                    .setTitle("Error: Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }


    }


    /**
     * Sets onClickListener for dynamically generated TextViews
     * Starts new activity that displays all sensor data, RSSI,
     * connected nodes and their corresponding LQI readings
     * @param textView
     * @param temp
     * @param motion
     * @param light
     * @param RSSI
     */
    public void setonClick(TextView textView, final String temp, final String motion, final String light, final String RSSI) {


        textView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), temp + " " + motion + " " + light + " " + RSSI, Toast.LENGTH_LONG).show();

                Intent test = new Intent(Bluetooth.this, NodeData.class);
                test.putExtra("temp", temp);
                test.putExtra("motion", motion);
                test.putExtra("light", light);
                test.putExtra("RSSI", RSSI);
                startActivity(test);

            }
        });

    }


    /**
     * When app starts up, will run this function by default and will start the "chat"
     * service (Bluetooth connection with data management) if Bluetooth is not turned on
     * user will receive a request message to enable it
     */
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!BAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    /**
     * Ran by default if Bluetooth connection stops, will completely cancel
     * the Bluetooth service and close all sockets
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mChatService != null) {
            mChatService.stop();
        }


    }

    /**
     * This method handles pop-up messages that pause the Bluetooth service,
     * will restart  upon any interruption
     */

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        scan_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent serverIntent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);

            }
        });
    }

    /**
     * Initializes the scan button for Bluetooth and the Bluetooth service object
     */
    private void setupChat() {

        /** Creating onClickListener for scan button **/
        scan_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent serverIntent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);

            }
        });


        /** Initialize the Bluetooth Service **/
        mChatService = new BluetoothChatService(getApplicationContext(), mHandler);


    }


    /**
     * Handler that receives messages the from BluetoothChatService thread, lets the application
     * know what state of Bluetooth it is currently in (No state, connecting, connected) Also
     * retrieves data received over Bluetooth from the ConnectedThread data management service
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            scan_button.setVisibility(View.INVISIBLE);
                            Nodes.setVisibility(View.VISIBLE);

                            break;

                        case BluetoothChatService.STATE_NONE:

                            scan_button.setVisibility(View.VISIBLE);
                            dbHelper.Delete();
                            Nodes.setVisibility(View.INVISIBLE);

                    }
                    break;

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    //TODO: check that info is at least 4 char long
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    removeViews();

                    testThread(readMessage);

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);

                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Automatically called when app starts up, listens for results for requests
     * such as connecting and enabling Bluetooth
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceList returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:

                if (resultCode == Activity.RESULT_OK)
                {
                    /**Bluetooth is enabled, can set up the chat service **/
                    setupChat();
                }
                else
                {
                    /** if user denies the request to enable Bluetooth, error message will pop up **/
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Bluetooth is disabled")
                            .setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableIntent, 8);

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
        }
    }


    public void testThread(final String readMessage){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                String[] messages = readMessage.split(";");

                //Toast.makeText(getApplicationContext(), messages[0], Toast.LENGTH_LONG).show();

                int dataSize = messages.length;

                for (int j = 0; j < dataSize; j++) {


                    String[] message = messages[j].split(" ");
                    //Toast.makeText(getApplicationContext(), message[0], Toast.LENGTH_LONG).show();
                    String node = message[0];

                    String temp = message[1];

                    String light = message[2];

                    String motion = message[3];

                    String RSSI = message[4];

                    int size = message.length;
                    String neighborLQI = "";

                    for (int x = 5; x < size; x += 2) {

                        //neighborLQI += message[x] + ": " + message[x + 1] + ", ";
                        neighborLQI+= message[x];
                    }

                    //Toast.makeText(getApplicationContext(), neighborLQI, Toast.LENGTH_SHORT).show();

                    boolean check = dbHelper.checkExist(node);

                    if (check) {

                        //checkPermissions(temp, motion, light);
                        updateView(node, temp, motion, light, RSSI, neighborLQI);
                        //Toast.makeText(getApplicationContext(), "placed in DB: " + cool, Toast.LENGTH_SHORT).show();

                    } else {

                        //checkPermissions(temp, motion, light);
                        String cool = dbHelper.updateSensorData(node);
                        ListViewUpdater(node, temp, motion, light, RSSI, neighborLQI);
                        //Toast.makeText(getApplicationContext(), "placed in DB: " + cool, Toast.LENGTH_SHORT).show();

                    }

                }
            }

        });

        thread.start();



    }

    /**
     * This function calls the connect method in the Bluetooth service thread
     * and gives it the Bluetooth device based off MAC address and whether
     * the connection is insecure or secure
     * @param data
     * @param insecure
     */
    private void connectDevice(Intent data, boolean insecure)
    {
        /** Getting MAC Address **/
        String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        /** Getting Bluetooth device based of MAC Address **/
        BluetoothDevice device = BAdapter.getRemoteDevice(address);
        /** Using Bluetooth service object to connect device **/
        mChatService.connect(device, insecure);
    }

    private void ListViewUpdater(final String NodeNumber, final String temp, final String motion, final String light, final String RSSI, final String neighborLQI) {


        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.Nodes);
                TextView textView = new TextView(context);
                /**Cursor ID = dbHelper.getID(NodeNumber);
                ID.moveToFirst();
                int num = Integer.valueOf(ID.getString(0));
                ID.close();**/
                //textView.setId(num);
                textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                textView.setText(NodeNumber);
                textView.setBackground(getResources().getDrawable(R.drawable.button));
                linearLayout.addView(textView);

                //TextView ref = (TextView) linearLayout.findViewById(num);
                //TextViewRef.add(ref);
                setonClick(textView, temp, motion, light, RSSI);

            }

        });


    }

    private void updateView(final String nodeNumber,final String temp, final String motion, final String light, final String RSSI, final String neighborLQI) {

        /**TODO: send all data separately like above and update Database
        *for node data display page query database and send over
        *bundle messages
         **/


        /**Cursor cursorID = dbHelper.getID(nodeNumber);

        int num;
        cursorID.moveToFirst();
        num = Integer.valueOf(cursorID.getString(0));
        cursorID.close();


        for (int x = 0; x < TextViewRef.size(); x++) {

            TextView test = TextViewRef.get(x);
            int ID = test.getId();
            if (ID == num) {**/

                runOnUiThread(new Runnable() {


                    @Override
                    public void run() {

                        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.Nodes);
                        TextView textView = new TextView(context);
                       /** Cursor ID = dbHelper.getID(nodeNumber);
                        ID.moveToFirst();
                        int num = Integer.valueOf(ID.getString(0));
                        ID.close();
                        textView.setId(num);**/
                        textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                        textView.setText(nodeNumber);
                        textView.setBackground(getResources().getDrawable(R.drawable.button));
                        linearLayout.addView(textView);
                        // check here to see if setOnClick worked with the values
                        //TextView ref = (TextView) linearLayout.findViewById(num);
                        //TextViewRef.add(ref);
                        Toast.makeText(getApplicationContext(),"updated", Toast.LENGTH_SHORT).show();
                        setonClick(textView, temp, motion, light, RSSI);
                    }

                });


    }

    public void removeViews()
    {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.Nodes);
                linearLayout.removeAllViews();

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_nav, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_preferences) {
            Intent intent = new Intent(Bluetooth.this, Notifications.class);
            startActivity(intent);

        } else if (id == R.id.discovery) {

            if (BAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        } else if (id == R.id.bluetooth_status) {
            if (BAdapter.isEnabled()) {
                new AlertDialog.Builder(this)
                        .setTitle("Bluetooth Status")
                        .setMessage("Bluetooth is enabled")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Bluetooth Status")
                        .setMessage("Bluetooth is disabled")
                        .setPositiveButton("enable", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableIntent, 1);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }


        return super.onOptionsItemSelected(item);
    }

    //public void Notifications()


    public void checkPermissions(String temp, String motion, String light)
    {


        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String motionPermission = sharedpreferences.getString("motionPermission", null);
        String tempPermission = sharedpreferences.getString("tempPermission", null);
        String lightPermission = sharedpreferences.getString("lightPermission", null);


        if (motionPermission != null){
            if (motionPermission.equals("yes")){
            Notifications(null, null, motion, "Motion");
            }
        }
        if (tempPermission != null){

            if (tempPermission.equals("yes")) {
                String lowerTemp = sharedpreferences.getString("lowerTemp", null);
                String upperTemp = sharedpreferences.getString("upperTemp", null);
                Notifications(lowerTemp, upperTemp, temp, "Temperature");
            }

        }
        if(lightPermission!= null){
            if( lightPermission.equals("yes")) {
                String lowerLight = sharedpreferences.getString("lowerLight", null);
                String upperLight = sharedpreferences.getString("upperLight", null);
                Notifications(lowerLight, upperLight, light, "Light");
            }
        }


    }


    public void Notifications(String lower, String upper, String data, String type)
    {
        float lowerBound;
        float upperBound;
        float sensorData = Float.valueOf(data);
        if(lower != null){
            lowerBound = Float.valueOf(lower);
        }
        if(upper != null){
            upperBound = Float.valueOf(upper);
        }


        /**
        notifications = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if( lower != null & upper != null){

            if(sensorData < lowerBound ) {
                Notification notify = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Sensor Reading Alert")
                        .setContentText(type+" sensor reading is below "+ lower)
                        .build();
                notifications.notify(0, notify);
            }
            else if(sensorData > upperBound){
                Notification notify = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Reach: Sensor Reading Alert")
                        .setContentText(type+" sensor reading is above "+ upper)
                        .build();
                notifications.notify(0, notify);
            }
        }
        else if(lower == null & upper != null){
            if(sensorData > upperBound){
                Notification notify = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Reach: Sensor Reading Alert")
                        .setContentText(type+" sensor reading is above "+ upper)
                        .build();
                notifications.notify(0, notify);
            }

        }else if(lower!= null){

            if(sensorData < lowerBound ) {
                Notification notify = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("Reach: Sensor Reading Alert")
                        .setContentText(type+" sensor reading is below "+ lower)
                        .build();
                notifications.notify(0, notify);
            }
        }else if(  type.equals("Motion") & sensorData == 1 )
        {
            Notification notify = new Notification.Builder(getApplicationContext())
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Reach: Sensor Reading Alert")
                    .setContentText(type+" sensor detected movement ")
                    .build();
            notifications.notify(0, notify);

        }**/


    }


}


