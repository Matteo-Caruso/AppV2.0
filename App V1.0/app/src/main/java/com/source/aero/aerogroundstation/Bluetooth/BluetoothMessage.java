package com.source.aero.aerogroundstation.Bluetooth;

import java.nio.ByteBuffer;

public class BluetoothMessage {

    //Default Message elements defined as byte arrays
    private final byte msgStart = (byte) 10;
    private final byte msgEnd = (byte) 255;
    private short msgType = BluetoothConstantsInterface.GNDSTATIONCIRCUIT;
    private int targetLat = 0;
    private int targetLon = 0;
    private byte calibrate = BluetoothConstantsInterface.NOCALIBRATE;
    private byte rssi = (byte) 0;
    private byte dropRequest = BluetoothConstantsInterface.NODROP;
    private byte gliders = (byte) 0;
    private short[] motors = new short[16];
    private short error = (short) 0;

    public BluetoothMessage() {
        //Initialize default values for motors
        for (int i = 0; i < 16; i++) {
            motors[i] = 0;
        }
    }

    public void setTarget(double lat, double lon) {
        targetLat = (int) (lat*10000000);
        targetLon = (int) (lon*10000000);
    }

    public void setMsgType(short type) {
        msgType = type;
    }

    public void setCalibrate(byte device) {
        calibrate = device;
    }

    public void setRssi(byte val) {
        rssi = val;
    }

    public void setDropRequest(byte flag) {
        dropRequest = flag;
    }

    public void setGliders(int glider) {
        gliders = (byte) glider;
    }

    public void setMotor(int motor, short val) {
        motors[motor] = val;
    }

    public void setError(short val) {
        error = val;
    }

    //Construct message
    public byte[] makeMessage() {
        ByteBuffer message = ByteBuffer.allocate(52);
        message.put(msgStart);
        message.putShort(msgType);
        message.putInt(targetLat);
        message.putInt(targetLon);
        message.put(calibrate);
        message.put(rssi);
        message.put(dropRequest);
        message.put(gliders);
        for (int i = 0; i < 16; i++) {
            message.putShort(motors[i]);
        }
        message.putShort(error);
        message.put(msgEnd);

        return message.array();
    }
}