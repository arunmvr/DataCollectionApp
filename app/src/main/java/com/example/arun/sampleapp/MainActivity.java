package com.example.arun.sampleapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckIfGpsIsEnabled();

        TextView mCategory = (TextView) findViewById(R.id.textView);
        final Spinner mTypes = (Spinner) findViewById(R.id.spinner);
        Button mSelect = (Button) findViewById(R.id.button);

        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,InfoActivity.class);
                intent.putExtra("Category", mTypes.getSelectedItem().toString());
                startActivity(intent);
            }
        });
    }

    public void buildAlertMessageNoGps() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(
                        "GPS is disabled in your device. Please enable GPS before proceeding further")
                .setCancelable(false)
                .setPositiveButton("Enable",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Toast.makeText(getApplicationContext(),
                                "Unable to fetch current Location. Please enable Location Services",
                                Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void CheckIfGpsIsEnabled(){
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if ( !mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }else if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Toast.makeText(getApplicationContext(), "Please Enable Network Services", Toast.LENGTH_SHORT).show();

        }
    }

}
