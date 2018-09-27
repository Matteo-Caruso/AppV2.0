package com.code.aero.groundstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserInterfaceActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener {

    //Global_context variable
    private Context global_context;
    boolean isRunning = false;
    String oldSessionName;


    //Check points
    CheckBox cFL, cFR, cBL, cBR, cDoors;

    //Thread execution frequency in MS
    int refreshRate = 1000;

    protected Marker planeMarker;
    protected Marker lastPosition;
    protected Polyline planePath;

    protected LatLng planePoint;
    String planePointString;
    protected int wayPointCount = 0;
    protected int droppedCount = 0;
    protected boolean dropped = false;


    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private android.location.Location originLocation;


    //Stores formatted string for the date
    protected String recordingSession;
    protected boolean recordingMode = false;
    protected boolean sessionCreated = false;

    //Temp until Jai creates getSessionIDs function
    protected ArrayList<String> sessions;

    //Plane Data
    protected float plane_lat = (float)0.0;
    protected float plane_long = (float)0.0;
    protected float plane_height = (float)0.0;
    protected float plane_roll = (float)0.0;
    protected float plane_pitch = (float)0.0;
    protected float plane_heading = (float)0.0;
    protected float plane_speed = (float)0.0;
    protected float plane_drop_time = (float)0.0;
    protected float plane_drop_heading = (float)0.0;
    protected float plane_drop_height = (float)0.0;

    protected  double targetLat = 27.97816585;
    protected double targetLong = -82.02474327;
    int waypointID;
    ArrayList<LatLng> points;

    TextView tv[];

    Handler handler;

    //UI Elements
    private DrawerLayout slidingMenuDrawer;
    private ActionBarDrawerToggle slidingMenuToggleButton;
    private TextView myDisplayButton;
    protected IconFactory factory;
    protected Bitmap icon;
    protected Bitmap target;

    protected int oldSize = 0;

    //Map
    private MapView mapView;
    private MapboxMap map;

    //Database
    private String testDatabaseName = "ttDB";
    private String dataDatabaseName = "rrDB";
    private DatabaseHelper testDB;
    private DatabaseHelper flightDB;

    private ProgressBar progressBar;
    private Button downloadButton;
    private Button listButton;

    private boolean isEndNotified;
    private int regionSelected;

    private static final String TAG = "OffManActivity";

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";


    // Offline objects
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;

    private ToggleButton dropToggle;

    //Radio Setup. Can use either RadioCommClass
    AbstractRadio radio;
    char mode = 'T';
    FragmentTransaction fragmentTransaction;

    DecimalFormat telemetryFormat = new DecimalFormat("#.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Points for creating polyline
        points = new ArrayList<>();

        //Creating handler for thread
        handler = new Handler();

        //Loading XML
        setContentView(R.layout.activity_user_interface);

        //Loading access token for mapbox
        Mapbox.getInstance(this, getString(R.string.access_token));

        //Setting up the MapView
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                enableLocationPlugin();
            }
        });

        //Creating Factory and Icon ONCE to avoid lag in updatePlane()
        factory = IconFactory.getInstance(UserInterfaceActivity.this);
        icon = factory.fromResource(R.drawable.ic_plane).getBitmap();


        //Sliding Menu
        slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);
        slidingMenuToggleButton = new ActionBarDrawerToggle(this, slidingMenuDrawer, R.string.open, R.string.close);

        slidingMenuDrawer.addDrawerListener(slidingMenuToggleButton);
        slidingMenuToggleButton.syncState();

        //Creating databases
        testDB = new DatabaseHelper(this, testDatabaseName);
        flightDB = new DatabaseHelper(this, dataDatabaseName);

        //testDB.flushAll();
        //flightDB.flushAll();

        //Variables used for storing differing flight path tables
        waypointID = 0;
        sessions = new ArrayList<String>();

        //Create radio. Should work for both test radio and regular radio
        if(radio == null && mode == 'T'){
            radio = new TestRadio(this);
        }

        if(radio == null && mode == 'R'){
            radio = new Radio(this);
            radio.setUpUsbIfNeeded();
        }




        cFL = (CheckBox) findViewById(R.id.payloadFL);
        cFR = (CheckBox) findViewById(R.id.payloadFR);
        cBL = (CheckBox) findViewById(R.id.payloadBL);
        cBR = (CheckBox) findViewById(R.id.payloadBR);
        cDoors = (CheckBox) findViewById(R.id.payloadDoors);

        // Assign progressBar for later use
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(this);

        // Bottom navigation bar button clicks are handled here.
        // Download offline button
        downloadButton = (Button) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadRegionDialog();
            }
        });

        // List offline regions
        listButton = (Button) findViewById(R.id.list_button);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadedRegionList();
            }
        });

        dropToggle = (ToggleButton) findViewById(R.id.autoDropToggle);


        //Storing context
        global_context = this;

    }



    @Override
    public boolean onTouchEvent(MotionEvent ev){
        int action = MotionEventCompat.getActionMasked(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(slidingMenuToggleButton.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        radio.startThread();
        isRunning = true;
        mapView.onResume();
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
        isRunning = false;
        mapView.onDestroy();

        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
        mapView.onPause();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    //Data Output
    public void updateData(){


        Log.d("UI", "updateData");
        TextView altView = (TextView) findViewById(R.id.currentHeight);

        TextView dropHeightView = (TextView) findViewById(R.id.dropHeight);

        TextView headingView = (TextView) findViewById(R.id.headingValue);
        TextView rollView = (TextView) findViewById(R.id.rollValue);
        TextView pitchView = (TextView) findViewById(R.id.pitchValue);

        TextView speedView = (TextView) findViewById(R.id.speedValue);

        TextView timeDropView = (TextView) findViewById(R.id.TimeToDropValue);

        TextView headingDropView = (TextView) findViewById(R.id.HeadingToDropValue);




        altView.setText(telemetryFormat.format(plane_height) + " ft");
        dropHeightView.setText(telemetryFormat.format(plane_drop_height) + " ft");
        headingView.setText(telemetryFormat.format(plane_heading) + " deg");
        rollView.setText(telemetryFormat.format(plane_roll) + " deg");
        pitchView.setText(telemetryFormat.format(plane_pitch) + " deg");
        speedView.setText(telemetryFormat.format(plane_speed) + " ft/s");
        timeDropView.setText(telemetryFormat.format(plane_drop_time) + " s");
        headingDropView.setText(telemetryFormat.format(plane_drop_heading) + " deg");

        //Toast.makeText(global_context, "madeUI", Toast.LENGTH_SHORT).show();



    }

    //Update Plan Position
    public void updatePlane() {

        //Remove if old marker exists
        if (planeMarker != null) {
            planeMarker.remove();
        }

        Log.d("UI", "planeUpdate");

        //Rotating Plane with matrix
        Matrix matrix = new Matrix();
        matrix.postRotate((float)plane_drop_heading);
        Bitmap rotatedBitmap = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
        rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 40, 50, false);

        //Plane Marker
        planeMarker = map.addMarker(new MarkerOptions()
                .position(planePoint)
                .icon(factory.fromBitmap(rotatedBitmap)));

        //Past Position Markers
        if(points.size() > 2){

            Bitmap circle = factory.fromResource(R.drawable.black_circle).getBitmap();
            circle = Bitmap.createScaledBitmap(circle, 10, 10, false);

            lastPosition = map.addMarker(new MarkerOptions()
                    .position(points.get(points.size()-2))
                    .icon(factory.fromBitmap(circle)));
        }


        planePath = map.addPolyline(new PolylineOptions()
                .addAll(points)
                .color(Color.parseColor("#3bb2d0"))
                .width(3));

        //From old code, leaving in till we deal with drop
        wayPointCount++;

        if (dropped) {
            droppedCount = wayPointCount;
        }

        //Toast.makeText(global_context, "madePlane", Toast.LENGTH_SHORT).show();
    }

    //Test for clearing screen
    public void clearMarkers(){
        Log.d("UI", "clearMarkers");
        lastPosition.remove();
        planeMarker.remove();
    }

    //Test for clearing path
    public void clearPath(){
        Log.d("UI", "clearPath");
        planePath.remove();
    }

    /*---------------------------------- BUTTON CLICK METHODS ----------------------------------------*/

    //Used to display the sub menu under the display button when it is clicked. This is done by adjusting its visibility.
    //For visual effect, the background colour is also adjusted
    public void displayTextClick(View v) {
        Log.d("UI", "displayTextClick");

        myDisplayButton = (TextView) findViewById(R.id.displayButton);
        LinearLayout myDisplayList = (LinearLayout) findViewById(R.id.displayList);

        if (myDisplayList.getVisibility() == View.INVISIBLE) {
            myDisplayList.setVisibility(View.VISIBLE);
            myDisplayButton.setBackgroundColor(Color.rgb(154, 154, 154));
        } else {
            myDisplayList.setVisibility(View.INVISIBLE);
            myDisplayButton.setBackgroundColor(Color.rgb(225, 225, 225));
        }
    }

    public void payloadClicked(View v) {
        Log.d("UI", "payloadClicked");

        myDisplayButton = (TextView) findViewById(R.id.payLoadControls);
        LinearLayout myPayloadList = (LinearLayout) findViewById(R.id.payloadList);

        if (myPayloadList.getVisibility() == View.INVISIBLE) {
            myPayloadList.setVisibility(View.VISIBLE);
            myDisplayButton.setBackgroundColor(Color.rgb(154, 154, 154));
        } else {
            myPayloadList.setVisibility(View.INVISIBLE);
            myDisplayButton.setBackgroundColor(Color.rgb(225, 225, 225));
        }
    }

    //This method is called when the target button is clicked in the menu. Currently it changes the colour of the background.
    //ColorDrawable is needed to access the current background colour of the button.
    public void targetTextClick(View v) {
        final List<Target> sessionList = testDB.getTargets();

        myDisplayButton = (TextView) findViewById(R.id.targetButton);

        slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);

        //Get the background colour of the textview
        ColorDrawable backgroundColour = (ColorDrawable) myDisplayButton.getBackground();

        tv = new TextView[sessionList.size()];
        LinearLayout linearLayout = findViewById(R.id.Right_Menu);

        //Upon selection of flight path, display two more text views with a radio button and test button
        //Create an on click listener for each of the new views
        if (backgroundColour.getColor() == Color.rgb(225, 225, 225)) {

            Log.d("UI", "target" + String.valueOf(sessionList.size()));
            myDisplayButton.setBackgroundColor(Color.rgb(154, 154, 154));

            TextView secondayMenuText = (TextView) findViewById(R.id.secondaryMenuTitle);

            //Identify desired layout parameters for the textview such that it fits in parent container
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            if (sessionList.size() > 0) {


                for (int i = sessionList.size() - 1; i >= 0; i--) {
                    final String sessionName = sessionList.get(i).getName();
                    final String sessionLocation = sessionList.get(i).getLocation();



                    tv[i] = new TextView(this);

                    tv[i].setText(sessionList.get(i).getName());
                    tv[i].setId(i);
                    tv[i].setTag("Text" + i);
                    tv[i].setClickable(true);
                    tv[i].setLayoutParams(params);
                    tv[i].setPadding(50, 25,0, 25);




                    tv[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Handle on click
                            Log.d("sL", String.valueOf(5));

                            String[] split = sessionLocation.split(",");

                            double latitude = Double.parseDouble(split[0]);
                            double longitude = Double.parseDouble(split[1]);


                            Location target = new Location("");
                            target.setLatitude(latitude);
                            target.setLongitude(longitude);
                            setCameraPosition(target);

                            originLocation = target;

                            //Set origin location to target location
                            //Send to teensy with set current
                        }
                    });
                    linearLayout.addView(tv[i], 2);



                }



            }

        }else{

            myDisplayButton.setBackgroundColor(Color.rgb(225, 225, 225));
            ViewGroup layout = (ViewGroup) findViewById(R.id.Right_Menu);

            for (int i = sessionList.size() - 1; i >= 0; i--) {
                linearLayout.removeView(linearLayout.findViewWithTag("Text" + i));
            }

        }


    }

    //This method is called when the flight path button is clicked in the menu. Currently it changes the colour of the background.
    //Additionally, when the button is clicked, if the button is the same colour as the default background, then we dynamically
    //add two additional textviews for other options that the user desires (radio and test). These are inserted in the desired location under the Flight Path text view
    public void flightPathTextClick(View v) {
        List<String> sessionList = testDB.getFlightSessions();

        myDisplayButton = (TextView) findViewById(R.id.flightPathButton);

        slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);

        //Get the background colour of the textview
        ColorDrawable backgroundColour = (ColorDrawable) myDisplayButton.getBackground();
        tv = new TextView[sessionList.size()];
        LinearLayout linearLayout = findViewById(R.id.Right_Menu);
        //Upon selection of flight path, display two more text views with a radio button and test button
        //Create an on click listener for each of the new views
        if (backgroundColour.getColor() == Color.rgb(225, 225, 225)) {

            Log.d("UI", "flightPathTextClick" + String.valueOf(sessionList.size()));
            myDisplayButton.setBackgroundColor(Color.rgb(154, 154, 154));

            TextView secondayMenuText = (TextView) findViewById(R.id.secondaryMenuTitle);

            //Identify desired layout parameters for the textview such that it fits in parent container
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            if (sessionList.size() > 0) {


                for (int i = sessionList.size() - 1; i >= 0; i--) {
                    final String sessionName = sessionList.get(i);

                   // if(sessionName != oldSessionName){
                        tv[i] = new TextView(this);

                        //CREATE SESSION 2 FILE
//                        try{
//                            boolean sessionCreator = testDB.session2file(sessionList.get(i));
//                        }catch(Exception c){
//                            c.printStackTrace();
//
//                        }


                        tv[i].setText(sessionList.get(i));
                        tv[i].setId(i);
                        tv[i].setTag("Text" + i);
                        tv[i].setLayoutParams(params);
                        tv[i].setPadding(50, 25,0, 25);





                        tv[i].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(UserInterfaceActivity.this, FlightPathActivity.class);

                                //Session is the name of the flight path we want to go through
                                List<Waypoint> waypoints = testDB.getWaypoints(sessionName);

                                //Log.d("Waypoints", waypoints.get(0).getName() + " " + waypoints.get(0).getLocation());
                                ArrayList<Waypoint> intentList = new ArrayList<Waypoint>(waypoints);

                                intent.putExtra("waypoints", intentList);
                                isRunning = false;
                                radio.stopThread();
                                UserInterfaceActivity.this.startActivity(intent);
                            }
                        });

                        linearLayout.addView(tv[i], 3);

                        //oldSessionName = sessionName;
                    //}


                }



            }

        }else{

            myDisplayButton.setBackgroundColor(Color.rgb(225, 225, 225));
            ViewGroup layout = (ViewGroup) findViewById(R.id.Right_Menu);

            for (int i = sessionList.size() - 1; i >= 0; i--)  {
                linearLayout.removeView(linearLayout.findViewWithTag("Text" + i));
            }

        }
    }

            //This method is called when the target button is clicked in the menu. Currently it changes the colour of the background.
    public void videoTextClick(View v) {
        Log.d("UI", "videoTextClick");

        myDisplayButton = (TextView) findViewById(R.id.videoButton);

        slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);

        TextView secondaryMenuText = (TextView) findViewById(R.id.secondaryMenuTitle);

        slidingMenuDrawer.closeDrawer(Gravity.END);
        slidingMenuDrawer.openDrawer(Gravity.START);
        secondaryMenuText.setText("Video");

    }

    //This method is called when the horizon button is clicked in the menu. WHen clicked, a new activity is displayed.
    public void horizonTextClick(View v) {
        Log.d("UI", "horizonTextClick");
        radio.closeDevice();
        Intent intent = new Intent(UserInterfaceActivity.this, HorizonActivity.class);
        UserInterfaceActivity.this.startActivity(intent);

//        Intent intent2 = new Intent(UserInterfaceActivity.this, HorizonActivity.class);
//        pointDb.close();
//        startActivityForResult(intent2, 2);
//        slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);
//        TextView secondaryMenuText = (TextView) findViewById(R.id.secondaryMenuTitle);
//        slidingMenuDrawer.closeDrawer(Gravity.END);
//        slidingMenuDrawer.openDrawer(Gravity.START);
//        secondaryMenuText.setText("Horizon");
//        /*myDisplayButton = (TextView) findViewById(R.id.horizonButton);
//        ColorDrawable backgroundColour = (ColorDrawable) myDisplayButton.getBackground();
//        if(backgroundColour.getColor() == Color.rgb(225, 225, 225)){
//            myDisplayButton.setBackgroundColor(Color.rgb(154, 154, 154));
//        }
//        else{
//            myDisplayButton.setBackgroundColor(Color.rgb(225, 225, 225));
//        }

    }

    //Remove the add flight button when click back button in left menu
    public void backArrowClick(View v) {
        Log.d("UI", "backArrowClick");
        slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);

        TextView secondaryMenuText = (TextView) findViewById(R.id.secondaryMenuTitle);

        LinearLayout leftMenu = (LinearLayout) findViewById(R.id.Left_Menu);

        TextView addButton = (TextView)findViewById(R.id.newFlightButton);
        leftMenu.removeView(addButton);

        slidingMenuDrawer.closeDrawer(Gravity.START);
        slidingMenuDrawer.openDrawer(Gravity.END);
        secondaryMenuText.setText("Menu");
    }

    //Connecting to the radio
    public void radioConnect(View v){
        Log.d("UI", "radioConnect");
        LatLng targetPoint = new LatLng(targetLat, targetLong);
        Marker m = map.addMarker(new MarkerOptions()
                .position(targetPoint));
        radio.setUpUsbIfNeeded();




        if(!dataOutputThread.isAlive()) {

            isRunning = true;
            //Toast.makeText(global_context, "threader", Toast.LENGTH_SHORT).show();
                dataOutputThread.start();


        }


        //Toast.makeText(this, "Connecting", Toast.LENGTH_SHORT).show();

    }
    /*---------------------------------- On Click Listener Methods Used to dynamically add button functionality ----------------------------------------*/

