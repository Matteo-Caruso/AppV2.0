package com.source.aero.aerogroundstation.ContainerClasses;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import java.nio.ByteBuffer;
import java.lang.String;

public class testParser {

    public static void main(String args[])
    {
        // test code below to check whether the pareser worked
        try {
            byte[] testMessage = Hex.decodeHex("0a0000029035D0FB27D200010201020000000100020003000400050006000700080009000a000b000c000d000e000f0000ff");
            ByteBuffer testBuffer = ByteBuffer.wrap(testMessage);
            messageParser testParser = new messageParser(testBuffer);


        }catch(DecoderException exception)
        {
            System.out.println(exception.getMessage());
        }



    }
}
