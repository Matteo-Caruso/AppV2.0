package com.code.aero.groundstation;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Random;

import static java.lang.Thread.sleep;

//import com.example.aerogroundstation.R;

/**
 * A simple {@link FragmentActivity} subclass.
 * Use the {@link TestRadio} factory method to
 * create an instance of this fragment.
 */
public class TestRadio extends AbstractRadio {

    private float startLat = (float) 28.0394650;
    private float startLong = (float) -81.9498040;
    private float inc =(float) 0.0001;
    private float r = (float) 0.25;

    TestRadio(){
        Log.d("Test Radio", "Test Radio Created");
    }

    TestRadio(Context context){
        this.globalContext = context;
    }

    @Override
    protected void openDevice(){
        Toast.makeText(globalContext, "Test Config", Toast.LENGTH_SHORT).show();
        if(mThreadIsStopped){
            mThreadIsStopped = false;
            new Thread(mLoop).start();
        }
    }

    @Override
    protected void setConfig(){
        Toast.makeText(globalContext, "Test Config", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void closeDevice(){
        Toast.makeText(globalContext, "Test Close", Toast.LENGTH_SHORT).show();
        mThreadIsStopped = true;
        telemetryOpen = false;
    }

    @Override
    protected void setUpUsbIfNeeded(){
        Toast.makeText(globalContext, "Test USB Setup", Toast.LENGTH_SHORT).show();

        // Check if already connected
        if (telemetryOpen) {
            String msg = "Port("+port+") is already opened.";
            Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT).show();
            return;
        }

        setConfig();
        Toast.makeText(globalContext, "Connected", Toast.LENGTH_SHORT).show();
        openDevice();
        telemetryOpen = true;
    }

    @Override
    protected void handleData(){
        Log.d("Data", "Generation");
        Random rand = new Random();

        for(int i=0; i<telemetry.length; i++) {
            int  n = rand.nextInt(360) + 1;
            telemetry[i] = (float)n;
        }

        telemetry[0] = startLat;
        telemetry[1] = startLong;
        telemetry[5] = r;
        r+=0.25;

        if(r == 360){
            r = 0;
        }
        startLat += inc;
        startLong += inc;

        setTelemetry(telemetry);


    }

    @Override
    protected void sendPayloadCommand(String command){
        switch (command){
            case "FL":

                break;
            case "FR":
                break;
            case "BL":
                break;
            case "BR":
                break;
            default:
                break;
        }
    }

    @Override
    protected void sendUnloadCommand(String command){
        switch (command){
            case "FL":
                break;
            case "FR":
                break;
            case "BL":
                break;
            case "BR":
                break;
            default:
                break;
        }
    }


    @Override
    protected void sendZeroCommand(String command){
        switch (command){
            case "B":
                break;
            case "G":
                break;
            default:
                break;
        }

    }

    @Override
    protected void sendTarget(float latitude, float longitiude){

        byte[] writeBuffer;
        writeBuffer = new byte[11];

        ByteBuffer buffer = ByteBuffer.allocate(8);

        buffer.putFloat(latitude);
        buffer.putFloat(longitiude);
        buffer.flip();

        writeBuffer[0] = 0x02;
        writeBuffer[1] = 0x07;
        Log.d("set", "");
        for(int i = 0; i < 8; i++){
            Log.d("set", String.valueOf(i));
            writeBuffer[i + 2]  = buffer.get();

        }

        writeBuffer[10] = (byte)(writeBuffer[0]^
                writeBuffer[1]^
                writeBuffer[2]^
                writeBuffer[3]^
                writeBuffer[4]^
                writeBuffer[5]^
                writeBuffer[6]^
                writeBuffer[7]^
                writeBuffer[8]^
                writeBuffer[9]);
    }


    @Override
    protected void sendAutoMode(boolean a){
    }

    @Override
    //door open = 0x01 0x00
    //door closed = 0x01 0x01
    protected void sendDoorCommand(String command) {
        byte[] writeBuffer;
        writeBuffer = new byte[3];

        switch (command) {
            case "B":


                break;
            case "G":

                break;
            default:
                break;
        }
    }

    @Override
    protected void sendDropCommand(){};
}


//    Context globalContext;
//
//    private double[] telemetry = {0,0,0,0,0,0,0,0};
//    private int refreshRate = 250;
//    public int port = 0;
//
//    private double startLat = 40.73581;
//    private double startLong = -73.99155;
//    private double inc = 0.0001;
//    private double r = 0.25;
//
//    public static final int LATITUDE = 0;
//    public static final int LONGITUDE = 1;
//    public static final int ALTITUDE = 2;
//    public static final int ROLL = 3;
//    public static final int PITCH = 4;
//    public static final int YAW = 5;
//    public static final int TIME = 6;
//    public static final int DIST = 7;
//
//    public double planeLong;
//    public double planeLat;
//    public double planeAlt;
//    public double planeTime;
//    public double planeHeading;
//    public double planePitch;
//    public double planeRoll;
//    public double planeDistance;
//
//    public boolean mThreadIsStopped = true;
//    protected boolean telemetryOpen = false;
//
//
//    public TestRadio(Context context){
//        this.globalContext = context;
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        openDevice();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        //Log.v(TAG, "Telemetry Destroyed!");
//        mThreadIsStopped = true;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    };
//
//
//
//    private void setTelemetry(double[] telem){
//        planeLong = telem[LONGITUDE];
//        planeLat = telem[LATITUDE];
//        planeAlt = telem[ALTITUDE];
//        planeTime = telem[TIME];
//        planeHeading = telem[YAW];
//        planePitch = telem[PITCH];
//        planeRoll = telem[ROLL];
//        planeDistance = telem[DIST];
//    }
//
//    protected void openDevice(){
//        Toast.makeText(globalContext, "Test Config", Toast.LENGTH_SHORT).show();
//        if(mThreadIsStopped){
//            new Thread(mLoop).start();
//        }
//    }
//    public void setConfig(){
//        Toast.makeText(globalContext, "Test Config", Toast.LENGTH_SHORT).show();
//    }
//    public void closeDevice(){
//        Toast.makeText(globalContext, "Test Close", Toast.LENGTH_SHORT).show();
//        mThreadIsStopped = true;
//        telemetryOpen = false;
//    }
//
//    protected void setUpUsbIfNeeded(){
//        Toast.makeText(globalContext, "Test USB Setup", Toast.LENGTH_SHORT).show();
//        // Check if already connected
//        if (telemetryOpen) {
//            String msg = "Port("+port+") is already opened.";
//            Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        setConfig();
//        Toast.makeText(globalContext, "Connected", Toast.LENGTH_SHORT).show();
//        openDevice();
//        telemetryOpen = true;
//    }
//
//    private Runnable mLoop = new Runnable(){
//        @Override
//        public void run(){
//            int readSize;
//            mThreadIsStopped = false;
//
//            while (true) {
//                if(mThreadIsStopped) {
//                    break;
//                }
//                try {
//                    sleep(refreshRate);
//                    Random rand = new Random();
//
//                    for(int i=0; i<telemetry.length; i++) {
//                        int  n = rand.nextInt(360) + 1;
//                        telemetry[i] = (double)n;
//
//                    }
//
//                    telemetry[0] = startLat;
//                    telemetry[1] = startLong;
//                    telemetry[5] = r;
//                    r+=0.25;
//                    if(r == 360){
//                        r = 0;
//                    }
//
//                    startLat += inc;
//                    startLong += inc;
//
//                    setTelemetry(telemetry);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//
////                synchronized (ftDev) {
////
////                    readSize = ftDev.getQueueStatus();
////                    if (readSize > 0) {
////
////                        if (readSize > READBUF_SIZE)
////                            readSize = READBUF_SIZE;
////
////                        ftDev.read(buffer, readSize);
////
////                        for(int i=0; i<telemetry.length; i++) {
////                            telemetry[i] = (double)buffer[i*8];
////                        }
////
////                        setTelemetry(telemetry);
////
////                    }
////                }
//
//
//            }
//        }
//    };
//}