//    View.OnClickListener radioFlightListener = new View.OnClickListener() {
//
//        public void onClick(View v) {
//            Log.d("UI", "radioFlightListener");
//
//            if(sessions.size() > 0){
//                Intent intent = new Intent(UserInterfaceActivity.this, FlightPathActivity.class);
//                String sessionToPass = new String();
//                Log.d("Session", sessions.get(0));
//                Log.d("Current", recordingSession);
//
//                //Session is the name of the flight path we want to go through
//                List<Waypoint> waypoints = testDB.getWaypoints(recordingSession);
//                List<Target> targets = testDB.getTargets();
//
//
//                //Log.d("Waypoints", waypoints.get(0).getName() + " " + waypoints.get(0).getLocation());
//                ArrayList<Waypoint> intentList = new ArrayList<Waypoint>(waypoints);
//
//                intent.putExtra("waypoints", intentList);
//                isRunning = false;
//                radio.stopThread();
//                UserInterfaceActivity.this.startActivity(intent);
//
//            }
//        }
//    };

//    View.OnClickListener testFlightListener = new View.OnClickListener() {
//
//        public void onClick(View v) {
//            Log.d("UI", "testFlightListener");
//
//            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.Left_Menu);
//
//            slidingMenuDrawer = (DrawerLayout) findViewById(R.id.slidingMenuLayout);
//
//            TextView secondaryMenuText = (TextView) findViewById(R.id.secondaryMenuTitle);
//
//            //Get radio icon drawable and scale it to an appropriate size
//            Drawable plusButton = getResources().getDrawable( R.drawable.plus_sign);
//            plusButton.setBounds( 0, 0, 60, 60 );
//
//            //Identify desired layout parameters for the textview such that it fits in parent container
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//
//            //Create add flight text view
//            TextView addFlight = new TextView(UserInterfaceActivity.this);
//            addFlight.setText("Add Flight");
//            addFlight.setId(R.id.newFlightButton);
//            addFlight.setBackgroundColor(Color.rgb(200,200,200));
//            addFlight.setTextSize(25);
//            addFlight.setCompoundDrawables(plusButton, null, null, null);
//            addFlight.setCompoundDrawablePadding(70);
//            addFlight.setPadding(150,50,0,50);
//            addFlight.setLayoutParams(params);
//            addFlight.setOnClickListener(radioFlightListener);
//
//            relativeLayout.addView(addFlight, 1);
//
//            slidingMenuDrawer.closeDrawer(Gravity.END);
//            slidingMenuDrawer.openDrawer(Gravity.START);
//            secondaryMenuText.setText("Test");
//
//        }
//    };


    //------------------------------------ Below is the method used to display Radio IO using the checkboxes ---------------------------------------------------

    public void checkBoxClicked(View v){

        //Interpret the current view that is passed in the function call as a checkbox
        CheckBox checkBoxSelected = (CheckBox) v;

        //Toggle the Heading IO label depending ion whether the respective checkbox is checked
        if (checkBoxSelected.getId() == R.id.checkBoxHeading){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioIOHeading);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }

        //Toggle the Heading IO label depending ion whether the respective checkbox is checked
        if (checkBoxSelected.getId() == R.id.checkBoxHeight){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioIOHeight);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }

        //Toggle the Roll IO label depending ion whether the respective checkbox is checked
        else if (checkBoxSelected.getId() == R.id.checkBoxRoll){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioIORoll);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }

        //Toggle the Pitch IO label depending ion whether the respective checkbox is checked
        else if (checkBoxSelected.getId() == R.id.checkBoxPitch){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioIOPitch);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }

        //Toggle the Height IO label depending ion whether the respective checkbox is checked
        else if (checkBoxSelected.getId() == R.id.checkBoxDropHeight){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioIODropHeight);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }





        else if (checkBoxSelected.getId() == R.id.checkboxGroundSpeed){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioSpeed);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }

        else if (checkBoxSelected.getId() == R.id.checkboxDropTime){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioTimeToDrop);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }

        else if (checkBoxSelected.getId() == R.id.checkboxHeadingTime){

            LinearLayout toggleVisibilitiyRadioIO = (LinearLayout) findViewById(R.id.radioHeadingToDrop);

            if(checkBoxSelected.isChecked() == true){
                toggleVisibilitiyRadioIO.setVisibility(View.VISIBLE);
            }

            else {
                toggleVisibilitiyRadioIO.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void payloadTextClicked(View v){

        //Interpret the current view that is passed in the function call as a checkbox
        CheckBox checkBoxSelected = (CheckBox) v;

        if (checkBoxSelected.getId() == R.id.payloadFL){

            if(cFL.isChecked()){
                Toast.makeText(global_context, "Loading FL", Toast.LENGTH_SHORT).show();
                radio.sendPayloadCommand("FL");
            }
            else{
                Toast.makeText(global_context, "Unloading FL", Toast.LENGTH_SHORT).show();
                radio.sendUnloadCommand("FL");
            }

            //SEND MESSAGE

        }

        else if (checkBoxSelected.getId() == R.id.payloadFR){

            if(cFR.isChecked()){
                Toast.makeText(global_context, "Loading FR", Toast.LENGTH_SHORT).show();
                radio.sendPayloadCommand("FR");
            }
            else{
                Toast.makeText(global_context, "Unloading FR", Toast.LENGTH_SHORT).show();
                radio.sendUnloadCommand("FR");
            }
        }

        else if (checkBoxSelected.getId() == R.id.payloadBL){

            if(cBL.isChecked()){
                Toast.makeText(global_context, "Loading BL", Toast.LENGTH_SHORT).show();
                radio.sendPayloadCommand("BL");
            }
            else{
                Toast.makeText(global_context, "Unloading BL", Toast.LENGTH_SHORT).show();
                radio.sendUnloadCommand("BL");
            }
        }

        else if (checkBoxSelected.getId() == R.id.payloadBR){

            if(cBR.isChecked()){
                Toast.makeText(global_context, "Loading BR", Toast.LENGTH_SHORT).show();
                radio.sendPayloadCommand("BR");
            }
            else{
                Toast.makeText(global_context, "Unloading BR", Toast.LENGTH_SHORT).show();
                radio.sendUnloadCommand("BR");
            }
        }

        else if (checkBoxSelected.getId() == R.id.payloadDoors){

            if(cDoors.isChecked()){
                Toast.makeText(global_context, "Payload Doors Opening", Toast.LENGTH_SHORT).show();
                radio.sendDoorCommand("DO");
            }
            else{
                Toast.makeText(global_context, "Payload Doors Closing", Toast.LENGTH_SHORT).show();
                radio.sendDoorCommand("DC");
            }
        }
    }

    public void zeroGPS(View v){
        radio.sendZeroCommand("G");
    }

    public void zeroBaro(View v){
        radio.sendZeroCommand("B");
    }


    //----------------------------------------------- Below are the implementations for clicking the floating buttons at the bottom of the screen ------------------------------------

    public void dropPayloadButtonClick(View v) {
        Log.d("UI", "dropPayloadButtonClick");
        Toast.makeText(global_context, "Payload", Toast.LENGTH_SHORT).show();

        if(cFL.isChecked()){
            cFL.toggle();
        }
        if(cFR.isChecked()){
            cFR.toggle();
        }
        if(cBL.isChecked()){
            cBL.toggle();
        }
        if(cBR.isChecked()){
            cBR.toggle();
        }

        if(!cDoors.isChecked()){
            cDoors.toggle();
        }

        radio.sendDropCommand();
        dropped = true;
        Marker m = map.addMarker(new MarkerOptions()
                    .position(planePoint));
        //plane_drop_height = plane_height;
//        if(recordingMode){
//            dropped = true;
//
//            testDB.addWaypoint(recordingSession, waypointID, planePointString, plane_height, plane_speed, plane_heading, (int) plane_drop_height);
//        }

    }



    @SuppressWarnings( {"MissingPermission"})
    public void currentLocationButtonClick(View v){
        Log.d("UI", "currentLocationButtonClick");
        Toast.makeText(global_context, "Location", Toast.LENGTH_SHORT).show();
        locationEngine.requestLocationUpdates();
        setCameraPosition(originLocation);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText regionNameEdit = new EditText(this);
        regionNameEdit.setHint("Enter Target ID");

        // Build the dialog box
        builder.setTitle("Save Location")
                .setView(regionNameEdit)
                .setMessage("Saves Location as Target")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String targetName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (targetName.length() == 0) {
                            Toast.makeText(UserInterfaceActivity.this, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            // Begin download process
                            testDB.addTarget(targetName, String.valueOf(originLocation.getLatitude()) + "," + String.valueOf(originLocation.getLongitude()));
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Display the dialog
        builder.show();
        //myLocation = new LocationServiceActivity();
        //myLocation.onLocationChanged(myLocation.getOriginLocation());

//        myLocation = new Location();
//        fragmentTransaction.add(R.id.location, myLocation);
//        fragmentTransaction.commit();

        //Intent intent = new Intent(UserInterfaceActivity.this, LocationServiceActivity.class);
        //UserInterfaceActivity.this.startActivity(intent);

    }

    public void setCurrent(View v){
        Log.d("UI", "set Current");
        radio.sendTarget((float)originLocation.getLatitude(), (float)originLocation.getLongitude());
    }

    public void autoDrop(View v){
        if(dropToggle.isChecked()) {
            Toast.makeText(global_context, "Auto EN", Toast.LENGTH_SHORT).show();
            Toast.makeText(global_context, "Auto EN", Toast.LENGTH_SHORT).show();
            radio.sendAutoMode(true);
        } else {
            Toast.makeText(global_context, "Auto DIS", Toast.LENGTH_SHORT).show();
            radio.sendAutoMode(false);
        }
    }

    //------------------------------------------------- Below is the implementation for when the record button is clicked -----------------------------------------------------------

    public void recordButtonClicked(View v){
        Log.d("UI", "recordButtonClicked");

        //Below is the GUI interaction to change status of objects, based on when the button is pressed
        Button recordButton = (Button) v;
        LinearLayout messageColour = (LinearLayout) findViewById(R.id.recordingIdentifier);
        TextView messageRecord = (TextView) findViewById(R.id.recordingText);

        if (messageRecord.getText().equals("Not Recording")){
            recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_border3));
            messageColour.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_border3));
            messageRecord.setText("Recording");

            //create new session ID
            recordingSession = new Date().toString();
            recordingMode = true;
            waypointID = 0;

        }

        else{
            recordButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_border2));
            messageColour.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_border2));
            messageRecord.setText("Not Recording");
            Toast.makeText(this, "Data being saved", Toast.LENGTH_SHORT).show();
            recordingMode = false;
            sessionCreated = false;
        }
        //Below, the implementation of the record button can be implemented
    }

