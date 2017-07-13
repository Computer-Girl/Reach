package com.example.a85314.meshnetwork;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.a85314.meshnetwork.Bluetooth.Bluetooth;
import com.example.a85314.meshnetwork.NodeData.NodeData;

/**
 * Created by a85314 on 7/12/2017.
 */

public class Test extends AppCompatActivity
{
    private Button test;
    NotificationManager notifications;

    @Override
    protected void onCreate(Bundle saved){

        super.onCreate(saved);
        setContentView(R.layout.notify_test);
        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                notifications = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification notify = new Notification.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("New Notification")
                        .setContentText("Sensor sensed somethinggg")
                        .build();

                notifications.notify(0, notify);

            }
        });



    }



}
