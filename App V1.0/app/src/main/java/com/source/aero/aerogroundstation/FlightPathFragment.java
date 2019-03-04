package com.source.aero.aerogroundstation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FlightPathFragment extends Fragment implements OnMapReadyCallback {
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

    public FlightPathFragment() {

    }

    //Required methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getActivity(),getResources().getString(R.string.mapboxToken));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_flight_path,parent,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        mapView = (MapView) view.findViewById(R.id.flightPathMapView);
        mapView.onCreate(savedInstance);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

        //Get waypoints
        //data = getArguments();
        //Retrieve data sent from activity
        try {
            //waypoints = (ArrayList<Waypoint>) data.getSerializable("WAYPOINTS");
            //Test data
            waypoints = populate();
        } catch (NullPointerException e) {
            Log.d(TAG,"Couldn't receive waypoints from main activity");
            getActivity().onBackPressed();
        } catch (ClassCastException e) {
            Log.d(TAG,"Data from main activity in wrong format");
            getActivity().onBackPressed();
        }

        //Set current point to first point;
        currentPoint = 0;

        forwardButton = (ImageButton) view.findViewById(R.id.flightPathForwardButton);
        backwardsButton = (ImageButton) view.findViewById(R.id.flightPathBackwardsButton);
        playButton = (ImageButton) view.findViewById(R.id.flightPathPlayButton);

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forward();
            }
        });
        backwardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backward();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });
    }

    public void updateData(Waypoint point) {
        //Initialize textview elements
        TextView altitudeVal = (TextView) getView().findViewById(R.id.flightPathAltitudeVal);
        TextView speedVal = (TextView)  getView().findViewById(R.id.flightPathSpeedVal);
        TextView headingVal = (TextView)  getView().findViewById(R.id.flightPathHeadingVal);
        TextView dropHeightVal = (TextView)  getView().findViewById(R.id.flightPathDropHeightVal);
        TextView rollVal = (TextView)  getView().findViewById(R.id.flightPathRollVal);
        TextView pitchVal = (TextView)  getView().findViewById(R.id.flightPathPitchVal);
        TextView yawVal = (TextView)  getView().findViewById(R.id.flightPathYawVal);

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

    public ArrayList<Waypoint> populate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd-HH:mm:ss_z");
        String sessionId = formatter.format(date);
        double altitude = 100;
        double speed = 10;
        double heading = 90;
        String location = "10101010101";
        int sid = 0;
        double drop = 10;
        double gliderDropHeight = 11;
        double roll = 0;
        double pitch = 0;
        double yaw = 0;
        String flight_type = "P";
        ArrayList<Waypoint> testArray = new ArrayList<Waypoint>();

        for (int i = 0; i < 100; i++) {
            altitude += 1;
            speed += 1;
            heading += 1;
            sid += 1;
            drop += 1;
            roll += 1;
            pitch += 1;
            yaw += 1;
            Waypoint point = new Waypoint(sessionId,sid,location,altitude,speed,heading,drop,gliderDropHeight,roll,pitch,yaw);
            testArray.add(point);
        }

        return testArray;

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
    }
}

