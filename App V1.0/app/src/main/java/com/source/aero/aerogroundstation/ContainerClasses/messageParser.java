package com.source.aero.aerogroundstation.ContainerClasses;

import java.nio.ByteBuffer;

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
    messageParser(ByteBuffer messageBuffer)
    {
        // this variables lets us know who the message is for
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

    // this function would create a message sent to the plane
    //convertMessageToByteBuffer();





}
