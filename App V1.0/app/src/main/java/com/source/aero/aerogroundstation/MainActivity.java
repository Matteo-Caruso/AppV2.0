// TODO: Barometer height offset if we use the barometer

package com.source.aero.aerogroundstation;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.EditText;
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

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.source.aero.aerogroundstation.Bluetooth.BluetoothConstantsInterface;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothDevices;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothMessage;
import com.source.aero.aerogroundstation.Bluetooth.BluetoothService;
import com.source.aero.aerogroundstation.ContainerClasses.*;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Debugging Tag
    private static final String TAG = "MainActivity";

    // String for configuration that the system boots into
    String configuration;

    // I FORGOT WHY
    private static final int WATER = 1;
    private static final int GLIDER = 2;
    private static final int HABITAT = 3;

    // Database helper functionality
    public DatabaseHelper mDatabaseHelper;
    private String dbName = "AeroDB";
    private boolean mRecording;             // Variable that is true if we are in recording mode. Else false
    private int MAX_POINTS = 100;           // Buffer of points stored
    private boolean sessionCreated;
    protected String recordingSession;
    private int waypointID;
    private int dropped;

    // String values for drop string and drop height string
    private String waterTextString = "N/A\n";
    private String waterDropString;
    private String habitatDropString;
    private String habitatTextString = "N/A\n";
    private String gliderDropString;
    private String gliderTextString = "N/A";

    //Mapbox elements
    private MapView mapView;
    private MapboxMap map;

    // Map markers
    protected Marker planeMarker;
    protected Marker mPayload;
    protected Marker mCDA;
    protected Marker lastPosition;

    // Other map elements
    protected Polyline planePath;
    protected IconFactory factory;
    protected Bitmap icon;

    // Bundle
    Bundle flightPathData;

    //Ui Elements
    BottomNavigationView bottomNavigationView;
    SpeedDialView speedDialView;
    SpeedDialView motorSpeedDialView;
    DrawerLayout drawerLayout;
    Spinner spinner;
    NavigationView navigationView;
    ImageButton statusTabButton;
    ArrayList<LatLng> points;
    int drawerMenu = 0;

    //Bluetooth Elements
    //Request Codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    boolean bluetoothDisplayed = false;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothService bluetoothService = null;
    private StringBuffer dataBuffer;
    private int discoveryTime = 300;
    private String connectedDevice = null;
    boolean[] motorState = new boolean[16];

    // Data text views
    private TextView currentAltitude;
    private TextView currentPayload;
    private TextView currentDropAltitude;
    private TextView currentSpeed;
    private TextView currentTimeToTarget;
    private TextView currentDistanceToTarget;

    // Vehicles
    private Vehicles vehicleManager;

    // Target coordinates
    float targetLat;
    float targetLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getResources().getString(R.string.mapboxToken));
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);
        
        //Initializing UI Elements
        initBottomNavigationBar();
        initSpeedDial();
        initNavigationDrawer(); //Needs to be called before spinner
        initSpinner();
        //initStatusTab();

        //Points for creating polyline
        points = new ArrayList<>();

        mRecording = false;
        waterDropString = "N/A\n";
        habitatDropString= "N/A\n";
        gliderDropString= "N/A";

        sessionCreated = false;

        recordingSession = null;
        waypointID = 0;

        mDatabaseHelper = new DatabaseHelper(this, dbName);

        Arrays.fill(motorState,false);

        //Creating Factory and Icon ONCE to avoid lag in updatePlane()
        factory = IconFactory.getInstance(MainActivity.this);
        icon = factory.fromResource(R.drawable.ic_plane).getBitmap();

        //Set configuration
        Intent intent = getIntent();
        configuration = intent.getStringExtra("CONFIGURATION");

        //Bluetooth Setup. Get local bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initTextDisplay();

        vehicleManager = new Vehicles();

        // Target
        targetLat = 0;
        targetLon = 0;

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });
    }

    private void initTextDisplay()
    {
        currentAltitude = (TextView) findViewById(R.id.currentAltitude);
        currentAltitude.setText("N/A");

        currentPayload = (TextView) findViewById(R.id.currentPayload);
        currentPayload.setText("N/A");

        currentDropAltitude = (TextView) findViewById(R.id.currentDropAltitude);
        currentDropAltitude.setText("N/A");

        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        currentSpeed.setText("N/A");

        currentTimeToTarget = (TextView) findViewById(R.id.currentTimeToTarget);
        currentTimeToTarget.setText("N/A");

        currentDistanceToTarget = (TextView) findViewById(R.id.currentDistanceToTarget);
        currentDistanceToTarget.setText("N/A");
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
    }

    //Make bluetooth menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bluetoothmain, menu);
        return true;
    }

    //Bluetooth options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secureConnectOption: {
                Intent intent = new Intent(this, BluetoothDevices.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecureConnectOption: {
                Intent intent = new Intent(this, BluetoothDevices.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.makeDiscoverableOption: {
                makeDiscoverable();
                return true;
            }
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();
        //mapView.onStart();

        //Bluetooth
        //Request for bluetooth to be enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else if (bluetoothService == null) {
            setup();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //mapView.onResume();

        //Bluetooth
        if (bluetoothService != null) {
            if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
                bluetoothService.start();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        //mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mapView.onDestroy();

        //Bluetooth
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mapView.onSaveInstanceState(outState);
    }

    //Performs speed dial initialization
    private void initSpeedDial() {
        speedDialView = findViewById(R.id.mainActivitySpeedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mainActivitySpeedDialAction1, R.drawable.ic_location)
                        .setLabel(getResources().getString(R.string.mainActivitySpeedDialOption1Text))
                        .create()
        );
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mainActivitySpeedDialAction2, R.drawable.ic_record)
                        .setLabel(getResources().getString(R.string.mainActivitySpeedDialOption2Text))
                        .create()
        );
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mainActivitySpeedDialAction3, R.drawable.ic_bluetoothconnect)
                        .setLabel(getResources().getString(R.string.mainActivitySpeedDialOption3Text))
                        .create()
        );
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.mainActivitySpeedDialAction4, R.drawable.ic_save)
                        .setLabel(getResources().getString(R.string.mainActivitySpeedDialOption4Text))
                        .create()
        );
        //On click listener for speed dial options
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.mainActivitySpeedDialAction1:
                        speedDialView.hide();
                        bottomNavigationView.setVisibility(View.INVISIBLE);
                        openFragment("CURRENTLOCATION");
                        break;
                    case R.id.mainActivitySpeedDialAction2:
                        speedDialView.close();
                        isRecording(!mRecording);
                        break;
                    case R.id.mainActivitySpeedDialAction3:
                        if (!bluetoothDisplayed) {
                            getSupportActionBar().show();
                            bluetoothDisplayed = true;
                        }
                        else {
                            getSupportActionBar().hide();
                            bluetoothDisplayed = false;
                        }
                        speedDialView.close();
                        break;
                    case R.id.mainActivitySpeedDialAction4:
                        speedDialView.hide();
                        bottomNavigationView.setVisibility(View.INVISIBLE);
                        openFragment("OFFLINEMAPS");
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        // Water drop
        motorSpeedDialView = findViewById(R.id.motorSpeedDial);
        motorSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.motorSpeedDialAction1, R.drawable.ic_payload)
                        .setLabel(getResources().getString(R.string.motorSpeedDialOption1Text))
                        .create()
        );

        // Habitat drop
        motorSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.motorSpeedDialAction2, R.drawable.ic_payload)
                        .setLabel(getResources().getString(R.string.motorSpeedDialOption2Text))
                        .create()
        );

        // Glider drop
        motorSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.motorSpeedDialAction3, R.drawable.ic_path)
                        .setLabel(getResources().getString(R.string.motorSpeedDialOption3Text))
                        .create()
        );
        // Glider 1 pup
        motorSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.motorSpeedDialAction4, R.drawable.ic_glider1)
                        .setLabel(getResources().getString(R.string.motorSpeedDialOption4Text))
                        .create()
        );
        // Glider 2 pup
        motorSpeedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.motorSpeedDialAction5, R.drawable.ic_glider2)
                        .setLabel(getResources().getString(R.string.motorSpeedDialOption5Text))
                        .create()
        );

        motorSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                BluetoothMessage command = new BluetoothMessage();
                switch (speedDialActionItem.getId()) {
                    case R.id.motorSpeedDialAction1:
                        motorSpeedDialView.close();
                        command = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                        command.setMsgType((short)1);
                        command.setDropRequest(BluetoothConstantsInterface.DROPWATER);
                        send(command.makeMessage());

                        Toast.makeText(MainActivity.this,"Dropping water...", Toast.LENGTH_SHORT).show();

                        if(mRecording)
                        {
                            addWaypointToDb("Plane");
                        }

                        if(configuration.equals("DEBUG")) { waterDropString = "100 \n"; }
                        else { waterDropString = String.valueOf(vehicleManager.getPlaneData().readPlaneAltitude()) + "\n";}


                        if(configuration.equals("DEBUG"))
                        {
                            mPayload = map.addMarker(new MarkerOptions()
                                    .title("Water")
                                    .position(new LatLng(28.0394650, -81.9498040)));
                        }
                        else
                        {
                            mPayload = map.addMarker(new MarkerOptions()
                                    .title("Water")
                                    .position(new LatLng(vehicleManager.getPlaneData().readPlaneLatitude(), vehicleManager.getPlaneData().readPlaneLongitude())));
                        }

                        currentDropAltitude.setText(waterDropString + habitatDropString + gliderDropString);

                        dropped = WATER;

                        waterTextString = "Water\n";
                        currentPayload.setText(waterTextString + habitatTextString + gliderTextString);

                        break;
                    case R.id.motorSpeedDialAction2:

                        motorSpeedDialView.close();
                        command = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                        command.setMsgType((short)1);
                        command.setDropRequest(BluetoothConstantsInterface.DROPHABITAT);
                        send(command.makeMessage());

                        Toast.makeText(MainActivity.this,"Dropping habitats...", Toast.LENGTH_SHORT).show();

                        if(mRecording)
                        {

                            addWaypointToDb("Plane");
                        }

                        // If we requested a drop, then define the drop string
                        // If we are in DEBUG, use a FAKE height. COMP build uses actual height

                        if(configuration.equals("DEBUG")) { habitatDropString = "75 ft\n"; }
                        else { habitatDropString = String.valueOf(vehicleManager.getPlaneData().readPlaneAltitude()) +" ft\n";}

                        // Add marker and drop string
                        if(configuration.equals("DEBUG"))
                        {
                            mCDA = map.addMarker(new MarkerOptions()
                                    .title("Habitat")
                                    .position(new LatLng(28.2494650, -81.9498040)));
                        }
                        else
                        {
                            // TODO: Only add marker if we have valid data on drop
                            mCDA = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(vehicleManager.getPlaneData().readPlaneLatitude(), vehicleManager.getPlaneData().readPlaneLongitude())));
                        }

                        currentDropAltitude.setText(waterDropString + habitatDropString + gliderDropString);

                        dropped = HABITAT;
                        habitatTextString = "Habitat\n";
                        currentPayload.setText(waterTextString + habitatTextString + gliderTextString);
                        break;

                    case R.id.motorSpeedDialAction3:
                        motorSpeedDialView.close();
                        command = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                        command.setMsgType((short)1);
                        command.setDropRequest(BluetoothConstantsInterface.DROPGLIDERS);
                        send(command.makeMessage());
                        Toast.makeText(MainActivity.this,"Dropping gliders...", Toast.LENGTH_SHORT).show();

                        if(mRecording)
                        {
                            addWaypointToDb("Plane");
                        }

                        if(configuration.equals("DEBUG")) { gliderDropString = "50 ft"; }
                        else { gliderDropString = String.valueOf(vehicleManager.getPlaneData().readPlaneAltitude())+" ft";}

                        // Add marker and drop string
                        if(configuration.equals("DEBUG"))
                        {
                            mCDA = map.addMarker(new MarkerOptions()
                                    .title("Glider")
                                    .position(new LatLng(28.2394650, -81.9498040)));
                        }
                        else
                        {
                            mCDA = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(vehicleManager.getPlaneData().readPlaneLatitude(), vehicleManager.getPlaneData().readPlaneLongitude())));
                        }
                        currentDropAltitude.setText(waterDropString + habitatDropString + gliderDropString);

                        gliderTextString = "Glider";
                        currentPayload.setText(waterTextString + habitatTextString + gliderTextString);

                        dropped = GLIDER;

                        break;
                    case R.id.motorSpeedDialAction4:
                        motorSpeedDialView.close();
                        command = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                        command.setMsgType((short)2);
                        command.setGliders(BluetoothConstantsInterface.GLIDER1);
                        send(command.makeMessage());
                        Toast.makeText(MainActivity.this,"Emergency Glider 1 Pitch Up", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.motorSpeedDialAction5:
                        motorSpeedDialView.close();
                        command = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                        command.setMsgType((short)3);
                        command.setGliders(BluetoothConstantsInterface.GLIDER2);
                        send(command.makeMessage());
                        Toast.makeText(MainActivity.this, "Emergency Glider 2 Pitch Up", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
    }

    //Initialize bottom navigation bar
    private void initBottomNavigationBar() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainActivityBottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                getSupportActionBar().hide();
                switch (item.getItemId()) {
                    case R.id.mainActivityBottomNavigationMap:
                        onBackPressed();
                        return true;
                    case R.id.mainActivityBottomNavigationPath:
                        onBackPressed();
                        // Get list of database sessions
                        List<String> sessionList = mDatabaseHelper.getFlightSessions();

                        for(String s : sessionList)
                        {
                            Log.d("Sessions", s);
                        }

                        String[] sessions = new String[sessionList.size()];
                        sessions = sessionList.toArray(sessions);

                        // Final copy
                        final String[] lmaoStringFix = sessions;

                        AlertDialog.Builder builderPaths = new AlertDialog.Builder(MainActivity.this);
                        builderPaths.setTitle("Flight Paths")
                                .setItems(sessions, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        flightPathData = new Bundle();

                                        List<Waypoint> waypoints = mDatabaseHelper.getWaypoints(lmaoStringFix[which], "Plane");

                                        ArrayList<Waypoint> bundleList = new ArrayList<Waypoint>(waypoints);
                                        flightPathData.putSerializable("WAYPOINTS",bundleList);
                                        openFragment("FLIGHTPATH");
                                    }
                                })
                                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                });

                        builderPaths.create();
                        builderPaths.show();

                        return true;
                    case R.id.mainActivityBottomNavigationTargets:
                        onBackPressed();

                        List<Target> targetList = mDatabaseHelper.getTargets();

                        List<String> targetNames = new ArrayList<String>();

                        for(Target target : targetList)
                        {
                            targetNames.add(target.getName());
                        }

                        String[] targets = new String[targetNames.size()];
                        targets = targetNames.toArray(targets);

                        // Final copy
                        final String[] lmaoTargetFix = targets;

                        AlertDialog.Builder builderTarget = new AlertDialog.Builder(MainActivity.this);
                        builderTarget.setTitle("Targets")
                                .setItems(lmaoTargetFix, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO: Set target based on selected argument. This may not compile
                                        String target = targetList.get(which).getLocation();
                                        String _target_[] = target.split(",");
                                        targetLat = Float.valueOf(_target_[0]);
                                        targetLon = Float.valueOf(_target_[1]);
                                        Toast.makeText(MainActivity.this, "Setting as target...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                });

                        builderTarget.create();
                        builderTarget.show();

                        return true;
                    case R.id.mainActivityBottomNavigationPayload:
                        onBackPressed();
                        openFragment("MOTORDIALOGUE");
                        return true;
                    case R.id.mainActivityBottomNavigationAttitude:
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void initNavigationDrawer() {
        drawerLayout = findViewById(R.id.mainActivityDrawerLayout);
        navigationView = findViewById(R.id.mainActivityNavigationView);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        BluetoothMessage message = new BluetoothMessage();
                        switch (menuItem.getItemId()) {
                            case R.id.navigationDrawerItem1:
                                if (drawerMenu == 0) {
                                    message = new BluetoothMessage();
                                    message.setMsgType((short)1);
                                    message.setCalibrate(BluetoothConstantsInterface.CALIBRATEIMU);
                                    send(message.makeMessage());
                                    Toast.makeText(getApplicationContext(),"IMU Calibration Command Sent", Toast.LENGTH_SHORT).show();
                                }
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.navigationDrawerItem2:
                                if (drawerMenu == 0) {
                                    message = new BluetoothMessage();
                                    message.setMsgType((short)1);
                                    message.setCalibrate(BluetoothConstantsInterface.CALIBRATEGPS);
                                    send(message.makeMessage());
                                    Toast.makeText(getApplicationContext(),"GPS Calibration Command Sent", Toast.LENGTH_SHORT).show();
                                }
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.navigationDrawerItem3:
                                if (drawerMenu == 0) {
                                    message = new BluetoothMessage();
                                    message.setMsgType((short)1);
                                    message.setCalibrate(BluetoothConstantsInterface.CALIBRATEBAROMETER);
                                    send(message.makeMessage());
                                    Toast.makeText(getApplicationContext(),"Barometer Calibration Command Sent", Toast.LENGTH_SHORT).show();
                                }
                                drawerLayout.closeDrawers();
                                break;
                            default:
                                return true;
                        }
                        return true;
                    }
                });
    }

    //Initialize spinner element
    public void initSpinner() {
        spinner = (Spinner) findViewById(R.id.mainActivityNavigationSpinner);
        //Using default android spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mainActivityNavigationSpinnerItems,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                navigationView.getMenu().clear();
                switch (pos) {
                    case 0:
                        drawerMenu = 0;
                        navigationView.inflateMenu(R.menu.main_navigation_calibration);
                        break;
                    case 1:
                        drawerMenu = 1;
                        navigationView.inflateMenu(R.menu.main_navigation_logs);
                        break;
                    default:
                        Log.d(TAG,"Spinner menu error");
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                navigationView.inflateMenu(R.menu.main_navigation_calibration);
            }
        });
    }

    //Create fragment on top of main
    public void openFragment(String fragmentType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment;

        //Determine desired fragment type
        switch (fragmentType) {
            case "OFFLINEMAPS":
                fragment = new OfflineMaps();
                break;
            case "CURRENTLOCATION":
                fragment = new MapboxLocationHandler();
                break;
            case "STATUSTAB":
                fragment = new StatusTab();
                statusTabButton.setVisibility(View.INVISIBLE);
                break;
            case "MOTORDIALOGUE":
                fragment = new MotorDialogue();
                break;
            case "INFLATEDDISPLAYFIELDS":
                Log.d("TEST", waterDropString+habitatDropString+gliderDropString);
                fragment = new InflatedDisplayFields(waterDropString, habitatDropString, gliderDropString);

                break;
            case "FLIGHTPATH":
                fragment = new FlightPathFragment();
                fragment.setArguments(flightPathData);
                break;
            default:
                Log.d("MainActivity", "Failed to create fragment");
                return;
        }

        //Add or remove fragment to backstack
        if (fragmentManager.findFragmentByTag(fragmentType) == null) {
            fragmentTransaction.add(R.id.mainActivityFragmentLayout, fragment).addToBackStack(fragmentType).commit();
        }
        else {
            fragment = fragmentManager.findFragmentById(R.id.mainActivityFragmentLayout);
            fragmentTransaction.remove(fragment).commit();
        }

    }

    //------------------------------------------------- Below is the implementation for when the recording identifier is adjusted -----------------------------------------------------------

    public void isRecording(boolean value){

        //Obtain the ID's of the recording identifier that we are to change
        LinearLayout messageColour = (LinearLayout) findViewById(R.id.recordingIdentifier);
        TextView messageRecord = (TextView) findViewById(R.id.recordingText);

        //Below, use the if statement to identify if the status of the recording identifier is to be changed to
        //"recording"

        //EX. If a record button is pressed, "value" should be greater than zero to identify that recording has started
        if (value == true){

            messageColour.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_boarder_white_outline));
            messageRecord.setText("Recording");
            mRecording = true;

            // Create new session for db
            recordingSession = new Date().toString();
            waypointID = 0;
        }

        else{

            messageColour.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_boarder_white_outline));
            messageRecord.setText("Not Recording");
            mRecording = false;
            sessionCreated = false;

            if(mPayload != null)
            {
                mPayload.remove();
                mPayload = null;
            }

            if(mCDA != null)
            {
                mCDA.remove();
                mCDA = null;
            }
        }
    }

    //------------------------------------------------- Below is the implementation for when the display field values need to be adjusted -----------------------------------------------------------

    void updateUI()
    {
        // Update value
        currentAltitude.setText(String.valueOf(vehicleManager.getPlaneData().readPlaneAltitude()));
        currentSpeed.setText(String.valueOf(vehicleManager.getPlaneData().readPlaneSpeed()));

        // TODO: Calculate distance to target based on plane position and target position
        // TODO: Calculate time based on distance approximated to meters and current speed or change of speed from past lat, lons
        if(points.size() > 2)
        {
            LatLng curLoc = points.get(points.size()-1);
            LatLng tarLoc = new LatLng((double)targetLat, (double)targetLon);
            double distance = curLoc.distanceTo(tarLoc);
            currentDistanceToTarget.setText(String.valueOf(distance*3.28)+ " ft");

            // v = d/t therefore t = d/v
            double time = distance/vehicleManager.getPlaneData().readPlaneSpeed();
            currentTimeToTarget.setText(String.valueOf(time) +" s");
        }

        updatePlane();
    }

    //------------------------------------------------- Below is the implementation of the motor dialogue when a button is selected -----------------------------------------------------------
    public void motorDialogueSelect(View view)
    {
        int id = view.getId();
        BluetoothMessage message = new BluetoothMessage();
        switch(id)
        {
            case R.id.motorOneOpenButton: {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(0,BluetoothConstantsInterface.MOTORON);
                send(message.makeMessage());
                motorState[0] = true;
                Toast.makeText(getApplicationContext(),"Turning Motor 1 On", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorOneCloseButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(0,BluetoothConstantsInterface.MOTOROFF);
                send(message.makeMessage());
                motorState[0] = false;
                Toast.makeText(getApplicationContext(),"Turning Motor 1 Off", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorTwoOpenButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(1,BluetoothConstantsInterface.MOTORON);
                send(message.makeMessage());
                motorState[1] = true;
                Toast.makeText(getApplicationContext(),"Turning Motor 2 On", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorTwoCloseButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(1,BluetoothConstantsInterface.MOTOROFF);
                send(message.makeMessage());
                motorState[1] = false;
                Toast.makeText(getApplicationContext(),"Turning Motor 2 Off", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorThreeOpenButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(2,BluetoothConstantsInterface.MOTORON);
                send(message.makeMessage());
                motorState[2] = true;
                Toast.makeText(getApplicationContext(),"Turning Motor 3 On", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorThreeCloseButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(2,BluetoothConstantsInterface.MOTOROFF);
                send(message.makeMessage());
                motorState[2] = false;
                Toast.makeText(getApplicationContext(),"Turning Motor 3 Off", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorFourOpenButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(3,BluetoothConstantsInterface.MOTORON);
                send(message.makeMessage());
                motorState[3] = true;
                Toast.makeText(getApplicationContext(),"Turning Motor 4 On", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorFourCloseButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(3,BluetoothConstantsInterface.MOTOROFF);
                send(message.makeMessage());
                motorState[3] = false;
                Toast.makeText(getApplicationContext(),"Turning Motor 4 Off", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorFiveOpenButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(4,BluetoothConstantsInterface.MOTORON);
                send(message.makeMessage());
                motorState[4] = true;
                Toast.makeText(getApplicationContext(),"Turning Motor 5 On", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorFiveCloseButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(4,BluetoothConstantsInterface.MOTOROFF);
                send(message.makeMessage());
                motorState[4] = false;
                Toast.makeText(getApplicationContext(),"Turning Motor 5 Off", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorSixOpenButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(5,BluetoothConstantsInterface.MOTORON);
                send(message.makeMessage());
                motorState[5] = true;
                Toast.makeText(getApplicationContext(),"Turning Motor 6 On", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.motorSixCloseButton:
            {
                message = new BluetoothMessage();     // Need default on button push cause then you will clobber commands
                message.setMsgType((short)1);
                message.setMotor(5,BluetoothConstantsInterface.MOTOROFF);
                send(message.makeMessage());
                motorState[5] = false;
                Toast.makeText(getApplicationContext(),"Turning Motor 6 Off", Toast.LENGTH_SHORT).show();
                break;
            }
            default: break;
        }
    }

    void updatePlane()
    {
        Log.d(TAG, "Plane update");

        if(planeMarker != null)
        {
            planeMarker.remove();
        }

        Matrix matrix = new Matrix();
        matrix.postRotate((float)vehicleManager.getPlaneData().readPlaneYaw());
        Bitmap rotatedBitmap = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
        rotatedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 40, 50, false);

        //Plane Marker
        LatLng point = new LatLng(vehicleManager.getPlaneData().readPlaneLatitude(), vehicleManager.getPlaneData().readPlaneLongitude());
        Log.d(TAG, "Plane position: " + point.toString());
        planeMarker = map.addMarker(new MarkerOptions()
                .position(point)
                .icon(factory.fromBitmap(rotatedBitmap)));

        //Past Position Markers
        if(points.size() > 2 && (points.size() % 10 == 0)){

            Bitmap circle = factory.fromResource(R.drawable.black_circle).getBitmap();
            circle = Bitmap.createScaledBitmap(circle, 10, 10, false);

            lastPosition = map.addMarker(new MarkerOptions()
                    .position(points.get(points.size()-2))
                    .icon(factory.fromBitmap(circle)));
        }

        planePath = map.addPolyline(new PolylineOptions()
                .addAll(points)
                .color(Color.parseColor("#3bb2d0"))
                .width(1));
    }

    //------------------------------------------------- Below is the implementation of increasing or decreasing display field font size when selected -----------------------------------------------------------
    public void increaseFontSize(View view)
    {
        onBackPressed();
        openFragment("INFLATEDDISPLAYFIELDS");
    }

    void addWaypointToDb(String flightType)
    {
        // do nothing
        if(!sessionCreated){
            Date currentTime = new Date();
            //;Date formattedDate = new Date();
            recordingSession = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss_z").format(currentTime);
            Log.d("Formatted", recordingSession);
            sessionCreated = true;
        }
        else{
            waypointID += 1;
            boolean databaseAddSuccess = false;
            if(dropped == WATER){
                databaseAddSuccess = mDatabaseHelper.addWaypoint(recordingSession, waypointID, points.get(points.size()-1).toString(),
                          (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                          (float)vehicleManager.getPlaneData().readPlaneSpeed(),
                          (float)vehicleManager.getPlaneData().readPlaneYaw(),
                          (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                          (float) 0.0,
                          (float) 0.0,
                          (float)vehicleManager.getPlaneData().readPlaneRoll(),
                          (float)vehicleManager.getPlaneData().readPlanePitch(),
                          (float)vehicleManager.getPlaneData().readPlaneYaw(),
                          flightType);
                dropped = 0;
            }
            else if(dropped == HABITAT){
                databaseAddSuccess = mDatabaseHelper.addWaypoint(recordingSession, waypointID, points.get(points.size()-1).toString(),
                        (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                        (float)vehicleManager.getPlaneData().readPlaneSpeed(),
                        (float) 0.0,
                        (float)vehicleManager.getPlaneData().readPlaneYaw(),
                        (float) 0.0,
                        (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                        (float)vehicleManager.getPlaneData().readPlaneRoll(),
                        (float)vehicleManager.getPlaneData().readPlanePitch(),
                        (float)vehicleManager.getPlaneData().readPlaneYaw(),
                        flightType);
                dropped = 0;
            }
            else if(dropped == GLIDER){
                databaseAddSuccess = mDatabaseHelper.addWaypoint(recordingSession, waypointID, points.get(points.size()-1).toString(),
                        (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                        (float)vehicleManager.getPlaneData().readPlaneSpeed(),
                        (float) 0.0,
                        (float) 0.0,
                        (float)vehicleManager.getPlaneData().readPlaneYaw(),
                        (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                        (float)vehicleManager.getPlaneData().readPlaneRoll(),
                        (float)vehicleManager.getPlaneData().readPlanePitch(),
                        (float)vehicleManager.getPlaneData().readPlaneYaw(),
                        flightType);
                dropped = 0;
            }
            else{
                databaseAddSuccess =mDatabaseHelper.addWaypoint(recordingSession, waypointID, points.get(points.size()-1).toString(),
                        (float)vehicleManager.getPlaneData().readPlaneAltitude(),
                        (float)vehicleManager.getPlaneData().readPlaneSpeed(),
                        (float)vehicleManager.getPlaneData().readPlaneYaw(),
                        (float) 0.0,
                        (float) 0.0,
                        (float) 0.0,
                        (float)vehicleManager.getPlaneData().readPlaneRoll(),
                        (float)vehicleManager.getPlaneData().readPlanePitch(),
                        (float)vehicleManager.getPlaneData().readPlaneYaw(),
                        flightType);
            }

            Log.d("Database addition flag: ", String.valueOf(databaseAddSuccess));

        }
    }

    //Close current fragment on back press
    //Status tab should always be closed
    @Override
    public void onBackPressed() {
        getFragmentManager().popBackStack();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.mainActivityFragmentLayout);
        if (existingFragment != null) {
            fragmentTransaction.remove(existingFragment).commit();
            speedDialView.show();
            bottomNavigationView.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
    }

    //Called from fragments that need access to map object
    public MapboxMap passMap() {
        return this.map;
    }

    //Bluetooth Functions
    private void setup() {
        //Initialize bluetooth connections
        bluetoothService = new BluetoothService(this, handler);
        dataBuffer = new StringBuffer("");
    }

    private void makeDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoveryTime);
            startActivity(intent);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //Send data
    private void send(byte[] data) {
        Log.d(TAG, "Sending data...");

        //Check device is connected
        if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.bluetooth_notConnectedToast, Toast.LENGTH_SHORT).show();
            return;
        }

        bluetoothService.write(data);
    }
    
    // Thiss function takes in a string that represents an incoming bluetooth msg from the HC-05
    private void readIncomingBluetoothData(byte[] data) {
            Log.d(TAG, "Incoming bluetooth data string preparing to be parsed");

            // Get first and last byte of the message
            byte startByte = data[0];
            byte endByte = data[data.length - 1];
            
            // Print out the two msg type bytes
            Log.d(TAG, "Incoming bluetooth data string msg type bytes: " + Byte.valueOf(data[0]) + " "  + Byte.valueOf(data[1]));
                
            // Check if start and stop bytes match message definition
            if((Byte.compare(startByte, (byte) 10) == 0) && (Byte.compare(endByte, (byte) 255) == 0)) {
                // Wrap data in ByteBuffer so we can parse the data easily
                ByteBuffer msgBuffer = ByteBuffer.wrap(data);  // BIG ENDIAN BY DEFAULT
                messageParser parser = new messageParser(msgBuffer);

                Log.d(TAG, "Parser result: " + parser.toString());

                switch (parser.whoIsThisMesssageFor)
                {
                    case 0:
                    case 1:
                    case 4:
                    {
                        // PLANE
                        Log.d(TAG, "Message came from plane");


                        vehicleManager.updatePlane(parser);

                        Log.d("LatLng", String.valueOf(vehicleManager.getPlaneData().readPlaneLatitude()) + " " + String.valueOf(vehicleManager.getPlaneData().readPlaneLongitude()));
                        LatLng planePoint =  new LatLng(vehicleManager.getPlaneData().readPlaneLatitude(), vehicleManager.getPlaneData().readPlaneLongitude());

                        if(points.size() > MAX_POINTS)
                        {

                            Log.d(TAG, "Removing points to limit waypoint list size...");
                            points.remove(0);

                        }
                        points.add(planePoint);

                        // Check if recording to add to db
                        if(mRecording)
                        {
                            Log.d(TAG, "Adding to db...");
                            addWaypointToDb("Plane");
                        }

                        updateUI();
                        break;
                    }
                    case 2:
                    {
                        // GLIDER 1
                        Log.d(TAG, "Message came from glider1");
                        vehicleManager.updateGliderOne(parser);

                        // Check if recording to add to db
                        if(mRecording)
                        {
                            addWaypointToDb("Glider 1");
                        }

                        break;
                    }
                    case 3:
                    {
                        // GLIDER 2
                        Log.d(TAG, "Message came from glider2");
                        vehicleManager.updateGliderTwo(parser);

                        // Check if recording to add to db
                        if(mRecording)
                        {
                            addWaypointToDb("Glider 2");
                        }
                        break;
                    }
                }
            }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case BluetoothConstantsInterface.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //setStatus(getString(R.string.bluetooth_titleConnectedTo, connectedDevice));
                            //logArrayAdapter.clear();
                            BluetoothMessage bMsg = new BluetoothMessage();
                            bMsg.setMsgType((short) 1);
                            send(bMsg.makeMessage());
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //setStatus(R.string.bluetooth_titleConnectedTo);
                            break;
                        case BluetoothService.STATE_LISTENING:
                        case BluetoothService.STATE_NONE:
                            //setStatus(R.string.bluetooth_titleNotConnectedTo);
                            break;
                    }
                    break;
                case BluetoothConstantsInterface.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeData = new String(writeBuffer);
                    //logArrayAdapter.add("Me: " + writeData);
                    break;
                case BluetoothConstantsInterface.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;

                    String readData = new String(readBuffer, 0, msg.arg1);

                    // Print bytes received and how many we received
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < readBuffer.length; ++j){
                        sb.append(String.format("%02X ", readBuffer[j]));
                    }
                    Log.d(TAG, "VALID PACKET LEN: : " + String.valueOf(readBuffer.length) + " CONTENTS: " + sb.toString());

                    if(readData.length() > 0){
                        readIncomingBluetoothData(readBuffer);
                    }

                    break;
                case BluetoothConstantsInterface.MESSAGE_DEVICE_NAME:
                    connectedDevice = msg.getData().getString(BluetoothConstantsInterface.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConstantsInterface.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConstantsInterface.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setup();
                }
                else {
                    Toast.makeText(this, R.string.bluetooth_btNotEnabledToast, Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(BluetoothDevices.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothService.connect(device, secure);
    }
}