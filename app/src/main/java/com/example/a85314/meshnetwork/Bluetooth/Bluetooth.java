package com.example.a85314.meshnetwork.Bluetooth;

import java.util.Calendar;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.example.a85314.meshnetwork.Notifications.Notifications;
import com.example.a85314.meshnetwork.R;
import com.example.a85314.meshnetwork.Database.*;
import com.example.a85314.meshnetwork.UI.MeshNetDiagram;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
//    private Button scan_button;
//    private DatabaseContract dbHelper;
    private MeshNetDiagram netDiagram;
    private NodeDatabaseHelper database;


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


    /**
     * Setting layout views, requesting permissions during runtime, initializing variables,
     * getting bluetooth adapter,
     */
    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        context = getApplicationContext();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);

        // initializing Database helper object
        database = new NodeDatabaseHelper(context);

        netDiagram = (MeshNetDiagram) findViewById(R.id.Net);
        netDiagram.setHubConnected(false);

        // fetching local Bluetooth adapter
        BAdapter = BluetoothAdapter.getDefaultAdapter();


        // checking if device is Bluetooth compatible
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

       /** Timer pollTimer = new Timer();

        pollTimer.schedule(new TimerTask()
        {

            @Override
            public void run()
            {
                String readMessage = "cake 78.090890 .09090900909909 0 90 bathroom 89 ceiling 34";
                handleData(readMessage);
            }
        },0,6000);**/


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

    }

    /**
     * Initializes the scan button for Bluetooth and the Bluetooth service object
     */
    private void setupChat() {

        Intent serverIntent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);


        // Initialize the Bluetooth Service
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
                            netDiagram.setHubConnected(true);
                            netDiagram.updateData();

                            break;

                        case BluetoothChatService.STATE_NONE:

                            // sets all nodes as disconnected
                            for (Node n : database.getAllNodes()){
                                n.setConnected(false);
                                database.updateNode(n);
                            }
                            netDiagram.setHubConnected(false);
                            netDiagram.updateData();
                            Intent serverIntent = new Intent(getApplicationContext(), DeviceList.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                            break;

                        case BluetoothChatService.STATE_BLUETOOTH_OFF:
                            onStart();
                            break;

                    }
                    break;

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer

                    String readMessage = new String(readBuf, 0, msg.arg1);

                    handleData(readMessage);

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
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    onStart();
                                }
                            })
                            .show();
                }
        }
    }


    public void handleData(final String readMessage){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Log.i(TAG, "Received Bluetooth String: "+readMessage);

                if (readMessage.charAt(0) == '@'){
                    return;
                }

                for (Node n : database.getAllNodes()){
                    n.setConnected(false);
                    database.updateNode(n);
                }

                if (readMessage.charAt(0) != '!') { // '!' indicates no nodes connected

                    String[] messages = readMessage.split(";");

                    //Toast.makeText(getApplicationContext(), messages[0], Toast.LENGTH_LONG).show();


                    int dataSize = messages.length;
                    for (String thisMessage : messages)
                    {


                        String[] message = thisMessage.split(" ");
                        //Toast.makeText(getApplicationContext(), message[0], Toast.LENGTH_LONG).show();
                        String mac = message[0];
                        Node thisNode = new Node(mac);

                        String temp = message[1];
                        thisNode.setTemp(Double.valueOf(temp));

                        String light = message[2];
                        thisNode.setLight(Double.valueOf(light));

                        String motion = message[3];
                        thisNode.setMotion(motion.equals("1"));

                        String RSSI = message[4];
                        thisNode.setRssi(Double.valueOf(RSSI));

                        int size = message.length;

                        for (int x = 5; x < size; x += 2) {
                            thisNode.addNeighbor(message[x], Integer.valueOf(message[x + 1]));

                        }
                        thisNode.setConnected(true);
                        database.addNode(thisNode);

                        notificationsCheck(temp, motion, light, mac);


                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        netDiagram.updateData();
                    }
                });

                Log.i(TAG, "data updated");
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
        // Getting MAC Address
        String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        // Getting Bluetooth device based of MAC Address
        BluetoothDevice device = BAdapter.getRemoteDevice(address);
        // Using Bluetooth service object to connect device
        mChatService.connect(device, insecure);
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

