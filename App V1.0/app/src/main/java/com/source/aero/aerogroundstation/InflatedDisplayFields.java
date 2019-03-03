package com.source.aero.aerogroundstation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class InflatedDisplayFields extends Fragment{

    String TAG = "INFLATEDDISPLAYFIELDS";

    public InflatedDisplayFields() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_inflated_display_fields, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {
//        LinearLayout displayFields = (LinearLayout) view.findViewById(R.id.displayFieldsLayout);
//        displayFields.setVisibility(View.INVISIBLE);
    }

/*    @Override
    public void onDestroyView() {
        LinearLayout displayFields = (LinearLayout) view.findViewById(R.id.displayFieldsLayout);
        displayFields.setVisibility(View.VISIBLE);
        super.onDestroyView();
    }*/
}

