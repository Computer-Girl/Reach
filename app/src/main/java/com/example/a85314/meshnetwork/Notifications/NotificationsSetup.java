package com.example.a85314.meshnetwork.Notifications;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.a85314.meshnetwork.R;

/**
 * Created by a85314 on 7/6/2017.
 */

public class NotificationsSetup extends Activity {


    private TextView label;
    private Button save;
    private ImageView image;
    public static final String MY_PREFS_NAME = "Notifications";
    SharedPreferences sharedpreferences;
    private EditText lowerEdit;
    private EditText upperEdit;
    public String lowerTemp = "lowerTemp";
    public String upperTemp = "upperTemp";
    public String lowerLight = "lowerLight";
    public String upperLight = "upperLight";


    @Override
    protected void onCreate(Bundle saved)
    {
        super.onCreate(saved);
        setContentView(R.layout.setup);


        label = (TextView) findViewById(R.id.notifications_setup_label);
        save = (Button) findViewById(R.id.saveButton);
        //image = (ImageView) findViewById(R.id.image_setup);
        lowerEdit = (EditText) findViewById(R.id.notifications_setup_lower);
        upperEdit = (EditText) findViewById(R.id.notifications_setup_upper);


        final String type = getIntent().getExtras().getString("type");

        if (type.equals("t"))
        {
            label.setText("Temperature");
            label.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            String tempLower = getPreference(lowerTemp);
            String tempUpper = getPreference(upperTemp);
            if(tempLower!=null){

                lowerEdit.setText(tempLower);
            }
            if(tempUpper!=null){
                upperEdit.setText(tempUpper);
            }

        }
        else{

            label.setText("Ambient Light");
            label.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            String lightLower = getPreference(lowerLight);
            String lightUpper = getPreference(upperLight);
            if(lightLower!=null){

                lowerEdit.setText(lightLower);
            }
            if(lightUpper!=null){
                upperEdit.setText(lightUpper);
            }
        }




        save.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {

                String lower = lowerEdit.getText().toString();
                String upper = upperEdit.getText().toString();
                storePreferences(lower, upper, type);
                finish();

            }
        });


    }

    public void storePreferences(String lower, String upper, String type){

        if(type.equals("t")){
            sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String low = lowerEdit.getText().toString();
            String high = upperEdit.getText().toString();
            editor.putString(lowerTemp,low );
            editor.putString(upperTemp, high);
            editor.apply();

        }
        else
        {
            sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String low = lowerEdit.getText().toString();
            String high = upperEdit.getText().toString();
            editor.putString(lowerLight, low );
            editor.putString(upperLight,high);
            editor.apply();
        }

    }

    public String  getPreference(String key){
        sharedpreferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
       return sharedpreferences.getString(key, null);

    }
}