//Thread dataOutput = new Thread(){
//    @Override
//    public void run() {
//        Toast.makeText(global_context, "threader", Toast.LENGTH_SHORT).show();
//        if(!isRunning){
//            return;
//        }
//
//        while(true){
//            try{
//                sleep(refreshRate);
//                Toast.makeText(global_context, "threader", Toast.LENGTH_SHORT).show();
//                //Grabbing data from radio. Might make function to store data in array
//                float telemetry[] = radio.getTelemetry();
//                Toast.makeText(global_context, "2", Toast.LENGTH_SHORT).show();
//                //plane_lat = telemetry[0];
//                //plane_long = telemetry[1];
//
//                plane_lat = telemetry[0];
//                plane_long = telemetry[1];
//
//                plane_height = telemetry[2];
//                plane_roll = telemetry[3];
//                plane_pitch = telemetry[4];
//                plane_heading = telemetry[5];
//                plane_speed = telemetry[6];
//                plane_drop_time = telemetry[7];
//                plane_drop_heading = telemetry[8];
//                plane_drop_height = telemetry[9];
//
//                //Create LatLng and store it in a list of all points
//                try{
//                    planePoint = new LatLng((double) plane_lat, (double) plane_long);
//                    points.add(planePoint);
//                }catch (Exception e){
//
//                }
//
//                planePointString = String.valueOf(plane_lat) + "," + String.valueOf(plane_long);
//            }catch(InterruptedException e){
//                e.printStackTrace();
//            }
//            if(isRunning){
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//
//
//
//                    }
//                });
//            }
//
//        }
//}
    Thread dataOutputThread = new Thread() {

        @Override
        public void run() {
//            Toast.makeText(global_context, "threader", Toast.LENGTH_SHORT).show();
//            if(!isRunning){
//                return;
//            }

            while (true) {
                if(!isRunning){
                    break;
                }

                try {
                    sleep(refreshRate);
                    //Toast.makeText(global_context, "threader", Toast.LENGTH_SHORT).show();
                    //Grabbing data from radio. Might make function to store data in array
                    float telemetry[] = radio.getTelemetry();
                    //Toast.makeText(global_context, "2", Toast.LENGTH_SHORT).show();
                    //plane_lat = telemetry[0];
                    //plane_long = telemetry[1];

                    plane_lat = telemetry[0];
                    plane_long = telemetry[1];

                    plane_height = telemetry[2];
                    plane_roll = telemetry[3];
                    plane_pitch = telemetry[4];
                    plane_heading = telemetry[5];
                    plane_speed = telemetry[6];
                    plane_drop_time = telemetry[7];
                    plane_drop_heading = telemetry[8];
                    plane_drop_height = telemetry[9];

                    //Create LatLng and store it in a list of all points
                    try{
                        planePoint = new LatLng((double) plane_lat, (double) plane_long);
                        points.add(planePoint);
                    }catch (Exception e){

                    }

                    planePointString = String.valueOf(plane_lat) + "," + String.valueOf(plane_long);




                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                if(isRunning){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Update UI
                            updateData();
                            updatePlane();

                            //Add waypoint to database
                            if(recordingMode){

                                if(!sessionCreated){
                                    Date currentTime = new Date();
                                    //;Date formattedDate = new Date();
                                    recordingSession = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss_z").format(currentTime);
                                    Log.d("Formatted", recordingSession);
                                    sessionCreated = true;
                                }
                                else{
                                    waypointID += 1;
                                    boolean g;
                                    if(dropped){
                                         g =testDB.addWaypoint(recordingSession, waypointID, planePointString, plane_height, plane_speed, plane_heading, plane_drop_height, plane_roll, plane_pitch, plane_heading);
                                        dropped = false;
                                    }
                                    else{
                                         g=testDB.addWaypoint(recordingSession, waypointID, planePointString, plane_height, plane_speed, plane_heading, 0, plane_roll, plane_pitch, plane_heading);
                                    }

                                    Log.d("Waypointc", String.valueOf(g));



                                }

                                }

                            }
                        });
                    }
                }

            }

        };





    // LOCATION SERVICES
    //@Override
    public void onPermissionResult(boolean granted) {
        Log.e("Location", "Permission Result");
        if (granted) {
            enableLocationPlugin();
        } else {
            this.finish();
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
        locationEngine = new LostLocationEngine(this);
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

    private void setCameraPosition(double lat, double lon) {
        Log.e("Location", "setCamPosition");
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 13));
    }


    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        Log.e("Location", "onConnected");
        locationEngine.requestLocationUpdates();
    }


    @Override
    public void onLocationChanged(android.location.Location location) {
        //Log.e("Location", "onLocationChanged"+ location.toString());
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener((LocationEngineListener) this);
        }
    }

    private void downloadRegionDialog() {
        // Set up download interaction. Display a dialog
        // when the user clicks download button and require
        // a user-provided region name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText regionNameEdit = new EditText(this);
        regionNameEdit.setHint(getString(R.string.set_region_name_hint));

        // Build the dialog box
        builder.setTitle(getString(R.string.dialog_title))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            Toast.makeText(UserInterfaceActivity.this, getString(R.string.dialog_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            // Begin download process
                            downloadRegion(regionName);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Display the dialog
        builder.show();
    }

    private void downloadRegion(final String regionName) {
        // Define offline region parameters, including bounds,
        // min/max zoom, and metadata

        // Start the progressBar
        startProgress();

        // Create offline definition using the current
        // style and boundaries of visible map area
        String styleUrl = map.getStyleUrl();
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = map.getCameraPosition().zoom;
        double maxZoom = map.getMaxZoomLevel();
        float pixelRatio = this.getResources().getDisplayMetrics().density;
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                styleUrl, bounds, minZoom, maxZoom, pixelRatio);

        // Build a JSONObject using the user-defined offline region title,
        // convert it into string, and use it to create a metadata variable.
        // The metadata variable will later be passed to createOfflineRegion()
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the offline region and launch the download
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Log.d(TAG, "Offline region created: " + regionName);
                UserInterfaceActivity.this.offlineRegion = offlineRegion;
                launchDownload();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void launchDownload() {
        // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0
                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
                    // Download complete
                    endProgress(getString(R.string.end_progress_success));
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
                    // Switch to determinate state
                    setPercentage((int) Math.round(percentage));
                }

                // Log what is being currently downloaded
                Log.d(TAG, String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                Log.e(TAG, "onError reason: " + error.getReason());
                Log.e(TAG, "onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
            }
        });

        // Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    private void downloadedRegionList() {
        // Build a region list when the user clicks the list button

        // Reset the region selected int to 0
        regionSelected = 0;

        // Query the DB asynchronously
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_no_regions_yet), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add all of the region names to a list
                ArrayList<String> offlineRegionsNames = new ArrayList<>();
                for (OfflineRegion offlineRegion : offlineRegions) {
                    offlineRegionsNames.add(getRegionName(offlineRegion));
                }
                final CharSequence[] items = offlineRegionsNames.toArray(new CharSequence[offlineRegionsNames.size()]);

                // Build a dialog containing the list of regions
                AlertDialog dialog = new AlertDialog.Builder(UserInterfaceActivity.this)
                        .setTitle(getString(R.string.navigate_title))
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Track which region the user selects
                                regionSelected = which;
                            }
                        })
                        .setPositiveButton(getString(R.string.navigate_positive_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Toast.makeText(UserInterfaceActivity.this, items[regionSelected], Toast.LENGTH_LONG).show();

                                // Get the region bounds and zoom
                                LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[regionSelected].getDefinition()).getBounds();
                                double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                        offlineRegions[regionSelected].getDefinition()).getMinZoom();

                                // Create new camera position
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(bounds.getCenter())
                                        .zoom(regionZoom)
                                        .build();

                                // Move camera to new position
                                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            }
                        })
                        .setNeutralButton(getString(R.string.navigate_neutral_button_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Make progressBar indeterminate and
                                // set it to visible to signal that
                                // the deletion process has begun
                                progressBar.setIndeterminate(true);
                                progressBar.setVisibility(View.VISIBLE);

                                // Begin the deletion process
                                offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                                    @Override
                                    public void onDelete() {
                                        // Once the region is deleted, remove the
                                        // progressBar and display a toast
                                        progressBar.setVisibility(View.INVISIBLE);
                                        progressBar.setIndeterminate(false);
                                        Toast.makeText(getApplicationContext(), getString(R.string.toast_region_deleted),
                                                Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        progressBar.setIndeterminate(false);
                                        Log.e(TAG, "Error: " + error);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getString(R.string.navigate_negative_button_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // When the user cancels, don't do anything.
                                // The dialog will automatically close
                            }
                        }).create();
                dialog.show();

            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private String getRegionName(OfflineRegion offlineRegion) {
        // Get the region name from the offline region metadata
        String regionName;

        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, JSON_CHARSET);
            JSONObject jsonObject = new JSONObject(json);
            regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to decode metadata: " + exception.getMessage());
            regionName = String.format(getString(R.string.region_name), offlineRegion.getID());
        }
        return regionName;
    }

    // Progress bar methods
    private void startProgress() {
        // Disable buttons
        downloadButton.setEnabled(false);
        listButton.setEnabled(false);

        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Enable buttons
        downloadButton.setEnabled(true);
        listButton.setEnabled(true);

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        // Show a toast
        Toast.makeText(UserInterfaceActivity.this, message, Toast.LENGTH_LONG).show();
    }

}

