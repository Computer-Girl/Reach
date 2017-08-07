package com.arrow.reach.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.arrow.reach.bluetooth.Bluetooth;


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