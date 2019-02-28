package com.source.aero.aerogroundstation;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import org.json.JSONObject;
import java.util.ArrayList;

public class OfflineMaps extends Fragment {
    private MapboxMap map;

    //UI Elements
    private ProgressBar progressBar;
    private Button downloadButton;
    private Button listButton;
    boolean disableOptions = false;

    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;

    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    private static final String TAG = "OfflineMap";

    private boolean isEndNotified;
    private int regionSelected;

    public OfflineMaps() {
        //empty fragment constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //Need action bar for menu items
        try {
            ((MainActivity) getActivity()).getSupportActionBar().show();
        } catch (NullPointerException e) {
            Log.d(TAG,"Couldn't get actionbar from main activity");
        }

        offlineManager = OfflineManager.getInstance(getActivity());
        try {
            this.map = ((MainActivity) getActivity()).passMap(); //Get reference to map object from main
        } catch (NullPointerException e) {
            Log.d(TAG, "Couldn't get map from main activity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_offline_maps, parent, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.offline_map, menu);
        //Check whether flag is enabled to disable or enable buttons
        if (disableOptions) {
            menu.findItem(R.id.offlineMapsDownloadMap).setEnabled(false);
            menu.findItem(R.id.offlineMapsRegionList).setEnabled(false);
            menu.findItem(R.id.offlineMapsDownloadMap).setVisible(false);
            menu.findItem(R.id.offlineMapsRegionList).setVisible(false);

        } else {
            menu.findItem(R.id.offlineMapsDownloadMap).setEnabled(true);
            menu.findItem(R.id.offlineMapsRegionList).setEnabled(true);
            menu.findItem(R.id.offlineMapsDownloadMap).setVisible(true);
            menu.findItem(R.id.offlineMapsRegionList).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.offlineMapsDownloadMap: {
                downloadRegionDialog();
                return true;
            }
            case R.id.offlineMapsRegionList: {
                regionList();
            }
            default:
                Log.d(TAG, "Menu option error");
                return false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        progressBar = (ProgressBar) view.findViewById(R.id.DownloadProgressBar);
    }

    private void downloadRegionDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(getActivity());

        final EditText regionNameEdit = new EditText(getActivity());
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
                            Toast.makeText(getActivity(), getString(R.string.OfflineMapsDownloadRegionDialogToast), Toast.LENGTH_SHORT).show();
                        } else {
                            // Begin download process
                            downloadRegion(regionName);
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

    public void downloadRegion(final String regionName) {
        startDownloadProgress();
        //Define map region
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
                OfflineMaps.this.offlineRegion = offlineRegion;
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
                    endDownloadProgress(getString(R.string.OfflineMapsDownloadCompleteProgress));
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

    // Progress bar methods
    private void startDownloadProgress() {
        // Disable buttons
        disableOptions = true;
        try {
            //Regenerate options menu
            getActivity().invalidateOptionsMenu();
        } catch (NullPointerException e) {
            Log.d(TAG, "Couldn't hide options menu");
        }

        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endDownloadProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        //Enable buttons
        disableOptions = false;
        try {
            //Regenerate options menu
            getActivity().invalidateOptionsMenu();
        } catch (NullPointerException e) {
            Log.d(TAG, "Couldn't show options menu");
        }

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        // Show a toast
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void regionList() {
        regionSelected = 0;
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.OfflineMapsRegionListNoRegionsToast), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add all of the region names to a list
                ArrayList<String> offlineRegionsNames = new ArrayList<>();
                for (OfflineRegion offlineRegion : offlineRegions) {
                    offlineRegionsNames.add(getRegionName(offlineRegion));
                }
                final CharSequence[] items = offlineRegionsNames.toArray(new CharSequence[offlineRegionsNames.size()]);

                // Build a dialog containing the list of regions
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.OfflineMapsRegionListDialogTitle))
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Track which region the user selects
                                regionSelected = which;
                            }
                        })
                        .setPositiveButton(getString(R.string.OfflineMapsRegionListDialogPositive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Toast.makeText(getActivity(), items[regionSelected], Toast.LENGTH_LONG).show();

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
                        .setNeutralButton(getString(R.string.OfflineMapsRegionListDialogNeutralButton), new DialogInterface.OnClickListener() {
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
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.OfflineMapsDeleteRegionToast),
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
                        .setNegativeButton(getString(R.string.OfflineMapsRegionListDialogNegative), new DialogInterface.OnClickListener() {
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
            regionName = String.format(getString(R.string.OfflineMapsRegionNameFormat), offlineRegion.getID());
        }
        return regionName;
    }
}
