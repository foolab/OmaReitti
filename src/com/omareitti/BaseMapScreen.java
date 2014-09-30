package com.omareitti;

import java.util.List;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import android.os.Looper;
import android.os.Handler;

public class BaseMapScreen extends Activity {
    private static final String TAG = BaseMapScreen.class.getSimpleName();

    MapView mView;
    private MapController mController;
    private MyLocationNewOverlay mLocation;
    private CompassOverlay mCompass;

    @Override
    protected void onCreate(Bundle icicle) {
	super.onCreate(icicle);

	getActionBar().setDisplayHomeAsUpEnabled(true);

	setContentView(R.layout.mapscreen);

	mView = (MapView) findViewById(R.id.mapview);
	mView.setBuiltInZoomControls(true);
	mView.setMultiTouchControls(true);

	// TODO: a menu with available tile sources
	////	mView.setSatellite(false);
	////	mView.setStreetView(false);

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	int zoomLevel = Integer.parseInt(prefs.getString("prefMapZoomLevel", "15"));

	mController = (MapController)mView.getController();
	mController.setZoom(zoomLevel);

	// Helsinki location from wikipedia
	setCenter(new GeoPoint(60.170833, 24.9375));

	mLocation = new MyLocationNewOverlay(BaseMapScreen.this, mView);
	mLocation.setDrawAccuracyEnabled(true);
	mView.getOverlays().add(mLocation);

	mCompass = new CompassOverlay(BaseMapScreen.this, mView);
	mView.getOverlays().add(mCompass);

	mView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
            onBackPressed();
	    return true;

	default:
	    break;
        }

	return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
    }

    @Override
    protected void onPause() {
	super.onPause();

	mCompass.disableCompass();
	mLocation.disableMyLocation();
    }

    @Override
    protected void onResume() {
	super.onResume();

	mCompass.enableCompass();
	mLocation.enableMyLocation();
    }

    public MapView getMapView() {
	return mView;
    }

    public void goToCurrentLocation() {
	// TODO: feedback if p is null
	GeoPoint p = mLocation.getMyLocation();

	if (p != null)
	    mController.animateTo(p);
    }

    private GeoPoint mCenter = null;
    public void setCenter(GeoPoint center) {
	// TODO: Does not work as it should and all workarounds failed :(
	// See https://github.com/osmdroid/osmdroid/issues/22
	if (mCenter != null) {
	    mCenter = center;
	    return;
	}

	mCenter = center;

	new Handler(Looper.getMainLooper())
	    .post(new Runnable() {
		    public void run() {
			if (mCenter == null)
			    return;

			mView.getController().setCenter(mCenter);
			mCenter = null;
		    }
		}
		);
    }
}
