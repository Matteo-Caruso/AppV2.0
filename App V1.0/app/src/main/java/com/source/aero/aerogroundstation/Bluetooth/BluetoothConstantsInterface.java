package com.source.aero.aerogroundstation.Bluetooth;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface BluetoothConstantsInterface {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final int MESSAGE_BUFFER_SIZE = 1024;

    //Message constants
    public static final byte MSGSTART = (byte) 10;
    public static final byte MSGEND = (byte) 127;
    public static final short GNDSTATIONCIRCUIT = 0;
    public static final short PLANE = 1;
    public static final short GLIDER1 = 2;
    public static final short GLIDER2 = 3;
    public static final short DEBUG = 4;
    public static final byte NOCALIBRATE = (byte) 0;
    public static final byte CALIBRATEIMU = (byte) 1;
    public static final byte CALIBRATEGPS = (byte) 2;
    public static final byte CALIBRATEBAROMETER = (byte) 3;
    public static final byte NODROP = (byte) 0;
    public static final byte DROPWATER = (byte) 1;
    public static final byte DROPHABITAT = (byte) 3;
    public static final byte DROPGLIDERS = (byte) 2;
    public static final short MOTOROFF = 2;
    public static final short MOTORON = 1;
    public static final short ERROR = 0;

}
