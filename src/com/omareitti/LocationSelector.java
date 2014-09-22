package com.omareitti;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.Filterable;
import android.view.View;
import android.view.View.OnClickListener;
import android.text.TextWatcher;
import android.util.Log;
import android.text.Editable;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.omareitti.datatypes.Coords;
import com.omareitti.datatypes.GeoRec;
import java.util.ArrayList;
import android.widget.ArrayAdapter;

// TODO: autocomplete popup shows only after we type 4 characters
public class LocationSelector extends LinearLayout implements LocationFinder.Listener {
    private AutoCompleteTextView mText;
    private Button mButton;
    private LocationFinder mFinder;
    private Coords mCoords;
    private int mHint;
    private Context mContext;
    private static final String TAG = MainApp.class.getSimpleName();

    public LocationSelector(Context context, AttributeSet attrs) {
	super(context, attrs);
	mContext = context;
    }

    public void setLocationFinder(LocationFinder finder) {
	mFinder = finder;
    }

    public void setInitialHint(int hint) {
	mHint = hint;
	mText.setHint(mHint);
	mText.setAdapter(new CursorAdapter(mContext));
    }

    public Coords getCoords() {
	return mCoords;
    }

    public void setLocation(String location, Coords coords) {
	if (!mText.getText().equals(location)) {
	    mText.setText(location);
	}

	mCoords = coords;
    }

    // TODO: kill these:
    public void setText(String text) {

    }

    public CharSequence getText() {
	return mText.getText();
    }

    public CharSequence getHint() {
	return mText.getHint();
    }

    public void setHint(String hint) {

    }

    // end TODO:

    @Override
    protected void onFinishInflate() {
	super.onFinishInflate();
	((Activity)getContext()).getLayoutInflater().inflate(R.layout.location_selector, this);
	setupViewItems();
    }

    private void setupViewItems() {
	mText = (AutoCompleteTextView)findViewById(R.id.editText);
	mButton = (Button)findViewById(R.id.button);

	mButton.setOnClickListener(new View.OnClickListener() {
		public void onClick(View v) {
		    // TODO:
		    // showGetAddress();
		}
	    });
    }

    public void onCoordinatesChanged() {
	new Thread(new Runnable() {
		public void run() {
		    Coords coords = mFinder.coordinates();
		    Log.i(TAG, "Getting address for coordinates "+coords.toString());
		    ArrayList<GeoRec> recs = ReittiopasAPI.getReverseGeocode(coords.toString());
		    // TODO:
		    if (recs.size() > 0) {
			GeoRec rec = recs.get(0);
			//			Log.e(TAG, "=============== " + rec.name);
			//			lastKnownAddress = rec.name;
			//			addressDiscovered(rec.name);
		    }
		}
	    }).run();
    }

    private void showGetAddress() {
	// TODO: add the first one only if gps is enabled
    	final String[] items = new String[]{
	    getResources().getString(R.string.maAddressMenuLocation)
	    /*
// TODO: implement me
,

	    getResources().getString(R.string.maAddressMenuMap),
	    getResources().getString(R.string.maAddressMenuContacts)
	    */
	};

	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
	builder.setItems(items, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		    //		case 0:
		    mText.setHint(R.string.maEditFromHintLocating);
		    // Try to start our GPS listener.
		    mFinder.add((LocationFinder.Listener)LocationSelector.this);
		    //		    break;

		    //		case 1:
		    //		case 2:
		}
	    });

	builder.show();
    }
}
