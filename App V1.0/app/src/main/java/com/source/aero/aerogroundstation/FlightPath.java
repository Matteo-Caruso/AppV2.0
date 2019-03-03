package com.source.aero.aerogroundstation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FlightPath extends Fragment {
    private final static String TAG = "FLIGHTPATH";

    private Polyline path;
    private MapView mapView;
    private MapboxMap map;
    protected Marker planeMarker;
    Marker Payload;
    Marker CDA;
    protected Marker lastPosition;
    protected Bitmap icon;

    //UI Elements
    ImageButton forwardButton;
    ImageButton backwardsButton;
    ImageButton playButton;
    DecimalFormat telemetryFormat = new DecimalFormat("#.00");

    //Data elements
    ArrayList<Waypoint> waypoints;
    int currentPoint;
    Bundle data;

    public FlightPath() {
        //Empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get waypoints
        data = getArguments();
        //Retrieve data sent from activity
        try {
            waypoints = (ArrayList<Waypoint>) data.getSerializable("WAYPOINTS");
        } catch (NullPointerException e) {
            Log.d(TAG,"Couldn't receive waypoints from main activity");
            getActivity().onBackPressed();
        } catch (ClassCastException e) {
            Log.d(TAG,"Data from main activity in wrong format");
            getActivity().onBackPressed();
        }

        //Set current point to first point;
        currentPoint = 0;

        //Map setup
        Mapbox.getInstance(getActivity(),getResources().getString(R.string.mapboxToken));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });
    }

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_flight_path, parent, false);
    }

    @Override
    public void onViewCreated(@Nullable View view, Bundle savedInstance) {
        forwardButton = (ImageButton) view.findViewById(R.id.flightPathForwardButton);
        backwardsButton = (ImageButton) view.findViewById(R.id.flightPathBackwardsButton);
        playButton = (ImageButton) view.findViewById(R.id.flightPathPlayButton);

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });
        backwardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forward();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backward();
            }
        });
    }

    public void updateData(Waypoint point) {
        //Initialize textview elements
        TextView altitudeVal = (TextView) getView().findViewById(R.id.flightPathAltitudeVal);
        TextView speedVal = (TextView) getView().findViewById(R.id.flightPathSpeedVal);
        TextView headingVal = (TextView) getView().findViewById(R.id.flightPathHeadingVal);
        TextView dropHeightVal = (TextView) getView().findViewById(R.id.flightPathDropHeightVal);
        TextView rollVal = (TextView) getView().findViewById(R.id.flightPathRollVal);
        TextView pitchVal = (TextView) getView().findViewById(R.id.flightPathPitchVal);
        TextView yawVal = (TextView) getView().findViewById(R.id.flightPathYawVal);

        //Update textviews for current point
        altitudeVal.setText(getString(R.string.flightPathAltitudeFormatString,point.getAltitude()));
        speedVal.setText(getString(R.string.flightPathSpeedFormatString,point.getSpeed()));
        headingVal.setText(getString(R.string.flightPathHeadingFormatString,point.getHeading()));
        dropHeightVal.setText(getString(R.string.flightPathAltitudeFormatString,point.getAltitude()));
        rollVal.setText(getString(R.string.flightPathRollFormatString,point.getRoll()));
        pitchVal.setText(getString(R.string.flightPathPitchFormatString,point.getPitch()));
        yawVal.setText(getString(R.string.flightPathYawFormatString,point.getYaw()));
    }

    public void play() {
        //TODO: Add play functionality
    }

    public void forward() {
        if (currentPoint < waypoints.size()-1) {
            currentPoint += 1;
            updateData(waypoints.get(currentPoint));
        }
        else {
            Toast.makeText(getActivity(),"End of path reached",Toast.LENGTH_SHORT).show();
        }
    }

    public void backward() {
        if (currentPoint > 0) {
            currentPoint -= 1;
            updateData(waypoints.get(currentPoint));
        } else {
            Toast.makeText(getActivity(),"Start of path reached",Toast.LENGTH_SHORT).show();
        }
    }
}
