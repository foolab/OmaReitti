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

	if (routeString.equals("")) {
	    Log.e(TAG, "no route string");
	    startMainActivity();
	    return;
	}

	try {
	    mRoute = new Route(routeString);
	} catch (Exception e) {
	    Log.e(TAG, "Couldn't get the route from JSONobj "+routeString, e);
	    startMainActivity();
	    return;
	}

	if (currentStep < -1 || mRoute.steps.size() < currentStep) {
	    Log.e(TAG, "Invalid current step " + currentStep);
	    startMainActivity();
	    return;
	}

	mListener = new MarkerClickListener();

	if (currentStep == -1) {
	    for (int x = 0; x < mRoute.steps.size(); x++) {
		Route.RouteStep r = mRoute.steps.get(x);
		addStep(r);
	    }
	} else {
	    Route.RouteStep r = mRoute.steps.get(currentStep);
	    addStep(r);
	}

	getMapView().invalidate();
    }

    private void startMainActivity() {
	startActivity(new Intent(RouteMap.this, MainApp.class));
	finish();
    }

    private void addStep(Route.RouteStep r) {
	ArrayList<Route.PathSegment> paths = r.path;

	Polyline line = new Polyline(this);
	line.setColor(r.getColor());
	line.setWidth(5);

	ArrayList<GeoPoint> list = new ArrayList<GeoPoint>(paths.size());

	for (int x = 0; x < paths.size(); x++) {
	    Coords c = paths.get(x).coords;
	    list.add(c.toGeoPoint());
	}

	line.setPoints(list);
	getMapView().getOverlays().add(line);

	// Now our markers:

	for (int x = 0; x < paths.size(); x++) {
	    Coords c = paths.get(x).coords;
	    Marker m = new Marker(getMapView());

	    m.setPosition(c.toGeoPoint());

	    m.setOnMarkerClickListener(mListener);
	    m.setSubDescription(r.path.get(x).name);

	    getMapView().getOverlays().add(m);
	}
    }

    class MarkerClickListener implements Marker.OnMarkerClickListener {
	public boolean onMarkerClick(Marker marker, MapView mapView) {
	    Toast.makeText(RouteMap.this, marker.getSubDescription(),Toast.LENGTH_LONG).show();
	    return true;
	}
    }
}