package com.source.aero.aerogroundstation.ContainerClasses;

import java.nio.ByteBuffer;

public class GndToPlaneMsg {
    private byte startByte;
    private short msgType;
    private int targetLat;
    private int targetLon;
    private byte calibrate;
    private byte rssi;
    private byte dropRequest;
    private byte gliders;

    private short motor1;
    private short motor2;
    private short motor3;
    private short motor4;
    private short motor5;
    private short motor6;
    private short motor7;
    private short motor8;
    private short motor9;
    private short motor10;
    private short motor11;
    private short motor12;
    private short motor13;
    private short motor14;
    private short motor15;
    private short motor16;

    private short error;
    private byte endByte;

    public GndToPlaneMsg(short type)
    {
        startByte = (byte) 10;
        msgType = type;

        targetLat = 0;
        targetLon = 0;
        calibrate = (byte) 0;
        rssi = (byte) 0;
        dropRequest = (byte) 0;
        gliders = (byte) 0;

        motor1 = 0;
        motor2 = 0;
        motor3 = 0;
        motor4 = 0;
        motor5 = 0;
        motor6 = 0;
        motor7 = 0;
        motor8 = 0;
        motor9 = 0;
        motor10 = 0;
        motor11 = 0;
        motor12 = 0;
        motor13 = 0;
        motor14 = 0;
        motor15 = 0;
        motor16 = 0;

        endByte = (byte) 255;
    }

    public byte[] buildByteBuffer()
    {
        ByteBuffer buf = ByteBuffer.allocate(52);
        buf.put(startByte);

        buf.putShort(msgType);

        buf.putInt(targetLat);
        buf.putInt(targetLon);

        buf.put(calibrate);
        buf.put(rssi);
        buf.put(dropRequest);
        buf.put(gliders);

        buf.putShort(motor1);
        buf.putShort(motor2);
        buf.putShort(motor3);
        buf.putShort(motor4);
        buf.putShort(motor5);
        buf.putShort(motor6);
        buf.putShort(motor7);
        buf.putShort(motor8);
        buf.putShort(motor9);
        buf.putShort(motor10);
        buf.putShort(motor11);
        buf.putShort(motor12);
        buf.putShort(motor13);
        buf.putShort(motor14);
        buf.putShort(motor15);
        buf.putShort(motor16);

        buf.putShort(error);

        buf.put(endByte);

        return buf.array();
    }

    public void setError(short e)
    {
        error = e;
    }
    public void setTarget(double lat, double lon)
    {
        targetLat = (int)(lat * 10000000);
        targetLon = (int)(lon * 10000000);
    }

    public void setCalibrate(byte b)
    {
        calibrate = b;
    }

    public void setRSSI(byte b)
    {
        rssi = b;
    }

    public void setDropRequest(byte b)
    {
        dropRequest = b;
    }

    public void setGliders(byte b)
    {
        gliders = b;
    }

    // Worst code ever
    public void setMotor(int motor, short value)
    {
        switch (motor){
            case 1:
            {
                motor1 = value;
                break;
            }
            case 2:
            {
                motor2 = value;
                break;
            }
            case 3:
            {
                motor3 = value;
                break;
            }
            case 4:
            {
                motor4 = value;
                break;
            }
            case 5:
            {
                motor5 = value;
                break;
            }
            case 6:
            {
                motor6 = value;
                break;
            }
            case 7:
            {
                motor7 = value;
                break;
            }
            case 8:
            {
                motor8 = value;
                break;
            }
            case 9:
            {
                motor9 = value;
                break;
            }
            case 10:
            {
                motor10 = value;
                break;
            }
            case 11:
            {
                motor11 = value;
                break;
            }
            case 12:
            {
                motor12 = value;
                break;
            }
            case 13:
            {
                motor13 = value;
                break;
            }
            case 14:
            {
                motor14 = value;
                break;
            }
            case 15:
            {
                motor15 = value;
                break;
            }
            case 16:
            {
                motor16 = value;
                break;
            }
        }
    }

}
