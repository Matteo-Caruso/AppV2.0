package com.source.aero.aerogroundstation;

import java.util.List;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.geometry.LatLng;

import com.source.aero.aerogroundstation.MapboxLocationHandler;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    // Mapbox storage containers
    private MapView mapView;
    private MapboxMap mapboxMap;

    //myLocation
    private Button loadLocationButton;
    private boolean locationFragmentDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //myLocation
        loadLocationButton = (Button) findViewById(R.id.locationButton);
        loadLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findLocation();
            }
        });

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
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


    // This button is used for testing if the camera will zoom to our current location on button press and then print out the new latitude and longitude
    public void findLocation() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (!locationFragmentDisplayed) {
            MapboxLocationHandler locationHandlerFragment = new MapboxLocationHandler();
            fragmentTransaction.add(R.id.fragmentContainer, locationHandlerFragment).addToBackStack(null).commit();
            locationFragmentDisplayed = true;
            locationHandlerFragment.passMap(this.mapboxMap);
        }
        else {
            MapboxLocationHandler existingFragment = (MapboxLocationHandler) fragmentManager.findFragmentById(R.id.fragmentContainer);
            fragmentTransaction.remove(existingFragment).commit();
            locationFragmentDisplayed = false;
        }
    }
}