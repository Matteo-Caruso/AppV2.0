package com.code.aero.outdated_code;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.code.aero.groundstation.R;
import com.code.aero.groundstation.UserInterfaceActivity;
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

/**
 * Created by Carl on 2018-03-01.
 */


//WANT TO ADD BACK LATER BUT IS EATING TIME
public class Location extends Fragment implements LocationEngineListener, PermissionsListener{
    private MapView mapView;
    private UserInterfaceActivity mapActivity;
    // variables for adding location layer
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private android.location.Location originLocation;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("Location", "onCreate");
        //startLocationService();

        //mapView.getMapAsync(new OnMapReadyCallback() {
           // @Override
            //public void onMapReady(final MapboxMap mapboxMap) {
                enableLocationPlugin();
            //};
        //});

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

    }


//    public void startLocationService(){
//        mapActivity = (UserInterfaceActivity) getActivity();
//        this.map = mapActivity.getMap();
//        this.mapView = mapActivity.getMapView();
//
//    }

    //@Override
    public void onPermissionResult(boolean granted) {
        Log.e("Location", "Permission Result");
        if (granted) {
            enableLocationPlugin();
        } else {
            getActivity().finish();
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    public void startLocationEngine(){
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
    }

    public void stopLocationEngine(){
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
    }
    public void destroyLocationEngine(){
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    //@Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        Log.e("Location", "enableLocationPlugin");
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        Log.e("Location", "initLocationEngine");
        locationEngine = new LostLocationEngine(getActivity());
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        android.location.Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener((LocationEngineListener) this);
        }
    }
    private void setCameraPosition(android.location.Location location) {
        Log.e("Location", "setCamPosition");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }


    //@Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        Log.e("Location", "onConnected");
        locationEngine.requestLocationUpdates();
    }


    //@Override
    public void onLocationChanged(android.location.Location location) {
        //Log.e("Location", "onLocationChanged"+ location.toString());
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener((LocationEngineListener) this);
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onStart() {
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
    public void onStop() {
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
    public void onDestroy() {
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
    public void onResume() {
        super.onResume();
        Log.d("Location", "onResume");
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Location", "onPause");
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Location", "onSaveInstanceState");
        mapView.onSaveInstanceState(outState);
    }

}
