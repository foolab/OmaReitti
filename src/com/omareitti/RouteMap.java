package com.omareitti;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import com.omareitti.datatypes.Coords;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.overlays.Marker;
import java.util.ArrayList;
import org.osmdroid.views.MapView;
import android.widget.Toast;
import java.util.ArrayList;
import org.osmdroid.views.overlay.Overlay;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;

public class RouteMap extends BaseMapScreen {
    private static final String TAG = RouteMap.class.getSimpleName();

    private Route mRoute;
    private MarkerClickListener mListener;

    @Override
    protected void onCreate(Bundle icicle) {
	super.onCreate(icicle);

	if (getIntent().getExtras() == null) {
            Log.e(TAG, "intent extras is null, switching to main activity ");
	    startMainActivity();
	    return;
	}

	String routeString = getIntent().getExtras().getString("route");
        int currentStep = getIntent().getExtras().getInt("currentStep");
        String from = getIntent().getExtras().getString("from");
        String to = getIntent().getExtras().getString("to");

	if (routeString.equals("")) {
	    Log.e(TAG, "no route string");
	    startMainActivity();
	    return;
	}

	try {
	    mRoute = new Route(routeString, from, to);
	} catch (Exception e) {
	    Log.e(TAG, "Couldn't get the route from JSONobj "+routeString, e);
	    startMainActivity();
	    return;
	}

	if (currentStep < -1 || mRoute.mSteps.size() < currentStep) {
	    Log.e(TAG, "Invalid current step " + currentStep);
	    startMainActivity();
	    return;
	}

	mListener = new MarkerClickListener();

	ArrayList<Overlay> overlays = new ArrayList<Overlay>();
	ArrayList<Overlay> markers = new ArrayList<Overlay>();

	if (currentStep == -1) {
	    for (int x = 0; x < mRoute.mSteps.size(); x++) {
		Route.RouteStep r = mRoute.mSteps.get(x);
		addStep(r, overlays, markers);
	    }

	    setCenter(mRoute.mSteps.get(0).path.get(0).mCoords.toGeoPoint());
	    // Now our markers:

	    if (mRoute.mSteps.get(0).getTransportName() == R.string.tr_walk) {
		// First segment is walking.
		Marker m = createMarker();
		m.setPosition(mRoute.mSteps.get(0).path.get(0).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setIcon(getResources().getDrawable(R.drawable.marker_departure));
		m.setSubDescription(mRoute.mSteps.get(0).path.get(0).mName);
		markers.add(m);
	    } else {
		// First segment is not walking
		Marker m = (Marker)markers.get(0);
		m.setIcon(getResources().getDrawable(R.drawable.marker_departure));
	    }

	    if (mRoute.mSteps.get(mRoute.mSteps.size() - 1).getTransportName()
		== R.string.tr_walk) {
		// Last segment is walking

		Route.RouteStep r = mRoute.mSteps.get(mRoute.mSteps.size() - 1);

		Marker m = createMarker();
		m.setPosition(r.path.get(r.path.size() - 1).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(r.path.size() - 1).mName);
		m.setIcon(getResources().getDrawable(R.drawable.marker_destination));
		markers.add(m);
	    } else {
		// Last segment is not walking
		Marker m = (Marker)markers.get(markers.size() - 1);
		m.setIcon(getResources().getDrawable(R.drawable.marker_destination));
	    }


	} else {
	    Route.RouteStep r = mRoute.mSteps.get(currentStep);
	    addStep(r, overlays, markers);
	    setCenter(r.path.get(0).mCoords.toGeoPoint());

	    // Now our markers:
	    if (currentStep == 0 && r.getTransportName() == R.string.tr_walk) {
		// First walking segment
		Marker m = createMarker();
		m.setPosition(r.path.get(0).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(0).mName);
		m.setIcon(getResources().getDrawable(R.drawable.marker_departure));
		markers.add(m);

		// Add the end marker
		m = createMarker();
		m.setPosition(r.path.get(r.path.size() - 1).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(r.path.size() - 1).mName);
		markers.add(m);

	    } else if (currentStep == 0) {
		// First non walking segment
		Marker m = (Marker)markers.get(0);
		m.setIcon(getResources().getDrawable(R.drawable.marker_departure));
	    }

	    if (currentStep == mRoute.mSteps.size() - 1
		&& r.getTransportName() == R.string.tr_walk) {
		// Last walking segment

		Marker m = createMarker();
		m.setPosition(r.path.get(r.path.size() - 1).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(r.path.size() - 1).mName);
		m.setIcon(getResources().getDrawable(R.drawable.marker_destination));
		markers.add(m);

		// Add the start marker
		m = createMarker();
		m.setPosition(r.path.get(0).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(0).mName);
		markers.add(m);

	    } else if (currentStep == mRoute.mSteps.size() - 1) {
		// Last non walking segment
		Marker m = (Marker)markers.get(markers.size() - 1);
		m.setIcon(getResources().getDrawable(R.drawable.marker_destination));
	    }

	    if (currentStep != 0 && currentStep != mRoute.mSteps.size() - 1 &&
		r.getTransportName() == R.string.tr_walk) {
		// A walking segment in the middle:

		// Start
		Marker m = createMarker();
		m.setPosition(r.path.get(0).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(0).mName);
		markers.add(m);

		// End
		m = createMarker();
		m.setPosition(r.path.get(r.path.size() - 1).mCoords.toGeoPoint());
		m.setOnMarkerClickListener(mListener);
		m.setSubDescription(r.path.get(r.path.size() - 1).mName);
		markers.add(m);
	    }
	}

	overlays.addAll(markers);
	addOverlays(overlays);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.mapscreen_actions, menu);

    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_show_mylocation:
	    goToCurrentLocation();
	    return true;

	default:
	    break;
        }

	return super.onOptionsItemSelected(item);
    }

    private void startMainActivity() {
	startActivity(new Intent(RouteMap.this, MainApp.class));
	finish();
    }

    private void addStep(Route.RouteStep r,
			 ArrayList<Overlay> overlays, ArrayList<Overlay> markers) {
	ArrayList<Route.PathSegment> paths = r.path;

	Polyline line = new Polyline(this);
	line.setColor(r.getColor());
	line.setWidth(5);

	ArrayList<GeoPoint> list = new ArrayList<GeoPoint>(paths.size());

	for (int x = 0; x < paths.size(); x++) {
	    Coords c = paths.get(x).mCoords;
	    list.add(c.toGeoPoint());
	}

	line.setPoints(list);
	overlays.add(line);

	// Now our markers:
	for (int x = 0; x < paths.size(); x++) {
	    if (r.getTransportName() == R.string.tr_walk) {
		continue;
	    }

	    Coords c = paths.get(x).mCoords;
	    Marker m = createMarker();

	    m.setPosition(c.toGeoPoint());

	    m.setOnMarkerClickListener(mListener);
	    m.setSubDescription(paths.get(x).mName);

	    markers.add(m);
	}
    }

    class MarkerClickListener implements Marker.OnMarkerClickListener {
	public boolean onMarkerClick(Marker marker, MapView mapView) {
	    if (marker.getSubDescription() != null) {
		Toast.makeText(RouteMap.this,
			       marker.getSubDescription(), Toast.LENGTH_LONG).show();
	    }

	    return true;
	}
    }
}
