package com.omareitti;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.omareitti.IBackgroundServiceAPI;
import com.omareitti.IBackgroundServiceListener;
import com.omareitti.R;
import com.omareitti.History.HistoryItem;
import com.omareitti.History.RouteHistoryItem;
import com.omareitti.datatypes.GeoRec;
import com.omareitti.datatypes.Coords;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.DatePicker;
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
import android.widget.TimePicker;
import android.widget.ToggleButton;
import android.widget.ScrollView;
import android.widget.AdapterView.OnItemClickListener;

public class MainApp extends Activity {
    public static AutoCompleteTextView toEditText;
    public static Button searchButton;
    public static EditText timeEdit;
    public static EditText dateEdit;
    public static Button moreOptionsButton;

    private static final int TIME_DIALOG_ID = 0;
    private static final int DATE_DIALOG_ID = 1;

    private static String hour = "00";
    private static String minute = "00";

    private static String year = "1900";
    private static String month = "01";
    private static String day = "01";

    private static boolean isDateTimeUnchanged = true;
    private static boolean isMoreOptionsUnchanged = true;
    private static boolean isTimeTypeUnchanged = true;

    ArrayList<GeoRec> geoFrom;
    ArrayList<GeoRec> geoTo;

    private Coords fromCoords;
    private Coords toCoords;
    private String fromName = "";
    private String toName = "";
    private String optimize;
    private String transport_types;
    private String timetype;

    public volatile Handler handler;
    private ListView l1;
    public Dialog locationFromSelectDialog;
    public Dialog locationToSelectDialog;
    public Dialog moreOptionsDialog;
    public ProgressDialog processDialog;

    private ToggleButton tbBus;
    private ToggleButton tbTram;
    private ToggleButton tbMetro;
    private ToggleButton tbTrain;
    private ToggleButton tbWalk;
    private Spinner spinnerOptions;

    private static ImageButton imageButtonDepArr;

    SharedPreferences prefs;

    private static final int SWAP_MENU_ID = 0;
    private static final int SETTINGS_MENU_ID = 1;
    private static final int ABOUT_MENU_ID = 2;

    static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    static DateFormat timeFormat = new SimpleDateFormat("HH:mm");


    private static LocationSelector mFrom;
    private static LocationSelector mTo;
    private LocationFinder mLocation;

    private static String twodigits(int i) {
	return (i > 10 ? "" + i : "0"+i);
    }
	
    private void setCurrentDateTime() {
	Calendar c = Calendar.getInstance();
	Date dt = c.getTime();
	hour =		twodigits( c.get(Calendar.HOUR_OF_DAY) );
	minute =	twodigits( c.get(Calendar.MINUTE) );
	day =		twodigits( c.get(Calendar.DAY_OF_MONTH) );
	month =		twodigits( (c.get(Calendar.MONTH) + 1) );
	year =		"" + c.get(Calendar.YEAR);
	timeEdit.setText(timeFormat.format(dt));
	dateEdit.setText(dateFormat.format(dt));
	// TODO:
	// hack
	mFrom.clearFocus();
    }

