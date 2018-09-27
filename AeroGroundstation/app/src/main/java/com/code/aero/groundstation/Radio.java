package com.code.aero.groundstation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * Created by Carl on 2018-02-26.
 */

public class Radio extends AbstractRadio {

    private FT_Device ftDev;
    private D2xxManager ftD2xx = null;

    public static final int READBUF_SIZE = 512;
    private byte[] buffer = new byte[READBUF_SIZE];

    public static final int mSize = 34;

    byte msgBuf[] = new byte[256];
    int msgBufPos = 0;

    protected BufferedWriter bfWriter;

    //protected BroadcastReceiver mUsbReceiver;
    protected IntentFilter filter;

    public final byte XON = 0x11;    /* Resume transmission */
    public final byte XOFF = 0x13;    /* Pause transmission */

    private final int BAUD = 57600;

    protected String TAG = "TAG";

    public Radio() {
    }

    public Radio(Context context) {
        this.globalContext = context;


        // Initialize USB socket
        try {
            ftD2xx = D2xxManager.getInstance(globalContext);
        } catch (D2xxManager.D2xxException e) {
            Log.e("FTDI_HT", "getInstance fail!!");
        }
        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        globalContext.registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.v(TAG, "Telemetry Destroyed!");
        mThreadIsStopped = true;
        globalContext.unregisterReceiver(mUsbReceiver);
    }

    @Override
    protected void openDevice() {
        refreshRate = 10;
        if (ftDev != null) {
            if (ftDev.isOpen()) {
                if (mThreadIsStopped) {
                    //updateView(true);
                    setConfig();
                    ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                    ftDev.restartInTask();
                    new Thread(mLoop).start();

                }
                return;
            }
        }

        int devCount = 0;
        devCount = ftD2xx.createDeviceInfoList(globalContext);

        Log.d(TAG, "Device number : " + Integer.toString(devCount));

        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
        ftD2xx.getDeviceInfoList(devCount, deviceList);

        if (devCount <= 0) {
            //Error catching
            return;
        }

        if (ftDev == null) {
            ftDev = ftD2xx.openByIndex(globalContext, 0);
        } else {
            synchronized (ftDev) {
                ftDev = ftD2xx.openByIndex(globalContext, 0);
            }
        }

        if (ftDev.isOpen()) {
            if (mThreadIsStopped) {
                setConfig();
                ftDev.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                ftDev.restartInTask();
                new Thread(mLoop).start();
            }
        }
    }

