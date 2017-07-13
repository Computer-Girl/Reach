package com.example.a85314.meshnetwork.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import com.example.a85314.meshnetwork.Notifications.Notifications;
import com.example.a85314.meshnetwork.R;


public class MainActivity extends AppCompatActivity
{

    public static final String TAG = "MainActivity";



    private final BluetoothAdapter BAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Automatically called upon start of app
     * sets content view and sets up fragment from
     * BluetoothChat
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);

        /**if (savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Bluetooth fragobj = new Bluetooth();
            transaction.replace(R.id.sample_content_fragment, fragobj);
            transaction.commit();
        }**/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_nav, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();




        if (id == R.id.action_preferences)
        {
            Intent intent = new Intent(MainActivity.this, Notifications.class);
            startActivity(intent);

        }

        else if (id == R.id.bluetooth_status)
        {
            if (BAdapter.isEnabled())
            {
                new AlertDialog.Builder(this)
                        .setTitle("Bluetooth Status")
                        .setMessage("Bluetooth is enabled")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
            else
            {
                new AlertDialog.Builder(this)
                        .setTitle("Bluetooth Status")
                        .setMessage("Bluetooth is disabled")
                        .setPositiveButton("enable", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
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


}
