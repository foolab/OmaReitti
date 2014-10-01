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
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.AdapterView;
import android.os.AsyncTask;

public class LocationSelector extends LinearLayout implements LocationFinder.Listener {
    private AutoCompleteTextView mText;
    private Button mButton;
    private LocationFinder mFinder;
    private Coords mCoords;
    private int mHint;
    private Context mContext;

    private Activity mActivity;
    private int mContactsActivityId;
    private int mMapActivityId;
    private CursorAdapter mAdapter;
    private boolean mLocationAware;
    ReverseGeocode mTask;

    private static final String TAG = MainApp.class.getSimpleName();

    public LocationSelector(Context context, AttributeSet attrs) {
	super(context, attrs);
	mContext = context;
	mLocationAware = false;
    }

    public void setActivityIds(Activity activity, int contactsActivityId, int mapActivityId) {
	mActivity = activity;
	mContactsActivityId = contactsActivityId;
	mMapActivityId = mapActivityId;
    }

    public void setLocationFinder(LocationFinder finder) {
	mFinder = finder;
    }

    public void setInitialHint(int hint) {
	mHint = hint;
	mText.setHint(mHint);
    }

    public Coords getCoords() {
	return mCoords;
    }

    public String getName() {
	return mText.getText().toString();
    }

    public void setLocation(String location, Coords coords) {
	if (!mText.getText().equals(location)) {
	    mText.setText(location);
	}

	mCoords = coords;
    }

    @Override
    protected void onFinishInflate() {
	super.onFinishInflate();
	((Activity)getContext()).getLayoutInflater().inflate(R.layout.location_selector, this);
	setupViewItems();
    }

    private void setupViewItems() {
	mAdapter = new CursorAdapter(mContext);
	mText = (AutoCompleteTextView)findViewById(R.id.editText);
	mText.setAdapter(mAdapter);
	mText.addTextChangedListener(new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		    mCoords = null;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		    setLocationAware(false);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }
	    });

	mText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
		    Cursor c = mAdapter.getCursor();
		    c.moveToPosition(position);
		    c.getString(2);
		    setLocation(c.getString(1), new Coords(c.getString(2)));
		}

	    });

	mButton = (Button)findViewById(R.id.button);

	mButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
		    showGetAddress();
		}
	    });
    }

    public void setLocationAware(boolean enable) {
	if (mLocationAware == enable) {
	    return;
	}

	mLocationAware = enable;

	if (mLocationAware) {
	    // enable
	    mText.setHint(R.string.maEditFromHintLocating);
	    mFinder.add((LocationFinder.Listener)LocationSelector.this);
	} else {
	    // disable
	    mFinder.remove((LocationFinder.Listener)LocationSelector.this);
	    if (mTask != null) {
		mTask.cancel(true);
		mTask = null;
	    } else {
		// If we have a task then let it take care of resetting the hint.
		mText.setHint(mHint);
	    }
	}
    }

	@Override
    public void onCoordinatesChanged() {
	// Get our coordinates
	Coords coords = mFinder.coordinates();

	// We don't want anymore updates
	setLocationAware(false);

	// Now we take care of ourselves
	mTask = new ReverseGeocode();
	mTask.execute(coords);
    }

    private void showGetAddress() {
	// TODO: use a string array
    	final String[] items = new String[] {
	    getResources().getString(R.string.maAddressMenuLocation),
	    getResources().getString(R.string.maAddressMenuMap),
	    getResources().getString(R.string.maAddressMenuContacts)
	};

	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
	builder.setItems(items, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		    setLocationAware(false);

		    switch (which) {
		    case 0:
			// We disable first above.
			// This toggle makes sure we destroy any pending reverse geocoding tasks
			setLocationAware(true);
			break;

		    case 1:
			Intent mapIntent = new Intent(getContext().getApplicationContext(),
						      LocationPicker.class);
			mActivity.startActivityForResult(mapIntent, mMapActivityId);
			break;
		    case 2:
			Intent intent =
			    new Intent(Intent.ACTION_PICK,
				       android.provider.ContactsContract.Contacts.CONTENT_URI);
			mActivity.startActivityForResult(intent, mContactsActivityId);
			break;
		    }
		}
	    });

	builder.show();
    }

    public void onActivityResult(int id, Intent data) {
	if (id == mContactsActivityId) {
	    // TODO: show a dialog to select the address if we have multiple
	    Uri contactData = data.getData();
	    Cursor c = mActivity.getContentResolver().query(contactData, null, null, null, null);

	    if (c.getCount() <= 0) {
		c.close();
		return;
	    }

	    String ct = null;

	    while (c.moveToNext()) {
		ct = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
	    }

	    c.close();

	    if (ct == null || ct == "") {
		return;
	    }

	    String addrWhere =
		ContactsContract.Data.CONTACT_ID +
		" = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

	    String[] addrWhereParams =
		new String[]{ct,
			     ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
	    c = mActivity.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
						     null,addrWhere, addrWhereParams, null);

	    String addr = null;
	    if (c.moveToFirst()) {
		do {
		    try {
			int addrColumn =
			    c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);

			if (addrColumn == -1)
			    continue;

			addr = c.getString(addrColumn);
			if (addr != null && addr.length() > 0)
			    break;

		    } catch(Exception e) { Log.e(TAG, "PICK_CONTACT", e); };
		} while (c.moveToNext());
	    }

	    c.close();

	    if (addr != null && addr.length() > 0)
		setLocation(addr, null);

	} else if (id == mMapActivityId) {
	    setLocation(data.getStringExtra("mapAddress"),
			new Coords(data.getStringExtra("mapCoords")));
	}
    }


    private class ReverseGeocode extends AsyncTask<Coords, Integer, GeoRec> {
	@Override
	protected GeoRec doInBackground(Coords... tasks) {
	    Coords c = tasks[0];
	    ArrayList<GeoRec> recs = ReittiopasAPI.getReverseGeocode(c.toString());
	    if (recs.size() > 0)
		return recs.get(0);
	    return null;
	}

	@Override
	protected void onPostExecute(GeoRec result) {
	    if (result == null) {
		MainApp.showErrorDialog(mContext, mContext.getString(R.string.error),
					mContext.getString(R.string.maDlgErrorCurrentLocation));
	    } else {
		setLocation(result.name, result.coords);
	    }

	    mText.setHint(mHint);
	}

	@Override
	protected void onCancelled(GeoRec result) {
	    mText.setHint(mHint);

	    super.onCancelled(result);
	}
    }
}
