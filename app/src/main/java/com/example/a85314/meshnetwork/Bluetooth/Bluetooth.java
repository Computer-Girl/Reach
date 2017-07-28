package com.example.a85314.meshnetwork.Bluetooth;

import java.util.Calendar;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.a85314.meshnetwork.Notifications.Notifications;
import com.example.a85314.meshnetwork.R;
import com.example.a85314.meshnetwork.Database.*;
import com.example.a85314.meshnetwork.UI.MeshNetDiagram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: data may not have ; make sure to check for that

public class Bluetooth extends AppCompatActivity
{



    private NotificationManager notifications;
    public static final String MY_PREFS_NAME = "Notifications";
    private static final int NOTIFICATION_WAIT_TIME_MS = 14000;
    SharedPreferences sharedpreferences;

    private ArrayList<TextView> TextViewRef;


    private static final String TAG = "Bluetooth";


    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
//    private Button scan_button;
//    private DatabaseContract dbHelper;
    private TextView welcomeTitle, welcomePhrase;
    private Button connectButton;
    private MeshNetDiagram netDiagram;
    private NodeDatabaseHelper database;

    // used to make sure user doesn't get wave of a million notifications
    Map<String, Long> notifMap;


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

        welcomeTitle = (TextView) findViewById(R.id.welcome_text);
        welcomePhrase = (TextView) findViewById(R.id.welcome_phrase);
        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupChat();
            }
        });

        netDiagram = (MeshNetDiagram) findViewById(R.id.Net);
        showWelcome();
        netDiagram.setVisibility(View.INVISIBLE);
        netDiagram.setHubConnected(false);


        // fetching local Bluetooth adapter
        BAdapter = BluetoothAdapter.getDefaultAdapter();

        sharedpreferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        notifMap = new HashMap<>();


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
            showWelcome();
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            showWelcome();
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

    private void showWelcome(){
        welcomeTitle.setVisibility(View.VISIBLE);
        welcomePhrase.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
    }

    private void hideWelcome(){
        welcomeTitle.setVisibility(View.INVISIBLE);
        welcomePhrase.setVisibility(View.INVISIBLE);
        connectButton.setVisibility(View.INVISIBLE);
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
                            hideWelcome();
                            netDiagram.setHubConnected(true);
                            netDiagram.setVisibility(View.VISIBLE);
                            netDiagram.updateData();

                            break;

                        case BluetoothChatService.STATE_NONE:

                            // sets all nodes as disconnected
                            for (Node n : database.getAllNodes()){
                                n.setConnected(false);
                                database.updateNode(n);
                            }
                            showWelcome();
                            netDiagram.setHubConnected(false);
                            netDiagram.setVisibility(View.INVISIBLE);
                            netDiagram.updateData();
                            Intent serverIntent = new Intent(getApplicationContext(), DeviceList.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                            break;

                        case BluetoothChatService.STATE_BLUETOOTH_OFF:
                            netDiagram.setVisibility(View.INVISIBLE);
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
                    showWelcome();
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

                List<Node> previousConnectedNodes = database.getConnectedNodes();

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

                        sensorNotificationsCheck(Double.valueOf(temp), motion.equals("1"), Double.valueOf(light), mac);


                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        netDiagram.updateData();
                    }
                });

                List<Node> nowConnectedNodes = database.getConnectedNodes();
                if(sharedpreferences.getBoolean(getString(R.string.disconnectedPermission), false)) {
                    for (Node oldNode : previousConnectedNodes) {
                        if (!nowConnectedNodes.contains(oldNode)) {
                            String title = "Node Disconnected";
                            String content = oldNode.getName() + " is now disconnected from network.";
                            createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_remove);
                        }
                    }
                }
                if(sharedpreferences.getBoolean(getString(R.string.connectedPermission), false)) {
                    for (Node newNode : nowConnectedNodes) {
                        if (!previousConnectedNodes.contains(newNode)) {
                            String title = "Node Connected";
                            String content = newNode.getName() + " is now connected to network.";
                            createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_add);
                        }
                    }
                }

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
//        } else if (id == R.id.bluetooth_status) {
//            if (BAdapter.isEnabled()) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Bluetooth Status")
//                        .setMessage("Bluetooth is enabled")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_info)
//                        .show();
//            } else {
//                new AlertDialog.Builder(this)
//                        .setTitle("Bluetooth Status")
//                        .setMessage("Bluetooth is disabled")
//                        .setPositiveButton("enable", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                                startActivityForResult(enableIntent, 1);
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
        }


        return super.onOptionsItemSelected(item);
    }




    public void sensorNotificationsCheck(Double tempInC, boolean motion, Double light, String MAC)
    {



        boolean motionPermission = sharedpreferences.getBoolean(getString(R.string.motionPermission), false);
        boolean tempPermission = sharedpreferences.getBoolean(getString(R.string.tempPermission), false);
        boolean lightPermission = sharedpreferences.getBoolean(getString(R.string.lightPermission), false);
        String content, title;

        if (motionPermission){
            if (!(notifMap.containsKey(MAC+"motion") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(MAC+"motion"))<NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(MAC+"motion", Calendar.getInstance().getTimeInMillis());
                // TODO: check if default value is same as default values in popups
                if (motion && sharedpreferences.getBoolean(getString(R.string.motionWhenDetected), true)) {
                    title = database.getNodeName(MAC) + ": motion sensor alert";
                    content = "Motion is detected.";
                    createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_motion_sensor);
                } else if (!motion && !sharedpreferences.getBoolean(getString(R.string.motionWhenDetected), true)) {
                    title = database.getNodeName(MAC) + ": motion sensor alert";
                    content = "No motion is detected.";
                    createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_motion_sensor);
                }
            }
        }
        if (tempPermission){
            if (!(notifMap.containsKey(MAC+"temp") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(MAC+"temp"))<NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(MAC+"temp", Calendar.getInstance().getTimeInMillis());
                double lower = (double) sharedpreferences.getFloat(getString(R.string.tempLowBound), (float) 0);
                double upper = (double) sharedpreferences.getFloat(getString(R.string.tempHighBound), (float) 0);
                String displayTemp;
                tempInC = Math.round(tempInC * 10.0) / 10.0;
                if (sharedpreferences.getBoolean(getString(R.string.fahrenheit_temp_bound), false)) {
                    displayTemp = Double.toString(tempInC * 1.8 + 32) + " °F";
                } else {
                    displayTemp = Double.toString(tempInC) + " °C";
                }
                if (!sharedpreferences.getBoolean(getString(R.string.tempOutside), false)) {
                    if (tempInC >= lower && tempInC <= upper) {
                        title = database.getNodeName(MAC) + ": temperature sensor alert";
                        content = displayTemp;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_temp_sensor);
                    }
                } else {
                    if (tempInC < lower || tempInC > upper) {
                        title = database.getNodeName(MAC) + ": temperature sensor alert";
                        content = displayTemp;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_temp_sensor);
                    }
                }
            }
        }
        if(lightPermission)
        {
            if (!(notifMap.containsKey(MAC+"light") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(MAC+"light"))<NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(MAC+"light", Calendar.getInstance().getTimeInMillis());
                light = 100 - Math.round(light * 100.0) / 100.0 * 100;
                double lower = (double) sharedpreferences.getFloat(getString(R.string.lightLowBound), (float) 0);
                double upper = (double) sharedpreferences.getFloat(getString(R.string.lightHighBound), (float) 0);
                String displayLight = light + "%";
                if (!sharedpreferences.getBoolean(getString(R.string.lightOutside), false)) {
                    if (light >= lower && light <= upper) {
                        title = database.getNodeName(MAC) + ": ambient light sensor alert";
                        content = displayLight;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_light_sensor);
                    }
                } else {
                    if (light < lower || light > upper) {
                        title = database.getNodeName(MAC) + ": ambient light sensor alert";
                        content = displayLight;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_light_sensor);
                    }
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        
    }

    private void createNotification(long when, String title, String content, int smallIcon)
    {

//        String nodeName = database.getNodeName(MAC);

//        String notificationContent;
//        String notificationTitle;
//        if(!type.equals("Motion")){
//        notificationContent = type+" sensor "+ bound+" "+boundType+" at "+data;
//        notificationTitle =nodeName+" node: "+type +" sensor alert";
//        }else{
//            notificationContent = type+" sensor detects no motion";
//            notificationTitle =nodeName+" node: "+type +" sensor alert";
//        }

        //large icon for notification,normally use App icon
//        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),android.R.drawable.ic_dialog_alert);
//        int smallIcon =R.drawable.ic_final;
        //String notificationData="This is data : "+data;

		/*create intent for show notification details when user clicks notification*/
        Intent intent =new Intent(getApplicationContext(), Bluetooth.class);
//        intent.putExtra("Data", notificationData);

		/*create unique this intent from  other intent using setData */
        intent.setData(Uri.parse("content://"+when));
		/*create new task for each notification with pending intent so we set Intent.FLAG_ACTIVITY_NEW_TASK */
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Intent.FILL_IN_ACTION);

		/*get the system service that manage notification NotificationManager*/
        NotificationManager notificationManager =(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		/*build the notification*/
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(
                getApplicationContext())
                .setWhen(when)
                .setContentText(content)
                .setContentTitle(title)
                .setSmallIcon(smallIcon)
                .setAutoCancel(true)
                .setTicker(title)
//                .setLargeIcon(largeIcon)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE| Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

		/*Create notification with builder*/
        Notification notification=notificationBuilder.build();

		/*sending notification to system.Here we use unique id (when)for making different each notification
		 * if we use same id,then first notification replace by the last notification*/
        notificationManager.notify((int) when, notification);
    }





}
