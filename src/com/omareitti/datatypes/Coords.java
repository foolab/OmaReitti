package com.omareitti.datatypes;

import android.util.Log;
import org.osmdroid.util.GeoPoint;

// x is longitude. y is latitude

public class Coords {
    private static final String TAG = Coords.class.getSimpleName();

    private double x;
    private double y;
    private GeoPoint mPt = null;

    public Coords(String coord) {
	String p[] = coord.split(",");
	x = Double.parseDouble(p[0]);
	y = Double.parseDouble(p[1]);

	Log.i(TAG, "Created new with longitude = " + x + " and latitude = " + y);
    }

    public Coords(Double x, Double y) {
	this.y = y;
	this.x = x;

	Log.i(TAG, "Created new with longitude = " + x + " and latitude = " + y);
    }

    public String toString() {
	return ""+x+","+y;
    }

    public int xToInt() {
	return (int) (x*1E6);
    }

    public int yToInt() {
	return (int) (y*1E6);
    }

    public GeoPoint toGeoPoint() {
	if (mPt == null) {
	    // Coords.x is longitude but GeoPoint takes latitude first
	    mPt = new GeoPoint(y, x);
	}

	return mPt;
    }
}