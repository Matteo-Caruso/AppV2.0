package com.code.aero.groundstation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

import retrofit2.http.HEAD;

import static java.lang.Thread.sleep;

/**
 * Created by Carl on 2018-02-26.
 */

public abstract class AbstractRadio extends FragmentActivity {
    Context globalContext;

    Handler handler = new Handler();
    protected float[] telemetry = {0,0,0,0,0,0,0,0,0,0,0};
    protected int refreshRate = 250;
    public int port = 0;


    public static final int LATITUDE = 0;
    public static final int LONGITUDE = 1;
    public static final int ALTITUDE = 2;
    public static final int ROLL = 3;
    public static final int PITCH = 4;
    public static final int YAW = 5;
    public static final int SPEED = 6;
    public static final int DROP = 7;
    public static final int HEADING = 8;
    public static final int DROP_HEIGHT = 9;


    public float planeLat;
    public float planeLong;
    public float planeAlt;
    public float planeRoll;
    public float planePitch;
    public float planeYaw;
    public float planeSpeed;

    public float planeDropTime;
    public float planeHeadingTime;
    public float planeDropHeight;

    public boolean mThreadIsStopped = true;
    protected boolean telemetryOpen = false;

    public AbstractRadio(){}

    public AbstractRadio(Context context){
        this.globalContext = context;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        openDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.v(TAG, "Telemetry Destroyed!");
        mThreadIsStopped = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    };

    public float[] getTelemetry(){

        //Toast.makeText(globalContext, "gettelem", Toast.LENGTH_SHORT).show();

        float[] data = {0,0,0,0,0,0,0,0,0,0,0};

        data[LATITUDE] = planeLat;
        data[LONGITUDE] = planeLong;
        data[ALTITUDE] = planeAlt;
        data[ROLL] = planeRoll;
        data[PITCH] = planePitch;
        data[YAW] = planeYaw;
        data[SPEED] = planeSpeed;

        data[DROP] = planeDropTime;
        data[HEADING] = planeHeadingTime;
        data[DROP_HEIGHT] = planeDropHeight;


        return data;
    }

    public void stopThread(){
        mThreadIsStopped = true;
    }

    public void startThread(){
        mThreadIsStopped = false;
        new Thread(mLoop).start();
    }

    protected void setTelemetry(float[] telem){

        planeLat = telem[LATITUDE];
        planeLong = telem[LONGITUDE];
        planeAlt = telem[ALTITUDE];
        planeRoll = telem[ROLL];
        planePitch = telem[PITCH];
        planeYaw = telem[YAW];
        planeSpeed = telem[SPEED];

        planeDropTime = telem[DROP];
        planeHeadingTime = telem[HEADING];
        planeDropHeight = telem[DROP_HEIGHT];

    }

    protected abstract void openDevice();

    protected abstract void setConfig();

    protected abstract void closeDevice();

    protected abstract void setUpUsbIfNeeded();

    protected abstract void handleData();

    protected abstract void sendPayloadCommand(String command);

    protected abstract void sendUnloadCommand(String command);

    protected abstract void sendZeroCommand(String command);

    protected abstract void sendDoorCommand(String command);

    protected abstract void sendDropCommand();

    protected abstract void sendTarget(float latitude, float longitiude);

    protected abstract void sendAutoMode(boolean a);

    protected Runnable mLoop = new Runnable(){
        @Override
        public void run(){
            int readSize;
            //mThreadIsStopped = false;

            while (true) {
                if(mThreadIsStopped) {
                    break;
                }
                try {
                    sleep(refreshRate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleData();
                    }
                });
            }
        }
    };
}


