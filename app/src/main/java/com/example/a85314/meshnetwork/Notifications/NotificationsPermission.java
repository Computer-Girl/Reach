package com.example.a85314.meshnetwork.Notifications;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.a85314.meshnetwork.R;

/**
 * Created by a85314 on 7/10/2017.
 */

public class NotificationsPermission extends Activity
{
    private Button done;
    public static final String MY_PREFS_NAME = "Notifications";
    SharedPreferences sharedpreferences;
    CheckBox tempBox;
    CheckBox motionBox;
    CheckBox lightBox;
    String tempPermission = "tempPermission";
    String motionPermission = "motionPermission";
    String lightPermission = "lightPermission";


    @Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        setContentView(R.layout.notifications_permission);


        done = (Button) findViewById(R.id.notifications_setup_button_done);
        tempBox = (CheckBox) findViewById(R.id.temp_checkbox);
        motionBox = (CheckBox) findViewById(R.id.motion_checkbox);
        lightBox = (CheckBox) findViewById(R.id.light_checkbox);


        setChecked(getPreference(tempPermission) , tempBox);
        setChecked(getPreference(motionPermission) , motionBox);
        setChecked(getPreference(lightPermission) , lightBox);

        done.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                finish();
            }
        });

        tempBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                   storePreferences(tempPermission, "yes");
                } else {
                    storePreferences(tempPermission, null);
                }
            }
        });

        motionBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    storePreferences(motionPermission, "yes");
                } else {
                    storePreferences(motionPermission, null);
                }
            }
        });

        lightBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    storePreferences(lightPermission, "yes");
                } else {
                    storePreferences(lightPermission, null);
                }
            }
        });




    }

    public void storePreferences(String permissionName, String permission){

            sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(permissionName,permission );
            editor.apply();
    }

    public String getPreference(String key){
        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return sharedpreferences.getString(key, null);
    }

    public void setChecked (String check, CheckBox box){


            if(check!= null){
                box.setChecked(true);
            }else{
                box.setChecked(false);
            }


    }
}
