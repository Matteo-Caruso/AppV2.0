import java.lang.String;

/* List of future additions:
 *   1. how are the member variables initialized, with a constructor or individual initialization?
 *   2. need for both height and altitude?
 *   3. should member variables be initialized to 0?
 *
 */

public class Glider extends Aircraft {

        // important data of glider
        private double yaw = 0.0;
        private double pitch = 0.0;
        private double roll = 0.0;
        private double height = 0.0;
        private double speed = 0.0;
        private double altitude = 0.0;
        private double latitude = 0.0;
        private double longitude = 0.0;
        private double pidOutputPitch = 0.0;
        private double pidOutputYaw = 0.0;

        // identification of various gliders
        private String gliderName;


        // let the glider know, the load should be dropped
        private boolean loadDropActivation = false;

        //getter and setter functions for variables above

        void updateGliderYaw(double currentGliderYawValue)
        {
                this.yaw = currentGliderYawValue;
        }

        double readGliderYaw()
        {
                return yaw;
        }

        void updateGliderPitch(double currentGliderPitchValue)
        {
                this.pitch = currentGliderPitchValue;
        }

        double readGliderPitch()
        {
                return pitch;
        }

        void updateGliderRoll(double currentGliderRollValue)
        {
                this.roll = currentGliderRollValue;
        }

        double readGliderRoll()
        {
                return roll;
        }

        void updateGliderHeight(double currentGliderHeightValue)
        {
                this.height = currentGliderHeightValue;
        }

        double readGliderHeight()
        {
                return height;
        }

        void updateGliderSpeed(double currentGliderSpeedValue)
        {
                this.speed = currentGliderSpeedValue;
        }

        double readGliderSpeed()
        {
                return speed;
        }

        void updateGliderAltitude(double currentGliderAltitudeValue)
        {
                this.altitude = currentGliderAltitudeValue;
        }

        double readGliderAltitude()
        {
                return altitude;
        }

        void updateGliderLatitude(double currentGliderLatitudeValue)
        {
                this.latitude = currentGliderLatitudeValue;
        }

        double readGliderLatitude()
        {
                return latitude;
        }

        void updateGliderLongitude(double currentGliderLongitudeValue)
        {
                this.longitude = currentGliderLongitudeValue;
        }

        double readGliderLongitude()
        {
                return longitude;
        }

        void updateGliderPidOutputPitch(double currentGliderPidOutputPitchValue)
        {
                this.pidOutputPitch = currentGliderPidOutputPitchValue;
        }

        double readGliderPidOutputPitch()
        {
                return pidOutputPitch;
        }

        void updateGliderPidOutputYaw(double currentGliderPidOutputYawValue)
        {
                this.pidOutputYaw = currentGliderPidOutputYawValue;
        }

        double readGliderPidOutputYaw()
        {
                return pidOutputYaw;
        }

        void setGliderName(String gliderName)
        {
                this.gliderName = gliderName;
        }

        String readGliderName()
        {
                return gliderName;
        }

        void updateGliderLoadDropActivation(boolean currentLoadDropActivationValue)
        {
                this.loadDropActivation = currentLoadDropActivationValue;
        }

        boolean readGliderLoadDropActivation()
        {
                return loadDropActivation;
        }
}
