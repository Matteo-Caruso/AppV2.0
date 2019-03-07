package com.source.aero.aerogroundstation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;



/** Ankur Jai Sood
 * Last Modified: 01/19/2019
 * Description: A class to store and handle data for the UWO AeroDesign Team
 **/

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Constants:
    private String DATABASE_NAME;               //Stores the name of the database

    //For the session table
    private static final String SESSION_TABLE_NAME = "Sessions";
    private static final String SESSION_TABLE_COL_ID = "Session_ID";
    private static final String SESSION_TABLE_COL_FILEPATH = "Session_Filepath";
    /*Sample Table:
                    Sessions       
    Session_ID      Session_Filepath
    */

    private static final String FLIGHTPATH_TABLE_NAME = "Flightpaths";
    private static final String FLIGHTPATH_TABLE_COL_SESSION = "Session_ID";
    private static final String FLIGHTPATH_TABLE_COL_WAYPOINT = "Waypoint_ID";
    private static final String FLIGHTPATH_TABLE_COL_LOCATION = "Location";
    private static final String FLIGHTPATH_TABLE_COL_ALT = "Altitude";
    private static final String FLIGHTPATH_TABLE_COL_SPEED = "Speed";
    private static final String FLIGHTPATH_TABLE_COL_HEAD = "Heading";
    private static final String FLIGHTPATH_TABLE_COL_WATER_DROP = "WATER_DROP_HEIGHT";
    private static final String FLIGHTPATH_TABLE_COL_HABITAT_DROP = "HABITAT_DROP_HEIGHT";
    private static final String FLIGHTPATH_TABLE_COL_DROP_GLIDER = "GLIDER_DROP_HEIGHT";
    private static final String FLIGHTPATH_TABLE_COL_ROLL = "Roll";
    private static final String FLIGHTPATH_TABLE_COL_PITCH = "Pitch";
    private static final String FLIGHTPATH_TABLE_COL_YAW = "Yaw";
    //Added by Joy to specify the type of flight type: Plane(P)/Glider(G)
    private static final String FLIGHTPATH_TABLE_COL_TYPE = "Flight_Type";
    /*Sample Table:
                                                                    Flightpaths
    Session_ID  Waypoint_ID     Location    Altitude    Speed   Heading     DROP_HEIGHT     Roll    Pitch   Yaw     Flight_Type 
    */
    
    private static final String TARGET_TABLE_NAME = "Targets";
    private static final String TARGET_TABLE_COL_ID = "Target_ID";
    private static final String TARGET_TABLE_COL_LOCATION = "Location";
    /*
    Sample Table:
            Targets
    Target_ID       Location
    */

    String oldSession = "";

    SQLiteDatabase db;  //Variable to store the database
    // Set Variables
    private static String DB_PATH = "";

    public DatabaseHelper(Context context, String Name) {
        super(context, Name + ".db", null, 1);
        DATABASE_NAME = Name +".db";
        Log.d("Data", "ON CONSTRUCTOR RUNNNNINNININNIGGG");
        db = getWritableDatabase();

        if(android.os.Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //ON CREATE ONLY RUNS ONCE
        // Check to see if database exits
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        Log.d("Data", "Database onCreate...");
        //Good idea to check if name of db already exists but we know to avoid that problem

        boolean exists = dbFile.exists();
        db.execSQL("PRAGMA foreign_keys=TRUE;");

        // If database does not exist, create a session table
        db.execSQL("CREATE TABLE " + SESSION_TABLE_NAME + " (" + SESSION_TABLE_COL_ID + " TEXT PRIMARY KEY, " +
                    SESSION_TABLE_COL_FILEPATH + " TEXT)");

        //Creating a FlightPath table
        db.execSQL("CREATE TABLE " + FLIGHTPATH_TABLE_NAME + " (" + FLIGHTPATH_TABLE_COL_SESSION + " TEXT, " +
                FLIGHTPATH_TABLE_COL_WAYPOINT + " INTEGER, " +
                FLIGHTPATH_TABLE_COL_LOCATION + " BLOB, " +
                FLIGHTPATH_TABLE_COL_ALT + " REAL, " +
                FLIGHTPATH_TABLE_COL_SPEED + " REAL, " +
                FLIGHTPATH_TABLE_COL_HEAD + " REAL, " +
                FLIGHTPATH_TABLE_COL_WATER_DROP + " REAL, " +
                FLIGHTPATH_TABLE_COL_HABITAT_DROP + " REAL, " +
                FLIGHTPATH_TABLE_COL_DROP_GLIDER + " REAL, " +
                FLIGHTPATH_TABLE_COL_ROLL + " REAL, " +
                FLIGHTPATH_TABLE_COL_PITCH + " REAL, " +
                FLIGHTPATH_TABLE_COL_YAW + " REAL, " +
                FLIGHTPATH_TABLE_COL_TYPE + " BLOB, " +     //Adds the flight type column specifying a nchar data type --> one character value
                "PRIMARY KEY(" + FLIGHTPATH_TABLE_COL_SESSION + ", " + FLIGHTPATH_TABLE_COL_WAYPOINT + "), " +
                "FOREIGN KEY(" + FLIGHTPATH_TABLE_COL_SESSION + ") " + " REFERENCES " + SESSION_TABLE_NAME + "(" + SESSION_TABLE_COL_ID + ") ON DELETE CASCADE);");
        
        //Creating a Target table
        db.execSQL("CREATE TABLE " + TARGET_TABLE_NAME + " (" + TARGET_TABLE_COL_ID + " TEXT PRIMARY KEY, " + TARGET_TABLE_COL_LOCATION + " TEXT)");
        //String command = "CREATE TABLE Targets (Target_ID TEXT PRIMARY KEY, Location TEXT)";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //MADE CHANGES FROM EXITS TO EXISTS
        db.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE_NAME + " );");
        db.execSQL("DROP TABLE IF EXISTS " + FLIGHTPATH_TABLE_NAME + " );");
        db.execSQL("DROP TABLE IF EXISTS " + TARGET_TABLE_NAME + " );");
        onCreate(db);
    }

    // Method to create a new session
    public boolean createSession(Date sessionID) {
        // Database object
        SQLiteDatabase db = this.getWritableDatabase();

        // Get current date and create new session variables
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd-HH:mm:ss_z");
        String sessionName = formatter.format(sessionID);
        String sessionFile = formatter.format(sessionID)+".csv";

        //Insert session into DB
        ContentValues contentValues = new ContentValues();
        contentValues.put(SESSION_TABLE_COL_ID, sessionName);
        contentValues.put(SESSION_TABLE_COL_FILEPATH, sessionFile);

        // Insert data
        long result = db.insert(SESSION_TABLE_NAME, null, contentValues);
        db.close();

        // Check to see if inserted properly
        if (result ==-1)
            return false;
        else
            return true;
    }
    
    // Method to add new waypoints 
    // Added new parameter char flight_type where inputs should be 'G' or 'P' for glider/plane
    public boolean addWaypoint(String sessionID, int ID, String location, float altitude, float speed, float heading, float wdropHeight, float hdropHeight,float gliderDropHeight, float roll, float pitch, float yaw, String flight_type) {
        // Database object
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("drop", String.valueOf(wdropHeight) + " " + String.valueOf(hdropHeight)+ " " + String.valueOf(gliderDropHeight));

        // Get current date and create new session variables
        Log.d("Add Point", sessionID);

        //Insert session into DB
        ContentValues contentValues = new ContentValues();
        contentValues.put(FLIGHTPATH_TABLE_COL_SESSION, sessionID);
        contentValues.put(FLIGHTPATH_TABLE_COL_WAYPOINT, ID);
        contentValues.put(FLIGHTPATH_TABLE_COL_LOCATION, location);
        contentValues.put(FLIGHTPATH_TABLE_COL_ALT, altitude);
        contentValues.put(FLIGHTPATH_TABLE_COL_SPEED, speed);
        contentValues.put(FLIGHTPATH_TABLE_COL_HEAD, heading);
        contentValues.put(FLIGHTPATH_TABLE_COL_WATER_DROP, wdropHeight);
        contentValues.put(FLIGHTPATH_TABLE_COL_HABITAT_DROP, hdropHeight);
        contentValues.put(FLIGHTPATH_TABLE_COL_DROP_GLIDER, gliderDropHeight);

        contentValues.put(FLIGHTPATH_TABLE_COL_ROLL, roll);
        contentValues.put(FLIGHTPATH_TABLE_COL_PITCH, pitch);
        contentValues.put(FLIGHTPATH_TABLE_COL_YAW, yaw);
        
        //Populate the flight type column with a 'G' or a 'P'
        contentValues.put(FLIGHTPATH_TABLE_COL_TYPE, flight_type);

        // Insert data
        long result = db.insert(FLIGHTPATH_TABLE_NAME, null, contentValues);
        db.close();

        // Check to see if inserted properly
        if (result ==-1)
            return false;
        else
            return true;
    }

    // Method to add new targets
    public boolean addTarget(String name, String location) {
        // Database object
        SQLiteDatabase db = this.getWritableDatabase();

        //Insert session into DB
        ContentValues contentValues = new ContentValues();
        contentValues.put(TARGET_TABLE_COL_ID, name);
        contentValues.put(TARGET_TABLE_COL_LOCATION, location);
        Log.d("Data", contentValues.toString());
        // Insert data
        long result = db.insert(TARGET_TABLE_NAME, null, contentValues);
        db.close();

        // Check to see if inserted properly
        if (result ==-1)
            return false;
        else
            return true;
    }

    // Method to return all targets
    public List<Target> getTargets() {
        // Database object
        SQLiteDatabase db = this.getReadableDatabase();
        
        //Stores the data from the Target table
        List<Target> targetList = new ArrayList<Target>();

        // Cursor object
        Cursor cursor = db.rawQuery("SELECT DISTINCT * FROM " + TARGET_TABLE_NAME +";", null);
        // Return data
        Log.d("Get", String.valueOf(cursor.getCount()));
        
        //Scan through all of the rows in the target table
        if(cursor.moveToFirst()){
            do{
                //Add the target table data to the List
                String name = cursor.getString(0);
                String location = cursor.getString(1);

                Target target = new Target(name, location);

                targetList.add(target);

            }while(cursor.moveToNext());
        }

        //RETURN LIST and close the database
        db.close();
        return targetList;
    }
    
    //Method to return the sessions
    public List<String> getFlightSessions() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        //Stores the data from the Sessions table
        List<String> sessions = new ArrayList<>();

        //Setting up a cursor that will scan through the Flight path table 
        Cursor cursor = db.rawQuery("SELECT DISTINCT * FROM " + FLIGHTPATH_TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            //Scan through the table adding data to the List while there is a next row
            do {
                String name = cursor.getString(cursor.getColumnIndex(FLIGHTPATH_TABLE_COL_SESSION));

                if(!(new String(name).equals(oldSession))){

                    Log.d("flight", "equal");
                    sessions.add(name);
                    oldSession = name;
                }
                //Move the cursor to the next row
                cursor.moveToNext();
            } while (cursor.moveToNext());
        }
        //Return the list and consequently the data stored in it
        return sessions;
    }

    // Method to return all waypoints for a flightpath
    // Added a new parameter that specifies the flight_type data we want. Example: glider data or plane data
    public List<Waypoint> getWaypoints(String sessionID, String flight_type) {
        // Database object
        SQLiteDatabase db = this.getReadableDatabase();
        
        // List stores the data from the FlightPath table
        List<Waypoint> waypointList = new ArrayList<Waypoint>();
        Log.d("Getting", sessionID);

        // Cursor object
        Cursor cursor = db.rawQuery("SELECT * FROM " + FLIGHTPATH_TABLE_NAME + " WHERE " + FLIGHTPATH_TABLE_COL_SESSION + " =? AND " + FLIGHTPATH_TABLE_COL_TYPE + " = '" + flight_type +"'", new String[] {sessionID});
        // Added another condition to the query where the cursor only gathers data that matches the specified flight type
        
        // Return data
        Log.d("Get", String.valueOf(cursor.getCount()));

        if(cursor.moveToFirst()){
            //Add all of the data from the current row into the List as a Waypoint object
            do{
                //Note: first column is at index 0
                String name = cursor.getString(0);
                int id = cursor.getInt(1);
                String location = cursor.getString(2);
                double altitude = cursor.getDouble(3);
                double speed = cursor.getDouble(4);
                double heading = cursor.getDouble(5);
                double wdropHeight = cursor.getDouble(6);
                double hdropHeight = cursor.getDouble(7);
                double gliderDropHeight = cursor.getDouble(8);
                double roll = cursor.getDouble(9);
                double pitch = cursor.getDouble(10);
                double yaw = cursor.getDouble(11);
                
                //Not adding the flight type to the Waypoint object
                
                //We could add the flight type to the waypoint object - but we would need to alter the Waypoint class
                Waypoint waypoint = new Waypoint(name, id, location, altitude, speed, heading, wdropHeight, hdropHeight, gliderDropHeight, roll, pitch, yaw);
                
                //Add the Waypoint object to the list
                waypointList.add(waypoint);

            }while(cursor.moveToNext());
        }

        // Return the list 
        db.close();
        return waypointList;
    }

    // Method to turn a session into a .txt
    public boolean session2file(String sessionID, String flight_type) throws IOException {
        // Database object
        SQLiteDatabase db = this.getWritableDatabase();

        // Get all waypoints for sesison
        List<Waypoint> waypoints = getWaypoints(sessionID, flight_type);

        // Handle data
        if (waypoints.size() == 0) {
            return false;
        }
        else {
            // Create a buffer object
            StringBuffer buffer = new StringBuffer();

            // Iterate over cursor and store data
            buffer.append("#Session: " + sessionID + "\n");
            buffer.append(FLIGHTPATH_TABLE_COL_WAYPOINT + ", " + FLIGHTPATH_TABLE_COL_LOCATION + ", " + FLIGHTPATH_TABLE_COL_ALT + ", " + FLIGHTPATH_TABLE_COL_SPEED + ", " + FLIGHTPATH_TABLE_COL_HEAD + "\n");

            for (Iterator<Waypoint> iter = waypoints.iterator(); iter.hasNext(); ) {
                Waypoint w = iter.next();
                buffer.append(w.getName() + ", " + w.getID() + ", " + w.getLocation() + ", " + w.getAltitude() + ", " + w.getSpeed() + ", " +w.getHeading());
            }

            // Get filepath
            Cursor fileLocation = db.rawQuery("SELECT " + SESSION_TABLE_COL_FILEPATH + " FROM " + SESSION_TABLE_NAME + " WHERE " + SESSION_TABLE_COL_ID + " = " + sessionID, null);
            String filePath = fileLocation.getString(0);

            // Get full path
            String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
            String competePath = baseDir + File.separator + filePath;

            // Check if file exists otherwise create
            File f = new File(competePath);
            if (!f.exists()) {
                f.createNewFile();
            }

            // Write to file
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(buffer.toString());
            bw.flush();
            bw.close();

            // Return true on success
            return true;
        }
    }

    //Method that removes all information from the FlightPaths database
    //This method should be called flushFlightPaths??
    public void flushFlightPatbs(){
        SQLiteDatabase db = this.getWritableDatabase();
        //Removing all the information from database db -> table Flightpath
        db.execSQL("DELETE FROM " + FLIGHTPATH_TABLE_NAME);
        db.close();
    }
    
    //Method that removes all information from the Targets database
    public void flushTargets(){
        SQLiteDatabase db = this.getWritableDatabase();
        //Removing all the information from database db -> table Targets
        db.execSQL("DELETE FROM " + TARGET_TABLE_NAME);
        db.close();
    }

    //Method that removes all information from the Sessions database
    public void flushSessions(){
        SQLiteDatabase db = this.getWritableDatabase();
        //Removing all the information from database db -> table Sessions
        db.execSQL("DELETE FROM " + SESSION_TABLE_NAME);
        db.close();
    }
    
    //Method removes all information stored in all of the tables
    public void flushAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + FLIGHTPATH_TABLE_NAME);
        db.execSQL("DELETE FROM " + SESSION_TABLE_NAME);
        db.execSQL("DELETE FROM " + TARGET_TABLE_NAME);
        db.close();
    }
}

