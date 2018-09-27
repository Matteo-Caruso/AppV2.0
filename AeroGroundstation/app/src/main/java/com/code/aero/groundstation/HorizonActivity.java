package com.code.aero.groundstation;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class HorizonActivity extends AppCompatActivity {

    private AttitudeIndicator mAttitudeIndicator;
    private AbstractRadio telemetry;

    char mode = 'R';

    //might have to change to float
    private float pitch,roll;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artificial_horizon);
        mAttitudeIndicator = findViewById(R.id.attitude_indicator);
        //sets orientation to landscape
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        if(telemetry == null){
//            telemetry = new RadioCommActivity(this.getApplicationContext());
//        }

        if(telemetry == null && mode == 'T'){
            telemetry = new TestRadio(this);
        }

        if(telemetry == null && mode == 'R'){
            telemetry = new Radio(this);
        }

        //set up USB and have it start recording values within that class
        telemetry.setUpUsbIfNeeded();
        pitch = 0;
        roll = 0;
        thread.start();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy() {
        //telemetry.closeDevice();
        super.onDestroy();
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    sleep(160);
                    roll = (float) (telemetry.planeRoll*(-1.0));
                    pitch = (float) telemetry.planePitch;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //send those values to the attitude indicator class to display it in the new activity
                        mAttitudeIndicator.setAttitude(pitch, roll);
                    }
                });
            }
        }
    };
}


