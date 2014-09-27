package com.omareitti;

import java.util.ArrayList;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.omareitti.datatypes.Coords;

public class LocationFinder implements LocationListener {
    private static final String TAG = MainApp.class.getSimpleName();

    public interface Listener {
	public abstract void onCoordinatesChanged();
    }

    LocationFinder(Context ctx) {
	mCtx = ctx;
	mListeners = new ArrayList<Listener>();
    }

    public Coords coordinates() {
	return new Coords(mLocation.getLongitude(), mLocation.getLatitude());
    }

    public void add(Listener s) {
	if (!mListeners.contains(s)) {
	    mListeners.add(s);
	}

	if (mListeners.size() == 1) {
	    start();
	}
    }

    public void remove(Listener s) {
	mListeners.remove(s);
	if (mListeners.isEmpty()) {
	    stop();
	}
    }

    private void start() {
	if (mManager != null) {
	    return;
	}

	// TODO: error checking
	mManager = (LocationManager)mCtx.getSystemService(Context.LOCATION_SERVICE);
	try {
	    mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 20, this);
	} catch (Exception e) {
	    Log.e(TAG, "Error requesting location updates " + e);
	}

	try {
	    mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 20, this);
	} catch (Exception e) {
	    Log.e(TAG, "Error requesting location updates " + e);
	}

    }

    private void stop() {
	if (mManager == null) {
	    return;
	}

	mManager.removeUpdates(this);
	mManager = null;
    }

    // LocationListener stuff:
    public void onLocationChanged(Location location) {
	// TODO: we don't get a fix upon asking immediately. debug this or see if we need
	// to manually request the last known fix.
	if (mLocation == null) {
	    mLocation = location;
	} else {
	    long delta = location.getTime() - mLocation.getTime();

	    // If the new location is older then we discard it.
	    if (delta <= 0) {
		return;
	    }

	    // If we have an old fix then we will use the new one:
	    if (delta >= 10 * 1000) { // 10 seconds in ms
		mLocation = location;
	    } else if (location.getAccuracy() > mLocation.getAccuracy()) {
		mLocation = location;
	    }
	}

	if (mLocation == null) {
	    return;
	}

	// Invoke our listeners:
	for (int x = 0; x < mListeners.size(); x++) {
	    mListeners.get(x).onCoordinatesChanged();
	}
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}

    public void onProviderEnabled(String provider) {}

    public void onProviderDisabled(String provider) {}

    private Context mCtx;
    private ArrayList<Listener> mListeners;
    private LocationManager mManager;
    private Location mLocation;
}