//        } else if (id == R.id.discovery) {
//
//            if (BAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
//            }
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




    public void notificationsCheck(String temp, String motion, String light, String MAC)
    {


        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String motionPermission = sharedpreferences.getString("motionPermission", null);
        String tempPermission = sharedpreferences.getString("tempPermission", null);
        String lightPermission = sharedpreferences.getString("lightPermission", null);


        if (motionPermission != null){
            if (motionPermission.equals("yes"))
            {
                if(motion.equals("0")){
                    createNotification(Calendar.getInstance().getTimeInMillis(), 0.0, "", 0.0, MAC, "Motion");
                }
            }
        }
        //TODO: convert value of num to double
        if (tempPermission != null){

            if (tempPermission.equals("yes")) {
                String lowerTemp = sharedpreferences.getString("lowerTemp", null);
                String upperTemp = sharedpreferences.getString("upperTemp", null);
                if((lowerTemp == null | upperTemp == null) ){
                    Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TOAST, "Please set an upper and lower bound for temperature notifications");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
                else if (lowerTemp.equals("") | upperTemp.equals("")){
                    Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TOAST, "Please set an upper and lower bound for temperature notifications");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
                else{
                    Double lower = Double.valueOf(lowerTemp);
                    Double upper = Double.valueOf(upperTemp);
                    Double data = Double.valueOf(temp);
                    sendAlerts(lower, upper, data, MAC, "Temperature");
                }

            }

        }
        if(lightPermission!= null)
        {
            if( lightPermission.equals("yes")) {
                String lowerLight = sharedpreferences.getString("lowerLight", null);
                String upperLight = sharedpreferences.getString("upperLight", null);
                if((lowerLight == null | upperLight == null)){
                    Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TOAST, "Please set an upper and lower bound for light notifications");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }else if(lowerLight.equals("") | upperLight.equals("")) {
                    Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TOAST, "please set an upper and lower bound for light notifications");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                }else{
                    Double lower = Double.valueOf(lowerLight);
                    Double upper = Double.valueOf(upperLight);
                    Double data = Double.valueOf(light);
                    sendAlerts(lower, upper, data, MAC, "Light");
                }
            }
        }


    }

    public void sendAlerts(Double lower, Double upper, Double data, String MAC, String type)
    {


        if(data < lower) {
            createNotification(Calendar.getInstance().getTimeInMillis(), lower, "below", data, MAC, type);
        }
        else if(data > upper){
            createNotification(Calendar.getInstance().getTimeInMillis(), upper,"above", data, MAC, type);
        }
    }

    private void createNotification(long when, Double boundType, String bound, Double data, String MAC, String type)
    {

        String nodeName = database.getNodeName(MAC);

        String notificationContent;
        String notificationTitle;
        if(!type.equals("Motion")){
        notificationContent = type+" sensor "+ bound+" "+boundType+" at "+data;
        notificationTitle =nodeName+" node: "+type +" sensor alert";
        }else{
            notificationContent = type+" sensor detects no motion";
            notificationTitle =nodeName+" node: "+type +" sensor alert";
        }

        //large icon for notification,normally use App icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),android.R.drawable.ic_dialog_alert);
        int smalIcon =R.drawable.ic_final;
        //String notificationData="This is data : "+data;

		/*create intent for show notification details when user clicks notification*/
        //Intent intent =new Intent(getApplicationContext(), Bluetooth.class);
        //intent.putExtra("Data", notificationData);

		/*create unique this intent from  other intent using setData */
        //intent.setData(Uri.parse("content://"+when));
		/*create new task for each notification with pending intent so we set Intent.FLAG_ACTIVITY_NEW_TASK */
        //PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Intent.FILL_IN_ACTION);

		/*get the system service that manage notification NotificationManager*/
        NotificationManager notificationManager =(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		/*build the notification*/
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(
                getApplicationContext())
                .setWhen(when)
                .setContentText(notificationContent)
                .setContentTitle(notificationTitle)
                .setSmallIcon(smalIcon)
                .setAutoCancel(true)
                .setTicker(notificationTitle)
                .setLargeIcon(largeIcon)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE| Notification.DEFAULT_SOUND);
                //.setContentIntent(pendingIntent);

		/*Create notification with builder*/
        Notification notification=notificationBuilder.build();

		/*sending notification to system.Here we use unique id (when)for making different each notification
		 * if we use same id,then first notification replace by the last notification*/
        notificationManager.notify((int) when, notification);
    }





}
