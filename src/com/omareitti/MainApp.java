package com.omareitti;

import java.util.ArrayList;
import com.omareitti.History.HistoryItem;
import com.omareitti.History.RouteHistoryItem;
import com.omareitti.datatypes.GeoRec;
import com.omareitti.datatypes.Coords;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ScrollView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup;
import android.os.AsyncTask;
import android.view.MenuInflater;

public class MainApp extends Activity {
    // TODO: settings
    private static final String TAG = MainApp.class.getSimpleName();

    private static boolean isMoreOptionsUnchanged = true;
    ArrayList<HistoryItem> history;
    ArrayList<RouteHistoryItem> routes;
    ListView myPlaces, myRoutes;

    History.HistoryAdapter historyAdapter;
    History.RoutesAdapter routesAdapter;

    SharedPreferences prefs;

    private String mTimeType;
    private static Button mSearchButton;
    private LocationSelector mFrom;
    private LocationSelector mTo;
    private LocationFinder mLocation;
    private DateTimeSelector mDateTime;
    private static Button mMoreOptionsButton;
    private String mOptimize;
    private String mTransportTypes;

    private Geocode mGeocode = null;

    private static final int ACTIVITY_RESULT_CONTACTS_FROM = 1;
    private static final int ACTIVITY_RESULT_CONTACTS_TO = 2;
    private static final int ACTIVITY_RESULT_MAP_FROM = 3;
    private static final int ACTIVITY_RESULT_MAP_TO = 4;

    private void updateSettings() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (isMoreOptionsUnchanged) {
	    mOptimize = prefs.getString("prefRouteSearchOptionsOptimize", "default");
	    mTransportTypes = prefs.getString("prefRouteSearchOptionsTT", "all");
        }

	mTimeType = "departure";

