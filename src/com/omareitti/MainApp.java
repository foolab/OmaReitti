package com.omareitti;

import java.util.ArrayList;
import com.omareitti.IBackgroundServiceAPI;
import com.omareitti.IBackgroundServiceListener;
import com.omareitti.R;
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
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

public class MainApp extends Activity {
    public static Button searchButton;

    private static boolean isMoreOptionsUnchanged = true;

    ArrayList<GeoRec> geoFrom;
    ArrayList<GeoRec> geoTo;

    private Coords fromCoords;
    private Coords toCoords;
    private String fromName = "";
    private String toName = "";

    public volatile Handler handler;
    private ListView l1;
    public Dialog locationFromSelectDialog;
    public Dialog locationToSelectDialog;

    public ProgressDialog processDialog;

    SharedPreferences prefs;

    private static final int SWAP_MENU_ID = 0;
    private static final int SETTINGS_MENU_ID = 1;
    private static final int ABOUT_MENU_ID = 2;


    private String mTimeType;
    private LocationSelector mFrom;
    private LocationSelector mTo;
    private LocationFinder mLocation;
    private DateTimeSelector mDateTime;
    private static Button mMoreOptionsButton;
    private String mOptimize;
    private String mTransportTypes;

    private void updateSettings() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (isMoreOptionsUnchanged) {
	    mOptimize = prefs.getString("prefRouteSearchOptionsOptimize", "default");
	    mTransportTypes = prefs.getString("prefRouteSearchOptionsTT", "all");
        }

