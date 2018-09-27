package com.code.aero.planned_additions;

/**
 * Created by Carl on 2018-03-05.
 */


//PLANNED ADDITION

    //AN IN APP LOG VIEWER FOR DEBUGGING WHILE BEING ABLE TO HAVE THE USB PORT CONNECTED TO RADIO

//https://stackoverflow.com/questions/7863841/can-logcat-results-for-log-i-be-viewed-in-our-activity

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import android.app.Activity;
//import android.os.Bundle;
//import android.widget.TextView;
//
//import com.code.aero.groundstation.R;
//
//class LogViewer extends Activity {
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//
//        try {
//            Process process = Runtime.getRuntime().exec("logcat -d");
//            BufferedReader bufferedReader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));
//
//            StringBuilder log=new StringBuilder();
//            String line = "";
//            while ((line = bufferedReader.readLine()) != null) {
//                log.append(line);
//            }
//            TextView tv = (TextView)findViewById(R.id.textView1);
//            tv.setText(log.toString());
//        } catch (IOException e) {
//            // Handle Exception
//        }
//    }
//
//}
