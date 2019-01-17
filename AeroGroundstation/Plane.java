/* List of future additions:
*   1. how are the member variables initialized, with a constructor or individual initialization?
*   2. need for both height and altitude?
*   3. should member variables be initialized to 0?
*   4. when to initialize dropGliderPoints
*
*
*
*
*/

public class Plane extends Aircraft {

    //important data for the plane
    private double yaw = 0.0;
    private double pitch = 0.0;
    private double roll = 0.0;
    private double height = 0.0;
    private double speed = 0.0;
    private double altitude = 0.0;
    private double latitude = 0.0;
    private double longitude = 0.0;

    // I think these variable is for the glider drop point GPS
    private double dropGliderPointLatitude;
    private double dropGliderPointLongitude;

    //getter and setter function for variables above
    void updatePlaneYaw(double currentPlaneYawValue)
    {
        this.yaw = currentPlaneYawValue;
    }

    double readPlaneYaw()
    {
        return yaw;
    }

    void updatePlanePitch(double currentPlanePitchValue)
    {
        this.pitch = currentPlanePitchValue;
    }

    double readPlanePitch()
    {
        return pitch;
    }

    void updatePlaneRoll(double currentPlaneRollValue)
    {
        this.roll = currentPlaneRollValue;
    }

    double readPlaneRoll()
    {
        return roll;
    }

    void updatePlaneHeight(double currentPlaneHeightValue)
    {
        this.height = currentPlaneHeightValue;
    }

    double readPlaneHeight()
    {
        return height;
    }

    void updatePlaneSpeed(double currentPlaneSpeedValue)
    {
        this.speed = currentPlaneSpeedValue;
    }

    double readPlaneSpeed()
    {
        return speed;
    }

    void updatePlaneAltitude(double currentPlaneAltitudeValue)
    {
        this.altitude = currentPlaneAltitudeValue;
    }

    double readPlaneAltitude()
    {
        return altitude;
    }

    void updatePlaneLatitude(double currentPlaneLatitudeValue)
    {
        this.latitude = currentPlaneLatitudeValue;
    }

    double readPlaneLatitude()
    {
        return latitude;
    }

    void updatePlaneLongitude(double currentPlaneLongitudeValue)
    {
        this.longitude = currentPlaneLongitudeValue;
    }

    double readPlaneLongitude()
    {
        return longitude;
    }

    void dropGliderPointLatitude(double dropGliderPointLatitudeValue)
    {
        this.dropGliderPointLatitude = dropGliderPointLatitudeValue;
    }

    double getPlaneDropGliderPointLatitude()
    {
        return dropGliderPointLatitude;
    }

    void dropGliderPointLongitude(double dropGliderPointLongitudeValue)
    {
        this.dropGliderPointLongitude = dropGliderPointLongitudeValue;
    }

    double getPlaneDropGliderPointLongitude()
    {
        return dropGliderPointLongitude;
    }
}
