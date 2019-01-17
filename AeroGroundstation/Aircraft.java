
public class Aircraft {

    // drop gps for glider, needed in both plane and glider
    private double dropLoadPointLatitude;
    private double dropLoadPointLongitude;

    // radio strength of the glider and plane from ground station
    private double radioSignalStrengthRssi;

    // bluetooth strength data
    private double blueToothStrength;


    // below are the getter and setter functions for the airplane variables
    void setDropLoadPointLatitude(double dropLoadPointLatitudeValue)
    {
        this.dropLoadPointLatitude = dropLoadPointLatitudeValue;
    }

    double getDropLoadPointLatitude()
    {
        return this.dropLoadPointLatitude;
    }

    void setDropLoadPointLongitude(double dropLoadPointLongitudeValue)
    {
        this.dropLoadPointLongitude = dropLoadPointLongitudeValue;
    }

    double getDropLoadPointLongitude()
    {
        return this.dropLoadPointLongitude;
    }

    void updateRadioSignalStrength(double currentRadioSignalStrengthRssi)
    {
        this.radioSignalStrengthRssi = currentRadioSignalStrengthRssi;
    }

    double readRadioSignalStrength()
    {
        return this.radioSignalStrengthRssi;
    }

    void updateBlueToothStrength(double currentBlueToothStrength)
    {
        this.blueToothStrength = currentBlueToothStrength;
    }

    double readBlueToothStrength()
    {
        return this.blueToothStrength;
    }


}
