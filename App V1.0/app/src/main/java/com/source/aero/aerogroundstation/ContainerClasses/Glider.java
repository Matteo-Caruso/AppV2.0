package com.source.aero.aerogroundstation.ContainerClasses;


/* List of future additions:
 *   1. how are the member variables initialized, with a constructor or individual initialization?
 *   2. need for both height and altitude?
 *   3. should member variables be initialized to 0?
 *
 */

public class Glider implements GeneralAircraft {

        // important data of glider
        private double rssi = 0.0;
        private double blueToothStrength = 0.0;
        private double yaw = 0.0;
        private double pitch = 0.0;
        private double roll = 0.0;
        private double height = 0.0;
        private int speed = 0;
        private int altitude = 0;
        private double latitude = 0.0;
        private double longitude = 0.0;
        private double pidOutputPitch = 0.0;
        private double pidOutputYaw = 0.0;
        private double pidOutputRoll = 0.0;
        private double targetLongitude = 0.0;
        private double targetLatitude = 0.0;



        // let the glider know, the load should be dropped (drop request)
        private boolean loadDropActivation = false;

        //getter and setter functions for variables above

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

        public void updatePlaneSpeed(int currentPlaneSpeedValue)
        {
                this.speed = currentPlaneSpeedValue;
        }

        public int readPlaneSpeed()
        {
                return this.speed;
        }

        public void updatePlaneAltitude(int currentPlaneAltitudeValue)
        {
                this.altitude = currentPlaneAltitudeValue;
        }

        public int readPlaneAltitude()
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

        double readGliderPidOutputRoll()
        {
                return pidOutputRoll;
        }

        void updateGliderPidOutputRoll(double currentGliderPidOutputRollValue)
        {
                this.pidOutputRoll = currentGliderPidOutputRollValue;
        }

        void updateGlidertargetLatitude(double currentTargetLatitude)
        {
                this.targetLatitude = currentTargetLatitude;
        }

        double readGlidertargetLatitude()
        {
                return this.targetLatitude;
        }

        void updateGlidertargetLongitude(double currentTargetLongitude)
        {
                this.targetLongitude = currentTargetLongitude;
        }

        double readGlidertargetLongitude()
        {
                return this.targetLongitude;
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
