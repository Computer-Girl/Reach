package com.arrow.reach.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.arrow.reach.R;

public class Notifications extends AppCompatActivity
{

    ImageButton tempGear, motionGear, lightGear;
    Switch tempSwitch, motionSwitch, lightSwitch, connectedSwitch,
            disconnectedSwitch, batterySwitch;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;
    int popupSize;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences =
                getSharedPreferences(getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        popupSize = (int) (size.x *.7);

        tempGear = (ImageButton) findViewById(R.id.temp_gear);
        tempGear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TempPopup(popupSize).show();
            }
        });
        motionGear = (ImageButton) findViewById(R.id.motion_gear);
        motionGear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MotionPopup(popupSize).show();
            }
        });
        lightGear = (ImageButton) findViewById(R.id.light_gear);
        lightGear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LightPopup(popupSize).show();
            }
        });

        tempSwitch = (Switch) findViewById(R.id.temp_switch);
        tempSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferencesEditor.putBoolean(getString(R.string.tempPermission), true);
                    sharedPreferencesEditor.apply();
                } else {
                    sharedPreferencesEditor.putBoolean(getString(R.string.tempPermission), false);
                    sharedPreferencesEditor.apply();
                }
            }
        });
        motionSwitch = (Switch) findViewById(R.id.motion_switch);
        motionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferencesEditor.putBoolean(getString(R.string.motionPermission), true);
                    sharedPreferencesEditor.apply();
                } else {
                    sharedPreferencesEditor.putBoolean(getString(R.string.motionPermission), false);
                    sharedPreferencesEditor.apply();
                }
            }
        });
        lightSwitch = (Switch) findViewById(R.id.light_switch);
        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferencesEditor.putBoolean(getString(R.string.lightPermission), true);
                    sharedPreferencesEditor.apply();
                } else {
                    sharedPreferencesEditor.putBoolean(getString(R.string.lightPermission), false);
                    sharedPreferencesEditor.apply();
                }
            }
        });
        connectedSwitch = (Switch) findViewById(R.id.connect_switch);
        connectedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferencesEditor.putBoolean(getString(R.string.connectedPermission), true);
                    sharedPreferencesEditor.apply();
                } else {
                    sharedPreferencesEditor.putBoolean(getString(R.string.connectedPermission), false);
                    sharedPreferencesEditor.apply();
                }
            }
        });
        disconnectedSwitch = (Switch) findViewById(R.id.disconnect_switch);
        disconnectedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferencesEditor.putBoolean(getString(R.string.disconnectedPermission), true);
                    sharedPreferencesEditor.apply();
                } else {
                    sharedPreferencesEditor.putBoolean(getString(R.string.disconnectedPermission), false);
                    sharedPreferencesEditor.apply();
                }
            }
        });
        batterySwitch = (Switch) findViewById(R.id.battery_switch);
        batterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferencesEditor.putBoolean(getString(R.string.batteryPermission), true);
                    sharedPreferencesEditor.apply();
                } else {
                    sharedPreferencesEditor.putBoolean(getString(R.string.batteryPermission), false);
                    sharedPreferencesEditor.apply();
                }
            }
        });

        tempSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.tempPermission), false));
        motionSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.motionPermission), false));
        lightSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.lightPermission), false));
        connectedSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.connectedPermission), false));
        disconnectedSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.disconnectedPermission), false));
        batterySwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.batteryPermission), false));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private class TempPopup{
        PopupWindow popup;
        ImageButton closeButton;
        Spinner spinner;
        EditText lowerEditText, upperEditText;
        TextView cButton, fButton;
        Button saveButton;

        TempPopup(int size){
            LayoutInflater layoutInflater = getLayoutInflater();
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.temp_settings, null);
            popup = new PopupWindow(viewGroup, size, size, true);
            popup.setAnimationStyle(R.style.Animation);
            closeButton = (ImageButton) viewGroup.findViewById(R.id.closeButton_temp);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            spinner = (Spinner) viewGroup.findViewById(R.id.temp_spinner);
            lowerEditText = (EditText) viewGroup.findViewById(R.id.temp_notifications_setup_lower);
            upperEditText = (EditText) viewGroup.findViewById(R.id.temp_notifications_setup_upper);
            cButton = (TextView) viewGroup.findViewById(R.id.deg_c_button);
            fButton = (TextView) viewGroup.findViewById(R.id.def_f_button);
            cButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferencesEditor.putBoolean(
                            getString(R.string.fahrenheit_temp_bound), false);
                    sharedPreferencesEditor.apply();
                    updateCF();
                }
            });
            fButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sharedPreferencesEditor.putBoolean(
                            getString(R.string.fahrenheit_temp_bound), true);
                    sharedPreferencesEditor.apply();
                    updateCF();
                }
            });
            saveButton = (Button) viewGroup.findViewById(R.id.temp_save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save();
                    dismiss();
                }
            });
            update();
        }

        /**
         * Display the popup.
         */
        void show(){
            update();
            popup.showAtLocation(findViewById(R.id.notification_layout), Gravity.CENTER, 0, 0);
        }

        /**
         * Hide the popup.
         */
        void dismiss(){
            popup.dismiss();
        }

        void save(){
            float lowerValue = Float.valueOf(lowerEditText.getText().toString());
            sharedPreferencesEditor.putFloat(getString(R.string.tempLowBound), lowerValue);
            float upperValue = Float.valueOf(upperEditText.getText().toString());
            sharedPreferencesEditor.putFloat(getString(R.string.tempHighBound), upperValue);
            // if "within" selected
            if (spinner.getSelectedItem().toString().equals(getResources().getStringArray(R.array.temp_spinner_entries)[0])){
                Log.i("Notifications", "spinner saved as within");
                sharedPreferencesEditor.putBoolean(getString(R.string.tempOutside), false);
            } else {
                sharedPreferencesEditor.putBoolean(getString(R.string.tempOutside), true);
            }
            sharedPreferencesEditor.apply();
        }

        void updateCF(){
            if (sharedPreferences.getBoolean(getString(R.string.fahrenheit_temp_bound), false)){
                fButton.setTypeface(null, Typeface.BOLD);
                cButton.setTypeface(null, Typeface.NORMAL);
            } else{
                cButton.setTypeface(null, Typeface.BOLD);
                fButton.setTypeface(null, Typeface.NORMAL);
            }
        }

        /**
         * Updates the popup elements according to the set preferences.
         */
        void update(){
            String lowerBound = Float.toString(sharedPreferences.getFloat(getString(R.string.tempLowBound), 0));
            lowerEditText.setText(lowerBound.toCharArray(), 0 , lowerBound.length());
            String upperBound = Float.toString(sharedPreferences.getFloat(getString(R.string.tempHighBound), 10));
            upperEditText.setText(upperBound.toCharArray(), 0, upperBound.length());
            updateCF();
            if (sharedPreferences.getBoolean(getString(R.string.tempOutside), true)){
                spinner.setSelection(1);
            } else {
                spinner.setSelection(0);
            }
        }
    }

    private class LightPopup{
        PopupWindow popup;
        ImageButton closeButton;
        Spinner spinner;
        EditText lowerEditText, upperEditText;
        Button saveButton;

        LightPopup(int size){
            LayoutInflater layoutInflater = getLayoutInflater();
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.light_settings, null);
            popup = new PopupWindow(viewGroup, size, size, true);
            popup.setAnimationStyle(R.style.Animation);
            closeButton = (ImageButton) viewGroup.findViewById(R.id.closeButton_light);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            spinner = (Spinner) viewGroup.findViewById(R.id.light_spinner);
            lowerEditText = (EditText) viewGroup.findViewById(R.id.light_notifications_setup_lower);
            upperEditText = (EditText) viewGroup.findViewById(R.id.light_notifications_setup_upper);
            saveButton = (Button) viewGroup.findViewById(R.id.light_save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save();
                    dismiss();
                }
            });
            update();
        }

        /**
         * Display the popup.
         */
        void show(){
            update();
            popup.showAtLocation(findViewById(R.id.notification_layout), Gravity.CENTER, 0, 0);
        }

        /**
         * Hide the popup.
         */
        void dismiss(){
            popup.dismiss();
        }

        void save(){
            float lowerValue = Float.valueOf(lowerEditText.getText().toString());
            sharedPreferencesEditor.putFloat(getString(R.string.lightLowBound), lowerValue);
            float upperValue = Float.valueOf(upperEditText.getText().toString());
            sharedPreferencesEditor.putFloat(getString(R.string.lightHighBound), upperValue);
            // if "within" selected
            if (spinner.getSelectedItem().toString().equals(getResources().getStringArray(R.array.temp_spinner_entries)[0])){
                sharedPreferencesEditor.putBoolean(getString(R.string.lightOutside), false);
            } else {
                sharedPreferencesEditor.putBoolean(getString(R.string.lightOutside), true);
            }
            sharedPreferencesEditor.apply();
        }

        /**
         * Updates the popup elements according to the set preferences.
         */
        void update(){
            String lowerBound = Float.toString(sharedPreferences.getFloat(getString(R.string.lightLowBound), 0));
            lowerEditText.setText(lowerBound.toCharArray(), 0 , lowerBound.length());
            String upperBound = Float.toString(sharedPreferences.getFloat(getString(R.string.lightHighBound), 10));
            upperEditText.setText(upperBound.toCharArray(), 0, upperBound.length());
            if (sharedPreferences.getBoolean(getString(R.string.lightOutside), true)){
                spinner.setSelection(1);
            } else {
                spinner.setSelection(0);
            }
        }
    }

    private class MotionPopup{

        PopupWindow popup;
        ImageButton closeButton;
        Spinner spinner;
        Button saveButton;

        MotionPopup(int size){
            LayoutInflater layoutInflater = getLayoutInflater();
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.motion_settings, null);
            popup = new PopupWindow(viewGroup, size, size, true);
            popup.setAnimationStyle(R.style.Animation);
            closeButton = (ImageButton) viewGroup.findViewById(R.id.closeButton_motion);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            spinner = (Spinner) viewGroup.findViewById(R.id.motion_spinner);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // if "is" selected
                    if (parent.getItemAtPosition(position).toString().equals(
                            getResources().getStringArray(R.array.motion_spinner_entries)[0])){
                        sharedPreferencesEditor.putBoolean(getString(R.string.motionWhenDetected), true);
                    } else {
                        sharedPreferencesEditor.putBoolean(getString(R.string.motionWhenDetected), false);
                    }
                    sharedPreferencesEditor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            saveButton = (Button) viewGroup.findViewById(R.id.motion_save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save();
                    dismiss();
                }
            });
            update();
        }

        /**
         * Display the popup.
         */
        void show(){
            update();
            popup.showAtLocation(findViewById(R.id.notification_layout), Gravity.CENTER, 0, 0);
        }

        /**
         * Hide the popup.
         */
        void dismiss(){
            popup.dismiss();
        }

        void save(){
            // if 'is' selected
            if (spinner.getSelectedItem().toString().equals(getResources().getStringArray(R.array.motion_spinner_entries)[0])){
                sharedPreferencesEditor.putBoolean(getString(R.string.motionWhenDetected), true);
            } else {
                sharedPreferencesEditor.putBoolean(getString(R.string.motionWhenDetected), false);
            }
        }

        /**
         * Updates the popup elements according to the set preferences.
         */
        void update(){
            if (sharedPreferences.getBoolean(getString(R.string.motionWhenDetected), true)){
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }
        }
    }

}