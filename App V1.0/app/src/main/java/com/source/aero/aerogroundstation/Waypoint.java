package com.source.aero.aerogroundstation;
import java.io.Serializable;

/**
 * Created by Carl on 2018-02-25.
 */

public class Waypoint implements Serializable {
    private String name;
    private int id;
    private String location;
    private double altitude;
    private double speed;
    private double heading;
    private double wdrop;
    private double hdrop;
    private double gliderDrop;

    private double roll;
    private double pitch;
    private double yaw;

    Waypoint(){
        name = "";
        id = 0;
        location = "";
        altitude = 0;
        speed = 0;
        heading = 0;
    }

    Waypoint(String n, int i, String l, double a, double s, double h, double wh, double hh, double gdh, double r, double p, double y){
        name = n;
        id = i;
        location = l;
        altitude = a;
        speed = s;
        heading = h;
        wdrop = wh;
        hdrop = hh;
        gliderDrop = gdh;
        roll = r;
        pitch  = p;
        yaw = y;
    }

    public String getName(){
        return name;
    }

    public int getID(){
        return id;
    }

    public String getLocation(){
        return location;
    }

    public double getAltitude(){
        return altitude;
    }

    public double getSpeed(){
        return speed;
    }

    public double getHeading(){
        return heading;
    }

    public double getWaterDrop(){
        return wdrop;
    }
    public double getHabitatDrop(){
        return hdrop;
    }

    public double getGliderDrop(){return gliderDrop;}

    public double getRoll(){
        return roll;
    }
    public double getPitch(){
        return pitch;
    }
    public double getYaw(){
        return yaw;
    }


}