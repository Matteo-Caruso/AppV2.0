package com.source.aero.aerogroundstation;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxMap map;

    //Offline Maps Objects
    private boolean offlineMapsFragmentDisplayed = false;
    private Button loadOfflineMapsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize maps and mapview objects
        Mapbox.getInstance(this, "pk.eyJ1IjoiYWVyb2Rlc2lnbiIsImEiOiJjam9sczI0bjMwM3E4M2twMXk0NG93YXg1In0.jYhWqqiBnn4O4KrLImf-Gg");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

        //Offline Maps
        //Initialize load button for offline maps fragment
        loadOfflineMapsButton = (Button) findViewById(R.id.loadbutton);
        loadOfflineMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                offlineMapsDialog();
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

    //Offline Maps
    //Activate or deactivate offline maps fragment
    public void offlineMapsDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (!offlineMapsFragmentDisplayed) {
            //Add fragment to fragment stack
            OfflineMaps offlineMapsFragment = new OfflineMaps();
            fragmentTransaction.add(R.id.offlineMapsContainer, offlineMapsFragment).addToBackStack(null).commit();
            offlineMapsFragmentDisplayed = true;
            offlineMapsFragment.passMap(mapView,map);
        }
        else {
            //Remove fragment from stack
            OfflineMaps existingFragment = (OfflineMaps) fragmentManager.findFragmentById(R.id.offlineMapsContainer);
            fragmentTransaction.remove(existingFragment).commit();
            offlineMapsFragmentDisplayed = false;
        }
    }
}