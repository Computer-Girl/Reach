package com.example.a85314.meshnetwork;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by a85314 on 7/12/2017.
 */

public class TestDiscovery extends AppCompatActivity
{

    BluetoothAdapter bluetoothAdapter;
    private Button discover;

    @Override
    protected void onCreate(Bundle saved){

        super.onCreate(saved);
        setContentView(R.layout.notify_test);

        discover = (Button) findViewById(R.id.test);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        discover.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    startActivity(discoverableIntent);
                }
            }
        });;

       /** bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //makeDiscoverable(300);

        Method method;
        try {
            method = bluetoothAdapter.getClass().getMethod("setScanMode", int.class, int.class);
            method.invoke(bluetoothAdapter,BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,300);
            Log.e("invoke","method invoke successfully");
        }
        catch (Exception e){
            e.printStackTrace();
        }**/



    }



    public void makeDiscoverable (int timeOut){
        Class <?> baClass = BluetoothAdapter.class;
        Method [] methods = baClass.getDeclaredMethods();
        Method mSetScanMode = methods[44];
        try {
            //mSetScanMode.invoke(Util.mBluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeOut);
            Toast.makeText(getApplicationContext(), "made discoverable", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("discoverable", e.getMessage());
        }
    }
}
