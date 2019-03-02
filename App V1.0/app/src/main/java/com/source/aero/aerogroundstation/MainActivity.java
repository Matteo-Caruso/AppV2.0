package com.source.aero.aerogroundstation;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";

    //Mapbox elements
    private MapView mapView;
    private MapboxMap map;

    //Ui Elements
    BottomNavigationView bottomNavigationView;
    SpeedDialView speedDialView;
    DrawerLayout drawerLayout;
    Spinner spinner;
    NavigationView navigationView;
    ImageButton statusTabButton;

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
        initStatusTab();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
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
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
                        break;
                    case R.id.mainActivitySpeedDialAction3:
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
                        return true;
                    case R.id.mainActivityBottomNavigationPath:
                        //TODO:Open path fragment
                        return true;
                    case R.id.mainActivityBottomNavigationTargets:
                        //TODO:Open targets fragment
                        return true;
                    case R.id.mainActivityBottomNavigationPayload:
                        //TODO:Open payloads fragment
                        return true;
                    case R.id.mainActivityBottomNavigationAttitude:
                        //TODO: Open attitude fragment
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
                        switch (menuItem.getItemId()) {
                            case R.id.navigationDrawerItem1:
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.navigationDrawerItem2:
                                drawerLayout.closeDrawers();
                                break;
                            case R.id.navigationDrawerItem3:
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
                        navigationView.inflateMenu(R.menu.main_navigation_calibration);
                        //TODO: Implement spinner functionality
                        break;
                    case 1:
                        navigationView.inflateMenu(R.menu.main_navigation_logs);
                        //TODO: Implement spinner functionality
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

    //Initialize statustab fragment
    public void initStatusTab() {
        statusTabButton = (ImageButton) findViewById(R.id.mainActivityStatusTab);
        statusTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment("STATUSTAB");
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

    //Close current fragment on back press
    //Status tab should always be closed
    @Override
    public void onBackPressed() {
        getFragmentManager().popBackStack();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.mainActivityFragmentLayout);
        fragmentTransaction.remove(existingFragment).commit();
        speedDialView.show();
        bottomNavigationView.setVisibility(View.VISIBLE);
        statusTabButton.setVisibility(View.VISIBLE);
        getSupportActionBar().hide();
    }

    //Called from fragments that need access to map object
    //TODO: Redesign fragments using Viewmodels
    public MapboxMap passMap() {
        return this.map;
    }
}