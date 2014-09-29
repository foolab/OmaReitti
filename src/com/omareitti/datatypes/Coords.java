package com.omareitti.datatypes;

import android.util.Log;

// x is longitude. y is latitude

public class Coords {
    private static final String TAG = Coords.class.getSimpleName();

    public double x;
    public double y;

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
}