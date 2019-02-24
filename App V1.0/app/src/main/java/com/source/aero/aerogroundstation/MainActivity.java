package com.source.aero.aerogroundstation;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothFragment;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;

    //UI Elements
    Button startButton;
    boolean bluetoothFragmentDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiYWVyb2Rlc2lnbiIsImEiOiJjam9sczI0bjMwM3E4M2twMXk0NG93YXg1In0.jYhWqqiBnn4O4KrLImf-Gg");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        startButton = (Button) findViewById(R.id.testButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                startButton.setVisibility(View.INVISIBLE);
                startBlueTooth();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        startButton.setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BluetoothFragment existingFragment = (BluetoothFragment) fragmentManager.findFragmentById(R.id.fragmentContainer);
        fragmentTransaction.remove(existingFragment).commit();
        bluetoothFragmentDisplayed = false;
    }

    //Start bluetooth fragment
    public void startBlueTooth() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (!bluetoothFragmentDisplayed) {
            BluetoothFragment bluetoothfragment = new BluetoothFragment();
            fragmentTransaction.add(R.id.fragmentContainer, bluetoothfragment).addToBackStack("BLUETOOTH").commit();
            bluetoothFragmentDisplayed = true;
        }
        else {
            BluetoothFragment existingFragment = (BluetoothFragment) fragmentManager.findFragmentById(R.id.fragmentContainer);
            fragmentTransaction.remove(existingFragment).commit();
            bluetoothFragmentDisplayed = false;
            startButton.setVisibility(View.VISIBLE);
        }
    }
}