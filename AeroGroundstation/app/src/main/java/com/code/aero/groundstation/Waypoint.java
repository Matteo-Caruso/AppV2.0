package com.code.aero.groundstation;

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
    private double drop;

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

    Waypoint(String n, int i, String l, double a, double s, double h, double dh, double r, double p, double y){
        name = n;
        id = i;
        location = l;
        altitude = a;
        speed = s;
        heading = h;
        drop = dh;
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

    public double getDrop(){
        return drop;
    }

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