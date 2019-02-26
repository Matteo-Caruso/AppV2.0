package com.source.aero.aerogroundstation;

import java.io.Serializable;

/**
 * Created by Carl on 2018-02-24.
 */

public class Target implements Serializable{
    private String name;
    private String location;

    Target(){
        name = "";
        location = "";
    }

    Target(String n, String l){
        name = n;
        location = l;
    }

    public String getName(){
        return name;
    }
    public void setName(String n){
        name = n;
    }

    public String getLocation(){
        return location;
    }
    public void setLocation(String l){
        location = l;

    }
}