        ReittiopasAPI.walkingSpeed = (int)(Double.parseDouble(prefs.getString("prefWalkingSpeed", "1"))*60);
    }

    @Override
    public void onBackPressed() {
	if (mGeocode != null) {
	    mGeocode.cancel(true);
	    mGeocode = null;
	}

	super.onBackPressed();
    }

    @Override
    protected void onStart() {
	super.onStart();
    }

    @Override
    protected void onStop() {
	super.onStop();
    }

    @Override
    protected void onRestart() {
	super.onRestart();
    }

    @Override
    protected void onResume() {
	super.onResume();

	updateSettings();
    }

    @Override
    protected void onPause() {
	super.onPause();

	mFrom.setLocationAware(false);
	mTo.setLocationAware(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainapp);

	mLocation = new LocationFinder((Context)MainApp.this);

        mFrom = (LocationSelector)findViewById(R.id.fromLocation);
	mFrom.setInitialHint(R.string.maEditFromHint);
	mFrom.setLocationFinder(mLocation);
	mFrom.setActivityIds(MainApp.this,
			     ACTIVITY_RESULT_CONTACTS_FROM, ACTIVITY_RESULT_MAP_FROM);

	mTo = (LocationSelector)findViewById(R.id.toLocation);
	mTo.setInitialHint(R.string.maEditToHint);
	mTo.setLocationFinder(mLocation);
	mTo.setActivityIds(MainApp.this, ACTIVITY_RESULT_CONTACTS_TO, ACTIVITY_RESULT_MAP_TO);

	mDateTime = (DateTimeSelector)findViewById(R.id.dateTime);

	RadioGroup gp = (RadioGroup)findViewById(R.id.departureArrival);
	gp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
		    switch (checkedId) {
		    case R.id.radioArrival:
			mTimeType = "arrival";
			break;

		    case R.id.radioDeparture:
		    default:
			mTimeType = "departure";
			break;
		    }
		}
	    });

        mSearchButton = (Button)findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(searchRouteListener);

        isMoreOptionsUnchanged = true;
        updateSettings();

        mMoreOptionsButton = (Button)findViewById(R.id.MainAppMoreOptions);
        mMoreOptionsButton.setOnClickListener(moreOptionsListener);

	ListView l1 = (ListView) findViewById(R.id.MainAppGeoSelectorListView);

        history = History.getHistory(this);
        routes = History.getRoutes(this);

        Log.i(TAG, "HISTORY SIZE!!!!!!!!!!!!1: "+history.size()+" "+routes.size());

        TabHost tabs = (TabHost)findViewById(R.id.TabHost01);
        tabs.setup();

        if (history.size() > 0 || routes.size() > 0) {
	    TabHost.TabSpec spec1 = tabs.newTabSpec("tag1");

	    spec1.setContent(R.id.myPlacesList);
	    spec1.setIndicator(getString(R.string.maTabPlaces));
	    tabs.addTab(spec1);

	    TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
	    spec2.setContent(R.id.myRoutesList);
	    spec2.setIndicator(getString(R.string.maTabRoutes));

	    tabs.addTab(spec2);
        } else
	    tabs.setVisibility(View.GONE);

	myPlaces = (ListView)findViewById(R.id.myPlacesList);
	myRoutes = (ListView)findViewById(R.id.myRoutesList);

	// These prevent the ScrollView from hiding the tabhost
        myPlaces.setFocusable(false);
	myRoutes.setFocusable(false);

        historyAdapter = new History.HistoryAdapter(this);
        routesAdapter = new History.RoutesAdapter(this);
        myPlaces.setAdapter(historyAdapter);
        myRoutes.setAdapter(routesAdapter);

        Utils.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myPlacesList));
        Utils.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myRoutesList));

        myPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

		    final HistoryItem h = history.get(arg2);

		    final String[] items = new String[]{
			getString(R.string.maTabPlacesMenuFrom),
			getString(R.string.maTabPlacesMenuTo),
			getString(R.string.maTabPlacesMenuRename),
			getString(R.string.maTabPlacesMenuDelete),
			getString(R.string.maTabPlacesMenuHome)
		    };

		    AlertDialog.Builder builder = new AlertDialog.Builder(MainApp.this);
		    builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {

				switch(which) {
				case 0:
				    mFrom.setLocation(h.address, h.coords);
				    break;
				case 1:
				    mTo.setLocation(h.address, h.coords);
				    break;
				case 2:
				    showEditName(h);
				    break;
				case 3:
				    History.remove(MainApp.this, "history_"+h.address);
				    History.init(MainApp.this);
				    history = History.getHistory(MainApp.this);
				    historyAdapter.notifyDataSetChanged();
				    break;
				case 4:
				    String name = h.name;
				    if (name.equals(""))
					name = h.address;
				    Utils.addHomeScreenShortcut(MainApp.this, name,
								null, h.address, null, h.coords);
				    break;
				}
			    }
			});
		    builder.show();
		}
	    });

        myRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		    final History.RouteHistoryItem r = routes.get(arg2);

		    final String[] items = new String[]{
			getString(R.string.maTabRoutesMenuSet),
			getString(R.string.maTabRoutesMenuSetBackwards),
			getString(R.string.maTabRoutesMenuDelete),
			getString(R.string.maTabRoutesMenuHome)
		    };

		    AlertDialog.Builder builder = new AlertDialog.Builder(MainApp.this);
		    builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {

				switch(which) {
				case 0:
				    mFrom.setLocation(r.start, r.coords);
				    mTo.setLocation(r.end, r.coords2);
				    break;
				case 1:
				    mFrom.setLocation(r.end, r.coords2);
				    mTo.setLocation(r.start, r.coords);
				    break;
				case 2:
				    History.remove(MainApp.this, "route_"+r.start+"_"+r.end);
				    History.init(MainApp.this);
				    routes = History.getRoutes(MainApp.this);
				    routesAdapter.notifyDataSetChanged();
				    break;
				case 3:
				    String n1 = r.start.substring(0, 5);
				    if (r.start.length() > 0)
					n1 +=".";
				    String n2 = r.end;

				    Utils.addHomeScreenShortcut(MainApp.this,
								n1+"-"+n2, r.start, r.end,
								r.coords, r.coords2);
				    break;
				}
			    }
			});
		    builder.show();
		}
	    });

        Bundle b = getIntent().getExtras();
        if (b != null) {
	    String toAddress = b.getString("toAddress");
	    String fromAddress = b.getString("fromAddress");
	    String toCoordsInt = b.getString("toCoords");
	    String fromCoordsInt = b.getString("fromCoords");

	    if (toAddress != null)
		mTo.setLocation(toAddress, new Coords(toCoordsInt));

	    if (fromAddress != null)
		mFrom.setLocation(fromAddress, new Coords(fromCoordsInt));

	    // TODO: show an error if we don't have all the data we need

	    //	    if (toAddress != null && fromAddress != null && fromCoordsInt != null && toCoordsInt != null) {
		updateSettings();
		launchNextActivity();
		//	    }
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
	super.onActivityResult(reqCode, resultCode, data);

	if (resultCode != Activity.RESULT_OK) {
	    return;
	}

	switch (reqCode) {
	case ACTIVITY_RESULT_CONTACTS_FROM:
	case ACTIVITY_RESULT_MAP_FROM:
	    mFrom.onActivityResult(reqCode, data);
	    break;

	case ACTIVITY_RESULT_CONTACTS_TO:
	case ACTIVITY_RESULT_MAP_TO:
	    mTo.onActivityResult(reqCode, data);
	    break;
	}
    }

    private void showEditName(final HistoryItem h) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.maTabPlacesMenuRenameDlgTitle));
    	LayoutInflater inflater = LayoutInflater.from(this);
    	final View convertView = inflater.inflate(R.layout.editname, null);
    	builder.setView(convertView);

    	AlertDialog alertDialog = builder.create();

    	EditText et = (EditText)convertView.findViewById(R.id.editName);

    	String str = h.address;
    	if (!h.name.equals(""))
	    str = h.name;
    	et.setText(str);

    	alertDialog.setButton(getString(R.string.save), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
		    EditText et = (EditText)convertView.findViewById(R.id.editName);
		    History.saveHistory(MainApp.this, h.address, et.getText().toString(),
					h.coords);
		    History.init(MainApp.this);
		    history = History.getHistory(MainApp.this);
		    dialog.dismiss();
		    historyAdapter.notifyDataSetChanged();
		} });

   	alertDialog.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
		    dialog.dismiss();
		} });

    	alertDialog.show();
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
    }

    private class GeocodeTask {
	LocationSelector mSelector;
	String mKey;
	ArrayList<GeoRec> mResult;
	int mResource;
	int mDialogTitleResource;
    };

    private class Geocode extends AsyncTask<GeocodeTask, Integer, ArrayList<GeocodeTask>> {
	@Override
	protected ArrayList<GeocodeTask> doInBackground(GeocodeTask... tasks) {
	    ArrayList<GeocodeTask> result = new ArrayList<GeocodeTask>();
	    for (GeocodeTask task : tasks) {

		ArrayList<GeoRec> res
		    = ReittiopasAPI.getGeocode(task.mKey);

		GeocodeTask r = new GeocodeTask();
		r.mKey = task.mKey;
		r.mSelector = task.mSelector;
		r.mResult = res;
		r.mResource = task.mResource;
		r.mDialogTitleResource = task.mDialogTitleResource;

		result.add(r);
	    }

	    return result;
	}

	@Override
	protected void onPostExecute(ArrayList<GeocodeTask> results) {
	    mDlg.dismiss();

	    boolean launchActivity = true;

	    for (GeocodeTask task : results) {
		if (task.mResult == null) {
		    showErrorDialog(MainApp.this, getString(R.string.networkErrorTitle),
				    getString(R.string.networkErrorText));
		    launchActivity = false;
		} else if (task.mResult.size() == 0) {
		    showErrorDialog(MainApp.this,
				    getString(R.string.error), getString(task.mResource));
		    launchActivity = false;
		} else if (task.mResult.size() == 1) {
		    task.mSelector.setLocation(task.mResult.get(0).name,
					       task.mResult.get(0).coords);
		} else {
		    // Multiple results
		    launchActivity = false;
		    showLocationSelectDialog(task);
		}
	    }

	    if (launchActivity == true) {
		launchNextActivity();
	    }

	    mGeocode = null;
	}

	public void setProgressDialog(ProgressDialog dlg) {
	    mDlg = dlg;
	}

	private ProgressDialog mDlg;
    }

    private OnClickListener searchRouteListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (mFrom.getName() == null || mFrom.getName().length() == 0) {
		    showErrorDialog(MainApp.this, getString(R.string.error),
				    getString(R.string.maDlgErrorEmptyFrom));
		    return;
		}

		if (mTo.getName() == null || mTo.getName().length() == 0) {
		    showErrorDialog(MainApp.this, getString(R.string.error),
				    getString(R.string.maDlgErrorEmptyTo));
		    return;
		}

		if (mFrom.getName().length() < 3) {
		    showErrorDialog(MainApp.this, getString(R.string.error),
				    getString(R.string.maDlgErrorEmptyFromTooShort));
		    return;
		}

		if (mTo.getName().length() < 3) {
		    showErrorDialog(MainApp.this, getString(R.string.error),
				    getString(R.string.maDlgErrorEmptyToTooShort));
		    return;
		}

		mFrom.clearFocus();
		mTo.clearFocus();

		Coords fromCoords = mFrom.getCoords();
		Coords toCoords = mTo.getCoords();

		if (fromCoords == null || toCoords == null) {
		    ProgressDialog dlg =
			ProgressDialog.show(MainApp.this, "",
					    getString(R.string.maDlgSearch), true);
		    dlg.show();

		    mGeocode = new Geocode();
		    mGeocode.setProgressDialog(dlg);

		    ArrayList<GeocodeTask> tasks = new ArrayList<GeocodeTask>();

		    if (fromCoords == null) {
			GeocodeTask task = new GeocodeTask();
			task.mSelector = mFrom;
			task.mKey = mFrom.getName();
			task.mResource = R.string.maDlgErrorNoFrom;
			task.mDialogTitleResource = R.string.maDlgSelectFrom;
			tasks.add(task);
		    }

		    if (toCoords == null) {
			GeocodeTask task = new GeocodeTask();
			task.mSelector = mTo;
			task.mKey = mTo.getName();
			task.mResource = R.string.maDlgErrorNoTo;
			task.mDialogTitleResource = R.string.maDlgSelectTo;
			tasks.add(task);
		    }

		    mGeocode.execute(tasks.toArray(new GeocodeTask[] {}));

		} else {
		    // We can already search
		    launchNextActivity();
		}
	    }
	};

    private static class EfficientAdapter extends BaseAdapter {
    	private LayoutInflater mInflater;
    	private ArrayList<GeoRec> recs;

    	public EfficientAdapter(Context context, ArrayList<GeoRec> recs) {
	    mInflater = LayoutInflater.from(context);
	    this.recs = recs;
    	}

    	public int getCount() {
	    return recs.size();
    	}

    	public Object getItem(int position) {
	    return position;
    	}

    	public long getItemId(int position) {
	    return position;
    	}

    	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;
	    if (convertView == null) {
		convertView = mInflater.inflate(R.layout.geoselectoritem, null);
		holder = new ViewHolder();
		holder.text = (TextView) convertView.findViewById(R.id.MainAppGeoSelectorText);

		convertView.setTag(holder);
	    } else {
		holder = (ViewHolder) convertView.getTag();
	    }

	    holder.text.setText(recs.get(position).name+", "+recs.get(position).city);

	    return convertView;
    	}

    	static class ViewHolder {
	    TextView text;
    	}
    }

    private void showLocationSelectDialog(final GeocodeTask result) {
    	Context context = MainApp.this;
    	final Dialog locationSelectDialog = new Dialog(context);
    	locationSelectDialog.setTitle(getString(result.mDialogTitleResource));
	locationSelectDialog.setContentView(R.layout.geoselector);
	ListView l1 =
	    (ListView) locationSelectDialog.findViewById(R.id.MainAppGeoSelectorListView);

	l1.setAdapter(new EfficientAdapter(context, result.mResult));
	l1.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		    String name = result.mResult.get(position).name
			+ ", " + result.mResult.get(position).city;
		    result.mSelector.setLocation(name, result.mResult.get(position).coords);
		    locationSelectDialog.dismiss();
		    launchNextActivity();
		}
	    });
	locationSelectDialog.show();
    }

    private void launchNextActivity() {
	// Only if we successfully retrieved both from and to
	// coordinates, start the new activity

	if (mFrom.getCoords() != null && mTo.getCoords() != null) {
            Intent myIntent = new Intent(MainApp.this, SelectRouteScreen.class);
            myIntent.putExtra("fromCoords", mFrom.getCoords().toString());
            myIntent.putExtra("toCoords", mTo.getCoords().toString());
            myIntent.putExtra("fromName", mFrom.getName());
            myIntent.putExtra("toName", mTo.getName());
            myIntent.putExtra("date", mDateTime.getYear()+mDateTime.getMonth()+mDateTime.getDay());
            myIntent.putExtra("time", mDateTime.getHour() + mDateTime.getMinute());
            myIntent.putExtra("optimize", mOptimize);
            myIntent.putExtra("timetype", mTimeType);
            myIntent.putExtra("transport_types", mTransportTypes);
            startActivity(myIntent);
	}
    }

    private OnClickListener moreOptionsListener = new OnClickListener() {
	    public void onClick(View v) {
		TravelOptionsDialog.TravelOptionsDialogResults listener =
		    new TravelOptionsDialog.TravelOptionsDialogResults() {
			public void onDone(String optimize, String transportTypes) {
			    mOptimize = optimize;
			    mTransportTypes = transportTypes;
			}
		    };

		AlertDialog dlg =
		    TravelOptionsDialog.create(MainApp.this, mTransportTypes, mOptimize, listener);
		dlg.show();
	    }
	};

    static protected void showErrorDialog(Context context, String title, String message) {
	AlertDialog alertDialog = new AlertDialog.Builder(context).create();
	alertDialog.setTitle(title);
	alertDialog.setMessage(message);
	alertDialog.setButton(context.getString(R.string.ok),
			      new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
		    // Nothing
		} });
	alertDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.mainactivity_actions, menu);

    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
	case R.id.action_clear:
	    mFrom.setLocation("", null);
	    mTo.setLocation("", null);
	    return true;

	case R.id.action_swap:
	    String s = mFrom.getName();
	    Coords c = mFrom.getCoords();
	    mFrom.setLocation(mTo.getName(), mTo.getCoords());
	    mTo.setLocation(s, c);
	    return true;

	case R.id.action_settings:
	    startActivity(new Intent(MainApp.this, SettingsScreen.class));
	    return true;

	case R.id.action_about:
	    SpannableString ss = new SpannableString(getString(R.string.about));
	    Linkify.addLinks(ss, Linkify.WEB_URLS);
	    AlertDialog alertDialog = new AlertDialog.Builder(MainApp.this).create();
	    alertDialog.setTitle(getString(R.string.maMenuAboutDlgTitle));
	    alertDialog.setMessage(ss);
	    alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
			// Nothing
	    	    } });
	    alertDialog.show();
	    return true;

	default:
	    break;
        }

	return super.onOptionsItemSelected(item);
    }
}
