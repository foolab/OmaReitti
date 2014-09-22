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
import android.os.Handler;

// TODO: autocomplete popup shows only after we type 4 characters
public class LocationSelector extends LinearLayout implements LocationFinder.Listener {
    private AutoCompleteTextView mText;
    private Button mButton;
    private LocationFinder mFinder;
    private ArrayAdapter<String> mAdapter;
    private Coords mCoords;
    private int mHint;
    private volatile Handler mHandler;
    private static final String TAG = MainApp.class.getSimpleName();

    public LocationSelector(Context context, AttributeSet attrs) {
	super(context, attrs);
	mAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line);
	mHandler = new Handler();
    }

    public void setLocationFinder(LocationFinder finder) {
	mFinder = finder;
    }

    public void setInitialHint(int hint) {
	mHint = hint;
	mText.setHint(mHint);
	mText.setAdapter(mAdapter);
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

	mText.addTextChangedListener(new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		    if (mText.isPerformingCompletion()) {
			return;
		    }

		    String text = mText.getText().toString();

		    if (text == null || text.length() < 3) {
			return;
		    }

		    Log.i(TAG, "Text is now " + mText.getText());

		    // Start thread:
		    // TODO: We spawn a thread per char change which is really bad.
		    new Thread(new Runnable() {
			    public void run() {
				final ArrayList<GeoRec> rects =
				    ReittiopasAPI.getGeocode(mText.getText().toString());

				mHandler.post(new Runnable() {
					public void run() {
					    // TODO: We need a way to purge old suggestions
					    for (int x = 0; x < rects.size(); x++) {
						int index =
						    mAdapter.getPosition(rects.get(x).name);
						if (index == -1) {
						    mAdapter.add(rects.get(x).name);
						    Log.i(TAG, "Adding location suggestion: " +
							  rects.get(x).name);
						}
					    }

					    mAdapter.notifyDataSetChanged();
					}
				    });

			    }
			}).start();

		    // Text has been changed by user so we reset the hint
		    mText.setHint(mHint);
		    // No need to try to find our location.
		    mFinder.remove((LocationFinder.Listener)LocationSelector.this);
		    // Reset
		    mCoords = null;
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		public void afterTextChanged(Editable s) { }
	    });

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
