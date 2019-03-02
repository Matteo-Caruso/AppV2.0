package com.source.aero.aerogroundstation.ContainerClasses;

public class Plane implements GeneralAircraft {

    private double rssi = 0.0;
    private double blueToothStrength = 0.0;
    private double yaw = 0.0;
    private double pitch = 0.0;
    private double roll = 0.0;
    private double height = 0.0;
    private double speed = 0.0;
    private double altitude = 0.0;
    private double latitude = 0.0;
    private double longitude = 0.0;

    // I think these variable is for the glider drop point GPS (drop lat and drop lon)
    private double dropGliderPointLatitude;
    private double dropGliderPointLongitude;

    //getter and setter function for variables above

    public double readRadioSignalStrength()
    {
        return this.rssi;
    }

    public void updateRadioSignalStrength(double currentRadioSignalStrengthRssi)
    {
        this.rssi = currentRadioSignalStrengthRssi;
    }

    public double readBlueToothStrength()
    {
        return this.blueToothStrength;
    }

    public void updateBlueToothStrength(double currentBlueToothStrength)
    {
        this.blueToothStrength = currentBlueToothStrength;
    }

    public void updatePlaneYaw(double currentPlaneYawValue)
    {
        this.yaw = currentPlaneYawValue;
    }

    public double readPlaneYaw()
    {
        return this.yaw;
    }

    public void updatePlanePitch(double currentPlanePitchValue)
    {
        this.pitch = currentPlanePitchValue;
    }

    public double readPlanePitch()
    {
        return this.pitch;
    }

    public void updatePlaneRoll(double currentPlaneRollValue)
    {
        this.roll = currentPlaneRollValue;
    }

    public double readPlaneRoll()
    {
        return this.roll;
    }

    public void updatePlaneHeight(double currentPlaneHeightValue)
    {
        this.height = currentPlaneHeightValue;
    }

    public double readPlaneHeight()
    {
        return this.height;
    }

    public void updatePlaneSpeed(double currentPlaneSpeedValue)
    {
        this.speed = currentPlaneSpeedValue;
    }

    public double readPlaneSpeed()
    {
        return this.speed;
    }

    public void updatePlaneAltitude(double currentPlaneAltitudeValue)
    {
        this.altitude = currentPlaneAltitudeValue;
    }

    public double readPlaneAltitude()
    {
        return this.altitude;
    }

    public void updatePlaneLatitude(double currentPlaneLatitudeValue)
    {
        this.latitude = currentPlaneLatitudeValue;
    }

    public double readPlaneLatitude()
    {
        return this.latitude;
    }

    public void updatePlaneLongitude(double currentPlaneLongitudeValue)
    {
        this.longitude = currentPlaneLongitudeValue;
    }

    public double readPlaneLongitude()
    {
        return this.longitude;
    }

    void dropGliderPointLatitude(double dropGliderPointLatitudeValue)
    {
        this.dropGliderPointLatitude = dropGliderPointLatitudeValue;
    }

    double getPlaneDropGliderPointLatitude()
    {
        return this.dropGliderPointLatitude;
    }

    void dropGliderPointLongitude(double dropGliderPointLongitudeValue)
    {
        this.dropGliderPointLongitude = dropGliderPointLongitudeValue;
    }

    double getPlaneDropGliderPointLongitude()
    {
        return this.dropGliderPointLongitude;
    }
}
