package com.example.a85314.meshnetwork.NodeData;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.example.a85314.meshnetwork.R;

/**
 * Created by a85314 on 7/8/2017.
 */

public class NodeData extends AppCompatActivity
{


    private TextView temp;
    private TextView motion;
    private TextView light;
    private TextView RSSI;

    @Override
    protected void onCreate(Bundle saved){

        super.onCreate(saved);
        setContentView(R.layout.node_data);

        String tempData = getIntent().getExtras().getString("temp");
        String motionData = getIntent().getExtras().getString("motion");
        String lightData = getIntent().getExtras().getString("light");
        String RSSIData = getIntent().getExtras().getString("RSSI");


        temp = (TextView) findViewById(R.id.temp_display);
        motion = (TextView) findViewById(R.id.motion_display);
        light = (TextView) findViewById(R.id.light_display);
        RSSI = (TextView) findViewById(R.id.RSSI_display);


        temp.setText(tempData);
        motion.setText(motionData);
        light.setText(lightData);
        RSSI.setText(RSSIData);
    }
}