        ReittiopasAPI.walkingSpeed = (int)(Double.parseDouble(prefs.getString("prefWalkingSpeed", "1"))*60);
    }

    @Override
	protected void onStart() {
	super.onStart();

	handler = new Handler();
    }

    @Override
	protected void onStop() {
	super.onStop();

	// TODO: stop gps
    }

    @Override
	protected void onRestart() {
	super.onRestart();
    }

    @Override
	protected void onResume() {
	super.onResume();

	if (api != null) {
	    try {
		api.requestLastKnownAddress(1);
	    } catch (Exception e) { };
	}

	updateSettings();
    }

    private static final String TAG = MainApp.class.getSimpleName();

    ArrayList<HistoryItem> history;
    ArrayList<RouteHistoryItem> routes;
    private int lastSelectedHistory = -1;
    private int lastSelectedRoute = -1;
    ListView myPlaces, myRoutes;

    History.HistoryAdapter historyAdapter;
    History.RoutesAdapter routesAdapter;

    private ArrayAdapter<String> autoCompleteAdapter;

    /** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainapp);

	mLocation = new LocationFinder((Context)MainApp.this);

        mFrom = (LocationSelector)findViewById(R.id.fromLocation);
	mFrom.setInitialHint(R.string.maEditFromHint);
	mFrom.setLocationFinder(mLocation);

	mTo = (LocationSelector)findViewById(R.id.toLocation);
	mTo.setInitialHint(R.string.maEditToHint);
	mTo.setLocationFinder(mLocation);

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

        searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(searchRouteListener);

        isMoreOptionsUnchanged = true;
        updateSettings();

        mMoreOptionsButton = (Button)findViewById(R.id.MainAppMoreOptions);
        mMoreOptionsButton.setOnClickListener(moreOptionsListener);

	l1 = (ListView) findViewById(R.id.MainAppGeoSelectorListView);

	Log.i(TAG, "trying to bind service "+BackgroundService.class.getName());
	Intent servIntent = new Intent(BackgroundService.class.getName());//this, BackgroundService.class);
        startService(servIntent);
        Log.i(TAG, "starting service "+servIntent.toString());
        bindService(servIntent, servceConection, 0);

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

	// TODO: take into account history when completing
        autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, History.getHistoryAsArray());
        
	//        mFrom.setAdapter(autoCompleteAdapter);
	//        mTo.setAdapter(autoCompleteAdapter);
        Utils.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myPlacesList));
        Utils.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myRoutesList));
        
        myPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

		    lastSelectedHistory = arg2;

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
				HistoryItem h = history.get(lastSelectedHistory);
				switch(which) {
				case 0:
				    mFrom.setLocation(h.address, h.coords);
				    break;
				case 1:
				    mTo.setLocation(h.address, h.coords);
				    break;
				case 2:
				    showEditName();
				    break;
				case 3:
				    History.remove(MainApp.this, "history_"+history.get(lastSelectedHistory).address);
				    History.init(MainApp.this);
				    history = History.getHistory(MainApp.this);
				    historyAdapter.notifyDataSetChanged();
				    break;
				case 4:
				    String name = h.name;
				    if (name.equals("")) name = h.address;
				    Utils.addHomeScreenShortcut(MainApp.this, name, null, h.address, null, h.coords);
				    break;
				}
			    }
			});
		    builder.show();
		}
	    });

        myRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		    lastSelectedRoute = arg2;
		    final String[] items = new String[]{
			getString(R.string.maTabRoutesMenuSet),
			getString(R.string.maTabRoutesMenuSetBackwards),
			getString(R.string.maTabRoutesMenuDelete),
			getString(R.string.maTabRoutesMenuHome)
		    };

		    AlertDialog.Builder builder = new AlertDialog.Builder(MainApp.this);
		    builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
				History.RouteHistoryItem r = routes.get(lastSelectedRoute);
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
				    String n1 = r.start.substring(0, 5); if (r.start.length() > 0) n1 +=".";
				    String n2 = r.end;//.substring(0, 5); if (r.end.length() > 0) n2 +=".";
				    Utils.addHomeScreenShortcut(MainApp.this, n1+"-"+n2, r.start, r.end, r.coords, r.coords2);
				    break;
				}
								
			    }
			});
		    builder.show();				
		    /*History.RouteHistoryItem r = routes.get(arg2);
		      fromEditText.setText(r.start);
		      toEditText.setText(r.end);	*/				
		}
	    });

        Bundle b = getIntent().getExtras();
        if (b != null) {
	    String toAddress = b.getString("toAddress");
	    String fromAddress = b.getString("fromAddress");
	    String toCoordsInt = b.getString("toCoords");
	    String fromCoordsInt = b.getString("fromCoords");

	    if (toAddress != null) {
		mTo.setText(toAddress);
		toName = toAddress;
	    }
	    if (fromAddress != null) { 
		mFrom.setText(fromAddress);
		fromName = fromAddress;
	    }
	    // TODO:
	    //	    if (fromCoordsInt != null) 
	    //		fromCoords = fromCoordsInt;
	    //	    if (toCoordsInt != null) 
	    //		toCoords = toCoordsInt;
        	 
	    if (toAddress != null && fromAddress != null && fromCoordsInt != null && toCoordsInt != null) {
		//optimize = "default";
		updateSettings();
		launchNextActivity();
	    }
        }
    }
    
    private LocationSelector putAddressHere = null;
    private static final int PICK_CONTACT = 123;
    private static final int PICK_MAP = 124;
    private static int currentAction = 0;
    
    private void showGetAddress() {
    	final String[] items = new String[]{
	    getString(R.string.maAddressMenuLocation),
	    getString(R.string.maAddressMenuMap),
	    getString(R.string.maAddressMenuContacts)
	};

	AlertDialog.Builder builder = new AlertDialog.Builder(MainApp.this);
	builder.setItems(items, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		    currentAction = which;
		    switch(which) {
		    case 0:
			if (api != null && putAddressHere != null) {
			    putAddressHere.setText("");
			    putAddressHere.setHint(getString(R.string.maEditFromHintLocating));
			    try {
				api.requestLastKnownAddress(1);
			    } catch (Exception e) { };
			}
			break;
		    case 1:
			Intent myIntent = new Intent(MainApp.this, MapScreen.class);
			myIntent.putExtra("pickPoint", "yes");
			startActivityForResult(myIntent, PICK_MAP);
			break;
		    case 2:
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
			break;
		    }
		}
	    });
	builder.show();    	
    }
    
    @Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
	super.onActivityResult(reqCode, resultCode, data);

	switch (reqCode) {
	case PICK_CONTACT:
	    if (resultCode == Activity.RESULT_OK) {
		Uri contactData = data.getData();
		String id = null;
		Cursor c = managedQuery(contactData, null, null, null, null);
		if (c.getCount() > 0) {
		    while (c.moveToNext()) {
			id = c.getString(
					 c.getColumnIndex(ContactsContract.Contacts._ID));
			Log.i(TAG, "id:"+id);
		    }
		}
		if (id == null || id.equals("")) return;
		String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] addrWhereParams = new String[]{id, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
					
		ContentResolver cr = getContentResolver();
		c = cr.query(ContactsContract.Data.CONTENT_URI, 
			     null, addrWhere, addrWhereParams, null); 
					
		//Cursor c = managedQuery(contactData, null, null, null, null);
		if (c.moveToFirst()) {
		    String addr = "";
						
		    do {
			try {
			    int addrColumn = c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
			    String[] cc = c.getColumnNames();
			    if (addrColumn == -1) continue;
			    String tmp = c.getString(addrColumn);
			    if (tmp != null && tmp.length() > 0) addr = tmp; break;
			} catch(Exception e) { Log.e(TAG, "PICK_CONTACT", e); };
		    } while (c.moveToNext());	
	
		    putAddressHere.setText(addr);
		    c.close();
		}
		//putAddressHere = null;
	    }
	    break;
	case PICK_MAP:
	    if (resultCode == Activity.RESULT_OK) {
		String mapAddress = data.getStringExtra("mapAddress");
		String mapCoords = data.getStringExtra("mapCoords");
		putAddressHere.setText(mapAddress);
		// TODO:
		//		if (putAddressHere == mFrom) {
		//		    fromCoords = mapCoords;
		//		} else if (putAddressHere == mTo) {
		//		    toCoords = mapCoords;
		//		}
		Log.i(TAG, "PICK_MAP: "+mapAddress+" "+mapCoords);
		//putAddressHere = null;
	    }
	    break;
	}
	currentAction = 0;
	putAddressHere = null;
    }

    private void showEditName() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.maTabPlacesMenuRenameDlgTitle));
    	LayoutInflater inflater = LayoutInflater.from(this);
    	final View convertView = inflater.inflate(R.layout.editname, null);
    	builder.setView(convertView);

    	AlertDialog alertDialog = builder.create();

    	EditText et = (EditText)convertView.findViewById(R.id.editName);
    	HistoryItem h = history.get(lastSelectedHistory);

    	String str = h.address;
    	if (!h.name.equals("")) str = h.name;
    	et.setText(str);
    	
    	alertDialog.setButton(getString(R.string.save), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
		    HistoryItem h = history.get(lastSelectedHistory);
	    		
		    EditText et = (EditText)convertView.findViewById(R.id.editName);
		    History.saveHistory(MainApp.this, h.address, et.getText().toString(), h.coords);
		    History.init(MainApp.this);
		    history = History.getHistory(MainApp.this);
	    		
		    Log.i(TAG, h.address+" "+et.getText().toString());
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
	// TODO Auto-generated method stub
	super.onDestroy();
	try {
	    if (api != null) api.removeListener(serviceListener);
	} catch(Exception e) {
	    Log.e(TAG, "ERROR!!", e);
	}
		
	unbindService(servceConection);
	Log.i(TAG, "unbind ");
    }

    private OnClickListener searchRouteListener = new OnClickListener() {
	    public void onClick(View v) {
		if (mFrom.getText() == null || mFrom.getText().equals("")) {
		    // TODO: not working?!
		    showErrorDialog("", getString(R.string.maDlgErrorEmptyFrom));
		    return;
		}

		if (mTo.getText() == null || mTo.getText().equals("")) {
		    // TODO: not working?!
		    showErrorDialog("", getString(R.string.maDlgErrorEmptyTo));
		    return;
		}

        	processDialog =
		    ProgressDialog.show(MainApp.this, "",
					getString(R.string.maDlgSearch), true);

		// TODO: what if we have coordinates?

		new Thread(new Runnable() {
			public void run() {
			    ArrayList<GeoRec> res;
			    res = ReittiopasAPI.getGeocode(mFrom.getText().toString());

			    try {
				fromName = res.get(0).name;
				// TODO:
				//				fromCoords = res.get(0).coords;
			    } catch (Exception e) {
				Log.e(TAG, "error: ", e);
			    }

			    res = ReittiopasAPI.getGeocode(mTo.getText().toString());
			    try {
				toName = res.get(0).name;
				//TODO:
				//				toCoords = res.get(0).coords;
			    } catch (Exception e) {
				Log.e(TAG, "error: ", e);
			    }

			    handler.post(new Runnable() {
				    public void run() {
					processDialog.dismiss();
					// TODO: alerts
					launchNextActivity();
				    }
				});

			}
		    }).run();

		// //		String from = 
		// ArrayList<GeoRec> res = ReittiopasAPI.getGeocode(mFrom.getText().toString());
		// if (res.size() == 0) {
		//     // TODO: tell ths user something
		//     return;
		// }



		// String from = res.get(0).name;

		// res = ReittiopasAPI.getGeocode(mTo.getText().toString());
		// if (res.size() == 0) {
		//     // TODO: tell ths user something
		//     return;
		// }

		// String to = res.get(0).name;


        	// if (!fromCoords.equals("") && !toCoords.equals("")) {
		//     launchNextActivity();
		//     return;
		// }         	
		// fromCoords = "";
		// toCoords = "";
		// fromName = "";
		// toName = "";   
        	

        	
    		// new Thread(new Runnable() {
    		// 	public void run() {
		// 	    try {
		// 		ReittiopasAPI api = new ReittiopasAPI();
    					
		// 		if (mFrom.getText().toString().equals("") && !mFrom.getHint().equals(getString(R.string.maEditFromHint)) && !mFrom.getHint().equals("")) {
		// 		    geoFrom = api.getGeocode(mFrom.getHint().toString());
		// 		} else {    					
		// 		    geoFrom = api.getGeocode(mFrom.getText().toString()); // This makes an HTTP request don't call it many times
		// 		}
		// 		geoTo = api.getGeocode(toEditText.getText().toString());
		// 	    } catch ( Exception e ) {
		// 		Log.e("ERROR", "No network", e);
		// 	    }

		// 	    handler.post(new Runnable() {
		// 		    public void run() {	
		// 			processDialog.dismiss(); 
    						
		// 			if (geoFrom == null || geoTo == null) {
		// 			    showErrorDialog(getString(R.string.networkErrorTitle), getString(R.string.networkErrorText));
		// 			} else {
		// 			    if (geoFrom.size() == 0) showErrorDialog(getString(R.string.error), getString(R.string.maDlgErrorNoFrom));
		// 			    if (geoTo.size() == 0) showErrorDialog(getString(R.string.error), getString(R.string.maDlgErrorNoTo));
            					            					
		// 			    if (geoTo.size() > 1) {
		// 				showLocationToSelectDialog();
		// 			    }
            					
		// 			    if (geoFrom.size() > 1) {
		// 				showLocationFromSelectDialog();
		// 			    }            					
          					
		// 			    if (geoFrom.size() == 1) {
		// 				fromCoords = geoFrom.get(0).coords;
		// 				fromName = geoFrom.get(0).name+", "+geoFrom.get(0).city;
		// 			    }
		// 			    if (geoTo.size() == 1) {
		// 				toCoords = geoTo.get(0).coords;
		// 				toName = geoTo.get(0).name+", "+geoTo.get(0).city;
		// 			    }

		// 			    launchNextActivity();
		// 			}
		// 		    }
    		// 		});					
    		// 	}
		//     }).start();

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
    
    private OnItemClickListener locationFromClickListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
		fromName = geoFrom.get(position).name+", "+geoFrom.get(position).city;
		mFrom.setText(fromName);
		// NB! THIS SHOULD BE AFTER fromEditText.setText(fromName);
		fromCoords = geoFrom.get(position).coords;
		locationFromSelectDialog.dismiss();
		launchNextActivity();
	    }
	};
	
    private OnItemClickListener locationToClickListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
		//Log.i(TAG, "locationToClickListener toCoords:"+toCoords);
		toName = geoTo.get(position).name+", "+geoTo.get(position).city;
		mTo.setText(toName);
		// NB! THIS SHOULD BE AFTER toEditText.setText(toName);
		toCoords = geoTo.get(position).coords;
		locationToSelectDialog.dismiss();
		launchNextActivity();
	    }
	};

    private void showLocationFromSelectDialog() {
    	Context context = MainApp.this;
    	locationFromSelectDialog = new Dialog(context);
    	locationFromSelectDialog.setTitle(getString(R.string.maDlgSelectFrom));
	locationFromSelectDialog.setContentView(R.layout.geoselector);
	l1 = (ListView) locationFromSelectDialog.findViewById(R.id.MainAppGeoSelectorListView);		
	l1.setAdapter(new EfficientAdapter(context, geoFrom));
	l1.setOnItemClickListener(locationFromClickListener);
	locationFromSelectDialog.show();
    }
    
    private void showLocationToSelectDialog() {
	Context context = MainApp.this;
    	locationToSelectDialog = new Dialog(context);
    	locationToSelectDialog.setTitle(getString(R.string.maDlgSelectTo));
	locationToSelectDialog.setContentView(R.layout.geoselector);
	l1 = (ListView) locationToSelectDialog.findViewById(R.id.MainAppGeoSelectorListView);		
	l1.setAdapter(new EfficientAdapter(context, geoTo));
	l1.setOnItemClickListener(locationToClickListener);
	locationToSelectDialog.show();
    }
    
    private void launchNextActivity() {
	// only if we successfully retrieved both from and to
	// coordinates, start the new activity
    	Log.i(TAG, "launchNextActivity fromCoords:"+fromCoords+" toCoords:"+toCoords);
	if (mFrom.getCoords() != null && mTo.getCoords() != null) {
            Intent myIntent = new Intent(MainApp.this, SelectRouteScreen.class);
            myIntent.putExtra("fromCoords", mFrom.getCoords().toString());
            myIntent.putExtra("toCoords", mTo.getCoords().toString());
            myIntent.putExtra("fromName", mFrom.getText());
            myIntent.putExtra("toName", mTo.getText());
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

    private void showErrorDialog(String title, String message) {
	AlertDialog alertDialog = new AlertDialog.Builder(MainApp.this).create();
	alertDialog.setTitle(title);
	alertDialog.setMessage(message);
	alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
		    //SelectRouteScreen.this.finish();
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
    	
    	Drawable drSwap  = getResources().getDrawable(android.R.drawable.ic_menu_rotate);	
    	Drawable drSettings = getResources().getDrawable(android.R.drawable.ic_menu_manage);	
    	Drawable drAbout = getResources().getDrawable(android.R.drawable.ic_menu_info_details);	
    	
    	MenuItem tmp = menu.add(0, SWAP_MENU_ID, 0, getString(R.string.maMenuSwap));
    	tmp.setIcon(drSwap);
    	tmp = menu.add(0, SETTINGS_MENU_ID, 1, getString(R.string.maMenuSettings));
    	tmp.setIcon(drSettings);
    	tmp = menu.add(0, ABOUT_MENU_ID, 2, getString(R.string.maMenuAbout));
    	tmp.setIcon(drAbout);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	case SWAP_MENU_ID:
	    String f = "";
	    String t = "";
	    // TODO: crash if both locations are empty
	    if (!mFrom.getHint().equals(getString(R.string.maEditFromHint)) && !mFrom.getHint().equals("")) f = mFrom.getHint().toString();
	    //Log.i(TAG, "sWAP: "+fromEditText.getText().toString().equals("")+"  "+(!fromEditText.getHint().equals("From") && !fromEditText.getHint().equals(""))+" "+f);
	    if (!mFrom.getText().toString().equals("")) f = mFrom.getText().toString();
	    if (!mTo.getText().toString().equals("")) t = mTo.getText().toString();
	    mFrom.setText(t);
	    mTo.setText(f);
	    // TODO:
	    //	    String tmp = fromCoords;
	    //	    fromCoords = toCoords;
	    //	    toCoords = tmp;
	    break;
	case SETTINGS_MENU_ID:
	    Intent settingsActivity = new Intent(MainApp.this, SettingsScreen.class);
	    startActivity(settingsActivity);	 
	    break;
	case ABOUT_MENU_ID:
	    final SpannableString s = new SpannableString(getString(R.string.about));
	    Linkify.addLinks(s, Linkify.WEB_URLS);
	        	  
	    AlertDialog alertDialog = new AlertDialog.Builder(MainApp.this).create();
	    alertDialog.setTitle(getString(R.string.maMenuAboutDlgTitle));
	    	    
	    alertDialog.setMessage(s);
	    alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
			//
	    	    } });
	    alertDialog.show();	
	    break;
	default:
	    return super.onOptionsItemSelected(item);	        	
        }
        
        return true;
    }

    private String lastLocDisc = "";
    private Boolean launchedFromParamsAlready = false;
    /** 
     * Service interaction stuff
     */
    private IBackgroundServiceListener serviceListener = new IBackgroundServiceListener.Stub() {
		
	    public void locationDiscovered(double lat, double lon)
		throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(TAG, "locationDiscovered: "+lat+" "+lon);
		lastLocDisc = lon+","+lat;

		// TODO:
		//		if (fromCoords.equals(""))
		//		    fromCoords = lon+","+lat;			
	    }
		
	    public void handleUpdate(String s) throws RemoteException {
		//Log.i(TAG, "handleUpdate: "+s);
			
	    }
		
	    public void handleGPSUpdate(double lat, double lon, float angle) throws RemoteException {
		// TODO Auto-generated method stub
		Log.i(TAG, "handleGPSUpdate: "+lat+" "+lon);
		// TODO:
		//		if (fromCoords.equals(""))
		//  fromCoords = lon+","+lat;
	    }

	    public void addressDiscovered(String address) throws RemoteException {
		Log.i(TAG, "addressDiscovered: "+address);
		//fromEditText = (EditText) findViewById(R.id.editText1);
		if (putAddressHere == null) {
		    if (address.equals("")) {
			//			mFrom.setHint(getString(R.string.maEditFromHint));
		    } else {
			//			mFrom.setHint(address);
			fromName = address;
		    }
		} else if (currentAction == 0) putAddressHere.setText(address);

		if (launchedFromParamsAlready) return;
	        Bundle b = getIntent().getExtras();
	        if (b != null) {
		    Log.i(TAG, "Bundle: "+b);
		    String toAddress = b.getString("toAddress");
		    String fromAddress = b.getString("fromAddress");

		    String toCoordsInt = b.getString("toCoords");
		    if (toAddress != null && fromAddress == null && !lastLocDisc.equals("")) {
			toName = toAddress;
			// TODO:
			//			toCoords = toCoordsInt;
			fromName = address;
			//			fromCoords = lastLocDisc;
			updateSettings();
			launchNextActivity();
			launchedFromParamsAlready = true;
		    }
	        }
	    }
	};

    private IBackgroundServiceAPI api = null;

    private ServiceConnection servceConection = new ServiceConnection() {
	    public void onServiceDisconnected(ComponentName name) {
		Log.i(TAG, "Service disconnected!");
		api = null;
	    }

	    public void onServiceConnected(ComponentName name, IBinder service) {
		api = IBackgroundServiceAPI.Stub.asInterface(service);

		//		mFrom.setHint(getString(R.string.maEditFromHintLocating));

		Log.i(TAG, "Service connected! "+api.toString());
		try {
		    api.addListener(serviceListener);
		    int res = api.requestLastKnownAddress(1);
		    api.cancelRoute(0);
		    //if (res == 1) { }
		    Log.i(TAG, "requestLastKnownAddress: "+res);
		    //api.setRoute(new TestObject("{asd:\"Hello!\" }"));
		} catch(Exception e) {
		    Log.e(TAG, "ERROR!!", e);
		}
	    }
	};
}
