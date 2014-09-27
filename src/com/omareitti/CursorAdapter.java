package com.omareitti;

import android.widget.SimpleCursorAdapter;
import android.database.Cursor;
import android.content.Context;
import android.database.MatrixCursor;
import java.util.ArrayList;
import com.omareitti.datatypes.GeoRec;
import android.widget.FilterQueryProvider;

// Brilliant idea from here:
// http://stackoverflow.com/questions/19858843/how-to-dynamically-add-suggestions-to-autocompletetextview-with-preserving-chara

// TODO: add history
public class CursorAdapter extends SimpleCursorAdapter {
    CursorAdapter(Context context) {
	super(context, android.R.layout.simple_dropdown_item_1line,
	      null, new String[] { "name" }, new int[] { android.R.id.text1 }, 0);

	setStringConversionColumn(1);

	setFilterQueryProvider(new FilterQueryProvider() {
		@Override
		    public Cursor runQuery(CharSequence constraint) {
		    // run in the background thread

		    if (constraint == null) {
			return null;
		    }

		    String[] columnNames = { "_id", "name", "coords" };
		    MatrixCursor c = new MatrixCursor(columnNames);

		    final ArrayList<GeoRec> rects =
			ReittiopasAPI.getGeocode(constraint.toString());

		    for (int x = 0; x < rects.size(); x++) {
			c.newRow()
			    .add(x)
			    .add(rects.get(x).name + ", " + rects.get(x).city)
			    .add(rects.get(x).coords.toString());
		    }

		    return c;
		}
	    });
    }
}
