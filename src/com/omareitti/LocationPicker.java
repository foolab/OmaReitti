package com.omareitti;

import android.os.Bundle;
import android.widget.Toast;
import org.osmdroid.views.overlay.Overlay;
import android.graphics.Canvas;
import org.osmdroid.views.MapView;
import android.content.Context;
import android.view.MotionEvent;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import com.omareitti.datatypes.Coords;
import android.content.Intent;
import java.util.ArrayList;
import com.omareitti.datatypes.GeoRec;
import org.osmdroid.util.GeoPoint;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;

public class LocationPicker extends BaseMapScreen {

    @Override
    protected void onCreate(Bundle icicle) {
	super.onCreate(icicle);

	Toast.makeText(this,
		       getString(R.string.msToastAddressSelect), Toast.LENGTH_SHORT).show();

	getMapView().getOverlays().add(new DoubleTapOverlay(LocationPicker.this));

	getMapView().invalidate();
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

    private class DoubleTapOverlay extends Overlay {
	DoubleTapOverlay(Context context) {
	    super(context);
	}

	@Override
        protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {

	}

	@Override
        public boolean onDoubleTap(final MotionEvent e, final MapView mapView) {
	    GeoPoint p =
		(GeoPoint)mapView.getProjection().fromPixels((int) e.getX(),
							     (int) e.getY());

	    if (p == null)
		return false;

	    ProgressDialog dlg =
		ProgressDialog.show(LocationPicker.this, "",
				    getString(R.string.maDlgSearch), true);
	    dlg.show();

	    new Geocode(dlg, p).execute(p);

	    return true;
        }
    }

    private class Geocode extends AsyncTask<GeoPoint, Integer, String> {
	public Geocode(ProgressDialog dlg, GeoPoint p) {
	    super();

	    mDlg = dlg;
	    mPoint = p;
	}

	@Override
	protected String doInBackground(GeoPoint... points) {
	    GeoPoint p = points[0];

	    ArrayList<GeoRec> recs =
		ReittiopasAPI.getReverseGeocode(new Coords(p.getLongitudeE6() / 1E6,
							   p.getLatitudeE6() / 1E6).toString());
	    if (recs == null || recs.size() == 0)
		return null;

	    return recs.get(0).name;
	}

	@Override
	protected void onPostExecute(String result) {
	    mDlg.dismiss();

	    if (result == null || result.length() == 0) {
		MainApp.showErrorDialog(LocationPicker.this,
					getString(R.string.error),
					getString(R.string.msFailedAddress));
	    } else {
		Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("mapAddress", result);
		intent.putExtra("mapCoords",
				new Coords(mPoint.getLongitudeE6() / 1E6,
					   mPoint.getLatitudeE6() / 1E6).toString());
		setResult(RESULT_OK, intent);
		finish();
	    }
	}

	private ProgressDialog mDlg;
	private GeoPoint mPoint;
    }
}
