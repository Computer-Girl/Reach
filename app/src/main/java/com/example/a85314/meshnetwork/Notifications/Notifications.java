package com.example.a85314.meshnetwork.Notifications;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.a85314.meshnetwork.R;

/**
 * Created by Jasmine Rethmann on 6/26/17
 */

public class Notifications extends ActionBarActivity
{



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        ImageButton tempAdd = (ImageButton) findViewById(R.id.notification_temp);
        tempAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(Notifications.this, NotificationsSetup.class);
                intent.putExtra("type", "t");
                startActivity(intent);


            }
        });



        ImageButton motionAdd = (ImageButton) findViewById(R.id.notification_motion);
        motionAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Toast.makeText(getApplicationContext(), "Cannot set bounds for motion sensor readings", Toast.LENGTH_SHORT).show();

            }
        });


        ImageButton lightAdd = (ImageButton) findViewById(R.id.notification_light);
        lightAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Notifications.this, NotificationsSetup.class);
                intent.putExtra("type", "l");
                startActivity(intent);

            }
        });



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.notifications_nav, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();


        if (id == R.id.notifications_setup)
        {
            Intent intent = new Intent(Notifications.this, NotificationsPermission.class);
            startActivity(intent);

        }


        return super.onOptionsItemSelected(item);
    }

}