    @Override
    protected void setConfig() {

        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET); // reset to UART mode for 232 devices
        ftDev.setBaudRate(BAUD);
        ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1,
                D2xxManager.FT_PARITY_NONE);

        //ftDev.setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, XON, XOFF);
        ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x0b, (byte) 0x0d);
        Toast.makeText(globalContext, "comfig", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void closeDevice() {
        mThreadIsStopped = true;
        telemetryOpen = false;
        //updateView(false);
        if (ftDev != null) {
            ftDev.close();
        }
    }

    @Override
    protected void setUpUsbIfNeeded() {
        // Check if already connected
        if (telemetryOpen) {
            //String msg = "Port(" + port + ") is already opened.";
            //Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check whether there is a device plugged in
        if (ftD2xx.createDeviceInfoList(globalContext) < 1) {
            String msg = "Connect the USB radio";
            Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        // Open the device on port 0 (USB radio by default)
        ftDev = ftD2xx.openByIndex(globalContext, 0);

        // Check for successful connection
        if (ftDev == null) {
            String msg = "Connect the USB radio";
            Toast.makeText(globalContext, msg, Toast.LENGTH_SHORT).show();
            return;
        }

        setConfig();
        Toast.makeText(globalContext, "Connected", Toast.LENGTH_SHORT).show();
        openDevice();
        telemetryOpen = true;
    }

    @Override
    protected void handleData() {
        int readSize;

        synchronized (ftDev) {

            readSize = ftDev.getQueueStatus();
            if (readSize > 0) {

                if (readSize > READBUF_SIZE)
                    readSize = READBUF_SIZE;

                ftDev.read(buffer, readSize);
                int i = 0;

                if (msgBufPos == 0) {
                    while (i < readSize && buffer[i] != 0x02) i++;
                }

                while (i < readSize && msgBufPos < 38) msgBuf[msgBufPos++] = buffer[i++];

                if (msgBufPos == 38) {
                    ByteBuffer bb = ByteBuffer.wrap(msgBuf);
                    final byte START = bb.get();
                    final byte  ID = bb.get();
                    telemetry[LATITUDE] = bb.getFloat();
                    telemetry[LONGITUDE] = bb.getFloat();
                    telemetry[ALTITUDE] = (float) 3.28084*bb.getFloat();
                    telemetry[ROLL] = bb.getFloat();
                    telemetry[PITCH] = bb.getFloat();
                    telemetry[YAW] = bb.getFloat();
                    telemetry[SPEED]= bb.getFloat();
                    telemetry[DROP] = 0;
                    telemetry[HEADING] = bb.getFloat();
                    telemetry[DROP_HEIGHT] = (float) 3.28084*bb.getFloat();



                    setTelemetry(telemetry);
                    msgBufPos = 0;
                }
            }
        }
    }

    @Override
    //payload command = 0x00 0x00
    protected void sendPayloadCommand(String command) {
        byte[] writeBuffer;
        writeBuffer = new byte[4];

        switch (command) {
            case "FL":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x00;
                writeBuffer[2] = 0x00;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            case "FR":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x00;
                writeBuffer[2] = 0x01;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            case "BL":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x00;
                writeBuffer[2] = 0x02;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            case "BR":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x00;
                writeBuffer[2] = 0x03;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);
                ftDev.write(writeBuffer);
                break;
            default:
                break;
        }
    }

    //payload command = 0x01
    protected void sendUnloadCommand(String command) {
        byte[] writeBuffer;
        writeBuffer = new byte[4];

        switch (command) {
            case "FL":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x01;
                writeBuffer[2] = 0x00;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            case "FR":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x01;
                writeBuffer[2] = 0x01;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            case "BL":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x01;
                writeBuffer[2] = 0x02;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            case "BR":

                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x01;
                writeBuffer[2] = 0x03;
                writeBuffer[3] = (byte) (writeBuffer[0] ^ writeBuffer[1] ^ writeBuffer[2]);

                ftDev.write(writeBuffer);
                break;
            default:
                break;
        }
    }

    @Override
    //zero baro command = 0x00 0x10
    //zero gps command = 0x00 0x11
    protected void sendZeroCommand(String command) {
        byte[] writeBuffer;
        writeBuffer = new byte[3];

        switch (command) {
            case "B":
                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x02;
                writeBuffer[2] = (byte) (writeBuffer[0] ^ writeBuffer[1]);
                ftDev.write(writeBuffer);

                break;
            case "G":
                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x03;
                writeBuffer[2] = (byte) (writeBuffer[0] ^ writeBuffer[1]);
                ftDev.write(writeBuffer);
                break;
            default:
                break;
        }

    }

    @Override
    //door open = 0x01 0x00
    //door closed = 0x01 0x01
    protected void sendDoorCommand(String command) {
        byte[] writeBuffer;
        writeBuffer = new byte[3];

        switch (command) {
            case "DO":
                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x04;
                writeBuffer[2] = (byte) (writeBuffer[0] ^ writeBuffer[1]);
                ftDev.write(writeBuffer);

                break;
            case "DC":
                writeBuffer[0] = 0x02;
                writeBuffer[1] = 0x05;
                writeBuffer[2] = (byte) (writeBuffer[0] ^ writeBuffer[1]);
                ftDev.write(writeBuffer);
                break;
            default:
                break;
        }
    }

    @Override
    protected void sendDropCommand(){
        byte[] writeBuffer;
        writeBuffer = new byte[3];

        writeBuffer[0] = 0x02;
        writeBuffer[1] = 0x06;
        writeBuffer[2] = (byte) (writeBuffer[0] ^ writeBuffer[1]);
        ftDev.write(writeBuffer);

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

        for(int i = 0; i < 8; i++){
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

        ftDev.write(writeBuffer);

    }

    //Command 0x08, if 2 is 0x01, its enabled
    @Override
    protected void sendAutoMode(boolean a){
        byte[] writeBuffer;
        writeBuffer = new byte[4];

        writeBuffer[0] = 0x02;
        writeBuffer[1] = 0x08;

        if(a){
            writeBuffer[2] = 0x01;
        }else{
            writeBuffer[2] = 0x00;
        }

        writeBuffer[3] = (byte)(writeBuffer[0]^
                writeBuffer[1]^
                writeBuffer[2]);

        ftDev.write(writeBuffer);
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                // never come here(when attached, go to onNewIntent)
                openDevice();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                closeDevice();
            }
        }
    };

}