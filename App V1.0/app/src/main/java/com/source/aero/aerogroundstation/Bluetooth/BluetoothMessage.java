package com.source.aero.aerogroundstation.Bluetooth;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class BluetoothMessage {

    //Default Message elements defined as byte arrays
    private final byte[] msgStart = {(byte) 10};
    private final byte[] msgEnd = {(byte) 127};
    private byte[] msgType = shortToByte(BluetoothConstantsInterface.GNDSTATIONCIRCUIT);
    private byte[] targetLat = intToByte(0);
    private byte[] targetLon = intToByte(0);
    private byte[] calibrate = {BluetoothConstantsInterface.NOCALIBRATE};
    private byte[] rssi = {(byte) 0};
    private byte[] dropRequest = {BluetoothConstantsInterface.NODROP};
    private byte[] gliders = {(byte) 0};
    private byte[][] motors = new byte[16][];
    private byte[] error = shortToByte((short)0);

    public BluetoothMessage(short type) {
        msgType = shortToByte(type);
        //Initialize default values for motors
        for (int i = 0; i < 16; i++) {
            motors[i] = shortToByte(BluetoothConstantsInterface.MOTOROFF);
        }
    }

    public void setTarget(int[] coordinates) {
        targetLat = intToByte(coordinates[0]);
        targetLon = intToByte(coordinates[1]);
    }

    public void setCalibrate(byte device) {
        calibrate[0] = device;
    }

    public void setRssi(byte val) {
        rssi[0] = val;
    }

    public void setDropRequest(byte flag) {
        dropRequest[0] = flag;
    }

    public void setGliders(int glider) {
        gliders[0] = (byte) glider;
    }

    public void setMotor(int motor, short val) {
        motors[motor] = shortToByte(val);
    }

    public void setError(short val) {
        error = shortToByte(val);
    }

    //Construct message by concatenating byte arrays
    public byte[] makeMessage() {
        byte[] message1 = concat(msgStart,msgType,targetLat,targetLon,calibrate,rssi,dropRequest,
                gliders);
        byte[] message2 = motors[0];
        //Concatenate motor arrays
        for (int i = 1; i < 16; i++) {
            message2 = concat(message2,motors[i]);
        }
        byte[] message = concat(message2,error,msgEnd);
        return message;
    }

    public byte[] shortToByte(short val) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(val);
        return buffer.array();
    }

    public byte[] intToByte(int val) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(val);
        return buffer.array();
    }

    //Concatenate byte arrays
    public byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array: arrays) {
            System.arraycopy(array,0,result,offset, array.length);
            offset += array.length;
        }
        return result;
    }

}