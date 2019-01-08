package com.source.aero.aerogroundstation;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MapboxLocationHandler extends Fragment implements PermissionsListener{
    // Permission handling object
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;

    //UI Objects
    private Button currentLocationButton;

    // Storing last location of this device, received from location engine
    private Location lastKnownLocation;

    public MapboxLocationHandler() {
        //Empty fragment constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //Return inflater with location handler layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_mapboxlocationhandler, parent, false);
    }

    //Initialize UI elements
    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        currentLocationButton = (Button) view.findViewById(R.id.locationButton);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLocation();
            }
        });
    }

    //Get map object from main activity
    public void passMap(MapboxMap map) {
        this.mapboxMap = map;
    }

    // Gets current LatLng from location engine and updates last known location. Sets camera to track location.
    @SuppressWarnings( {"MissingPermission"})
    public void cameraToCurrentLocation() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(getActivity());
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            lastKnownLocation = locationComponent.getLastKnownLocation();

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    // Gets current LatLng from location engine and updates last known location. Does not set camera to track location.
    @SuppressWarnings( {"MissingPermission"})
    public void updateCurrentLocation() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(getActivity());
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            lastKnownLocation = locationComponent.getLastKnownLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    // Updates last known location LatLng value.
    public void cameraToLocation() {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())) // Sets the new camera position
                .zoom(17) // Sets the zoom
                .tilt(30)
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);
    }

    // Handling permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Prompt user with location services
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getActivity(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    // Handle permission result
    @Override
    public void onPermissionResult(boolean granted) {
        // If we have permission, zoom camera to location and store latest LatLng. Else, Toast out the not granted string
        if (granted) {
            cameraToCurrentLocation();
        } else {
            Toast.makeText(getActivity(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    //Updates map to current gps location
    public void currentLocation() {
        updateCurrentLocation();
        cameraToLocation();
        Toast.makeText(getActivity(),Location.convert(lastKnownLocation.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(lastKnownLocation.getLongitude(), Location.FORMAT_DEGREES), Toast.LENGTH_LONG).show();
    }
}