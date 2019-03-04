package com.source.aero.aerogroundstation;

import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
    protected IconFactory factory;

    //UI Elements
    ImageButton forwardButton;
    ImageButton backwardsButton;
    ImageButton playButton;

    //Data elements
    ArrayList<Waypoint> waypoints;
    ArrayList<LatLng> points;
    int[] showing; //0 = not showing, 1 = passed point (in polyline), 2 = possed point and showing black circle
    int currentPoint;
    int lastPoint;
    Bundle data;

    public FlightPathFragment() {
        //Empty constructor
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
        lastPoint = 0;
        showing = new int[waypoints.size()];
        Arrays.fill(showing,0);

        points = new ArrayList<LatLng>();

        forwardButton = (ImageButton) view.findViewById(R.id.flightPathForwardButton);
        backwardsButton = (ImageButton) view.findViewById(R.id.flightPathBackwardsButton);
        playButton = (ImageButton) view.findViewById(R.id.flightPathPlayButton);

        factory = IconFactory.getInstance(getActivity());
        icon = factory.fromResource(R.drawable.ic_plane).getBitmap();

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
        TextView locationVal = (TextView) getView().findViewById(R.id.flightPathLocationVal);

        //Update textviews for current point
        altitudeVal.setText(getString(R.string.flightPathAltitudeFormatString,point.getAltitude()));
        speedVal.setText(getString(R.string.flightPathSpeedFormatString,point.getSpeed()));
        headingVal.setText(getString(R.string.flightPathHeadingFormatString,point.getHeading()));
        dropHeightVal.setText(getString(R.string.flightPathAltitudeFormatString,point.getAltitude()));
        rollVal.setText(getString(R.string.flightPathRollFormatString,point.getRoll()));
        pitchVal.setText(getString(R.string.flightPathPitchFormatString,point.getPitch()));
        yawVal.setText(getString(R.string.flightPathYawFormatString,point.getYaw()));
        locationVal.setText(point.getLocation());
    }

    public void play() {
        //TODO: Add play functionality
    }

    public void forward() {
        if (currentPoint < waypoints.size()-1) {
            currentPoint += 1;
            updateData(waypoints.get(currentPoint));
            updatePlane(waypoints.get(currentPoint),lastPoint, currentPoint);
            lastPoint = currentPoint;
        }
        else {
            Toast.makeText(getActivity(),"End of path reached",Toast.LENGTH_SHORT).show();
        }
    }

    public void backward() {
        if (currentPoint > 0) {
            currentPoint -= 1;
            updateData(waypoints.get(currentPoint));
            updatePlane(waypoints.get(currentPoint),lastPoint,currentPoint);
            lastPoint = currentPoint;
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
        String location = "28.0394650,-81.9498040";
        int sid = 0;
        double drop = 10;
        double gliderDropHeight = 11;
        double roll = 0;
        double pitch = 0;
        double yaw = 20;
        String flight_type = "P";
        ArrayList<Waypoint> testArray = new ArrayList<Waypoint>();

        for (int i = 0; i < 100; i++) {
            altitude += 1;
            speed += 1;
            heading += 1;
            sid += 1;
            String[] split = location.split(",");
            double latitude = Double.parseDouble(split[0]);
            double longitude = Double.parseDouble(split[1]);
            latitude += 0.001;
            longitude += 0.001;
            yaw += 0.1;
            location = Double.toString(latitude) + "," + Double.toString(longitude);
            Waypoint point = new Waypoint(sessionId,sid,location,altitude,speed,heading,drop,gliderDropHeight,roll,pitch,yaw);
            testArray.add(point);
        }

        return testArray;

    }

    public void updatePlane(Waypoint point,int last,int current) {
        if (planeMarker != null) {
            planeMarker.remove();
        }
        String locat = point.getLocation();
        LatLng location = convertToLatLng(locat);
        Matrix matrix = new Matrix();
        matrix.postRotate((float)point.getYaw());
        Bitmap rotatedBitmap = Bitmap.createBitmap(icon,0,0,icon.getWidth(),icon.getHeight(),matrix,true);
        rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap,40,50,false);
        planeMarker = map.addMarker(new MarkerOptions().position(location).icon(factory.fromBitmap(rotatedBitmap)));

        updatePath(last,current);
    }

    public LatLng convertToLatLng(String locat) {
        String[] split = locat.split(",");
        double latitude = Double.parseDouble(split[0]);
        double longitude = Double.parseDouble(split[1]);
        LatLng location = new LatLng(latitude,longitude);
        return location;
    }

    //Update path for all previous points in case points were skipped
    public void updatePath(int last, int current) {
        if (path != null) {
            path.remove();
        }
        if (current > last) {
            for (int i = last; i <= current; i++) {
                if (showing[i] == 0) {
                    int showVal = 0;
                    if (points.size() > 2 && (points.size() % 10 ==0)) {
                        Bitmap circle = factory.fromResource(R.drawable.black_circle).getBitmap();
                        circle = Bitmap.createScaledBitmap(circle,10,10,false);
                        lastPosition = map.addMarker(new MarkerOptions().position(points.get(points.size()-2)).icon(factory.fromBitmap(circle)));
                        showVal += 1;
                    }
                    Waypoint point = waypoints.get(i);
                    LatLng location = convertToLatLng(point.getLocation());
                    points.add(location);
                    showing[i] = showVal + 1;
                }
            }
        }
        else {
            for (int i = current; i < last; i++) {
                if (showing[i] == 1) {
                    points.remove(i);
                    showing[i] = 0;
                } else if (showing[i] == 2) {
                    lastPosition.remove();
                }
            }
        }
        path = map.addPolyline(new PolylineOptions().addAll(points).color(getResources().getColor(R.color.colorSecondary)).width(3));
    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
    }
}