//package com.code.aero.groundstation;
//
//        import android.content.BroadcastReceiver;
//        import android.content.Context;
//        import android.content.Intent;
//        import android.content.IntentFilter;
//        import android.hardware.usb.UsbManager;
//        import android.os.Handler;
//        import android.support.v4.app.FragmentActivity;
//        import android.util.Log;
//        import android.widget.Toast;
//
//        import com.ftdi.j2xx.D2xxManager;
//        import com.ftdi.j2xx.FT_Device;
//
//        import java.io.BufferedWriter;
//        import java.io.IOException;
//
//
//public class RadioCommActivity extends FragmentActivity {
//
//    private FT_Device ftDev;
//    private D2xxManager ftD2xx = null;
//
//    private double[] telemetry = {0,0,0,0,0,0,0,0};
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
//    protected boolean telemetryOpen = false;
//    protected String TAG = "TAG";
//
//    public int port = 0;
//
//    protected BroadcastReceiver mUsbReceiver;
//    protected IntentFilter filter;
//
//    Context global_context;
//
//    public static final int READBUF_SIZE  = 256;
//    private byte[] buffer  = new byte[READBUF_SIZE];
//    protected BufferedWriter bfWriter;
//
//    public boolean mThreadIsStopped = true;     //implemented in runnable, therefore not needed
//
//    public final byte XON = 0x11;    /* Resume transmission */
//    public final byte XOFF = 0x13;    /* Pause transmission */
//
//    private final int BAUD = 57600;
//
//    Handler mHandler = new Handler();
//
//    public RadioCommActivity(Context context) {
//
//        this.global_context = context;
//
//        mUsbReceiver = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                    // never come here(when attached, go to onNewIntent)
//                    openDevice();
//                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                    closeDevice();
//                }
//            }
//        };
//
//        // Initialize USB socket
//        try {
//            ftD2xx = D2xxManager.getInstance(global_context);
//        } catch (D2xxManager.D2xxException e) {
//            Log.e("FTDI_HT", "getInstance fail!!");
//        }
//        filter = new IntentFilter();
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//
//
//        global_context.registerReceiver(mUsbReceiver, filter);
//
//    }
//
//    private void setTelemetry(double[] telem) {
//
//        planeLong = telem[LONGITUDE];
//        planeLat = telem[LATITUDE];
//        planeAlt = telem[ALTITUDE];
//        planeTime = telem[TIME];
//        planeHeading = telem[YAW];
//        planePitch = telem[PITCH];
//        planeRoll = telem[ROLL];
//        planeDistance = telem[DIST];
//
//        //currentTime = System.currentTimeMillis() - startTime;   // time
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        openDevice();
//    }
//
//
//    void setConfig() {
//        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET); // reset to UART mode for 232 devices
//        ftDev.setBaudRate(BAUD);
//        ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1,
//                D2xxManager.FT_PARITY_NONE);
//        ftDev.setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, XON, XOFF);
//    }
//
//    protected void openDevice() {
//        if (ftDev != null) {
//            if (ftDev.isOpen()) {
//                if(mThreadIsStopped) {
//                    //updateView(true);
//                    setConfig();
//                    ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
//                    ftDev.restartInTask();
//                    new Thread(mLoop).start();
//
//                }
//                return;
//            }
//        }
//
//        int devCount = 0;
//        devCount = ftD2xx.createDeviceInfoList(global_context);
//
//        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
//        ftD2xx.getDeviceInfoList(devCount, deviceList);
//
//        if (devCount <= 0) {
//            //Error catching
//            return;
//        }
//
//        if (ftDev == null) {
//            ftDev = ftD2xx.openByIndex(global_context, 0);
//        } else {
//            synchronized (ftDev) {
//                ftDev = ftD2xx.openByIndex(global_context, 0);
//            }
//        }
//
//        if (ftDev.isOpen()) {
//            if(mThreadIsStopped) {
//                setConfig();
//                ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
//                ftDev.restartInTask();
//                new Thread(mLoop).start();
//            }
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        //Log.v(TAG, "Telemetry Destroyed!");
//        mThreadIsStopped = true;
//        global_context.unregisterReceiver(mUsbReceiver);
//    }
//
//    /*    @Override
//        protected void onPause() {
//            super.onPause();
//            unregisterReceiver(mUsbReceiver);
//        };*/
//    public void closeDevice() {
//        mThreadIsStopped = true;
//        telemetryOpen = false;
//        //updateView(false);
//        if (ftDev != null) {
//            ftDev.close();
//            try {
//                bfWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * Sets up the USB socket if not already opened. Checks for connectivity
//     * and then opens 'port 0' since there is only one USB port on the
//     * device. This step is critical for communication with the plane.
//     */
//    protected void setUpUsbIfNeeded() {
//        // Check if already connected
//        if (telemetryOpen) {
//            String msg = "Port("+port+") is already opened.";
//            Toast.makeText(global_context, msg, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // Check whether there is a device plugged in
//        if (ftD2xx.createDeviceInfoList(global_context) < 1) {
//            String msg = "Connect the USB radio";
//            Toast.makeText(global_context, msg, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // Open the device on port 0 (USB radio by default)
//        ftDev = ftD2xx.openByIndex(global_context, 0);
//
//        // Check for successful connection
//        if (ftDev == null) {
//            String msg = "Connect the USB radio";
//            Toast.makeText(global_context, msg, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        setConfig();
//        Toast.makeText(global_context, "Connected", Toast.LENGTH_SHORT).show();
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
//                synchronized (ftDev) {
//
//                    readSize = ftDev.getQueueStatus();
//                    if (readSize > 0) {
//
//                        if (readSize > READBUF_SIZE)
//                            readSize = READBUF_SIZE;
//
//                        ftDev.read(buffer, readSize);
//
//                        for(int i=0; i<telemetry.length; i++) {
//                            telemetry[i] = (double)buffer[i*8];
//                        }
//
//                        setTelemetry(telemetry);
//
//                    }
//                }
//            }
//        }
//    };
//
//
//
//}




