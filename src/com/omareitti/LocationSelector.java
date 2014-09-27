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

    private static final String TAG = MainApp.class.getSimpleName();

    public LocationSelector(Context context, AttributeSet attrs) {
	super(context, attrs);
	mContext = context;
	mText.setAdapter(new CursorAdapter(mContext));
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

    public void setLocation(String location, Coords coords) {
	if (!mText.getText().equals(location)) {
	    mText.setText(location);
	}

	mCoords = coords;
    }

    // TODO: kill these:
    public void setText(String text) {
	mText.setText(text);
    }

    public String getText() {
	return mText.getText().toString();
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
		    showGetAddress();
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
	// TODO: use a string array
    	final String[] items = new String[] {
	    getResources().getString(R.string.maAddressMenuLocation),
	    getResources().getString(R.string.maAddressMenuMap),
	    getResources().getString(R.string.maAddressMenuContacts)
	};

	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
	builder.setItems(items, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		    switch (which) {
		    case 0:
			// TODO:
		    //		    mText.setHint(R.string.maEditFromHintLocating);
		    // Try to start our GPS listener.
		    //		    mFinder.add((LocationFinder.Listener)LocationSelector.this);
			break;

		    case 1:
			{
			    Intent intent = new Intent(getContext().getApplicationContext(),
						       MapScreen.class);
			    intent.putExtra("pickPoint", "yes");
			    mActivity.startActivityForResult(intent, mMapActivityId);
			}
			break;
		    case 2:
			{
			    Intent intent =
				new Intent(Intent.ACTION_PICK,
					   android.provider.ContactsContract.Contacts.CONTENT_URI);
			    mActivity.startActivityForResult(intent, mContactsActivityId);
			}
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
		setText(addr);

	} else if (id == mMapActivityId) {
	    setLocation(data.getStringExtra("mapAddress"),
			new Coords(data.getStringExtra("mapCoords")));
	}
    }
}
