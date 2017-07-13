package com.example.a85314.meshnetwork.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.a85314.meshnetwork.Bluetooth.Bluetooth;


/**
 * Created by a85314 on 5/23/2017.
 */

public class Splash extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, Bluetooth.class);
        startActivity(intent);
        finish();
    }
}