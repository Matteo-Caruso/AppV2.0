package com.code.aero.outdated_code;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

//import com.example.aerogroundstation.R;
import com.code.aero.groundstation.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.util.List;

public class LocationServiceActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener {

    private MapView mapView;

    // variables for adding location layer
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Location", "onCreate");

        Mapbox.getInstance(this, getString(R.string.access_token));
        //setContentView(R.layout.activity_location_service);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Add user location to the map
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                map = mapboxMap;
                enableLocationPlugin();
            };
        });

    }

    public Location getOriginLocation(){
        Log.e("Location", "getOriginLocation");
        return originLocation;
    }
    // Add the mapView's own lifecycle methods to the activity's lifecycle methods
    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        Log.e("Location", "onStart");
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Location", "onStop");
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Location", "onDestroy");
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("Location", "onLowMemory");
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Location", "onResume");
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Location", "onPause");
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Location", "onSaveInstanceState");
        mapView.onSaveInstanceState(outState);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        Log.e("Location", "enableLocationPlugin");
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        Log.e("Location", "initLocationEngine");
        locationEngine = new LostLocationEngine(LocationServiceActivity.this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener((LocationEngineListener) this);
        }
    }

    private void setCameraPosition(Location location) {
        Log.e("Location", "setCamPosition");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("Location", "RequestPermssion");
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //@Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    //@Override
    public void onPermissionResult(boolean granted) {
        Log.e("Location", "Permission Result");
        if (granted) {
            enableLocationPlugin();
        } else {
            finish();
        }
    }

    //@Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        Log.e("Location", "onConnected");
        locationEngine.requestLocationUpdates();
    }

    //@Override
    public void onLocationChanged(Location location) {
        //Log.e("Location", "onLocationChanged"+ location.toString());
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener((LocationEngineListener) this);
        }
    }

}
