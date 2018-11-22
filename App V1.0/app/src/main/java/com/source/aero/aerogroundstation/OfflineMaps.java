package com.source.aero.aerogroundstation;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;

public class OfflineMaps extends AppCompatActivity {
    //UI Elements
    private MapView mapView;
    private MapboxMap map;

    private ProgressBar progressBar;
    private Button downloadButton;
    private Button listButton;

    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_maps);
        progressBar = (ProgressBar) findViewById(R.id.DownloadProgressBar);
        downloadButton = (Button) findViewById(R.id.DownloadButton);
        listButton = (Button) findViewById(R.id.ListButton);

        Mapbox.getInstance(this, "pk.eyJ1IjoiYWVyb2Rlc2lnbiIsImEiOiJjam9sczI0bjMwM3E4M2twMXk0NG93YXg1In0.jYhWqqiBnn4O4KrLImf-Gg");
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadRegionDialog();
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //listRegion();
            }
        });
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

    private void downloadRegionDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(OfflineMaps.this);

        final EditText regionNameEdit = new EditText(OfflineMaps.this);
        regionNameEdit.setHint(getString(R.string.OfflineMapsSetRegionNameHint));

        // Build the dialog box
        downloadDialog.setTitle(getString(R.string.OfflineMapsDownloadRegionDialogTitle))
                .setView(regionNameEdit)
                .setMessage(getString(R.string.OfflineMapsDownloadRegionDialogMessage))
                .setPositiveButton(getString(R.string.OfflineMapsDownloadRegionDialogPositive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String regionName = regionNameEdit.getText().toString();
                        // Require a region name to begin the download.
                        // If the user-provided string is empty, display
                        // a toast message and do not begin download.
                        if (regionName.length() == 0) {
                            Toast.makeText(OfflineMaps.this, getString(R.string.OfflineMapsDownloadRegionDialogToast), Toast.LENGTH_SHORT).show();
                        } else {
                            // Begin download process
                            //downloadRegion(regionName);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.OfflineMapsDownloadRegionDialogCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Display the dialog
        downloadDialog.show();
    }

}
