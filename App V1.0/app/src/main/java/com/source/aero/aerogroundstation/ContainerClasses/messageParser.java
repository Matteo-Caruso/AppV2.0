package com.source.aero.aerogroundstation.ContainerClasses;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class messageParser {

    public short whoIsThisMesssageFor = -1;
    public int latFromMessage = 0;
    public int lonFromMessage = 0;
    public short yawFromMessage = 0;
    public short rollFromMessaage = 0;
    public short pitchFromMessage = 0;
    public short speedFromMessage = 0;
    public short altFromMessage = 0;
    public short pidYawFromMessage = 0;
    public short pidPitchFromMessage = 0;
    public short pidRollFromMessage = 0;
    public int gDropLatFromMessage = 0;
    public int gDropLonFromMessage = 0;
    public int pDropLatFromMessage = 0;
    public int pDropLonFromMessage = 0;
    public short  rssiFromMessage = 0;
    public short errorFromMessage = 0;

    // constructor intializes the plane/glider variables
    public messageParser(ByteBuffer messageBuffer)
    {
        // this variables lets us know who the message is for
        messageBuffer.order(ByteOrder.LITTLE_ENDIAN);
        whoIsThisMesssageFor = messageBuffer.getShort(1);
        latFromMessage = messageBuffer.getInt(3);
        lonFromMessage = messageBuffer.getInt(7);
        yawFromMessage = messageBuffer.getShort(11);
        pitchFromMessage = messageBuffer.getShort(13);
        rollFromMessaage = messageBuffer.getShort(15);
        speedFromMessage = messageBuffer.getShort(17);
        altFromMessage = messageBuffer.getShort(19);
        pidYawFromMessage = messageBuffer.getShort(21);
        pidPitchFromMessage = messageBuffer.getShort(23);
        pidRollFromMessage = messageBuffer.getShort(25);
        gDropLatFromMessage = messageBuffer.getInt(27);
        gDropLonFromMessage = messageBuffer.getInt(31);
        pDropLatFromMessage = messageBuffer.getInt(35);
        pDropLonFromMessage = messageBuffer.getInt(39);
        rssiFromMessage = messageBuffer.getShort(43);
        errorFromMessage = messageBuffer.getShort(44);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Msg type: ");
        sb.append(whoIsThisMesssageFor);

        sb.append(" Lat: ");
        sb.append(latFromMessage);

        sb.append(" Lon: ");
        sb.append(lonFromMessage);

        sb.append(" Yaw: ");
        sb.append(yawFromMessage);

        sb.append(" Roll: ");
        sb.append(rollFromMessaage);

        sb.append(" Pitch: ");
        sb.append(pitchFromMessage);

        sb.append(" Speed: ");
        sb.append(speedFromMessage);

        sb.append(" Altitude: ");
        sb.append(altFromMessage);

        sb.append(" PID Yaw: ");
        sb.append(pidYawFromMessage);

        sb.append(" PID Roll: ");
        sb.append(pidPitchFromMessage);

        sb.append(" PID Pitch: ");
        sb.append(pidRollFromMessage);

        sb.append(" Glider Drop Lat: ");
        sb.append(gDropLatFromMessage);

        sb.append(" Glider Drop Lon: ");
        sb.append(gDropLonFromMessage);

        sb.append(" Plane Drop Lat: ");
        sb.append(pDropLatFromMessage);

        sb.append(" Plane Drop Lon: ");
        sb.append(pDropLonFromMessage);

        sb.append(" RSSI: ");
        sb.append(rssiFromMessage);

        sb.append(" Error: ");
        sb.append(errorFromMessage);

        return sb.toString();
    }


    // this function would create a message sent to the plane
    //convertMessageToByteBuffer();

}
