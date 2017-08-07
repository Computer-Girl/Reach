package com.arrow.reach.bluetooth;

import java.util.Calendar;

import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.Display;
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
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.arrow.reach.notifications.Notifications;
import com.arrow.reach.R;
import com.arrow.reach.database.*;
import com.arrow.reach.ui.MeshNetDiagram;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bluetooth extends AppCompatActivity
{
    private static final String TAG = "Bluetooth";

    // time between sensor notifications of same node and type
    private static final int NOTIFICATION_WAIT_TIME_MS = 14000;
    // time between low battery notifications of same node
    private static final int BATTERY_NOTIFICATION_WAIT_TIME_MS = 4*60*1000;

    SharedPreferences sharedpreferences;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView welcomeTitle, welcomePhrase;
    private ImageButton connectButton;
    private MeshNetDiagram netDiagram;
    private NodeDatabaseHelper database;

    // used to make sure user doesn't get wave of same notifications
    // maps type of notification to last time notification was displayed
    Map<String, Long> notifMap;


    // Name of the connected device
    private String mConnectedDeviceName = null;


    // Local Bluetooth adapter
    private BluetoothAdapter BAdapter = null;

    private BluetoothChatService mChatService = null;

    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        context = getApplicationContext();

        database = new NodeDatabaseHelper(context);

        welcomeTitle = (TextView) findViewById(R.id.welcome_text);
        welcomePhrase = (TextView) findViewById(R.id.welcome_phrase);
        connectButton = (ImageButton) findViewById(R.id.connect_button);
        ViewGroup.LayoutParams params = connectButton.getLayoutParams();
        Display display = getWindowManager().getDefaultDisplay();
        Point size= new Point();
        display.getSize(size);
        params.width = size.x / 5;
        params.height = size.x / 5;
        connectButton.setLayoutParams(params);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_DENIED){
                    new AlertDialog.Builder(Bluetooth.this)
                            .setTitle(context.getString(R.string.location_permission_title))
                            .setMessage(context.getString(R.string.location_permission_content))
                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(Bluetooth.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else if (!BAdapter.isEnabled()){
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

                }
                else if (BAdapter.isEnabled() &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    setupChat();
                }
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
                    .setTitle(context.getString(R.string.not_compatible))
                    .setMessage(context.getString(R.string.does_not_support))
                    .setPositiveButton(context.getString(R.string.exit), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            connectButton.callOnClick();
        }
    }



    /**
     * Run by default if Bluetooth connection stops, will completely cancel
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
     * Begins connection to hub
     */
    private void setupChat() {

        Intent serverIntent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);


        // Initialize the Bluetooth Service
        mChatService = new BluetoothChatService(mHandler);


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
                            break;

                        case BluetoothChatService.STATE_BLUETOOTH_OFF:
                            Toast.makeText(getApplicationContext(), "Bluetooth disabled", Toast.LENGTH_SHORT).show();
                            netDiagram.setVisibility(View.INVISIBLE);
                            showWelcome();
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
                    // Bluetooth is enabled, can set up the chat service
                    connectButton.callOnClick();
                }
        }
    }


    public void handleData(final String readMessage){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {

                Log.i(TAG, "Received Bluetooth String: "+readMessage);

                List<Node> previousConnectedNodes = database.getConnectedNodes();

                // indicates hub in discovery mode (not currently used)
                if (readMessage.charAt(0) == '@'){
                    return;
                }

                for (Node n : database.getAllNodes()){
                    n.setConnected(false);
                    database.updateNode(n);
                }

                if (readMessage.charAt(0) != '!') { // '!' indicates no nodes connected

                    String[] messages = readMessage.split(";");


                    for (String thisMessage : messages)
                    {


                        String[] message = thisMessage.split(" ");
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

                        String battery = message[5];
                        // Swap value because 0 signal mean low battery
                        thisNode.setBatteryLow(!battery.equals("1"));

                        int size = message.length;

                        for (int x = 6; x < size; x += 2) {
                            thisNode.addNeighbor(message[x], Integer.valueOf(message[x + 1]));

                        }
                        thisNode.setConnected(true);
                        database.addNode(thisNode);

                        sensorNotificationsCheck(thisNode);


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
        }


        return super.onOptionsItemSelected(item);
    }




    public void sensorNotificationsCheck(Node node)
    {
        boolean motionPermission = sharedpreferences.getBoolean(getString(R.string.motionPermission), false);
        boolean tempPermission = sharedpreferences.getBoolean(getString(R.string.tempPermission), false);
        boolean lightPermission = sharedpreferences.getBoolean(getString(R.string.lightPermission), false);
        boolean batteryPermission = sharedpreferences.getBoolean(getString(R.string.batteryPermission), false);
        String content, title;

        if (motionPermission){
            if (!(notifMap.containsKey(node.getMac()+"motion") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(node.getMac()+"motion"))<NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(node.getMac()+"motion", Calendar.getInstance().getTimeInMillis());
                if (node.isMotion() && sharedpreferences.getBoolean(getString(R.string.motionWhenDetected), true)) {
                    title = database.getNodeName(node.getMac()) + context.getString(R.string.motion_alert);
                    content = context.getString(R.string.motion_is_detected);
                    createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_motion_sensor);
                } else if (!node.isMotion() && !sharedpreferences.getBoolean(getString(R.string.motionWhenDetected), true)) {
                    title = database.getNodeName(node.getMac()) + context.getString(R.string.motion_alert);
                    content = context.getString(R.string.motion_is_not_detected);
                    createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_motion_sensor);
                }
            }
        }
        if (tempPermission){
            if (!(notifMap.containsKey(node.getMac()+"temp") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(node.getMac()+"temp"))<NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(node.getMac()+"temp", Calendar.getInstance().getTimeInMillis());
                double lower = (double) sharedpreferences.getFloat(getString(R.string.tempLowBound), (float) 0);
                double upper = (double) sharedpreferences.getFloat(getString(R.string.tempHighBound), (float) 10);
                String displayTemp;
                double tempInC = Math.round(node.getTemp() * 10.0) / 10.0;
                if (sharedpreferences.getBoolean(getString(R.string.fahrenheit_temp_bound), false)) {
                    displayTemp = Double.toString(tempInC * 1.8 + 32) + " °F";
                } else {
                    displayTemp = Double.toString(tempInC) + " °C";
                }
                if (!sharedpreferences.getBoolean(getString(R.string.tempOutside), true)) {
                    if (tempInC >= lower && tempInC <= upper) {
                        title = database.getNodeName(node.getMac()) + context.getString(R.string.temperature_alert);
                        content = displayTemp;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_temp_sensor);
                    }
                } else {
                    if (tempInC < lower || tempInC > upper) {
                        title = database.getNodeName(node.getMac()) + context.getString(R.string.temperature_alert);
                        content = displayTemp;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_temp_sensor);
                    }
                }
            }
        }
        if(lightPermission)
        {
            if (!(notifMap.containsKey(node.getMac()+"light") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(node.getMac()+"light"))<NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(node.getMac()+"light", Calendar.getInstance().getTimeInMillis());
                double light = 100 - Math.round(node.getLight() * 100.0) / 100.0 * 100;
                double lower = (double) sharedpreferences.getFloat(getString(R.string.lightLowBound), (float) 0);
                double upper = (double) sharedpreferences.getFloat(getString(R.string.lightHighBound), (float) 10);
                String displayLight = light + "%";
                if (!sharedpreferences.getBoolean(getString(R.string.lightOutside), true)) {
                    if (light >= lower && light <= upper) {
                        title = database.getNodeName(node.getMac()) + context.getString(R.string.light_alert);
                        content = displayLight;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_light_sensor);
                    }
                } else {
                    if (light < lower || light > upper) {
                        title = database.getNodeName(node.getMac()) + context.getString(R.string.light_alert);
                        content = displayLight;
                        createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_light_sensor);
                    }
                }
            }
        }

        if (batteryPermission){
            if (!(notifMap.containsKey(node.getMac()+"battery") &&
                    (Calendar.getInstance().getTimeInMillis()-notifMap.get(node.getMac()+"battery"))<BATTERY_NOTIFICATION_WAIT_TIME_MS)) {
                notifMap.put(node.getMac()+"battery", Calendar.getInstance().getTimeInMillis());
                if (node.isBatteryLow()) {
                    title = database.getNodeName(node.getMac()) + context.getString(R.string.battery_low);
                    content = context.getString(R.string.charge_node);
                    createNotification(Calendar.getInstance().getTimeInMillis(), title, content, R.drawable.ic_battery);
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        
    }

    private void createNotification(long when, String title, String content, int smallIcon)
    {

		/*create intent for show notification details when user clicks notification*/
        Intent intent =new Intent(getApplicationContext(), Bluetooth.class);

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
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE| Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

		/*Create notification with builder*/
        Notification notification=notificationBuilder.build();

		/*sending notification to system.Here we use unique id (when)for making different each notification
		 * if we use same id,then first notification replace by the last notification*/
        notificationManager.notify((int) when, notification);
    }





}
