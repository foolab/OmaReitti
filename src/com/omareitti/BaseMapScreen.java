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
import org.osmdroid.views.overlay.Overlay;
import android.os.Looper;
import android.os.Handler;
import java.util.List;
import org.osmdroid.bonuspack.overlays.Marker;
import android.widget.Toast;

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

    public void goToCurrentLocation() {
	GeoPoint p = mLocation.getMyLocation();

	if (p != null)
	    mController.animateTo(p);
	else
	    Toast.makeText(BaseMapScreen.this,
			   getString(R.string.maDlgErrorCurrentLocation),
			   Toast.LENGTH_SHORT).show();
    }

    public void addOverlays(List<Overlay> overlays) {
	mView.getOverlays().addAll(0, overlays);
	mView.invalidate();
    }

    public Marker createMarker() {
	return new Marker(mView);
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
