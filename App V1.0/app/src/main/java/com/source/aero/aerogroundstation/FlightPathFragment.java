package com.source.aero.aerogroundstation;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.gestures.RotateGestureDetector;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import static java.lang.Thread.sleep;

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
    int[] showing; //0 = not showing, 1 = passed point (in polyline), 2 = passed point and showing black circle
    int currentPoint = 0;
    int lastPoint = 0;
    Bundle data;
    boolean endPath = false;
    boolean startPath = true;
    boolean running = false;
    boolean playMode = false;

    Handler handler;

    int playbackRate = 1000;

    Thread thread;

    ArrayList<Marker> intermediatePoints;



    public FlightPathFragment() {
        //Empty constructor
    }

    //Required methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getActivity(),getResources().getString(R.string.mapboxToken));
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                boolean run = (boolean) message.obj;
            }
        };
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
        data = getArguments();
        //Retrieve data sent from activity
        try {
            waypoints = (ArrayList<Waypoint>) data.getSerializable("WAYPOINTS");
            //Test data
            //waypoints = populate();
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
        intermediatePoints = new ArrayList<Marker>();
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
        if (playMode) {
            playButton.setImageResource(R.drawable.ic_play);
            playMode = false;
            thread.interrupt();
        }
        else {
            points.clear();
            //Remove intermediate points (marked by black circles)
            Iterator<Marker> removePoints = intermediatePoints.iterator();
            while (removePoints.hasNext()) {
                Marker temp = removePoints.next();
                temp.remove();
                removePoints.remove();
            }
            //Resetting path
            showing = new int[waypoints.size()];
            Arrays.fill(showing,0);
            updateData(waypoints.get(0));
            currentPoint = 0;
            lastPoint = 0;
            endPath = false;
            playButton.setImageResource(R.drawable.ic_pause_symbol);
            playMode = true;
            thread = new playThread();
            thread.start();
        }
    }

    public void forward() {
        if (currentPoint == waypoints.size()-1) {
            endPath = true;
        }
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

    //move marker backwards
    public void backward() {
        if (currentPoint == 0) {
            startPath = true;
        }
        if (currentPoint > 0) {
            currentPoint -= 1;
            updateData(waypoints.get(currentPoint));
            updatePlane(waypoints.get(currentPoint),lastPoint,currentPoint);
            lastPoint = currentPoint;
        } else {
            Toast.makeText(getActivity(),"Start of path reached",Toast.LENGTH_SHORT).show();
        }
    }

    //Update plane marker
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
        updatePath(last,current);
        planeMarker = map.addMarker(new MarkerOptions().position(location).icon(factory.fromBitmap(rotatedBitmap)));
    }

    //Convert string location to LatLng
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
                        intermediatePoints.add(lastPosition);
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
            for (int i = current + 1 ; i <= last; i++) {
                if (showing[i] == 1) {
                    showing[i] = 0;
                }
                 if (showing[i] == 2) {
                    map.removeMarker(lastPosition);
                    showing[i] = 0;
                }
                points.remove(i);
            }
        }
        path = map.addPolyline(new PolylineOptions().addAll(points).color(getResources().getColor(R.color.colorSecondary)).width(3));
    }

    //Thread implements delay in between updating the position of the marker
    //Actual UI changes are made on the UI thread since only the main activity thread can access the UI
    private class playThread extends Thread {
        private static final String TAG = "Play Thread";
        @Override
        public void run() {
            Log.d(TAG,"Running thread");
            //Run forward until the end of the path is reached
            while (!endPath) {
                try {
                    sleep(playbackRate);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            forward();
                        }
                    });
                }catch (InterruptedException e) {
                    return;
                }

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        if (thread != null) {
            thread.interrupt();
        }
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        if (thread != null) {
            thread.interrupt();
        }
        Toast.makeText(getActivity(),"Memory resources low, stopping playback",Toast.LENGTH_SHORT).show();
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        if (thread != null) {
            thread.interrupt();
        }
        super.onDestroy();
    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
    }
}

