package com.source.aero.aerogroundstation.ContainerClasses;

import java.lang.String;
public class Vehicles
{
    // here we have the single plane and two gliders

    private Plane mainPlane = null;

    private Glider gliderOne = null;
    private Glider glidertwo = null;


    public boolean planeCheck(Plane isPlaneCreated)
    {
        return isPlaneCreated == null;
    }

    public boolean gliderCheck(Glider isGliderCreated)
    {
        return isGliderCreated == null;
    }

    // we come here if the current message is for a plane
    public void updatePlane(messageParser messageData)
    {
        // we make a new plane here if there wasn't one already made
        if(planeCheck(mainPlane))
        {
            mainPlane = new Plane();
        }

        // here we update the plane members, using the message crated by the parser class
        mainPlane.updateRadioSignalStrength(messageData.rssiFromMessage);
        mainPlane.updatePlaneYaw(messageData.yawFromMessage/100.0);
        mainPlane.updatePlanePitch(messageData.pitchFromMessage/100.0);
        mainPlane.updatePlaneRoll(messageData.rollFromMessaage/100.0);
        mainPlane.updatePlaneSpeed((int)((messageData.speedFromMessage*3.28)/100));   // ft/s
        mainPlane.updatePlaneAltitude((int)((messageData.altFromMessage*3.28)/10));   // ft
        mainPlane.updatePlaneLatitude(messageData.latFromMessage / 10000000.0);
        mainPlane.updatePlaneLongitude(messageData.lonFromMessage / 10000000.0);

        // here we set the drop location for the plane
        mainPlane.dropGliderPointLatitude(messageData.gDropLatFromMessage/10000000.0);
        mainPlane.dropGliderPointLongitude(messageData.gDropLonFromMessage/10000000.0);
    }


    // we come here if the message is for glider 1
    public void  updateGliderOne(messageParser messageData)
    {
        //make a new glider here is there wasn't one already made
        if(gliderCheck(gliderOne))
        {
            gliderOne = new Glider();
        }

        // here we update glider one data
        gliderOne.updateRadioSignalStrength(messageData.rssiFromMessage);
        gliderOne.updatePlaneYaw(messageData.yawFromMessage);
        gliderOne.updatePlanePitch(messageData.pitchFromMessage);
        gliderOne.updatePlaneRoll(messageData.rollFromMessaage);
        gliderOne.updatePlaneSpeed(messageData.speedFromMessage);
        gliderOne.updatePlaneAltitude(messageData.altFromMessage);
        gliderOne.updatePlaneLatitude(messageData.latFromMessage);
        gliderOne.updatePlaneLongitude(messageData.lonFromMessage);
        gliderOne.updateGliderPidOutputPitch(messageData.pidPitchFromMessage);
        gliderOne.updateGliderPidOutputYaw(messageData.pidYawFromMessage);
        gliderOne.updateGliderPidOutputRoll(messageData.pidRollFromMessage);

        // here we set the glider drop location
        gliderOne.updateGlidertargetLatitude(messageData.pDropLatFromMessage);
        gliderOne.updateGlidertargetLongitude(messageData.pDropLonFromMessage);
    }

    // come here if message is for glider 2
    public void  updateGliderTwo(messageParser messageData)
    {
        // here we create a new glider if this glider wasn't already created
        if(gliderCheck(glidertwo))
        {
            glidertwo = new Glider();
        }

        // here we update glider one data
        gliderOne.updateRadioSignalStrength(messageData.rssiFromMessage);
        gliderOne.updatePlaneYaw(messageData.yawFromMessage);
        gliderOne.updatePlanePitch(messageData.pitchFromMessage);
        gliderOne.updatePlaneRoll(messageData.rollFromMessaage);
        gliderOne.updatePlaneSpeed(messageData.speedFromMessage);
        gliderOne.updatePlaneAltitude(messageData.altFromMessage);
        gliderOne.updatePlaneLatitude(messageData.latFromMessage);
        gliderOne.updatePlaneLongitude(messageData.lonFromMessage);
        gliderOne.updateGliderPidOutputPitch(messageData.pidPitchFromMessage);
        gliderOne.updateGliderPidOutputYaw(messageData.pidYawFromMessage);
        gliderOne.updateGliderPidOutputRoll(messageData.pidRollFromMessage);

        // here we set the glider drop location
        gliderOne.updateGlidertargetLatitude(messageData.pDropLatFromMessage);
        gliderOne.updateGlidertargetLongitude(messageData.pDropLonFromMessage);
    }

    public Plane getPlaneData()
    {
        return this.mainPlane;
    }

    public Glider getGliderOne()
    {
        return this.gliderOne;
    }

    public Glider getGliderTwo()
    {
        return this.glidertwo;
    }


}
