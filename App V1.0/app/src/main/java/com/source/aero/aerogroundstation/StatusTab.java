package com.source.aero.aerogroundstation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class StatusTab extends Fragment {
    String TAG = "STATUSTAB";
    //UI Elements
    ImageButton statusButton;

    public StatusTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_statustab, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
        //Initialize status button to close fragment
        statusButton = (ImageButton)view.findViewById(R.id.statusTabStatusButton);
        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Call onBackPressed to do fragment shutdown
                /*try {
                    getActivity().onBackPressed();
                } catch (NullPointerException e) {
                    Log.d(TAG, "Couldn't close status tab fragment");
                }*/
            }
        });
    }
}
