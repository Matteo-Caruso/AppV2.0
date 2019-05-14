package com.source.aero.aerogroundstation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.source.aero.aerogroundstation.R;

public class MotorDialogue extends Fragment{

    String TAG = "MOTORDIALOGUE";

    public MotorDialogue() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.motor_dialogue, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstance) {

    }
}