    private void updateSettings(boolean showDialog) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // if run for the first time, ask for coords permissions
        String a = prefs.getString("prefAllowCoords", "");        
        if (a.equals("") && showDialog) {
	    final SharedPreferences.Editor editor = prefs.edit();
            
	    AlertDialog alertDialog = new AlertDialog.Builder(MainApp.this).create();
    	    alertDialog.setTitle(getString(R.string.maDlgAllowCoordsTitle));    	    
    	    alertDialog.setMessage(getString(R.string.maDlgAllowCoordsText));
    	    alertDialog.setButton(getString(R.string.maDlgAllowCoordsAgree), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
    	    		editor.putString("prefAllowCoords", "Yes");
    	    		editor.commit();
		    } });
    	    alertDialog.setButton2(getString(R.string.maDlgAllowCoordsDisagree), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
    	    		editor.putString("prefAllowCoords", "No");
    	    		editor.commit();
		    } });
    	    alertDialog.show();    	    
        }
        
        if (isMoreOptionsUnchanged) {
	    optimize = prefs.getString("prefRouteSearchOptionsOptimize", "default");
	    transport_types = prefs.getString("prefRouteSearchOptionsTT", "all");
        }
        if (isTimeTypeUnchanged) {
	    timetype = prefs.getString("prefTimeType", "Departure").toLowerCase();
	    if (timetype.equals("arrival")) {
		imageButtonDepArr.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_entrance));
	    } else {
		imageButtonDepArr.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_exit));
	    }
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
				
	//if (isDateTimeUnchanged) setCurrentDateTime();		
	//updateSettings();
    }

    @Override
	protected void onResume() {
	super.onResume();
		
	if (api != null) { 
	    try {
		api.requestLastKnownAddress(1);
	    } catch (Exception e) { };
	}

	if (isDateTimeUnchanged)
	    setCurrentDateTime();
	updateSettings(false);
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

	// TODO:
	ScrollView view = (ScrollView)findViewById(R.id.scroller);
	view.scrollTo(0, 0);


	mLocation = new LocationFinder((Context)MainApp.this);

        mFrom = (LocationSelector)findViewById(R.id.fromLocation);
	mFrom.setInitialHint(R.string.maEditFromHint);
	mFrom.setLocationFinder(mLocation);

	mTo = (LocationSelector)findViewById(R.id.toLocation);
	mTo.setInitialHint(R.string.maEditToHint);
	mTo.setLocationFinder(mLocation);

        searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(searchRouteListener);

        imageButtonDepArr = (ImageButton)findViewById(R.id.imageButtonDepArr);
        imageButtonDepArr.setOnClickListener(depArrDirectionListener);
 
        isTimeTypeUnchanged = true;
        isMoreOptionsUnchanged = true;
        isDateTimeUnchanged = true;
        updateSettings(true);
        
	/*      
		Drawable dd = getResources().getDrawable(android.R.drawable.ic_menu_search); 
		dd.setBounds( 0, 0, 60, 60 );
		searchButton.setCompoundDrawables(dd, null, null, null);
	*/
        
        timeEdit = (EditText)findViewById(R.id.editTime);
        timeEdit.setOnClickListener(timeEditListener);
        dateEdit = (EditText)findViewById(R.id.editDate);
        dateEdit.setOnClickListener(dateEditListener);
        
        moreOptionsButton = (Button)findViewById(R.id.MainAppMoreOptions);
        moreOptionsButton.setOnClickListener(moreOptionsListener);

        setCurrentDateTime();
	/* WTF IS THIS SHIT??!?!
	   final Calendar c = Calendar.getInstance();
	   String h = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
	   hour = (h.length()==1) ? "0"+h : h;
	   String min = Integer.toString(c.get(Calendar.MINUTE));
	   minute = (min.length()==1) ? "0"+min : min;
	   timeEdit.setText(hour+":"+minute);
        
	   year = Integer.toString(c.get(Calendar.YEAR));
	   // January is month 0, hence +1
	   String m = Integer.toString(c.get(Calendar.MONTH)+1);
	   month = (m.length()==1) ? "0"+m : m;
	   String d = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
	   day = (d.length()==1) ? "0"+d : d;
	   dateEdit.setText(day+"."+month+"."+year);
        */

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
	    spec1.setIndicator(getString(R.string.maTabPlaces), getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
	        
	    tabs.addTab(spec1);
	
	    TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
	    spec2.setContent(R.id.myRoutesList);
	    spec2.setIndicator(getString(R.string.maTabRoutes), getResources().getDrawable(android.R.drawable.ic_menu_myplaces));
	
	    tabs.addTab(spec2);
        } else tabs.setVisibility(View.GONE);
        //tabs.getTabWidget().getChildAt(0).getLayoutParams().height = 35; 
        
        /*History.saveHistory(this, "Matinraitti 5", "");
	  History.saveHistory(this, "Kamppi", "");
	  History.saveRoute(this, "Matinraitti 5", "Kamppi");        

	  History.saveHistory(this, "Pienen Villasaaren tie 1", "");
	  History.saveHistory(this, "Nupurintie 56", "");
	  History.saveRoute(this, "Pienen Villasaaren tie 1", "Nupurintie 56");  */      

	myPlaces = (ListView)findViewById(R.id.myPlacesList);
	myRoutes = (ListView)findViewById(R.id.myRoutesList);

	// These prevent the ScrollView from hiding the tabhost
        myPlaces.setFocusable(false);
	myRoutes.setFocusable(false);

        historyAdapter = new History.HistoryAdapter(this);
        routesAdapter = new History.RoutesAdapter(this);
        myPlaces.setAdapter(historyAdapter);
        myRoutes.setAdapter(routesAdapter);
        
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
	    Log.i(TAG, "Bundle: "+b);
	    String toAddress = b.getString("toAddress");
	    String fromAddress = b.getString("fromAddress");
        	 
	    String toCoordsInt = b.getString("toCoords");
	    String fromCoordsInt = b.getString("fromCoords");
        	 
	    if (toAddress != null) {
		toEditText.setText(toAddress);
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
		//TODO: test 
		setCurrentDateTime();
		//optimize = "default";
		//timetype = "departure";
		updateSettings(false);                
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
		toEditText.setText(toName);
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
            myIntent.putExtra("date", year+month+day);
            myIntent.putExtra("time", hour+minute);
            myIntent.putExtra("optimize", optimize);
            myIntent.putExtra("timetype", timetype);
            myIntent.putExtra("transport_types", transport_types);
            startActivity(myIntent);
	}
    }
    
    private OnClickListener depArrDirectionListener = new OnClickListener() {
	    public void onClick(View v) {
        	isTimeTypeUnchanged = false;
        	if (timetype.equals("departure")) {
		    timetype = "arrival";
		    imageButtonDepArr.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_entrance));
        	} else {
		    timetype = "departure";
		    imageButtonDepArr.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu_exit));	
        	}
	    }
	};
    
    private OnClickListener moreOptionsListener = new OnClickListener() {
	    public void onClick(View v) {
    		Context context = MainApp.this;
        	/*moreOptionsDialog = new Dialog(context);
		  moreOptionsDialog.setTitle("Search options");
		  View inflatedView = View.inflate(context, R.layout.moreoptionsdialog, null);
		  moreOptionsDialog.setContentView(inflatedView);   */     

    		View inflatedView = View.inflate(context, R.layout.moreoptionsdialog, null);
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(getString(R.string.moTitle)).setView(inflatedView);   
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    setMoreOptions();
			}
		    });
		moreOptionsDialog = builder.create();	
        	
        	tbBus = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonBus);
        	Drawable d = getResources().getDrawable(R.drawable.bus);
        	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        	tbBus.setCompoundDrawables(null,d,null,null); 
        	tbBus.setCompoundDrawablePadding(8);
        	tbBus.setChecked(transport_types.contains("bus|uline|service") || transport_types.equals("all"));
        	tbBus.setOnClickListener(toggleListener);
        	
        	tbTram = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonTram);
        	d = getResources().getDrawable(R.drawable.tram);
        	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        	tbTram.setCompoundDrawables(null,d,null,null); 
        	tbTram.setCompoundDrawablePadding(8);        	
        	tbTram.setChecked(transport_types.contains("tram") || transport_types.equals("all"));
        	tbTram.setOnClickListener(toggleListener);
        	
        	tbMetro = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonMetro);
        	d = getResources().getDrawable(R.drawable.metro);
        	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        	tbMetro.setCompoundDrawables(null,d,null,null); 
        	tbMetro.setCompoundDrawablePadding(8);
        	tbMetro.setChecked(transport_types.contains("metro") || transport_types.equals("all"));
        	tbMetro.setOnClickListener(toggleListener);
        	
        	tbTrain = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonTrain);
        	d = getResources().getDrawable(R.drawable.train);
        	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        	tbTrain.setCompoundDrawables(null,d,null,null); 
        	tbTrain.setCompoundDrawablePadding(8);
        	tbTrain.setChecked(transport_types.contains("train") || transport_types.equals("all"));
        	tbTrain.setOnClickListener(toggleListener);
        	
        	tbWalk = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonWalk);
        	d = getResources().getDrawable(R.drawable.man);
        	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        	tbWalk.setCompoundDrawables(null,d,null,null); 
        	tbWalk.setCompoundDrawablePadding(8);
        	tbWalk.setChecked(transport_types.contains("walk"));
        	tbWalk.setOnClickListener(toggleListener);
        	
		spinnerOptions = (Spinner) inflatedView.findViewById(R.id.moreOptionsSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(inflatedView.getContext(), R.array.moOptimize, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerOptions.setAdapter(adapter);
		if (optimize.equals("default")) spinnerOptions.setSelection(0);
		if (optimize.equals("fastest")) spinnerOptions.setSelection(1);
		if (optimize.equals("least_transfers")) spinnerOptions.setSelection(2);
		if (optimize.equals("least_walking")) spinnerOptions.setSelection(3);
            
		/*optionsButtonOK = (Button) inflatedView.findViewById(R.id.moreOptionsOK);
		  optionsButtonOK.setOnClickListener(optionsOK);*/
            
        	moreOptionsDialog.show();
	    }
	};
    
    private void setMoreOptions() {
    	transport_types = "";
    	if (tbBus.isChecked()) transport_types = (transport_types.equals("")) ? "bus|uline|service" : transport_types+"|bus|uline|service";
    	if (tbTram.isChecked()) transport_types = (transport_types.equals("")) ? "tram" : transport_types+"|tram";
    	if (tbMetro.isChecked()) transport_types = (transport_types.equals("")) ? "metro" : transport_types+"|metro";
    	if (tbTrain.isChecked()) transport_types = (transport_types.equals("")) ? "train" : transport_types+"|train";
    	if (tbWalk.isChecked()) transport_types = "walk";

    	if (transport_types.equals("bus|uline|service|tram|metro|train")) transport_types = "all";
    	
    	optimize = "";
    	switch (spinnerOptions.getSelectedItemPosition()) {
	case 0: 
	    optimize = "default";
	    break;
	case 1:
	    optimize = "fastest";
	    break;
	case 2:
	    optimize = "least_transfers";
	    break;
	case 3:
	    optimize = "least_walking";
	    break;
	default:
	    break;
    	}
    	
    	isMoreOptionsUnchanged = false;
    	moreOptionsDialog.dismiss();    	
    }
    
    /*
      private OnClickListener optionsOK = new OnClickListener() {
      public void onClick(View v) {
      setMoreOptions();
      }
      };*/
    
    private OnClickListener toggleListener = new OnClickListener() {
	    public void onClick(View v) {
        	if (v.getId() == tbWalk.getId()) {
		    tbBus.setChecked(false);
		    tbTram.setChecked(false);
		    tbMetro.setChecked(false);
		    tbTrain.setChecked(false);        		
        	} else {
		    if (tbWalk.isChecked()) tbWalk.setChecked(false);
        	}
	    }
	};
    
    @Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
	case TIME_DIALOG_ID:
	    return new TimePickerDialog(this, 
					mTimeSetListener, 
					Integer.parseInt(hour), Integer.parseInt(minute), true); 
	case DATE_DIALOG_ID:
	    return new DatePickerDialog(this,
					mDateSetListener,
					Integer.parseInt(year), Integer.parseInt(month)-1, Integer.parseInt(day));
	}
        return null;
    }
       
    private OnClickListener timeEditListener = new OnClickListener() {
	    public void onClick(View v) {
        	showDialog(TIME_DIALOG_ID);
	    }
	};
    
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int h, int m) {
            	isDateTimeUnchanged = false;
                hour = (Integer.toString(h).length()==1) ? "0"+Integer.toString(h) : Integer.toString(h);
                minute = (Integer.toString(m).length()==1) ? "0"+Integer.toString(m) : Integer.toString(m);;
                timeEdit.setText(hour+":"+minute);
	    }
	};
    
    private OnClickListener dateEditListener = new OnClickListener() {
	    public void onClick(View v) {
        	showDialog(DATE_DIALOG_ID);        	
	    }
	};
            
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int y, int m, int d) {
            	isDateTimeUnchanged = false;
            	// January is month 0
            	m++;
                year = Integer.toString(y);
                month = (Integer.toString(m).length()==1) ? "0"+Integer.toString(m) : Integer.toString(m);
                day = (Integer.toString(d).length()==1) ? "0"+Integer.toString(d) : Integer.toString(d);
                dateEdit.setText(day+"."+month+"."+year);
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
	    if (!mFrom.getHint().equals(getString(R.string.maEditFromHint)) && !mFrom.getHint().equals("")) f = mFrom.getHint().toString();
	    //Log.i(TAG, "sWAP: "+fromEditText.getText().toString().equals("")+"  "+(!fromEditText.getHint().equals("From") && !fromEditText.getHint().equals(""))+" "+f);
	    if (!mFrom.getText().toString().equals("")) f = mFrom.getText().toString();
	    if (!toEditText.getText().toString().equals("")) t = toEditText.getText().toString();
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
			setCurrentDateTime();
			updateSettings(false);
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


/* This is the right way to display lists in dialogs!
 *  final String[] PENS = new String[]{
 "MONT Blanc",
 "Gucci",
 "Parker",
 "Sailor",
 "Porsche Design",
 "Rotring",
 "Sheaffer",
 "Waterman"
 };

 AlertDialog.Builder builder = new AlertDialog.Builder(this);
 builder.setTitle("Pick a color").setView(arg0)
 .setItems(PENS, new DialogInterface.OnClickListener() {
		
 @Override
 public void onClick(DialogInterface dialog, int which) {
 // TODO Auto-generated method stub
			
 }
 });   
 builder.show();	
 MainApp.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myPlacesList));

 final LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

 ArrayList<HistoryItem> history = History.getHistory(this);
 myPlaces.setAdapter(
 new ArrayAdapter<String>(this, android.R.layout.two_line_list_item, PENS) {
 @Override
 public View getView(int position, View convertView, ViewGroup parent) {
 View row;
     
 if (null == convertView) {
 row = mInflater.inflate(android.R.layout.two_line_list_item, null);
 } else {
 row = convertView;
 }
    		
    		
    		
 TextView tv = (TextView) row.findViewById(android.R.id.text1);
 tv.setText(getItem(position));

 TextView tv = (TextView) row.findViewById(android.R.id.text2);
 tv.setText(getItem(position));
    		
 return row;
 }
 }
 );*/  