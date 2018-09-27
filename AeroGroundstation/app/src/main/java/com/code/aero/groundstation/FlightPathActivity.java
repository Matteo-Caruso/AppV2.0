package com.code.aero.groundstation;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.aerogroundstation.R;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FlightPathActivity extends AppCompatActivity {
    //first jump to current location
    //flight path activity creates new view on the map
    //jump through
    //past list to activity
    boolean isRunning = false;
    int playbackRate = 1000;
    double dropH;
    private Polyline path;
    private MapView mapView;
    private MapboxMap map;
    boolean isPlay = false;
    boolean playMode = false;
    private ImageButton btn;
    Marker m;
    private ArrayList<Waypoint> waypoints;
    ArrayList<LatLng> points;
    Handler handler;
    boolean backwards;
    boolean dropped = false;
    DecimalFormat telemetryFormat = new DecimalFormat("#.00");
    int oldIndex;
    int listIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_path);

        Mapbox.getInstance(this, getString(R.string.access_token));

        //Setting up the MapView
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                replayFlight();
            }
        });

        btn = (ImageButton) findViewById(R.id.play_pause);

        waypoints = (ArrayList<Waypoint>) getIntent().getSerializableExtra("waypoints");
        points = new ArrayList<LatLng>();

        handler = new Handler();

        oldIndex = -1;
        listIndex = 0;

        isRunning = true;


    }
    private void updateData(){

        TextView altView = (TextView) findViewById(R.id.altitudeValue);
        TextView speedView = (TextView) findViewById(R.id.speedValue);
        TextView headingView = (TextView) findViewById(R.id.headingValue);
        TextView dropHeight = (TextView) findViewById(R.id.dropHeightValue);
        TextView rollView = (TextView) findViewById(R.id.rollValue);
        TextView pitchView = (TextView) findViewById(R.id.pitchValue);
        TextView yawView = (TextView) findViewById(R.id.yawValue);

        altView.setText(telemetryFormat.format(waypoints.get(listIndex).getAltitude()) + " ft");
        speedView.setText(telemetryFormat.format(waypoints.get(listIndex).getSpeed()) + " ft/s");
        headingView.setText(telemetryFormat.format(waypoints.get(listIndex).getHeading()) + " deg");

        rollView.setText(telemetryFormat.format(waypoints.get(listIndex).getRoll()) + " deg");
        pitchView.setText(telemetryFormat.format(waypoints.get(listIndex).getPitch()) + " deg");
        yawView.setText(telemetryFormat.format(waypoints.get(listIndex).getYaw()) + " deg");

        if(!dropped){
            dropHeight.setText(telemetryFormat.format(waypoints.get(listIndex).getDrop()) + " ft");
        }
        else{
            dropHeight.setText(telemetryFormat.format(dropH) + " ft");
        }



        if(waypoints.get(listIndex).getDrop() != 0){
            dropped = true;
            dropH = waypoints.get(listIndex).getDrop();
            Log.d("dropc", String.valueOf(waypoints.get(listIndex).getDrop()));
            String LL = waypoints.get(listIndex).getLocation();
            String[] split = LL.split(",");

            Log.d("Locationss", split[0] + " " +split[1] );

            double latitude = Double.parseDouble(split[0]);
            double longitude = Double.parseDouble(split[1]);

            LatLng location = new LatLng(latitude, longitude);

            Marker m = map.addMarker(new MarkerOptions()
                    .position(location));

        }

    }
    private void replayFlight(){
        //display first point
            //Marker at first point
            playbackThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        int action = MotionEventCompat.getActionMasked(ev);

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        //playbackThread.;\
        isRunning = false;
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
        isRunning = false;
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void play_pause_button(View v){

        if(isPlay){
            btn.setImageResource(R.drawable.pause_button);
            playMode = true;

            //Playing automatically
        }else{
            btn.setImageResource(R.drawable.play_button);
            playMode = false;
            //Step through mode
        }

       isPlay = !isPlay; // reverse
    }

    public void backward_button(View v){
        if(playMode){
            return;
        }
        else{
            if(listIndex >= 0){
                backwards = true;
                listIndex -= 1;
            }
        }

    }

    public void forward_button(View v){
        if(playMode){
            return;
        }
        else{
            if(listIndex < waypoints.size()-1){
                listIndex += 1;
            }
        }
    }




    Thread playbackThread = new Thread() {
        @Override
        public void run() {
            if(!isRunning){
                return;
            }

            while (true) {
                try {
                    sleep(playbackRate);
                    //Play mode
                    if(playMode){


                        if(m != null){
                            m.remove();
                        }
                        if(path != null){
                            path.remove();
                        }

                        String LL = waypoints.get(listIndex).getLocation();
                        String[] split = LL.split(",");

                        Log.d("Locationss", split[0] + " " +split[1] );

                        double latitude = Double.parseDouble(split[0]);
                        double longitude = Double.parseDouble(split[1]);

                        LatLng location = new LatLng(latitude, longitude);

                        IconFactory factory = IconFactory.getInstance(FlightPathActivity.this);

                        Icon icon = factory.fromResource(R.drawable.ic_plane);


                        m = map.addMarker(new MarkerOptions()
                                .position(location).icon(icon));

                        if(listIndex < waypoints.size()-1){
                            listIndex += 1;
                        }

                        points.add(location);

                        path = map.addPolyline(new PolylineOptions()
                                .addAll(points)
                                .color(Color.parseColor("#3bb2d0"))
                                .width(3));

                    }
                    else{
                        //sleep(playbackRate);
                        if(oldIndex != listIndex){

                            if(m != null){
                                m.remove();
                            }
                            if(path != null){
                                path.remove();
                            }

                            String LL = waypoints.get(listIndex).getLocation();
                            String[] split = LL.split(",");

                            Log.d("Locationss", split[0] + " " +split[1] );

                            double latitude = Double.parseDouble(split[0]);
                            double longitude = Double.parseDouble(split[1]);

                            LatLng location = new LatLng(latitude, longitude);

                            IconFactory factory = IconFactory.getInstance(FlightPathActivity.this);

                            Icon icon = factory.fromResource(R.drawable.ic_plane);

                            //Marker m;
                            m = map.addMarker(new MarkerOptions()
                                    .position(location).icon(icon));
                            if(backwards){
                                points.remove(points.size()-1);
                                backwards = false;
                            }
                            else{
                                points.add(location);
                            }

                            path = map.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .color(Color.parseColor("#3bb2d0"))
                                    .width(3));


                            oldIndex = listIndex;
                        }


                        //remove old marker if old index was different
                        //createMarker at index if old index was different
                    }
                    //Add waypoint to database
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isRunning) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateData();

                        }
                    });

                }


            }

        }
    };
}
