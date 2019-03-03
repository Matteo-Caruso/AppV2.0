package com.source.aero.aerogroundstation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FlightPath extends Fragment {

    public FlightPath() {
        //Empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_flight_path, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {

    }
}
